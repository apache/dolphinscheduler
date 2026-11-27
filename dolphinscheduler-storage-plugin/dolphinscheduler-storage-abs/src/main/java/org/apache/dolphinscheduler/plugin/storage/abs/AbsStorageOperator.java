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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

@Slf4j
public class AbsStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final BlobContainerClient blobContainerClient;

    private final BlobServiceClient blobServiceClient;

    public AbsStorageOperator(AbsStorageProperties absStorageProperties) {
        super(absStorageProperties.getResourceUploadPath(), FOLDER_SEPARATOR);
        blobServiceClient = new BlobServiceClientBuilder()
                .endpoint("https://" + absStorageProperties.getStorageAccountName() + ".blob.core.windows.net/")
                .connectionString(absStorageProperties.getConnectionString())
                .buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(absStorageProperties.getContainerName());
        checkContainerNameExists(absStorageProperties.getContainerName());
    }

    @Override
    public String getStorageBaseDirectory() {
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
        BlobClient blobClient = getLiteralBlobClient(objectName);
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

        BlobClient blobClient = getLiteralBlobClient(srcFilePath);
        blobClient.downloadToFile(dstFilePath, true);
    }

    @Override
    public boolean exists(String fullName) {
        String absKey = transformAbsolutePathToAbsKey(fullName);
        if (isObjectExists(absKey)) {
            return true;
        }
        if (!absKey.endsWith(FOLDER_SEPARATOR)) {
            return false;
        }
        return isObjectExists(transformAbsKeyToAbsolutePath(absKey)) || hasBlobUnder(absKey);
    }

    @Override
    public boolean existsForRecursiveDelete(String resourceAbsolutePath, boolean isDirectory) {
        if (!isDirectory) {
            return isObjectExists(transformAbsKeyToAbsolutePath(resourceAbsolutePath));
        }
        // A blob with the directory's name does not make the directory exist, only its marker, a hierarchical
        // namespace directory or a blob under it do
        String directoryKey = toDirectoryKey(resourceAbsolutePath);
        return isObjectExists(directoryKey)
                || getHierarchicalDirectoryProperties(transformAbsKeyToAbsolutePath(directoryKey)) != null
                || hasBlobUnder(directoryKey);
    }

    private boolean hasBlobUnder(String directoryKey) {
        return blobContainerClient
                .listBlobs(new ListBlobsOptions().setPrefix(directoryKey).setMaxResultsPerPage(1), null)
                .iterator().hasNext();
    }

    protected boolean isObjectExists(String objectName) {
        return getLiteralBlobClient(objectName).exists();
    }

    @SneakyThrows
    @Override
    public void delete(String filePath, boolean recursive) {
        String absKey = transformAbsolutePathToAbsKey(filePath);
        if (!absKey.endsWith(FOLDER_SEPARATOR)) {
            getLiteralBlobClient(absKey).deleteIfExists();
            return;
        }
        deleteDirectory(absKey, recursive);
    }

    @Override
    public void delete(String resourceAbsolutePath, boolean recursive, boolean isDirectory) {
        if (isDirectory) {
            deleteDirectory(toDirectoryKey(resourceAbsolutePath), recursive);
            return;
        }
        // A file only owns its exact blob, even when a directory with the same name exists
        getLiteralBlobClient(transformAbsKeyToAbsolutePath(resourceAbsolutePath)).deleteIfExists();
    }

    private void deleteDirectory(String absKey, boolean recursive) {
        if (recursive) {
            // The prefix listing contains the directory marker blob as well
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(absKey);
            List<String> blobNames = new ArrayList<>();
            for (BlobItem blobItem : blobContainerClient.listBlobs(options, null)) {
                blobNames.add(blobItem.getName());
            }
            // With a hierarchical namespace a directory can only be deleted when it is empty, and a name always sorts
            // before the names below it, so delete in reverse order to remove the children first
            blobNames.sort(Comparator.reverseOrder());
            for (String blobName : blobNames) {
                getLiteralBlobClient(blobName).deleteIfExists();
            }
        } else {
            getLiteralBlobClient(absKey).deleteIfExists();
        }
        deleteHierarchicalDirectory(transformAbsKeyToAbsolutePath(absKey));
    }

    /**
     * {@link BlobContainerClient#getBlobClient(String)} url-decodes the name, so every literal name is encoded first,
     * otherwise {@code report%20final.csv} would address the blob {@code report final.csv}.
     */
    private BlobClient getLiteralBlobClient(String blobName) {
        return blobContainerClient.getBlobClient(encodeBlobName(blobName));
    }

    static String encodeBlobName(String blobName) {
        return Arrays.stream(blobName.split(FOLDER_SEPARATOR, -1))
                .map(AbsStorageOperator::encodeSegment)
                .collect(Collectors.joining(FOLDER_SEPARATOR));
    }

    @SneakyThrows
    private static String encodeSegment(String segment) {
        // URLEncoder writes a space as "+", which the SDK keeps as a literal plus
        return URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20");
    }

    private void deleteHierarchicalDirectory(String name) {
        Response<BlobProperties> properties = getHierarchicalDirectoryProperties(name);
        if (properties != null) {
            // Delete only the directory we inspected, even if the key is replaced concurrently.
            BlobRequestConditions conditions = new BlobRequestConditions().setIfMatch(properties.getValue().getETag());
            getLiteralBlobClient(name).deleteIfExistsWithResponse(null, conditions, null, null);
        }
    }

    /**
     * Returns the properties of the hierarchical namespace directory with this name, or null when there is none.
     */
    private Response<BlobProperties> getHierarchicalDirectoryProperties(String name) {
        Response<BlobProperties> properties;
        try {
            properties = getLiteralBlobClient(name).getPropertiesWithResponse(null, null, null);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
        return "directory".equals(properties.getHeaders().getValue("x-ms-resource-type")) ? properties : null;
    }

    private String toDirectoryKey(String resourceAbsolutePath) {
        return resourceAbsolutePath.endsWith(FOLDER_SEPARATOR) ? resourceAbsolutePath
                : resourceAbsolutePath + FOLDER_SEPARATOR;
    }

    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        BlobClient srcBlobClient = getLiteralBlobClient(srcPath);
        BlockBlobClient dstBlobClient = getLiteralBlobClient(dstPath).getBlockBlobClient();

        dstBlobClient.uploadFromUrl(srcBlobClient.getBlobUrl(), overwrite);

        if (deleteSource) {
            srcBlobClient.delete();
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcFile, String dstPath, boolean deleteSource, boolean overwrite) {
        BlobClient blobClient = getLiteralBlobClient(dstPath);
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

        BlobClient blobClient = getLiteralBlobClient(filePath);
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
        final boolean isDirectory = getResourceMetaData(resourceAbsolutePath).isDirectory();
        final String absResourceAbsolutePath = transformAbsolutePathToAbsKey(resourceAbsolutePath);
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(absResourceAbsolutePath)
                .setDetails(new BlobListDetails().setRetrieveMetadata(true));
        List<StorageEntity> storageEntities = new ArrayList<>();
        // A hierarchical namespace lists a non-empty directory both as a folder blob and as a prefix
        Set<String> directoryNames = new HashSet<>();
        // The returned PagedIterable fetches the following pages while iterating
        for (BlobItem blobItem : blobContainerClient.listBlobsByHierarchy(FOLDER_SEPARATOR, options, null)) {
            boolean isExactMatch = blobItem.getName().equals(absResourceAbsolutePath);
            if (!isDirectory) {
                // The listing is prefix based, a file path must only return the blob with exactly that name
                if (isExactMatch && !Boolean.TRUE.equals(blobItem.isPrefix())) {
                    storageEntities.add(transformBlobItemToStorageEntity(blobItem));
                }
                continue;
            }
            if (Boolean.TRUE.equals(blobItem.isPrefix())) {
                StorageEntity directory = transformPrefixToStorageEntity(blobItem.getName());
                if (directoryNames.add(directory.getFullName())) {
                    storageEntities.add(directory);
                }
                continue;
            }
            // Filter out the marker blob of the listed directory itself
            if (isExactMatch) {
                continue;
            }
            // Type the row from the listing, not from the extension: a blob named like a directory is still a file
            StorageEntity storageEntity = transformBlobItemToStorageEntity(blobItem);
            storageEntity.setDirectory(isHierarchicalDirectory(blobItem));
            if (!storageEntity.isDirectory() || directoryNames.add(storageEntity.getFullName())) {
                storageEntities.add(storageEntity);
            }
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
        BlobProperties properties = getLiteralBlobClient(absKey).getProperties();

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
        storageEntity.setDirectory(true);
        storageEntity.setSize(0L);
        return storageEntity;
    }

    private boolean isHierarchicalDirectory(BlobItem blobItem) {
        Map<String, String> metadata = blobItem.getMetadata();
        return metadata != null && "true".equalsIgnoreCase(metadata.get("hdi_isfolder"));
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
            return StringUtils.appendIfMissing(absolutePath, FOLDER_SEPARATOR);
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
