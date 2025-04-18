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

package org.apache.dolphinscheduler.plugin.storage.s3;

import org.apache.dolphinscheduler.authentication.aws.AmazonS3ClientFactory;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Slf4j
public class S3StorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final String bucketName;

    private final AmazonS3 s3Client;

    public S3StorageOperator(S3StorageProperties s3StorageProperties) {
        super(s3StorageProperties.getResourceUploadPath());
        bucketName = s3StorageProperties.getBucketName();
        s3Client = AmazonS3ClientFactory.createAmazonS3Client(s3StorageProperties.getS3Configuration());
        exceptionWhenBucketNameNotExists(bucketName);
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith("/")) {
            log.warn("{} -> {} should not start with / in s3", StorageConstants.RESOURCE_UPLOAD_PATH,
                    resourceBaseAbsolutePath);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @Override
    public void close() throws IOException {
        s3Client.shutdown();
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        directoryAbsolutePath = transformAbsolutePathToS3Key(directoryAbsolutePath);
        if (s3Client.doesObjectExist(bucketName, directoryAbsolutePath)) {
            throw new FileAlreadyExistsException(
                    "The directory " + directoryAbsolutePath + " already exists in the bucket " + bucketName);
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(bucketName, directoryAbsolutePath, emptyContent, metadata);
        s3Client.putObject(putObjectRequest);
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath,
                         String dstFilePath,
                         boolean overwrite) {
        srcFilePath = transformAbsolutePathToS3Key(srcFilePath);
        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
        }
        S3Object o = s3Client.getObject(bucketName, srcFilePath);
        try (
                S3ObjectInputStream s3is = o.getObjectContent();
                FileOutputStream fos = new FileOutputStream(dstFilePath)) {
            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = s3is.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        }
    }

    @Override
    public boolean exists(String fullName) {
        fullName = transformAbsolutePathToS3Key(fullName);
        return s3Client.doesObjectExist(bucketName, fullName);
    }

    @Override
    public void delete(String absolutePath, boolean recursive) {
        absolutePath = transformAbsolutePathToS3Key(absolutePath);
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (!resourceMetaData.isDirectory()) {
            s3Client.deleteObject(bucketName, absolutePath);
            return;
        }
        if (recursive) {
            List<StorageEntity> storageEntities = listStorageEntityRecursively(absolutePath);
            for (StorageEntity storageEntity : storageEntities) {
                s3Client.deleteObject(bucketName, transformAbsolutePathToS3Key(storageEntity.getFullName()));
            }
        }
        s3Client.deleteObject(bucketName, absolutePath);
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        srcPath = transformAbsolutePathToS3Key(srcPath);
        dstPath = transformAbsolutePathToS3Key(dstPath);

        ResourceMetadata resourceMetaData = getResourceMetaData(srcPath);
        if (resourceMetaData.isDirectory()) {
            throw new UnsupportedOperationException("S3 does not support copying directories.");
        }
        s3Client.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            s3Client.deleteObject(bucketName, srcPath);
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        dstPath = transformAbsolutePathToS3Key(dstPath);

        if (s3Client.doesObjectExist(bucketName, dstPath)) {
            if (overwrite) {
                s3Client.deleteObject(bucketName, dstPath);
            } else {
                throw new FileAlreadyExistsException("The file " + dstPath + " already exists in the bucket "
                        + bucketName + " and overwrite is not allowed.");
            }
        }

        s3Client.putObject(bucketName, dstPath, new File(srcFile));

        if (deleteSource) {
            Files.delete(Paths.get(srcFile));
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        filePath = transformAbsolutePathToS3Key(filePath);
        S3Object s3Object = s3Client.getObject(bucketName, filePath);
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(s3Object.getObjectContent());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    void exceptionWhenBucketNameNotExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("resource.aws.s3.bucket.name is blank");
        }

        boolean existsBucket = s3Client.doesBucketExistV2(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found, the current regionName is {}", bucketName,
                s3Client.getRegionName());
    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        final String s3ResourceAbsolutePath = transformAbsolutePathToS3Key(resourceAbsolutePath);
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withDelimiter("/")
                .withPrefix(s3ResourceAbsolutePath);

        ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(listObjectsV2Request);
        List<StorageEntity> storageEntities = new ArrayList<>();
        storageEntities.addAll(listObjectsV2Result.getCommonPrefixes()
                .stream()
                .map(this::transformCommonPrefixToStorageEntity)
                .collect(Collectors.toList()));
        storageEntities.addAll(
                listObjectsV2Result.getObjectSummaries().stream()
                        .filter(s3ObjectSummary -> !s3ObjectSummary.getKey().equals(s3ResourceAbsolutePath))
                        .map(this::transformS3ObjectToStorageEntity)
                        .collect(Collectors.toList()));

        return storageEntities;
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        return listStorageEntityRecursively(resourceAbsolutePath)
                .stream()
                .filter(storageEntity -> !storageEntity.isDirectory())
                .collect(Collectors.toList());
    }

    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsolutePathToS3Key(resourceAbsolutePath);

        S3Object object = s3Client.getObject(bucketName, resourceAbsolutePath);
        return transformS3ObjectToStorageEntity(object);
    }

    private List<StorageEntity> listStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsolutePathToS3Key(resourceAbsolutePath);

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

    private StorageEntity transformS3ObjectToStorageEntity(S3Object object) {

        String s3Key = object.getKey();
        String absolutePath = transformS3KeyToAbsolutePath(s3Key);

        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);

        StorageEntity entity = new StorageEntity();
        entity.setFileName(new File(absolutePath).getName());
        entity.setFullName(absolutePath);
        entity.setDirectory(resourceMetaData.isDirectory());
        entity.setType(resourceMetaData.getResourceType());
        entity.setSize(object.getObjectMetadata().getContentLength());
        entity.setRelativePath(resourceMetaData.getResourceRelativePath());
        entity.setCreateTime(object.getObjectMetadata().getLastModified());
        entity.setUpdateTime(object.getObjectMetadata().getLastModified());
        return entity;
    }

    private StorageEntity transformCommonPrefixToStorageEntity(String commonPrefix) {
        String absolutePath = transformS3KeyToAbsolutePath(commonPrefix);

        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);

        StorageEntity entity = new StorageEntity();
        entity.setFileName(new File(absolutePath).getName());
        entity.setFullName(absolutePath);
        entity.setDirectory(resourceMetaData.isDirectory());
        entity.setType(resourceMetaData.getResourceType());
        entity.setSize(0L);
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
        return entity;
    }

    private StorageEntity transformS3ObjectToStorageEntity(S3ObjectSummary s3ObjectSummary) {
        String absolutePath = transformS3KeyToAbsolutePath(s3ObjectSummary.getKey());

        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);

        StorageEntity entity = new StorageEntity();
        entity.setFileName(new File(absolutePath).getName());
        entity.setFullName(absolutePath);
        entity.setPfullName(resourceMetaData.getResourceParentAbsolutePath());
        entity.setDirectory(resourceMetaData.isDirectory());
        entity.setType(resourceMetaData.getResourceType());
        entity.setSize(s3ObjectSummary.getSize());
        entity.setCreateTime(s3ObjectSummary.getLastModified());
        entity.setUpdateTime(s3ObjectSummary.getLastModified());
        return entity;
    }

    private String transformAbsolutePathToS3Key(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (resourceMetaData.isDirectory()) {
            return FileUtils.concatFilePath(absolutePath, "/");
        }
        return absolutePath;
    }

    private String transformS3KeyToAbsolutePath(String s3Key) {
        if (s3Key.endsWith("/")) {
            return s3Key.substring(0, s3Key.length() - 1);
        }
        return s3Key;
    }

}
