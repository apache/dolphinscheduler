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
package cn.escheduler.server.utils;


import cn.escheduler.common.task.flink.FlinkParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static cn.escheduler.common.Constants.*;


/**
 *  flink args utils
 */
public class FlinkArgsUtils {

    /**
     *  build args
     * @param param
     * @return
     */
    public static List<String> buildArgs(FlinkParameters param) {
        List<String> args = new ArrayList<>();

        // use flink run option
        args.add(FLINK_RUN);

        if(StringUtils.isNotEmpty(param.getDeployMode()) && "yarn-cluster".equals(param.getDeployMode())){
            args.add(FLINK_MODE);
            args.add(param.getDeployMode());

            if (param.getYarncontainer() != 0) {
                args.add(FLINK_YARN_CONTAINER);
                args.add(String.valueOf(param.getYarncontainer()));
            }

            if (param.getYarnjobManagerMemory() != 0) {
                args.add(FLINK_JOB_MANAGER_MEMORY);
                args.add(String.valueOf(param.getYarnjobManagerMemory()));
            }

            if (param.getYarntaskManagerMemory() != 0) {
                args.add(FLINK_TASK_MANAGER_MEMORY);
                args.add(String.valueOf(param.getYarntaskManagerMemory()));
            }
            if(StringUtils.isNotBlank(param.getYarnName())){
                args.add(FLINK_YARN_JOB_NAME);
                args.add(param.getYarnName());
            }
        }
        // user inputs Command-line
        if(param.getMainArgs() != null && !"".equals(param.getMainArgs())){
            args.add(param.getMainArgs());
        }

        if(StringUtils.isNotEmpty(param.getMainClass()) && !"PYTHON".equals(param.getProgramType())) {
            args.add(FLINK_CLASS);
            args.add(param.getMainClass());

            args.add(param.getMainJar().getRes());
        }

        return args;
    }

}