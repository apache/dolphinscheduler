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
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(SparkParameters param,TaskExecutionContext taskRequest) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(SparkConstants.MASTER);

        String deployMode = StringUtils.isNotEmpty(param.getDeployMode()) ? param.getDeployMode() : SPARK_CLUSTER;
        if (!SPARK_LOCAL.equals(deployMode)) {
            args.add(SPARK_ON_YARN);
            args.add(SparkConstants.DEPLOY_MODE);
        }
        args.add(deployMode);

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        String rawScript = param.getRawScript();
        if (programType != null && programType != ProgramType.PYTHON && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainClass)) {
            args.add(SparkConstants.MAIN_CLASS);
            args.add(mainClass);
        }
        if (programType != null && programType == ProgramType.SQL && StringUtils.isNotEmpty(rawScript)){
            args.add(SparkConstants.SQL_FILE);

            StringBuffer sb = new StringBuffer();
            sb.append("set");
            sb.append(programType);
            sb.append(";\n");

            // generate scripts
            String fileName = String.format("%s/%s_node.%s",
                    taskRequest.getExecutePath(),
                    taskRequest.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sql");

            File file = new File(fileName);
            Path path = file.toPath();

            if (!Files.exists(path)) {
                String script = param.getRawScript().replaceAll("\\r\\n", "\n");
                param.setRawScript(script);
                logger.info("raw script : {}", param.getRawScript());
                logger.info("task execute path : {}", taskRequest.getExecutePath());

                Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
                FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
                if (OSUtils.isWindows()) {
                    Files.createFile(path);
                } else {
                    if (!file.getParentFile().exists()){
                        file.getParentFile().mkdirs();
                    }
                    Files.createFile(path, attr);
                }
                Files.write(path, param.getRawScript().getBytes(), StandardOpenOption.APPEND);
            }
            args.add(fileName);
        }

        int driverCores = param.getDriverCores();
        if (driverCores > 0) {
            args.add(SparkConstants.DRIVER_CORES);
            args.add(String.format("%d", driverCores));
        }

        String driverMemory = param.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(SparkConstants.DRIVER_MEMORY);
            args.add(driverMemory);
        }

        int numExecutors = param.getNumExecutors();
        if (numExecutors > 0) {
            args.add(SparkConstants.NUM_EXECUTORS);
            args.add(String.format("%d", numExecutors));
        }

        int executorCores = param.getExecutorCores();
        if (executorCores > 0) {
            args.add(SparkConstants.EXECUTOR_CORES);
            args.add(String.format("%d", executorCores));
        }

        String executorMemory = param.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(SparkConstants.EXECUTOR_MEMORY);
            args.add(executorMemory);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(SparkConstants.SPARK_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        String others = param.getOthers();
        if (!SPARK_LOCAL.equals(deployMode) && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_QUEUE))) {
            String queue = param.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(SparkConstants.SPARK_QUEUE);
                args.add(queue);
            }
        }

        // --conf --files --jars --packages
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        ResourceInfo mainJar = param.getMainJar();
//        if (mainJar != null) {
        if (programType != null && programType != ProgramType.SQL && mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = param.getMainArgs();
//        if (StringUtils.isNotEmpty(mainArgs)) {
        if (programType != null && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}
