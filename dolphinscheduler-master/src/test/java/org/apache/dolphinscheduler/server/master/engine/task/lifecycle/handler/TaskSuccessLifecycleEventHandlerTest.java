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

package org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.statemachine.ITaskStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskSuccessLifecycleEventHandlerTest {

    @Mock
    private TaskExecutorClient taskExecutorClient;

    @Mock
    private WorkflowAlertManager workflowAlertManager;

    @Mock
    private ITaskStateAction taskStateAction;

    @Mock
    private ITaskExecution taskExecution;

    @Mock
    private IWorkflowExecution workflowExecution;

    private TaskInstance taskInstance;

    private TaskSuccessLifecycleEventHandler taskSuccessLifecycleEventHandler;

    @BeforeEach
    void setUp() {
        taskSuccessLifecycleEventHandler =
                new TaskSuccessLifecycleEventHandler(taskExecutorClient, workflowAlertManager);

        taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("localhost");

        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getId()).thenReturn(1);
    }

    @Test
    void shouldSendResultAlertWhenSucceedEventTransitionsTaskToSuccess() {
        doAnswer(invocation -> {
            taskInstance.setState(TaskExecutionStatus.SUCCESS);
            return null;
        }).when(taskStateAction)
                .onSucceedEvent(any(), any(), any());

        final TaskSuccessLifecycleEvent taskSuccessEvent = newSuccessEvent(true);

        taskSuccessLifecycleEventHandler.handle(
                taskStateAction, workflowExecution, taskExecution, taskSuccessEvent);

        verify(workflowAlertManager).sendTaskResultAlert(
                any(), eq(taskInstance), any());
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldNotSendResultAlertWhenSucceedEventArrivesOnPausedTask() {
        taskInstance.setState(TaskExecutionStatus.PAUSE);

        final TaskSuccessLifecycleEvent taskSuccessEvent = newSuccessEvent(true);

        taskSuccessLifecycleEventHandler.handle(
                taskStateAction, workflowExecution, taskExecution, taskSuccessEvent);

        verifyNoInteractions(workflowAlertManager);
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldNotSendResultAlertWhenSucceedEventArrivesOnKilledTask() {
        taskInstance.setState(TaskExecutionStatus.KILL);

        final TaskSuccessLifecycleEvent taskSuccessEvent = newSuccessEvent(true);

        taskSuccessLifecycleEventHandler.handle(
                taskStateAction, workflowExecution, taskExecution, taskSuccessEvent);

        verifyNoInteractions(workflowAlertManager);
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldStillSendResultAlertForRepeatedSucceedEvent() {
        taskInstance.setState(TaskExecutionStatus.SUCCESS);

        final TaskSuccessLifecycleEvent taskSuccessEvent = newSuccessEvent(true);

        taskSuccessLifecycleEventHandler.handle(
                taskStateAction, workflowExecution, taskExecution, taskSuccessEvent);

        // Repeated success events are accepted idempotently: the alert is sent again and
        // deduplicated by the DB unique constraint.
        verify(workflowAlertManager).sendTaskResultAlert(
                any(), eq(taskInstance), any());
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldNotSendResultAlertWhenNeedAlertIsFalse() {
        doAnswer(invocation -> {
            taskInstance.setState(TaskExecutionStatus.SUCCESS);
            return null;
        }).when(taskStateAction)
                .onSucceedEvent(any(), any(), any());

        final TaskSuccessLifecycleEvent taskSuccessEvent = newSuccessEvent(false);

        taskSuccessLifecycleEventHandler.handle(
                taskStateAction, workflowExecution, taskExecution, taskSuccessEvent);

        verifyNoInteractions(workflowAlertManager);
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    private TaskSuccessLifecycleEvent newSuccessEvent(final boolean needAlert) {
        return TaskSuccessLifecycleEvent.builder()
                .taskExecution(taskExecution)
                .endTime(new Date())
                .varPool(Collections.emptyList())
                .needAlert(needAlert)
                .taskAlertInfo(new TaskAlertInfo())
                .build();
    }
}
