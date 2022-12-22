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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.curator.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.zeroturnaround.zip.ZipUtil;

public class TaskFilesTransferUtilsTest {

    private final long processDefineCode = 123;
    private final int processDefineVersion = 456;
    private final int processInstanceId = 678;
    private final int taskInstanceId = 789;
    private final String taskName = "test";

    private final String tenantCode = "ubuntu";

    private long endTime;

    private String exceptTemplate;

    @BeforeEach
    void init() {
        endTime = System.currentTimeMillis();
        String date = DateUtils.formatTimeStamp(endTime, DateTimeFormatter.ofPattern("yyyyMMdd"));
        exceptTemplate = String.format("%s/%s/%d/%d_%d/%s_%d",
                TaskFilesTransferUtils.RESOURCE_TAG,
                date,
                processDefineCode,
                processDefineVersion,
                processInstanceId,
                taskName,
                taskInstanceId);
    }

    @Test
    void testUploadOutputFiles() throws IOException {
        File executePath = Files.createTempDir();
        File folderPath = new File(executePath, "data");
        File file = new File(folderPath.getPath() + "/test.txt");
        if (!(folderPath.mkdirs() && file.createNewFile())) {
            return;
        }
        String varPool = "[" +
                String.format("{\"prop\":\"folder\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"%s\"},",
                        folderPath.getName())
                +
                String.format(" {\"prop\":\"file\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"%s/%s\"},",
                        folderPath.getName(), file.getName())
                +
                "{\"prop\":\"a\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"a\"}," +
                "{\"prop\":\"b\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"b\"}" +
                "]";
        String taskParams = String.format("{\"localParams\": %s}", varPool);
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .varPool(varPool)
                .taskParams(taskParams)
                .processInstanceId(processInstanceId)
                .processDefineVersion(processDefineVersion)
                .processDefineCode(processDefineCode)
                .taskInstanceId(taskInstanceId)
                .taskName(taskName)
                .tenantCode(tenantCode)
                .executePath(executePath.toString())
                .endTime(endTime)
                .build();

        List<Property> oriProperties = TaskFilesTransferUtils.getVarPools(taskExecutionContext);

        StorageOperate storageOperate = Mockito.mock(StorageOperate.class);
        TaskFilesTransferUtils.uploadOutputFiles(taskExecutionContext, storageOperate);
        System.out.println(taskExecutionContext.getVarPool());

        String exceptFolder =
                String.format("%s_%s", exceptTemplate, folderPath.getName() + TaskFilesTransferUtils.PACK_SUFFIX);
        String exceptFile = String.format("%s_%s", exceptTemplate, file.getName());

        List<Property> properties = TaskFilesTransferUtils.getVarPools(taskExecutionContext);
        Assertions.assertEquals(4, properties.size());

        Assertions.assertEquals(String.format("%s.%s", taskName, "folder"), properties.get(0).getProp());
        Assertions.assertEquals(exceptFolder, properties.get(0).getValue());

        Assertions.assertEquals(String.format("%s.%s", taskName, "file"), properties.get(1).getProp());
        Assertions.assertEquals(exceptFile, properties.get(1).getValue());

        Assertions.assertEquals(oriProperties.get(2).getProp(), properties.get(2).getProp());
        Assertions.assertEquals(oriProperties.get(3).getValue(), properties.get(3).getValue());

    }

    @Test
    void testDownloadUpstreamFiles() {
        File executePath = Files.createTempDir();
        String folderPath = exceptTemplate + "_folder" + TaskFilesTransferUtils.PACK_SUFFIX;
        String filePath = exceptTemplate + "_file";
        String varPool = "[" +
                String.format(
                        "{\"prop\":\"task1.folder\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"%s\"},", folderPath)
                +
                String.format(" {\"prop\":\"task2.file\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"%s\"},",
                        filePath)
                +
                "{\"prop\":\"a\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"a\"}," +
                "{\"prop\":\"b\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"b\"}" +
                "]";
        String varPoolParams = "[" +
                "{\"prop\":\"folder\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task1.folder\"}," +
                " {\"prop\":\"file\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task2.file\"}" +
                "]";
        String taskParams = String.format("{\"localParams\": %s}", varPoolParams);
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .varPool(varPool)
                .taskParams(taskParams)
                .processInstanceId(processInstanceId)
                .processDefineVersion(processDefineVersion)
                .processDefineCode(processDefineCode)
                .taskInstanceId(taskInstanceId)
                .taskName(taskName)
                .tenantCode(tenantCode)
                .executePath(executePath.toString())
                .endTime(endTime)
                .build();

        StorageOperate storageOperate = Mockito.mock(StorageOperate.class);
        Mockito.mockStatic(ZipUtil.class);
        Assertions.assertDoesNotThrow(
                () -> TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, storageOperate));
    }

    @Test
    void testGetFileLocalParams() {
        String taskParams = "{\"localParams\":[" +
                "{\"prop\":\"inputFile\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task1.data\"}," +
                "{\"prop\":\"outputFile\",\"direct\":\"OUT\",\"type\":\"FILE\",\"value\":\"data\"}," +
                "{\"prop\":\"a\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"a\"}," +
                "{\"prop\":\"b\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"b\"}" +
                "]}";
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(taskParams);

        List<Property> fileLocalParamsIn = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.IN);
        Assertions.assertEquals(1, fileLocalParamsIn.size());
        Assertions.assertEquals("inputFile", fileLocalParamsIn.get(0).getProp());
        Assertions.assertEquals("task1.data", fileLocalParamsIn.get(0).getValue());

        List<Property> fileLocalParamsOut = TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT);
        Assertions.assertEquals(1, fileLocalParamsOut.size());
        Assertions.assertEquals("outputFile", fileLocalParamsOut.get(0).getProp());
        Assertions.assertEquals("data", fileLocalParamsOut.get(0).getValue());

    }

    @Test
    void testGetResourcePath() {
        String fileName = "test.txt";
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);

        Mockito.when(taskExecutionContext.getEndTime()).thenReturn(endTime);

        Mockito.when(taskExecutionContext.getProcessDefineCode()).thenReturn(processDefineCode);
        Mockito.when(taskExecutionContext.getProcessDefineVersion()).thenReturn(processDefineVersion);
        Mockito.when(taskExecutionContext.getProcessInstanceId()).thenReturn(processInstanceId);
        Mockito.when(taskExecutionContext.getTaskInstanceId()).thenReturn(taskInstanceId);
        Mockito.when(taskExecutionContext.getTaskName()).thenReturn(taskName);

        String except = String.format("%s_%s", exceptTemplate, fileName);
        Assertions.assertEquals(except, TaskFilesTransferUtils.getResourcePath(taskExecutionContext, fileName));

    }

    @Test
    void testGetVarPools() {
        String varPoolsString = "[" +
                "{\"prop\":\"input\",\"direct\":\"IN\",\"type\":\"FILE\",\"value\":\"task1.output\"}" +
                ",{\"prop\":\"a\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"${a}\"}" +
                "]";
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getVarPool()).thenReturn(varPoolsString);

        List<Property> varPools = TaskFilesTransferUtils.getVarPools(taskExecutionContext);
        Assertions.assertEquals(2, varPools.size());

        Property varPool0 = varPools.get(0);
        Assertions.assertEquals("input", varPool0.getProp());
        Assertions.assertEquals(Direct.IN, varPool0.getDirect());
        Assertions.assertEquals(DataType.FILE, varPool0.getType());
        Assertions.assertEquals("task1.output", varPool0.getValue());

        Property varPool1 = varPools.get(1);
        Assertions.assertEquals("a", varPool1.getProp());
        Assertions.assertEquals(Direct.IN, varPool1.getDirect());
        Assertions.assertEquals(DataType.VARCHAR, varPool1.getType());
        Assertions.assertEquals("${a}", varPool1.getValue());

        Mockito.when(taskExecutionContext.getVarPool()).thenReturn("[]");
        List<Property> varPoolsEmpty = TaskFilesTransferUtils.getVarPools(taskExecutionContext);
        Assertions.assertEquals(0, varPoolsEmpty.size());

        Mockito.when(taskExecutionContext.getVarPool()).thenReturn(null);
        List<Property> varPoolsNull = TaskFilesTransferUtils.getVarPools(taskExecutionContext);
        Assertions.assertEquals(0, varPoolsNull.size());

    }

    @Test
    void testPackIfDir() throws Exception {
        File folderPath = Files.createTempDir();
        File file1 = new File(folderPath.getPath() + "/test.txt");
        File file2 = new File(folderPath.getPath() + "/test.zip");
        boolean isSuccess1 = file1.createNewFile();
        boolean isSuccess2 = file2.createNewFile();

        Assertions.assertTrue(isSuccess1);
        Assertions.assertTrue(isSuccess2);

        Assertions.assertEquals(file1.getPath(), TaskFilesTransferUtils.packIfDir(file1.getPath()));
        Assertions.assertEquals(file2.getPath(), TaskFilesTransferUtils.packIfDir(file2.getPath()));

        String expectFolderPackPath = folderPath.getPath() + TaskFilesTransferUtils.PACK_SUFFIX;
        Assertions.assertEquals(expectFolderPackPath, TaskFilesTransferUtils.packIfDir(folderPath.getPath()));
    }
}
