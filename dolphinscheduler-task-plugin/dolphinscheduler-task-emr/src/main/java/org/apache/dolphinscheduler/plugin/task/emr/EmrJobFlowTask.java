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

package org.apache.dolphinscheduler.plugin.task.emr;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.SdkBaseException;
import com.amazonaws.services.elasticmapreduce.model.ClusterState;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReason;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReasonCode;
import com.amazonaws.services.elasticmapreduce.model.ClusterStatus;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;

@Slf4j
public class EmrJobFlowTask extends AbstractEmrTask {

    private final HashSet<String> waitingStateSet = Sets.newHashSet(
            ClusterState.STARTING.toString(),
            ClusterState.BOOTSTRAPPING.toString(),
            ClusterState.RUNNING.toString());

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected EmrJobFlowTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void submitApplication() throws TaskException {
        ClusterStatus clusterStatus = null;
        try {
            RunJobFlowRequest runJobFlowRequest = createRunJobFlowRequest();

            // submit runJobFlowRequest to aws
            RunJobFlowResult result = emrClient.runJobFlow(runJobFlowRequest);

            clusterId = result.getJobFlowId();
            // Failover on EMR Task type has not been implemented. In this time, DS only supports failover on yarn task
            // type . Other task type, such as EMR task, k8s task not ready yet.
            setAppIds(clusterId);

            clusterStatus = getClusterStatus();

        } catch (EmrTaskException | SdkBaseException e) {
            log.error("emr task submit failed with error", e);
            throw new TaskException("emr task submit failed", e);
        } finally {
            final int exitStatusCode = calculateExitStatusCode(clusterStatus);
            setExitStatusCode(exitStatusCode);
            log.info("emr task finished with cluster status : {}", clusterStatus);
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        ClusterStatus clusterStatus = null;
        try {
            clusterStatus = getClusterStatus();

            while (waitingStateSet.contains(clusterStatus.getState())) {
                TimeUnit.SECONDS.sleep(10);
                clusterStatus = getClusterStatus();
            }
        } catch (EmrTaskException | SdkBaseException e) {
            log.error("emr task failed with error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("Execute emr task failed", e);
        } finally {
            final int exitStatusCode = calculateExitStatusCode(clusterStatus);
            setExitStatusCode(exitStatusCode);
            log.info("emr task finished with cluster status : {}", clusterStatus);
        }
    }

    /**
     * parse json string to RunJobFlowRequest
     *
     * @return RunJobFlowRequest
     */
    protected RunJobFlowRequest createRunJobFlowRequest() {

        final RunJobFlowRequest runJobFlowRequest;
        String jobFlowDefineJson = null;
        try {
            jobFlowDefineJson = ParameterUtils.convertParameterPlaceholders(
                    emrParameters.getJobFlowDefineJson(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
            runJobFlowRequest = objectMapper.readValue(jobFlowDefineJson, RunJobFlowRequest.class);
        } catch (JsonProcessingException e) {
            throw new EmrTaskException("can not parse RunJobFlowRequest from json: " + jobFlowDefineJson, e);
        }

        return runJobFlowRequest;
    }

    /**
     * calculate task exitStatusCode
     *
     * @param clusterStatus aws emr cluster status
     * @return exitStatusCode
     */
    private int calculateExitStatusCode(ClusterStatus clusterStatus) {
        if (clusterStatus == null) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            String state = clusterStatus.getState();
            ClusterStateChangeReason stateChangeReason = clusterStatus.getStateChangeReason();
            ClusterState clusterState = ClusterState.valueOf(state);
            switch (clusterState) {
                case WAITING:
                    return TaskConstants.EXIT_CODE_SUCCESS;
                case TERMINATED:
                case TERMINATING:
                    String code = stateChangeReason.getCode();
                    if (code != null
                            && code.equalsIgnoreCase(ClusterStateChangeReasonCode.ALL_STEPS_COMPLETED.toString())) {
                        return TaskConstants.EXIT_CODE_SUCCESS;
                    } else {
                        return TaskConstants.EXIT_CODE_KILL;
                    }
                default:
                    return TaskConstants.EXIT_CODE_FAILURE;
            }
        }

    }

    private ClusterStatus getClusterStatus() {
        DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest().withClusterId(clusterId);
        DescribeClusterResult result = emrClient.describeCluster(describeClusterRequest);
        if (result == null) {
            throw new EmrTaskException("fetch cluster status failed");
        }
        ClusterStatus clusterStatus = result.getCluster().getStatus();
        log.info("emr cluster [clusterId:{}] running with status:{}", clusterId, clusterStatus);
        return clusterStatus;

    }

    @Override
    public void cancelApplication() throws TaskException {
        log.info("trying terminate job flow, taskId:{}, clusterId:{}", this.taskExecutionContext.getTaskInstanceId(),
                clusterId);
        TerminateJobFlowsRequest terminateJobFlowsRequest = new TerminateJobFlowsRequest().withJobFlowIds(clusterId);
        TerminateJobFlowsResult terminateJobFlowsResult = emrClient.terminateJobFlows(terminateJobFlowsRequest);
        log.info("the result of terminate job flow is:{}", terminateJobFlowsResult);
    }

}
