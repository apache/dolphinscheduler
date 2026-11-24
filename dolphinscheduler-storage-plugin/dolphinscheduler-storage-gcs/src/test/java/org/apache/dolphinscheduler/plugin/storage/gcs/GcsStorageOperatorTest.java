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

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.TestBlobFactory;

public class GcsStorageOperatorTest {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String RESOURCE_UPLOAD_PATH = "/dolphinscheduler";

    private Storage mockStorage;
    private GcsStorageOperator storageOperator;
    private Set<String> existingKeys;
    private List<BlobId> singleDeletedBlobIds;
    private List<List<BlobId>> batchDeletedBlobIds;
    private List<Blob> listBlobsToReturn;
    private List<Storage.BlobListOption> capturedListOptions;
    private AtomicInteger getCallCount;

    @BeforeEach
    void setUp() {
        existingKeys = new HashSet<>();
        singleDeletedBlobIds = new ArrayList<>();
        batchDeletedBlobIds = new ArrayList<>();
        listBlobsToReturn = new ArrayList<>();
        capturedListOptions = new ArrayList<>();
        getCallCount = new AtomicInteger(0);

        mockStorage = (Storage) Proxy.newProxyInstance(
                Storage.class.getClassLoader(),
                new Class<?>[]{Storage.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("get".equals(methodName)) {
                        getCallCount.incrementAndGet();
                        BlobId blobId = (BlobId) args[0];
                        if (existingKeys.contains(blobId.getName())) {
                            return TestBlobFactory.createBlob((Storage) proxy, blobId);
                        }
                        return null;
                    } else if ("delete".equals(methodName)) {
                        Object target = args[0];
                        if (target instanceof BlobId) {
                            singleDeletedBlobIds.add((BlobId) target);
                            return true;
                        } else if (target instanceof Iterable) {
                            List<BlobId> batch = new ArrayList<>();
                            for (Object item : (Iterable<?>) target) {
                                batch.add((BlobId) item);
                            }
                            batchDeletedBlobIds.add(batch);
                            return Collections.nCopies(batch.size(), true);
                        }
                        return null;
                    } else if ("list".equals(methodName)) {
                        if (args.length > 1 && args[1] instanceof Storage.BlobListOption[]) {
                            for (Storage.BlobListOption opt : (Storage.BlobListOption[]) args[1]) {
                                capturedListOptions.add(opt);
                            }
                        }
                        return createMockPage(listBlobsToReturn);
                    }
                    return null;
                });

        storageOperator = new GcsStorageOperator(RESOURCE_UPLOAD_PATH, BUCKET_NAME, mockStorage);
    }

    @SuppressWarnings("unchecked")
    private Page<Blob> createMockPage(List<Blob> blobs) {
        return (Page<Blob>) Proxy.newProxyInstance(
                Page.class.getClassLoader(),
                new Class<?>[]{Page.class},
                (proxy, method, args) -> {
                    if ("iterateAll".equals(method.getName())) {
                        return blobs;
                    }
                    return null;
                });
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
        existingKeys.add(filePath);

        storageOperator.delete(filePath, false);

        assertEquals(1, singleDeletedBlobIds.size());
        assertEquals(BlobId.of(BUCKET_NAME, filePath), singleDeletedBlobIds.get(0));
        assertTrue(capturedListOptions.isEmpty());
    }

    @Test
    void deleteDirectoryNonRecursively_removesOnlyMarker() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "reports");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");
        existingKeys.add(dirKey);

        storageOperator.delete(dirPath, false);

        assertEquals(1, singleDeletedBlobIds.size());
        assertEquals(BlobId.of(BUCKET_NAME, dirKey), singleDeletedBlobIds.get(0));
        assertTrue(capturedListOptions.isEmpty());
    }

    @Test
    void deleteDirectoryRecursively_removesAllChildBlobsAndMarker() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "reports");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        BlobId markerId = BlobId.of(BUCKET_NAME, dirKey);
        BlobId file1Id = BlobId.of(BUCKET_NAME, dirKey + "data.csv");
        BlobId file2Id = BlobId.of(BUCKET_NAME, dirKey + "sub/nested.txt");

        listBlobsToReturn.add(TestBlobFactory.createBlob(mockStorage, markerId));
        listBlobsToReturn.add(TestBlobFactory.createBlob(mockStorage, file1Id));
        listBlobsToReturn.add(TestBlobFactory.createBlob(mockStorage, file2Id));

        storageOperator.delete(dirPath, true);

        assertEquals(1, batchDeletedBlobIds.size());
        List<BlobId> deletedIds = batchDeletedBlobIds.get(0);
        assertEquals(3, deletedIds.size());
        assertEquals(Arrays.asList(markerId, file1Id, file2Id), deletedIds);
        // Verify no redundant get() call was made after listing
        assertEquals(0, getCallCount.get());
        assertTrue(singleDeletedBlobIds.isEmpty());
    }

    @Test
    void deleteDirectoryRecursively_emptyDirectory_deletesMarkerViaListing() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "empty");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");
        BlobId markerId = BlobId.of(BUCKET_NAME, dirKey);

        // Prefix listing returns the directory marker object when directory exists
        listBlobsToReturn.add(TestBlobFactory.createBlob(mockStorage, markerId));

        storageOperator.delete(dirPath, true);

        assertEquals(1, batchDeletedBlobIds.size());
        assertEquals(Collections.singletonList(markerId), batchDeletedBlobIds.get(0));
        // Verify no redundant get() or single delete was called
        assertEquals(0, getCallCount.get());
        assertTrue(singleDeletedBlobIds.isEmpty());
    }

    @Test
    void deleteDirectoryRecursively_withPaginationAndChunking_processesAllBatches() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "largeDir");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        List<BlobId> expectedBlobIds = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            BlobId blobId = BlobId.of(BUCKET_NAME, dirKey + "item_" + i + ".txt");
            listBlobsToReturn.add(TestBlobFactory.createBlob(mockStorage, blobId));
            expectedBlobIds.add(blobId);
        }

        storageOperator.delete(dirPath, true);

        // With 250 items and chunk size 100, batch delete should be called exactly 3 times (100 + 100 + 50)
        assertEquals(3, batchDeletedBlobIds.size());
        assertEquals(100, batchDeletedBlobIds.get(0).size());
        assertEquals(100, batchDeletedBlobIds.get(1).size());
        assertEquals(50, batchDeletedBlobIds.get(2).size());

        List<BlobId> allDeletedIds = new ArrayList<>();
        batchDeletedBlobIds.forEach(allDeletedIds::addAll);
        assertEquals(expectedBlobIds, allDeletedIds);
        assertEquals(0, getCallCount.get());
    }

    @Test
    void deleteDirectoryRecursively_prefixBoundaryIsolation_doesNotMatchSiblingFolder() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "folder");
        String dirKey = FileUtils.concatFilePath(dirPath, "/");

        storageOperator.delete(dirPath, true);

        // Verify that the prefix used ends with '/', preventing collision with folder2/
        assertEquals(1, capturedListOptions.size());
        String capturedOptionStr = capturedListOptions.get(0).toString();
        assertTrue(capturedOptionStr.contains(dirKey));
        assertFalse(capturedOptionStr.contains("folder2"));
    }
}
