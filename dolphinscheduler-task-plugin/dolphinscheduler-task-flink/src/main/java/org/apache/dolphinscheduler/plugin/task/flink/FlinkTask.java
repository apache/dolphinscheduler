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

import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.OSUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlinkTask extends AbstractYarnTask {

    /**
     * flink parameters
     */
    private FlinkParameters flinkParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    public FlinkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        logger.info("flink task params {}", taskExecutionContext.getTaskParams());

        flinkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkParameters.class);

        if (flinkParameters == null || !flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }
        flinkParameters.setQueue(taskExecutionContext.getQueue());

        if (ProgramType.SQL != flinkParameters.getProgramType()) {
            setMainJarName();
        }
    }

    /**
     * create command
     *
     * @return command
     */
    @Override
    protected String buildCommand() {
        List<String> args = new ArrayList<>();

        if (ProgramType.SQL != flinkParameters.getProgramType()) {
            // execute flink run [OPTIONS] <jar-file> <arguments>
            args.add(FlinkConstants.FLINK_COMMAND);
            args.add(FlinkConstants.FLINK_RUN);
            args.addAll(populateFlinkOptions());
        } else {
            // execute sql-client.sh -f <script file>
            args.add(FlinkConstants.FLINK_SQL_COMMAND);
            args.addAll(populateFlinkSqlOptions());
        }
        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());
        logger.info("flink task command : {}", command);
        return command;
    }

    /**
     * build flink options
     *
     * @return argument list
     */
    private List<String> populateFlinkOptions() {
        List<String> args = new ArrayList<>();

        String deployMode = StringUtils.isNotEmpty(flinkParameters.getDeployMode()) ? flinkParameters.getDeployMode() : FlinkConstants.DEPLOY_MODE_CLUSTER;

        if (!FlinkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)) {
            populateFlinkOnYarnOptions(args);
        }

        // -p
        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            args.add(FlinkConstants.FLINK_PARALLELISM);
            args.add(String.format("%d", parallelism));
        }

        /**
         * -sae
         *
         * If the job is submitted in attached mode, perform a best-effort cluster shutdown when the CLI is terminated abruptly.
         * The task status will be synchronized with the cluster job status.
         */
        args.add(FlinkConstants.FLINK_SHUTDOWN_ON_ATTACHED_EXIT);

        // -s -yqu -yat -yD -D
        String others = flinkParameters.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        // -c
        ProgramType programType = flinkParameters.getProgramType();
        String mainClass = flinkParameters.getMainClass();
        if (programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(FlinkConstants.FLINK_MAIN_CLASS);
            args.add(flinkParameters.getMainClass());
        }

        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = flinkParameters.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            // combining local and global parameters
            Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
            if (MapUtils.isEmpty(paramsMap)) {
                paramsMap = new HashMap<>();
            }
            if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
                paramsMap.putAll(taskExecutionContext.getParamsMap());
            }
            args.add(ParameterUtils.convertParameterPlaceholders(mainArgs, ParamUtils.convert(paramsMap)));
        }

        return args;
    }

    private void populateFlinkOnYarnOptions(List<String> args) {
        // -m yarn-cluster
        args.add(FlinkConstants.FLINK_RUN_MODE);
        args.add(FlinkConstants.FLINK_YARN_CLUSTER);

        // -ys
        int slot = flinkParameters.getSlot();
        if (slot > 0) {
            args.add(FlinkConstants.FLINK_YARN_SLOT);
            args.add(String.format("%d", slot));
        }

        // -ynm
        String appName = flinkParameters.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(FlinkConstants.FLINK_APP_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        /**
         * -yn
         *
         * Note: judge flink version, the parameter -yn has removed from flink 1.10
         */
        String flinkVersion = flinkParameters.getFlinkVersion();
        if (flinkVersion == null || FlinkConstants.FLINK_VERSION_BEFORE_1_10.equals(flinkVersion)) {
            int taskManager = flinkParameters.getTaskManager();
            if (taskManager > 0) {
                args.add(FlinkConstants.FLINK_TASK_MANAGE);
                args.add(String.format("%d", taskManager));
            }
        }

        // -yjm
        String jobManagerMemory = flinkParameters.getJobManagerMemory();
        if (StringUtils.isNotEmpty(jobManagerMemory)) {
            args.add(FlinkConstants.FLINK_JOB_MANAGE_MEM);
            args.add(jobManagerMemory);
        }

        // -ytm
        String taskManagerMemory = flinkParameters.getTaskManagerMemory();
        if (StringUtils.isNotEmpty(taskManagerMemory)) {
            args.add(FlinkConstants.FLINK_TASK_MANAGE_MEM);
            args.add(taskManagerMemory);
        }

        // -yqu
        String others = flinkParameters.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(FlinkConstants.FLINK_QUEUE)) {
            String queue = flinkParameters.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(FlinkConstants.FLINK_QUEUE);
                args.add(queue);
            }
        }
    }

    /**
     * build flink sql options
     *
     * @return argument list
     */
    private List<String> populateFlinkSqlOptions() {
        List<String> args = new ArrayList<>();
        List<String> defalutOptions = new ArrayList<>();

        String deployMode = StringUtils.isNotEmpty(flinkParameters.getDeployMode()) ? flinkParameters.getDeployMode() : FlinkConstants.DEPLOY_MODE_CLUSTER;

        /**
         * Currently flink sql on yarn only supports yarn-per-job mode
         */
        if (!FlinkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)) {
            populateFlinkSqlOnYarnOptions(defalutOptions);
        } else {
            // execution.target
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.EXECUTION_TARGET_LOACL));
        }

        // parallelism.default
        int parallelism = flinkParameters.getParallelism();
        if (parallelism > 0) {
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_PARALLELISM_DEFAULT, parallelism));
        }

        // -i
        args.add(FlinkConstants.FLINK_SQL_INIT_FILE);
        args.add(generateInitScriptFile(StringUtils.join(defalutOptions, FlinkConstants.FLINK_SQL_NEWLINE).concat(FlinkConstants.FLINK_SQL_NEWLINE)));

        // -f
        args.add(FlinkConstants.FLINK_SQL_SCRIPT_FILE);
        args.add(generateScriptFile());

        String others = flinkParameters.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }
        return args;
    }

    private void populateFlinkSqlOnYarnOptions(List<String> defalutOptions) {
        // execution.target
        defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_EXECUTION_TARGET, FlinkConstants.EXECUTION_TARGET_YARN_PER_JOB));

        // taskmanager.numberOfTaskSlots
        int slot = flinkParameters.getSlot();
        if (slot > 0) {
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_NUMBEROFTASKSLOTS, slot));
        }

        // yarn.application.name
        String appName = flinkParameters.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_NAME, ArgsUtils.escape(appName)));
        }

        // jobmanager.memory.process.size
        String jobManagerMemory = flinkParameters.getJobManagerMemory();
        if (StringUtils.isNotEmpty(jobManagerMemory)) {
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_JOBMANAGER_MEMORY_PROCESS_SIZE, jobManagerMemory));
        }

        // taskmanager.memory.process.size
        String taskManagerMemory = flinkParameters.getTaskManagerMemory();
        if (StringUtils.isNotEmpty(taskManagerMemory)) {
            defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_TASKMANAGER_MEMORY_PROCESS_SIZE, taskManagerMemory));
        }

        // yarn.application.queue
        String others = flinkParameters.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(FlinkConstants.FLINK_QUEUE)) {
            String queue = flinkParameters.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                defalutOptions.add(String.format(FlinkConstants.FLINK_FORMAT_YARN_APPLICATION_QUEUE, queue));
            }
        }
    }

    private String generateInitScriptFile(String parameters) {
        String initScriptFileName = String.format("%s/%s_init.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskAppId());

        File file = new File(initScriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            try {
                if (OSUtils.isWindows()) {
                    Files.createFile(path);
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    Files.createFile(path, attr);
                }

                // Flink sql common parameters are written to the script file
                logger.info("common parameters : {}", parameters);
                Files.write(path, parameters.getBytes(), StandardOpenOption.APPEND);

                // Flink init script is written to the script file
                if (StringUtils.isNotEmpty(flinkParameters.getInitScript())) {
                    String script = flinkParameters.getInitScript().replaceAll("\\r\\n", "\n");
                    flinkParameters.setInitScript(script);
                    logger.info("init script : {}", flinkParameters.getInitScript());
                    Files.write(path, flinkParameters.getInitScript().getBytes(), StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                throw new RuntimeException("generate flink sql script error", e);
            }
        }
        return initScriptFileName;
    }

    private String generateScriptFile() {
        String scriptFileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = flinkParameters.getRawScript().replaceAll("\\r\\n", "\n");
            flinkParameters.setRawScript(script);

            logger.info("raw script : {}", flinkParameters.getRawScript());
            logger.info("task execute path : {}", taskExecutionContext.getExecutePath());

            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            try {
                if (OSUtils.isWindows()) {
                    Files.createFile(path);
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    Files.createFile(path, attr);
                }
                // Flink sql raw script is written to the script file
                Files.write(path, flinkParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("generate flink sql script error", e);
            }
        }
        return scriptFileName;
    }

    @Override
    protected void setMainJarName() {
        ResourceInfo mainJar = flinkParameters.getMainJar();
        String resourceName = getResourceNameOfMainJar(mainJar);
        mainJar.setRes(resourceName);
        flinkParameters.setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }
}
