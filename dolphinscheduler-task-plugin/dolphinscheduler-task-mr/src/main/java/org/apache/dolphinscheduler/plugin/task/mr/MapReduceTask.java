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

import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mapreduce task
 */
public class MapReduceTask extends AbstractYarnTask {

    /**
     *  mapreduce command
     *  usage: hadoop jar <jar> [mainClass] [GENERIC_OPTIONS] args...
     */
    private static final String MAPREDUCE_COMMAND = TaskConstants.HADOOP;

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
     */
    public MapReduceTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
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

        // replace placeholder,and combine local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext,getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }

        String args = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getMainArgs(),  ParamUtils.convert(paramsMap));
        mapreduceParameters.setMainArgs(args);
        if (mapreduceParameters.getProgramType() != null && mapreduceParameters.getProgramType() == ProgramType.PYTHON) {
            String others = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getOthers(),  ParamUtils.convert(paramsMap));
            mapreduceParameters.setOthers(others);
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
        String resourceName = getResourceNameOfMainJar(mainJar);
        mainJar.setRes(resourceName);
        mapreduceParameters.setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return mapreduceParameters;
    }
}
