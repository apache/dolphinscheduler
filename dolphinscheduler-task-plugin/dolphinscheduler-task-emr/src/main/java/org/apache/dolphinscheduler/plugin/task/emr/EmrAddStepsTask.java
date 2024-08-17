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
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.CancelStepsInfo;
import com.amazonaws.services.elasticmapreduce.model.CancelStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.CancelStepsRequestStatus;
import com.amazonaws.services.elasticmapreduce.model.CancelStepsResult;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepResult;
import com.amazonaws.services.elasticmapreduce.model.StepState;
import com.amazonaws.services.elasticmapreduce.model.StepStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;

/**
 * AddJobFlowSteps task executor
 *
 * @since v3.1.0
 */
@Slf4j
public class EmrAddStepsTask extends AbstractEmrTask {

    private String stepId;

    private final HashSet<String> waitingStateSet = Sets.newHashSet(
            StepState.PENDING.toString(),
            StepState.CANCEL_PENDING.toString(),
            StepState.RUNNING.toString());

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected EmrAddStepsTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void submitApplication() throws TaskException {
        StepStatus stepStatus = null;
        try {
            AddJobFlowStepsRequest addJobFlowStepsRequest = createAddJobFlowStepsRequest();

            // submit addJobFlowStepsRequest to aws
            AddJobFlowStepsResult result = emrClient.addJobFlowSteps(addJobFlowStepsRequest);

            clusterId = addJobFlowStepsRequest.getJobFlowId();
            stepId = result.getStepIds().get(0);
            // use clusterId-stepId as appIds
            setAppIds(clusterId + TaskConstants.SUBTRACT_STRING + stepId);

            stepStatus = getStepStatus();

        } catch (EmrTaskException | SdkBaseException e) {
            log.error("emr task submit failed with error", e);
            throw new TaskException("emr task submit fail", e);
        } finally {
            final int exitStatusCode = calculateExitStatusCode(stepStatus);
            setExitStatusCode(exitStatusCode);
            log.info("emr task finished with step status : {}", stepStatus);
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        StepStatus stepStatus = getStepStatus();

        try {
            while (waitingStateSet.contains(stepStatus.getState())) {
                TimeUnit.SECONDS.sleep(10);
                stepStatus = getStepStatus();
            }
        } catch (EmrTaskException | SdkBaseException e) {
            log.error("emr task failed with error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("Execute emr task failed", e);
        } finally {
            final int exitStatusCode = calculateExitStatusCode(stepStatus);
            setExitStatusCode(exitStatusCode);
            log.info("emr task finished with step status : {}", stepStatus);
        }
    }

    /**
     * parse json string to AddJobFlowStepsRequest
     *
     * @return AddJobFlowStepsRequest
     */
    protected AddJobFlowStepsRequest createAddJobFlowStepsRequest() {

        final AddJobFlowStepsRequest addJobFlowStepsRequest;
        String jobStepDefineJson = null;
        try {
            jobStepDefineJson = ParameterUtils.convertParameterPlaceholders(
                    emrParameters.getStepsDefineJson(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
            addJobFlowStepsRequest =
                    objectMapper.readValue(jobStepDefineJson, AddJobFlowStepsRequest.class);
        } catch (JsonProcessingException e) {
            throw new EmrTaskException("can not parse AddJobFlowStepsRequest from json: " + jobStepDefineJson, e);
        }

        // When a single task definition is associated with multiple steps, the state tracking will have high
        // complexity.
        // Therefore, A task definition only supports the association of a single step, which can better ensure the
        // reliability of the task state.
        if (addJobFlowStepsRequest.getSteps().size() > 1) {
            throw new EmrTaskException("ds emr addJobFlowStepsTask only support one step");
        }

        return addJobFlowStepsRequest;
    }

    /**
     * calculate task exitStatusCode
     *
     * @param stepStatus aws emr execution status details of the cluster step.
     * @return exitStatusCode
     */
    private int calculateExitStatusCode(StepStatus stepStatus) {
        if (stepStatus == null) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            String state = stepStatus.getState();
            StepState stepState = StepState.valueOf(state);
            switch (stepState) {
                case COMPLETED:
                    return TaskConstants.EXIT_CODE_SUCCESS;
                case CANCELLED:
                    return TaskConstants.EXIT_CODE_KILL;
                default:
                    return TaskConstants.EXIT_CODE_FAILURE;
            }
        }

    }

    private StepStatus getStepStatus() {
        DescribeStepRequest describeStepRequest = new DescribeStepRequest().withClusterId(clusterId).withStepId(stepId);
        DescribeStepResult result = emrClient.describeStep(describeStepRequest);
        if (result == null) {
            throw new EmrTaskException("fetch step status failed");
        }
        StepStatus stepStatus = result.getStep().getStatus();
        log.info("emr step [clusterId:{}, stepId:{}] running with status:{}", clusterId, stepId, stepStatus);
        return stepStatus;

    }

    @Override
    public void cancelApplication() throws TaskException {
        log.info("trying cancel emr step, taskId:{}, clusterId:{}, stepId:{}",
                this.taskExecutionContext.getTaskInstanceId(), clusterId, stepId);
        CancelStepsRequest cancelStepsRequest = new CancelStepsRequest().withClusterId(clusterId).withStepIds(stepId);
        CancelStepsResult cancelStepsResult = emrClient.cancelSteps(cancelStepsRequest);

        if (cancelStepsResult == null) {
            throw new EmrTaskException("cancel emr step failed");
        }

        CancelStepsInfo cancelEmrStepInfo = cancelStepsResult.getCancelStepsInfoList()
                .stream()
                .filter(cancelStepsInfo -> cancelStepsInfo.getStepId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new EmrTaskException("cancel emr step failed"));

        if (CancelStepsRequestStatus.FAILED.toString().equals(cancelEmrStepInfo.getStatus())) {
            throw new EmrTaskException("cancel emr step failed, message:" + cancelEmrStepInfo.getReason());
        }

        log.info("the result of cancel emr step is:{}", cancelStepsResult);
    }

}
