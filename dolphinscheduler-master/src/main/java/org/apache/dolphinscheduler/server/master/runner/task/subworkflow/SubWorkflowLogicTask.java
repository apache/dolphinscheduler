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

package org.apache.dolphinscheduler.server.master.runner.task.subworkflow;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceMapDao;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverSuspendTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubWorkflowParameters;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class SubWorkflowLogicTask extends BaseAsyncLogicTask<SubWorkflowParameters> {

    private SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext;

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;

    private final ApplicationContext applicationContext;

    public SubWorkflowLogicTask(final TaskExecutionContext taskExecutionContext,
                                final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ApplicationContext applicationContext) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<SubWorkflowParameters>() {
                }));
        this.workflowExecutionRunnable = workflowExecutionRunnable;
        this.applicationContext = applicationContext;
        this.subWorkflowLogicTaskRuntimeContext = JSONUtils.parseObject(
                taskExecutionContext.getAppIds(),
                SubWorkflowLogicTaskRuntimeContext.class);
    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() {
        subWorkflowLogicTaskRuntimeContext = initializeSubWorkflowInstance();
        upsertSubWorkflowRelation();
        taskExecutionContext.setAppIds(JSONUtils.toJsonString(subWorkflowLogicTaskRuntimeContext));

        applicationContext
                .getBean(LogicTaskInstanceExecutionEventSenderManager.class)
                .runningEventSender()
                .sendMessage(taskExecutionContext);

        return new SubWorkflowAsyncTaskExecuteFunction(
                subWorkflowLogicTaskRuntimeContext,
                applicationContext.getBean(WorkflowInstanceDao.class));
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        if (subWorkflowLogicTaskRuntimeContext == null) {
            log.info("subWorkflowLogicTaskRuntimeContext is null cannot pause");
            return;
        }
        final Integer subWorkflowInstanceId = subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId();
        final WorkflowInstancePauseResponse pauseResponse = applicationContext
                .getBean(SubWorkflowControlClient.class)
                .pauseWorkflowInstance(new WorkflowInstancePauseRequest(subWorkflowInstanceId));
        if (pauseResponse.isSuccess()) {
            log.info("Pause sub workflowInstance: id={}", subWorkflowInstanceId + " success");
        } else {
            log.info("Pause sub workflowInstance: id={} failed with response: {}", subWorkflowInstanceId,
                    pauseResponse);
        }
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        if (subWorkflowLogicTaskRuntimeContext == null) {
            log.info("subWorkflowLogicTaskRuntimeContext is null cannot kill");
            return;
        }
        final Integer subWorkflowInstanceId = subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId();
        final WorkflowInstanceStopResponse stopResponse = applicationContext
                .getBean(SubWorkflowControlClient.class)
                .stopWorkflowInstance(new WorkflowInstanceStopRequest(subWorkflowInstanceId));
        if (stopResponse.isSuccess()) {
            log.info("Kill sub workflowInstance: id={}", subWorkflowInstanceId + " success");
        } else {
            log.info("Kill sub workflowInstance: id={} failed with response: {}", subWorkflowInstanceId, stopResponse);
        }
    }

    private SubWorkflowLogicTaskRuntimeContext initializeSubWorkflowInstance() {
        // todo: doFailover if the runtime context is not null and task is generated by failover

        if (subWorkflowLogicTaskRuntimeContext == null) {
            return triggerNewSubWorkflow();
        }

        switch (workflowExecutionRunnable.getWorkflowInstance().getCommandType()) {
            case RECOVER_SUSPENDED_PROCESS:
                return recoverFromSuspendTasks();
            case START_FAILURE_TASK_PROCESS:
                return recoverFromFailedTasks();
            default:
                return triggerNewSubWorkflow();
        }

    }

    private SubWorkflowLogicTaskRuntimeContext recoverFromFailedTasks() {
        final SubWorkflowControlClient subWorkflowControlClient =
                applicationContext.getBean(SubWorkflowControlClient.class);
        if (subWorkflowLogicTaskRuntimeContext == null) {
            log.info("The task: {} triggerType is FAILED_RECOVER but runtimeContext is null will trigger again",
                    taskExecutionContext.getTaskName());
            return triggerNewSubWorkflow();
        }
        final WorkflowInstanceRecoverFailureTasksRequest recoverFailureTasksRequest =
                WorkflowInstanceRecoverFailureTasksRequest.builder()
                        .workflowInstanceId(subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId())
                        .userId(taskExecutionContext.getExecutorId())
                        .build();
        subWorkflowControlClient.triggerFromFailureTasks(recoverFailureTasksRequest);
        return subWorkflowLogicTaskRuntimeContext;
    }

    private SubWorkflowLogicTaskRuntimeContext recoverFromSuspendTasks() {
        final SubWorkflowControlClient subWorkflowControlClient =
                applicationContext.getBean(SubWorkflowControlClient.class);
        if (subWorkflowLogicTaskRuntimeContext == null) {
            log.info("The task: {} is recover from suspend but runtimeContext is null will trigger again",
                    taskExecutionContext.getTaskName());
            return triggerNewSubWorkflow();
        }
        final WorkflowInstanceRecoverSuspendTasksRequest recoverSuspendTasksRequest =
                WorkflowInstanceRecoverSuspendTasksRequest.builder()
                        .workflowInstanceId(subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId())
                        .userId(taskExecutionContext.getExecutorId())
                        .build();
        subWorkflowControlClient.triggerFromSuspendTasks(recoverSuspendTasksRequest);
        return subWorkflowLogicTaskRuntimeContext;
    }

    private SubWorkflowLogicTaskRuntimeContext triggerNewSubWorkflow() {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();

        final WorkflowDefinition subWorkflowDefinition = applicationContext.getBean(WorkflowDefinitionDao.class)
                .queryByCode(taskParameters.getWorkflowDefinitionCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find the sub workflow definition: " + taskParameters.getWorkflowDefinitionCode()));

        final ICommandParam commandParam =
                JSONUtils.parseObject(workflowInstance.getCommandParam(), ICommandParam.class);

        final WorkflowManualTriggerRequest workflowManualTriggerRequest = WorkflowManualTriggerRequest.builder()
                .userId(taskExecutionContext.getExecutorId())
                .workflowDefinitionCode(subWorkflowDefinition.getCode())
                .workflowDefinitionVersion(subWorkflowDefinition.getVersion())
                .failureStrategy(workflowInstance.getFailureStrategy())
                .warningType(workflowInstance.getWarningType())
                .warningGroupId(workflowInstance.getWarningGroupId())
                .workflowInstancePriority(workflowInstance.getWorkflowInstancePriority())
                .workerGroup(workflowInstance.getWorkerGroup())
                .tenantCode(workflowInstance.getTenantCode())
                .environmentCode(workflowInstance.getEnvironmentCode())
                // todo: transport varpool and local params
                .startParamList(commandParam.getCommandParams())
                .dryRun(Flag.of(workflowInstance.getDryRun()))
                .testFlag(Flag.of(workflowInstance.getTestFlag()))
                .build();
        final Integer subWorkflowInstanceId = applicationContext
                .getBean(SubWorkflowControlClient.class)
                .triggerSubWorkflow(workflowManualTriggerRequest);
        return SubWorkflowLogicTaskRuntimeContext.of(subWorkflowInstanceId);
    }

    private void upsertSubWorkflowRelation() {
        final WorkflowInstanceMapDao workflowInstanceMapDao = applicationContext.getBean(WorkflowInstanceMapDao.class);
        WorkflowInstanceRelation workflowInstanceRelation = workflowInstanceMapDao.queryWorkflowMapByParent(
                taskExecutionContext.getWorkflowInstanceId(),
                taskExecutionContext.getTaskInstanceId());
        if (workflowInstanceRelation == null) {
            workflowInstanceRelation = WorkflowInstanceRelation.builder()
                    .parentWorkflowInstanceId(taskExecutionContext.getWorkflowInstanceId())
                    .parentTaskInstanceId(taskExecutionContext.getTaskInstanceId())
                    .workflowInstanceId(subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId())
                    .build();
            workflowInstanceMapDao.insert(workflowInstanceRelation);
        } else {
            workflowInstanceRelation
                    .setWorkflowInstanceId(subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId());
            workflowInstanceMapDao.updateById(workflowInstanceRelation);
        }
    }
}
