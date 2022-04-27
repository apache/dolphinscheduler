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

package org.apache.dolphinscheduler.plugin.task.spark;

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

public class SparkTask extends AbstractYarnTask {

    /**
     * spark parameters
     */
    private SparkParameters sparkParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    public SparkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        logger.info("spark task params {}", taskExecutionContext.getTaskParams());

        sparkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SparkParameters.class);

        if (null == sparkParameters) {
            logger.error("Spark params is null");
            return;
        }

        if (!sparkParameters.checkParameters()) {
            throw new RuntimeException("spark task params is not valid");
        }
        sparkParameters.setQueue(taskExecutionContext.getQueue());

        if (sparkParameters.getProgramType() != ProgramType.SQL) {
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
        /**
         * (1) spark-submit [options] <app jar | python file> [app arguments]
         * (2) spark-sql [options] -f <filename>
         */
        List<String> args = new ArrayList<>();

        // spark version
        String sparkCommand = SparkVersion.SPARK2.getCommand();

        // If the programType is non-SQL, execute bin/spark-submit
        if (SparkVersion.SPARK1.name().equals(sparkParameters.getSparkVersion())) {
            sparkCommand = SparkVersion.SPARK1.getCommand();
        }

        // If the programType is SQL, execute bin/spark-sql
        if (sparkParameters.getProgramType() == ProgramType.SQL) {
            sparkCommand = SparkVersion.SPARKSQL.getCommand();
        }

        args.add(sparkCommand);

        // populate spark options
        args.addAll(populateSparkOptions());

        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }

        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));

        logger.info("spark task command: {}", command);

        return command;
    }

    /**
     * build spark options
     *
     * @return argument list
     */
    private List<String> populateSparkOptions() {
        List<String> args = new ArrayList<>();
        args.add(SparkConstants.MASTER);

        String deployMode = StringUtils.isNotEmpty(sparkParameters.getDeployMode()) ? sparkParameters.getDeployMode() : SparkConstants.DEPLOY_MODE_LOCAL;
        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)) {
            args.add(SparkConstants.SPARK_ON_YARN);
            args.add(SparkConstants.DEPLOY_MODE);
        }
        args.add(deployMode);

        ProgramType programType = sparkParameters.getProgramType();
        String mainClass = sparkParameters.getMainClass();
        if (programType != ProgramType.PYTHON && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainClass)) {
            args.add(SparkConstants.MAIN_CLASS);
            args.add(mainClass);
        }

        populateSparkResourceDefinitions(args);

        String appName = sparkParameters.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(SparkConstants.SPARK_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        String others = sparkParameters.getOthers();
        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode) && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_QUEUE))) {
            String queue = sparkParameters.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(SparkConstants.SPARK_QUEUE);
                args.add(queue);
            }
        }

        // --conf --files --jars --packages
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        ResourceInfo mainJar = sparkParameters.getMainJar();
        if (programType != ProgramType.SQL) {
            args.add(mainJar.getRes());
        }

        String mainArgs = sparkParameters.getMainArgs();
        if (programType != ProgramType.SQL && StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        // bin/spark-sql -f fileName
        if (ProgramType.SQL == programType) {
            args.add(SparkConstants.SQL_FROM_FILE);
            args.add(generateScriptFile());
        }
        return args;
    }

    private void populateSparkResourceDefinitions(List<String> args) {
        int driverCores = sparkParameters.getDriverCores();
        if (driverCores > 0) {
            args.add(SparkConstants.DRIVER_CORES);
            args.add(String.format("%d", driverCores));
        }

        String driverMemory = sparkParameters.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(SparkConstants.DRIVER_MEMORY);
            args.add(driverMemory);
        }

        int numExecutors = sparkParameters.getNumExecutors();
        if (numExecutors > 0) {
            args.add(SparkConstants.NUM_EXECUTORS);
            args.add(String.format("%d", numExecutors));
        }

        int executorCores = sparkParameters.getExecutorCores();
        if (executorCores > 0) {
            args.add(SparkConstants.EXECUTOR_CORES);
            args.add(String.format("%d", executorCores));
        }

        String executorMemory = sparkParameters.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(SparkConstants.EXECUTOR_MEMORY);
            args.add(executorMemory);
        }
    }

    private String generateScriptFile() {
        String scriptFileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = sparkParameters.getRawScript().replaceAll("\\r\\n", "\n");
            sparkParameters.setRawScript(script);

            logger.info("raw script : {}", sparkParameters.getRawScript());
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
                Files.write(path, sparkParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("generate spark sql script error", e);
            }

        }
        return scriptFileName;
    }

    @Override
    protected void setMainJarName() {
        // main jar
        ResourceInfo mainJar = sparkParameters.getMainJar();
        String resourceName = getResourceNameOfMainJar(mainJar);
        mainJar.setRes(resourceName);
        sparkParameters.setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return sparkParameters;
    }
}
