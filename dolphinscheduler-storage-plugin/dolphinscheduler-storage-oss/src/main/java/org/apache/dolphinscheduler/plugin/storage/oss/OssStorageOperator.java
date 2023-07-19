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

package org.apache.dolphinscheduler.plugin.storage.oss;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.factory.OssClientFactory;
import org.apache.dolphinscheduler.common.model.OssConnection;
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

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ServiceException;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;

@Data
@Slf4j
public class OssStorageOperator implements Closeable, StorageOperate {

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private String bucketName;

    private String endPoint;

    private OssConnection ossConnection;

    private OSS ossClient;

    public OssStorageOperator() {
    }

    public void init() {
        this.accessKeyId = readOssAccessKeyID();
        this.accessKeySecret = readOssAccessKeySecret();
        this.endPoint = readOssEndPoint();
        this.region = readOssRegion();
        this.bucketName = readOssBucketName();
        this.ossConnection = buildOssConnection();
        this.ossClient = buildOssClient();
        ensureBucketSuccessfullyCreated(bucketName);
    }

    // TODO: change to use the following init method after DS supports Configuration / Connection Center
    public void init(OssConnection ossConnection) {
        this.accessKeyId = readOssAccessKeyID();
        this.accessKeySecret = readOssAccessKeySecret();
        this.endPoint = readOssEndPoint();
        this.region = readOssRegion();
        this.bucketName = readOssBucketName();
        this.ossConnection = ossConnection;
        this.ossClient = getOssClient();
        ensureBucketSuccessfullyCreated(bucketName);
    }

    protected String readOssAccessKeyID() {
        return PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_ID);
    }

    protected String readOssAccessKeySecret() {
        return PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_SECRET);
    }

    protected String readOssRegion() {
        return PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_REGION);
    }

    protected String readOssBucketName() {
        return PropertyUtils.getString(Constants.ALIBABA_CLOUD_OSS_BUCKET_NAME);
    }

    protected String readOssEndPoint() {
        return PropertyUtils.getString(Constants.ALIBABA_CLOUD_OSS_END_POINT);
    }

    protected OssConnection buildOssConnection() {
        return new OssConnection(accessKeyId, accessKeySecret, endPoint);
    }

    @Override
    public void close() throws IOException {
        ossClient.shutdown();
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        mkdir(tenantCode, getOssResDir(tenantCode));
        mkdir(tenantCode, getOssUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getOssResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getOssUdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        final String key = path + FOLDER_SEPARATOR;
        if (!ossClient.doesObjectExist(bucketName, key)) {
            createOssPrefix(bucketName, key);
        }
        return true;
    }

    protected void createOssPrefix(final String bucketName, final String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, emptyContent, metadata);
        ossClient.putObject(putObjectRequest);
    }

    @Override
    public String getResourceFullName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getOssResDir(tenantCode), fileName);
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

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(childrenPathList);
        try {
            ossClient.deleteObjects(deleteObjectsRequest);
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
        OSSObject ossObject = ossClient.getObject(bucketName, srcFilePath);
        try (
                InputStream ossInputStream = ossObject.getObjectContent();
                FileOutputStream fos = new FileOutputStream(dstFilePath)) {
            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = ossInputStream.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        } catch (OSSException e) {
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            log.error("cannot find the destination file {}", dstFilePath);
            throw e;
        }
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return ossClient.doesObjectExist(bucketName, fileName);
    }

    @Override
    public boolean delete(String filePath, boolean recursive) throws IOException {
        try {
            ossClient.deleteObject(bucketName, filePath);
            return true;
        } catch (OSSException e) {
            log.error("fail to delete the object, the resource path is {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        ossClient.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            ossClient.deleteObject(bucketName, srcPath);
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
                return getOssDataBasePath();
            default:
                return "";
        }
    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            ossClient.putObject(bucketName, dstPath, new File(srcFile));
            if (deleteSource) {
                Files.delete(Paths.get(srcFile));
            }
            return true;
        } catch (OSSException e) {
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
        OSSObject ossObject = ossClient.getObject(bucketName, filePath);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()))) {
            Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.OSS;
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

        ListObjectsV2Result result = null;
        String nextContinuationToken = null;
        do {
            try {
                ListObjectsV2Request request = new ListObjectsV2Request();
                request.setBucketName(bucketName);
                request.setPrefix(path);
                request.setDelimiter(FOLDER_SEPARATOR);
                request.setContinuationToken(nextContinuationToken);

                result = ossClient.listObjectsV2(request);
            } catch (Exception e) {
                throw new ServiceException("Get OSS file list exception", e);
            }

            List<OSSObjectSummary> summaries = result.getObjectSummaries();

            for (OSSObjectSummary summary : summaries) {
                if (!summary.getKey().endsWith(FOLDER_SEPARATOR)) {
                    // the path is a file
                    String[] aliasArr = summary.getKey().split(FOLDER_SEPARATOR);
                    String alias = aliasArr[aliasArr.length - 1];
                    String fileName = StringUtils.difference(defaultPath, summary.getKey());

                    StorageEntity entity = new StorageEntity();
                    entity.setAlias(alias);
                    entity.setFileName(fileName);
                    entity.setFullName(summary.getKey());
                    entity.setDirectory(false);
                    entity.setUserName(tenantCode);
                    entity.setType(type);
                    entity.setSize(summary.getSize());
                    entity.setCreateTime(summary.getLastModified());
                    entity.setUpdateTime(summary.getLastModified());
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

            nextContinuationToken = result.getNextContinuationToken();
        } while (result.isTruncated());

        return storageEntityList;
    }

    @Override
    public StorageEntity getFileStatus(String path, String defaultPath, String tenantCode,
                                       ResourceType type) throws Exception {
        ListObjectsV2Request request = new ListObjectsV2Request();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setDelimiter(FOLDER_SEPARATOR);

        ListObjectsV2Result result;
        try {
            result = ossClient.listObjectsV2(request);
        } catch (Exception e) {
            throw new ServiceException("Get OSS file list exception", e);
        }

        List<OSSObjectSummary> summaries = result.getObjectSummaries();

        if (path.endsWith(FOLDER_SEPARATOR)) {
            // the path is a directory that may or may not exist in OSS
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
            // the path is a file
            if (summaries.size() > 0) {
                OSSObjectSummary summary = summaries.get(0);
                String[] aliasArr = summary.getKey().split(FOLDER_SEPARATOR);
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, summary.getKey());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(summary.getKey());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(summary.getSize());
                entity.setCreateTime(summary.getLastModified());
                entity.setUpdateTime(summary.getLastModified());

                return entity;
            }
        }

        throw new FileNotFoundException("Object is not found in OSS Bucket: " + bucketName);
    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        deleteTenantCode(tenantCode);
    }

    public String getOssResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getOssTenantDir(tenantCode));
    }

    public String getOssUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getOssTenantDir(tenantCode));
    }

    public String getOssTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getOssDataBasePath(), tenantCode);
    }

    public String getOssDataBasePath() {
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
            throw new IllegalArgumentException("resource.alibaba.cloud.oss.bucket.name is empty");
        }

        boolean existsBucket = ossClient.doesBucketExist(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found, the current regionName is {}", bucketName, region);
    }

    protected void deleteDir(String directoryName) {
        if (ossClient.doesObjectExist(bucketName, directoryName)) {
            ossClient.deleteObject(bucketName, directoryName);
        }
    }

    protected OSS buildOssClient() {
        return OssClientFactory.buildOssClient(ossConnection);
    }

    private String findDirAlias(String dirPath) {
        if (!dirPath.endsWith(FOLDER_SEPARATOR)) {
            return dirPath;
        }

        Path path = Paths.get(dirPath);
        return path.getName(path.getNameCount() - 1) + FOLDER_SEPARATOR;
    }
}
