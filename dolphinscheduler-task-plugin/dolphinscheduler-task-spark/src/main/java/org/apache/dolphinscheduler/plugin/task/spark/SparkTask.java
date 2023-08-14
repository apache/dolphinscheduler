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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.util.stream.Collectors;

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

        log.info("Initialize spark task params {}", JSONUtils.toPrettyJsonString(sparkParameters));
    }

    @Override
    protected String getScript() {
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

        // replace placeholder
        return args.stream().collect(Collectors.joining(" "));
    }

    @Override
    protected Map<String, String> getProperties() {
        return ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap());
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
                && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_YARN_QUEUE))) {
            String yarnQueue = sparkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                args.add(SparkConstants.SPARK_YARN_QUEUE);
                args.add(yarnQueue);
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
            args.add(taskExecutionContext.getResources().get(mainJar.getResourceName()));
        }

        String mainArgs = sparkParameters.getMainArgs();
        if (programType != ProgramType.SQL && StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        // bin/spark-sql -f fileName
        if (ProgramType.SQL == programType) {
            String sqlContent = "";
            String resourceFileName = "";
            args.add(SparkConstants.SQL_FROM_FILE);
            if (SparkConstants.TYPE_FILE.equals(sparkParameters.getSqlExecutionType())) {
                final List<ResourceInfo> resourceInfos = sparkParameters.getResourceList();
                if (resourceInfos.size() > 1) {
                    log.warn("more than 1 files detected, use the first one by default");
                }

                try {
                    resourceFileName = resourceInfos.get(0).getResourceName();
                    sqlContent = FileUtils.readFileToString(
                            new File(String.format("%s/%s", taskExecutionContext.getExecutePath(), resourceFileName)),
                            StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("read sql content from file {} error ", resourceFileName, e);
                    throw new TaskException("read sql content error", e);
                }
            } else {
                sqlContent = sparkParameters.getRawScript();
            }
            args.add(generateScriptFile(sqlContent));
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

    private String generateScriptFile(String sqlContent) {
        String scriptFileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = replaceParam(sqlContent);

            log.info("raw script : {}", script);
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
                Files.write(path, script.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("generate spark sql script error", e);
            }

        }
        return scriptFileName;
    }

    private String replaceParam(String script) {
        script = script.replaceAll("\\r\\n", System.lineSeparator());
        // replace placeholder
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        script = ParameterUtils.convertParameterPlaceholders(script, ParameterUtils.convert(paramsMap));
        return script;
    }

    @Override
    public AbstractParameters getParameters() {
        return sparkParameters;
    }
}
