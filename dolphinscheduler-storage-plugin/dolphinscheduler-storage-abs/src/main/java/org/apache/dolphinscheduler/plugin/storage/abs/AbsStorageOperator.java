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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
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
        if (getStorageBaseDirectory().startsWith("/")) {
            log.warn("{} -> {} should not start with / in abs", StorageConstants.RESOURCE_UPLOAD_PATH,
                    getStorageBaseDirectory());
            return getStorageBaseDirectory().substring(1);
        }
        return getStorageBaseDirectory();
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
        return null;
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        return null;
    }

    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        return null;
    }

    public void checkContainerNameExists(String containerName) {
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
