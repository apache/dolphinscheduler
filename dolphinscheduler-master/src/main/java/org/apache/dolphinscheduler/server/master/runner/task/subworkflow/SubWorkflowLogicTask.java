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

import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubProcessParameters;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class SubWorkflowLogicTask extends BaseAsyncLogicTask<SubProcessParameters> {

    public static final String TASK_TYPE = "SUB_PROCESS";

    private LogicTaskInstanceExecutionEventSenderManager logicTaskInstanceExecutionEventSenderManager;

    private final ProcessInstanceDao processInstanceDao;

    private final ProcessDefinitionDao processDefinitionDao;

    private final CommandDao commandDao;

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;

    private final SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext;

    public SubWorkflowLogicTask(final TaskExecutionContext taskExecutionContext,
                                final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ApplicationContext applicationContext) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<SubProcessParameters>() {
                }));
        this.processDefinitionDao = applicationContext.getBean(ProcessDefinitionDao.class);
        this.processInstanceDao = applicationContext.getBean(ProcessInstanceDao.class);
        this.commandDao = applicationContext.getBean(CommandDao.class);
        this.logicTaskInstanceExecutionEventSenderManager =
                applicationContext.getBean(LogicTaskInstanceExecutionEventSenderManager.class);
        this.workflowExecutionRunnable = workflowExecutionRunnable;
        this.subWorkflowLogicTaskRuntimeContext = initializeSubWorkflowLogicTaskRuntimeContext();
        taskExecutionContext.setAppIds(JSONUtils.toJsonString(subWorkflowLogicTaskRuntimeContext));
        logicTaskInstanceExecutionEventSenderManager.runningEventSender().sendMessage(taskExecutionContext);
    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() {
        return new SubWorkflowAsyncTaskExecuteFunction(taskExecutionContext, processInstanceDao);
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        WorkflowInstance subWorkflowInstance =
                processInstanceDao.querySubProcessInstanceByParentId(taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTaskInstanceId());

        try {
            WorkflowInstancePauseResponse pauseResponse = Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(subWorkflowInstance.getHost())
                    .pauseWorkflowInstance(new WorkflowInstancePauseRequest(subWorkflowInstance.getId()));
            if (pauseResponse.isSuccess()) {
                log.info("Pause sub workflowInstance: {}", subWorkflowInstance.getName() + " success");
            } else {
                throw new MasterTaskExecuteException(
                        "Pause sub workflowInstance: " + subWorkflowInstance.getName() + " failed with response: "
                                + pauseResponse);
            }
        } catch (MasterTaskExecuteException me) {
            throw me;
        } catch (Exception e) {
            throw new MasterTaskExecuteException(
                    "Send pause request to SubWorkflow's master: " + subWorkflowInstance.getName() + " failed", e);
        }
    }

    @Override
    public void kill() {
        WorkflowInstance subWorkflowInstance =
                processInstanceDao.querySubProcessInstanceByParentId(taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTaskInstanceId());
        if (subWorkflowInstance == null) {
            log.info("SubWorkflow instance is null");
            return;
        }
        try {
            WorkflowInstanceStopResponse stopResponse = Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(subWorkflowInstance.getHost())
                    .stopWorkflowInstance(new WorkflowInstanceStopRequest(subWorkflowInstance.getId()));
            if (stopResponse.isSuccess()) {
                log.info("Kill sub workflowInstance: {}", subWorkflowInstance.getName() + " success");
            } else {
                log.error("Kill sub workflowInstance: {} failed with response: {}", subWorkflowInstance.getName(),
                        stopResponse);
            }
        } catch (Exception e) {
            log.error("Send kill request to SubWorkflow's master: {} failed", subWorkflowInstance.getHost(), e);
        }
    }

    private SubWorkflowLogicTaskRuntimeContext initializeSubWorkflowLogicTaskRuntimeContext() {
        if (taskExecutionContext.isFailover() && StringUtils.isNotEmpty(taskExecutionContext.getAppIds())) {
            return JSONUtils.parseObject(taskExecutionContext.getAppIds(), SubWorkflowLogicTaskRuntimeContext.class);
        }
        // If the task is not in failover mode or the runtime context is not exist
        // then we should create the runtime context by command type we should start/recover from failure...
        final IWorkflowExecuteContext workflowExecuteContext = workflowExecutionRunnable.getWorkflowExecuteContext();
        final WorkflowInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        switch (workflowInstance.getCommandType()) {
            case START_PROCESS:
            case SCHEDULER:
            case START_CURRENT_TASK_PROCESS:
            case RECOVER_SERIAL_WAIT:
            case COMPLEMENT_DATA:
                return createSubWorkflowInstanceFromWorkflowDefinition();
            case REPEAT_RUNNING:
            case START_FAILURE_TASK_PROCESS:
            case RECOVER_SUSPENDED_PROCESS:
                return createSubWorkflowInstanceWithWorkflowInstance();
            default:
                throw new IllegalArgumentException("Unsupported command type: " + workflowInstance.getCommandType());
        }
    }

    private SubWorkflowLogicTaskRuntimeContext createSubWorkflowInstanceFromWorkflowDefinition() {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        final ICommandParam commandParam =
                JSONUtils.parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        final RunWorkflowCommandParam runWorkflowCommandParam =
                RunWorkflowCommandParam.builder()
                        .commandParams(new ArrayList<>(taskExecutionContext.getPrepareParamsMap().values()))
                        .startNodes(new ArrayList<>())
                        .timeZone(commandParam.getTimeZone())
                        .subWorkflowInstance(true)
                        .build();

        final WorkflowDefinition subWorkflowDefinition = getSubWorkflowDefinition();
        final Command command = Command.builder()
                .commandType(workflowInstance.getCommandType())
                .processDefinitionCode(subWorkflowDefinition.getCode())
                .processDefinitionVersion(subWorkflowDefinition.getVersion())
                .executorId(workflowInstance.getExecutorId())
                .commandParam(JSONUtils.toJsonString(runWorkflowCommandParam))
                .taskDependType(TaskDependType.TASK_POST)
                .failureStrategy(workflowInstance.getFailureStrategy())
                .warningType(workflowInstance.getWarningType())
                .warningGroupId(workflowInstance.getWarningGroupId())
                .startTime(new Date())
                .processInstancePriority(workflowInstance.getProcessInstancePriority())
                .updateTime(new Date())
                .workerGroup(taskExecutionContext.getWorkerGroup())
                .tenantCode(taskExecutionContext.getTenantCode())
                .dryRun(taskExecutionContext.getDryRun())
                .testFlag(taskExecutionContext.getTestFlag())
                .build();
        commandDao.insert(command);
        return SubWorkflowLogicTaskRuntimeContext.builder()
                .subWorkflowCommandId(command.getId())
                .build();
    }

    private SubWorkflowLogicTaskRuntimeContext createSubWorkflowInstanceWithWorkflowInstance() {
        return null;
    }

    private WorkflowDefinition getSubWorkflowDefinition() {
        return processDefinitionDao.queryByCode(taskParameters.getProcessDefinitionCode()).orElseThrow(
                () -> new IllegalArgumentException(
                        "Cannot find the sub workflow definition: " + taskParameters.getProcessDefinitionCode()));
    }
}
