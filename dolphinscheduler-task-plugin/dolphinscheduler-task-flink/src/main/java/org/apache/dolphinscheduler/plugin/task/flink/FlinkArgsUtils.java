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
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkArgsUtils {

    private FlinkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String LOCAL_DEPLOY_MODE = "local";
    private static final String FLINK_VERSION_BEFORE_1_10 = "<1.10";
    private static final String FLINK_VERSION_AFTER_OR_EQUALS_1_12 = ">=1.12";
    private static final String FLINK_VERSION_AFTER_OR_EQUALS_1_13 = ">=1.13";
    /**
     *  default flink deploy mode
     */
    public static final FlinkDeployMode DEFAULT_DEPLOY_MODE = FlinkDeployMode.CLUSTER;

    /**
     * build flink run command line
     *
     * @param param flink parameters
     * @return argument list
     */
    public static List<String> buildRunCommandLine(TaskExecutionContext taskExecutionContext, FlinkParameters param) {
        switch (param.getProgramType()) {
            case SQL:
                return buildRunCommandLineForSql(taskExecutionContext, param);
            default:
                return buildRunCommandLineForOthers(taskExecutionContext, param);
        }
    }

    /**
     * build flink cancel command line
     * @param taskExecutionContext
     * @return
     */
    public static List<String> buildCancelCommandLine(TaskExecutionContext taskExecutionContext) {
        List<String> args = new ArrayList<>();
        args.add(buildFlinkCommand(taskExecutionContext));
        args.add(FlinkConstants.FLINK_CANCEL);
        args.add(taskExecutionContext.getAppIds());
        return args;
    }

    /**
     * build flink savepoint command line, the savepoint folder should be set in flink conf
     * @return
     */
    public static List<String> buildSavePointCommandLine(TaskExecutionContext taskExecutionContext) {
        List<String> args = new ArrayList<>();
        args.add(buildFlinkCommand(taskExecutionContext));
        args.add(FlinkConstants.FLINK_SAVEPOINT);
        args.add(taskExecutionContext.getAppIds());
        return args;
    }

    /**
     * Build the flink command, with the ${FLINK_HOME} placeholder resolved.
     *
     * <p>The task script is executed by a shell, which expands ${FLINK_HOME} for us, but the cancel
     * and savepoint commands are executed by {@link ProcessBuilder} directly, and it doesn't expand
     * shell variables. So the placeholder has to be resolved before the command is executed.
     *
     * @param taskExecutionContext task execution context
     * @return the flink command, with ${FLINK_HOME} resolved when it can be resolved
     */
    public static String buildFlinkCommand(TaskExecutionContext taskExecutionContext) {
        String flinkHome = null;
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        if (paramsMap != null) {
            flinkHome = ParameterUtils.convert(paramsMap).get(FlinkConstants.FLINK_HOME);
        }
        if (StringUtils.isEmpty(flinkHome)) {
            flinkHome = System.getenv(FlinkConstants.FLINK_HOME);
        }
        if (StringUtils.isEmpty(flinkHome)) {
            return FlinkConstants.FLINK_COMMAND;
        }
        return FlinkConstants.FLINK_COMMAND.replace("${" + FlinkConstants.FLINK_HOME + "}", flinkHome);
    }

    /**
     * Execute a flink command and wait for it to finish.
     *
     * @param args the command arguments
     * @return true if the command finished successfully
     */
    public static boolean executeCommand(List<String> args) {
        try {
            Process process = new ProcessBuilder(args).start();
            if (!process.waitFor(FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Execute flink command timeout after {}s, args: {}",
                        FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS, args);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Execute flink command failed, exitCode: {}, args: {}", exitCode, args);
                return false;
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Execute flink command interrupted, args: {}", args, e);
            return false;
        } catch (Exception e) {
            log.error("Execute flink command error, args: {}", args, e);
            return false;
        }
    }

    /**
     * build flink run command line for SQL
     *
     * @return argument list
     */
    private static List<String> buildRunCommandLineForSql(TaskExecutionContext taskExecutionContext,
                                                          FlinkParameters flinkParameters) {
        List<String> args = new ArrayList<>();

        args.add(FlinkConstants.FLINK_SQL_COMMAND);

        // -i
        String initScriptFilePath = FileUtils.getInitScriptFilePath(taskExecutionContext);
        args.add(FlinkConstants.FLINK_SQL_INIT_FILE);
        args.add(initScriptFilePath);

        // -f
        String scriptFilePath = FileUtils.getScriptFilePath(taskExecutionContext);
        args.add(FlinkConstants.FLINK_SQL_SCRIPT_FILE);
        args.add(scriptFilePath);

        String others = flinkParameters.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }
        return args;
    }

    public static List<String> buildInitOptionsForSql(FlinkParameters flinkParameters) {
        List<String> initOptions = new ArrayList<>();

        FlinkDeployMode deployMode =
                Optional.ofNullable(flinkParameters.getDeployMode()).orElse(FlinkDeployMode.CLUSTER);

        /**
         * Currently flink sql on yarn only supports yarn-per-job mode
         */
        if (FlinkDeployMode.LOCAL == deployMode) {
            // execution.target
            initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.FLINK_LOCAL));
        } else if (FlinkDeployMode.STANDALONE == deployMode) {
            // standalone exec
        } else {
            // execution.target
            initOptions.add(
                    String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.FLINK_YARN_PER_JOB));

            // taskmanager.numberOfTaskSlots
            int slot = flinkParameters.getSlot();
            if (slot > 0) {
                initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_NUMBEROFTASKSLOTS, slot));
            }

            // yarn.application.name
            String appName = flinkParameters.getAppName();
            if (StringUtils.isNotEmpty(appName)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_NAME, ArgsUtils.escape(appName)));
            }

            // jobmanager.memory.process.size
            String jobManagerMemory = flinkParameters.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_JOBMANAGER_MEMORY_PROCESS_SIZE, jobManagerMemory));
            }

            // taskmanager.memory.process.size
            String taskManagerMemory = flinkParameters.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) {
                initOptions.add(
                        String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_MEMORY_PROCESS_SIZE, taskManagerMemory));
            }

            // yarn.application.queue
            String yarnQueue = flinkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_QUEUE, yarnQueue));
            }
        }

        // parallelism.default
        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            initOptions.add(String.format(FlinkConstants.FLINK_FORMAT_PARALLELISM_DEFAULT, parallelism));
        }

        return initOptions;
    }

    private static List<String> buildRunCommandLineForOthers(TaskExecutionContext taskExecutionContext,
                                                             FlinkParameters flinkParameters) {
        List<String> args = new ArrayList<>();

        args.add(FlinkConstants.FLINK_COMMAND);
        FlinkDeployMode deployMode = Optional.ofNullable(flinkParameters.getDeployMode()).orElse(DEFAULT_DEPLOY_MODE);
        String flinkVersion = flinkParameters.getFlinkVersion();
        // build run command
        switch (deployMode) {
            case CLUSTER:
                if (FLINK_VERSION_AFTER_OR_EQUALS_1_12.equals(flinkVersion)
                        || FLINK_VERSION_AFTER_OR_EQUALS_1_13.equals(flinkVersion)) {
                    args.add(FlinkConstants.FLINK_RUN); // run
                    args.add(FlinkConstants.FLINK_EXECUTION_TARGET); // -t
                    args.add(FlinkConstants.FLINK_YARN_PER_JOB); // yarn-per-job
                } else {
                    args.add(FlinkConstants.FLINK_RUN); // run
                    args.add(FlinkConstants.FLINK_RUN_MODE); // -m
                    args.add(FlinkConstants.FLINK_YARN_CLUSTER); // yarn-cluster
                }
                break;
            case APPLICATION:
                args.add(FlinkConstants.FLINK_RUN_APPLICATION); // run-application
                args.add(FlinkConstants.FLINK_EXECUTION_TARGET); // -t
                args.add(FlinkConstants.FLINK_YARN_APPLICATION); // yarn-application
                break;
            case LOCAL:
                args.add(FlinkConstants.FLINK_RUN); // run
                break;
            case STANDALONE:
                args.add(FlinkConstants.FLINK_RUN); // run
                break;
        }

        String others = flinkParameters.getOthers();

        // build args
        switch (deployMode) {
            case CLUSTER:
            case APPLICATION:
                int slot = flinkParameters.getSlot();
                if (slot > 0) {
                    args.add(FlinkConstants.FLINK_YARN_SLOT);
                    args.add(String.format("%d", slot)); // -ys
                }

                String appName = flinkParameters.getAppName();
                if (StringUtils.isNotEmpty(appName)) { // -ynm
                    args.add(FlinkConstants.FLINK_APP_NAME);
                    args.add(ArgsUtils.escape(appName));
                }

                // judge flink version, the parameter -yn has removed from flink 1.10
                if (flinkVersion == null || FLINK_VERSION_BEFORE_1_10.equals(flinkVersion)) {
                    int taskManager = flinkParameters.getTaskManager();
                    if (taskManager > 0) { // -yn
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

                break;
            case LOCAL:
                break;
            case STANDALONE:
                break;
        }

        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            args.add(FlinkConstants.FLINK_PARALLELISM);
            args.add(String.format("%d", parallelism)); // -p
        }

        // -s -yqu -yat -yD -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        // determine yarn queue
        determinedYarnQueue(args, flinkParameters, deployMode, flinkVersion);
        ProgramType programType = flinkParameters.getProgramType();
        String mainClass = flinkParameters.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(FlinkConstants.FLINK_MAIN_CLASS); // -c
            args.add(flinkParameters.getMainClass()); // main class
        }

        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            // -py
            if (ProgramType.PYTHON == programType) {
                args.add(FlinkConstants.FLINK_PYTHON);
            }
            ResourceContext resourceContext = taskExecutionContext.getResourceContext();
            args.add(resourceContext.getResourceItem(mainJar.getResourceName()).getResourceAbsolutePathInLocal());
        }

        String mainArgs = flinkParameters.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
            args.add(ParameterUtils.convertParameterPlaceholders(mainArgs, ParameterUtils.convert(paramsMap)));
        }

        return args;
    }

    private static void determinedYarnQueue(List<String> args, FlinkParameters flinkParameters,
                                            FlinkDeployMode deployMode, String flinkVersion) {
        switch (deployMode) {
            case CLUSTER:
                if (FLINK_VERSION_AFTER_OR_EQUALS_1_12.equals(flinkVersion)
                        || FLINK_VERSION_AFTER_OR_EQUALS_1_13.equals(flinkVersion)) {
                    doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS);
                } else {
                    doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE);
                }
                break;
            case APPLICATION:
                doAddQueue(args, flinkParameters, FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS);
                break;
        }
    }

    private static void doAddQueue(List<String> args, FlinkParameters flinkParameters, String option) {
        String others = flinkParameters.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(option)) {
            String yarnQueue = flinkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                switch (option) {
                    case FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS:
                        args.add(String.format(FlinkConstants.FLINK_YARN_QUEUE_FOR_TARGETS + "=%s", yarnQueue));
                        break;
                    case FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE:
                        args.add(FlinkConstants.FLINK_YARN_QUEUE_FOR_MODE);
                        args.add(yarnQueue);
                        break;
                }
            }
        }
    }

}
