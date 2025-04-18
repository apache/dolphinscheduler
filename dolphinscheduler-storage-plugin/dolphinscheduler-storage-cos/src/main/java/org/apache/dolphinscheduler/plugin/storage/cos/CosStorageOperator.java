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

package org.apache.dolphinscheduler.plugin.storage.cos;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.CopyResult;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Copy;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;

@Slf4j
public class CosStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private static final int TRANSFER_THREAD_POOL_SIZE = 16;

    private static final long MULTIPART_UPLOAD_BYTES_THRESHOLD = 5 * 1024 * 1024L;
    private static final long MIN_UPLOAD_PART_BYTES = 1024 * 1024L;

    private final String bucketName;

    private final COSClient cosClient;

    private final TransferManager cosTransferManager;

    public CosStorageOperator(CosStorageProperties cosStorageProperties) {
        super(cosStorageProperties.getResourceUploadPath());
        String secretId = cosStorageProperties.getAccessKeyId();
        String secretKey = cosStorageProperties.getAccessKeySecret();
        COSCredentials cosCredentials = new BasicCOSCredentials(secretId, secretKey);
        String regionName = cosStorageProperties.getRegion();
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cosCredentials, clientConfig);
        this.bucketName = cosStorageProperties.getBucketName();
        ensureBucketSuccessfullyCreated(bucketName);
        this.cosTransferManager = getCosTransferManager();
    }

    @Override
    public void close() throws IOException {
        this.cosTransferManager.shutdownNow(true);
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith(File.separator)) {
            String warnMessage =
                    String.format("%s -> %s should not start with %s in tencent cos",
                            StorageConstants.RESOURCE_UPLOAD_PATH,
                            resourceBaseAbsolutePath, File.separator);
            log.warn(warnMessage);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        String cosKey = transformAbsolutePathToCOSKey(directoryAbsolutePath);
        if (cosClient.doesObjectExist(bucketName, cosKey)) {
            throw new FileAlreadyExistsException("directory: " + cosKey + " already exists");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0L);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, cosKey, emptyContent, metadata);
        cosClient.putObject(putObjectRequest);
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath, String dstFilePath, boolean overwrite) {
        String cosKey = transformAbsolutePathToCOSKey(srcFilePath);
        Path dsTempFolder = Paths.get(FileUtils.DATA_BASEDIR).normalize().toAbsolutePath();
        Path fileDownloadPathNormalized = dsTempFolder.resolve(dstFilePath).normalize().toAbsolutePath();
        if (!fileDownloadPathNormalized.startsWith(dsTempFolder)) {
            // if the destination file path is NOT in DS temp folder (e.g., '/tmp/dolphinscheduler'),
            // an IllegalArgumentException should be thrown.
            throw new IllegalArgumentException("failed to download to " + fileDownloadPathNormalized);
        }
        File dstFile = fileDownloadPathNormalized.toFile();
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
        }

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, cosKey);
        Download download = cosTransferManager.download(getObjectRequest, dstFile);
        download.waitForCompletion();
    }

    @Override
    public boolean exists(String fileName) {
        String cosKey = transformAbsolutePathToCOSKey(fileName);
        return cosClient.doesObjectExist(bucketName, cosKey);
    }

    @Override
    public void delete(String filePath, boolean recursive) {
        String cosKey = transformAbsolutePathToCOSKey(filePath);
        cosClient.deleteObject(bucketName, cosKey);
    }

    @SneakyThrows
    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        String srcCosKey = transformAbsolutePathToCOSKey(srcPath);
        String destCosKey = transformAbsolutePathToCOSKey(dstPath);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, srcCosKey, bucketName, destCosKey);
        Copy copy = cosTransferManager.copy(copyObjectRequest, cosClient, null);

        CopyResult copyResult = copy.waitForCopyResult();
        if (copyResult != null && deleteSource) {
            cosClient.deleteObject(bucketName, srcPath);
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        dstPath = transformAbsolutePathToCOSKey(dstPath);

        if (cosClient.doesObjectExist(bucketName, dstPath)) {
            if (!overwrite) {
                throw new CosServiceException("file: " + dstPath + " already exists");
            } else {
                cosClient.deleteObject(bucketName, dstPath);
            }
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, dstPath, new File(srcFile));
        Upload upload = cosTransferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        if (uploadResult != null && deleteSource) {
            Files.delete(Paths.get(srcFile));
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        String cosKey = transformAbsolutePathToCOSKey(filePath);

        COSObject cosObject = cosClient.getObject(bucketName, cosKey);
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(cosObject.getObjectContent());
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
        resourceAbsolutePath = transformCOSKeyToAbsolutePath(resourceAbsolutePath);

        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.setPrefix(resourceAbsolutePath);
        request.setDelimiter(File.separator);

        ObjectListing result = cosClient.listObjects(request);

        return result.getObjectSummaries()
                .stream()
                .map((COSObjectSummary summary) -> {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(summary.getSize());
                    metadata.setLastModified(summary.getLastModified());
                    COSObject object = new COSObject();
                    object.setObjectMetadata(metadata);
                    object.setKey(summary.getKey());
                    return transformCOSObjectToStorageEntity(object);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformCOSKeyToAbsolutePath(resourceAbsolutePath);

        Set<String> visited = new HashSet<>();
        List<StorageEntity> storageEntityList = new ArrayList<>();
        LinkedList<String> foldersToFetch = new LinkedList<>();
        foldersToFetch.addLast(resourceAbsolutePath);

        while (!foldersToFetch.isEmpty()) {
            String pathToExplore = foldersToFetch.pop();
            visited.add(pathToExplore);
            List<StorageEntity> storageEntities = listStorageEntity(pathToExplore);
            for (StorageEntity entity : storageEntities) {
                if (entity.isDirectory()) {
                    if (visited.contains(entity.getFullName())) {
                        continue;
                    }
                    foldersToFetch.add(entity.getFullName());
                }
            }
            storageEntityList.addAll(storageEntities);
        }

        return storageEntityList;
    }

    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        String cosKey = transformCOSKeyToAbsolutePath(resourceAbsolutePath);

        COSObject object = cosClient.getObject(bucketName, cosKey);
        return transformCOSObjectToStorageEntity(object);
    }

    public void ensureBucketSuccessfullyCreated(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(CosStorageConstants.TENCENT_CLOUD_COS_BUCKET_NAME + " is empty");
        }

        if (!cosClient.doesBucketExist(bucketName)) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found", bucketName);
    }

    protected StorageEntity transformCOSObjectToStorageEntity(COSObject object) {
        ObjectMetadata metadata = object.getObjectMetadata();
        String fileAbsolutePath = transformCOSKeyToAbsolutePath(object.getKey());
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

    private String transformAbsolutePathToCOSKey(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (resourceMetaData.isDirectory()) {
            return FileUtils.concatFilePath(absolutePath, File.separator);
        }
        return absolutePath;
    }

    private String transformCOSKeyToAbsolutePath(String cosKey) {
        if (cosKey.endsWith(File.separator)) {
            return cosKey.substring(0, cosKey.length() - 1);
        }
        return cosKey;
    }

    private TransferManager getCosTransferManager() {
        ExecutorService threadPool = Executors.newFixedThreadPool(TRANSFER_THREAD_POOL_SIZE);
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(MULTIPART_UPLOAD_BYTES_THRESHOLD);
        transferManagerConfiguration.setMinimumUploadPartSize(MIN_UPLOAD_PART_BYTES);
        transferManager.setConfiguration(transferManagerConfiguration);
        return transferManager;
    }
}
