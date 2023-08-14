/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.storage.obs;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.internal.ServiceException;
import com.obs.services.model.DeleteObjectsRequest;
import com.obs.services.model.GetObjectRequest;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

@Data
@Slf4j
public class ObsStorageOperator implements Closeable, StorageOperate {

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    private String endPoint;

    private ObsClient obsClient;

    public ObsStorageOperator() {
    }

    public void init() {
        this.accessKeyId = readObsAccessKeyID();
        this.accessKeySecret = readObsAccessKeySecret();
        this.endPoint = readObsEndPoint();
        this.bucketName = readObsBucketName();
        this.obsClient = buildObsClient();
        ensureBucketSuccessfullyCreated(bucketName);
    }

    protected String readObsAccessKeyID() {
        return PropertyUtils.getString(TaskConstants.HUAWEI_CLOUD_ACCESS_KEY_ID);
    }

    protected String readObsAccessKeySecret() {
        return PropertyUtils.getString(TaskConstants.HUAWEI_CLOUD_ACCESS_KEY_SECRET);
    }

    protected String readObsBucketName() {
        return PropertyUtils.getString(Constants.HUAWEI_CLOUD_OBS_BUCKET_NAME);
    }

    protected String readObsEndPoint() {
        return PropertyUtils.getString(Constants.HUAWEI_CLOUD_OBS_END_POINT);
    }

    @Override
    public void close() throws IOException {
        obsClient.close();
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        mkdir(tenantCode, getObsResDir(tenantCode));
        mkdir(tenantCode, getObsUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getObsResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getObsUdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        final String key = path + FOLDER_SEPARATOR;
        if (!obsClient.doesObjectExist(bucketName, key)) {
            createObsPrefix(bucketName, key);
        }
        return true;
    }

    protected void createObsPrefix(final String bucketName, final String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0L);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, emptyContent);
        obsClient.putObject(putObjectRequest);
    }

    @Override
    public String getResourceFullName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getObsResDir(tenantCode), fileName);
    }

    @Override
    public String getResourceFileName(String tenantCode, String fullName) {
        String resDir = getResDir(tenantCode);
        return fullName.replaceFirst(resDir, "");
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return getDir(resourceType, tenantCode) + fileName;
    }

    @Override
    public boolean delete(String fullName, List<String> childrenPathList, boolean recursive) throws IOException {
        // append the resource fullName to the list for deletion.
        childrenPathList.add(fullName);

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
        for (String deleteKys : childrenPathList) {
            deleteObjectsRequest.addKeyAndVersion(deleteKys);
        }

        try {
            obsClient.deleteObjects(deleteObjectsRequest);
        } catch (Exception e) {
            log.error("delete objects error", e);
            return false;
        }

        return true;
    }

    @Override
    public void download(String tenantCode, String srcFilePath, String dstFilePath,
                         boolean overwrite) throws IOException {
        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            Files.createDirectories(dstFile.getParentFile().toPath());
        }
        ObsObject obsObject = obsClient.getObject(bucketName, srcFilePath);
        try (
                InputStream obsInputStream = obsObject.getObjectContent();
                FileOutputStream fos = new FileOutputStream(dstFilePath)) {
            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = obsInputStream.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        } catch (ObsException e) {
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            log.error("cannot find the destination file {}", dstFilePath);
            throw e;
        }
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return obsClient.doesObjectExist(bucketName, fileName);
    }

    @Override
    public boolean delete(String filePath, boolean recursive) throws IOException {
        try {
            obsClient.deleteObject(bucketName, filePath);
            return true;
        } catch (ObsException e) {
            log.error("fail to delete the object, the resource path is {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        obsClient.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            obsClient.deleteObject(bucketName, srcPath);
        }
        return true;
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        switch (resourceType) {
            case UDF:
                return getUdfDir(tenantCode);
            case FILE:
                return getResDir(tenantCode);
            case ALL:
                return getObsDataBasePath();
            default:
                return "";
        }
    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            obsClient.putObject(bucketName, dstPath, new File(srcFile));
            if (deleteSource) {
                Files.delete(Paths.get(srcFile));
            }
            return true;
        } catch (ObsException e) {
            log.error("upload failed, the bucketName is {}, the filePath is {}", bucketName, dstPath, e);
            return false;
        }
    }

    @Override
    public List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            log.error("file path:{} is empty", filePath);
            return Collections.emptyList();
        }
        ObsObject obsObject = obsClient.getObject(bucketName, filePath);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(obsObject.getObjectContent()))) {
            Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.OBS;
    }

    @Override
    public List<StorageEntity> listFilesStatusRecursively(String path, String defaultPath, String tenantCode,
                                                          ResourceType type) {
        List<StorageEntity> storageEntityList = new ArrayList<>();
        LinkedList<StorageEntity> foldersToFetch = new LinkedList<>();

        StorageEntity initialEntity = null;
        try {
            initialEntity = getFileStatus(path, defaultPath, tenantCode, type);
        } catch (Exception e) {
            log.error("error while listing files status recursively, path: {}", path, e);
            return storageEntityList;
        }
        foldersToFetch.add(initialEntity);

        while (!foldersToFetch.isEmpty()) {
            String pathToExplore = foldersToFetch.pop().getFullName();
            try {
                List<StorageEntity> tempList = listFilesStatus(pathToExplore, defaultPath, tenantCode, type);
                for (StorageEntity temp : tempList) {
                    if (temp.isDirectory()) {
                        foldersToFetch.add(temp);
                    }
                }
                storageEntityList.addAll(tempList);
            } catch (Exception e) {
                log.error("error while listing files stat:wus recursively, path: {}", pathToExplore, e);
            }
        }

        return storageEntityList;
    }

    @Override
    public List<StorageEntity> listFilesStatus(String path, String defaultPath, String tenantCode,
                                               ResourceType type) throws Exception {
        List<StorageEntity> storageEntityList = new ArrayList<>();

        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setDelimiter(FOLDER_SEPARATOR);
        ObjectListing result = null;
        try {
            result = obsClient.listObjects(request);
        } catch (Exception e) {
            throw new ServiceException("Get ObsClient file list exception", e);
        }

        while (result != null) {
            String nextMarker = result.getNextMarker();
            List<ObsObject> objects = result.getObjects();

            for (ObsObject object : objects) {
                if (!object.getObjectKey().endsWith(FOLDER_SEPARATOR)) {
                    // the path is a file
                    String[] aliasArr = object.getObjectKey().split(FOLDER_SEPARATOR);
                    String alias = aliasArr[aliasArr.length - 1];
                    String fileName = StringUtils.difference(defaultPath, object.getObjectKey());

                    StorageEntity entity = new StorageEntity();
                    ObjectMetadata metadata = object.getMetadata();
                    entity.setAlias(alias);
                    entity.setFileName(fileName);
                    entity.setFullName(object.getObjectKey());
                    entity.setDirectory(false);
                    entity.setUserName(tenantCode);
                    entity.setType(type);
                    entity.setSize(metadata.getContentLength());
                    entity.setCreateTime(metadata.getLastModified());
                    entity.setUpdateTime(metadata.getLastModified());
                    entity.setPfullName(path);

                    storageEntityList.add(entity);
                }
            }

            for (String commonPrefix : result.getCommonPrefixes()) {
                // the paths in commonPrefix are directories
                String suffix = StringUtils.difference(path, commonPrefix);
                String fileName = StringUtils.difference(defaultPath, commonPrefix);

                StorageEntity entity = new StorageEntity();
                entity.setAlias(suffix);
                entity.setFileName(fileName);
                entity.setFullName(commonPrefix);
                entity.setDirectory(true);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(0);
                entity.setCreateTime(null);
                entity.setUpdateTime(null);
                entity.setPfullName(path);

                storageEntityList.add(entity);
            }

            if (!StringUtils.isNotBlank(nextMarker)) {
                break;
            }

            request.setMarker(nextMarker);
            try {
                result = obsClient.listObjects(request);
            } catch (Exception e) {
                throw new ServiceException("Get ObsClient file list exception", e);
            }
        }
        return storageEntityList;
    }

    @Override
    public StorageEntity getFileStatus(String path, String defaultPath, String tenantCode,
                                       ResourceType type) throws Exception {

        if (path.endsWith(FOLDER_SEPARATOR)) {
            // the path is a directory that may or may not exist in ObsClient
            String alias = findDirAlias(path);
            String fileName = StringUtils.difference(defaultPath, path);

            StorageEntity entity = new StorageEntity();
            entity.setAlias(alias);
            entity.setFileName(fileName);
            entity.setFullName(path);
            entity.setDirectory(true);
            entity.setUserName(tenantCode);
            entity.setType(type);
            entity.setSize(0);

            return entity;

        } else {
            GetObjectRequest request = new GetObjectRequest();
            request.setBucketName(bucketName);
            request.setObjectKey(path);
            ObsObject object;
            try {
                object = obsClient.getObject(request);
            } catch (Exception e) {
                throw new ServiceException("Get ObsClient file list exception", e);
            }

            String[] aliasArr = object.getObjectKey().split(FOLDER_SEPARATOR);
            String alias = aliasArr[aliasArr.length - 1];
            String fileName = StringUtils.difference(defaultPath, object.getObjectKey());

            StorageEntity entity = new StorageEntity();
            ObjectMetadata metadata = object.getMetadata();
            entity.setAlias(alias);
            entity.setFileName(fileName);
            entity.setFullName(object.getObjectKey());
            entity.setDirectory(false);
            entity.setUserName(tenantCode);
            entity.setType(type);
            entity.setSize(metadata.getContentLength());
            entity.setCreateTime(metadata.getLastModified());
            entity.setUpdateTime(metadata.getLastModified());

            return entity;
        }

    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        deleteTenantCode(tenantCode);
    }

    public String getObsResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getObsTenantDir(tenantCode));
    }

    public String getObsUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getObsTenantDir(tenantCode));
    }

    public String getObsTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getObsDataBasePath(), tenantCode);
    }

    public String getObsDataBasePath() {
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return "";
        } else {
            return RESOURCE_UPLOAD_PATH.replaceFirst(FOLDER_SEPARATOR, "");
        }
    }

    protected void deleteTenantCode(String tenantCode) {
        deleteDir(getResDir(tenantCode));
        deleteDir(getUdfDir(tenantCode));
    }

    public void ensureBucketSuccessfullyCreated(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("resource.alibaba.cloud.obs.bucket.name is empty");
        }

        boolean existsBucket = obsClient.headBucket(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found", bucketName);
    }

    protected void deleteDir(String directoryName) {
        if (obsClient.doesObjectExist(bucketName, directoryName)) {
            obsClient.deleteObject(bucketName, directoryName);
        }
    }

    protected ObsClient buildObsClient() {
        return new ObsClient(accessKeyId, accessKeySecret, endPoint);
    }

    private String findDirAlias(String dirPath) {
        if (!dirPath.endsWith(FOLDER_SEPARATOR)) {
            return dirPath;
        }

        Path path = Paths.get(dirPath);
        return path.getName(path.getNameCount() - 1) + FOLDER_SEPARATOR;
    }
}
