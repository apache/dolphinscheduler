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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.InputStream;
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
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

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

        assertEquals("dolphinscheduler/default/resources/report.sql",
                storageOperator.getStorageFileAbsolutePath("default", "report.sql"));
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
    void listStorageEntityTypesABlobAndADirectoryWithTheSameName() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/dir5");

        assertEquals(2, storageEntities.size());
        assertEquals("dolphinscheduler/default/resources/dir5/reports", storageEntities.get(0).getFullName());
        assertFalse(storageEntities.get(0).isDirectory());
        assertEquals(10L, storageEntities.get(0).getSize());
        assertEquals("dolphinscheduler/default/resources/dir5/reports", storageEntities.get(1).getFullName());
        assertTrue(storageEntities.get(1).isDirectory());
    }

    @Test
    void listStorageEntityKeepsHierarchicalDirectoriesAsOneDirectoryRow() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/dir6");

        assertEquals(Arrays.asList("dolphinscheduler/default/resources/dir6/empty",
                "dolphinscheduler/default/resources/dir6/full"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
        assertTrue(storageEntities.stream().allMatch(StorageEntity::isDirectory));
    }

    @Test
    void listStorageEntityOfADottedDirectoryListsOnlyItsChildren() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/dir7/reports.v1", true);

        assertEquals(Collections.singletonList("dolphinscheduler/default/resources/dir7/reports.v1/child.csv"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
        // the same path as a file is still the exact blob only
        assertEquals(Collections.singletonList("dolphinscheduler/default/resources/dir7/reports.v1"),
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/dir7/reports.v1", false).stream()
                        .map(StorageEntity::getFullName).collect(Collectors.toList()));
    }

    @Test
    void listFileStorageEntityRecursivelyDescendsIntoADottedDirectory() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listFileStorageEntityRecursively("dolphinscheduler/default/resources/dir7");

        assertEquals(Arrays.asList("dolphinscheduler/default/resources/dir7/reports.v1",
                "dolphinscheduler/default/resources/dir7/reports.v1",
                "dolphinscheduler/default/resources/dir7/reports.v1/child.csv"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
        assertEquals(Arrays.asList(false, true, false),
                storageEntities.stream().map(StorageEntity::isDirectory).collect(Collectors.toList()));
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
    void listStorageEntityOfFilePathReturnsOnlyThatFile() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockListBlobsByHierarchy(containerClient);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("dolphinscheduler/default/resources/a.sh");

        assertEquals(Collections.singletonList("dolphinscheduler/default/resources/a.sh"),
                storageEntities.stream().map(StorageEntity::getFullName).collect(Collectors.toList()));
        assertFalse(storageEntities.get(0).isDirectory());
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

    @Test
    void deleteDirectoryRecursivelyRemovesEveryBlobUnderIt() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient directory = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1");
        mockResourceType(directory, null);
        BlobClient marker = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/");
        BlobClient child = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/b.sql");
        BlobClient nested = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/sub/c.sql");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir1/",
                "dolphinscheduler/default/resources/dir1/", "dolphinscheduler/default/resources/dir1/b.sql",
                "dolphinscheduler/default/resources/dir1/sub/c.sql");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir1", true);

        verify(marker).deleteIfExists();
        verify(child).deleteIfExists();
        verify(nested).deleteIfExists();
        verify(directory, never()).deleteIfExistsWithResponse(isNull(), any(), isNull(), isNull());
    }

    @Test
    void deleteDirectoryRecursivelyKeepsListedBlobNamesLiteral() {
        // BlobContainerClient#getBlobClient url-decodes its argument, so a listed name has to be encoded or
        // "report%20final.csv" would address the other blob "report final.csv"
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient directory = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir3");
        mockResourceType(directory, null);
        BlobClient percentNamed =
                mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir3/a%2520b.csv");
        BlobClient spaceNamed = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir3/a%20b.csv");
        BlobClient decoded = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir3/a b.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir3/",
                "dolphinscheduler/default/resources/dir3/a%20b.csv", "dolphinscheduler/default/resources/dir3/a b.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir3", true);

        verify(percentNamed).deleteIfExists();
        verify(spaceNamed).deleteIfExists();
        verify(decoded, never()).deleteIfExists();
    }

    @Test
    void encodedBlobNameAddressesTheLiteralBlobInTheSdk() {
        BlobContainerClient realClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=test;AccountKey=dGVzdA==;"
                        + "EndpointSuffix=core.windows.net")
                .containerName("test-container")
                .buildClient();

        for (String literalName : Arrays.asList("dir/report%20final.csv", "dir/report final.csv", "dir/a+b.csv",
                "dir/100%.csv", "dir/ünï.csv", "dir/plain.csv")) {
            assertEquals(literalName,
                    realClient.getBlobClient(AbsStorageOperator.encodeBlobName(literalName)).getBlobName());
        }
        // copy() passes getBlobUrl() on to the service, the name must be encoded there exactly once
        // (the SDK writes the separator as %2F)
        assertEquals("https://test.blob.core.windows.net/test-container/dir%2Freport%2520final.csv",
                realClient.getBlobClient(AbsStorageOperator.encodeBlobName("dir/report%20final.csv")).getBlobUrl());
        assertEquals("https://test.blob.core.windows.net/test-container/dir%2Freport%20final.csv",
                realClient.getBlobClient(AbsStorageOperator.encodeBlobName("dir/report final.csv")).getBlobUrl());
    }

    @Test
    void deleteDirectoryRecursivelyRemovesChildrenBeforeTheirParentDirectories() {
        // With a hierarchical namespace the directories are blobs without a trailing separator,
        // and such a directory can only be deleted when it is empty
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient directory = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1");
        mockResourceType(directory, "directory");
        BlobClient subDirectory = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/sub");
        BlobClient nested = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/sub/c.sql");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir1/",
                "dolphinscheduler/default/resources/dir1/sub", "dolphinscheduler/default/resources/dir1/sub/c.sql");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir1", true);

        InOrder inOrder = inOrder(nested, subDirectory, directory);
        inOrder.verify(nested).deleteIfExists();
        inOrder.verify(subDirectory).deleteIfExists();
        inOrder.verify(directory).deleteIfExistsWithResponse(isNull(),
                argThat(conditions -> "etag".equals(conditions.getIfMatch())), isNull(), isNull());
    }

    @Test
    void deleteDirectoryNonRecursivelyOnlyRemovesItsMarker() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient marker = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1/");
        BlobClient exact = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir1");
        mockResourceType(exact, null);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir1", false);

        verify(marker).deleteIfExists();
        verify(exact, never()).deleteIfExists();
        verify(exact, never()).deleteIfExistsWithResponse(isNull(), any(), isNull(), isNull());
        verify(containerClient, never()).listBlobs(any(ListBlobsOptions.class), any());
    }

    @Test
    void deleteEmptyHierarchicalDirectoryNonRecursivelyRemovesTheDirectoryObject() {
        assertNonRecursiveDeleteRemovesTheDirectoryObject("dolphinscheduler/default/resources/dir1");
        assertNonRecursiveDeleteRemovesTheDirectoryObject("dolphinscheduler/tenant_a/resources/dir1/sub");
    }

    @Test
    void deleteNonEmptyHierarchicalDirectoryNonRecursivelyFails() {
        assertNonRecursiveDeleteOfANonEmptyDirectoryFails("dolphinscheduler/default/resources/dir1");
        assertNonRecursiveDeleteOfANonEmptyDirectoryFails("dolphinscheduler/tenant_a/resources/dir1/sub");
    }

    @Test
    void deleteFileOnlyRemovesThatBlob() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient file = mockBlobClient(containerClient, "dolphinscheduler/default/resources/a.sh");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/a.sh", true);

        verify(file).deleteIfExists();
        verify(containerClient, never()).listBlobs(any(ListBlobsOptions.class), any());
    }

    @Test
    void existsRecognisesTheDirectoryMarker() {
        // A directory is stored as the marker blob "<path>/", the resource center asks for "<path>"
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir1", false);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir1/", true);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.exists("dolphinscheduler/default/resources/dir1"));
    }

    @Test
    void existsRecognisesTheDirectoryObjectOfAHierarchicalNamespace() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir1", true);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir1/", false);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.exists("dolphinscheduler/default/resources/dir1"));
    }

    @Test
    void existsKeepsTheBlobNameLiteral() {
        // BlobContainerClient#getBlobClient url-decodes its argument, "report%20final.csv" has to be encoded
        // or the check would look at the other blob "report final.csv"
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/report%2520final.csv", true);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/report%20final.csv", false);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.exists("dolphinscheduler/default/resources/report%20final.csv"));
        assertFalse(storageOperator.exists("dolphinscheduler/default/resources/report final.csv"));
    }

    @Test
    void uploadCopyAndCreateDirectoryKeepBlobNamesLiteral() {
        // exists() checks the literal name, so the writes have to address the same literal blob or a rename to
        // "c%20d.csv" would pass the existence check and then overwrite "c d.csv"
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient uploaded = mockBlobClient(containerClient, "dolphinscheduler/default/resources/a%2520b.csv");
        BlobClient source = mockBlobClient(containerClient, "dolphinscheduler/default/resources/b%2520c.csv");
        when(source.getBlobUrl()).thenReturn("https://test-account.blob.core.windows.net/c/b%2520c.csv");
        BlobClient target = mockBlobClient(containerClient, "dolphinscheduler/default/resources/c%2520d.csv");
        BlockBlobClient targetBlock = mock(BlockBlobClient.class);
        when(target.getBlockBlobClient()).thenReturn(targetBlock);
        BlobClient marker = mockBlobClient(containerClient, "dolphinscheduler/default/resources/d%2520e/");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.upload("/tmp/a.csv", "dolphinscheduler/default/resources/a%20b.csv", false, false);
        storageOperator.copy("dolphinscheduler/default/resources/b%20c.csv",
                "dolphinscheduler/default/resources/c%20d.csv", false, true);
        storageOperator.createStorageDir("dolphinscheduler/default/resources/d%20e");

        verify(uploaded).uploadFromFile("/tmp/a.csv", false);
        verify(targetBlock).uploadFromUrl("https://test-account.blob.core.windows.net/c/b%2520c.csv", true);
        verify(marker).exists();
        verify(marker).upload(any(InputStream.class), eq(0L));
    }

    @Test
    void deleteTheFileRowOfASameNamedPairKeepsTheDirectory() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient file = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports");
        mockResourceType(file, null);
        BlobClient child = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports/a.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir5/reports/",
                "dolphinscheduler/default/resources/dir5/reports/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir5/reports", true, false);

        verify(file).deleteIfExists();
        verify(child, never()).deleteIfExists();
    }

    @Test
    void deleteTheDirectoryRowOfASameNamedPairKeepsTheFile() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient file = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports");
        mockResourceType(file, null);
        BlobClient child = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports/a.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir5/reports/",
                "dolphinscheduler/default/resources/dir5/reports/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir5/reports", true, true);

        verify(child).deleteIfExists();
        verify(file, never()).deleteIfExists();
        verify(file, never()).deleteIfExistsWithResponse(any(), any(), any(), any());
    }

    @Test
    void existsForRecursiveDeleteWithTheTypeChecksOnlyThatTarget() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        // "only-file" is a blob without anything under it, "only-dir/" has a child but no blob "only-dir"
        BlobClient onlyFile = mockBlobClient(containerClient, "dolphinscheduler/default/resources/only-file");
        when(onlyFile.exists()).thenReturn(true);
        mockResourceType(onlyFile, null);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/only-file/", false);
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/only-file/");
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/only-dir/", false);
        BlobClient noFile = mockBlobClient(containerClient, "dolphinscheduler/default/resources/only-dir");
        when(noFile.exists()).thenReturn(false);
        mockResourceType(noFile, null);
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/only-dir/",
                "dolphinscheduler/default/resources/only-dir/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/only-file", false));
        assertFalse(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/only-file", true));
        assertTrue(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/only-dir", true));
        assertFalse(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/only-dir", false));
    }

    @Test
    void existsDoesNotCountAChildOnlyPrefix() {
        // rename copies the exact source blob, so a prefix without its own blob must not pass its validation
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/child-only/", false);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/child-only", false);
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/child-only/",
                "dolphinscheduler/default/resources/child-only/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertFalse(storageOperator.exists("dolphinscheduler/default/resources/child-only"));
    }

    @Test
    void existsForRecursiveDeleteCountsAChildOnlyPrefix() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/child-only/", false);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/child-only", false);
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/child-only/",
                "dolphinscheduler/default/resources/child-only/a.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/empty/");
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/empty/", false);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/empty", false);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/child-only"));
        assertFalse(storageOperator.existsForRecursiveDelete("dolphinscheduler/default/resources/empty"));
    }

    @Test
    void deleteWithoutTheTypeRemovesTheExactFileOfASameNamedPair() {
        // without the row type the exact blob wins, as before the directory support
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient file = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports");
        when(file.exists()).thenReturn(true);
        mockResourceType(file, null);
        BlobClient child = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports/a.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir5/reports/",
                "dolphinscheduler/default/resources/dir5/reports/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir5/reports", true);

        verify(file).deleteIfExists();
        verify(child, never()).deleteIfExists();
    }

    @Test
    void deleteWithATrailingSlashTreatsThePathAsADirectory() {
        // the slash says "directory", the exact blob of the same name must stay
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient file = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports");
        when(file.exists()).thenReturn(true);
        mockResourceType(file, null);
        BlobClient child = mockBlobClient(containerClient, "dolphinscheduler/default/resources/dir5/reports/a.csv");
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir5/reports/",
                "dolphinscheduler/default/resources/dir5/reports/a.csv");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete("dolphinscheduler/default/resources/dir5/reports/", true);

        verify(child).deleteIfExists();
        verify(file, never()).deleteIfExists();
    }

    @Test
    void existsOfAFileChecksTheFileOnly() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/a.sh", true);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/missing.sh", false);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertTrue(storageOperator.exists("dolphinscheduler/default/resources/a.sh"));
        assertFalse(storageOperator.exists("dolphinscheduler/default/resources/missing.sh"));
    }

    @Test
    void existsOfAMissingDirectoryIsFalse() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir2", false);
        mockBlobExists(containerClient, "dolphinscheduler/default/resources/dir2/", false);
        mockListBlobs(containerClient, "dolphinscheduler/default/resources/dir2/");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        assertFalse(storageOperator.exists("dolphinscheduler/default/resources/dir2"));
    }

    private void assertNonRecursiveDeleteRemovesTheDirectoryObject(String directory) {
        // With a hierarchical namespace there is no "<path>/" marker, the directory is the blob "<path>"
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobClient(containerClient, directory + "/");
        BlobClient directoryObject = mockBlobClient(containerClient, directory);
        mockResourceType(directoryObject, "directory");
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        storageOperator.delete(directory, false);

        verify(directoryObject).deleteIfExistsWithResponse(isNull(),
                argThat(conditions -> "etag".equals(conditions.getIfMatch())), isNull(), isNull());
        verify(containerClient, never()).listBlobs(any(ListBlobsOptions.class), any());
    }

    private void assertNonRecursiveDeleteOfANonEmptyDirectoryFails(String directory) {
        // Azure refuses to delete a directory that still has children with 409 Conflict
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        mockBlobClient(containerClient, directory + "/");
        BlobClient directoryObject = mockBlobClient(containerClient, directory);
        mockResourceType(directoryObject, "directory");
        BlobStorageException conflict = mock(BlobStorageException.class);
        when(conflict.getStatusCode()).thenReturn(409);
        when(directoryObject.deleteIfExistsWithResponse(isNull(), any(), isNull(), isNull())).thenThrow(conflict);
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler", containerClient);

        BlobStorageException thrown =
                assertThrows(BlobStorageException.class, () -> storageOperator.delete(directory, false));
        assertEquals(409, thrown.getStatusCode());
    }

    private void mockResourceType(BlobClient client, String type) {
        @SuppressWarnings("unchecked")
        Response<BlobProperties> properties = mock(Response.class);
        HttpHeaders headers = new HttpHeaders();
        if (type != null) {
            headers.set("x-ms-resource-type", type);
        }
        BlobProperties value = mock(BlobProperties.class);
        when(value.getETag()).thenReturn("etag");
        when(properties.getValue()).thenReturn(value);
        when(properties.getHeaders()).thenReturn(headers);
        when(client.getPropertiesWithResponse(null, null, null)).thenReturn(properties);
    }

    private void mockBlobExists(BlobContainerClient containerClient, String name, boolean exists) {
        BlobClient blobClient = mockBlobClient(containerClient, name);
        when(blobClient.exists()).thenReturn(exists);
    }

    private BlobClient mockBlobClient(BlobContainerClient containerClient, String name) {
        BlobClient blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient(name)).thenReturn(blobClient);
        return blobClient;
    }

    private void mockListBlobs(BlobContainerClient containerClient, String prefix, String... names) {
        @SuppressWarnings("unchecked")
        PagedIterable<BlobItem> blobItems = mock(PagedIterable.class);
        when(blobItems.iterator()).thenReturn(
                Arrays.stream(names).map(name -> new BlobItem().setName(name)).collect(Collectors.toList())
                        .iterator());
        when(containerClient.listBlobs(
                argThat((ListBlobsOptions options) -> options != null && prefix.equals(options.getPrefix())),
                isNull())).thenReturn(blobItems);
    }

    private void mockListBlobsByHierarchy(BlobContainerClient containerClient) {
        Map<String, List<BlobItem>> blobsByPrefix = new HashMap<>();
        blobsByPrefix.put("dolphinscheduler/default/resources/", Arrays.asList(
                // the marker blob of the listed directory itself
                new BlobItem().setName("dolphinscheduler/default/resources/").setIsPrefix(false)
                        .setProperties(new BlobItemProperties().setContentLength(0L)),
                new BlobItem().setName("dolphinscheduler/default/resources/dir1/").setIsPrefix(true),
                newFileBlobItem("dolphinscheduler/default/resources/a.sh")));
        // a prefix listing of a file path also returns the blobs that merely start with its name
        blobsByPrefix.put("dolphinscheduler/default/resources/a.sh", Arrays.asList(
                newFileBlobItem("dolphinscheduler/default/resources/a.sh"),
                newFileBlobItem("dolphinscheduler/default/resources/a.sh.bak")));
        blobsByPrefix.put("dolphinscheduler/default/resources/dir1/", Collections.singletonList(
                newFileBlobItem("dolphinscheduler/default/resources/dir1/b.sql")));
        // a flat namespace can hold the blob "reports" next to the blobs under "reports/"
        blobsByPrefix.put("dolphinscheduler/default/resources/dir5/", Arrays.asList(
                newFileBlobItem("dolphinscheduler/default/resources/dir5/reports"),
                new BlobItem().setName("dolphinscheduler/default/resources/dir5/reports/").setIsPrefix(true)));
        // a directory with a dot in its name next to a blob with the same name
        blobsByPrefix.put("dolphinscheduler/default/resources/dir7/", Arrays.asList(
                newFileBlobItem("dolphinscheduler/default/resources/dir7/reports.v1"),
                new BlobItem().setName("dolphinscheduler/default/resources/dir7/reports.v1/").setIsPrefix(true)));
        blobsByPrefix.put("dolphinscheduler/default/resources/dir7/reports.v1/", Collections.singletonList(
                newFileBlobItem("dolphinscheduler/default/resources/dir7/reports.v1/child.csv")));
        blobsByPrefix.put("dolphinscheduler/default/resources/dir7/reports.v1", Arrays.asList(
                newFileBlobItem("dolphinscheduler/default/resources/dir7/reports.v1"),
                new BlobItem().setName("dolphinscheduler/default/resources/dir7/reports.v1/").setIsPrefix(true)));
        // with a hierarchical namespace a directory is a blob flagged as folder, next to its prefix if not empty
        blobsByPrefix.put("dolphinscheduler/default/resources/dir6/", Arrays.asList(
                newHierarchicalDirectoryBlobItem("dolphinscheduler/default/resources/dir6/empty"),
                newHierarchicalDirectoryBlobItem("dolphinscheduler/default/resources/dir6/full"),
                new BlobItem().setName("dolphinscheduler/default/resources/dir6/full/").setIsPrefix(true)));

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

    private BlobItem newHierarchicalDirectoryBlobItem(String name) {
        return new BlobItem().setName(name).setIsPrefix(false)
                .setMetadata(Collections.singletonMap("hdi_isfolder", "true"))
                .setProperties(new BlobItemProperties().setContentLength(0L).setLastModified(LAST_MODIFIED));
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
