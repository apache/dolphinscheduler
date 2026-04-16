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

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.ITaskExecutorEventListener;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.utils.WorkflowLogUtils;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskExecutorEventListenerImpl implements ITaskExecutorEventListener {

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Override
    public void onTaskExecutorDispatched(final TaskExecutorDispatchedLifecycleEvent taskExecutorDispatchedLifecycleEvent) {
        int workflowInstanceId = taskExecutorDispatchedLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
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
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorRunning(final TaskExecutorStartedLifecycleEvent taskExecutorStartedLifecycleEvent) {
        int workflowInstanceId = taskExecutorStartedLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
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
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorRuntimeContextChanged(final TaskExecutorRuntimeContextChangedLifecycleEvent taskExecutorRuntimeContextChangedLifecycleEvent) {
        int workflowInstanceId = taskExecutorRuntimeContextChangedLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorRuntimeContextChangedLifecycleEvent);

            final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent =
                    TaskRuntimeContextChangedEvent.builder()
                            .taskExecution(taskExecution)
                            .runtimeContext(taskExecutorRuntimeContextChangedLifecycleEvent.getAppIds())
                            .build();

            taskExecution.getWorkflowEventBus().publish(taskRuntimeContextChangedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorSuccess(final TaskExecutorSuccessLifecycleEvent taskExecutorSuccessLifecycleEvent) {
        int workflowInstanceId = taskExecutorSuccessLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorSuccessLifecycleEvent);
            final TaskSuccessLifecycleEvent taskSuccessEvent = TaskSuccessLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorSuccessLifecycleEvent.getEndTime()))
                    .varPool(taskExecutorSuccessLifecycleEvent.getVarPool())
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskSuccessEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorFailed(final TaskExecutorFailedLifecycleEvent taskExecutorFailedLifecycleEvent) {
        int workflowInstanceId = taskExecutorFailedLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorFailedLifecycleEvent);
            final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorFailedLifecycleEvent.getEndTime()))
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskFailedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorKilled(final TaskExecutorKilledLifecycleEvent taskExecutorKilledLifecycleEvent) {
        int workflowInstanceId = taskExecutorKilledLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorKilledLifecycleEvent);
            final TaskKilledLifecycleEvent taskKilledEvent = TaskKilledLifecycleEvent.builder()
                    .taskExecution(taskExecution)
                    .endTime(new Date(taskExecutorKilledLifecycleEvent.getEndTime()))
                    .build();
            taskExecution.getWorkflowEventBus().publish(taskKilledEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    @Override
    public void onTaskExecutorPaused(final TaskExecutorPausedLifecycleEvent taskExecutorPausedLifecycleEvent) {
        int workflowInstanceId = taskExecutorPausedLifecycleEvent.getWorkflowInstanceId();
        LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
        WorkflowInstance workflowInstance = workflowRepository.get(workflowInstanceId).getWorkflowInstance();
        WorkflowLogUtils.setWorkflowInstanceLogFullPathMDC(workflowInstance.getLogPath());
        try {
            final ITaskExecution taskExecution =
                    getTaskExecution(taskExecutorPausedLifecycleEvent);
            final TaskPausedLifecycleEvent taskPausedEvent = TaskPausedLifecycleEvent.of(taskExecution);
            taskExecution.getWorkflowEventBus().publish(taskPausedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    private ITaskExecution getTaskExecution(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
        final int workflowInstanceId = reportableTaskExecutorLifecycleEvent.getWorkflowInstanceId();
        final int taskInstanceId = reportableTaskExecutorLifecycleEvent.getTaskInstanceId();

        final IWorkflowExecution workflowExecution = workflowRepository.get(workflowInstanceId);
        if (workflowExecution == null) {
            throw new IllegalArgumentException("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
        }
        final ITaskExecution taskExecution = workflowExecution.getWorkflowExecuteContext()
                .getWorkflowExecutionGraph()
                .getTaskExecutionById(taskInstanceId);
        if (taskExecution == null) {
            throw new IllegalArgumentException("Cannot find the TaskExecuteRunnable: " + taskInstanceId);
        }
        return taskExecution;
    }

}
