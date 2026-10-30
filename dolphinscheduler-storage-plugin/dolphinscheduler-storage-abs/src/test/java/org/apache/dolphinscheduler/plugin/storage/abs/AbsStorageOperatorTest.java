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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;

class AbsStorageOperatorTest {

    private static final OffsetDateTime LAST_MODIFIED = OffsetDateTime.of(2026, 10, 30, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void getStorageBaseDirectoryWithoutLeadingSlash() {
        AbsStorageOperator storageOperator = createStorageOperator("dolphinscheduler");

        assertEquals("dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithLeadingSlash() {
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler");

        assertEquals("dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithTenantAndResourceType() {
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler");

        assertEquals("dolphinscheduler/default", storageOperator.getStorageBaseDirectory("default"));
        assertEquals("dolphinscheduler/default/resources",
                storageOperator.getStorageBaseDirectory("default", ResourceType.FILE));
    }

    @Test
    void listStorageEntityReturnsDirectoriesAndFiles() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("dolphinscheduler/default/resources");

        assertNotNull(storageEntities);
        assertEquals(
                Arrays.asList("dolphinscheduler/default/resources/dir1", "dolphinscheduler/default/resources/a.sh"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
        StorageEntity directory = storageEntities.get(0);
        assertTrue(directory.isDirectory());
        assertEquals("dir1", directory.getFileName());
        assertEquals(0L, directory.getSize());
        StorageEntity file = storageEntities.get(1);
        assertFalse(file.isDirectory());
        assertEquals("a.sh", file.getFileName());
        assertEquals("dolphinscheduler/default/resources", file.getPfullName());
        assertEquals(10L, file.getSize());
        assertEquals(Date.from(LAST_MODIFIED.toInstant()), file.getUpdateTime());
    }

    @Test
    void listStorageEntityOfEmptyDirectoryReturnsEmptyList() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertEquals(Collections.emptyList(),
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/empty"));
    }

    @Test
    void listFileStorageEntityRecursivelyWalksSubdirectories() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listFileStorageEntityRecursively("dolphinscheduler/default/resources");

        assertNotNull(storageEntities);
        assertEquals(Arrays.asList("dolphinscheduler/default/resources/dir1", "dolphinscheduler/default/resources/a.sh",
                "dolphinscheduler/default/resources/dir1/b.sql"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
    }

    @Test
    void getStorageEntityReadsBlobProperties() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties blobProperties = mock(BlobProperties.class);
        when(blobProperties.getBlobSize()).thenReturn(10L);
        when(blobProperties.getCreationTime()).thenReturn(LAST_MODIFIED);
        when(blobProperties.getLastModified()).thenReturn(LAST_MODIFIED);
        when(blobClient.getProperties()).thenReturn(blobProperties);
        when(containerClient.getBlobClient("dolphinscheduler/default/resources/a.sh")).thenReturn(blobClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        StorageEntity storageEntity = storageOperator.getStorageEntity("dolphinscheduler/default/resources/a.sh");

        assertNotNull(storageEntity);
        assertEquals("dolphinscheduler/default/resources/a.sh", storageEntity.getFullName());
        assertEquals("a.sh", storageEntity.getFileName());
        assertFalse(storageEntity.isDirectory());
        assertEquals(10L, storageEntity.getSize());
        assertEquals(Date.from(LAST_MODIFIED.toInstant()), storageEntity.getCreateTime());
    }

    private void mockListBlobsByHierarchy(BlobContainerClient containerClient) {
        Map<String, List<BlobItem>> blobsByPrefix = new HashMap<>();
        blobsByPrefix.put("dolphinscheduler/default/resources/", Arrays.asList(
                // the marker blob of the listed directory itself
                new BlobItem().setName("dolphinscheduler/default/resources/").setIsPrefix(false)
                        .setProperties(new BlobItemProperties().setContentLength(0L)),
                new BlobItem().setName("dolphinscheduler/default/resources/dir1/").setIsPrefix(true),
                newFileBlobItem("dolphinscheduler/default/resources/a.sh")));
        blobsByPrefix.put("dolphinscheduler/default/resources/dir1/", Collections.singletonList(
                newFileBlobItem("dolphinscheduler/default/resources/dir1/b.sql")));

        when(containerClient.listBlobsByHierarchy(eq("/"), any(ListBlobsOptions.class), isNull()))
                .thenAnswer(invocation -> {
                    ListBlobsOptions options = invocation.getArgument(1);
                    @SuppressWarnings("unchecked")
                    PagedIterable<BlobItem> blobItems = mock(PagedIterable.class);
                    when(blobItems.iterator()).thenReturn(
                            blobsByPrefix.getOrDefault(options.getPrefix(), Collections.emptyList()).iterator());
                    return blobItems;
                });
    }

    private BlobItem newFileBlobItem(String name) {
        return new BlobItem().setName(name).setIsPrefix(false)
                .setProperties(new BlobItemProperties().setContentLength(10L).setLastModified(LAST_MODIFIED));
    }

    private AbsStorageOperator createStorageOperator(String resourceUploadPath) {
        return createStorageOperator(resourceUploadPath, mock(BlobContainerClient.class));
    }

    private AbsStorageOperator createStorageOperator(String resourceUploadPath, BlobContainerClient containerClient) {
        BlobServiceClient serviceClient = mock(BlobServiceClient.class);
        @SuppressWarnings("unchecked")
        PagedIterable<BlobContainerItem> containers = mock(PagedIterable.class);
        when(containers.iterator())
                .thenReturn(Collections.singletonList(new BlobContainerItem().setName("test-container")).iterator());
        when(serviceClient.listBlobContainers()).thenReturn(containers);
        when(serviceClient.getBlobContainerClient("test-container")).thenReturn(containerClient);

        AbsStorageProperties properties = AbsStorageProperties.builder()
                .storageAccountName("test-account")
                .connectionString("test-connection-string")
                .containerName("test-container")
                .resourceUploadPath(resourceUploadPath)
                .build();
        try (
                MockedConstruction<BlobServiceClientBuilder> construction =
                        Mockito.mockConstruction(BlobServiceClientBuilder.class, (builder, context) -> {
                            when(builder.endpoint("https://test-account.blob.core.windows.net/")).thenReturn(builder);
                            when(builder.connectionString("test-connection-string")).thenReturn(builder);
                            when(builder.buildClient()).thenReturn(serviceClient);
                        })) {
            return new AbsStorageOperator(properties);
        }
    }
}
