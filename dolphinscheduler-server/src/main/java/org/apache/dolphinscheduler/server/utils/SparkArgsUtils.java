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
package org.apache.dolphinscheduler.server.utils;


import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 *  spark args utils
 */
public class SparkArgsUtils {

    /**
     * build args
     *
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(SparkParameters param) {
        List<String> args = new ArrayList<>();
        String deployMode = "cluster";

        args.add(Constants.MASTER);
        if(StringUtils.isNotEmpty(param.getDeployMode())){
            deployMode = param.getDeployMode();

        }
        if(!"local".equals(deployMode)){
            args.add("yarn");
            args.add(Constants.DEPLOY_MODE);
        }

        args.add(param.getDeployMode());

        ProgramType type = param.getProgramType();
        String mainClass = param.getMainClass();
        if(type != null && type != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)){
            args.add(Constants.MAIN_CLASS);
            args.add(mainClass);
        }

        int driverCores = param.getDriverCores();
        if (driverCores != 0) {
            args.add(Constants.DRIVER_CORES);
            args.add(String.format("%d", driverCores));
        }

        String driverMemory = param.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(Constants.DRIVER_MEMORY);
            args.add(driverMemory);
        }

        int numExecutors = param.getNumExecutors();
        if (numExecutors != 0) {
            args.add(Constants.NUM_EXECUTORS);
            args.add(String.format("%d", numExecutors));
        }

        int executorCores = param.getExecutorCores();
        if (executorCores != 0) {
            args.add(Constants.EXECUTOR_CORES);
            args.add(String.format("%d", executorCores));
        }

        String executorMemory = param.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(Constants.EXECUTOR_MEMORY);
            args.add(executorMemory);
        }

        // --files --conf --libjar ...
        String others = param.getOthers();
        String queue = param.getQueue();
        if (StringUtils.isNotEmpty(others)) {

            if(!others.contains(Constants.SPARK_QUEUE) && StringUtils.isNotEmpty(queue)){
                args.add(Constants.SPARK_QUEUE);
                args.add(queue);
            }

            args.add(others);

        }else if (StringUtils.isNotEmpty(queue)) {
            args.add(Constants.SPARK_QUEUE);
            args.add(queue);

        }

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}