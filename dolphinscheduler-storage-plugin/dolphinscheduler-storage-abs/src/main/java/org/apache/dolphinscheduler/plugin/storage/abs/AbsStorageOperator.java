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

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.specialized.BlockBlobClient;

@Data
@Slf4j
public class AbsStorageOperator implements Closeable, StorageOperate {

    private BlobContainerClient blobContainerClient;

    private BlobServiceClient blobServiceClient;

    private String connectionString;

    private String storageAccountName;

    private String containerName;

    public AbsStorageOperator() {

    }

    public void init() {
        containerName = readContainerName();
        connectionString = readConnectionString();
        storageAccountName = readAccountName();
        blobServiceClient = buildBlobServiceClient();
        blobContainerClient = buildBlobContainerClient();
        checkContainerNameExists();
    }

    protected BlobServiceClient buildBlobServiceClient() {
        return new BlobServiceClientBuilder()
                .endpoint("https://" + storageAccountName + ".blob.core.windows.net/")
                .connectionString(connectionString)
                .buildClient();
    }

    protected BlobContainerClient buildBlobContainerClient() {
        return blobServiceClient.getBlobContainerClient(containerName);
    }

    protected String readConnectionString() {
        return PropertyUtils.getString(Constants.AZURE_BLOB_STORAGE_CONNECTION_STRING);
    }

    protected String readContainerName() {
        return PropertyUtils.getString(Constants.AZURE_BLOB_STORAGE_CONTAINER_NAME);
    }

    protected String readAccountName() {
        return PropertyUtils.getString(Constants.AZURE_BLOB_STORAGE_ACCOUNT_NAME);
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        mkdir(tenantCode, getAbsResDir(tenantCode));
        mkdir(tenantCode, getAbsUdfDir(tenantCode));
    }

    public String getAbsResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getAbsTenantDir(tenantCode));
    }

    public String getAbsUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getAbsTenantDir(tenantCode));
    }

    public String getAbsTenantDir(String tenantCode) {
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
    public String getResDir(String tenantCode) {
        return getAbsResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getAbsUdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getResourceFileName(String tenantCode, String fullName) {
        String resDir = getResDir(tenantCode);
        return fullName.replaceFirst(resDir, "");
    }

    @Override
    public String getResourceFullName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName.replaceFirst(FOLDER_SEPARATOR, EMPTY_STRING);
        }
        return String.format(FORMAT_S_S, getAbsResDir(tenantCode), fileName);
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

        BlobClient blobClient = blobContainerClient.getBlobClient(srcFilePath);
        blobClient.downloadToFile(dstFilePath, true);
    }

    @Override
    public boolean exists(String fullName) throws IOException {
        return isObjectExists(fullName);
    }

    protected boolean isObjectExists(String objectName) {
        return blobContainerClient.getBlobClient(objectName).exists();
    }

    @Override
    public boolean delete(String filePath, boolean recursive) throws IOException {
        try {
            if (isObjectExists(filePath)) {
                blobContainerClient.getBlobClient(filePath).delete();
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
        BlobClient srcBlobClient = blobContainerClient.getBlobClient(srcPath);
        BlockBlobClient dstBlobClient = blobContainerClient.getBlobClient(dstPath).getBlockBlobClient();

        dstBlobClient.uploadFromUrl(srcBlobClient.getBlobUrl(), overwrite);

        if (deleteSource) {
            srcBlobClient.delete();
        }
        return true;
    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(dstPath);
            blobClient.uploadFromFile(srcFile, overwrite);

            Path srcPath = Paths.get(srcFile);
            if (deleteSource) {
                Files.delete(srcPath);
            }
            return true;
        } catch (Exception e) {
            log.error("upload failed,the container is {},the filePath is {}", containerName, dstPath);
            return false;
        }
    }

    @Override
    public List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException {
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
            blobContainerClient.getBlobClient(directoryName).delete();
        }
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        String objectName = path + FOLDER_SEPARATOR;
        if (!isObjectExists(objectName)) {
            BlobClient blobClient = blobContainerClient.getBlobClient(objectName);
            blobClient.upload(new ByteArrayInputStream(EMPTY_STRING.getBytes()), 0);
        }
        return true;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.ABS;
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

        PagedIterable<BlobItem> blobItems;
        blobItems = blobContainerClient.listBlobsByHierarchy(path);
        if (blobItems == null) {
            return storageEntityList;
        }

        for (BlobItem blobItem : blobItems) {
            if (path.equals(blobItem.getName())) {
                continue;
            }
            if (blobItem.isPrefix()) {
                String suffix = StringUtils.difference(path, blobItem.getName());
                String fileName = StringUtils.difference(defaultPath, blobItem.getName());
                StorageEntity entity = new StorageEntity();
                entity.setAlias(suffix);
                entity.setFileName(fileName);
                entity.setFullName(blobItem.getName());
                entity.setDirectory(true);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(0);
                entity.setCreateTime(null);
                entity.setUpdateTime(null);
                entity.setPfullName(path);

                storageEntityList.add(entity);
            } else {
                String[] aliasArr = blobItem.getName().split("/");
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, blobItem.getName());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(blobItem.getName());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(blobItem.getProperties().getContentLength());
                entity.setCreateTime(Date.from(blobItem.getProperties().getCreationTime().toInstant()));
                entity.setUpdateTime(Date.from(blobItem.getProperties().getLastModified().toInstant()));
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
                BlobClient blobClient = blobContainerClient.getBlobClient(path);

                String[] aliasArr = blobClient.getBlobName().split(FOLDER_SEPARATOR);
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, blobClient.getBlobName());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(blobClient.getBlobName());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(blobClient.getProperties().getBlobSize());
                entity.setCreateTime(Date.from(blobClient.getProperties().getCreationTime().toInstant()));
                entity.setUpdateTime(Date.from(blobClient.getProperties().getLastModified().toInstant()));

                return entity;
            } else {
                throw new FileNotFoundException("Object is not found in ABS container: " + containerName);
            }
        }
    }

    private String findDirAlias(String dirPath) {
        if (!dirPath.endsWith(FOLDER_SEPARATOR)) {
            return dirPath;
        }

        Path path = Paths.get(dirPath);
        return path.getName(path.getNameCount() - 1) + FOLDER_SEPARATOR;
    }

    public void checkContainerNameExists() {
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
