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

package org.apache.dolphinscheduler.server.master.engine.workflow.trigger;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksResponse;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class WorkflowInstanceRecoverFailureTaskTrigger
        extends
            AbstractWorkflowInstanceTrigger<WorkflowInstanceRecoverFailureTasksRequest, WorkflowInstanceRecoverFailureTasksResponse> {

    @Override
    protected WorkflowInstance constructWorkflowInstance(final WorkflowInstanceRecoverFailureTasksRequest workflowInstanceRecoverFailureTasksRequest) {
        WorkflowInstance workflowInstance =
                getWorkflowInstance(workflowInstanceRecoverFailureTasksRequest.getWorkflowInstanceId());
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                CommandType.START_FAILURE_TASK_PROCESS.name());
        workflowInstance.setRunTimes(workflowInstance.getRunTimes() + 1);
        workflowInstance.setRestartTime(new Date());
        workflowInstance.setEndTime(null);
        return workflowInstance;
    }

    @Override
    protected Command constructTriggerCommand(final WorkflowInstanceRecoverFailureTasksRequest workflowInstanceRecoverFailureTasksRequest,
                                              final WorkflowInstance workflowInstance) {
        return Command.builder()
                .commandType(CommandType.START_FAILURE_TASK_PROCESS)
                .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                .processDefinitionVersion(workflowInstance.getProcessDefinitionVersion())
                .processInstanceId(workflowInstance.getId())
                .executorId(workflowInstanceRecoverFailureTasksRequest.getUserId())
                .startTime(new Date())
                .updateTime(new Date())
                .build();
    }

    @Override
    protected WorkflowInstanceRecoverFailureTasksResponse onTriggerSuccess(WorkflowInstance workflowInstance) {
        return WorkflowInstanceRecoverFailureTasksResponse.success();
    }
}
