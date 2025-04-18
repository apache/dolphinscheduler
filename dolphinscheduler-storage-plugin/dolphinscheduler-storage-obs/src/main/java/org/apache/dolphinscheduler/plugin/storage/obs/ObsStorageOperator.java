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

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

@Slf4j
public class ObsStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final String bucketName;

    private final ObsClient obsClient;

    public ObsStorageOperator(ObsStorageProperties obsStorageProperties) {
        super(obsStorageProperties.getResourceUploadPath());
        this.bucketName = obsStorageProperties.getBucketName();
        this.obsClient = new ObsClient(
                obsStorageProperties.getAccessKeyId(),
                obsStorageProperties.getAccessKeySecret(),
                obsStorageProperties.getEndPoint());
        ensureBucketSuccessfullyCreated(bucketName);
    }

    @Override
    public void close() throws IOException {
        obsClient.close();
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith("/")) {
            log.warn("{} -> {} should not start with / in obs", StorageConstants.RESOURCE_UPLOAD_PATH,
                    resourceBaseAbsolutePath);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        directoryAbsolutePath = transformAbsolutePathToObsKey(directoryAbsolutePath);
        if (obsClient.doesObjectExist(bucketName, directoryAbsolutePath)) {
            throw new FileAlreadyExistsException("directory: " + directoryAbsolutePath + " already exists");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0L);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, directoryAbsolutePath, emptyContent);
        obsClient.putObject(putObjectRequest);
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath, String dstFilePath, boolean overwrite) {
        srcFilePath = transformAbsolutePathToObsKey(srcFilePath);

        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
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
        }
    }

    @Override
    public boolean exists(String fileName) {
        fileName = transformAbsolutePathToObsKey(fileName);
        return obsClient.doesObjectExist(bucketName, fileName);
    }

    @Override
    public void delete(String filePath, boolean recursive) {
        filePath = transformAbsolutePathToObsKey(filePath);
        obsClient.deleteObject(bucketName, filePath);
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        srcPath = transformAbsolutePathToObsKey(srcPath);
        dstPath = transformAbsolutePathToObsKey(dstPath);
        obsClient.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            obsClient.deleteObject(bucketName, srcPath);
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        dstPath = transformAbsolutePathToObsKey(dstPath);
        if (obsClient.doesObjectExist(bucketName, dstPath)) {
            if (!overwrite) {
                throw new ObsException("file: " + dstPath + " already exists");
            } else {
                obsClient.deleteObject(bucketName, dstPath);
            }
        }
        obsClient.putObject(bucketName, dstPath, new File(srcFile));
        if (deleteSource) {
            Files.delete(Paths.get(srcFile));
        }

    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        filePath = transformAbsolutePathToObsKey(filePath);
        ObsObject obsObject = obsClient.getObject(bucketName, filePath);
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(obsObject.getObjectContent());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader
                    .lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        resourceAbsolutePath = transformObsKeyToAbsolutePath(resourceAbsolutePath);

        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.setPrefix(resourceAbsolutePath);
        request.setDelimiter("/");

        ObjectListing result = obsClient.listObjects(request);

        return result.getObjects()
                .stream()
                .map(this::transformObsObjectToStorageEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformObsKeyToAbsolutePath(resourceAbsolutePath);

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
        resourceAbsolutePath = transformObsKeyToAbsolutePath(resourceAbsolutePath);

        ObsObject object = obsClient.getObject(bucketName, resourceAbsolutePath);
        return transformObsObjectToStorageEntity(object);
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

    protected StorageEntity transformObsObjectToStorageEntity(ObsObject object) {
        ObjectMetadata metadata = object.getMetadata();
        String fileAbsolutePath = transformObsKeyToAbsolutePath(object.getObjectKey());
        ResourceMetadata resourceMetaData = getResourceMetaData(fileAbsolutePath);
        String fileExtension = com.google.common.io.Files.getFileExtension(resourceMetaData.getResourceAbsolutePath());

        return StorageEntity.builder()
                .fileName(new File(fileAbsolutePath).getName())
                .fullName(fileAbsolutePath)
                .pfullName(resourceMetaData.getResourceParentAbsolutePath())
                .type(resourceMetaData.getResourceType())
                .isDirectory(StringUtils.isEmpty(fileExtension))
                .size(metadata.getContentLength())
                .relativePath(resourceMetaData.getResourceRelativePath())
                .createTime(metadata.getLastModified())
                .updateTime(metadata.getLastModified())
                .build();
    }

    private String transformAbsolutePathToObsKey(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (resourceMetaData.isDirectory()) {
            return FileUtils.concatFilePath(absolutePath, "/");
        }
        return absolutePath;
    }

    private String transformObsKeyToAbsolutePath(String s3Key) {
        if (s3Key.endsWith("/")) {
            return s3Key.substring(0, s3Key.length() - 1);
        }
        return s3Key;
    }

}
