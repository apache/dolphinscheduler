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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Find the Flink JobID in a log line. Both the {@code flink run} format (JobID) and the Flink
     * SQL Client format (Job ID:) are supported. The returned id is the Flink JobID, it is not the
     * YARN/K8s application id.
     *
     * @param line line
     * @return the Flink JobID, or null when the line does not contain one
     */
    protected String findAppId(String line) {
        Matcher matcher = FLINK_APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Cancel the flink task.
     *
     * <p>The process started by the worker is only the flink client, not the job itself: when the
     * client exits, or when it is killed, the job keeps running for local/standalone/session
     * deployments. So the job is cancelled through the flink CLI first, and the default behaviour
     * (kill the process tree and cancel the YARN/K8s application) is only used as a fallback.
     */
    @Override
    public void cancelApplication() throws TaskException {
        if (cancelFlinkJob()) {
            return;
        }
        super.cancelApplication();
    }

    /**
     * Cancel the flink job through the flink CLI.
     *
     * <p>Tasks submitted to YARN or K8s are not handled here: for them the application id is known
     * and cancelling the application through the resource manager is the reliable way.
     *
     * @return true if the job was cancelled through the CLI, false if the caller should fall back
     * @throws TaskException if a cancelable job was found but could not be cancelled, so that the
     *         failure is reported instead of being hidden behind the fallback
     */
    protected boolean cancelFlinkJob() throws TaskException {
        try {
            if (CollectionUtils.isNotEmpty(getApplicationIds())) {
                return false;
            }
        } catch (Exception e) {
            log.warn("Get application ids failed, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        List<String> jobIds = getFlinkJobIds();
        if (CollectionUtils.isEmpty(jobIds)) {
            log.info(
                    "Cannot find flink JobID from the task log, taskInstanceId: {}, will fall back to cancel by process",
                    taskExecutionContext.getTaskInstanceId());
            return false;
        }
        for (String jobId : jobIds) {
            List<String> args = FlinkArgsUtils.buildCancelCommandLine(jobId);
            log.info("Cancel flink job, jobId: {}, args: {}", jobId, args);
            if (!executeFlinkCommand(args)) {
                throw new TaskException(String.format(
                        "Cancel flink job %s failed, the job may still be running, taskInstanceId: %s", jobId,
                        taskExecutionContext.getTaskInstanceId()));
            }
        }
        log.info("Successfully cancelled flink jobs: {}", jobIds);
        return true;
    }

    /**
     * The flink JobID is printed by `flink run` and is the id which `flink cancel` accepts.
     */
    protected List<String> getFlinkJobIds() {
        String logPath = taskExecutionContext.getLogPath();
        File logFile = StringUtils.isEmpty(logPath) ? null : new File(logPath);
        if (logFile == null || !logFile.isFile()) {
            return Collections.emptyList();
        }
        try (Stream<String> lines = Files.lines(logFile.toPath())) {
            return lines.map(this::findAppId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Read task log failed, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * Execute the given flink command, kept as a separate method so it can be verified in tests.
     */
    protected boolean executeFlinkCommand(List<String> args) {
        return FlinkArgsUtils.executeCommand(taskExecutionContext, args);
    }
}
