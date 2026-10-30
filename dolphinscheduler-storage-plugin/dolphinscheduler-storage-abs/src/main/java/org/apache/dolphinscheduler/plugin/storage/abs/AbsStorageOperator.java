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

package org.apache.dolphinscheduler.plugin.storage.abs;

import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;

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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

@Slf4j
public class AbsStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final BlobContainerClient blobContainerClient;

    private final BlobServiceClient blobServiceClient;

    public AbsStorageOperator(AbsStorageProperties absStorageProperties) {
        super(absStorageProperties.getResourceUploadPath());
        blobServiceClient = new BlobServiceClientBuilder()
                .endpoint("https://" + absStorageProperties.getStorageAccountName() + ".blob.core.windows.net/")
                .connectionString(absStorageProperties.getConnectionString())
                .buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(absStorageProperties.getContainerName());
        checkContainerNameExists(absStorageProperties.getContainerName());
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        if (resourceBaseAbsolutePath.startsWith("/")) {
            log.warn("{} -> {} should not start with / in abs", StorageConstants.RESOURCE_UPLOAD_PATH,
                    resourceBaseAbsolutePath);
            return resourceBaseAbsolutePath.substring(1);
        }
        return resourceBaseAbsolutePath;
    }

    @SneakyThrows
    @Override
    public void createStorageDir(String directory) {
        String objectName = directory + FOLDER_SEPARATOR;
        if (isObjectExists(objectName)) {
            throw new FileAlreadyExistsException("directory: " + objectName + " already exists");
        }
        BlobClient blobClient = blobContainerClient.getBlobClient(objectName);
        blobClient.upload(new ByteArrayInputStream(EMPTY_STRING.getBytes()), 0);
    }

    @SneakyThrows
    @Override
    public void download(String srcFilePath, String dstFilePath, boolean overwrite) {
        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWithPermission(dstFile.getParentFile().toPath(), FileUtils.PERMISSION_755);
        }

        BlobClient blobClient = blobContainerClient.getBlobClient(srcFilePath);
        blobClient.downloadToFile(dstFilePath, true);
    }

    @Override
    public boolean exists(String fullName) {
        return isObjectExists(fullName);
    }

    protected boolean isObjectExists(String objectName) {
        return blobContainerClient.getBlobClient(objectName).exists();
    }

    @SneakyThrows
    @Override
    public void delete(String filePath, boolean recursive) {
        blobContainerClient.getBlobClient(filePath).deleteIfExists();
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        BlobClient srcBlobClient = blobContainerClient.getBlobClient(srcPath);
        BlockBlobClient dstBlobClient = blobContainerClient.getBlobClient(dstPath).getBlockBlobClient();

        dstBlobClient.uploadFromUrl(srcBlobClient.getBlobUrl(), overwrite);

        if (deleteSource) {
            srcBlobClient.delete();
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        BlobClient blobClient = blobContainerClient.getBlobClient(dstPath);
        blobClient.uploadFromFile(srcFile, overwrite);

        Path srcPath = Paths.get(srcFile);
        if (deleteSource) {
            Files.delete(srcPath);
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String filePath, int skipLineNums, int limit) {
        if (StringUtils.isBlank(filePath)) {
            log.error("file path:{} is blank", filePath);
            return Collections.emptyList();
        }

        BlobClient blobClient = blobContainerClient.getBlobClient(filePath);
        try (
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(
                                new ByteArrayInputStream(blobClient.downloadContent().toBytes())))) {
            Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        final String absResourceAbsolutePath = transformAbsolutePathToAbsKey(resourceAbsolutePath);
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(absResourceAbsolutePath);
        List<StorageEntity> storageEntities = new ArrayList<>();
        // The returned PagedIterable fetches the following pages while iterating
        for (BlobItem blobItem : blobContainerClient.listBlobsByHierarchy(FOLDER_SEPARATOR, options, null)) {
            if (Boolean.TRUE.equals(blobItem.isPrefix())) {
                storageEntities.add(transformPrefixToStorageEntity(blobItem.getName()));
                continue;
            }
            // Filter out the current directory itself
            if (blobItem.getName().equals(absResourceAbsolutePath)) {
                continue;
            }
            storageEntities.add(transformBlobItemToStorageEntity(blobItem));
        }
        return storageEntities;
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        resourceAbsolutePath = transformAbsKeyToAbsolutePath(resourceAbsolutePath);

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
        String absKey = transformAbsolutePathToAbsKey(resourceAbsolutePath);
        BlobProperties properties = blobContainerClient.getBlobClient(absKey).getProperties();

        String absolutePath = transformAbsKeyToAbsolutePath(absKey);
        StorageEntity storageEntity = newStorageEntity(absolutePath);
        storageEntity.setSize(properties.getBlobSize());
        storageEntity.setCreateTime(toDate(properties.getCreationTime()));
        storageEntity.setUpdateTime(toDate(properties.getLastModified()));
        return storageEntity;
    }

    private StorageEntity transformBlobItemToStorageEntity(BlobItem blobItem) {
        StorageEntity storageEntity = newStorageEntity(transformAbsKeyToAbsolutePath(blobItem.getName()));
        BlobItemProperties properties = blobItem.getProperties();
        if (properties != null) {
            storageEntity.setSize(properties.getContentLength() == null ? 0L : properties.getContentLength());
            storageEntity.setCreateTime(toDate(properties.getCreationTime()));
            storageEntity.setUpdateTime(toDate(properties.getLastModified()));
        }
        return storageEntity;
    }

    private StorageEntity transformPrefixToStorageEntity(String prefix) {
        StorageEntity storageEntity = newStorageEntity(transformAbsKeyToAbsolutePath(prefix));
        storageEntity.setSize(0L);
        return storageEntity;
    }

    private StorageEntity newStorageEntity(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setFileName(new File(absolutePath).getName());
        storageEntity.setFullName(absolutePath);
        storageEntity.setPfullName(resourceMetaData.getResourceParentAbsolutePath());
        storageEntity.setType(resourceMetaData.getResourceType());
        storageEntity.setDirectory(resourceMetaData.isDirectory());
        storageEntity.setRelativePath(resourceMetaData.getResourceRelativePath());
        return storageEntity;
    }

    private Date toDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : Date.from(offsetDateTime.toInstant());
    }

    private String transformAbsolutePathToAbsKey(String absolutePath) {
        ResourceMetadata resourceMetaData = getResourceMetaData(absolutePath);
        if (resourceMetaData.isDirectory()) {
            return FileUtils.concatFilePath(absolutePath, FOLDER_SEPARATOR);
        }
        return absolutePath;
    }

    private String transformAbsKeyToAbsolutePath(String absKey) {
        if (absKey.endsWith(FOLDER_SEPARATOR)) {
            return absKey.substring(0, absKey.length() - 1);
        }
        return absKey;
    }

    private void checkContainerNameExists(String containerName) {
        if (StringUtils.isBlank(containerName)) {
            throw new IllegalArgumentException(containerName + " is blank");
        }

        boolean exist = false;
        for (BlobContainerItem item : blobServiceClient.listBlobContainers()) {
            if (containerName.equals(item.getName())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            throw new IllegalArgumentException(
                    "containerName: " + containerName + " is not exists, you need to create them by yourself");
        } else {
            log.info("containerName: {} has been found", containerName);
        }
    }
}
