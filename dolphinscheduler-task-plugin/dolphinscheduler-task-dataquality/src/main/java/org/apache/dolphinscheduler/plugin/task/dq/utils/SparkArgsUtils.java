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

package org.apache.dolphinscheduler.plugin.task.dq.utils;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.spark.ProgramType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.spark.SparkConstants;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.spark.SparkParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * spark args utils
 */
public class SparkArgsUtils {

    private static final String SPARK_CLUSTER = "cluster";

    private static final String SPARK_LOCAL = "local";

    private static final String SPARK_ON_YARN = "yarn";

    private static final String DEFAULT_QUALITY_CLASS =
            "org.apache.dolphinscheduler.data.quality.DataQualityApplication";

    private SparkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     *
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(SparkParameters param) {
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
        if (programType != null && programType != ProgramType.PYTHON) {
            args.add(SparkConstants.MAIN_CLASS);
            args.add(StringUtils.isNotEmpty(mainClass) ? mainClass : DEFAULT_QUALITY_CLASS);
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
        if (!SPARK_LOCAL.equals(deployMode)
                && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_QUEUE))) {
            String queue = param.getYarnQueue();
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
        if (mainJar != null) {
            args.add(mainJar.getResourceName());
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}
