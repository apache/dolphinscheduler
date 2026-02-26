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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FlinkTaskTest {

    @TempDir
    Path tempDir;

    @Test
    public void testParameterReplacementInScript() throws Exception {
        String executePath = tempDir.toString();
        String taskAppId = "test-app";

        FlinkParameters flinkParameters = new FlinkParameters();
        flinkParameters.setProgramType(ProgramType.SQL);
        flinkParameters.setDeployMode(FlinkDeployMode.LOCAL);
        flinkParameters.setParallelism(2);
        flinkParameters.setInitScript("set batch_size=${batch_size};");
        flinkParameters.setRawScript("SELECT * FROM logs WHERE dt='$[yyyyMMdd]';");

        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("batch_size", new Property("batch_size", null, null, "1000"));
        prepareParamsMap.put(PARAMETER_DATETIME, new Property(PARAMETER_DATETIME, null, null, "20201201120000"));

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskParams(JSONUtils.toJsonString(flinkParameters));
        context.setExecutePath(executePath);
        context.setTaskAppId(taskAppId);
        context.setPrepareParamsMap(prepareParamsMap);

        FlinkTask task = new FlinkTask(context);
        task.init();
        task.getScript();

        String initScriptPath = String.format("%s/%s_init.sql", executePath, taskAppId);
        String nodeScriptPath = String.format("%s/%s_node.sql", executePath, taskAppId);

        String initContent = new String(Files.readAllBytes(Paths.get(initScriptPath)), StandardCharsets.UTF_8);
        String nodeContent = new String(Files.readAllBytes(Paths.get(nodeScriptPath)), StandardCharsets.UTF_8);

        String expectedInitOptions = String.join(FlinkConstants.FLINK_SQL_NEWLINE,
                FlinkArgsUtils.buildInitOptionsForSql(flinkParameters)).concat(FlinkConstants.FLINK_SQL_NEWLINE);
        Assertions.assertEquals(expectedInitOptions + "set batch_size=1000;", initContent);
        Assertions.assertEquals("SELECT * FROM logs WHERE dt='20201201';", nodeContent.trim());
    }

    @Test
    public void testParameterReplacementWithNullParamsMap() throws Exception {
        String executePath = tempDir.toString();
        String taskAppId = "test-null-params";

        FlinkParameters flinkParameters = new FlinkParameters();
        flinkParameters.setProgramType(ProgramType.SQL);
        flinkParameters.setDeployMode(FlinkDeployMode.LOCAL);
        flinkParameters.setParallelism(2);
        flinkParameters.setInitScript("");
        flinkParameters.setRawScript("SELECT 1;");

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskParams(JSONUtils.toJsonString(flinkParameters));
        context.setExecutePath(executePath);
        context.setTaskAppId(taskAppId);
        context.setPrepareParamsMap(null);

        FlinkTask task = new FlinkTask(context);
        task.init();
        String script = task.getScript();

        String nodeScriptPath = String.format("%s/%s_node.sql", executePath, taskAppId);
        String nodeContent = new String(Files.readAllBytes(Paths.get(nodeScriptPath)), StandardCharsets.UTF_8);
        Assertions.assertEquals("SELECT 1;", nodeContent.trim());
        Assertions.assertNotNull(script);
    }
}
