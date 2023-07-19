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

import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;
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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.DataQualityParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.DataQualityConfiguration;
import org.apache.dolphinscheduler.plugin.task.dq.utils.SparkArgsUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In DataQualityTask, the input parameters will be converted into DataQualityConfiguration,
 * which will be converted into a string as the parameter of DataQualityApplication,
 * and DataQualityApplication is spark application
 */
public class DataQualityTask extends AbstractYarnTask {

    /**
     * spark command
     */
    private static final String SPARK_COMMAND = "${SPARK_HOME}/bin/spark-submit";

    private DataQualityParameters dataQualityParameters;

    private final TaskExecutionContext dqTaskExecutionContext;

    public DataQualityTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.dqTaskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        dataQualityParameters =
                JSONUtils.parseObject(dqTaskExecutionContext.getTaskParams(), DataQualityParameters.class);
        log.info("Initialize data quality task params {}", JSONUtils.toPrettyJsonString(dataQualityParameters));

        if (null == dataQualityParameters) {
            log.error("data quality params is null");
            return;
        }

        if (!dataQualityParameters.checkParameters()) {
            throw new RuntimeException("data quality task params is not valid");
        }

        Map<String, String> inputParameter = dataQualityParameters.getRuleInputParameter();
        for (Map.Entry<String, String> entry : inputParameter.entrySet()) {
            if (entry != null && entry.getValue() != null) {
                entry.setValue(entry.getValue().trim());
            }
        }

        DataQualityTaskExecutionContext dataQualityTaskExecutionContext =
                dqTaskExecutionContext.getDataQualityTaskExecutionContext();

        operateInputParameter(inputParameter, dataQualityTaskExecutionContext);

        RuleManager ruleManager = new RuleManager(
                inputParameter,
                dataQualityTaskExecutionContext);

        DataQualityConfiguration dataQualityConfiguration =
                ruleManager.generateDataQualityParameter();

        dataQualityParameters
                .getSparkParameters()
                .setMainArgs("\""
                        + replaceDoubleBrackets(
                                StringEscapeUtils.escapeJava(JSONUtils.toJsonString(dataQualityConfiguration)))
                        + "\"");

        setMainJarName();
    }

    private void operateInputParameter(Map<String, String> inputParameter,
                                       DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
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
            inputParameter.put(DATA_TIME, ArgsUtils.wrapperSingleQuotes(now));
        }

        if (StringUtils.isNotEmpty(inputParameter.get(REGEXP_PATTERN))) {
            inputParameter.put(REGEXP_PATTERN,
                    StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(inputParameter.get(REGEXP_PATTERN))));
        }

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getHdfsPath())) {
            inputParameter.put(ERROR_OUTPUT_PATH,
                    dataQualityTaskExecutionContext.getHdfsPath()
                            + SLASH + dqTaskExecutionContext.getProcessDefineId()
                            + UNDERLINE + dqTaskExecutionContext.getProcessInstanceId()
                            + UNDERLINE + dqTaskExecutionContext.getTaskName());
        } else {
            inputParameter.put(ERROR_OUTPUT_PATH, "");
        }
    }

    @Override
    protected String getScript() {
        List<String> args = new ArrayList<>();
        args.add(SPARK_COMMAND);
        args.addAll(SparkArgsUtils.buildArgs(dataQualityParameters.getSparkParameters()));
        return args.stream().collect(Collectors.joining(" "));
    }

    @Override
    protected Map<String, String> getProperties() {
        return ParameterUtils.convert(dqTaskExecutionContext.getPrepareParamsMap());
    }

    protected void setMainJarName() {
        ResourceInfo mainJar = new ResourceInfo();
        String basePath = System.getProperty("user.dir").replace(File.separator + "bin", "");
        mainJar.setResourceName(
                basePath + File.separator + "libs" + File.separator + CommonUtils.getDataQualityJarName());
        dataQualityParameters.getSparkParameters().setMainJar(mainJar);
    }

    @Override
    public AbstractParameters getParameters() {
        return dataQualityParameters;
    }

    private static String replaceDoubleBrackets(String mainParameter) {
        mainParameter = mainParameter
                .replace(Constants.DOUBLE_BRACKETS_LEFT, Constants.DOUBLE_BRACKETS_LEFT_SPACE)
                .replace(Constants.DOUBLE_BRACKETS_RIGHT, Constants.DOUBLE_BRACKETS_RIGHT_SPACE);
        if (mainParameter.contains(Constants.DOUBLE_BRACKETS_LEFT)
                || mainParameter.contains(Constants.DOUBLE_BRACKETS_RIGHT)) {
            return replaceDoubleBrackets(mainParameter);
        } else {
            return mainParameter;
        }
    }
}
