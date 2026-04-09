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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkTask extends AbstractYarnTask {

    private FlinkParameters flinkParameters;

    private TaskExecutionContext taskExecutionContext;

    /**
     * rules for flink application ID
     */
    protected static final Pattern FLINK_APPLICATION_REGEX = Pattern.compile(TaskConstants.FLINK_APPLICATION_REGEX);

    public FlinkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        flinkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkParameters.class);
        log.info("Initialize flink task params {}", JSONUtils.toPrettyJsonString(flinkParameters));

        if (flinkParameters == null || !flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }
    }

    /**
     * create command
     *
     * @return command
     */
    @Override
    protected String getScript() {
        return buildScriptWithParameterReplacement(flinkParameters);
    }

    /**
     * Apply parameter replacement to initScript/rawScript, generate script files and build run command.
     *
     * @param params flink parameters
     * @return run command string
     */
    protected String buildScriptWithParameterReplacement(FlinkParameters params) {
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        Map<String, String> stringParams = ParameterUtils.convert(paramsMap);

        if (StringUtils.isNotBlank(params.getInitScript())) {
            params.setInitScript(
                    ParameterUtils.convertParameterPlaceholders(params.getInitScript(), stringParams));
        }
        if (StringUtils.isNotBlank(params.getRawScript())) {
            params.setRawScript(
                    ParameterUtils.convertParameterPlaceholders(params.getRawScript(), stringParams));
        }

        FileUtils.generateScriptFile(taskExecutionContext, params);

        List<String> args = FlinkArgsUtils.buildRunCommandLine(taskExecutionContext, params);
        return args.stream().collect(Collectors.joining(" "));
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }

    /**
     * find app id
     *
     * @param line line
     * @return appid
     */
    protected String findAppId(String line) {
        Matcher matcher = FLINK_APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            String str = matcher.group();
            return str.substring(6);
        }
        return null;
    }
}
