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
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;
import org.apache.dolphinscheduler.common.utils.StringUtils;

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
    public static List<String> buildArgs(MapReduceParameters param) {
        List<String> args = new ArrayList<>();

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(Constants.JAR);
            args.add(mainJar.getRes());
        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(mainClass);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(String.format("%s%s=%s", Constants.D, Constants.MR_NAME, ArgsUtils.escape(appName)));
        }

        String others = param.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(Constants.MR_QUEUE)) {
            String queue = param.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(String.format("%s%s=%s", Constants.D, Constants.MR_QUEUE, queue));
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
