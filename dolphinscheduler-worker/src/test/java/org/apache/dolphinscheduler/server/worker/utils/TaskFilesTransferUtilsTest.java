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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.curator.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.zeroturnaround.zip.ZipUtil;

public class TaskFilesTransferUtilsTest {

    private final long processDefineCode = 123;
    private final int processDefineVersion = 456;
    private final int processInstanceId = 678;
    private final int taskInstanceId = 789;
    private final String taskName = "test";

    private final String tenantCode = "ubuntu";

    private long startTime;

    private String exceptTemplate;

    @BeforeEach
    void init() {
        startTime = System.currentTimeMillis();
        String date = DateUtils.formatTimeStamp(startTime, DateTimeFormatter.ofPattern("yyyyMMdd"));
        exceptTemplate = String.format("%s/%s/%d/%d_%d/%s_%d",
                Constants.RESOURCE_TAG,
                date,
                processDefineCode,
                processDefineVersion,
                processInstanceId,
                taskName,
                taskInstanceId);
    }

    @Test
    void testTryUploadOutputFiles() throws IOException {
        File executePath = Files.createTempDir();
        File folderPath = new File(executePath, "data");
        File file = new File(folderPath.getPath() + "/test.txt");
        if (!(folderPath.mkdirs() && file.createNewFile())) {
            return;
        }
        String fileParamValue = String.format("%s/%s", folderPath.getName(), file.getName());
        String params = "[" +
                String.format("{\"prop\":\"folder\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"%s\"},",
                        folderPath.getName())
                +
                String.format(" {\"prop\":\"file\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"%s\"},",
                        fileParamValue)
                +
                "{\"prop\":\"a\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"a\"}," +
                "{\"prop\":\"b\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"b\"}" +
                "]";
        String taskParams = String.format("{\"localParams\": %s}", params);
        List<Property> oriVarPool = new ArrayList<>(4);
        oriVarPool.add(new Property("folder", Direct.OUT, DataType.FILE, folderPath.getName()));
        oriVarPool.add(new Property("a", Direct.OUT, DataType.VARCHAR, "a"));
        oriVarPool.add(new Property("b", Direct.OUT, DataType.VARCHAR, "b"));
        List<Property> varPool = new ArrayList<>(oriVarPool);
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .taskParams(taskParams)
                .workflowInstanceId(processInstanceId)
                .workflowDefinitionVersion(processDefineVersion)
                .workflowDefinitionCode(processDefineCode)
                .taskInstanceId(taskInstanceId)
                .taskName(taskName)
                .tenantCode(tenantCode)
                .executePath(executePath.toString())
                .startTime(startTime)
                .varPool(varPool)
                .build();

        StorageOperator storageOperator = Mockito.mock(StorageOperator.class);
        List<Property> uploadOutputFiles =
                TaskFilesTransferUtils.tryUploadOutputFiles(taskExecutionContext, storageOperator);

        Assertions.assertNotNull(uploadOutputFiles);
        Assertions.assertEquals(2, uploadOutputFiles.size());

        Assertions.assertEquals("folder", uploadOutputFiles.get(0).getProp());
        Assertions.assertEquals(folderPath.getName(), uploadOutputFiles.get(0).getValue());

        Assertions.assertEquals("file", uploadOutputFiles.get(1).getProp());
        Assertions.assertEquals(fileParamValue, uploadOutputFiles.get(1).getValue());

        String exceptFolder =
                String.format("%s_%s", exceptTemplate, folderPath.getName() + Constants.PACK_SUFFIX);
        String exceptFile = String.format("%s_%s", exceptTemplate, file.getName());
        List<Property> contextVarPool = taskExecutionContext.getVarPool();
        Assertions.assertEquals(4, contextVarPool.size());

        Assertions.assertEquals(String.format("%s.%s", taskName, "folder"), contextVarPool.get(0).getProp());
        Assertions.assertEquals(exceptFolder, contextVarPool.get(0).getValue());

        Assertions.assertEquals(oriVarPool.get(1).getProp(), contextVarPool.get(1).getProp());
        Assertions.assertEquals(oriVarPool.get(2).getValue(), contextVarPool.get(2).getValue());

        Assertions.assertEquals(String.format("%s.%s", taskName, "file"), contextVarPool.get(3).getProp());
        Assertions.assertEquals(exceptFile, contextVarPool.get(3).getValue());
    }

    @Test
    void testTryDownloadUpstreamFiles(@TempDir Path tempDir) {
        File executePath = tempDir.toFile();
        String folderPath = exceptTemplate + "_folder" + Constants.PACK_SUFFIX;
        String filePath = exceptTemplate + "_file";

        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("task1.folder", new Property("folder", Direct.IN, DataType.FILE, folderPath));
        prepareParamsMap.put("task2.file", new Property("file", Direct.IN, DataType.FILE, filePath));
        prepareParamsMap.put("a", new Property("a", Direct.OUT, DataType.VARCHAR, "a"));
        String localParams = "[" +
                "{\"prop\":\"folder\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task1.folder\"}," +
                " {\"prop\":\"file\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task2.file\"}," +
                " {\"prop\":\"a\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"a\"}" +
                "]";
        String taskParams = String.format("{\"localParams\": %s}", localParams);
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .prepareParamsMap(prepareParamsMap)
                .taskParams(taskParams)
                .workflowInstanceId(processInstanceId)
                .workflowDefinitionVersion(processDefineVersion)
                .workflowDefinitionCode(processDefineCode)
                .taskInstanceId(taskInstanceId)
                .taskName(taskName)
                .tenantCode(tenantCode)
                .executePath(executePath.toString())
                .startTime(startTime)
                .build();

        StorageOperator storageOperator = Mockito.mock(StorageOperator.class);
        Mockito.mockStatic(ZipUtil.class);
        TaskChannel taskChannel = Mockito.mock(TaskChannel.class);
        AbstractParameters abstractParameters = Mockito.mock(AbstractParameters.class);
        Mockito.when(abstractParameters.getLocalParams()).thenReturn(JSONUtils.toList(localParams, Property.class));
        Mockito.when(taskChannel.parseParameters(Mockito.anyString())).thenReturn(abstractParameters);

        List<Property> downloadUpstreamFiles = TaskFilesTransferUtils.tryDownloadUpstreamFiles(
                taskChannel,
                taskExecutionContext,
                storageOperator);

        Assertions.assertNotNull(downloadUpstreamFiles);
        Assertions.assertEquals(2, downloadUpstreamFiles.size());

        Assertions.assertEquals("folder", downloadUpstreamFiles.get(0).getProp());
        Assertions.assertEquals("task1.folder", downloadUpstreamFiles.get(0).getValue());

        Assertions.assertEquals("file", downloadUpstreamFiles.get(1).getProp());
        Assertions.assertEquals("task2.file", downloadUpstreamFiles.get(1).getValue());
    }

    @Test
    void testGetOutFileLocalParams() {
        String taskParams = "{\"localParams\":[" +
                "{\"prop\":\"inputFile\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task1.data\"}," +
                "{\"prop\":\"outputFile\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"data\"}," +
                "{\"prop\":\"a\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"a\"}," +
                "{\"prop\":\"b\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"b\"}" +
                "]}";
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(taskParams);

        List<Property> fileLocalParamsOut = TaskFilesTransferUtils.getOutFileLocalParams(taskExecutionContext);
        Assertions.assertEquals(1, fileLocalParamsOut.size());
        Assertions.assertEquals("outputFile", fileLocalParamsOut.get(0).getProp());
        Assertions.assertEquals("data", fileLocalParamsOut.get(0).getValue());
    }

    @Test
    void testGetResourcePath() {
        String fileName = "test.txt";
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);

        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(startTime);

        Mockito.when(taskExecutionContext.getWorkflowDefinitionCode()).thenReturn(processDefineCode);
        Mockito.when(taskExecutionContext.getWorkflowDefinitionVersion()).thenReturn(processDefineVersion);
        Mockito.when(taskExecutionContext.getWorkflowInstanceId()).thenReturn(processInstanceId);
        Mockito.when(taskExecutionContext.getTaskInstanceId()).thenReturn(taskInstanceId);
        Mockito.when(taskExecutionContext.getTaskName()).thenReturn(taskName);

        String except = String.format("%s_%s", exceptTemplate, fileName);
        Assertions.assertEquals(except, TaskFilesTransferUtils.buildResourcePath(taskExecutionContext, fileName));

    }

    @Test
    void testPackIfDirectory(@TempDir Path tempDir) throws Exception {
        File folderPath = tempDir.toFile();
        File file1 = new File(folderPath.getPath() + "/test.txt");
        File file2 = new File(folderPath.getPath() + "/test.zip");
        boolean isSuccess1 = file1.createNewFile();
        boolean isSuccess2 = file2.createNewFile();

        Assertions.assertTrue(isSuccess1);
        Assertions.assertTrue(isSuccess2);

        Assertions.assertEquals(file1.getPath(), TaskFilesTransferUtils.packIfDirectory(file1.getPath()));
        Assertions.assertEquals(file2.getPath(), TaskFilesTransferUtils.packIfDirectory(file2.getPath()));

        String expectFolderPackPath = folderPath.getPath() + Constants.PACK_SUFFIX;
        Assertions.assertEquals(expectFolderPackPath, TaskFilesTransferUtils.packIfDirectory(folderPath.getPath()));
    }
}
