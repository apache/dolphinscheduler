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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.google.api.gax.paging.Page;
import com.google.cloud.NoCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GcsStorageOperatorTest {

    private static final int PORT = 4443;
    private static final String BUCKET_NAME = "dolphinscheduler-test-bucket";
    private static final String RESOURCE_UPLOAD_PATH = "/dolphinscheduler";

    private static GenericContainer<?> gcsContainer;
    private static Storage storage;
    private static GcsStorageOperator storageOperator;

    @BeforeAll
    public static void setUp() {
        gcsContainer = new GenericContainer<>(DockerImageName.parse("fsouza/fake-gcs-server:1.56.1"))
                .withExposedPorts(PORT)
                .withCommand("-scheme", "http", "-backend", "memory")
                .waitingFor(Wait.forHttp("/storage/v1/b").forStatusCode(200));
        gcsContainer.start();

        String endpoint = String.format("http://%s:%d", gcsContainer.getHost(), gcsContainer.getMappedPort(PORT));
        storage = StorageOptions.newBuilder()
                .setHost(endpoint)
                .setProjectId("test-project")
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();

        storage.create(BucketInfo.of(BUCKET_NAME));
        storageOperator = new GcsStorageOperator(RESOURCE_UPLOAD_PATH, BUCKET_NAME, storage);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDown() {
        if (storageOperator != null) {
            storageOperator.close();
        }
        if (gcsContainer != null) {
            gcsContainer.stop();
        }
    }

    private String toGcsKey(String applicationPath) {
        return applicationPath.replace(File.separatorChar, '/');
    }

    private String toGcsDirKey(String applicationPath) {
        String key = toGcsKey(applicationPath);
        return key.endsWith("/") ? key : key + "/";
    }

    @Test
    void delete_file_exists_deletedSuccessfully() {
        String filePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "test.txt");
        String gcsKey = toGcsKey(filePath);
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, gcsKey)).build(),
                "hello".getBytes(StandardCharsets.UTF_8));
        assertTrue(storageOperator.exists(filePath));

        storageOperator.delete(filePath, false);

        assertFalse(storageOperator.exists(filePath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, gcsKey)));
    }

    @Test
    void delete_file_doesNotExist_noOp() {
        String filePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "non_existent.txt");
        assertFalse(storageOperator.exists(filePath));

        storageOperator.delete(filePath, false);

        assertFalse(storageOperator.exists(filePath));
    }

    @Test
    void delete_directory_nonRecursive_deletesMarkerRetainsChildren() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "reports");
        storageOperator.createStorageDir(dirPath);
        assertTrue(storageOperator.exists(dirPath));

        String childAppPath = FileUtils.concatFilePath(dirPath, "report.txt");
        String childGcsKey = toGcsKey(childAppPath);
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, childGcsKey)).build(),
                "data".getBytes(StandardCharsets.UTF_8));

        storageOperator.delete(dirPath, false);

        // Marker should be deleted
        assertFalse(storageOperator.exists(dirPath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(dirPath))));

        // Child object should remain in the bucket
        assertTrue(storageOperator.exists(childAppPath));
        assertNotNull(storage.get(BlobId.of(BUCKET_NAME, childGcsKey)));
    }

    @Test
    void delete_directory_empty_markerDeleted() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "emptyDir");
        String expectedMarkerKey = toGcsDirKey(dirPath);

        storageOperator.createStorageDir(dirPath);

        assertTrue(storageOperator.exists(dirPath));
        Blob directBlob = storage.get(BlobId.of(BUCKET_NAME, expectedMarkerKey));
        assertNotNull(directBlob);
        assertTrue(directBlob.exists());

        Page<Blob> listed = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(expectedMarkerKey));
        assertTrue(listed.iterateAll().iterator().hasNext());

        storageOperator.delete(dirPath, true);

        assertFalse(storageOperator.exists(dirPath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, expectedMarkerKey)));
        Page<Blob> afterDelete = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(expectedMarkerKey));
        assertFalse(afterDelete.iterateAll().iterator().hasNext());
    }

    @Test
    void delete_directory_recursive_deletesMarkerAndNestedChildren() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "parentDir");
        storageOperator.createStorageDir(dirPath);

        String subDirPath = FileUtils.concatFilePath(dirPath, "subDir");
        storageOperator.createStorageDir(subDirPath);

        String file1App = FileUtils.concatFilePath(dirPath, "file1.txt");
        String file2App = FileUtils.concatFilePath(subDirPath, "file2.txt");
        String file1Key = toGcsKey(file1App);
        String file2Key = toGcsKey(file2App);
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, file1Key)).build(),
                "1".getBytes(StandardCharsets.UTF_8));
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, file2Key)).build(),
                "2".getBytes(StandardCharsets.UTF_8));

        storageOperator.delete(dirPath, true);

        assertFalse(storageOperator.exists(dirPath));
        assertFalse(storageOperator.exists(subDirPath));
        assertFalse(storageOperator.exists(file1App));
        assertFalse(storageOperator.exists(file2App));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(dirPath))));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(subDirPath))));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, file1Key)));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, file2Key)));
    }

    @Test
    void delete_directory_prefixBoundaryIsolation_doesNotMatchSiblingFolder() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "folder");
        String siblingDirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default",
                "resources", "folder2");

        storageOperator.createStorageDir(dirPath);
        storageOperator.createStorageDir(siblingDirPath);

        String file1App = FileUtils.concatFilePath(dirPath, "f1.txt");
        String file2App = FileUtils.concatFilePath(siblingDirPath, "f2.txt");
        String file1Key = toGcsKey(file1App);
        String file2Key = toGcsKey(file2App);
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, file1Key)).build(),
                "1".getBytes(StandardCharsets.UTF_8));
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, file2Key)).build(),
                "2".getBytes(StandardCharsets.UTF_8));

        storageOperator.delete(dirPath, true);

        // folder should be deleted
        assertFalse(storageOperator.exists(dirPath));
        assertFalse(storageOperator.exists(file1App));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(dirPath))));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, file1Key)));

        // folder2 and its child must remain untouched
        assertTrue(storageOperator.exists(siblingDirPath));
        assertTrue(storageOperator.exists(file2App));
        assertNotNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(siblingDirPath))));
        assertNotNull(storage.get(BlobId.of(BUCKET_NAME, file2Key)));
    }

    @Test
    void delete_directory_batchDeletion_moreThan100Objects() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "batchDir");
        storageOperator.createStorageDir(dirPath);

        int count = 250;
        List<String> fileKeys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String filePath = FileUtils.concatFilePath(dirPath, "item_" + i + ".txt");
            String fileKey = toGcsKey(filePath);
            fileKeys.add(fileKey);
            storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, fileKey)).build(), new byte[0]);
        }

        storageOperator.delete(dirPath, true);

        assertFalse(storageOperator.exists(dirPath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(dirPath))));
        for (String fileKey : fileKeys) {
            assertNull(storage.get(BlobId.of(BUCKET_NAME, fileKey)));
        }

        Page<Blob> remaining = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(toGcsDirKey(dirPath)));
        assertFalse(remaining.iterateAll().iterator().hasNext());
    }

    @Test
    void delete_directory_pagination_moreThan1000Objects() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "paginationDir");
        storageOperator.createStorageDir(dirPath);

        int count = 1050;
        for (int i = 0; i < count; i++) {
            String filePath = FileUtils.concatFilePath(dirPath, "paged_item_" + i + ".txt");
            String fileKey = toGcsKey(filePath);
            storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, fileKey)).build(), new byte[0]);
        }

        // Verify emulator listing returns multiple pages before deletion
        Page<Blob> initialPage = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(toGcsDirKey(dirPath)),
                Storage.BlobListOption.pageSize(1000));
        assertTrue(initialPage.hasNextPage());

        storageOperator.delete(dirPath, true);

        assertFalse(storageOperator.exists(dirPath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsDirKey(dirPath))));

        Page<Blob> remaining = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(toGcsDirKey(dirPath)));
        assertFalse(remaining.iterateAll().iterator().hasNext());
    }

    @Test
    void testExists() {
        String filePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "exists.txt");
        assertFalse(storageOperator.exists(filePath));

        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, toGcsKey(filePath))).build(), new byte[0]);
        assertTrue(storageOperator.exists(filePath));
    }

    @Test
    void testFetchFileContent() {
        String filePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "content.txt");
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, toGcsKey(filePath))).build(),
                "line1\nline2\nline3".getBytes(StandardCharsets.UTF_8));

        List<String> lines = storageOperator.fetchFileContent(filePath, 0, 10);
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
    }

    @Test
    void testUpload(@TempDir Path tempDir) throws Exception {
        Path localFile = tempDir.resolve("local-upload.txt");
        String expectedContent = "hello gcs upload";
        Files.write(localFile, expectedContent.getBytes(StandardCharsets.UTF_8));

        String dstPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "uploaded.txt");
        storageOperator.upload(localFile.toString(), dstPath, false, false);

        assertTrue(storageOperator.exists(dstPath));
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, toGcsKey(dstPath)));
        assertNotNull(blob);
        assertTrue(blob.exists());
        assertEquals(expectedContent, new String(blob.getContent(), StandardCharsets.UTF_8));
    }

    @Test
    void existsForRecursiveDelete_markerlessDirectoryWithChild_returnsTrueWhileExistsReturnsFalse() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "markerlessDir");
        String childPath = FileUtils.concatFilePath(dirPath, "child.txt");
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, toGcsKey(childPath))).build(),
                "child".getBytes(StandardCharsets.UTF_8));

        assertFalse(storageOperator.exists(dirPath));
        assertTrue(storageOperator.existsForRecursiveDelete(dirPath));

        storageOperator.delete(dirPath, true);
        assertFalse(storageOperator.existsForRecursiveDelete(dirPath));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsKey(childPath))));
    }

    @Test
    void existsForRecursiveDelete_markerlessDirectoryWithoutChildren_returnsFalse() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "missingMarkerlessDir");

        assertFalse(storageOperator.exists(dirPath));
        assertFalse(storageOperator.existsForRecursiveDelete(dirPath));
    }

    @Test
    void existsForRecursiveDelete_siblingPrefixOnly_returnsFalse() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "isolated-reports");
        String siblingChildPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default",
                "resources", "isolated-reports-old", "legacy.txt");
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, toGcsKey(siblingChildPath))).build(),
                "legacy".getBytes(StandardCharsets.UTF_8));

        assertFalse(storageOperator.existsForRecursiveDelete(dirPath));
    }

    @Test
    void existsForRecursiveDelete_missingFile_returnsFalse() {
        String missingFilePath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default",
                "resources", "missing-file.txt");

        assertFalse(storageOperator.existsForRecursiveDelete(missingFilePath));
    }

    @Test
    void delete_directory_partialBatchFailure_continuesOtherBatchesAndThrowsWithFailedKey() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "partialFailureDir");
        storageOperator.createStorageDir(dirPath);

        int totalFiles = 105;
        String failingKey = toGcsKey(FileUtils.concatFilePath(dirPath, "item_010.txt"));
        for (int i = 0; i < totalFiles; i++) {
            String fileName = String.format("item_%03d.txt", i);
            String fileKey = toGcsKey(FileUtils.concatFilePath(dirPath, fileName));
            storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, fileKey)).build(), new byte[0]);
        }

        // Delegate to real fake-gcs-server Storage, injecting Boolean.FALSE for one specific key in batch delete
        Storage faultInjectingStorage = (Storage) Proxy.newProxyInstance(
                Storage.class.getClassLoader(),
                new Class<?>[]{Storage.class},
                (proxy, method, args) -> {
                    if ("delete".equals(method.getName()) && args.length == 1 && args[0] instanceof Iterable) {
                        List<BlobId> ids = new ArrayList<>();
                        for (Object item : (Iterable<?>) args[0]) {
                            ids.add((BlobId) item);
                        }
                        List<BlobId> toActuallyDelete = new ArrayList<>();
                        int failIndex = -1;
                        for (int i = 0; i < ids.size(); i++) {
                            if (failingKey.equals(ids.get(i).getName())) {
                                failIndex = i;
                            } else {
                                toActuallyDelete.add(ids.get(i));
                            }
                        }
                        List<Boolean> realResults = toActuallyDelete.isEmpty()
                                ? new ArrayList<>()
                                : new ArrayList<>(storage.delete(toActuallyDelete));
                        if (failIndex >= 0) {
                            realResults.add(failIndex, Boolean.FALSE);
                        }
                        return realResults;
                    }
                    return method.invoke(storage, args);
                });

        GcsStorageOperator faultOperator = new GcsStorageOperator(RESOURCE_UPLOAD_PATH, BUCKET_NAME,
                faultInjectingStorage);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> faultOperator.delete(dirPath, true));
        assertTrue(ex.getMessage().contains(failingKey));

        // Verify the failing object remains while the rest (including the second batch) were deleted
        assertNotNull(storage.get(BlobId.of(BUCKET_NAME, failingKey)));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsKey(FileUtils.concatFilePath(dirPath, "item_000.txt")))));
        assertNull(storage.get(BlobId.of(BUCKET_NAME, toGcsKey(FileUtils.concatFilePath(dirPath, "item_104.txt")))));
    }

    @Test
    void delete_directory_unexpectedBatchResultCount_throwsException() {
        String dirPath = FileUtils.concatFilePath(storageOperator.getStorageBaseDirectory(), "default", "resources",
                "unexpectedResultDir");
        String fileKey = toGcsKey(FileUtils.concatFilePath(dirPath, "item.txt"));
        storage.create(BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, fileKey)).build(), new byte[0]);

        Storage truncatedResultStorage = (Storage) Proxy.newProxyInstance(
                Storage.class.getClassLoader(),
                new Class<?>[]{Storage.class},
                (proxy, method, args) -> {
                    if ("delete".equals(method.getName()) && args.length == 1 && args[0] instanceof Iterable) {
                        return Collections.emptyList();
                    }
                    return method.invoke(storage, args);
                });

        GcsStorageOperator faultOperator = new GcsStorageOperator(RESOURCE_UPLOAD_PATH, BUCKET_NAME,
                truncatedResultStorage);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> faultOperator.delete(dirPath, true));
        assertTrue(ex.getMessage().contains(fileKey));
    }
}
