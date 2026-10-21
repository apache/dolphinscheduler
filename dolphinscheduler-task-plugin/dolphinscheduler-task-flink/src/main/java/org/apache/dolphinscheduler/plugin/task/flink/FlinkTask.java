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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
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

    /**
     * The pattern of the YARN application id printed by the submission, it identifies the cluster
     * which runs a job and is not the Flink JobID.
     */
    protected static final Pattern YARN_APPLICATION_ID_REGEX =
            Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    /**
     * The message the flink CLI reports when the job is already in another terminal state, so it does
     * not have to be cancelled any more.
     */
    private static final String JOB_ALREADY_IN_TERMINAL_STATE_MESSAGE = "already reached another terminal state";

    /**
     * The message the SQL Client prints when it waits for a synchronous submission to finish. The
     * JobID of such a submission is never printed, so it is only reachable through the task log.
     */
    private static final String SYNCHRONOUS_SUBMISSION_STARTED_MESSAGE = "Execute statement in sync mode";

    /**
     * The message the SQL Client prints when a synchronous submission has finished.
     */
    private static final String SYNCHRONOUS_SUBMISSION_FINISHED_MESSAGE =
            "Complete execution of the SQL update statement";

    /**
     * The maximum time to wait for the submission output which is still buffered when the client is
     * stopped to be written to the task log.
     */
    private static final long SUBMISSION_OUTPUT_DRAIN_TIMEOUT_MILLIS = 2_000L;

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
     * deployments. So the jobs are cancelled through the flink CLI.
     *
     * <p>The client is stopped before the JobIDs are collected: while it is running it can submit
     * further jobs, which would be missing from an earlier snapshot. The client stop also cancels the
     * YARN/K8s application of the task.
     */
    @Override
    public void cancelApplication() throws TaskException {
        TaskException cancelFailure = null;
        try {
            cancelClientByProcess();
        } catch (TaskException e) {
            cancelFailure = e;
        }
        // the submission output is collected asynchronously, its tail has to reach the task log before
        // the JobIDs are collected from it
        awaitBufferedSubmissionOutput();
        try {
            cancelFlinkJob();
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
     * Wait until the output of the submission has been written to the task log, so that the JobIDs of
     * the jobs which were submitted right before the client was stopped are not missed. The wait is
     * bounded, a command which does not release its output pipe must not block the cancellation.
     */
    private void awaitBufferedSubmissionOutput() {
        if (!awaitSubmissionOutputCollected(SUBMISSION_OUTPUT_DRAIN_TIMEOUT_MILLIS)) {
            log.warn("The submission output could not be collected within {}ms, collect the JobIDs anyway",
                    SUBMISSION_OUTPUT_DRAIN_TIMEOUT_MILLIS);
        }
    }

    /**
     * Wait for the output of the submission to be written to the task log, kept as a separate method so
     * that the wait can be verified in tests.
     */
    protected boolean awaitSubmissionOutputCollected(long timeoutMillis) {
        return awaitTaskOutputCollected(timeoutMillis);
    }

    /**
     * Cancel the flink jobs through the flink CLI.
     *
     * <p>Tasks submitted to YARN are not handled here: their application id is known and cancelling
     * the application through the resource manager is the reliable way. A Kubernetes cluster is
     * identified by a cluster id rather than by an application id, so a job submitted to Kubernetes
     * is cancelled through the CLI, with the cluster connection options of the submission.
     *
     * <p>Every collected JobID is attempted. A job which is already in a terminal state cannot be
     * cancelled any more, which is not a failure, so it is not reported as one.
     *
     * @throws TaskException if at least one job which is not in a terminal state could not be
     *         cancelled
     */
    protected void cancelFlinkJob() throws TaskException {
        try {
            if (CollectionUtils.isNotEmpty(getApplicationIds())) {
                return;
            }
        } catch (Exception e) {
            log.warn("Get application ids failed, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        List<String> jobIds = getFlinkJobIds();
        // a synchronous submission which is still running prints no JobID, so it cannot be cancelled
        // through the flink CLI: it must not be reported as cancelled either
        int pendingSynchronousSubmissions = countPendingSynchronousSubmissions();
        if (CollectionUtils.isEmpty(jobIds) && pendingSynchronousSubmissions == 0) {
            log.info("Cannot find flink JobID from the task log, taskInstanceId: {}",
                    taskExecutionContext.getTaskInstanceId());
            return;
        }
        List<List<String>> clusterConnectionOptionsPerJob = getClusterConnectionOptionsPerJob();
        // a job which the scripts do not describe, for example one submitted from another file, uses
        // the last configuration of the submission
        List<String> lastClusterConnectionOptions = getClusterConnectionOptions();
        List<String> failedJobIds = new ArrayList<>();
        for (int i = 0; i < jobIds.size(); i++) {
            String jobId = jobIds.get(i);
            List<String> clusterConnectionOptions = i < clusterConnectionOptionsPerJob.size()
                    ? clusterConnectionOptionsPerJob.get(i)
                    : lastClusterConnectionOptions;
            List<String> args = FlinkArgsUtils.buildCancelCommandLine(jobId, clusterConnectionOptions);
            FlinkArgsUtils.FlinkCommandResult result = executeFlinkCancelCommand(args);
            if (result.isSuccess()) {
                log.info("Cancelled flink job: {}", jobId);
                continue;
            }
            if (isJobAlreadyInTerminalState(result.getOutput())) {
                log.info("Flink job {} is already in a terminal state, it does not need to be cancelled", jobId);
                continue;
            }
            log.error("Cancel flink job {} failed, the job may still be running", jobId);
            failedJobIds.add(jobId);
        }
        List<String> failures = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(failedJobIds)) {
            failures.add(String.format("the jobs %s could not be cancelled, they may still be running", failedJobIds));
        }
        if (pendingSynchronousSubmissions > 0) {
            failures.add(String.format(
                    "%d synchronous SQL submission(s) were still running, the SQL Client prints no JobID for them "
                            + "so they cannot be cancelled through the flink CLI",
                    pendingSynchronousSubmissions));
        }
        if (CollectionUtils.isNotEmpty(failures)) {
            throw new TaskException(String.format("Cancel the flink jobs of the task failed, %s, taskInstanceId: %s",
                    String.join("; ", failures), taskExecutionContext.getTaskInstanceId()));
        }
        log.info("Successfully cancelled flink jobs: {}", jobIds);
    }

    /**
     * The number of synchronous submissions which the SQL Client started but did not finish.
     *
     * <p>With {@code table.dml-sync} the client waits for the job instead of printing its JobID, so a
     * submission which is still running when the client is stopped has no id in the task log and
     * cannot be addressed by the flink CLI. The messages which the client prints around such a
     * submission tell it apart from one which has finished.
     */
    protected int countPendingSynchronousSubmissions() {
        String logPath = taskExecutionContext.getLogPath();
        File logFile = StringUtils.isEmpty(logPath) ? null : new File(logPath);
        if (logFile == null || !logFile.isFile()) {
            return 0;
        }
        int started = 0;
        int finished = 0;
        try (Stream<String> lines = Files.lines(logFile.toPath())) {
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (StringUtils.contains(line, SYNCHRONOUS_SUBMISSION_STARTED_MESSAGE)) {
                    started++;
                }
                if (StringUtils.contains(line, SYNCHRONOUS_SUBMISSION_FINISHED_MESSAGE)) {
                    finished++;
                }
            }
        } catch (Exception e) {
            log.error("Read task log failed, logPath: {}", logPath, e);
            return 0;
        }
        return Math.max(0, started - finished);
    }

    /**
     * The flink CLI reports a job which is already in a terminal state instead of cancelling it. See
     * the {@code JobCancellationHandler} of flink.
     */
    private boolean isJobAlreadyInTerminalState(String output) {
        return StringUtils.contains(output, JOB_ALREADY_IN_TERMINAL_STATE_MESSAGE);
    }

    /**
     * The YARN application id of the log line, it identifies the cluster which runs a job and is not
     * the Flink JobID.
     */
    protected String findYarnApplicationId(String line) {
        Matcher matcher = YARN_APPLICATION_ID_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * The jobs the task submitted, in the order they appear in the task log. Each job keeps its Flink
     * JobID and the YARN application which runs it, because the SQL Client with {@code yarn-per-job}
     * submits every job into its own application, so a savepoint has to target the application of
     * its own job.
     */
    protected List<FlinkJobSubmission> getFlinkJobSubmissions() {
        String logPath = taskExecutionContext.getLogPath();
        File logFile = StringUtils.isEmpty(logPath) ? null : new File(logPath);
        if (logFile == null || !logFile.isFile()) {
            return Collections.emptyList();
        }
        List<FlinkJobSubmission> submissions = new ArrayList<>();
        List<String> applicationIds = new ArrayList<>();
        Set<String> jobIds = new HashSet<>();
        try (Stream<String> lines = Files.lines(logFile.toPath())) {
            Iterator<String> iterator = lines.iterator();
            String yarnApplicationId = null;
            while (iterator.hasNext()) {
                String line = iterator.next();
                String applicationId = findYarnApplicationId(line);
                if (applicationId != null) {
                    // the client prints the application before it submits the job into it
                    yarnApplicationId = applicationId;
                    if (!applicationIds.contains(applicationId)) {
                        applicationIds.add(applicationId);
                    }
                }
                String jobId = findFlinkJobId(line);
                if (jobId != null && jobIds.add(jobId)) {
                    submissions.add(new FlinkJobSubmission(jobId, yarnApplicationId));
                }
            }
        } catch (Exception e) {
            log.error("Read task log failed, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
        associateApplications(submissions, applicationIds);
        return submissions;
    }

    /**
     * Associate the jobs whose application id is not in the task log with the application of the
     * task.
     *
     * <p>The application id is printed in the task log by a submission which targets YARN directly.
     * When it is missing there, for example because the task log only contains the JobIDs, the
     * application-id collection source of the task is used as well, which also covers
     * {@code appId.collect=aop} and its {@code appInfo.log}.
     *
     * <p>That source cannot tell which job of a multi-job task belongs to which application, so it is
     * only used when it names a single application. A task with several applications which cannot be
     * associated is reported explicitly instead of targeting a guessed cluster.
     */
    private void associateApplications(List<FlinkJobSubmission> submissions, List<String> logApplicationIds) {
        boolean applicationIdMissing =
                submissions.stream().anyMatch(submission -> submission.getYarnApplicationId() == null);
        if (!applicationIdMissing) {
            return;
        }
        List<String> applicationIds = new ArrayList<>(logApplicationIds);
        try {
            List<String> collectedApplicationIds = getApplicationIds();
            if (CollectionUtils.isNotEmpty(collectedApplicationIds)) {
                for (String applicationId : collectedApplicationIds) {
                    if (!applicationIds.contains(applicationId)) {
                        applicationIds.add(applicationId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Get application ids failed, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        if (applicationIds.size() == 1) {
            // a task which belongs to a single application: every job of the log runs in it
            String onlyApplicationId = applicationIds.get(0);
            submissions.replaceAll(submission -> submission.getYarnApplicationId() == null
                    ? new FlinkJobSubmission(submission.getJobId(), onlyApplicationId)
                    : submission);
            return;
        }
        if (applicationIds.size() > 1) {
            log.error("Cannot associate the jobs {} with the applications {} of taskInstanceId: {}, "
                    + "they are not triggered with a -yid",
                    submissions.stream()
                            .map(FlinkJobSubmission::getJobId)
                            .collect(Collectors.toList()),
                    applicationIds, taskExecutionContext.getTaskInstanceId());
            submissions.replaceAll(submission -> submission.getYarnApplicationId() == null
                    ? new FlinkJobSubmission(submission.getJobId(), null, true)
                    : submission);
        }
    }

    /**
     * The Flink JobIDs of the submitted jobs, in the order they appear in the task log. It is the id
     * which `flink cancel` accepts, it is not the YARN/K8s application id.
     */
    protected List<String> getFlinkJobIds() {
        return getFlinkJobSubmissions().stream()
                .map(FlinkJobSubmission::getJobId)
                .collect(Collectors.toList());
    }

    /**
     * The cluster connection options which are effective for each job the SQL scripts submit, in the
     * order the jobs are submitted. A script can submit a job to one cluster, change the connection
     * and submit the next job to another cluster, so the options have to be kept per job, otherwise
     * the first job would be cancelled against the cluster of the second one.
     */
    protected List<List<String>> getClusterConnectionOptionsPerJob() {
        AbstractParameters parameters = getParameters();
        if (!(parameters instanceof FlinkParameters)) {
            return Collections.emptyList();
        }
        FlinkParameters flinkParameters = (FlinkParameters) parameters;
        return FlinkArgsUtils.extractClusterConnectionOptionsPerJob(flinkParameters.getOthers(),
                flinkParameters.getInitScript(), flinkParameters.getRawScript());
    }

    /**
     * The cluster connection options of the submission, for example {@code -m <jobmanager>} or
     * {@code -t <target>}. They have to be repeated on the cancel / savepoint command, otherwise
     * that command targets the default cluster of the worker instead of the cluster the job was
     * submitted to.
     *
     * <p>This is the last configuration of the submission, it is used for a job which
     * {@link #getClusterConnectionOptionsPerJob()} does not describe.
     */
    protected List<String> getClusterConnectionOptions() {
        AbstractParameters parameters = getParameters();
        if (!(parameters instanceof FlinkParameters)) {
            return Collections.emptyList();
        }
        FlinkParameters flinkParameters = (FlinkParameters) parameters;
        List<String> options = FlinkArgsUtils.extractClusterConnectionOptions(flinkParameters.getOthers());
        // a SQL task can also configure its connection with SET statements in its scripts
        List<String> sqlOptions = FlinkArgsUtils.extractClusterConnectionOptionsFromSql(
                flinkParameters.getInitScript(), flinkParameters.getRawScript());
        return FlinkArgsUtils.mergeClusterConnectionOptions(options, sqlOptions);
    }

    /**
     * Execute a cancel command and keep its output, so that a job which is already in a terminal state
     * can be told apart from a real cancellation failure. It is kept as a separate method so that it
     * can be verified in tests.
     */
    protected FlinkArgsUtils.FlinkCommandResult executeFlinkCancelCommand(List<String> args) {
        return FlinkArgsUtils.executeCommandWithOutput(taskExecutionContext, args,
                FlinkConstants.FLINK_COMMAND_TIMEOUT_SECONDS);
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

    /**
     * A job submitted by the task: its Flink JobID, which identifies the job, and the YARN
     * application which runs it, which is only used to target the cluster. The application is null
     * when the job was not submitted to YARN, or when the task runs in several applications and the
     * log does not tell which one runs this job.
     */
    @Getter
    protected static class FlinkJobSubmission {

        private final String jobId;

        private final String yarnApplicationId;

        /**
         * true when the task runs in several YARN applications and this job could not be associated
         * with the application which runs it.
         */
        private final boolean yarnApplicationUnresolved;

        FlinkJobSubmission(String jobId, String yarnApplicationId) {
            this(jobId, yarnApplicationId, false);
        }

        FlinkJobSubmission(String jobId, String yarnApplicationId, boolean yarnApplicationUnresolved) {
            this.jobId = jobId;
            this.yarnApplicationId = yarnApplicationId;
            this.yarnApplicationUnresolved = yarnApplicationUnresolved;
        }
    }
}
