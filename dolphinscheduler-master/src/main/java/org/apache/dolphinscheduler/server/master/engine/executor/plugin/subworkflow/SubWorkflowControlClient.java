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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverSuspendTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverSuspendTasksResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow.trigger.SubWorkflowManualTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowInstanceRecoverFailureTaskTrigger;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowInstanceRecoverSuspendTaskTrigger;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubWorkflowControlClient {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private SubWorkflowManualTrigger subWorkflowManualTrigger;

    @Autowired
    private WorkflowInstanceRecoverFailureTaskTrigger workflowInstanceRecoverFailureTaskTrigger;

    @Autowired
    private WorkflowInstanceRecoverSuspendTaskTrigger workflowInstanceRecoverSuspendTaskTrigger;

    public Integer triggerSubWorkflow(final WorkflowManualTriggerRequest workflowManualTriggerRequest) {
        return subWorkflowManualTrigger.triggerWorkflow(workflowManualTriggerRequest).getWorkflowInstanceId();
    }

    public WorkflowInstanceRecoverFailureTasksResponse triggerFromFailureTasks(
                                                                               final WorkflowInstanceRecoverFailureTasksRequest recoverFailureTasksRequest) {
        return workflowInstanceRecoverFailureTaskTrigger.triggerWorkflow(recoverFailureTasksRequest);
    }

    public WorkflowInstanceRecoverSuspendTasksResponse triggerFromSuspendTasks(
                                                                               final WorkflowInstanceRecoverSuspendTasksRequest recoverSuspendTasksRequest) {
        return workflowInstanceRecoverSuspendTaskTrigger.triggerWorkflow(recoverSuspendTasksRequest);
    }

    public WorkflowInstancePauseResponse pauseWorkflowInstance(
                                                               final WorkflowInstancePauseRequest workflowInstancePauseRequest) throws MasterTaskExecuteException {
        final Integer subWorkflowInstanceId = workflowInstancePauseRequest.getWorkflowInstanceId();
        final WorkflowInstance subWorkflowInstance = workflowInstanceDao.queryById(subWorkflowInstanceId);
        if (subWorkflowInstance.getState() != WorkflowExecutionStatus.RUNNING_EXECUTION) {
            return WorkflowInstancePauseResponse.fail("SubWorkflow instance is not running, cannot pause");
        }
        try {
            return Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(subWorkflowInstance.getHost())
                    .pauseWorkflowInstance(new WorkflowInstancePauseRequest(subWorkflowInstanceId));
        } catch (Exception e) {
            throw new MasterTaskExecuteException("Pause SubWorkflow: " + subWorkflowInstance.getName() + " failed", e);
        }
    }

    public WorkflowInstanceStopResponse stopWorkflowInstance(
                                                             final WorkflowInstanceStopRequest workflowInstanceStopRequest) throws MasterTaskExecuteException {
        final Integer subWorkflowInstanceId = workflowInstanceStopRequest.getWorkflowInstanceId();
        final WorkflowInstance subWorkflowInstance = workflowInstanceDao.queryById(subWorkflowInstanceId);
        if (subWorkflowInstance.getState() != WorkflowExecutionStatus.RUNNING_EXECUTION) {
            return WorkflowInstanceStopResponse.fail("SubWorkflow instance is not running, cannot stop");
        }
        try {
            return Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(subWorkflowInstance.getHost())
                    .stopWorkflowInstance(new WorkflowInstanceStopRequest(subWorkflowInstance.getId()));
        } catch (Exception e) {
            throw new MasterTaskExecuteException("Kill SubWorkflow: " + subWorkflowInstance.getName() + " failed", e);
        }
    }
}
