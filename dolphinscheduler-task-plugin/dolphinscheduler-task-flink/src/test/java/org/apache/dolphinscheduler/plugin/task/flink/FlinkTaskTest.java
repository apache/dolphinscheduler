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

package org.apache.dolphinscheduler.plugin.task.flink;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test FlinkTask parameter replacement for initScript and rawScript.
 */
@ExtendWith(MockitoExtension.class)
class FlinkTaskTest {

    @Test
    void testInitReplacesPlaceholdersInInitScriptAndRawScript() {
        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("bizdate", new Property("bizdate", Direct.IN, DataType.VARCHAR, "20250101"));
        prepareParamsMap.put("customVar", new Property("customVar", Direct.IN, DataType.VARCHAR, "hello"));

        FlinkParameters taskParams = new FlinkParameters();
        taskParams.setProgramType(ProgramType.SQL);
        taskParams.setInitScript("SET 'dt' = '${bizdate}';");
        taskParams.setRawScript("SELECT * FROM orders WHERE dt = '${bizdate}' AND tag = '${customVar}'");
        String taskParamsJson = JSONUtils.toJsonString(taskParams);

        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(taskParamsJson);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/flink");
        when(taskExecutionContext.getTaskAppId()).thenReturn("task-001");

        try (MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.generateScriptFile(Mockito.any(), Mockito.any())).then(inv -> null);

            FlinkTask task = new FlinkTask(taskExecutionContext);
            task.init();

            AbstractParameters params = task.getParameters();
            Assertions.assertInstanceOf(FlinkParameters.class, params);
            FlinkParameters flinkParams = (FlinkParameters) params;
            Assertions.assertEquals("SET 'dt' = '20250101';", flinkParams.getInitScript());
            Assertions.assertEquals("SELECT * FROM orders WHERE dt = '20250101' AND tag = 'hello'",
                    flinkParams.getRawScript());
        }
    }

    @Test
    void testInitReplacesTimePlaceholderWhenParamMapContainsScheduleTime() {
        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put(TaskConstants.PARAMETER_DATETIME,
                new Property(TaskConstants.PARAMETER_DATETIME, Direct.IN, DataType.VARCHAR, "20201201120000"));

        FlinkParameters taskParams = new FlinkParameters();
        taskParams.setProgramType(ProgramType.SQL);
        taskParams.setInitScript("");
        taskParams.setRawScript("SELECT * FROM t WHERE dt = '$[yyyyMMdd]'");
        String taskParamsJson = JSONUtils.toJsonString(taskParams);

        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(taskParamsJson);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/flink");
        when(taskExecutionContext.getTaskAppId()).thenReturn("task-002");

        try (MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.generateScriptFile(Mockito.any(), Mockito.any())).then(inv -> null);

            FlinkTask task = new FlinkTask(taskExecutionContext);
            task.init();

            FlinkParameters flinkParams = (FlinkParameters) task.getParameters();
            // $[yyyyMMdd] with schedule time 20201201120000 -> 20201201
            Assertions.assertEquals("SELECT * FROM t WHERE dt = '20201201'", flinkParams.getRawScript());
        }
    }

    @Test
    void testInitWithEmptyPrepareParamsMapStillReplacesTimePlaceholders() {
        Map<String, Property> prepareParamsMap = new HashMap<>();

        FlinkParameters taskParams = new FlinkParameters();
        taskParams.setProgramType(ProgramType.SQL);
        taskParams.setInitScript("SET dt = '$[yyyyMMdd]';");
        taskParams.setRawScript("SELECT * FROM t WHERE dt = '$[yyyyMMdd]'");
        String taskParamsJson = JSONUtils.toJsonString(taskParams);

        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(taskParamsJson);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/flink");
        when(taskExecutionContext.getTaskAppId()).thenReturn("task-003");

        try (MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.generateScriptFile(Mockito.any(), Mockito.any())).then(inv -> null);

            FlinkTask task = new FlinkTask(taskExecutionContext);
            task.init();

            FlinkParameters flinkParams = (FlinkParameters) task.getParameters();
            // Even with empty paramsMap, time placeholders $[yyyyMMdd] should still be replaced
            // (using current date/time)
            String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            Assertions.assertTrue(flinkParams.getInitScript().contains(today) || 
                    flinkParams.getInitScript().matches("SET dt = '\\d{8}';"));
            Assertions.assertTrue(flinkParams.getRawScript().contains(today) || 
                    flinkParams.getRawScript().matches("SELECT \\* FROM t WHERE dt = '\\d{8}'"));
        }
    }
}
