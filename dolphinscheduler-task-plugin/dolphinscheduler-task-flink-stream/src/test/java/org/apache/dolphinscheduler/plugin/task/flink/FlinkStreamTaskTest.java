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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

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

        String initContent = new String(Files.readAllBytes(Paths.get(initScriptPath)), StandardCharsets.UTF_8);
        String nodeContent = new String(Files.readAllBytes(Paths.get(nodeScriptPath)), StandardCharsets.UTF_8);

        String expectedInitOptions = String.join(FlinkConstants.FLINK_SQL_NEWLINE,
                FlinkArgsUtils.buildInitOptionsForSql(flinkParameters)).concat(FlinkConstants.FLINK_SQL_NEWLINE);
        Assertions.assertEquals(expectedInitOptions + "SET 'date' = '20250601';", initContent);
        Assertions.assertEquals("SELECT * FROM logs WHERE dt = '20250601' AND env = 'prod'", nodeContent.trim());
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
        String nodeContent = new String(Files.readAllBytes(Paths.get(nodeScriptPath)), StandardCharsets.UTF_8);

        Assertions.assertEquals("INSERT INTO t SELECT * FROM s WHERE dt = '20210815'", nodeContent.trim());
    }

    private TaskExecutionContext buildSavepointTaskExecutionContext(String logContent) throws Exception {
        Path logPath = tempDir.resolve("task.log");
        Files.write(logPath, logContent.getBytes(StandardCharsets.UTF_8));

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId(16789);
        context.setTaskAppId("16789");
        context.setExecutePath(tempDir.toString());
        context.setLogPath(logPath.toString());
        context.setAppInfoPath(tempDir.resolve("appInfo.log").toString());
        return context;
    }

    @Test
    public void testSavePointCommand() throws Exception {
        // the task log contains both identifiers, the JobID is the one `flink savepoint` takes
        TaskExecutionContext context = buildSavepointTaskExecutionContext(
                "Job has been submitted with JobID 1234567890abcdef1234567890abcdef\n"
                        + "Submitted application application_1700000000000_0001");
        FlinkStreamTask task = Mockito.spy(new FlinkStreamTask(context));

        List<List<String>> executedCommands = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            executedCommands.add(invocation.getArgument(0));
            return true;
        }).when(task).executeFlinkCommand(Mockito.anyList());

        task.savePoint();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals("${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef",
                String.join(" ", executedCommands.get(0)));
        // the YARN application id is kept separate and is not passed to `flink savepoint`
        Assertions.assertNull(context.getAppIds());
    }

    @Test
    public void testSavePointFailWhenCommandFailed() throws Exception {
        TaskExecutionContext context = buildSavepointTaskExecutionContext(
                "Job has been submitted with JobID 1234567890abcdef1234567890abcdef");
        FlinkStreamTask task = Mockito.spy(new FlinkStreamTask(context));

        Mockito.doReturn(false).when(task).executeFlinkCommand(Mockito.anyList());

        Assertions.assertThrows(TaskException.class, task::savePoint);
    }

    @Test
    public void testSavePointFailWhenJobIdIsMissing() throws Exception {
        // a YARN application id cannot be used as a Flink JobID
        TaskExecutionContext context = buildSavepointTaskExecutionContext(
                "Submitted application application_1700000000000_0001");
        FlinkStreamTask task = Mockito.spy(new FlinkStreamTask(context));

        Assertions.assertThrows(TaskException.class, task::savePoint);
        Mockito.verify(task, Mockito.never()).executeFlinkCommand(Mockito.anyList());
    }
}
