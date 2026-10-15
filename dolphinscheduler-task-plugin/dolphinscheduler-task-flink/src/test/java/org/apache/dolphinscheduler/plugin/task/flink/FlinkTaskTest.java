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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mockito;

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

    private TaskExecutionContext buildTaskExecutionContext(String logContent) throws Exception {
        Path logPath = tempDir.resolve("task.log");
        Files.write(logPath, Collections.singletonList(logContent));

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId(16789);
        context.setTaskName("flink-test");
        context.setTaskAppId("16789");
        context.setExecutePath(tempDir.toString());
        context.setLogPath(logPath.toString());
        context.setAppInfoPath(tempDir.resolve("appInfo.log").toString());
        return context;
    }

    /**
     * Stub the cancel commands with the given results, in order, and record the executed commands.
     */
    private List<List<String>> stubCancelCommands(FlinkTask task, FlinkArgsUtils.FlinkCommandResult... results) {
        List<List<String>> executedCommands = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            executedCommands.add(invocation.getArgument(0));
            return results[Math.min(index.getAndIncrement(), results.length - 1)];
        }).when(task).executeFlinkCancelCommand(Mockito.anyList());
        return executedCommands;
    }

    private FlinkArgsUtils.FlinkCommandResult cancelSucceeded() {
        return FlinkArgsUtils.FlinkCommandResult.success("");
    }

    private FlinkArgsUtils.FlinkCommandResult cancelFailed(String output) {
        return FlinkArgsUtils.FlinkCommandResult.failure(output);
    }

    @Test
    public void testCancelApplicationCancelJobThroughFlinkCli() throws Exception {
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 1234567890abcdef1234567890abcdef");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef",
                String.join(" ", executedCommands.get(0)));
        // the Flink JobID must not leak into the application ids used by the YARN/K8s fallback
        Assertions.assertNull(context.getAppIds());
        // the client is stopped even when the remote job was cancelled, it may submit another job
        Mockito.verify(task).cancelClientByProcess();
    }

    @Test
    public void testCancelApplicationCancelSqlClientJobThroughFlinkCli() throws Exception {
        // the Flink SQL Client prints "Job ID: <id>", which is a different format from `flink run`
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 1234567890abcdef1234567890abcdef");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef",
                String.join(" ", executedCommands.get(0)));
    }

    @Test
    public void testCancelApplicationKeepsClusterConnectionOptions() throws Exception {
        // the job was submitted to a remote JobManager, so the cancel command must target it as well
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 1234567890abcdef1234567890abcdef");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SELECT 1;");
        parameters.setOthers("-m remote-jm:8081");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef -m remote-jm:8081",
                String.join(" ", executedCommands.get(0)));
    }

    @Test
    public void testCancelApplicationKeepsConnectionOptionsFromSqlScripts() throws Exception {
        // a SQL task can select the endpoint with SET statements instead of the extra options
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 1234567890abcdef1234567890abcdef");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setInitScript("SET 'rest.address' = 'remote-jm';\nSET 'rest.port' = '8081';");
        parameters.setRawScript("INSERT INTO t SELECT * FROM s;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef -Drest.address=remote-jm "
                        + "-Drest.port=8081",
                String.join(" ", executedCommands.get(0)));
    }

    @Test
    public void testCancelApplicationCollectsJobIdSubmittedWhileTheClientIsStopped() throws Exception {
        // a multi statement SQL script can submit another job while the client is being stopped, the
        // final snapshot has to contain it
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        Mockito.doAnswer(invocation -> {
            Files.write(Paths.get(context.getLogPath()),
                    "\nJob has been submitted with JobID 22222222222222222222222222222222"
                            .getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            return null;
        }).when(task).cancelClientByProcess();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222",
                String.join(" ", executedCommands.get(1)));

        // the client has to be stopped before the jobs are cancelled, otherwise it could submit a job
        // which is not part of the snapshot
        InOrder inOrder = Mockito.inOrder(task);
        inOrder.verify(task).cancelClientByProcess();
        inOrder.verify(task).awaitSubmissionOutputCollected(Mockito.anyLong());
        inOrder.verify(task, Mockito.atLeastOnce()).executeFlinkCancelCommand(Mockito.anyList());
    }

    @Test
    public void testCancelApplicationFailWhenCliFailed() throws Exception {
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 1234567890abcdef1234567890abcdef");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        stubCancelCommands(task, cancelFailed("Could not cancel job."));
        Mockito.doNothing().when(task).cancelClientByProcess();

        // the job was found but could not be cancelled, the failure must not be hidden
        Assertions.assertThrows(TaskException.class, task::cancelApplication);
        // the client process of the task must still be stopped, otherwise the task instance cannot
        // reach a final state
        Mockito.verify(task).cancelClientByProcess();
        Assertions.assertNull(context.getAppIds());
    }

    @Test
    public void testCancelApplicationAttemptsEveryJobId() throws Exception {
        // a SQL script can submit several jobs, a failure of the first one must not leave the others
        // running
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111\n"
                        + "Job has been submitted with JobID 22222222222222222222222222222222");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        List<List<String>> executedCommands = stubCancelCommands(task, cancelFailed("Could not cancel job."),
                cancelSucceeded());

        Assertions.assertThrows(TaskException.class, task::cancelApplication);

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationIgnoresJobAlreadyInTerminalState() throws Exception {
        // cancelling a job which already finished fails, but there is nothing left to cancel, so the
        // job which is still running must be cancelled without reporting a failure
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111\n"
                        + "Job has been submitted with JobID 22222222222222222222222222222222");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        List<List<String>> executedCommands = stubCancelCommands(task,
                cancelFailed("Job cancellation failed because the job has already reached another terminal state "
                        + "(FINISHED)."),
                cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
    }

    @Test
    public void testCancelApplicationFallbackWhenNoJobIdFound() throws Exception {
        TaskExecutionContext context = buildTaskExecutionContext("Flink job is running");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        // the task has never been started, so falling back must neither throw nor kill anything
        task.cancelApplication();

        Mockito.verify(task, Mockito.never()).executeFlinkCancelCommand(Mockito.anyList());
    }

    @Test
    public void testCancelApplicationSkipCliWhenSubmittedToYarn() throws Exception {
        TaskExecutionContext context = buildTaskExecutionContext(
                "Submitted application application_1700000000000_0001");
        FlinkTask task = Mockito.spy(new FlinkTask(context));

        task.cancelApplication();

        // the YARN application is cancelled through the resource manager by the fallback
        Mockito.verify(task, Mockito.never()).executeFlinkCancelCommand(Mockito.anyList());
    }
}
