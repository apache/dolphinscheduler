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

package org.apache.dolphinscheduler.server.master.engine.workflow.listener;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowBackfillTrigger;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowSuccessLifecycleListener implements IWorkflowLifecycleListener {

    @Autowired
    private WorkflowBackfillTrigger workflowBackfillTrigger;

    @Autowired
    private CommandDao commandDao;

    public void notifyWorkflowLifecycleEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                             final AbstractWorkflowLifecycleLifecycleEvent lifecycleEvent) {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        if (Flag.YES == workflowInstance.getIsSubWorkflow()) {
            // The sub workflow does not need to generate the backfill command
            // Since the parent workflow will trigger the task to generate the sub workflow instance.
            return;
        }

        final ICommandParam commandParam =
                JSONUtils.parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        if (commandParam == null) {
            log.warn("Command param: {} is invalid for workflow: {}", workflowInstance.getCommandParam(),
                    workflowInstance.getName());
            return;
        }
        if (commandParam.getCommandType() != CommandType.COMPLEMENT_DATA) {
            return;
        }
        generateNextBackfillCommand((BackfillWorkflowCommandParam) commandParam, workflowInstance);
    }

    private void generateNextBackfillCommand(final BackfillWorkflowCommandParam commandParam,
                                             final WorkflowInstance workflowInstance) {
        // Generate next backfill command
        final List<String> backfillTimeList = commandParam.getBackfillTimeList();
        backfillTimeList.remove(DateUtils.dateToString(workflowInstance.getScheduleTime()));
        if (CollectionUtils.isEmpty(backfillTimeList)) {
            return;
        }
        final WorkflowBackfillTriggerRequest backfillTriggerRequest = WorkflowBackfillTriggerRequest.builder()
                .userId(workflowInstance.getExecutorId())
                .backfillTimeList(backfillTimeList)
                .workflowCode(workflowInstance.getWorkflowDefinitionCode())
                .workflowVersion(workflowInstance.getWorkflowDefinitionVersion())
                .startNodes(commandParam.getStartNodes())
                .failureStrategy(workflowInstance.getFailureStrategy())
                .taskDependType(workflowInstance.getTaskDependType())
                .warningType(workflowInstance.getWarningType())
                .warningGroupId(workflowInstance.getWarningGroupId())
                .workflowInstancePriority(workflowInstance.getWorkflowInstancePriority())
                .workerGroup(workflowInstance.getWorkerGroup())
                .tenantCode(workflowInstance.getTenantCode())
                .environmentCode(workflowInstance.getEnvironmentCode())
                .startParamList(commandParam.getCommandParams())
                .dryRun(Flag.of(workflowInstance.getDryRun()))
                .testFlag(Flag.of(workflowInstance.getTestFlag()))
                .build();
        final WorkflowBackfillTriggerResponse backfillTriggerResponse =
                workflowBackfillTrigger.triggerWorkflow(backfillTriggerRequest);
        if (!backfillTriggerResponse.isSuccess()) {
            log.warn("Backfill workflow failed: {}", backfillTriggerResponse.getMessage());
        }
    }

    @Override
    public boolean match(AbstractWorkflowLifecycleLifecycleEvent event) {
        return event.getEventType() == WorkflowLifecycleEventType.SUCCEED;
    }

}
