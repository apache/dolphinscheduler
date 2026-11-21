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

package org.apache.dolphinscheduler.plugin.storage.gcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

public class GcsStorageOperatorTest {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String RESOURCE_UPLOAD_PATH = "/dolphinscheduler";

    private Storage mockStorage;
    private GcsStorageOperator storageOperator;

    @BeforeEach
    void setUp() {
        mockStorage = mock(Storage.class);
        storageOperator = new GcsStorageOperator(RESOURCE_UPLOAD_PATH, BUCKET_NAME, mockStorage);
    }

    @Test
    void getStorageBaseDirectoryWithoutLeadingSlash() {
        GcsStorageOperator operator = new GcsStorageOperator("dolphinscheduler", BUCKET_NAME, mockStorage);
        assertEquals("dolphinscheduler", operator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithLeadingSlash() {
        assertEquals("dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithTenantAndResourceType() {
        assertEquals(FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default"),
                storageOperator.getStorageBaseDirectory("default"));
        assertEquals(FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources"),
                storageOperator.getStorageBaseDirectory("default", ResourceType.FILE));
    }

    @Test
    void deleteFile_removesOnlyTargetBlob() {
        String filePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "test.txt");
        Blob mockBlob = mock(Blob.class);
        when(mockBlob.exists()).thenReturn(true);
        when(mockStorage.get(BlobId.of(BUCKET_NAME, filePath))).thenReturn(mockBlob);

        storageOperator.delete(filePath, false);

        verify(mockStorage).delete(BlobId.of(BUCKET_NAME, filePath));
        verify(mockStorage, never()).list(eq(BUCKET_NAME), any(Storage.BlobListOption.class));
    }

    @Test
    void deleteDirectoryNonRecursively_removesOnlyMarker() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "reports");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        Blob mockBlob = mock(Blob.class);
        when(mockBlob.exists()).thenReturn(true);
        when(mockStorage.get(BlobId.of(BUCKET_NAME, dirKey))).thenReturn(mockBlob);

        storageOperator.delete(dirPath, false);

        verify(mockStorage).delete(BlobId.of(BUCKET_NAME, dirKey));
        verify(mockStorage, never()).list(eq(BUCKET_NAME), any(Storage.BlobListOption.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteDirectoryRecursively_removesAllChildBlobsAndMarker() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "reports");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        BlobId markerId = BlobId.of(BUCKET_NAME, dirKey);
        BlobId file1Id = BlobId.of(BUCKET_NAME, dirKey + "data.csv");
        BlobId file2Id = BlobId.of(BUCKET_NAME, dirKey + "sub/nested.txt");

        Blob markerBlob = mock(Blob.class);
        when(markerBlob.getBlobId()).thenReturn(markerId);

        Blob file1Blob = mock(Blob.class);
        when(file1Blob.getBlobId()).thenReturn(file1Id);

        Blob file2Blob = mock(Blob.class);
        when(file2Blob.getBlobId()).thenReturn(file2Id);

        Page<Blob> mockPage = mock(Page.class);
        when(mockPage.iterateAll()).thenReturn(Arrays.asList(markerBlob, file1Blob, file2Blob));

        when(mockStorage.list(eq(BUCKET_NAME), any(Storage.BlobListOption.class))).thenReturn(mockPage);
        when(mockStorage.get(markerId)).thenReturn(null);

        storageOperator.delete(dirPath, true);

        ArgumentCaptor<List<BlobId>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockStorage).delete(captor.capture());
        List<BlobId> deletedIds = captor.getValue();
        assertEquals(3, deletedIds.size());
        assertEquals(Arrays.asList(markerId, file1Id, file2Id), deletedIds);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteDirectoryRecursively_emptyDirectory_deletesMarker() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "empty");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");
        BlobId markerId = BlobId.of(BUCKET_NAME, dirKey);

        Page<Blob> emptyPage = mock(Page.class);
        when(emptyPage.iterateAll()).thenReturn(Collections.emptyList());
        when(mockStorage.list(eq(BUCKET_NAME), any(Storage.BlobListOption.class))).thenReturn(emptyPage);

        Blob markerBlob = mock(Blob.class);
        when(markerBlob.exists()).thenReturn(true);
        when(mockStorage.get(markerId)).thenReturn(markerBlob);

        storageOperator.delete(dirPath, true);

        verify(mockStorage).delete(markerId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteDirectoryRecursively_withPaginationAndChunking_processesAllBatches() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "largeDir");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        List<Blob> pagedBlobs = new ArrayList<>();
        List<BlobId> expectedBlobIds = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            BlobId blobId = BlobId.of(BUCKET_NAME, dirKey + "item_" + i + ".txt");
            Blob blob = mock(Blob.class);
            when(blob.getBlobId()).thenReturn(blobId);
            pagedBlobs.add(blob);
            expectedBlobIds.add(blobId);
        }

        Page<Blob> mockPage = mock(Page.class);
        when(mockPage.iterateAll()).thenReturn(pagedBlobs);
        when(mockStorage.list(eq(BUCKET_NAME), any(Storage.BlobListOption.class))).thenReturn(mockPage);

        storageOperator.delete(dirPath, true);

        ArgumentCaptor<List<BlobId>> captor = ArgumentCaptor.forClass(List.class);
        // With 250 items and chunk size 100, delete should be called exactly 3 times
        // (100 + 100 + 50)
        verify(mockStorage, org.mockito.Mockito.times(3)).delete(captor.capture());
        List<List<BlobId>> allBatches = captor.getAllValues();
        assertEquals(3, allBatches.size());
        assertEquals(100, allBatches.get(0).size());
        assertEquals(100, allBatches.get(1).size());
        assertEquals(50, allBatches.get(2).size());

        List<BlobId> allDeletedIds = new ArrayList<>();
        allBatches.forEach(allDeletedIds::addAll);
        assertEquals(expectedBlobIds, allDeletedIds);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteDirectoryRecursively_prefixBoundaryIsolation_doesNotMatchSiblingFolder() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "folder");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        ArgumentCaptor<Storage.BlobListOption> optionCaptor = ArgumentCaptor.forClass(Storage.BlobListOption.class);
        Page<Blob> emptyPage = mock(Page.class);
        when(emptyPage.iterateAll()).thenReturn(Collections.emptyList());
        when(mockStorage.list(eq(BUCKET_NAME), optionCaptor.capture())).thenReturn(emptyPage);

        storageOperator.delete(dirPath, true);

        // Verify that the prefix used ends with '/', preventing collision with folder2/
        Storage.BlobListOption capturedOption = optionCaptor.getValue();
        assertTrue(capturedOption.toString().contains(dirKey));
        assertFalse(capturedOption.toString().contains("folder2"));
    }
}
