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

package org.apache.dolphinscheduler.server.master.engine.task.statemachine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.WorkerGroupDispatcherCoordinator;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKillLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPauseLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskSubmittedStateActionTest {

    @InjectMocks
    private TaskSubmittedStateAction taskSubmittedStateAction;

    @Mock
    private WorkerGroupDispatcherCoordinator workerGroupDispatcherCoordinator;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ITaskGroupCoordinator taskGroupCoordinator;

    @Mock
    private IWorkflowExecution workflowExecution;

    @Mock
    private ITaskExecution taskExecution;

    @Mock
    private WorkflowEventBus workflowEventBus;

    private TaskInstance taskInstance;

    @BeforeEach
    void setUp() {
        taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setName("testTask");
        taskInstance.setId(1);

        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getWorkflowEventBus()).thenReturn(workflowEventBus);
        when(taskExecution.getName()).thenReturn("testTask");
        when(taskExecution.getId()).thenReturn(1);
    }

    @Test
    void onPauseEvent_taskWaitingForTaskGroupSlot_shouldPauseDirectly() {
        // Task not in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(false);
        // Task is waiting for TaskGroup slot
        when(taskGroupCoordinator.isTaskWaitingForTaskGroupSlot(taskInstance)).thenReturn(true);

        taskSubmittedStateAction.onPauseEvent(workflowExecution, taskExecution, null);

        verify(taskGroupCoordinator).releaseTaskGroupSlot(taskInstance);
        verify(workflowEventBus).publish(any(TaskPausedLifecycleEvent.class));
        // Should NOT publish a delayed TaskPauseLifecycleEvent
        verify(workflowEventBus, never()).publish(any(TaskPauseLifecycleEvent.class));
    }

    @Test
    void onPauseEvent_taskInDispatchQueue_shouldPauseDirectly() {
        // Task is in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(true);

        taskSubmittedStateAction.onPauseEvent(workflowExecution, taskExecution, null);

        verify(workflowEventBus).publish(any(TaskPausedLifecycleEvent.class));
        // Should NOT check TaskGroup since task was removed from dispatch queue
        verify(taskGroupCoordinator, never()).isTaskWaitingForTaskGroupSlot(any());
    }

    @Test
    void onPauseEvent_taskNotInDispatchQueueAndNotWaiting_shouldRetryAfter5s() {
        // Task not in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(false);
        // Task is NOT waiting for TaskGroup slot
        when(taskGroupCoordinator.isTaskWaitingForTaskGroupSlot(taskInstance)).thenReturn(false);

        taskSubmittedStateAction.onPauseEvent(workflowExecution, taskExecution, null);

        verify(taskGroupCoordinator, never()).releaseTaskGroupSlot(any());
        // Should publish a delayed TaskPauseLifecycleEvent (retry after 5s)
        verify(workflowEventBus).publish(any(TaskPauseLifecycleEvent.class));
    }

    @Test
    void onKillEvent_taskWaitingForTaskGroupSlot_shouldKillDirectly() {
        // Task not in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(false);
        // Task is waiting for TaskGroup slot
        when(taskGroupCoordinator.isTaskWaitingForTaskGroupSlot(taskInstance)).thenReturn(true);

        taskSubmittedStateAction.onKillEvent(workflowExecution, taskExecution, null);

        verify(taskGroupCoordinator).releaseTaskGroupSlot(taskInstance);
        verify(workflowEventBus).publish(any(TaskKilledLifecycleEvent.class));
        // Should NOT publish a delayed TaskKillLifecycleEvent
        verify(workflowEventBus, never()).publish(any(TaskKillLifecycleEvent.class));
    }

    @Test
    void onKillEvent_taskInDispatchQueue_shouldKillDirectly() {
        // Task is in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(true);

        taskSubmittedStateAction.onKillEvent(workflowExecution, taskExecution, null);

        verify(workflowEventBus).publish(any(TaskKilledLifecycleEvent.class));
        // Should NOT check TaskGroup since task was removed from dispatch queue
        verify(taskGroupCoordinator, never()).isTaskWaitingForTaskGroupSlot(any());
    }

    @Test
    void onKillEvent_taskNotInDispatchQueueAndNotWaiting_shouldRetryAfter5s() {
        // Task not in dispatch queue
        when(workerGroupDispatcherCoordinator.removeTask(taskExecution)).thenReturn(false);
        // Task is NOT waiting for TaskGroup slot
        when(taskGroupCoordinator.isTaskWaitingForTaskGroupSlot(taskInstance)).thenReturn(false);

        taskSubmittedStateAction.onKillEvent(workflowExecution, taskExecution, null);

        verify(taskGroupCoordinator, never()).releaseTaskGroupSlot(any());
        // Should publish a delayed TaskKillLifecycleEvent (retry after 5s)
        verify(workflowEventBus).publish(any(TaskKillLifecycleEvent.class));
    }
}
