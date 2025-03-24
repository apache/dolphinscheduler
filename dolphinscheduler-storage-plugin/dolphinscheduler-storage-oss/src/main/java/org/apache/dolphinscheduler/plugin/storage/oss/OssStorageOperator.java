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

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
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
import java.io.FileNotFoundException;
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

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;

@Slf4j
public class OssStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private String bucketName;

    private String endPoint;

    private OssConnection ossConnection;

    private OSS ossClient;

    public OssStorageOperator(String resourceBaseAbsolutePath) {
        super(resourceBaseAbsolutePath);
    }

    private void init() {
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
        this.ossClient = buildOssClient();
        ensureBucketSuccessfullyCreated(bucketName);
    }

    protected String readOssAccessKeyID() {
        return PropertyUtils.getString(OssConstants.ALIBABA_CLOUD_ACCESS_KEY_ID);
    }

    protected String readOssAccessKeySecret() {
        return PropertyUtils.getString(OssConstants.ALIBABA_CLOUD_ACCESS_KEY_SECRET);
    }

    protected String readOssRegion() {
        return PropertyUtils.getString(OssConstants.ALIBABA_CLOUD_REGION);
    }

    protected String readOssBucketName() {
        return PropertyUtils.getString(StorageConstants.ALIBABA_CLOUD_OSS_BUCKET_NAME);
    }

    protected String readOssEndPoint() {
        return PropertyUtils.getString(StorageConstants.ALIBABA_CLOUD_OSS_END_POINT);
    }

    protected OssConnection buildOssConnection() {
        return new OssConnection(accessKeyId, accessKeySecret, endPoint);
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith("/")) {
            log.warn("{} -> {} should not start with / in Oss", StorageConstants.RESOURCE_UPLOAD_PATH,
                    resourceBaseAbsolutePath);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @Override
    public void close() throws IOException {
        ossClient.shutdown();
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directory) {
        directory = transformAbsolutePathToOssKey(directory);

        if (ossClient.doesObjectExist(bucketName, directory)) {
            throw new FileAlreadyExistsException("directory: " + directory + " already exists");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, directory, emptyContent, metadata);
        ossClient.putObject(putObjectRequest);
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath,
                         String dstFilePath,
                         boolean overwrite) {
        srcFilePath = transformAbsolutePathToOssKey(srcFilePath);

        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
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
    public boolean exists(String fileName) {
        fileName = transformAbsolutePathToOssKey(fileName);
        return ossClient.doesObjectExist(bucketName, fileName);
    }

    @Override
    public void delete(String filePath, boolean recursive) {
        filePath = transformAbsolutePathToOssKey(filePath);
        ossClient.deleteObject(bucketName, filePath);
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        srcPath = transformAbsolutePathToOssKey(srcPath);
        dstPath = transformAbsolutePathToOssKey(dstPath);

        ossClient.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            ossClient.deleteObject(bucketName, srcPath);
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        dstPath = transformAbsolutePathToOssKey(dstPath);
        if (ossClient.doesObjectExist(bucketName, dstPath)) {
            if (!overwrite) {
                throw new FileAlreadyExistsException("file: " + dstPath + " already exists");
            } else {
                ossClient.deleteObject(bucketName, dstPath);
            }

        }
        ossClient.putObject(bucketName, dstPath, new File(srcFile));
        if (deleteSource) {
            Files.delete(Paths.get(srcFile));
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        filePath = transformAbsolutePathToOssKey(filePath);
        OSSObject ossObject = ossClient.getObject(bucketName, filePath);
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(ossObject.getObjectContent());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        final String ossResourceAbsolutePath = transformAbsolutePathToOssKey(resourceAbsolutePath);

        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withDelimiter("/")
                .withPrefix(ossResourceAbsolutePath);

        ListObjectsV2Result listObjectsV2Result = ossClient.listObjectsV2(listObjectsV2Request);
        List<StorageEntity> storageEntities = new ArrayList<>();
        storageEntities.addAll(listObjectsV2Result.getCommonPrefixes()
                .stream()
                .map(this::transformCommonPrefixToStorageEntity)
                .collect(Collectors.toList()));
        storageEntities.addAll(
                listObjectsV2Result.getObjectSummaries().stream()
                        .filter(s3ObjectSummary -> !s3ObjectSummary.getKey().equals(resourceAbsolutePath))
                        .map(this::transformOSSObjectToStorageEntity)
                        .collect(Collectors.toList()));

        return storageEntities;

    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformOssKeyToAbsolutePath(resourceAbsolutePath);

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
        resourceAbsolutePath = transformAbsolutePathToOssKey(resourceAbsolutePath);
        OSSObject object = ossClient.getObject(new GetObjectRequest(bucketName, resourceAbsolutePath));
        return transformOSSObjectToStorageEntity(object);
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

    protected OSS buildOssClient() {
        return OssClientFactory.buildOssClient(ossConnection);
    }

    protected StorageEntity transformOSSObjectToStorageEntity(OSSObject ossObject) {
        ResourceMetadata resourceMetaData = getResourceMetaData(ossObject.getKey());

        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setFileName(new File(ossObject.getKey()).getName());
        storageEntity.setFullName(ossObject.getKey());
        storageEntity.setPfullName(resourceMetaData.getResourceParentAbsolutePath());
        storageEntity.setType(resourceMetaData.getResourceType());
        storageEntity.setDirectory(resourceMetaData.isDirectory());
        storageEntity.setSize(ossObject.getObjectMetadata().getContentLength());
        storageEntity.setRelativePath(resourceMetaData.getResourceRelativePath());
        storageEntity.setCreateTime(ossObject.getObjectMetadata().getLastModified());
        storageEntity.setUpdateTime(ossObject.getObjectMetadata().getLastModified());
        return storageEntity;
    }

    private StorageEntity transformOSSObjectToStorageEntity(OSSObjectSummary ossObjectSummary) {
        String absolutePath = transformOssKeyToAbsolutePath(ossObjectSummary.getKey());

        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);

        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setFileName(new File(absolutePath).getName());
        storageEntity.setFullName(absolutePath);
        storageEntity.setPfullName(resourceMetaData.getResourceParentAbsolutePath());
        storageEntity.setType(resourceMetaData.getResourceType());
        storageEntity.setDirectory(resourceMetaData.isDirectory());
        storageEntity.setSize(ossObjectSummary.getSize());
        storageEntity.setCreateTime(ossObjectSummary.getLastModified());
        storageEntity.setUpdateTime(ossObjectSummary.getLastModified());
        return storageEntity;
    }

    private StorageEntity transformCommonPrefixToStorageEntity(String commonPrefix) {
        String absolutePath = transformOssKeyToAbsolutePath(commonPrefix);

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

    private String transformAbsolutePathToOssKey(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (resourceMetaData.isDirectory()) {
            return FileUtils.concatFilePath(absolutePath, "/");
        }
        return absolutePath;
    }

    private String transformOssKeyToAbsolutePath(String s3Key) {
        if (s3Key.endsWith("/")) {
            return s3Key.substring(0, s3Key.length() - 1);
        }
        return s3Key;
    }
}
