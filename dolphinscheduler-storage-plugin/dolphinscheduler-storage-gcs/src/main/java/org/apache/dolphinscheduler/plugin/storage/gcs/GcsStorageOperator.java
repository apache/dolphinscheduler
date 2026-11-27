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

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.storage.api.AbstractStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Slf4j
public class GcsStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final Storage gcsStorage;

    private final String bucketName;

    @SneakyThrows
    public GcsStorageOperator(GcsStorageProperties gcsStorageProperties) {
        super(gcsStorageProperties.getResourceUploadPath());
        bucketName = gcsStorageProperties.getBucketName();
        gcsStorage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        Files.newInputStream(Paths.get(gcsStorageProperties.getCredential()))))
                .build()
                .getService();

        checkBucketNameExists(bucketName);
    }

    // VisibleForTesting
    GcsStorageOperator(String resourceUploadPath, String bucketName, Storage gcsStorage) {
        super(resourceUploadPath);
        this.bucketName = bucketName;
        this.gcsStorage = gcsStorage;
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith("/")) {
            log.warn("{} -> {} should not start with / in Gcs", StorageConstants.RESOURCE_UPLOAD_PATH,
                    resourceBaseAbsolutePath);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        if (exists(directoryAbsolutePath)) {
            throw new FileAlreadyExistsException("directory: " + directoryAbsolutePath + " already exists");
        }
        String gcsKey = transformAbsolutePathToGcsKey(directoryAbsolutePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, gcsKey)).build();
        gcsStorage.create(blobInfo, EMPTY_STRING.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath, String dstFilePath, boolean overwrite) {
        srcFilePath = transformAbsolutePathToGcsKey(srcFilePath);

        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
        }

        Blob blob = gcsStorage.get(BlobId.of(bucketName, srcFilePath));
        blob.downloadTo(Paths.get(dstFilePath));
    }

    @Override
    public boolean exists(String fullName) {
        fullName = transformAbsolutePathToGcsKey(fullName);
        Blob blob = gcsStorage.get(BlobId.of(bucketName, fullName));
        return blob != null && blob.exists();
    }

    @Override
    public boolean existsForRecursiveDelete(String resourceAbsolutePath) {
        String gcsKey = transformAbsolutePathToGcsKey(resourceAbsolutePath);
        Blob blob = gcsStorage.get(BlobId.of(bucketName, gcsKey));
        if (blob != null && blob.exists()) {
            return true;
        }
        if (!getResourceMetaData(resourceAbsolutePath).isDirectory()) {
            return false;
        }
        Page<Blob> blobs = gcsStorage.list(bucketName, Storage.BlobListOption.prefix(gcsKey),
                Storage.BlobListOption.pageSize(1));
        return blobs.iterateAll().iterator().hasNext();
    }

    @SneakyThrows
    @Override
    public void delete(String filePath, boolean recursive) {
        String gcsKey = transformAbsolutePathToGcsKey(filePath);
        ResourceMetadata resourceMetaData = getResourceMetaData(filePath);
        if (!resourceMetaData.isDirectory()) {
            if (exists(filePath)) {
                gcsStorage.delete(BlobId.of(bucketName, gcsKey));
            }
            return;
        }

        if (recursive) {
            Page<Blob> blobs = gcsStorage.list(bucketName, Storage.BlobListOption.prefix(gcsKey),
                    Storage.BlobListOption.pageSize(1000));
            List<BlobId> batch = new ArrayList<>();
            List<String> failedKeys = new ArrayList<>();
            for (Blob blob : blobs.iterateAll()) {
                batch.add(blob.getBlobId());
                if (batch.size() >= 100) {
                    deleteBatchAndRecordFailures(batch, failedKeys);
                    batch = new ArrayList<>();
                }
            }
            if (!batch.isEmpty()) {
                deleteBatchAndRecordFailures(batch, failedKeys);
            }
            if (!failedKeys.isEmpty()) {
                throw new IllegalStateException("GCS failed to delete objects: " + failedKeys);
            }
        } else {
            if (exists(filePath)) {
                gcsStorage.delete(BlobId.of(bucketName, gcsKey));
            }
        }
    }

    private void deleteBatchAndRecordFailures(List<BlobId> batch, List<String> failedKeys) {
        List<Boolean> results = gcsStorage.delete(batch);
        if (results == null) {
            batch.forEach(blobId -> failedKeys.add(blobId.getName()));
            return;
        }
        int checkedCount = Math.min(batch.size(), results.size());
        for (int i = 0; i < checkedCount; i++) {
            if (!Boolean.TRUE.equals(results.get(i))) {
                failedKeys.add(batch.get(i).getName());
            }
        }
        if (results.size() < batch.size()) {
            for (int i = results.size(); i < batch.size(); i++) {
                failedKeys.add(batch.get(i).getName());
            }
        } else if (results.size() > batch.size()) {
            throw new IllegalStateException(
                    "Unexpected GCS batch delete result count: expected " + batch.size() + ", got " + results.size());
        }
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        srcPath = transformGcsKeyToAbsolutePath(srcPath);
        dstPath = transformGcsKeyToAbsolutePath(dstPath);

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
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        if (exists(dstPath) && !overwrite) {
            throw new FileAlreadyExistsException("file: " + dstPath + " already exists");
        }
        String gcsKey = transformAbsolutePathToGcsKey(dstPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(bucketName, gcsKey)).build();

        Path srcPath = Paths.get(srcFile);
        gcsStorage.create(blobInfo, Files.readAllBytes(srcPath));

        if (deleteSource) {
            Files.delete(srcPath);
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        filePath = transformAbsolutePathToGcsKey(filePath);
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

    @SneakyThrows
    @Override
    public void close() throws IOException {
        if (gcsStorage != null) {
            gcsStorage.close();
        }
    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsolutePathToGcsKey(resourceAbsolutePath);

        Page<Blob> blobs = gcsStorage.list(bucketName, Storage.BlobListOption.prefix(resourceAbsolutePath));
        List<StorageEntity> storageEntities = new ArrayList<>();
        blobs.iterateAll().forEach(blob -> storageEntities.add(transformBlobToStorageEntity(blob)));
        return storageEntities;
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsolutePathToGcsKey(resourceAbsolutePath);

        Set<String> visited = new HashSet<>();
        List<StorageEntity> storageEntityList = new ArrayList<>();
        LinkedList<String> foldersToFetch = new LinkedList<>();
        foldersToFetch.addLast(resourceAbsolutePath);

        while (!foldersToFetch.isEmpty()) {
            String pathToExplore = foldersToFetch.pop();
            visited.add(pathToExplore);
            List<StorageEntity> tempList = listStorageEntity(pathToExplore);
            for (StorageEntity temp : tempList) {
                if (temp.isDirectory()) {
                    if (visited.contains(temp.getFullName())) {
                        continue;
                    }
                    foldersToFetch.add(temp.getFullName());
                }
            }
            storageEntityList.addAll(tempList);
        }
        return storageEntityList;
    }

    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsolutePathToGcsKey(resourceAbsolutePath);
        Blob blob = gcsStorage.get(BlobId.of(bucketName, resourceAbsolutePath));
        return transformBlobToStorageEntity(blob);
    }

    private void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(StorageConstants.GOOGLE_CLOUD_STORAGE_BUCKET_NAME + " is blank");
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

    private StorageEntity transformBlobToStorageEntity(Blob blob) {
        String absolutePath = transformGcsKeyToAbsolutePath(blob.getName());

        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);

        StorageEntity entity = new StorageEntity();
        entity.setFileName(new File(absolutePath).getName());
        entity.setFullName(absolutePath);
        entity.setDirectory(resourceMetaData.isDirectory());
        entity.setType(resourceMetaData.getResourceType());
        entity.setSize(blob.getSize());
        entity.setRelativePath(resourceMetaData.getResourceRelativePath());
        entity.setCreateTime(Date.from(blob.getCreateTimeOffsetDateTime().toInstant()));
        entity.setUpdateTime(Date.from(blob.getUpdateTimeOffsetDateTime().toInstant()));
        return entity;
    }

    private String transformAbsolutePathToGcsKey(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        String gcsKey = absolutePath.replace(File.separatorChar, '/');
        if (resourceMetaData.isDirectory() && !gcsKey.endsWith("/")) {
            return gcsKey + "/";
        }
        return gcsKey;
    }

    private String transformGcsKeyToAbsolutePath(String gcsKey) {
        if (gcsKey.endsWith("/")) {
            return gcsKey.substring(0, gcsKey.length() - 1);
        }
        return gcsKey;
    }

}
