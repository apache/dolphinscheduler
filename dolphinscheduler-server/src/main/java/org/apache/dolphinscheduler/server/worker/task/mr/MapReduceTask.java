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

package org.apache.dolphinscheduler.server.worker.task.mr;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.MapReduceArgsUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * mapreduce task
 */
public class MapReduceTask extends AbstractYarnTask {

    /**
     *  mapreduce command
     *  usage: hadoop jar <jar> [mainClass] [GENERIC_OPTIONS] args...
     */
    private static final String MAPREDUCE_COMMAND = Constants.HADOOP;

    /**
     * mapreduce parameters
     */
    private MapReduceParameters mapreduceParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext taskExecutionContext
     * @param logger    logger
     */
    public MapReduceTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        logger.info("mapreduce task params {}", taskExecutionContext.getTaskParams());

        this.mapreduceParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), MapReduceParameters.class);

        // check parameters
        if (mapreduceParameters == null || !mapreduceParameters.checkParameters()) {
            throw new RuntimeException("mapreduce task params is not valid");
        }

        mapreduceParameters.setQueue(taskExecutionContext.getQueue());
        setMainJarName();

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                taskExecutionContext.getDefinedParams(),
                mapreduceParameters.getLocalParametersMap(),
                CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                taskExecutionContext.getScheduleTime());

        if (paramsMap != null) {
            String args = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getMainArgs(),  ParamUtils.convert(paramsMap));
            mapreduceParameters.setMainArgs(args);
            if (mapreduceParameters.getProgramType() != null && mapreduceParameters.getProgramType() == ProgramType.PYTHON) {
                String others = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getOthers(),  ParamUtils.convert(paramsMap));
                mapreduceParameters.setOthers(others);
            }
        }
    }

    /**
     * build command
     * @return command
     */
    @Override
    protected String buildCommand() {
        // hadoop jar <jar> [mainClass] [GENERIC_OPTIONS] args...
        List<String> args = new ArrayList<>();
        args.add(MAPREDUCE_COMMAND);

        // other parameters
        args.addAll(MapReduceArgsUtils.buildArgs(mapreduceParameters));

        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args),
                taskExecutionContext.getDefinedParams());
        logger.info("mapreduce task command: {}", command);

        return command;
    }

    @Override
    protected void setMainJarName() {
        // main jar
        ResourceInfo mainJar = mapreduceParameters.getMainJar();
        if (mainJar != null) {
            int resourceId = mainJar.getId();
            String resourceName;
            if (resourceId == 0) {
                resourceName = mainJar.getRes();
            } else {
                Resource resource = processService.getResourceById(mapreduceParameters.getMainJar().getId());
                if (resource == null) {
                    logger.error("resource id: {} not exist", resourceId);
                    throw new RuntimeException(String.format("resource id: %d not exist", resourceId));
                }
                resourceName = resource.getFullName().replaceFirst("/", "");
            }
            mainJar.setRes(resourceName);
            mapreduceParameters.setMainJar(mainJar);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return mapreduceParameters;
    }
}
