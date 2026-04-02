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
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.utils.WorkflowLogUtils;

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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorDispatchedLifecycleEvent);
            final TaskDispatchedLifecycleEvent taskDispatchedLifecycleEvent = TaskDispatchedLifecycleEvent.builder()
                    .taskExecutionRunnable(taskExecutionRunnable)
                    .executorHost(taskExecutorDispatchedLifecycleEvent.getTaskInstanceHost())
                    .build();

            taskExecutionRunnable.getWorkflowEventBus().publish(taskDispatchedLifecycleEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorStartedLifecycleEvent);
            final TaskRunningLifecycleEvent taskRunningEvent = TaskRunningLifecycleEvent.builder()
                    .taskExecutionRunnable(taskExecutionRunnable)
                    .startTime(new Date(taskExecutorStartedLifecycleEvent.getStartTime()))
                    .logPath(taskExecutorStartedLifecycleEvent.getLogPath())
                    .build();

            taskExecutionRunnable.getWorkflowEventBus().publish(taskRunningEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorRuntimeContextChangedLifecycleEvent);

            final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent =
                    TaskRuntimeContextChangedEvent.builder()
                            .taskExecutionRunnable(taskExecutionRunnable)
                            .runtimeContext(taskExecutorRuntimeContextChangedLifecycleEvent.getAppIds())
                            .build();

            taskExecutionRunnable.getWorkflowEventBus().publish(taskRuntimeContextChangedEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorSuccessLifecycleEvent);
            final TaskSuccessLifecycleEvent taskSuccessEvent = TaskSuccessLifecycleEvent.builder()
                    .taskExecutionRunnable(taskExecutionRunnable)
                    .endTime(new Date(taskExecutorSuccessLifecycleEvent.getEndTime()))
                    .varPool(taskExecutorSuccessLifecycleEvent.getVarPool())
                    .build();
            taskExecutionRunnable.getWorkflowEventBus().publish(taskSuccessEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorFailedLifecycleEvent);
            final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                    .taskExecutionRunnable(taskExecutionRunnable)
                    .endTime(new Date(taskExecutorFailedLifecycleEvent.getEndTime()))
                    .build();
            taskExecutionRunnable.getWorkflowEventBus().publish(taskFailedEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorKilledLifecycleEvent);
            final TaskKilledLifecycleEvent taskKilledEvent = TaskKilledLifecycleEvent.builder()
                    .taskExecutionRunnable(taskExecutionRunnable)
                    .endTime(new Date(taskExecutorKilledLifecycleEvent.getEndTime()))
                    .build();
            taskExecutionRunnable.getWorkflowEventBus().publish(taskKilledEvent);
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
            final ITaskExecutionRunnable taskExecutionRunnable =
                    getTaskExecutionRunnable(taskExecutorPausedLifecycleEvent);
            final TaskPausedLifecycleEvent taskPausedEvent = TaskPausedLifecycleEvent.of(taskExecutionRunnable);
            taskExecutionRunnable.getWorkflowEventBus().publish(taskPausedEvent);
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
            WorkflowLogUtils.removeWorkflowInstanceLogFullPathMDC();
        }
    }

    private ITaskExecutionRunnable getTaskExecutionRunnable(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
        final int workflowInstanceId = reportableTaskExecutorLifecycleEvent.getWorkflowInstanceId();
        final int taskInstanceId = reportableTaskExecutorLifecycleEvent.getTaskInstanceId();

        final IWorkflowExecutionRunnable workflowExecutionRunnable = workflowRepository.get(workflowInstanceId);
        if (workflowExecutionRunnable == null) {
            throw new IllegalArgumentException("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
        }
        final ITaskExecutionRunnable taskExecutionRunnable = workflowExecutionRunnable.getWorkflowExecuteContext()
                .getWorkflowExecutionGraph()
                .getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            throw new IllegalArgumentException("Cannot find the TaskExecuteRunnable: " + taskInstanceId);
        }
        return taskExecutionRunnable;
    }
}
