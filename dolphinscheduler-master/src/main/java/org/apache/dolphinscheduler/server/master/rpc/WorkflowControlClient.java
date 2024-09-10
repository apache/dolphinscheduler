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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverSuspendTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverSuspendTasksResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowScheduleTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowScheduleTriggerResponse;
import org.apache.dolphinscheduler.server.master.engine.WorkflowCacheRepository;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowBackfillTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowInstanceRecoverFailureTaskTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowInstanceRecoverSuspendTaskTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowInstanceRepeatTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowManualTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowScheduleTrigger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowControlClient implements IWorkflowControlClient {

    @Autowired
    private WorkflowManualTrigger workflowManualTrigger;

    @Autowired
    private WorkflowBackfillTrigger workflowBackfillTrigger;

    @Autowired
    private WorkflowScheduleTrigger workflowScheduleTrigger;

    @Autowired
    private WorkflowInstanceRepeatTrigger workflowInstanceRepeatTrigger;

    @Autowired
    private WorkflowInstanceRecoverFailureTaskTrigger workflowInstanceRecoverFailureTaskTrigger;

    @Autowired
    private WorkflowInstanceRecoverSuspendTaskTrigger workflowInstanceRecoverSuspendTaskTrigger;

    @Autowired
    private WorkflowCacheRepository workflowRepository;

    @Override
    public WorkflowManualTriggerResponse manualTriggerWorkflow(final WorkflowManualTriggerRequest manualTriggerRequest) {
        try {
            return workflowManualTrigger.triggerWorkflow(manualTriggerRequest);
        } catch (Exception ex) {
            log.error("Handle workflowTriggerRequest: {} failed", manualTriggerRequest, ex);
            return WorkflowManualTriggerResponse.fail("Trigger workflow failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowBackfillTriggerResponse backfillTriggerWorkflow(final WorkflowBackfillTriggerRequest backfillTriggerRequest) {
        try {
            return workflowBackfillTrigger.triggerWorkflow(backfillTriggerRequest);
        } catch (Exception ex) {
            log.error("Handle workflowBackfillTriggerRequest: {} failed", backfillTriggerRequest, ex);
            return WorkflowBackfillTriggerResponse.fail("Backfill workflow failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowScheduleTriggerResponse scheduleTriggerWorkflow(final WorkflowScheduleTriggerRequest workflowScheduleTriggerRequest) {
        try {
            return workflowScheduleTrigger.triggerWorkflow(workflowScheduleTriggerRequest);
        } catch (Exception ex) {
            log.error("Handle workflowScheduleTriggerRequest: {} failed", workflowScheduleTriggerRequest, ex);
            return WorkflowScheduleTriggerResponse
                    .fail("Schedule trigger workflow failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstanceRepeatRunningResponse repeatTriggerWorkflowInstance(final WorkflowInstanceRepeatRunningRequest workflowInstanceRepeatRunningRequest) {
        try {
            return workflowInstanceRepeatTrigger.triggerWorkflow(workflowInstanceRepeatRunningRequest);
        } catch (Exception ex) {
            log.error("Handle workflowInstanceRepeatRunningRequest: {} failed", workflowInstanceRepeatRunningRequest,
                    ex);
            return WorkflowInstanceRepeatRunningResponse
                    .fail("Repeat trigger workflow instance failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstanceRecoverFailureTasksResponse triggerFromFailureTasks(final WorkflowInstanceRecoverFailureTasksRequest workflowInstanceRecoverFailureTasksRequest) {
        try {
            return workflowInstanceRecoverFailureTaskTrigger
                    .triggerWorkflow(workflowInstanceRecoverFailureTasksRequest);
        } catch (Exception ex) {
            log.error("Handle workflowInstanceRecoverFailureTaskRequest: {} failed",
                    workflowInstanceRecoverFailureTasksRequest, ex);
            return WorkflowInstanceRecoverFailureTasksResponse
                    .fail("Recover failure task failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstanceRecoverSuspendTasksResponse triggerFromSuspendTasks(WorkflowInstanceRecoverSuspendTasksRequest workflowInstanceRecoverSuspendTasksRequest) {
        try {
            return workflowInstanceRecoverSuspendTaskTrigger
                    .triggerWorkflow(workflowInstanceRecoverSuspendTasksRequest);
        } catch (Exception ex) {
            log.error("Handle workflowInstanceRecoverSuspendTaskRequest: {} failed",
                    workflowInstanceRecoverSuspendTasksRequest, ex);
            return WorkflowInstanceRecoverSuspendTasksResponse
                    .fail("Recover suspend task failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstancePauseResponse pauseWorkflowInstance(final WorkflowInstancePauseRequest workflowInstancePauseRequest) {
        try {
            final Integer workflowInstanceId = workflowInstancePauseRequest.getWorkflowInstanceId();
            final IWorkflowExecutionRunnable workflow = workflowRepository.get(workflowInstanceId);
            if (workflow == null) {
                return WorkflowInstancePauseResponse.fail(
                        "Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            workflow.pause();
            return WorkflowInstancePauseResponse.success();
        } catch (Exception ex) {
            log.error("Handle workflowInstancePauseRequest: {} failed", workflowInstancePauseRequest, ex);
            return WorkflowInstancePauseResponse.fail(
                    "Pause workflow instance failed: " + ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstanceStopResponse stopWorkflowInstance(final WorkflowInstanceStopRequest workflowInstanceStopRequest) {
        try {
            final Integer workflowInstanceId = workflowInstanceStopRequest.getWorkflowInstanceId();
            final IWorkflowExecutionRunnable workflow = workflowRepository.get(workflowInstanceId);
            if (workflow == null) {
                return WorkflowInstanceStopResponse
                        .fail("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            workflow.stop();
            return WorkflowInstanceStopResponse.success();
        } catch (Exception ex) {
            log.error("Handle workflowInstanceStopRequest: {} failed", workflowInstanceStopRequest, ex);
            return WorkflowInstanceStopResponse.fail(
                    "Stop workflow instance failed:" + ExceptionUtils.getMessage(ex));
        }
    }
}
