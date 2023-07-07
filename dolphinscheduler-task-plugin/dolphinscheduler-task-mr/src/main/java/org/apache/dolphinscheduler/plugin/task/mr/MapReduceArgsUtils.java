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

package org.apache.dolphinscheduler.plugin.task.mr;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.D;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAR;
import static org.apache.dolphinscheduler.plugin.task.mr.MapReduceTaskConstants.MR_NAME;
import static org.apache.dolphinscheduler.plugin.task.mr.MapReduceTaskConstants.MR_YARN_QUEUE;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * mapreduce args utils
 */
public class MapReduceArgsUtils {

    private MapReduceArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     *
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(MapReduceParameters param, TaskExecutionContext taskExecutionContext) {
        List<String> args = new ArrayList<>();

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(JAR);
            args.add(taskExecutionContext.getResources().get(mainJar.getResourceName()));
        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(mainClass);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(String.format("%s%s=%s", D, MR_NAME, ArgsUtils.escape(appName)));
        }

        String others = param.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(MR_YARN_QUEUE)) {
            String yarnQueue = param.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                args.add(String.format("%s%s=%s", D, MR_YARN_QUEUE, yarnQueue));
            }
        }

        // -conf -archives -files -libjars -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}
