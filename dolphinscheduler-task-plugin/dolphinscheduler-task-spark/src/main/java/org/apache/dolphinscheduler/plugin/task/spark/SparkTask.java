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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNIQUE_LABEL_NAME;
import static org.apache.dolphinscheduler.plugin.task.spark.SparkConstants.DRIVER_LABEL_CONF;
import static org.apache.dolphinscheduler.plugin.task.spark.SparkConstants.SPARK_KUBERNETES_NAMESPACE;
import static org.apache.dolphinscheduler.plugin.task.spark.SparkConstants.SPARK_ON_K8S_MASTER_PREFIX;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric8.kubernetes.client.Config;

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

        sparkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SparkParameters.class);

        if (null == sparkParameters) {
            log.error("Spark params is null");
            return;
        }

        if (!sparkParameters.checkParameters()) {
            throw new RuntimeException("spark task params is not valid");
        }
        sparkParameters.setQueue(taskExecutionContext.getQueue());

        if (sparkParameters.getProgramType() != ProgramType.SQL) {
            setMainJarName();
        }
        log.info("Initialize spark task params {}", JSONUtils.toPrettyJsonString(sparkParameters));
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

        String sparkCommand;
        // If the programType is SQL, execute bin/spark-sql
        if (sparkParameters.getProgramType() == ProgramType.SQL) {
            sparkCommand = SparkConstants.SPARK_SQL_COMMAND;
        } else {
            // If the programType is non-SQL, execute bin/spark-submit
            sparkCommand = SparkConstants.SPARK_SUBMIT_COMMAND;
        }

        args.add(sparkCommand);

        // populate spark options
        args.addAll(populateSparkOptions());

        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

        String command =
                ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));

        log.info("spark task command: {}", command);

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

        String deployMode = StringUtils.isNotEmpty(sparkParameters.getDeployMode()) ? sparkParameters.getDeployMode()
                : SparkConstants.DEPLOY_MODE_LOCAL;

        boolean onNativeKubernetes = StringUtils.isNotEmpty(sparkParameters.getNamespace());

        String masterUrl = onNativeKubernetes ? SPARK_ON_K8S_MASTER_PREFIX +
                Config.fromKubeconfig(taskExecutionContext.getK8sTaskExecutionContext().getConfigYaml()).getMasterUrl()
                : SparkConstants.SPARK_ON_YARN;

        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)) {
            args.add(masterUrl);
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
        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)
                && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_QUEUE))) {
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

        // add driver label for spark on native kubernetes
        if (onNativeKubernetes) {
            args.add(String.format(DRIVER_LABEL_CONF, UNIQUE_LABEL_NAME, taskExecutionContext.getTaskAppId()));
            args.add(String.format(SPARK_KUBERNETES_NAMESPACE,
                    JSONUtils.toMap(sparkParameters.getNamespace()).get(NAMESPACE_NAME)));
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
            args.add(String.format(SparkConstants.DRIVER_CORES, driverCores));
        }

        String driverMemory = sparkParameters.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(String.format(SparkConstants.DRIVER_MEMORY, driverMemory));
        }

        int numExecutors = sparkParameters.getNumExecutors();
        if (numExecutors > 0) {
            args.add(String.format(SparkConstants.NUM_EXECUTORS, numExecutors));
        }

        int executorCores = sparkParameters.getExecutorCores();
        if (executorCores > 0) {
            args.add(String.format(SparkConstants.EXECUTOR_CORES, executorCores));
        }

        String executorMemory = sparkParameters.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(String.format(SparkConstants.EXECUTOR_MEMORY, executorMemory));
        }
    }

    private String generateScriptFile() {
        String scriptFileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = replaceParam(sparkParameters.getRawScript());
            sparkParameters.setRawScript(script);

            log.info("raw script : {}", sparkParameters.getRawScript());
            log.info("task execute path : {}", taskExecutionContext.getExecutePath());

            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            try {
                if (SystemUtils.IS_OS_WINDOWS) {
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

    private String replaceParam(String script) {
        script = script.replaceAll("\\r\\n", System.lineSeparator());
        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        script = ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
        return script;
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
