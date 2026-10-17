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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class FlinkArgsUtilsTest {

    @TempDir
    Path tempDir;

    private String joinStringListWithSpace(List<String> stringList) {
        return String.join(" ", stringList);
    }

    private FlinkParameters buildTestFlinkParametersWithDeployMode(FlinkDeployMode flinkDeployMode) {
        FlinkParameters flinkParameters = new FlinkParameters();
        flinkParameters.setProgramType(ProgramType.SCALA);
        flinkParameters.setDeployMode(flinkDeployMode);
        flinkParameters.setParallelism(4);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName("/opt/job.jar");
        flinkParameters.setMainJar(resourceInfo);
        flinkParameters.setMainClass("org.example.Main");
        flinkParameters.setSlot(4);
        flinkParameters.setAppName("demo-app-name");
        flinkParameters.setJobManagerMemory("1024m");
        flinkParameters.setTaskManagerMemory("1024m");

        return flinkParameters;
    }
    private TaskExecutionContext buildTestTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("app-id");
        taskExecutionContext.setExecutePath("/tmp/execution");

        ResourceContext.ResourceItem resourceItem = new ResourceContext.ResourceItem();
        resourceItem.setResourceAbsolutePathInLocal("/opt/job.jar");
        resourceItem.setResourceAbsolutePathInStorage("/opt/job.jar");

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem);
        taskExecutionContext.setResourceContext(resourceContext);
        return taskExecutionContext;
    }

    @Test
    public void testRunJarInApplicationMode() throws Exception {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.APPLICATION);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run-application -t yarn-application -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testRunJarInClusterMode() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER);
        flinkParameters.setFlinkVersion("1.11");
        List<String> commandLine1 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -m yarn-cluster -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine1));

        flinkParameters.setFlinkVersion("<1.10");
        List<String> commandLine2 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -m yarn-cluster -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine2));

        flinkParameters.setFlinkVersion(">=1.12");
        List<String> commandLine3 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -t yarn-per-job -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine3));
    }

    @Test
    public void testRunJarInLocalMode() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.LOCAL);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -p 4 -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testRunSql() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER);
        flinkParameters.setProgramType(ProgramType.SQL);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/sql-client.sh -i /tmp/execution/app-id_init.sql -f /tmp/execution/app-id_node.sql",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testInitOptionsInLocalMode() {
        List<String> initOptions =
                FlinkArgsUtils.buildInitOptionsForSql(buildTestFlinkParametersWithDeployMode(FlinkDeployMode.LOCAL));
        Assertions.assertEquals(2, initOptions.size());
        Assertions.assertTrue(initOptions.contains("set execution.target=local"));
        Assertions.assertTrue(initOptions.contains("set parallelism.default=4"));
    }

    @Test
    public void testInitOptionsInClusterMode() throws Exception {
        List<String> initOptions = FlinkArgsUtils
                .buildInitOptionsForSql(buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER));
        Assertions.assertEquals(6, initOptions.size());
        Assertions.assertTrue(initOptions.contains("set execution.target=yarn-per-job"));
        Assertions.assertTrue(initOptions.contains("set taskmanager.numberOfTaskSlots=4"));
        Assertions.assertTrue(initOptions.contains("set yarn.application.name=demo-app-name"));
        Assertions.assertTrue(initOptions.contains("set jobmanager.memory.process.size=1024m"));
        Assertions.assertTrue(initOptions.contains("set taskmanager.memory.process.size=1024m"));
        Assertions.assertTrue(initOptions.contains("set parallelism.default=4"));
    }

    @Test
    public void testBuildCancelCommandLine() {
        // ${FLINK_HOME} is resolved when the command is executed, in the same environment as the task
        List<String> commandLine =
                FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", Collections.emptyList());

        Assertions.assertEquals("${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testBuildCancelCommandLineKeepsClusterConnectionOptions() {
        // the job was submitted to a remote JobManager, the cancel command must target it as well
        List<String> commandLine = FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef",
                Arrays.asList("-m", "remote-jm:8081"));

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef -m remote-jm:8081",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testBuildSavePointCommandLine() {
        // the Flink JobID is passed explicitly, the YARN/K8s application id is not used here
        List<String> commandLine = FlinkArgsUtils.buildSavePointCommandLine("1234567890abcdef1234567890abcdef",
                null, Collections.emptyList());

        Assertions.assertEquals("${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testBuildSavePointCommandLineWithYarnApplicationId() {
        // the YARN application id only targets the cluster, it is passed after the JobID as -yid
        List<String> commandLine = FlinkArgsUtils.buildSavePointCommandLine("1234567890abcdef1234567890abcdef",
                "application_1700000000000_0001", Collections.emptyList());

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef -yid application_1700000000000_0001",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testBuildSavePointCommandLineKeepsClusterConnectionOptions() {
        List<String> commandLine = FlinkArgsUtils.buildSavePointCommandLine("1234567890abcdef1234567890abcdef",
                "application_1700000000000_0001", Arrays.asList("-m", "remote-jm:8081"));

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef -yid application_1700000000000_0001 -m remote-jm:8081",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testExtractClusterConnectionOptions() {
        // the short and the long form of the connection options are kept, in the submission order
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "--jobmanager remote-jm:8081 -t yarn-per-job");

        Assertions.assertEquals(
                Arrays.asList("--jobmanager", "remote-jm:8081", "-t", "yarn-per-job"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsWithAttachedAndSeparatedValues() {
        // --jobmanager=<address> and -D <key>=<value> are accepted by the flink CLI as well
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "--jobmanager=remote-jm:8081 -D rest.port=8082");

        Assertions.assertEquals(
                Arrays.asList("--jobmanager=remote-jm:8081", "-D", "rest.port=8082"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsOnlyClusterConfigs() {
        // only the -D options which identify the cluster are kept
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "-Drest.address=remote-jm -Djobmanager.rpc.port=6123 -Dkubernetes.cluster-id=my-cluster "
                        + "-Dexecution.target=yarn-per-job -Dyarn.application.id=application_1700000000000_0001");

        Assertions.assertEquals(
                Arrays.asList("-Drest.address=remote-jm", "-Djobmanager.rpc.port=6123",
                        "-Dkubernetes.cluster-id=my-cluster", "-Dexecution.target=yarn-per-job",
                        "-Dyarn.application.id=application_1700000000000_0001"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsRestTlsConfigs() {
        // a submission over TLS has to keep the REST TLS settings on the cancel / savepoint command
        // as well, otherwise it falls back to a plain connection
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "-m remote-jm:8081 -Dsecurity.ssl.rest.enabled=true "
                        + "-Dsecurity.ssl.rest.truststore=/opt/flink/truststore "
                        + "-Dparallelism.default=4");

        Assertions.assertEquals(
                Arrays.asList("-m", "remote-jm:8081", "-Dsecurity.ssl.rest.enabled=true",
                        "-Dsecurity.ssl.rest.truststore=/opt/flink/truststore"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsFromSqlScripts() {
        // a SQL task configures its connection with SET statements as well
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptionsFromSql(
                "SET 'rest.address' = 'remote-jm';\nSET 'rest.port'='8081';",
                "set security.ssl.rest.enabled=true;\nset parallelism.default=4;");

        Assertions.assertEquals(
                Arrays.asList("-Drest.address=remote-jm", "-Drest.port=8081", "-Dsecurity.ssl.rest.enabled=true"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsFromSqlScriptsOnlyMatchesStatements() {
        // a SET inside a string literal or a comment is not a statement, the double quoted form is not
        // the SQL Client syntax, and several statements on one line are all matched
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptionsFromSql(
                "INSERT INTO t SELECT 'SET rest.address=literal';\n"
                        + "SET \"rest.address\" = \"quoted\";\n"
                        + "-- SET rest.port = 9999;\n"
                        + "SET rest.address = remote-jm; SET rest.port = 8081;");

        Assertions.assertEquals(Arrays.asList("-Drest.address=remote-jm", "-Drest.port=8081"), options);
    }

    @Test
    public void testMergeClusterConnectionOptions() {
        // a later option for the same key wins, so a connection setting is passed once
        List<String> options = FlinkArgsUtils.mergeClusterConnectionOptions(
                Arrays.asList("-m", "default-jm:8081", "-Drest.port=8081"),
                Arrays.asList("-Drest.address=remote-jm", "-Drest.port=8082"));

        Assertions.assertEquals(
                Arrays.asList("-m", "default-jm:8081", "-Drest.port=8082", "-Drest.address=remote-jm"),
                options);
    }

    @Test
    public void testMergeClusterConnectionOptionsKeepsSeparatedOptions() {
        // "-D <key>=<value>" is one option which is given as two arguments, merging it must not drop
        // the second -D and leave its value behind as a bare argument
        List<String> options = FlinkArgsUtils.mergeClusterConnectionOptions(
                FlinkArgsUtils.extractClusterConnectionOptions("-D rest.address=remote-jm -D rest.port=8081"),
                Collections.emptyList());

        Assertions.assertEquals(
                Arrays.asList("-D", "rest.address=remote-jm", "-D", "rest.port=8081"), options);

        List<String> commandLine =
                FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", options);
        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                        + "-D rest.address=remote-jm -D rest.port=8081",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testExtractClusterConnectionOptionsFromSqlScriptsQuotesShellValues() {
        // a value which contains a space or a shell metacharacter has to reach the flink CLI as a
        // single argument, while a plain value is left as it is
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptionsFromSql(
                "SET 'security.ssl.rest.truststore' = '/opt/flink configs/client.jks';",
                "SET 'security.ssl.rest.keystore' = '/opt/($HOME)/client.jks';",
                "SET 'rest.address' = 'plain-host';");

        Assertions.assertEquals(
                Arrays.asList("-Dsecurity.ssl.rest.truststore='/opt/flink configs/client.jks'",
                        "-Dsecurity.ssl.rest.keystore='/opt/($HOME)/client.jks'",
                        "-Drest.address=plain-host"),
                options);
    }

    @Test
    public void testExtractClusterConnectionOptionsPerJob() {
        // the SQL Client executes the statements in order, so a script which changes the connection
        // between two jobs keeps the settings which were effective for each of them
        List<List<String>> optionsPerJob = FlinkArgsUtils.extractClusterConnectionOptionsPerJob("-m cli-jm:8081",
                "SET 'rest.address'='cluster-a';\nINSERT INTO t SELECT * FROM s;",
                "SET 'rest.address'='cluster-b';\nINSERT INTO u SELECT * FROM v;");

        Assertions.assertEquals(2, optionsPerJob.size());
        Assertions.assertEquals(Arrays.asList("-m", "cli-jm:8081", "-Drest.address=cluster-a"),
                optionsPerJob.get(0));
        Assertions.assertEquals(Arrays.asList("-m", "cli-jm:8081", "-Drest.address=cluster-b"),
                optionsPerJob.get(1));
    }

    @Test
    public void testExtractClusterConnectionOptionsPerJobIgnoresStatementsWhichSubmitNoJob() {
        // a statement which does not submit a job must not consume a snapshot, otherwise the settings
        // of the jobs which follow it would be shifted
        List<List<String>> optionsPerJob = FlinkArgsUtils.extractClusterConnectionOptionsPerJob(null,
                "SET 'rest.address'='cluster-a';\nCREATE TABLE t (x INT);\nSELECT * FROM t;",
                "INSERT INTO t SELECT * FROM s;");

        Assertions.assertEquals(1, optionsPerJob.size());
        Assertions.assertEquals(Arrays.asList("-Drest.address=cluster-a"), optionsPerJob.get(0));
    }

    @Test
    public void testExtractClusterConnectionOptionsPerJobCountsAStatementSetAsOneJob() {
        // the update statements of a statement set are submitted as a single job, so the settings of
        // the job which follows the set are not shifted
        List<List<String>> optionsPerJob = FlinkArgsUtils.extractClusterConnectionOptionsPerJob(null,
                "SET 'rest.address'='cluster-a';\n"
                        + "EXECUTE STATEMENT SET\n"
                        + "BEGIN\n"
                        + "INSERT INTO t SELECT * FROM s;\n"
                        + "INSERT INTO u SELECT * FROM v;\n"
                        + "END;\n"
                        + "SET 'rest.address'='cluster-b';\n"
                        + "INSERT INTO w SELECT * FROM x;");

        Assertions.assertEquals(2, optionsPerJob.size());
        Assertions.assertEquals(Arrays.asList("-Drest.address=cluster-a"), optionsPerJob.get(0));
        Assertions.assertEquals(Arrays.asList("-Drest.address=cluster-b"), optionsPerJob.get(1));
    }

    @Test
    public void testExtractClusterConnectionOptionsDropsSubmissionOnlyOptions() {
        // the parallelism, slots, memory, application name, main class, savepoint and configuration
        // of the submission only matter at submission time
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "-m remote-jm:8081 -p 4 -ys 2 -ynm demo-app -yjm 1024m -ytm 2048m -c org.example.Main "
                        + "-s hdfs:///flink/savepoint-1 -d -Dparallelism.default=4 "
                        + "-Dtaskmanager.memory.process.size=1024m");

        Assertions.assertEquals(Arrays.asList("-m", "remote-jm:8081"), options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsQuotedValues() {
        // a quoted address keeps its whitespace and is not split
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions("-ynm \"my app\" -m \"remote jm:8081\"");

        Assertions.assertEquals(Arrays.asList("-m", "\"remote jm:8081\""), options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsQuotesInsideTokens() {
        // the quote does not start the token, the value with a space stays in the same token
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "-Dkubernetes.config.file=\"/opt/flink configs/kube.yaml\" -Dparallelism.default=4");

        Assertions.assertEquals(
                Collections.singletonList("-Dkubernetes.config.file=\"/opt/flink configs/kube.yaml\""), options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsSeparatedQuotedValues() {
        // the value of a separated -D option may contain a quoted segment with a space as well
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(
                "-D kubernetes.config.file=\"/opt/flink configs/kube.yaml\"");

        Assertions.assertEquals(
                Arrays.asList("-D", "kubernetes.config.file=\"/opt/flink configs/kube.yaml\""), options);
    }

    @Test
    public void testExtractClusterConnectionOptionsKeepsWholeQuotedOptions() {
        // the quotes may wrap the whole key=value pair, or the whole option
        Assertions.assertEquals(
                Arrays.asList("-D", "\"kubernetes.config.file=/opt/flink configs/kube.yaml\""),
                FlinkArgsUtils.extractClusterConnectionOptions(
                        "-D \"kubernetes.config.file=/opt/flink configs/kube.yaml\""));

        Assertions.assertEquals(
                Collections.singletonList("\"-Dkubernetes.config.file=/opt/flink configs/kube.yaml\""),
                FlinkArgsUtils.extractClusterConnectionOptions(
                        "\"-Dkubernetes.config.file=/opt/flink configs/kube.yaml\""));
    }

    @Test
    public void testExtractClusterConnectionOptionsWithoutOthers() {
        Assertions.assertTrue(FlinkArgsUtils.extractClusterConnectionOptions(null).isEmpty());
        Assertions.assertTrue(FlinkArgsUtils.extractClusterConnectionOptions("   ").isEmpty());
    }

    @Test
    public void testExecuteCommandReuseTaskEnvironment() throws Exception {
        // FLINK_HOME is only defined in the task environment, not in the task parameters
        Path executePath = tempDir.resolve("execute");
        Files.createDirectories(executePath);
        Path flinkHome = tempDir.resolve("flink");
        Files.createDirectories(flinkHome.resolve("bin"));
        Path markerFile = tempDir.resolve("invoked.txt");
        Path flinkCommand = flinkHome.resolve("bin/flink");
        // write more output than a pipe buffer holds, so the command would block if it is not consumed
        Files.write(flinkCommand, Arrays.asList(
                "#!/bin/bash",
                "echo \"$@\" > " + markerFile,
                "for i in $(seq 1 20000); do echo \"line $i\"; done"));
        Assertions.assertTrue(flinkCommand.toFile().setExecutable(true));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(16789);
        taskExecutionContext.setTaskAppId("16789");
        taskExecutionContext.setExecutePath(executePath.toString());
        taskExecutionContext.setTenantCode("");
        taskExecutionContext.setEnvironmentConfig("export FLINK_HOME=" + flinkHome);
        taskExecutionContext.setPrepareParamsMap(Collections.emptyMap());

        boolean success = FlinkArgsUtils.executeCommand(taskExecutionContext,
                FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", Collections.emptyList()),
                false);

        Assertions.assertTrue(success);
        Assertions.assertEquals("cancel 1234567890abcdef1234567890abcdef",
                new String(Files.readAllBytes(markerFile), StandardCharsets.UTF_8).trim());
    }

    @Test
    public void testExecuteCommandTimeoutKillsTheCommand() throws Exception {
        // the command outlives its timeout, so it must be killed and reported as a failure
        Path executePath = tempDir.resolve("timeout-execute");
        Files.createDirectories(executePath);
        Path flinkHome = tempDir.resolve("flink-timeout");
        Files.createDirectories(flinkHome.resolve("bin"));
        Path flinkCommand = flinkHome.resolve("bin/flink");
        Files.write(flinkCommand, Arrays.asList("#!/bin/bash", "sleep 30"));
        Assertions.assertTrue(flinkCommand.toFile().setExecutable(true));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(16789);
        taskExecutionContext.setTaskAppId("16789");
        taskExecutionContext.setExecutePath(executePath.toString());
        taskExecutionContext.setTenantCode("");
        taskExecutionContext.setEnvironmentConfig("export FLINK_HOME=" + flinkHome);
        taskExecutionContext.setPrepareParamsMap(Collections.emptyMap());

        long start = System.currentTimeMillis();
        boolean success = FlinkArgsUtils.executeCommand(taskExecutionContext,
                FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", Collections.emptyList()),
                false, 1);
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertFalse(success);
        // the command is killed when the timeout expires, it does not wait for the command to finish
        Assertions.assertTrue(elapsed < 20_000, "the command must be killed on timeout, elapsed: " + elapsed + "ms");
    }

    @Test
    public void testExecuteCommandTimeoutTerminatesTheProcessTree() throws Exception {
        // the started process is the bootstrap shell, the flink CLI runs as its descendant, so the
        // process tree has to be terminated instead of only the bootstrap process
        Path executePath = tempDir.resolve("tree-execute");
        Files.createDirectories(executePath);
        Path flinkHome = tempDir.resolve("flink-tree");
        Files.createDirectories(flinkHome.resolve("bin"));
        Path flinkCommand = flinkHome.resolve("bin/flink");
        Files.write(flinkCommand, Arrays.asList("#!/bin/bash", "sleep 30"));
        Assertions.assertTrue(flinkCommand.toFile().setExecutable(true));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(16789);
        taskExecutionContext.setTaskAppId("16789");
        taskExecutionContext.setExecutePath(executePath.toString());
        taskExecutionContext.setTenantCode("");
        taskExecutionContext.setEnvironmentConfig("export FLINK_HOME=" + flinkHome);
        taskExecutionContext.setPrepareParamsMap(Collections.emptyMap());

        try (MockedStatic<ProcessUtils> processUtils = Mockito.mockStatic(ProcessUtils.class)) {
            processUtils.when(() -> ProcessUtils.getProcessId(Mockito.any())).thenReturn(4321);
            processUtils.when(() -> ProcessUtils.kill(4321, "")).thenReturn(true);

            boolean success = FlinkArgsUtils.executeCommand(taskExecutionContext,
                    FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", Collections.emptyList()),
                    false, 1);

            Assertions.assertFalse(success);
            processUtils.verify(() -> ProcessUtils.kill(4321, ""));
        }
    }

    @Test
    public void testExecuteCommandTimeoutStopsTheDescendantProcess() throws Exception {
        Path executePath = tempDir.resolve("descendant-execute");
        Files.createDirectories(executePath);
        Path flinkHome = tempDir.resolve("flink-descendant");
        Files.createDirectories(flinkHome.resolve("bin"));
        Path descendantPidFile = tempDir.resolve("descendant.pid");
        Path flinkCommand = flinkHome.resolve("bin/flink");
        // the background process is the descendant which must not outlive the timed out command
        Files.write(flinkCommand, Arrays.asList(
                "#!/bin/bash",
                "sleep 300 &",
                "echo $! > " + descendantPidFile,
                "wait"));
        Assertions.assertTrue(flinkCommand.toFile().setExecutable(true));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(16789);
        taskExecutionContext.setTaskAppId("16789");
        taskExecutionContext.setExecutePath(executePath.toString());
        taskExecutionContext.setTenantCode("");
        taskExecutionContext.setEnvironmentConfig("export FLINK_HOME=" + flinkHome);
        taskExecutionContext.setPrepareParamsMap(Collections.emptyMap());

        boolean success = FlinkArgsUtils.executeCommand(taskExecutionContext,
                FlinkArgsUtils.buildCancelCommandLine("1234567890abcdef1234567890abcdef", Collections.emptyList()),
                false, 1);

        Assertions.assertFalse(success);
        int descendantPid = Integer
                .parseInt(new String(Files.readAllBytes(descendantPidFile), StandardCharsets.UTF_8).trim());
        Assertions.assertFalse(isProcessAlive(descendantPid),
                "the descendant process " + descendantPid + " must be terminated with the command");
    }

    private static boolean isProcessAlive(int processId) throws Exception {
        return new ProcessBuilder("/bin/kill", "-0", String.valueOf(processId)).start().waitFor() == 0;
    }
}
