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

package org.apache.dolphinscheduler.plugin.task.dq;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SLASH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNDERLINE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.CREATE_TIME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.DATA_TIME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.ERROR_OUTPUT_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.PROCESS_DEFINITION_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.PROCESS_INSTANCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.REGEXP_PATTERN;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.RULE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.RULE_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.RULE_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.UPDATE_TIME;
import static org.apache.dolphinscheduler.spi.utils.Constants.YYYY_MM_DD_HH_MM_SS;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.DataQualityConfiguration;
import org.apache.dolphinscheduler.plugin.task.dq.utils.spark.SparkArgsUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In DataQualityTask, the input parameters will be converted into DataQualityConfiguration,
 * which will be converted into a string as the parameter of DataQualityApplication,
 * and DataQualityApplication is spark application
 */
public class DataQualityTask extends AbstractYarnTask {

    /**
     * spark2 command
     */
    private static final String SPARK2_COMMAND = "${SPARK_HOME2}/bin/spark-submit";

    private DataQualityParameters dataQualityParameters;

    private final TaskExecutionContext dqTaskExecutionContext;

    public DataQualityTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.dqTaskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("data quality task params {}", dqTaskExecutionContext.getTaskParams());

        dataQualityParameters = JSONUtils.parseObject(dqTaskExecutionContext.getTaskParams(), DataQualityParameters.class);

        if (null == dataQualityParameters) {
            logger.error("data quality params is null");
            return;
        }

        if (!dataQualityParameters.checkParameters()) {
            throw new RuntimeException("data quality task params is not valid");
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

        inputParameter.put(RULE_ID, String.valueOf(dataQualityTaskExecutionContext.getRuleId()));
        inputParameter.put(RULE_TYPE, String.valueOf(dataQualityTaskExecutionContext.getRuleType()));
        inputParameter.put(RULE_NAME, ArgsUtils.wrapperSingleQuotes(dataQualityTaskExecutionContext.getRuleName()));
        inputParameter.put(CREATE_TIME, ArgsUtils.wrapperSingleQuotes(now));
        inputParameter.put(UPDATE_TIME, ArgsUtils.wrapperSingleQuotes(now));
        inputParameter.put(PROCESS_DEFINITION_ID, String.valueOf(dqTaskExecutionContext.getProcessDefineId()));
        inputParameter.put(PROCESS_INSTANCE_ID, String.valueOf(dqTaskExecutionContext.getProcessInstanceId()));
        inputParameter.put(TASK_INSTANCE_ID, String.valueOf(dqTaskExecutionContext.getTaskInstanceId()));

        if (StringUtils.isEmpty(inputParameter.get(DATA_TIME))) {
            inputParameter.put(DATA_TIME,ArgsUtils.wrapperSingleQuotes(now));
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
        Map<String, Property> paramsMap = ParamUtils.convert(dqTaskExecutionContext,getParameters());

        String command = null;

        if (null != paramsMap) {
            command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));
        }

        logger.info("data quality task command: {}", command);

        return command;
    }

    @Override
    protected void setMainJarName() {
        ResourceInfo mainJar = new ResourceInfo();
        String basePath = System.getProperty("user.dir").replace(File.separator + "bin", File.separator + "libs");
        mainJar.setRes(basePath + File.separator + CommonUtils.getDataQualityJarName());
        dataQualityParameters.getSparkParameters().setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return dataQualityParameters;
    }
}
