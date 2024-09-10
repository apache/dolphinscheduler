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
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.utils.EnvironmentUtils;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.extract.master.command.ScheduleWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowScheduleTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowScheduleTriggerResponse;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class WorkflowScheduleTrigger
        extends
            AbstractWorkflowTrigger<WorkflowScheduleTriggerRequest, WorkflowScheduleTriggerResponse> {

    @Override
    protected WorkflowInstance constructWorkflowInstance(WorkflowScheduleTriggerRequest scheduleTriggerRequest) {
        final CommandType commandType = CommandType.SCHEDULER;
        final Long workflowCode = scheduleTriggerRequest.getWorkflowCode();
        final Integer workflowVersion = scheduleTriggerRequest.getWorkflowVersion();
        final WorkflowDefinition workflowDefinition = getProcessDefinition(workflowCode, workflowVersion);

        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        workflowInstance.setCommandType(commandType);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUBMITTED_SUCCESS, commandType.name());
        workflowInstance.setRecovery(Flag.NO);
        workflowInstance.setScheduleTime(scheduleTriggerRequest.getScheduleTIme());
        workflowInstance.setStartTime(new Date());
        workflowInstance.setRestartTime(workflowInstance.getStartTime());
        workflowInstance.setRunTimes(1);
        workflowInstance.setName(String.join("-", workflowDefinition.getName(), DateUtils.getCurrentTimeStamp()));
        workflowInstance.setTaskDependType(scheduleTriggerRequest.getTaskDependType());
        workflowInstance.setFailureStrategy(scheduleTriggerRequest.getFailureStrategy());
        workflowInstance
                .setWarningType(ObjectUtils.defaultIfNull(scheduleTriggerRequest.getWarningType(), WarningType.NONE));
        workflowInstance.setWarningGroupId(scheduleTriggerRequest.getWarningGroupId());
        workflowInstance.setExecutorId(scheduleTriggerRequest.getUserId());
        workflowInstance.setExecutorName(getExecutorUser(scheduleTriggerRequest.getUserId()).getUserName());
        workflowInstance.setTenantCode(scheduleTriggerRequest.getTenantCode());
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.addHistoryCmd(commandType);
        workflowInstance.setWorkflowInstancePriority(scheduleTriggerRequest.getWorkflowInstancePriority());
        workflowInstance
                .setWorkerGroup(WorkerGroupUtils.getWorkerGroupOrDefault(scheduleTriggerRequest.getWorkerGroup()));
        workflowInstance.setEnvironmentCode(
                EnvironmentUtils.getEnvironmentCodeOrDefault(scheduleTriggerRequest.getEnvironmentCode()));
        workflowInstance.setTimeout(workflowDefinition.getTimeout());
        workflowInstance.setDryRun(scheduleTriggerRequest.getDryRun().getCode());
        workflowInstance.setTestFlag(scheduleTriggerRequest.getTestFlag().getCode());
        return workflowInstance;
    }

    @Override
    protected Command constructTriggerCommand(final WorkflowScheduleTriggerRequest scheduleTriggerRequest,
                                              final WorkflowInstance workflowInstance) {
        final ScheduleWorkflowCommandParam scheduleWorkflowCommandParam = ScheduleWorkflowCommandParam.builder()
                .timeZone(scheduleTriggerRequest.getTimezoneId())
                .build();
        return Command.builder()
                .commandType(CommandType.SCHEDULER)
                .workflowDefinitionCode(scheduleTriggerRequest.getWorkflowCode())
                .workflowDefinitionVersion(scheduleTriggerRequest.getWorkflowVersion())
                .workflowInstanceId(workflowInstance.getId())
                .workflowInstancePriority(workflowInstance.getWorkflowInstancePriority())
                .commandParam(JSONUtils.toJsonString(scheduleWorkflowCommandParam))
                .build();
    }

    @Override
    protected WorkflowScheduleTriggerResponse onTriggerSuccess(WorkflowInstance workflowInstance) {
        return WorkflowScheduleTriggerResponse.success(workflowInstance.getId());
    }
}
