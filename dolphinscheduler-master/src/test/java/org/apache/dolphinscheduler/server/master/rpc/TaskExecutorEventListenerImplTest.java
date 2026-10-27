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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowCacheRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

class TaskExecutorEventListenerImplTest {

    private static final int WORKFLOW_ID = 101;
    private static final int TASK_ID = 7;
    private static final String WORKER_HOST = "worker.test:1234";

    private final WorkflowCacheRepository workflowRepository = new WorkflowCacheRepository();
    private final TaskExecutorTerminalAckRecovery recovery = mock(TaskExecutorTerminalAckRecovery.class);
    private final TaskExecutorEventListenerImpl listener = new TaskExecutorEventListenerImpl();
    private final IWorkflowExecutionGraph graph = mock(IWorkflowExecutionGraph.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "workflowRepository", workflowRepository);
        ReflectionTestUtils.setField(listener, "taskExecutorTerminalAckRecovery", recovery);
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutorLifecycleEventType.class, names = {"SUCCESS", "FAILED", "KILLED", "PAUSED"})
    void shouldRecoverTerminalAckOnlyWhenWorkflowIsNotCached(final TaskExecutorLifecycleEventType type) {
        final IReportableTaskExecutorLifecycleEvent event = deliver(type);

        final TaskExecutionStatus reportedState;
        switch (type) {
            case SUCCESS:
                reportedState = TaskExecutionStatus.SUCCESS;
                break;
            case FAILED:
                reportedState = TaskExecutionStatus.FAILURE;
                break;
            case KILLED:
                reportedState = TaskExecutionStatus.KILL;
                break;
            case PAUSED:
                reportedState = TaskExecutionStatus.PAUSE;
                break;
            default:
                throw new IllegalArgumentException(type.name());
        }
        verify(recovery).recover(event, reportedState, WORKER_HOST);
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutorLifecycleEventType.class, names = {"SUCCESS", "FAILED", "KILLED", "PAUSED"})
    void shouldPublishToCachedWorkflowWithoutRecoveringAck(final TaskExecutorLifecycleEventType type) {
        cacheWorkflow();
        final WorkflowEventBus bus = new WorkflowEventBus();
        final ITaskExecution taskExecution = mock(ITaskExecution.class);
        when(taskExecution.getWorkflowEventBus()).thenReturn(bus);
        when(graph.getTaskExecutionById(TASK_ID)).thenReturn(taskExecution);

        deliver(type);

        assertThat(bus.poll()).hasValueSatisfying(event -> {
            switch (type) {
                case SUCCESS:
                    assertThat(event).isInstanceOf(TaskSuccessLifecycleEvent.class);
                    assertThat(((TaskSuccessLifecycleEvent) event).getTaskExecution()).isSameAs(taskExecution);
                    break;
                case FAILED:
                    assertThat(event).isInstanceOf(TaskFailedLifecycleEvent.class);
                    assertThat(((TaskFailedLifecycleEvent) event).getTaskExecution()).isSameAs(taskExecution);
                    break;
                case KILLED:
                    assertThat(event).isInstanceOf(TaskKilledLifecycleEvent.class);
                    assertThat(((TaskKilledLifecycleEvent) event).getTaskExecution()).isSameAs(taskExecution);
                    break;
                case PAUSED:
                    assertThat(event).isInstanceOf(TaskPausedLifecycleEvent.class);
                    assertThat(((TaskPausedLifecycleEvent) event).getTaskExecution()).isSameAs(taskExecution);
                    break;
                default:
                    throw new IllegalArgumentException(type.name());
            }
        });
        verifyNoInteractions(recovery);
    }

    @Test
    void shouldNotRecoverAckForAMissingTaskInACachedWorkflow() {
        cacheWorkflow();

        assertThatThrownBy(() -> deliver(TaskExecutorLifecycleEventType.SUCCESS))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(recovery);
    }

    private void cacheWorkflow() {
        final IWorkflowExecuteContext context = mock(IWorkflowExecuteContext.class);
        when(context.getWorkflowExecutionGraph()).thenReturn(graph);
        final IWorkflowExecution workflowExecution = mock(IWorkflowExecution.class);
        when(workflowExecution.getId()).thenReturn(WORKFLOW_ID);
        when(workflowExecution.getWorkflowExecuteContext()).thenReturn(context);
        workflowRepository.put(workflowExecution);
    }

    private IReportableTaskExecutorLifecycleEvent deliver(final TaskExecutorLifecycleEventType type) {
        switch (type) {
            case SUCCESS:
                final TaskExecutorSuccessLifecycleEvent success = TaskExecutorSuccessLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).build();
                listener.onTaskExecutorSuccess(success);
                return success;
            case FAILED:
                final TaskExecutorFailedLifecycleEvent failed = TaskExecutorFailedLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).build();
                listener.onTaskExecutorFailed(failed);
                return failed;
            case KILLED:
                final TaskExecutorKilledLifecycleEvent killed = TaskExecutorKilledLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).build();
                listener.onTaskExecutorKilled(killed);
                return killed;
            case PAUSED:
                final TaskExecutorPausedLifecycleEvent paused = TaskExecutorPausedLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).build();
                listener.onTaskExecutorPaused(paused);
                return paused;
            default:
                throw new IllegalArgumentException(type.name());
        }
    }
}
