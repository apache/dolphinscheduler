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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.OSUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * flink args utils
 */
public class FlinkArgsUtils {

    private final static Logger logger = LoggerFactory.getLogger(FlinkArgsUtils.class);

    private FlinkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String LOCAL_DEPLOY_MODE = "local";
    private static final String FLINK_VERSION_BEFORE_1_10 = "<1.10";

    /**
     * build args
     *
     * @param flinkParameters FlinkParameters
     * @return argument list
     */
    public static List<String> buildArgs(FlinkParameters flinkParameters) {
        List<String> args = new ArrayList<>();

        String deployMode = "cluster";
        String tmpDeployMode = flinkParameters.getDeployMode();
        if (StringUtils.isNotEmpty(tmpDeployMode)) {
            deployMode = tmpDeployMode;
        }
        String others = flinkParameters.getOthers();
        if (!LOCAL_DEPLOY_MODE.equals(deployMode)) {
            args.add(FlinkConstants.FLINK_RUN_MODE);  // -m

            args.add(FlinkConstants.FLINK_YARN_CLUSTER);   // yarn-cluster

            int slot = flinkParameters.getSlot();
            if (slot > 0) {
                args.add(FlinkConstants.FLINK_YARN_SLOT);
                args.add(String.format("%d", slot));   // -ys
            }

            String appName = flinkParameters.getAppName();
            if (StringUtils.isNotEmpty(appName)) { // -ynm
                args.add(FlinkConstants.FLINK_APP_NAME);
                args.add(ArgsUtils.escape(appName));
            }

            // judge flink version, the parameter -yn has removed from flink 1.10
            String flinkVersion = flinkParameters.getFlinkVersion();
            if (flinkVersion == null || FLINK_VERSION_BEFORE_1_10.equals(flinkVersion)) {
                int taskManager = flinkParameters.getTaskManager();
                if (taskManager > 0) {                        // -yn
                    args.add(FlinkConstants.FLINK_TASK_MANAGE);
                    args.add(String.format("%d", taskManager));
                }
            }
            String jobManagerMemory = flinkParameters.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                args.add(FlinkConstants.FLINK_JOB_MANAGE_MEM);
                args.add(jobManagerMemory); // -yjm
            }

            String taskManagerMemory = flinkParameters.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) { // -ytm
                args.add(FlinkConstants.FLINK_TASK_MANAGE_MEM);
                args.add(taskManagerMemory);
            }

            if (StringUtils.isEmpty(others) || !others.contains(FlinkConstants.FLINK_QUEUE)) {
                String queue = flinkParameters.getQueue();
                if (StringUtils.isNotEmpty(queue)) { // -yqu
                    args.add(FlinkConstants.FLINK_QUEUE);
                    args.add(queue);
                }
            }
        }

        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            args.add(FlinkConstants.FLINK_PARALLELISM);
            args.add(String.format("%d", parallelism));   // -p
        }

        // If the job is submitted in attached mode, perform a best-effort cluster shutdown when the CLI is terminated abruptly
        // The task status will be synchronized with the cluster job status
        args.add(FlinkConstants.FLINK_SHUTDOWN_ON_ATTACHED_EXIT); // -sae

        // -s -yqu -yat -yD -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        ProgramType programType = flinkParameters.getProgramType();
        String mainClass = flinkParameters.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(FlinkConstants.FLINK_MAIN_CLASS);    //-c
            args.add(flinkParameters.getMainClass());          //main class
        }

        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = flinkParameters.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }


    /**
     * build args for flink sql
     *
     * @param flinkParameters      FlinkParameters
     * @param taskExecutionContext TaskExecutionContext
     * @return argument list
     * @throws Exception
     */
    public static List<String> buildSqlArgs(FlinkParameters flinkParameters, TaskExecutionContext taskExecutionContext) throws Exception {
        List<String> args = new ArrayList<>();
        StringBuilder parameters = new StringBuilder();

        String deployMode = "cluster";
        String tmpDeployMode = flinkParameters.getDeployMode();
        if (StringUtils.isNotEmpty(tmpDeployMode)) {
            deployMode = tmpDeployMode;
        }
        String others = flinkParameters.getOthers();
        if (!LOCAL_DEPLOY_MODE.equals(deployMode)) {
            parameters.append(FlinkConstants.FLINK_SQL_EXECUTION_TARGET);  // -m execution.target=YARN_PER_JOB;
            parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);

            int slot = flinkParameters.getSlot();
            if (slot > 0) {
                parameters.append(FlinkConstants.FLINK_SQL_TASKMANAGER_NUMBEROFTASKSLOTS);
                parameters.append(String.format("%d", slot));   // -ys taskmanager.numberOfTaskSlots
                parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
            }

            String appName = flinkParameters.getAppName();
            if (StringUtils.isNotEmpty(appName)) {
                parameters.append(FlinkConstants.FLINK_SQL_YARN_APPLICATION_NAME);
                parameters.append(ArgsUtils.escape(appName));  // -ynm yarn.application.name
                parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
            }

            String jobManagerMemory = flinkParameters.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                parameters.append(FlinkConstants.FLINK_SQL_JOBMANAGER_MEMORY_PROCESS_SIZE);
                parameters.append(jobManagerMemory); // -yjm  jobmanager.memory.process.size
                parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
            }

            String taskManagerMemory = flinkParameters.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) {
                parameters.append(FlinkConstants.FLINK_SQL_TASKMANAGER_MEMORY_PROCESS_SIZE);
                parameters.append(taskManagerMemory);  // -ytm  taskmanager.memory.process.size
                parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
            }

            if (StringUtils.isEmpty(others) || !others.contains(FlinkConstants.FLINK_QUEUE)) {
                String queue = flinkParameters.getQueue();
                if (StringUtils.isNotEmpty(queue)) {
                    parameters.append(FlinkConstants.FLINK_SQL_YARN_APPLICATION_QUEUE);
                    parameters.append(queue); // -yqu   yarn.application.queue
                    parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
                }
            }
        }

        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            parameters.append(FlinkConstants.FLINK_SQL_PARALLELISM_DEFAULT);
            parameters.append(String.format("%d", parallelism));   // -p  parallelism.default
            parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
        }

        String resultMode = flinkParameters.getResultMode();
        if (StringUtils.isNotEmpty(resultMode)) {
            parameters.append(FlinkConstants.FLINK_SQL_EXECUTION_RESULT_MODE);
            parameters.append(resultMode);   // sql-client    execution result-mode
            parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
        }

        String runtimeMode = flinkParameters.getRuntimeMode();
        if (StringUtils.isNotEmpty(runtimeMode)) {
            parameters.append(FlinkConstants.FLINK_SQL_EXECUTION_RUNTIME_MODE);
            parameters.append(runtimeMode);     // execution runtime-mode
            parameters.append(FlinkConstants.FLINK_SQL_NEWLINE);
        }

        ProgramType programType = flinkParameters.getProgramType();

        if (ProgramType.SQL == programType) {
            args.add(FlinkConstants.FLINK_SQL_FILE);
            args.add(generateSqlScript(flinkParameters, taskExecutionContext, parameters)); // -f
        }

        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }
        return args;
    }


    private static String generateSqlScript(FlinkParameters flinkParameters, TaskExecutionContext taskExecutionContext, StringBuilder parameters) throws Exception {
        String sqlScriptfileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskAppId());

        File file = new File(sqlScriptfileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = flinkParameters.getRawScript().replaceAll("\\r\\n", "\n");
            flinkParameters.setRawScript(script);
            logger.info("raw script : {}", flinkParameters.getRawScript());
            logger.info("task execute path : {}", taskExecutionContext.getExecutePath());

            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            if (OSUtils.isWindows()) {
                Files.createFile(path);
            } else {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                Files.createFile(path, attr);
            }
            // Flink sql common parameters are written to the script file
            Files.write(path, parameters.toString().getBytes(), StandardOpenOption.APPEND);

            Files.write(path, flinkParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
        }
        return sqlScriptfileName;
    }
}
