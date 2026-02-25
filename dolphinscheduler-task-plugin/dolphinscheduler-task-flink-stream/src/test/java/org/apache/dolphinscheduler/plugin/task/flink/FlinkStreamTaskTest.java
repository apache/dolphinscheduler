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

import static org.apache.dolphinscheduler.common.constants.DateConstants.PARAMETER_DATETIME;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * FlinkStreamTask unit test. Verifies parameter replacement in initScript and rawScript without Mockito.
 */
public class FlinkStreamTaskTest {

    @TempDir
    Path tempDir;

    @Test
    public void testParameterReplacementInScript() throws Exception {
        String executePath = tempDir.toString();
        String taskAppId = "test-app";

        FlinkStreamParameters flinkParameters = new FlinkStreamParameters();
        flinkParameters.setProgramType(ProgramType.SQL);
        flinkParameters.setDeployMode(FlinkDeployMode.LOCAL);
        flinkParameters.setParallelism(2);
        flinkParameters.setInitScript("SET 'date' = '${bizdate}';");
        flinkParameters.setRawScript("SELECT * FROM logs WHERE dt = '${bizdate}' AND env = '${env}'");

        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("bizdate", new Property("bizdate", null, null, "20250601"));
        prepareParamsMap.put("env", new Property("env", null, null, "prod"));

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskParams(JSONUtils.toJsonString(flinkParameters));
        context.setExecutePath(executePath);
        context.setTaskAppId(taskAppId);
        context.setPrepareParamsMap(prepareParamsMap);

        FlinkStreamTask task = new FlinkStreamTask(context);
        task.init();
        task.getScript();

        String initScriptPath = String.format("%s/%s_init.sql", executePath, taskAppId);
        String nodeScriptPath = String.format("%s/%s_node.sql", executePath, taskAppId);

        String initContent = Files.readString(Path.of(initScriptPath), StandardCharsets.UTF_8);
        String nodeContent = Files.readString(Path.of(nodeScriptPath), StandardCharsets.UTF_8);

        Assertions.assertTrue(initContent.contains("SET 'date' = '20250601';"),
                "Expected ${bizdate} to be replaced, got: " + initContent);
        Assertions.assertTrue(nodeContent.contains("dt = '20250601'"),
                "Expected ${bizdate} to be replaced, got: " + nodeContent);
        Assertions.assertTrue(nodeContent.contains("env = 'prod'"),
                "Expected ${env} to be replaced, got: " + nodeContent);
    }

    @Test
    public void testParameterReplacementTimePlaceholder() throws Exception {
        String executePath = tempDir.toString();
        String taskAppId = "test-time";

        FlinkStreamParameters flinkParameters = new FlinkStreamParameters();
        flinkParameters.setProgramType(ProgramType.SQL);
        flinkParameters.setDeployMode(FlinkDeployMode.LOCAL);
        flinkParameters.setParallelism(2);
        flinkParameters.setInitScript("");
        flinkParameters.setRawScript("INSERT INTO t SELECT * FROM s WHERE dt = '$[yyyyMMdd]'");

        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put(PARAMETER_DATETIME, new Property(PARAMETER_DATETIME, null, null, "20210815080000"));

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskParams(JSONUtils.toJsonString(flinkParameters));
        context.setExecutePath(executePath);
        context.setTaskAppId(taskAppId);
        context.setPrepareParamsMap(prepareParamsMap);

        FlinkStreamTask task = new FlinkStreamTask(context);
        task.init();
        task.getScript();

        String nodeScriptPath = String.format("%s/%s_node.sql", executePath, taskAppId);
        String nodeContent = Files.readString(Path.of(nodeScriptPath), StandardCharsets.UTF_8);

        Assertions.assertTrue(nodeContent.contains("dt = '20210815'"),
                "Expected $[yyyyMMdd] to be replaced with 20210815, got: " + nodeContent);
    }
}
