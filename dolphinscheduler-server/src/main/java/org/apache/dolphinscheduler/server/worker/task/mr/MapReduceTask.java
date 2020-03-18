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
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.mr.MapreduceParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * mapreduce task
 */
public class MapReduceTask extends AbstractYarnTask {


    /**
     * mapreduce parameters
     */
    private MapreduceParameters mapreduceParameters;

    /**
     * constructor
     * @param props     task props
     * @param logger    logger
     */
    public MapReduceTask(TaskProps props, Logger logger) {
        super(props, logger);
    }

    @Override
    public void init() {

        logger.info("mapreduce task params {}", taskProps.getTaskParams());

        this.mapreduceParameters = JSONUtils.parseObject(taskProps.getTaskParams(), MapreduceParameters.class);

        // check parameters
        if (!mapreduceParameters.checkParameters()) {
            throw new RuntimeException("mapreduce task params is not valid");
        }

        mapreduceParameters.setQueue(taskProps.getQueue());

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                taskProps.getDefinedParams(),
                mapreduceParameters.getLocalParametersMap(),
                taskProps.getCmdTypeIfComplement(),
                taskProps.getScheduleTime());
        if (paramsMap != null){
            String args = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getMainArgs(),  ParamUtils.convert(paramsMap));
            mapreduceParameters.setMainArgs(args);
            if(mapreduceParameters.getProgramType() != null && mapreduceParameters.getProgramType() == ProgramType.PYTHON){
                String others = ParameterUtils.convertParameterPlaceholders(mapreduceParameters.getOthers(),  ParamUtils.convert(paramsMap));
                mapreduceParameters.setOthers(others);
            }
        }
    }

    /**
     * build command
     * @return command
     * @throws Exception exception
     */
    @Override
    protected String buildCommand() throws Exception {
        List<String> parameterList = buildParameters(mapreduceParameters);

        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", parameterList),
                taskProps.getDefinedParams());
        logger.info("mapreduce task command: {}", command);

        return command;
    }

    @Override
    public AbstractParameters getParameters() {
        return mapreduceParameters;
    }

    /**
     * build parameters
     * @param mapreduceParameters mapreduce parameters
     * @return parameter list
     */
    private List<String> buildParameters(MapreduceParameters mapreduceParameters){

        List<String> result = new ArrayList<>();

        result.add(Constants.HADOOP);

        // main jar
        if(mapreduceParameters.getMainJar()!= null){
            result.add(Constants.JAR);
            result.add(mapreduceParameters.getMainJar().getRes());
        }

        // main class
        if(!ProgramType.PYTHON.equals(mapreduceParameters.getProgramType())
                && StringUtils.isNotEmpty(mapreduceParameters.getMainClass())){
            result.add(mapreduceParameters.getMainClass());
        }

        // others
        if (StringUtils.isNotEmpty(mapreduceParameters.getOthers())) {
            String others = mapreduceParameters.getOthers();
            if (!others.contains(Constants.MR_QUEUE)
                    && StringUtils.isNotEmpty(mapreduceParameters.getQueue())) {
                result.add(String.format("%s %s=%s", Constants.D, Constants.MR_QUEUE, mapreduceParameters.getQueue()));
            }

            result.add(mapreduceParameters.getOthers());
        }else if (StringUtils.isNotEmpty(mapreduceParameters.getQueue())) {
            result.add(String.format("%s %s=%s", Constants.D, Constants.MR_QUEUE, mapreduceParameters.getQueue()));

        }

        // command args
        if(StringUtils.isNotEmpty(mapreduceParameters.getMainArgs())){
            result.add(mapreduceParameters.getMainArgs());
        }
        return result;
    }
}

