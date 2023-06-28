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

package org.apache.dolphinscheduler.plugin.storage.gcs;

import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Data
@Slf4j
public class GcsStorageOperator implements Closeable, StorageOperate {

    private Storage gcsStorage;

    private String bucketName;

    private String credential;

    public GcsStorageOperator() {

    }

    public void init() {
        try {
            credential = readCredentials();
            bucketName = readBucketName();
            gcsStorage = buildGcsStorage(credential);

            checkBucketNameExists(bucketName);
        } catch (IOException e) {
            log.error("GCS Storage operator init failed", e);
        }
    }

    protected Storage buildGcsStorage(String credential) throws IOException {
        return StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        Files.newInputStream(Paths.get(credential))))
                .build()
                .getService();
    }

    protected String readCredentials() {
        return PropertyUtils.getString(Constants.GOOGLE_CLOUD_STORAGE_CREDENTIAL);
    }

    protected String readBucketName() {
        return PropertyUtils.getString(Constants.GOOGLE_CLOUD_STORAGE_BUCKET_NAME);
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        mkdir(tenantCode, getGcsResDir(tenantCode));
        mkdir(tenantCode, getGcsUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getGcsResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getGcsUdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getResourceFullName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName.replaceFirst(FOLDER_SEPARATOR, EMPTY_STRING);
        }
        return String.format(FORMAT_S_S, getGcsResDir(tenantCode), fileName);
    }

    @Override
    public String getResourceFileName(String tenantCode, String fullName) {
        String resDir = getResDir(tenantCode);
        return fullName.replaceFirst(resDir, "");
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, EMPTY_STRING);
        }
        return getDir(resourceType, tenantCode) + fileName;
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

        Blob blob = gcsStorage.get(BlobId.of(bucketName, srcFilePath));
        blob.downloadTo(Paths.get(dstFilePath));
    }

    @Override
    public boolean exists(String fullName) throws IOException {
        return isObjectExists(fullName);
    }

    @Override
    public boolean delete(String filePath, boolean recursive) throws IOException {
        try {
            if (isObjectExists(filePath)) {
                gcsStorage.delete(BlobId.of(bucketName, filePath));
            }
            return true;
        } catch (Exception e) {
            log.error("delete the object error,the resource path is {}", filePath);
            return false;
        }
    }

    @Override
    public boolean delete(String fullName, List<String> childrenPathList, boolean recursive) throws IOException {
        // append the resource fullName to the list for deletion.
        childrenPathList.add(fullName);

        boolean result = true;
        for (String filePath : childrenPathList) {
            if (!delete(filePath, recursive)) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        BlobId source = BlobId.of(bucketName, srcPath);
        BlobId target = BlobId.of(bucketName, dstPath);

        gcsStorage.copy(
                Storage.CopyRequest.newBuilder()
                        .setSource(source)
                        .setTarget(target)
                        .build());

        if (deleteSource) {
            gcsStorage.delete(source);
        }
        return true;
    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    BlobId.of(bucketName, dstPath)).build();

            Path srcPath = Paths.get(srcFile);
            gcsStorage.create(blobInfo, Files.readAllBytes(srcPath));

            if (deleteSource) {
                Files.delete(srcPath);
            }
            return true;
        } catch (Exception e) {
            log.error("upload failed,the bucketName is {},the filePath is {}", bucketName, dstPath);
            return false;
        }
    }

    @Override
    public List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            log.error("file path:{} is blank", filePath);
            return Collections.emptyList();
        }

        Blob blob = gcsStorage.get(BlobId.of(bucketName, filePath));
        try (
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(blob.getContent())))) {
            Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        deleteTenantCode(tenantCode);
    }

    protected void deleteTenantCode(String tenantCode) {
        deleteDirectory(getResDir(tenantCode));
        deleteDirectory(getUdfDir(tenantCode));
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        switch (resourceType) {
            case UDF:
                return getUdfDir(tenantCode);
            case FILE:
                return getResDir(tenantCode);
            case ALL:
                return getGcsDataBasePath();
            default:
                return EMPTY_STRING;
        }

    }

    protected void deleteDirectory(String directoryName) {
        if (isObjectExists(directoryName)) {
            gcsStorage.delete(BlobId.of(bucketName, directoryName));
        }
    }

    public String getGcsResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getGcsTenantDir(tenantCode));
    }

    public String getGcsUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getGcsTenantDir(tenantCode));
    }

    public String getGcsTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getGcsDataBasePath(), tenantCode);
    }

    public String getGcsDataBasePath() {
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return EMPTY_STRING;
        } else {
            return RESOURCE_UPLOAD_PATH.replaceFirst(FOLDER_SEPARATOR, EMPTY_STRING);
        }
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        String objectName = path + FOLDER_SEPARATOR;
        if (!isObjectExists(objectName)) {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    BlobId.of(bucketName, objectName)).build();

            gcsStorage.create(blobInfo, EMPTY_STRING.getBytes(StandardCharsets.UTF_8));
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        try {
            if (gcsStorage != null) {
                gcsStorage.close();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.GCS;
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

        Page<Blob> blobs;
        try {
            blobs =
                    gcsStorage.list(
                            bucketName,
                            Storage.BlobListOption.prefix(path),
                            Storage.BlobListOption.currentDirectory());
        } catch (Exception e) {
            throw new RuntimeException("Get GCS file list exception. ", e);
        }

        if (blobs == null) {
            return storageEntityList;
        }

        for (Blob blob : blobs.iterateAll()) {
            if (path.equals(blob.getName())) {
                continue;
            }
            if (blob.isDirectory()) {
                String suffix = StringUtils.difference(path, blob.getName());
                String fileName = StringUtils.difference(defaultPath, blob.getName());
                StorageEntity entity = new StorageEntity();
                entity.setAlias(suffix);
                entity.setFileName(fileName);
                entity.setFullName(blob.getName());
                entity.setDirectory(true);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(0);
                entity.setCreateTime(null);
                entity.setUpdateTime(null);
                entity.setPfullName(path);

                storageEntityList.add(entity);
            } else {
                String[] aliasArr = blob.getName().split("/");
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, blob.getName());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(blob.getName());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(blob.getSize());
                entity.setCreateTime(Date.from(blob.getCreateTimeOffsetDateTime().toInstant()));
                entity.setUpdateTime(Date.from(blob.getUpdateTimeOffsetDateTime().toInstant()));
                entity.setPfullName(path);

                storageEntityList.add(entity);
            }
        }

        return storageEntityList;
    }

    @Override
    public StorageEntity getFileStatus(String path, String defaultPath, String tenantCode,
                                       ResourceType type) throws Exception {
        if (path.endsWith(FOLDER_SEPARATOR)) {
            // the path is a directory that may or may not exist
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
            if (isObjectExists(path)) {
                Blob blob = gcsStorage.get(BlobId.of(bucketName, path));

                String[] aliasArr = blob.getName().split(FOLDER_SEPARATOR);
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, blob.getName());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(blob.getName());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(blob.getSize());
                entity.setCreateTime(Date.from(blob.getCreateTimeOffsetDateTime().toInstant()));
                entity.setUpdateTime(Date.from(blob.getUpdateTimeOffsetDateTime().toInstant()));

                return entity;
            } else {
                throw new FileNotFoundException("Object is not found in GCS Bucket: " + bucketName);
            }
        }
    }

    protected boolean isObjectExists(String objectName) {
        Blob blob = gcsStorage.get(BlobId.of(bucketName, objectName));
        return blob != null && blob.exists();
    }

    public void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(Constants.GOOGLE_CLOUD_STORAGE_BUCKET_NAME + " is blank");
        }

        boolean exist = false;
        for (Bucket bucket : gcsStorage.list().iterateAll()) {
            if (bucketName.equals(bucket.getName())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        } else {
            log.info("bucketName: {} has been found", bucketName);
        }
    }

    private String findDirAlias(String dirPath) {
        if (!dirPath.endsWith(FOLDER_SEPARATOR)) {
            return dirPath;
        }

        Path path = Paths.get(dirPath);
        return path.getName(path.getNameCount() - 1) + FOLDER_SEPARATOR;
    }
}
