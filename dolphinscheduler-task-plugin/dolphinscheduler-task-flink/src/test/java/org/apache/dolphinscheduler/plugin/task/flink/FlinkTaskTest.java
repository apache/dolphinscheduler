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
    public void testCancelApplicationCancelsEachJobWithItsOwnClusterConnectionOptions() throws Exception {
        // the script selects another endpoint before it submits the second job, so the first job must
        // not be cancelled against the second endpoint
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111\n"
                        + "Job has been submitted with JobID 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET 'rest.address' = 'cluster-a';\n"
                + "INSERT INTO t SELECT * FROM s;\n"
                + "SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO u SELECT * FROM v;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());

        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111 -Drest.address=cluster-a",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationKeepsTheEndpointOfEachJobWithCommentsAndReset() throws Exception {
        // a comment before the INSERT must not shift the snapshots, and RESET must restore the
        // endpoint of the submission for the job which follows it
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111\n"
                        + "Job has been submitted with JobID 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setOthers("-Drest.address=default-jm");
        parameters.setRawScript("/* first job */ SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO t SELECT * FROM s;\n"
                + "-- back to the default endpoint\n"
                + "RESET 'rest.address';\n"
                + "INSERT INTO u SELECT * FROM v;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=default-jm",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationKeepsTheEndpointOfASingleInsertStatementSet() throws Exception {
        // a statement set with one INSERT is submitted as a single job, the snapshot of that job must
        // not be lost, otherwise the first job is cancelled against the second endpoint
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job has been submitted with JobID 11111111111111111111111111111111\n"
                        + "Job has been submitted with JobID 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET 'rest.address' = 'cluster-a';\n"
                + "EXECUTE STATEMENT SET\n"
                + "BEGIN\n"
                + "INSERT INTO t SELECT * FROM s;\n"
                + "END;\n"
                + "SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO u SELECT * FROM v;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111 -Drest.address=cluster-a",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationKeepsEscapedQuotesInConnectionOptions() throws Exception {
        // a doubled quote in a SQL string is an escaped quote, the effective value must reach the CLI
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 1234567890abcdef1234567890abcdef");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET 'security.ssl.rest.truststore-password' = 'pa''ss';\n"
                + "INSERT INTO t SELECT * FROM s;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                        + "-Dsecurity.ssl.rest.truststore-password='pa'\\''ss'",
                String.join(" ", executedCommands.get(0)));
    }

    @Test
    public void testCancelApplicationKeepsTheEndpointOfACreateTableAsSelectJob() throws Exception {
        // a CREATE TABLE ... AS SELECT statement submits a job, it must not be missing from the
        // snapshots, otherwise the following job is cancelled against the wrong endpoint
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 11111111111111111111111111111111\n"
                        + "Job ID: 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET 'rest.address' = 'cluster-a';\n"
                + "CREATE TABLE t AS SELECT * FROM s;\n"
                + "SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO u SELECT * FROM v;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111 -Drest.address=cluster-a",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationKeepsTheEndpointWhenACommentSeparatesTokens() throws Exception {
        // a block comment between two keywords must not join them, otherwise the statement is not
        // recognized and the snapshots are shifted
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 11111111111111111111111111111111\n"
                        + "Job ID: 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET/* endpoint */ 'rest.address' = 'cluster-a';\n"
                + "INSERT/* first job */INTO t SELECT * FROM s;\n"
                + "SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO u SELECT * FROM v;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded(), cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(2, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 11111111111111111111111111111111 -Drest.address=cluster-a",
                String.join(" ", executedCommands.get(0)));
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(1)));
    }

    @Test
    public void testCancelApplicationIgnoresTheSynchronousJobOfAnotherEndpoint() throws Exception {
        // a synchronous update statement is completed by the client and prints no JobID, so it is not
        // cancelled: the asynchronous job which follows is the only one and keeps its own endpoint
        TaskExecutionContext context = buildTaskExecutionContext(
                "Job ID: 22222222222222222222222222222222");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setDeployMode(FlinkDeployMode.STANDALONE);
        parameters.setRawScript("SET 'table.dml-sync' = 'true';\n"
                + "SET 'rest.address' = 'cluster-a';\n"
                + "INSERT INTO first_sink SELECT * FROM bounded_source;\n"
                + "SET 'table.dml-sync' = 'false';\n"
                + "SET 'rest.address' = 'cluster-b';\n"
                + "INSERT INTO second_sink SELECT * FROM streaming_source;");
        context.setTaskParams(JSONUtils.toJsonString(parameters));

        FlinkTask task = Mockito.spy(new FlinkTask(context));
        task.init();

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222 -Drest.address=cluster-b",
                String.join(" ", executedCommands.get(0)));
    }

    @Test
    public void testCancelApplicationFailsWhenASynchronousSubmissionIsStillRunning() throws Exception {
        // the client waits for the synchronous statement instead of printing its JobID, so stopping
        // the client is not proof that the remote job is cancelled: the cancellation must fail
        TaskExecutionContext context = buildTaskExecutionContext(
                "Execute statement in sync mode. Please wait for the execution finish...");

        FlinkTask task = Mockito.spy(new FlinkTask(context));

        Assertions.assertThrows(TaskException.class, task::cancelApplication);
        Mockito.verify(task, Mockito.never()).executeFlinkCancelCommand(Mockito.anyList());
    }

    @Test
    public void testCancelApplicationIgnoresAFailedSynchronousSubmission() throws Exception {
        // the client prints the failure message instead of the completion message when the synchronous
        // statement fails, so it does not wait for that submission any more: a stop which races with
        // the failure must not report a submission which cannot be cancelled
        TaskExecutionContext context = buildTaskExecutionContext(
                "Execute statement in sync mode. Please wait for the execution finish...\n"
                        + "Could not execute SQL statement.\n"
                        + "org.apache.flink.table.client.gui.shell.SqlClientException: the source table "
                        + "does not exist");

        FlinkTask task = Mockito.spy(new FlinkTask(context));

        task.cancelApplication();

        Assertions.assertEquals(0, task.countPendingSynchronousSubmissions());
        Mockito.verify(task, Mockito.never()).executeFlinkCancelCommand(Mockito.anyList());
    }

    @Test
    public void testCancelApplicationFailsWhenTheSubmissionAfterAFailureIsStillRunning() throws Exception {
        // a failed submission ends the previous synchronous execution, but the following one is still
        // running, so the task must still report the submission which cannot be cancelled
        TaskExecutionContext context = buildTaskExecutionContext(
                "Execute statement in sync mode. Please wait for the execution finish...\n"
                        + "Could not execute SQL statement.\n"
                        + "Execute statement in sync mode. Please wait for the execution finish...");

        FlinkTask task = Mockito.spy(new FlinkTask(context));

        Assertions.assertThrows(TaskException.class, task::cancelApplication);
        Assertions.assertEquals(1, task.countPendingSynchronousSubmissions());
        Mockito.verify(task, Mockito.never()).executeFlinkCancelCommand(Mockito.anyList());
    }

    @Test
    public void testCancelApplicationAfterACompletedSynchronousSubmission() throws Exception {
        // a synchronous statement which has finished has nothing left to cancel, only the
        // asynchronous job is cancelled
        TaskExecutionContext context = buildTaskExecutionContext(
                "Execute statement in sync mode. Please wait for the execution finish...\n"
                        + "Complete execution of the SQL update statement.\n"
                        + "Job ID: 22222222222222222222222222222222");

        FlinkTask task = Mockito.spy(new FlinkTask(context));

        List<List<String>> executedCommands = stubCancelCommands(task, cancelSucceeded());
        task.cancelApplication();

        Assertions.assertEquals(1, executedCommands.size());
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 22222222222222222222222222222222",
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
