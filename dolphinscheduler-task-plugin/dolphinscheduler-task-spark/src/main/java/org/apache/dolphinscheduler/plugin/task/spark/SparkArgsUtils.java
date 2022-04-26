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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.OSUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Set;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;

/**
 * spark args utils
 * add main logical for support Spark command
 */
public class SparkArgsUtils {

    private final static Logger logger = LoggerFactory.getLogger(SparkArgsUtils.class);

    private static final String SPARK_CLUSTER = "cluster";

    private static final String SPARK_LOCAL = "local";

    private static final String SPARK_ON_YARN = "yarn";

    private SparkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     *
     * @param sparkParameters SparkParameters
     * @param taskRequest     TaskExecutionContext
     * @return argument list
     * @throws Exception exception
     */
    public static List<String> buildArgs(SparkParameters sparkParameters, TaskExecutionContext taskRequest) throws Exception {
        List<String> args = new ArrayList<>();
        args.add(SparkConstants.MASTER);

        String deployMode = StringUtils.isNotEmpty(sparkParameters.getDeployMode()) ? sparkParameters.getDeployMode() : SPARK_CLUSTER;
        if (!SPARK_LOCAL.equals(deployMode)) {
            args.add(SPARK_ON_YARN);
            args.add(SparkConstants.DEPLOY_MODE);
        }
        args.add(deployMode);

        ProgramType programType = sparkParameters.getProgramType();
        String mainClass = sparkParameters.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainClass)) {
            args.add(SparkConstants.MAIN_CLASS);
            args.add(mainClass);
        }

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

        String appName = sparkParameters.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(SparkConstants.SPARK_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        String others = sparkParameters.getOthers();
        if (!SPARK_LOCAL.equals(deployMode) && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_QUEUE))) {
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
        if (programType != null && programType != ProgramType.SQL && mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = sparkParameters.getMainArgs();
        if (programType != null && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        // bin/spark-sql -f fileName
        if (ProgramType.SQL == programType) {
            args.add(SparkConstants.SQL_FROM_FILE);
            args.add(generateScriptFile(sparkParameters, taskRequest));
        }
        return args;
    }

    private static String generateScriptFile(SparkParameters sparkParameters, TaskExecutionContext taskRequest) throws IOException {
        String scriptFileName = String.format("%s/%s_node.sql", taskRequest.getExecutePath(), taskRequest.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = sparkParameters.getRawScript().replaceAll("\\r\\n", "\n");
            sparkParameters.setRawScript(script);
            logger.info("raw script : {}", sparkParameters.getRawScript());
            logger.info("task execute path : {}", taskRequest.getExecutePath());

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
            Files.write(path, sparkParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
        }
        return scriptFileName;
    }
}
