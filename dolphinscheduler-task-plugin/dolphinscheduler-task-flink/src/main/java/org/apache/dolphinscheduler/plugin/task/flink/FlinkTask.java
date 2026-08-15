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
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
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

    /**
     * Cancel the Flink application.
     * <p>
     * This method first attempts to gracefully cancel the Flink job using the
     * {@code flink cancel} command with the application ID. This ensures that
     * the Flink job is properly stopped and resources are released. If the
     * graceful cancel fails (e.g., the Flink CLI is unavailable or the job
     * is already terminated), it falls back to the parent class's
     * {@link #cancelApplication()} which uses process-level kill signals.
     *
     * @throws TaskException if both the Flink cancel and the fallback kill fail
     */
    @Override
    public void cancelApplication() throws TaskException {
        try {
            String appIds = String.join(TaskConstants.COMMA, getApplicationIds());
            if (StringUtils.isNotBlank(appIds)) {
                log.info("Attempting to gracefully cancel Flink job with appId(s): {}", appIds);
                List<String> cancelCommand = FlinkArgsUtils.buildCancelCommandLine(taskExecutionContext);
                String command = String.join(" ", cancelCommand);
                log.info("Executing Flink cancel command: {}", command);
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
                Process cancelProcess = processBuilder.start();
                boolean finished = cancelProcess.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
                if (finished && cancelProcess.exitValue() == 0) {
                    log.info("Flink job cancelled successfully via flink cancel command, appId(s): {}", appIds);
                    return;
                } else {
                    log.warn("Flink cancel command did not succeed (exit code: {}, finished: {}), "
                            + "falling back to process kill", 
                            finished ? cancelProcess.exitValue() : -1, finished);
                    cancelProcess.destroyForcibly();
                }
            } else {
                log.info("No appIds found for Flink task, skipping flink cancel, falling back to process kill");
            }
        } catch (Exception e) {
            log.warn("Failed to gracefully cancel Flink job, falling back to process kill", e);
        }
        // Fall back to the default process-level kill
        super.cancelApplication();
    }
}
