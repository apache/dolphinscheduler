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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.stream.StreamTask;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkStreamTask extends FlinkTask implements StreamTask {

    private FlinkStreamParameters flinkParameters;

    private final TaskExecutionContext taskExecutionContext;

    public FlinkStreamTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        flinkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkStreamParameters.class);
        log.info("Initialize Flink task params {}", JSONUtils.toPrettyJsonString(flinkParameters));

        if (flinkParameters == null || !flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }
    }

    @Override
    protected String getScript() {
        return buildScriptWithParameterReplacement(flinkParameters);
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }

    /**
     * Trigger the savepoint of every job the task submitted.
     *
     * <p>A job which cannot be checkpointed must not prevent the savepoint of the other jobs: the
     * savepoint is attempted for every collected JobID and the failures are reported afterwards, the
     * same way the cancellation of the task works. A second call can therefore checkpoint a job which
     * was skipped because an earlier job failed.
     *
     * <p>The snapshot is best effort: the SQL Client is still running while the savepoint is
     * triggered, so the task log may not contain the JobID of a submission which was started right
     * before the request. A savepoint does not stop the client, so it cannot wait for the output the
     * way the cancellation does.
     */
    @Override
    public void savePoint() throws Exception {
        // `flink savepoint` takes the Flink JobID. The YARN/K8s application id is a different
        // identifier: it is not the savepoint target, it only targets the cluster which runs the
        // job, and is passed as the `-yid` option. It is kept per job, because the SQL Client with
        // `yarn-per-job` submits every job into its own application.
        List<FlinkJobSubmission> submissions = getFlinkJobSubmissions();
        List<String> failures = new ArrayList<>();
        // a submission which reported neither a JobID nor a completion can still be running, and the
        // client prints no id for a synchronous submission, so no savepoint can be triggered for it
        int unreportedSubmissions = countUnreportedSubmissions();
        if (unreportedSubmissions > 0) {
            failures.add(String.format(
                    "%d submitted job(s) reported no result from the SQL client, they may still be running so no "
                            + "savepoint can be triggered for them",
                    unreportedSubmissions));
        }
        if (CollectionUtils.isEmpty(submissions)) {
            failures.add("Cannot find the flink JobID of the task");
        } else {
            List<List<String>> clusterConnectionOptionsPerJob = getClusterConnectionOptionsPerJob();
            if (!clusterConnectionOptionsPerJob.isEmpty()
                    && clusterConnectionOptionsPerJob.size() != submissions.size()) {
                log.warn("The SQL scripts describe {} job(s) but {} JobID(s) were collected, the jobs which are not "
                        + "described use the last connection options, taskInstanceId: {}",
                        clusterConnectionOptionsPerJob.size(), submissions.size(),
                        taskExecutionContext.getTaskInstanceId());
            }
            // a job which the scripts do not describe uses the last configuration of the submission
            List<String> lastClusterConnectionOptions = getClusterConnectionOptions();
            for (int i = 0; i < submissions.size(); i++) {
                FlinkJobSubmission submission = submissions.get(i);
                List<String> clusterConnectionOptions = i < clusterConnectionOptionsPerJob.size()
                        ? clusterConnectionOptionsPerJob.get(i)
                        : lastClusterConnectionOptions;
                if (submission.isYarnApplicationUnresolved() && CollectionUtils.isEmpty(clusterConnectionOptions)) {
                    // the job runs in one of several applications and nothing else selects the cluster it
                    // runs in, so a savepoint would be triggered against an arbitrary one
                    failures.add(String.format(
                            "Cannot determine the YARN application which runs flink job %s, the task submitted it "
                                    + "to one of several applications and the task log does not say which one",
                            submission.getJobId()));
                    continue;
                }
                List<String> args = FlinkArgsUtils.buildSavePointCommandLine(submission.getJobId(),
                        submission.getYarnApplicationId(), clusterConnectionOptions);

                if (!executeFlinkSavepointCommand(args)) {
                    failures.add(String.format("Trigger savepoint for flink job %s failed", submission.getJobId()));
                    continue;
                }
                log.info("Triggered the savepoint of flink job: {}", submission.getJobId());
            }
        }
        if (CollectionUtils.isNotEmpty(failures)) {
            String message = String.format("Trigger the savepoints of the task failed, %s, taskInstanceId: %s",
                    String.join("; ", failures), taskExecutionContext.getTaskInstanceId());
            // the failure is written to the task log explicitly instead of relying on the thrown
            // exception only: the task is stopped anyway, so the task log is what the user sees
            log.error(message);
            throw new TaskException(message);
        }
    }
}
