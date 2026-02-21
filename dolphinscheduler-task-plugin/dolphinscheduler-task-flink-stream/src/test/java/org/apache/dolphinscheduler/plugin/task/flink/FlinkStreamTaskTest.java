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
 * Test FlinkStreamTask parameter replacement for initScript and rawScript.
 */
@ExtendWith(MockitoExtension.class)
class FlinkStreamTaskTest {

    @Test
    void testInitReplacesPlaceholdersInInitScriptAndRawScript() {
        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("bizdate", new Property("bizdate", Direct.IN, DataType.VARCHAR, "20250601"));
        prepareParamsMap.put("env", new Property("env", Direct.IN, DataType.VARCHAR, "prod"));

        FlinkStreamParameters taskParams = new FlinkStreamParameters();
        taskParams.setProgramType(ProgramType.SQL);
        taskParams.setInitScript("SET 'date' = '${bizdate}';");
        taskParams.setRawScript("SELECT * FROM logs WHERE dt = '${bizdate}' AND env = '${env}'");
        String taskParamsJson = JSONUtils.toJsonString(taskParams);

        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(taskParamsJson);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/flink-stream");
        when(taskExecutionContext.getTaskAppId()).thenReturn("stream-001");

        try (MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.generateScriptFile(Mockito.any(), Mockito.any())).then(inv -> null);

            FlinkStreamTask task = new FlinkStreamTask(taskExecutionContext);
            task.init();

            AbstractParameters params = task.getParameters();
            Assertions.assertInstanceOf(FlinkStreamParameters.class, params);
            FlinkStreamParameters flinkParams = (FlinkStreamParameters) params;
            Assertions.assertEquals("SET 'date' = '20250601';", flinkParams.getInitScript());
            Assertions.assertEquals("SELECT * FROM logs WHERE dt = '20250601' AND env = 'prod'",
                    flinkParams.getRawScript());
        }
    }

    @Test
    void testInitReplacesTimePlaceholderWhenParamMapContainsScheduleTime() {
        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put(TaskConstants.PARAMETER_DATETIME,
                new Property(TaskConstants.PARAMETER_DATETIME, Direct.IN, DataType.VARCHAR, "20210815080000"));

        FlinkStreamParameters taskParams = new FlinkStreamParameters();
        taskParams.setProgramType(ProgramType.SQL);
        taskParams.setInitScript("");
        taskParams.setRawScript("INSERT INTO t SELECT * FROM s WHERE dt = '$[yyyyMMdd]'");
        String taskParamsJson = JSONUtils.toJsonString(taskParams);

        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(taskParamsJson);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/flink-stream");
        when(taskExecutionContext.getTaskAppId()).thenReturn("stream-002");

        try (MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMock.when(() -> FileUtils.generateScriptFile(Mockito.any(), Mockito.any())).then(inv -> null);

            FlinkStreamTask task = new FlinkStreamTask(taskExecutionContext);
            task.init();

            FlinkStreamParameters flinkParams = (FlinkStreamParameters) task.getParameters();
            Assertions.assertEquals("INSERT INTO t SELECT * FROM s WHERE dt = '20210815'", flinkParams.getRawScript());
        }
    }
}
