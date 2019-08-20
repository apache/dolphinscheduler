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


import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ProgramType;
import cn.escheduler.common.task.flink.FlinkParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 *  spark args utils
 */
public class FlinkArgsUtils {

    /**
     *  build args
     * @param param
     * @return
     */
    public static List<String> buildArgs(FlinkParameters param) {
        List<String> args = new ArrayList<>();

        args.add(Constants.FLINK_RUN_MODE);  //-m

        args.add(Constants.FLINK_YARN_CLUSTER);   //yarn-cluster

        if (param.getSlot() != 0) {
            args.add(Constants.FLINK_YARN_SLOT);
            args.add(String.format("%d", param.getSlot()));   //-ys
        }

        if (StringUtils.isNotEmpty(param.getAppName())) { //-ynm
            args.add(Constants.FLINK_APP_NAME);
            args.add(param.getAppName());
        }

        if (param.getTaskManager() != 0) {                        //-yn
            args.add(Constants.FLINK_TASK_MANAGE);
            args.add(String.format("%d", param.getTaskManager()));
        }

        if (StringUtils.isNotEmpty(param.getJobManagerMemory())) {
            args.add(Constants.FLINK_JOB_MANAGE_MEM);
            args.add(param.getJobManagerMemory()); //-yjm
        }

        if (StringUtils.isNotEmpty(param.getTaskManagerMemory())) { // -ytm
            args.add(Constants.FLINK_TASK_MANAGE_MEM);
            args.add(param.getTaskManagerMemory());
        }
        args.add(Constants.FLINK_detach); //-d


        if(param.getProgramType() !=null ){
            if(param.getProgramType()!=ProgramType.PYTHON){
                if (StringUtils.isNotEmpty(param.getMainClass())) {
                    args.add(Constants.FLINK_MAIN_CLASS);    //-c
                    args.add(param.getMainClass());          //main class
                }
            }
        }

        if (param.getMainJar() != null) {
            args.add(param.getMainJar().getRes());
        }


        // --files --conf --libjar ...
      if (StringUtils.isNotEmpty(param.getOthers())) {
            String others = param.getOthers();
            if(!others.contains("--queue")){
                if (StringUtils.isNotEmpty(param.getQueue())) {
                    args.add(Constants.SPARK_QUEUE);
                    args.add(param.getQueue());
                }
            }
            args.add(param.getOthers());
        }else if (StringUtils.isNotEmpty(param.getQueue())) {
            args.add(Constants.SPARK_QUEUE);
            args.add(param.getQueue());

        }

       if (StringUtils.isNotEmpty(param.getMainArgs())) {
            args.add(param.getMainArgs());
        }

        return args;
    }

}
