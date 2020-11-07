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

package org.apache.dolphinscheduler.server.worker.task.sqoop;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;

import java.util.Map;

import org.slf4j.Logger;

/**
 * sqoop task extends the shell task
 */
public class SqoopTask extends AbstractYarnTask {

    /**
     * sqoop task params
     */
    private SqoopParameters sqoopParameters;

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;

    public SqoopTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("sqoop task params {}", taskExecutionContext.getTaskParams());
        sqoopParameters =
            JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqoopParameters.class);
        //check sqoop task params
        if (null == sqoopParameters) {
            throw new RuntimeException("Sqoop Task params is null");
        }

        if (!sqoopParameters.checkParameters()) {
            throw new RuntimeException("Sqoop Task params check fail");
        }
    }

    @Override
    protected String buildCommand() {
        //get sqoop scripts
        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters, taskExecutionContext);

        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
            taskExecutionContext.getDefinedParams(),
            sqoopParameters.getLocalParametersMap(),
            CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
            taskExecutionContext.getScheduleTime());

        if (paramsMap != null) {
            String resultScripts = ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
            logger.info("sqoop script: {}", resultScripts);
            return resultScripts;
        }

        return null;
    }

    @Override
    protected void setMainJarName() {
    }

    @Override
    public AbstractParameters getParameters() {
        return sqoopParameters;
    }
}
