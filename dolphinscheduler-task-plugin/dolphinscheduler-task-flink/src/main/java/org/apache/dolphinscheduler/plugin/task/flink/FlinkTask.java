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
     * The pattern of the Flink JobID printed by the submission, it is not the YARN/K8s application
     * id.
     */
    protected static final Pattern FLINK_JOB_ID_REGEX = Pattern.compile(TaskConstants.FLINK_JOB_ID_REGEX);

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
    protected String findFlinkJobId(String line) {
        Matcher matcher = FLINK_JOB_ID_REGEX.matcher(line);
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
     * deployments. So the job is cancelled through the flink CLI first. The process tree kill and
     * the YARN/K8s application cancellation are used when no JobID was found or when the task was
     * submitted to a resource manager, and they are always used to stop the client process when the
     * CLI fails.
     */
    @Override
    public void cancelApplication() throws TaskException {
        TaskException cancelFailure = null;
        try {
            if (cancelFlinkJob()) {
                return;
            }
        } catch (TaskException e) {
            cancelFailure = e;
        }
        // The client process has to be stopped even when the remote job could not be cancelled,
        // otherwise the worker keeps the task executor running and the task instance cannot reach a
        // final state. The failure is still propagated afterwards.
        try {
            cancelClientByProcess();
        } catch (TaskException e) {
            if (cancelFailure == null) {
                cancelFailure = e;
            } else {
                cancelFailure.addSuppressed(e);
            }
        }
        if (cancelFailure != null) {
            throw cancelFailure;
        }
    }

    /**
     * Stop the flink client process of the task, kept as a separate method so that the fallback can
     * be verified in tests.
     */
    protected void cancelClientByProcess() throws TaskException {
        super.cancelApplication();
    }

    /**
     * Cancel the flink job through the flink CLI.
     *
     * <p>Tasks submitted to YARN are not handled here: their application id is known and cancelling
     * the application through the resource manager is the reliable way. A Kubernetes cluster is
     * identified by a cluster id rather than by an application id, so a job submitted to Kubernetes
     * is cancelled through the CLI, with the cluster connection options of the submission.
     *
     * @return true if the job was cancelled through the CLI, false if the caller should fall back
     * @throws TaskException if a cancelable job was found but could not be cancelled, so that the
     *         failure is not hidden by the fallback
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
        List<String> clusterConnectionOptions = getClusterConnectionOptions();
        for (String jobId : jobIds) {
            List<String> args = FlinkArgsUtils.buildCancelCommandLine(jobId, clusterConnectionOptions);
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
            return lines.map(this::findFlinkJobId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Read task log failed, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * The cluster connection options of the submission, for example {@code -m <jobmanager>} or
     * {@code -t <target>}. They have to be repeated on the cancel / savepoint command, otherwise
     * that command targets the default cluster of the worker instead of the cluster the job was
     * submitted to.
     */
    protected List<String> getClusterConnectionOptions() {
        AbstractParameters parameters = getParameters();
        if (!(parameters instanceof FlinkParameters)) {
            return Collections.emptyList();
        }
        return FlinkArgsUtils.extractClusterConnectionOptions(((FlinkParameters) parameters).getOthers());
    }

    /**
     * Execute the given flink command, kept as a separate method so it can be verified in tests.
     */
    protected boolean executeFlinkCommand(List<String> args) {
        return FlinkArgsUtils.executeCommand(taskExecutionContext, args);
    }

    /**
     * Execute the savepoint command. It is kept as a separate method so it can be verified in tests,
     * and it gets its own timeout because a savepoint of a large stateful job takes much longer than
     * a cancel.
     */
    protected boolean executeFlinkSavepointCommand(List<String> args) {
        return FlinkArgsUtils.executeCommand(taskExecutionContext, args,
                FlinkConstants.FLINK_SAVEPOINT_TIMEOUT_SECONDS);
    }
}
