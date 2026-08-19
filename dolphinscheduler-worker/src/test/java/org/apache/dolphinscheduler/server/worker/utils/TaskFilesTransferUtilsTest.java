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

package org.apache.dolphinscheduler.server.worker.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskFilesTransferUtilsTest {

    @TempDir
    Path tempDir;

    private TaskExecutionContext taskExecutionContext;

    @BeforeEach
    void setUp() {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(100);
        taskExecutionContext.setTaskName("test-task");
        taskExecutionContext.setStartTime(System.currentTimeMillis());
        taskExecutionContext.setWorkflowDefinitionCode(1000L);
        taskExecutionContext.setWorkflowDefinitionVersion(1);
        taskExecutionContext.setWorkflowInstanceId(200);
        taskExecutionContext.setTenantCode("default");
        taskExecutionContext.setExecutePath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // Clean up temp directory
        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {
        }
    }

    @Test
    void testGetFileLocalParams_WithValidOutFileParams() {
        String taskParams = "{\"localParams\":[{\"prop\":\"output.csv\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"result.csv\"}]}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> result = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);

        assertEquals(1, result.size());
        assertEquals("output.csv", result.get(0).getProp());
        assertEquals(Direct.OUT, result.get(0).getDirect());
        assertEquals(DataType.FILE, result.get(0).getType());
    }

    @Test
    void testGetFileLocalParams_WithValidInFileParams() {
        String taskParams = "{\"localParams\":[{\"prop\":\"input.csv\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"upstream.csv\"}]}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> result = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.IN);

        assertEquals(1, result.size());
        assertEquals("input.csv", result.get(0).getProp());
        assertEquals(Direct.IN, result.get(0).getDirect());
    }

    @Test
    void testGetFileLocalParams_WithMixedParams() {
        String taskParams = "{\"localParams\":["
                + "{\"prop\":\"output.csv\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"result.csv\"},"
                + "{\"prop\":\"input.csv\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"upstream.csv\"},"
                + "{\"prop\":\"var1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"hello\"}"
                + "]}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> outResult = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);
        assertEquals(1, outResult.size());
        assertEquals("output.csv", outResult.get(0).getProp());

        List<Property> inResult = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.IN);
        assertEquals(1, inResult.size());
        assertEquals("input.csv", inResult.get(0).getProp());
    }

    @Test
    void testGetFileLocalParams_WithNullLocalParams() {
        String taskParams = "{\"otherField\":\"value\"}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> result = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFileLocalParams_WithEmptyLocalParams() {
        String taskParams = "{\"localParams\":[]}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> result = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFileLocalParams_WithEmptyTaskParams() {
        String taskParams = "{}";
        taskExecutionContext.setTaskParams(taskParams);

        List<Property> result = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuildResourcePath() {
        taskExecutionContext.setStartTime(1700000000000L); // 2023-11-14 22:13:20 UTC
        String fileName = "test-file.csv";

        String resourcePath = TaskFilesTransferUtils.buildResourcePath(taskExecutionContext, fileName);

        assertNotNull(resourcePath);
        assertTrue(resourcePath.startsWith("DATA_TRANSFER/"));
        assertTrue(resourcePath.contains("/1000/1_200/"));
        assertTrue(resourcePath.contains("test-task_100_test-file.csv"));
    }

    @Test
    void testBuildResourcePath_WithSpacesInTaskName() {
        taskExecutionContext.setTaskName("test task with spaces");
        String fileName = "output.csv";

        String resourcePath = TaskFilesTransferUtils.buildResourcePath(taskExecutionContext, fileName);

        assertNotNull(resourcePath);
        assertTrue(resourcePath.contains("test_task_with_spaces"));
    }

    @Test
    void testPackIfDirectory_FileExists() throws IOException {
        Path testFile = tempDir.resolve("test-file.txt");
        Files.writeString(testFile, "test content");

        String result = TaskFilesTransferUtils.packIfDirectory(testFile.toString());

        assertEquals(testFile.toString(), result);
    }

    @Test
    void testPackIfDirectory_Directory() throws IOException {
        Path testDir = tempDir.resolve("test-dir");
        Files.createDirectory(testDir);
        Files.writeString(testDir.resolve("file1.txt"), "content1");
        Files.writeString(testDir.resolve("file2.txt"), "content2");

        String result = TaskFilesTransferUtils.packIfDirectory(testDir.toString());

        assertTrue(result.endsWith("_ds_pack.zip"));
        File zipFile = new File(result);
        assertTrue(zipFile.exists());
    }

    @Test
    void testPackIfDirectory_FileNotExists() {
        String nonExistentPath = tempDir.resolve("non-existent").toString();

        assertThrows(TaskException.class, () -> {
            TaskFilesTransferUtils.packIfDirectory(nonExistentPath);
        });
    }

    @Test
    void testUploadOutputFiles_NoOutputFiles() {
        taskExecutionContext.setTaskParams("{}");
        StorageOperator mockStorage = mock(StorageOperator.class);

        List<Property> result = TaskFilesTransferUtils.uploadOutputFiles(taskExecutionContext, mockStorage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUploadOutputFiles_WithOutputFile() throws Exception {
        // Create a test file
        Path testFile = tempDir.resolve("result.csv");
        Files.writeString(testFile, "col1,col2\n1,2");

        String taskParams = "{\"localParams\":[{\"prop\":\"output.csv\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"result.csv\"}]}";
        taskExecutionContext.setTaskParams(taskParams);
        taskExecutionContext.setVarPool(new ArrayList<>());

        StorageOperator mockStorage = mock(StorageOperator.class);
        when(mockStorage.getStorageFileAbsolutePath(anyString(), anyString()))
                .thenReturn("/storage/path/file")
                .thenReturn("/storage/path/file.crc");
        doNothing().when(mockStorage).upload(anyString(), anyString(), anyBoolean(), anyBoolean());

        List<Property> result = TaskFilesTransferUtils.uploadOutputFiles(taskExecutionContext, mockStorage);

        assertEquals(1, result.size());
        assertEquals("output.csv", result.get(0).getProp());
        verify(mockStorage, times(2)).upload(anyString(), anyString(), anyBoolean(), anyBoolean());
    }

    @Test
    void testDownloadUpstreamFiles_NoInputFiles() {
        taskExecutionContext.setTaskParams("{}");
        StorageOperator mockStorage = mock(StorageOperator.class);

        List<Property> result = TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, mockStorage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDownloadUpstreamFiles_WithInputFile() {
        String taskParams = "{\"localParams\":[{\"prop\":\"input.csv\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"ot.output-data\"}]}";
        taskExecutionContext.setTaskParams(taskParams);

        // Set up varPool with upstream entry
        Property upstreamProp = new Property("ot.output-data", Direct.OUT, DataType.FILE, "DATA_TRANSFER/20231114/1000/1_200/ot_101_result.csv");
        List<Property> varPool = new ArrayList<>();
        varPool.add(upstreamProp);
        taskExecutionContext.setVarPool(varPool);

        StorageOperator mockStorage = mock(StorageOperator.class);
        when(mockStorage.getStorageFileAbsolutePath(anyString(), anyString()))
                .thenReturn("/storage/path/result.csv");
        doNothing().when(mockStorage).download(anyString(), anyString(), anyBoolean());

        List<Property> result = TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, mockStorage);

        assertEquals(1, result.size());
        assertEquals("input.csv", result.get(0).getProp());
        verify(mockStorage, times(1)).download(anyString(), anyString(), anyBoolean());
    }

    @Test
    void testDownloadUpstreamFiles_UpstreamKeyNotFound() {
        String taskParams = "{\"localParams\":[{\"prop\":\"input.csv\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"ot.non-existent\"}]}";
        taskExecutionContext.setTaskParams(taskParams);

        // varPool has different key
        Property upstreamProp = new Property("ot.other", Direct.OUT, DataType.FILE, "DATA_TRANSFER/20231114/1000/1_200/ot_101_result.csv");
        List<Property> varPool = new ArrayList<>();
        varPool.add(upstreamProp);
        taskExecutionContext.setVarPool(varPool);

        StorageOperator mockStorage = mock(StorageOperator.class);

        assertThrows(TaskException.class, () -> {
            TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, mockStorage);
        });
    }

    @Test
    void testDownloadUpstreamFiles_PackedFile() throws IOException {
        String taskParams = "{\"localParams\":[{\"prop\":\"input-dir\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"ot.output-dir\"}]}";
        taskExecutionContext.setTaskParams(taskParams);

        // Set up varPool with packed upstream entry
        Property upstreamProp = new Property("ot.output-dir", Direct.OUT, DataType.FILE,
                "DATA_TRANSFER/20231114/1000/1_200/ot_101_result_ds_pack.zip");
        List<Property> varPool = new ArrayList<>();
        varPool.add(upstreamProp);
        taskExecutionContext.setVarPool(varPool);

        StorageOperator mockStorage = mock(StorageOperator.class);
        when(mockStorage.getStorageFileAbsolutePath(anyString(), anyString()))
                .thenReturn("/storage/path/result_ds_pack.zip");
        doNothing().when(mockStorage).download(anyString(), anyString(), anyBoolean());

        List<Property> result = TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, mockStorage);

        assertEquals(1, result.size());
        verify(mockStorage, times(1)).download(anyString(), anyString(), anyBoolean());
    }

    @Test
    void testDownloadUpstreamFiles_EmptyVarPool() {
        String taskParams = "{\"localParams\":[{\"prop\":\"input.csv\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"ot.output-data\"}]}";
        taskExecutionContext.setTaskParams(taskParams);
        taskExecutionContext.setVarPool(Collections.emptyList());

        StorageOperator mockStorage = mock(StorageOperator.class);

        List<Property> result = TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, mockStorage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}