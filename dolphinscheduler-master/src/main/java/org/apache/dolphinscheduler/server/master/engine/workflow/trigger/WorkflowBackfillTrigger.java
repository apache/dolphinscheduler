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
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Backfill trigger of the workflow, used to trigger the workflow and generate the workflow instance in the backfill way.
 */
@Component
public class WorkflowBackfillTrigger
        extends
            AbstractWorkflowTrigger<WorkflowBackfillTriggerRequest, WorkflowBackfillTriggerResponse> {

    @Override
    protected WorkflowInstance constructWorkflowInstance(WorkflowBackfillTriggerRequest backfillTriggerRequest) {
        final CommandType commandType = CommandType.COMPLEMENT_DATA;
        final Long workflowCode = backfillTriggerRequest.getWorkflowCode();
        final Integer workflowVersion = backfillTriggerRequest.getWorkflowVersion();
        final List<String> backfillTimeList = backfillTriggerRequest.getBackfillTimeList();
        final WorkflowDefinition workflowDefinition = getProcessDefinition(workflowCode, workflowVersion);

        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        workflowInstance.setCommandType(commandType);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUBMITTED_SUCCESS, commandType.name());
        workflowInstance.setRecovery(Flag.NO);
        workflowInstance.setScheduleTime(DateUtils.stringToDate(backfillTimeList.get(0)));
        workflowInstance.setStartTime(new Date());
        workflowInstance.setRestartTime(workflowInstance.getStartTime());
        workflowInstance.setRunTimes(1);
        workflowInstance.setName(String.join("-", workflowDefinition.getName(), DateUtils.getCurrentTimeStamp()));
        workflowInstance.setTaskDependType(backfillTriggerRequest.getTaskDependType());
        workflowInstance.setFailureStrategy(backfillTriggerRequest.getFailureStrategy());
        workflowInstance
                .setWarningType(ObjectUtils.defaultIfNull(backfillTriggerRequest.getWarningType(), WarningType.NONE));
        workflowInstance.setWarningGroupId(backfillTriggerRequest.getWarningGroupId());
        workflowInstance.setExecutorId(backfillTriggerRequest.getUserId());
        workflowInstance.setExecutorName(getExecutorUser(backfillTriggerRequest.getUserId()).getUserName());
        workflowInstance.setTenantCode(backfillTriggerRequest.getTenantCode());
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.addHistoryCmd(commandType);
        workflowInstance.setWorkflowInstancePriority(backfillTriggerRequest.getWorkflowInstancePriority());
        workflowInstance
                .setWorkerGroup(WorkerGroupUtils.getWorkerGroupOrDefault(backfillTriggerRequest.getWorkerGroup()));
        workflowInstance.setEnvironmentCode(
                EnvironmentUtils.getEnvironmentCodeOrDefault(backfillTriggerRequest.getEnvironmentCode()));
        workflowInstance.setTimeout(workflowDefinition.getTimeout());
        workflowInstance.setDryRun(backfillTriggerRequest.getDryRun().getCode());
        workflowInstance.setTestFlag(backfillTriggerRequest.getTestFlag().getCode());
        return workflowInstance;
    }

    @Override
    protected Command constructTriggerCommand(WorkflowBackfillTriggerRequest backfillTriggerRequest,
                                              WorkflowInstance workflowInstance) {
        final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                .commandParams(backfillTriggerRequest.getStartParamList())
                .startNodes(backfillTriggerRequest.getStartNodes())
                .timeZone(DateUtils.getTimezone())
                .backfillTimeList(backfillTriggerRequest.getBackfillTimeList())
                .build();
        return Command.builder()
                .commandType(CommandType.COMPLEMENT_DATA)
                .workflowDefinitionCode(backfillTriggerRequest.getWorkflowCode())
                .workflowDefinitionVersion(backfillTriggerRequest.getWorkflowVersion())
                .workflowInstanceId(workflowInstance.getId())
                .workflowInstancePriority(workflowInstance.getWorkflowInstancePriority())
                .commandParam(JSONUtils.toJsonString(backfillWorkflowCommandParam))
                .build();
    }

    @Override
    protected WorkflowBackfillTriggerResponse onTriggerSuccess(WorkflowInstance workflowInstance) {
        return WorkflowBackfillTriggerResponse.success(workflowInstance.getId());
    }
}
