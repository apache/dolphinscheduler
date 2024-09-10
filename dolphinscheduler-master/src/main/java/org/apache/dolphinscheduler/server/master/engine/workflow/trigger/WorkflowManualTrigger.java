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
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerResponse;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Manual trigger of the workflow, used to trigger the workflow and generate the workflow instance in the manual way.
 */
@Component
public class WorkflowManualTrigger
        extends
            AbstractWorkflowTrigger<WorkflowManualTriggerRequest, WorkflowManualTriggerResponse> {

    @Override
    protected WorkflowInstance constructWorkflowInstance(final WorkflowManualTriggerRequest workflowManualTriggerRequest) {
        final CommandType commandType = CommandType.START_PROCESS;
        final Long workflowCode = workflowManualTriggerRequest.getWorkflowDefinitionCode();
        final Integer workflowVersion = workflowManualTriggerRequest.getWorkflowDefinitionVersion();
        final WorkflowDefinition workflowDefinition = getProcessDefinition(workflowCode, workflowVersion);

        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        workflowInstance.setCommandType(commandType);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUBMITTED_SUCCESS, commandType.name());
        workflowInstance.setRecovery(Flag.NO);
        workflowInstance.setStartTime(new Date());
        workflowInstance.setRestartTime(workflowInstance.getStartTime());
        workflowInstance.setRunTimes(1);
        workflowInstance.setName(String.join("-", workflowDefinition.getName(), DateUtils.getCurrentTimeStamp()));
        workflowInstance.setTaskDependType(workflowManualTriggerRequest.getTaskDependType());
        workflowInstance.setFailureStrategy(workflowManualTriggerRequest.getFailureStrategy());
        workflowInstance.setWarningType(
                ObjectUtils.defaultIfNull(workflowManualTriggerRequest.getWarningType(), WarningType.NONE));
        workflowInstance.setWarningGroupId(workflowManualTriggerRequest.getWarningGroupId());
        workflowInstance.setExecutorId(workflowManualTriggerRequest.getUserId());
        workflowInstance.setExecutorName(getExecutorUser(workflowManualTriggerRequest.getUserId()).getUserName());
        workflowInstance.setTenantCode(workflowManualTriggerRequest.getTenantCode());
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.addHistoryCmd(commandType);
        workflowInstance.setWorkflowInstancePriority(workflowManualTriggerRequest.getWorkflowInstancePriority());
        workflowInstance.setWorkerGroup(
                WorkerGroupUtils.getWorkerGroupOrDefault(workflowManualTriggerRequest.getWorkerGroup()));
        workflowInstance.setEnvironmentCode(
                EnvironmentUtils.getEnvironmentCodeOrDefault(workflowManualTriggerRequest.getEnvironmentCode()));
        workflowInstance.setTimeout(workflowDefinition.getTimeout());
        workflowInstance.setDryRun(workflowManualTriggerRequest.getDryRun().getCode());
        workflowInstance.setTestFlag(workflowManualTriggerRequest.getTestFlag().getCode());
        return workflowInstance;
    }

    @Override
    protected Command constructTriggerCommand(final WorkflowManualTriggerRequest workflowManualTriggerRequest,
                                              final WorkflowInstance workflowInstance) {
        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .commandParams(workflowManualTriggerRequest.getStartParamList())
                .startNodes(workflowManualTriggerRequest.getStartNodes())
                .timeZone(DateUtils.getTimezone())
                .build();
        return Command.builder()
                .commandType(CommandType.START_PROCESS)
                .workflowDefinitionCode(workflowManualTriggerRequest.getWorkflowDefinitionCode())
                .workflowDefinitionVersion(workflowManualTriggerRequest.getWorkflowDefinitionVersion())
                .workflowInstanceId(workflowInstance.getId())
                .workflowInstancePriority(workflowInstance.getWorkflowInstancePriority())
                .commandParam(JSONUtils.toJsonString(runWorkflowCommandParam))
                .build();
    }

    @Override
    protected WorkflowManualTriggerResponse onTriggerSuccess(WorkflowInstance workflowInstance) {
        return WorkflowManualTriggerResponse.success(workflowInstance.getId());
    }

}
