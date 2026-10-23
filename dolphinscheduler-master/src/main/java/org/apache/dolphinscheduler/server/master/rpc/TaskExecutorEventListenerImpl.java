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

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.ITaskExecutorEventListener;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskExecutorEventListenerImpl implements ITaskExecutorEventListener {

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskExecutorClient taskExecutorClient;

    @Autowired
    private WorkflowAlertManager workflowAlertManager;

    @Override
    public void onTaskExecutorDispatched(final TaskExecutorDispatchedLifecycleEvent taskExecutorDispatchedLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorDispatchedLifecycleEvent.getWorkflowInstanceId());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorDispatchedLifecycleEvent);
            final TaskDispatchedLifecycleEvent taskDispatchedLifecycleEvent = TaskDispatchedLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .executorHost(taskExecutorDispatchedLifecycleEvent.getTaskInstanceHost())
                    .build();

            taskExecution.getWorkflowEventBus().publish(taskDispatchedLifecycleEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorRunning(final TaskExecutorStartedLifecycleEvent taskExecutorStartedLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorStartedLifecycleEvent.getWorkflowInstanceId());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorStartedLifecycleEvent);
            final TaskRunningLifecycleEvent taskRunningEvent = TaskRunningLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .startTime(new Date(taskExecutorStartedLifecycleEvent.getStartTime()))
                    .logPath(taskExecutorStartedLifecycleEvent.getLogPath())
                    .build();

            taskExecution.getWorkflowEventBus().publish(taskRunningEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorRuntimeContextChanged(final TaskExecutorRuntimeContextChangedLifecycleEvent taskExecutorRuntimeContextChangedLifecycleEventr) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorRuntimeContextChangedLifecycleEventr.getWorkflowInstanceId());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorRuntimeContextChangedLifecycleEventr);

            final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent =
                    TaskRuntimeContextChangedEvent.builder()
                            .taskExecution(taskExecution)
                            .runtimeContext(taskExecutorRuntimeContextChangedLifecycleEventr.getAppIds())
                            .build();

            taskExecution.getWorkflowEventBus().publish(taskRuntimeContextChangedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorSuccess(final TaskExecutorSuccessLifecycleEvent taskExecutorSuccessLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorSuccessLifecycleEvent.getWorkflowInstanceId());
        try {
            final IWorkflowExecution workflowExecution =
                    workflowRepository.get(taskExecutorSuccessLifecycleEvent.getWorkflowInstanceId());
            if (workflowExecution == null) {
                ackTaskExecutorTerminalEvent(taskExecutorSuccessLifecycleEvent, TaskExecutionStatus.SUCCESS,
                        taskExecutorSuccessLifecycleEvent.getTaskInstanceHost());
                return;
            }
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorSuccessLifecycleEvent, workflowExecution);
            final TaskSuccessLifecycleEvent taskSuccessEvent = TaskSuccessLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorSuccessLifecycleEvent.getEndTime()))
                    .varPool(taskExecutorSuccessLifecycleEvent.getVarPool())
                    .needAlert(taskExecutorSuccessLifecycleEvent.isNeedAlert())
                    .taskAlertInfo(taskExecutorSuccessLifecycleEvent.getTaskAlertInfo())
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskSuccessEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorFailed(final TaskExecutorFailedLifecycleEvent taskExecutorFailedLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorFailedLifecycleEvent.getWorkflowInstanceId());
        try {
            final IWorkflowExecution workflowExecution =
                    workflowRepository.get(taskExecutorFailedLifecycleEvent.getWorkflowInstanceId());
            if (workflowExecution == null) {
                ackTaskExecutorTerminalEvent(taskExecutorFailedLifecycleEvent, TaskExecutionStatus.FAILURE,
                        taskExecutorFailedLifecycleEvent.getTaskInstanceHost());
                return;
            }
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorFailedLifecycleEvent, workflowExecution);
            final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorFailedLifecycleEvent.getEndTime()))
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskFailedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorKilled(final TaskExecutorKilledLifecycleEvent taskExecutorKilledLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorKilledLifecycleEvent.getWorkflowInstanceId());
        try {
            final IWorkflowExecution workflowExecution =
                    workflowRepository.get(taskExecutorKilledLifecycleEvent.getWorkflowInstanceId());
            if (workflowExecution == null) {
                ackTaskExecutorTerminalEvent(taskExecutorKilledLifecycleEvent, TaskExecutionStatus.KILL,
                        taskExecutorKilledLifecycleEvent.getTaskInstanceHost());
                return;
            }
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorKilledLifecycleEvent, workflowExecution);
            final TaskKilledLifecycleEvent taskKilledEvent = TaskKilledLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorKilledLifecycleEvent.getEndTime()))
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskKilledEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    @Override
    public void onTaskExecutorPaused(final TaskExecutorPausedLifecycleEvent taskExecutorPausedLifecycleEvent) {
        LogUtils.setWorkflowInstanceIdMDC(taskExecutorPausedLifecycleEvent.getWorkflowInstanceId());
        try {
            final IWorkflowExecution workflowExecution =
                    workflowRepository.get(taskExecutorPausedLifecycleEvent.getWorkflowInstanceId());
            if (workflowExecution == null) {
                ackTaskExecutorTerminalEvent(taskExecutorPausedLifecycleEvent, TaskExecutionStatus.PAUSE,
                        taskExecutorPausedLifecycleEvent.getTaskInstanceHost());
                return;
            }
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorPausedLifecycleEvent, workflowExecution);
            final TaskPausedLifecycleEvent taskPausedEvent = TaskPausedLifecycleEvent.of(taskExecution);
            taskExecution.getWorkflowEventBus().publish(taskPausedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    private void ackTaskExecutorTerminalEvent(final IReportableTaskExecutorLifecycleEvent event,
                                              final TaskExecutionStatus reportedTaskExecutionStatus,
                                              final String taskInstanceHost) {
        // Finalization releases the workflow without waiting for the executor to receive its ACK.
        // Confirm the terminal result or a later user operation before acknowledging the original event.
        final TaskInstance taskInstance = taskInstanceDao.queryOptionalById(event.getTaskInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find the TaskInstance: "
                        + event.getTaskInstanceId()));
        checkArgument(Objects.equals(taskInstance.getId(), event.getTaskInstanceId())
                && taskInstance.getWorkflowInstanceId() == event.getWorkflowInstanceId()
                && isTaskTerminalStateCompatible(reportedTaskExecutionStatus, taskInstance.getState())
                && StringUtils.isNotEmpty(taskInstanceHost)
                && taskInstanceHost.equals(taskInstance.getHost()),
                "Terminal event does not match persisted task instance: %s", event.getTaskInstanceId());

        final WorkflowInstance workflowInstance =
                workflowInstanceDao.queryOptionalById(event.getWorkflowInstanceId())
                        .orElseThrow(() -> new IllegalArgumentException("Cannot find the WorkflowInstance: "
                                + event.getWorkflowInstanceId()));
        checkArgument(Objects.equals(workflowInstance.getId(), event.getWorkflowInstanceId())
                && workflowInstance.getState().isFinalState(),
                "Cannot confirm finished WorkflowInstance: %s", event.getWorkflowInstanceId());

        if (event instanceof TaskExecutorSuccessLifecycleEvent) {
            final TaskExecutorSuccessLifecycleEvent successEvent = (TaskExecutorSuccessLifecycleEvent) event;
            if (successEvent.isNeedAlert()) {
                // Preserve the normal handler's at-least-once alert delivery before acknowledging.
                workflowAlertManager.sendTaskResultAlert(workflowInstance, taskInstance,
                        successEvent.getTaskAlertInfo());
            }
        }
        taskExecutorClient.ackTaskExecutorLifecycleEvent(taskInstance,
                new ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck(taskInstance.getId(),
                        event.getType()));
    }

    private boolean isTaskTerminalStateCompatible(final TaskExecutionStatus reportedState,
                                                  final TaskExecutionStatus persistedState) {
        if (persistedState == reportedState) {
            return true;
        }
        // A failed task awaiting retry can be paused or killed without another executor event.
        // Failed and killed tasks can also be forced to success after the workflow has finished.
        switch (reportedState) {
            case FAILURE:
                return persistedState == TaskExecutionStatus.PAUSE
                        || persistedState == TaskExecutionStatus.KILL
                        || persistedState == TaskExecutionStatus.FORCED_SUCCESS;
            case KILL:
                return persistedState == TaskExecutionStatus.FORCED_SUCCESS;
            default:
                return false;
        }
    }

    private ITaskExecution getTaskExecution(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
        final int workflowInstanceId = reportableTaskExecutorLifecycleEvent.getWorkflowInstanceId();

        final IWorkflowExecution workflowExecution = workflowRepository.get(workflowInstanceId);
        if (workflowExecution == null) {
            throw new IllegalArgumentException("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
        }
        return getTaskExecution(reportableTaskExecutorLifecycleEvent, workflowExecution);
    }

    private ITaskExecution getTaskExecution(final IReportableTaskExecutorLifecycleEvent event,
                                            final IWorkflowExecution workflowExecution) {
        final int taskInstanceId = event.getTaskInstanceId();
        final ITaskExecution taskExecution = workflowExecution.getWorkflowExecuteContext()
                .getWorkflowExecutionGraph()
                .getTaskExecutionById(taskInstanceId);
        if (taskExecution == null) {
            throw new IllegalArgumentException("Cannot find the TaskExecuteRunnable: " + taskInstanceId);
        }
        return taskExecution;
    }

}
