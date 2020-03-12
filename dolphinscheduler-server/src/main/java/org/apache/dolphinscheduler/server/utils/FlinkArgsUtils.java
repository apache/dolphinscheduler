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


import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;

import java.util.ArrayList;
import java.util.List;


/**
 *  spark args utils
 */
public class FlinkArgsUtils {
    private static final String LOCAL_DEPLOY_MODE = "local";
    /**
     * build args
     * @param param flink parameters
     * @return argument list
     */
    public static List<String> buildArgs(FlinkParameters param) {
        List<String> args = new ArrayList<>();

        String deployMode = "cluster";
        String tmpDeployMode = param.getDeployMode();
        if (StringUtils.isNotEmpty(tmpDeployMode)) {
            deployMode = tmpDeployMode;

        }
        if (!LOCAL_DEPLOY_MODE.equals(deployMode)) {
            args.add(Constants.FLINK_RUN_MODE);  //-m

            args.add(Constants.FLINK_YARN_CLUSTER);   //yarn-cluster

            int slot = param.getSlot();
            if (slot != 0) {
                args.add(Constants.FLINK_YARN_SLOT);
                args.add(String.format("%d", slot));   //-ys
            }

            String appName = param.getAppName();
            if (StringUtils.isNotEmpty(appName)) { //-ynm
                args.add(Constants.FLINK_APP_NAME);
                args.add(appName);
            }

            int taskManager = param.getTaskManager();
            if (taskManager != 0) {                        //-yn
                args.add(Constants.FLINK_TASK_MANAGE);
                args.add(String.format("%d", taskManager));
            }

            String jobManagerMemory = param.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                args.add(Constants.FLINK_JOB_MANAGE_MEM);
                args.add(jobManagerMemory); //-yjm
            }

            String taskManagerMemory = param.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) { // -ytm
                args.add(Constants.FLINK_TASK_MANAGE_MEM);
                args.add(taskManagerMemory);
            }

            args.add(Constants.FLINK_DETACH); //-d

        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(Constants.FLINK_MAIN_CLASS);    //-c
            args.add(param.getMainClass());          //main class
        }

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        // --files --conf --libjar ...
        String others = param.getOthers();
        String queue = param.getQueue();
        if (StringUtils.isNotEmpty(others)) {

            if (!others.contains(Constants.FLINK_QUEUE) && StringUtils.isNotEmpty(queue) && !deployMode.equals(LOCAL_DEPLOY_MODE)) {
                args.add(Constants.FLINK_QUEUE);
                args.add(param.getQueue());
            }
            args.add(others);
        } else if (StringUtils.isNotEmpty(queue) && !deployMode.equals(LOCAL_DEPLOY_MODE)) {
            args.add(Constants.FLINK_QUEUE);
            args.add(param.getQueue());
        }

        return args;
    }


}
