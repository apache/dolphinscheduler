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

package org.apache.dolphinscheduler.server.worker.task.dq;

import static org.apache.dolphinscheduler.common.Constants.DATA_TIME;
import static org.apache.dolphinscheduler.common.Constants.ERROR_OUTPUT_PATH;
import static org.apache.dolphinscheduler.common.Constants.REGEXP_PATTERN;
import static org.apache.dolphinscheduler.common.Constants.SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.YYYY_MM_DD_HH_MM_SS;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.exception.DolphinException;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.dq.DataQualityParameters;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.SparkArgsUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.DataQualityConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * DataQualityTask
 */
public class DataQualityTask extends AbstractYarnTask {

    /**
     * spark2 command
     */
    private static final String SPARK2_COMMAND = "${SPARK_HOME2}/bin/spark-submit";

    private DataQualityParameters dataQualityParameters;

    private final TaskExecutionContext dqTaskExecutionContext;

    public DataQualityTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.dqTaskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() throws Exception {
        logger.info(" data quality task params {}", dqTaskExecutionContext.getTaskParams());

        dataQualityParameters = JSONUtils.parseObject(dqTaskExecutionContext.getTaskParams(), DataQualityParameters.class);

        if (null == dataQualityParameters) {
            logger.error("data quality params is null");
            return;
        }

        if (!dataQualityParameters.checkParameters()) {
            throw new DolphinException("data quality task params is not valid");
        }

        Map<String,String> inputParameter = dataQualityParameters.getRuleInputParameter();
        for (Map.Entry<String,String> entry: inputParameter.entrySet()) {
            if (entry != null && entry.getValue() != null) {
                entry.setValue(entry.getValue().trim());
            }
        }

        DataQualityTaskExecutionContext dataQualityTaskExecutionContext
                        = dqTaskExecutionContext.getDataQualityTaskExecutionContext();

        operateInputParameter(inputParameter, dataQualityTaskExecutionContext);

        RuleManager ruleManager = new RuleManager(
                inputParameter,
                dataQualityTaskExecutionContext);

        DataQualityConfiguration dataQualityConfiguration =
                ruleManager.generateDataQualityParameter();

        dataQualityParameters
                .getSparkParameters()
                .setMainArgs("\""
                        + StringUtils.replaceDoubleBrackets(StringUtils.escapeJava(JSONUtils.toJsonString(dataQualityConfiguration))) + "\"");

        dataQualityParameters
                .getSparkParameters()
                .setQueue(dqTaskExecutionContext.getQueue());

        setMainJarName();
    }

    private void operateInputParameter(Map<String, String> inputParameter, DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        LocalDateTime time = LocalDateTime.now();
        String now = df.format(time);

        inputParameter.put("rule_id", String.valueOf(dataQualityTaskExecutionContext.getRuleId()));
        inputParameter.put("rule_type", String.valueOf(dataQualityTaskExecutionContext.getRuleType().getCode()));
        inputParameter.put("rule_name", StringUtils.wrapperSingleQuotes(dataQualityTaskExecutionContext.getRuleName()));
        inputParameter.put("create_time", StringUtils.wrapperSingleQuotes(now));
        inputParameter.put("update_time", StringUtils.wrapperSingleQuotes(now));
        inputParameter.put("process_definition_id", String.valueOf(dqTaskExecutionContext.getProcessDefineId()));
        inputParameter.put("process_instance_id", String.valueOf(dqTaskExecutionContext.getProcessInstanceId()));
        inputParameter.put("task_instance_id", String.valueOf(dqTaskExecutionContext.getTaskInstanceId()));

        if (StringUtils.isEmpty(inputParameter.get(DATA_TIME))) {
            inputParameter.put(DATA_TIME,StringUtils.wrapperSingleQuotes(now));
        }

        if (StringUtils.isNotEmpty(inputParameter.get(REGEXP_PATTERN))) {
            inputParameter.put(REGEXP_PATTERN,StringUtils.escapeJava(StringUtils.escapeJava(inputParameter.get(REGEXP_PATTERN))));
        }

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getHdfsPath())) {
            inputParameter.put(ERROR_OUTPUT_PATH,
                    dataQualityTaskExecutionContext.getHdfsPath()
                            + SLASH + dqTaskExecutionContext.getProcessDefineId()
                            + UNDERLINE + dqTaskExecutionContext.getProcessInstanceId()
                            + UNDERLINE + dqTaskExecutionContext.getTaskName());
        } else {
            inputParameter.put(ERROR_OUTPUT_PATH,"");
        }
    }

    @Override
    protected String buildCommand() {
        List<String> args = new ArrayList<>();

        args.add(SPARK2_COMMAND);

        // other parameters
        args.addAll(SparkArgsUtils.buildArgs(dataQualityParameters.getSparkParameters()));

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(
                dqTaskExecutionContext.getDefinedParams()),
                dqTaskExecutionContext.getDefinedParams(),
                dataQualityParameters.getLocalParametersMap(),
                CommandType.of(dqTaskExecutionContext.getCmdTypeIfComplement()),
                dqTaskExecutionContext.getScheduleTime());

        String command = null;

        if (null != paramsMap) {
            command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));
        }

        logger.info("spark task command: {}", command);

        return command;
    }

    @Override
    protected void setMainJarName() {
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setRes(System.getProperty("user.dir") + File.separator + "lib" + File.separator + CommonUtils.getDataQualityJarName());
        dataQualityParameters.getSparkParameters().setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return dataQualityParameters;
    }
}
