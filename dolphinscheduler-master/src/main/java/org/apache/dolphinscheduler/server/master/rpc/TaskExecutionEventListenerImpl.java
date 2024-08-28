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

import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.ITaskExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.ITaskExecutionEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionDispatchEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionFailedEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionKilledEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionPausedEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionRunningEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionSuccessEvent;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceExecutionEventAckListener;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionFailedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionKilledEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionPausedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionSuccessEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionDispatchedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionRunningEventAck;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskExecutionEventListenerImpl implements ITaskExecutionEventListener {

    @Autowired
    private IWorkflowRepository IWorkflowRepository;

    @Override
    public void onTaskInstanceDispatched(TaskExecutionDispatchEvent taskExecutionDispatchEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionDispatchEvent);
        final TaskDispatchedLifecycleEvent taskDispatchedLifecycleEvent = TaskDispatchedLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .executorHost(taskExecutionDispatchEvent.getTaskInstanceHost())
                .build();

        taskExecutionRunnable.getWorkflowEventBus().publish(taskDispatchedLifecycleEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        // The logic task doesn't need to send ack
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionDispatchEvent.getTaskInstanceHost())
                    .handleTaskInstanceDispatchedEventAck(
                            TaskInstanceExecutionDispatchedEventAck
                                    .success(taskExecutionDispatchEvent.getTaskInstanceId()));
        }
    }

    @Override
    public void onTaskInstanceExecutionRunning(TaskExecutionRunningEvent taskExecutionRunningEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionRunningEvent);
        final TaskRunningLifecycleEvent taskRunningEvent = TaskRunningLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .startTime(new Date(taskExecutionRunningEvent.getStartTime()))
                .logPath(taskExecutionRunningEvent.getLogPath())
                .runtimeContext(taskExecutionRunningEvent.getAppIds())
                .build();

        taskExecutionRunnable.getWorkflowEventBus().publish(taskRunningEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        // The logic task doesn't need to send ack
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionRunningEvent.getTaskInstanceHost())
                    .handleTaskInstanceExecutionRunningEventAck(
                            TaskInstanceExecutionRunningEventAck
                                    .success(taskExecutionRunningEvent.getTaskInstanceId()));
        }
    }

    @Override
    public void onTaskInstanceExecutionSuccess(final TaskExecutionSuccessEvent taskExecutionSuccessEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionSuccessEvent);
        final TaskSuccessLifecycleEvent taskSuccessEvent = TaskSuccessLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .endTime(new Date(taskExecutionSuccessEvent.getEndTime()))
                .varPool(taskExecutionSuccessEvent.getVarPool())
                .build();
        taskExecutionRunnable.getWorkflowEventBus().publish(taskSuccessEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionSuccessEvent.getTaskInstanceHost())
                    .handleTaskExecutionSuccessEventAck(
                            TaskExecutionSuccessEventAck.success(taskExecutionSuccessEvent.getTaskInstanceId()));
        }
    }

    @Override
    public void onTaskInstanceExecutionFailed(TaskExecutionFailedEvent taskExecutionFailedEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionFailedEvent);
        final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .endTime(new Date(taskExecutionFailedEvent.getEndTime()))
                .build();
        taskExecutionRunnable.getWorkflowEventBus().publish(taskFailedEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionFailedEvent.getTaskInstanceHost())
                    .handleTaskExecutionFailedEventAck(
                            TaskExecutionFailedEventAck.success(taskExecutionFailedEvent.getTaskInstanceId()));
        }
    }

    @Override
    public void onTaskInstanceExecutionKilled(TaskExecutionKilledEvent taskExecutionKilledEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionKilledEvent);
        final TaskKilledLifecycleEvent taskKilledEvent = TaskKilledLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .endTime(new Date(taskExecutionKilledEvent.getEndTime()))
                .build();
        taskExecutionRunnable.getWorkflowEventBus().publish(taskKilledEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionKilledEvent.getTaskInstanceHost())
                    .handleTaskExecutionKilledEventAck(
                            TaskExecutionKilledEventAck.success(taskExecutionKilledEvent.getTaskInstanceId()));
        }
    }

    @Override
    public void onTaskInstanceExecutionPaused(TaskExecutionPausedEvent taskExecutionPausedEvent) {
        final ITaskExecutionRunnable taskExecutionRunnable = getTaskExecutionRunnable(taskExecutionPausedEvent);
        final TaskPausedLifecycleEvent taskPausedEvent = TaskPausedLifecycleEvent.of(taskExecutionRunnable);
        taskExecutionRunnable.getWorkflowEventBus().publish(taskPausedEvent);
        // Once the master receive the event, then will send ack to the worker
        // This means the worker will not retry to send the event to the master
        // So once the master failover and we take over the task instance success, then we should fetch the latest task
        // instance state.
        if (!TaskTypeUtils.isLogicTask(taskExecutionRunnable.getTaskDefinition().getTaskType())) {
            Clients
                    .withService(ITaskInstanceExecutionEventAckListener.class)
                    .withHost(taskExecutionPausedEvent.getTaskInstanceHost())
                    .handleTaskExecutionPausedEventAck(
                            TaskExecutionPausedEventAck.success(taskExecutionPausedEvent.getTaskInstanceId()));
        }
    }

    private ITaskExecutionRunnable getTaskExecutionRunnable(final ITaskExecutionEvent taskExecutionEvent) {
        final int workflowInstanceId = taskExecutionEvent.getWorkflowInstanceId();
        final int taskInstanceId = taskExecutionEvent.getTaskInstanceId();

        final IWorkflowExecutionRunnable workflowExecutionRunnable = IWorkflowRepository.get(workflowInstanceId);
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
