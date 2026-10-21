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

    @Override
    public void savePoint() throws Exception {
        // `flink savepoint` takes the Flink JobID. The YARN/K8s application id is a different
        // identifier: it is not the savepoint target, it only targets the cluster which runs the
        // job, and is passed as the `-yid` option. It is kept per job, because the SQL Client with
        // `yarn-per-job` submits every job into its own application.
        List<FlinkJobSubmission> submissions = getFlinkJobSubmissions();
        if (CollectionUtils.isEmpty(submissions)) {
            throw new TaskException(
                    "Cannot find the flink JobID of the task, taskInstanceId: "
                            + taskExecutionContext.getTaskInstanceId());
        }
        List<List<String>> clusterConnectionOptionsPerJob = getClusterConnectionOptionsPerJob();
        // a job which the scripts do not describe uses the last configuration of the submission
        List<String> lastClusterConnectionOptions = getClusterConnectionOptions();
        for (int i = 0; i < submissions.size(); i++) {
            FlinkJobSubmission submission = submissions.get(i);
            List<String> clusterConnectionOptions = i < clusterConnectionOptionsPerJob.size()
                    ? clusterConnectionOptionsPerJob.get(i)
                    : lastClusterConnectionOptions;
            List<String> args = FlinkArgsUtils.buildSavePointCommandLine(submission.getJobId(),
                    submission.getYarnApplicationId(), clusterConnectionOptions);

            if (!executeFlinkSavepointCommand(args)) {
                throw new TaskException("Trigger savepoint for flink job " + submission.getJobId()
                        + " failed, taskInstanceId: " + taskExecutionContext.getTaskInstanceId());
            }
        }
    }
}
