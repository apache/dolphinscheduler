package org.apache.dolphinscheduler.plugin.task.flink;/*
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

import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class FlinkTask  extends AbstractYarnTask {


    /**
     *  flink command
     *  usage: flink run [OPTIONS] <jar-file> <arguments>
     */
    private static final String FLINK_COMMAND = "flink";
    private static final String FLINK_RUN = "run";

    /**
     *  flink parameters
     */
    private FlinkParameters flinkParameters;


    private TaskRequest taskRequest;

    public FlinkTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);
        this.taskRequest = taskRequest;
    }

    @Override
    public String getPreScript() throws Exception {
        return super.getPreScript();
    }

    @Override
    public void buildCommand(String script) throws Exception {
        super.buildCommand(script);
    }

    @Override
    public void init() {

        logger.info("flink task params {}", taskRequest.getTaskParams());

        flinkParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), FlinkParameters.class);

        if (!flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }
        flinkParameters.setQueue(taskRequest.getQueue());
        setMainJarName();


        if (StringUtils.isNotEmpty(flinkParameters.getMainArgs())) {
            String args = flinkParameters.getMainArgs();


            // replace placeholder
            Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                    taskExecutionContext.getDefinedParams(),
                    flinkParameters.getLocalParametersMap(),
                    CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                    taskExecutionContext.getScheduleTime());

            logger.info("param Map : {}", paramsMap);
            if (paramsMap != null ){

                args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
                logger.info("param args : {}", args);
            }
            flinkParameters.setMainArgs(args);
        }
    }

    /**
     * create command
     * @return command
     */
    @Override
    protected String buildCommand() {
        // flink run [OPTIONS] <jar-file> <arguments>
        List<String> args = new ArrayList<>();

        args.add(FLINK_COMMAND);
        args.add(FLINK_RUN);
        logger.info("flink task args : {}", args);
        // other parameters
        args.addAll(FlinkArgsUtils.buildArgs(flinkParameters));

        String command = ParameterUtils
                .convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());

        logger.info("flink task command : {}", command);

        return command;
    }

    @Override
    protected void setMainJarName() {
        // main jar
        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            int resourceId = mainJar.getId();
            String resourceName;
            if (resourceId == 0) {
                resourceName = mainJar.getRes();
            } else {
                //when update resource maybe has error ,也许也可以交给上层去做控制 需要看资源是否可以抽象为共性 目前来讲我认为是可以的
                resourceName = mainJar.getResourceName().replaceFirst("/", "");
            }
            mainJar.setRes(resourceName);
            flinkParameters.setMainJar(mainJar);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }
}
