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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.ILogicTaskExecutorOperator;
import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorLifecycleEventRemoteReporter;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

class TaskExecutorTerminalAckRecoveryTest {

    private static final int WORKFLOW_ID = 101;
    private static final int TASK_ID = 7;
    private static final String WORKER_HOST = "worker.test:1234";

    private final TaskInstanceDao taskInstanceDao = mock(TaskInstanceDao.class);
    private final WorkflowInstanceDao workflowInstanceDao = mock(WorkflowInstanceDao.class);
    private final IPhysicalTaskExecutorOperator physicalOperator = mock(IPhysicalTaskExecutorOperator.class);
    private final ILogicTaskExecutorOperator logicOperator = mock(ILogicTaskExecutorOperator.class);
    private MockedStatic<Clients> clients;
    private MockedStatic<TaskTypeUtils> taskTypes;
    private final WorkflowAlertManager workflowAlertManager = mock(WorkflowAlertManager.class);

    private TaskExecutorTerminalAckRecovery recovery;
    private TaskInstance task;
    private WorkflowInstance workflow;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        recovery = new TaskExecutorTerminalAckRecovery();
        ReflectionTestUtils.setField(recovery, "taskInstanceDao", taskInstanceDao);
        ReflectionTestUtils.setField(recovery, "workflowInstanceDao", workflowInstanceDao);
        ReflectionTestUtils.setField(recovery, "workflowAlertManager", workflowAlertManager);

        clients = mockStatic(Clients.class);
        taskTypes = mockStatic(TaskTypeUtils.class);
        final Clients.JdkDynamicRpcClientProxyBuilder<IPhysicalTaskExecutorOperator> physicalBuilder =
                mock(Clients.JdkDynamicRpcClientProxyBuilder.class);
        final Clients.JdkDynamicRpcClientProxyBuilder<ILogicTaskExecutorOperator> logicBuilder =
                mock(Clients.JdkDynamicRpcClientProxyBuilder.class);
        clients.when(() -> Clients.withService(IPhysicalTaskExecutorOperator.class)).thenReturn(physicalBuilder);
        clients.when(() -> Clients.withService(ILogicTaskExecutorOperator.class)).thenReturn(logicBuilder);
        when(physicalBuilder.withHost(WORKER_HOST)).thenReturn(physicalOperator);
        when(logicBuilder.withHost(WORKER_HOST)).thenReturn(logicOperator);

        task = new TaskInstance();
        task.setId(TASK_ID);
        task.setWorkflowInstanceId(WORKFLOW_ID);
        task.setName("last-task");
        task.setHost(WORKER_HOST);
        task.setTaskType("SHELL");
        task.setState(TaskExecutionStatus.SUCCESS);
        task.setFlag(Flag.YES);
        workflow = new WorkflowInstance();
        workflow.setId(WORKFLOW_ID);
        workflow.setState(WorkflowExecutionStatus.SUCCESS);
        when(taskInstanceDao.queryOptionalById(TASK_ID)).thenReturn(Optional.of(task));
        when(workflowInstanceDao.queryOptionalById(WORKFLOW_ID)).thenReturn(Optional.of(workflow));
    }

    @AfterEach
    void tearDown() {
        clients.close();
        taskTypes.close();
    }

    @Test
    void shouldRouteALogicTaskAckToItsPersistedHost() {
        taskTypes.when(() -> TaskTypeUtils.isLogicTask("SHELL")).thenReturn(true);

        recoverSuccess(successEvent(false));

        final ArgumentCaptor<TaskExecutorLifecycleEventAck> ack =
                ArgumentCaptor.forClass(TaskExecutorLifecycleEventAck.class);
        verify(logicOperator).ackTaskExecutorLifecycleEvent(ack.capture());
        assertThat(ack.getValue().getTaskExecutorId()).isEqualTo(TASK_ID);
        assertThat(ack.getValue().getTaskExecutorLifecycleEventType())
                .isEqualTo(TaskExecutorLifecycleEventType.SUCCESS);
        verifyNoInteractions(physicalOperator);
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutorLifecycleEventType.class, names = {"SUCCESS", "FAILED", "KILLED", "PAUSED"})
    void shouldAcknowledgePersistedTerminalReportAfterCacheEviction(final TaskExecutorLifecycleEventType type) {
        task.setState(taskState(type));

        deliver(type);

        final ArgumentCaptor<TaskExecutorLifecycleEventAck> ack =
                ArgumentCaptor.forClass(TaskExecutorLifecycleEventAck.class);
        verify(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(ack.capture());
        assertThat(ack.getValue().getTaskExecutorId()).isEqualTo(TASK_ID);
        assertThat(ack.getValue().getTaskExecutorLifecycleEventType()).isEqualTo(type);
        verify(taskInstanceDao, never()).updateById(any(TaskInstance.class));
        verifyNoInteractions(workflowAlertManager);
    }

    @Test
    void shouldAcknowledgeAnOldPersistedAttemptWithoutRequiringItsActiveFlag() {
        task.setFlag(Flag.NO);

        recoverSuccess(successEvent(false));

        verify(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {"SUCCESS", "FAILURE", "STOP", "PAUSE"})
    void shouldAcknowledgeTheTaskWhenItsWorkflowHasAnyPersistedFinalState(final WorkflowExecutionStatus state) {
        workflow.setState(state);

        recoverSuccess(successEvent(false));

        verify(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutionStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "SUCCESS")
    void shouldNotAcknowledgeSuccessUnlessThatExactTerminalStateWasPersisted(final TaskExecutionStatus state) {
        task.setState(state);

        assertRejectedSuccess();
    }

    @Test
    void shouldNotAcknowledgeFailureWhileThePersistedTaskNeedsFaultTolerance() {
        task.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);

        assertThatThrownBy(() -> deliver(TaskExecutorLifecycleEventType.FAILED))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    @ParameterizedTest
    @CsvSource({
            "FAILED, PAUSE, PAUSE",
            "FAILED, KILL, STOP",
            "FAILED, FORCED_SUCCESS, SUCCESS",
            "KILLED, FORCED_SUCCESS, SUCCESS"
    })
    void shouldReleasePendingReportAfterTerminalStateIsChangedByUser(final TaskExecutorLifecycleEventType type,
                                                                     final TaskExecutionStatus persistedState,
                                                                     final WorkflowExecutionStatus workflowState) {
        final TaskExecutorEventBus workerBus = new TaskExecutorEventBus();
        final ITaskExecutor executor = mock(ITaskExecutor.class);
        when(executor.getId()).thenReturn(TASK_ID);
        when(executor.getTaskExecutorEventBus()).thenReturn(workerBus);
        final TaskExecutorRepository executors = new TaskExecutorRepository();
        executors.put(executor);
        final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                "terminal-ack-test", (host, event) -> {
                }, executors);
        final IReportableTaskExecutorLifecycleEvent pendingEvent = type == TaskExecutorLifecycleEventType.FAILED
                ? TaskExecutorFailedLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).endTime(1_791_964_800_123L).build()
                : TaskExecutorKilledLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).endTime(1_791_964_800_123L).build();
        reporter.reportTaskExecutorLifecycleEvent(pendingEvent);
        task.setState(persistedState);
        workflow.setState(workflowState);
        doAnswer(invocation -> {
            reporter.receiveTaskExecutorLifecycleEventACK(invocation.getArgument(0));
            return null;
        }).when(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));

        deliver(type);

        assertThat(reporter.getEventChannels()).isEmpty();
        assertThat(workerBus.poll()).hasValueSatisfying(event -> assertThat(event.getType())
                .isEqualTo(TaskExecutorLifecycleEventType.FINALIZE));
        assertThat(task.getState()).isEqualTo(persistedState);
        verify(taskInstanceDao, never()).updateById(any(TaskInstance.class));
        verifyNoInteractions(workflowAlertManager);
    }

    @ParameterizedTest
    @CsvSource({
            "FAILED, SUCCESS",
            "FAILED, RUNNING_EXECUTION",
            "KILLED, PAUSE",
            "KILLED, FAILURE",
            "PAUSED, FORCED_SUCCESS"
    })
    void shouldNotAcknowledgeAnUnrelatedPersistedState(final TaskExecutorLifecycleEventType type,
                                                       final TaskExecutionStatus persistedState) {
        task.setState(persistedState);

        assertThatThrownBy(() -> deliver(type)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldStillValidateHostAndWorkflowAfterTerminalStateChanges(final boolean unfinishedWorkflow) {
        task.setState(TaskExecutionStatus.PAUSE);
        if (unfinishedWorkflow) {
            workflow.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        } else {
            task.setHost("another-worker:1234");
        }

        assertThatThrownBy(() -> deliver(TaskExecutorLifecycleEventType.FAILED))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    private enum InvalidIdentity {
        MISSING_TASK, WRONG_TASK, WRONG_TASK_WORKFLOW, MISSING_HOST, EMPTY_HOST, DIFFERENT_HOST,
        MISSING_WORKFLOW, WRONG_WORKFLOW, UNFINISHED_WORKFLOW
    }

    @ParameterizedTest
    @EnumSource(InvalidIdentity.class)
    void shouldNotAcknowledgeAnUnknownOrMismatchedExecution(final InvalidIdentity invalidIdentity) {
        switch (invalidIdentity) {
            case MISSING_TASK:
                when(taskInstanceDao.queryOptionalById(TASK_ID)).thenReturn(Optional.empty());
                break;
            case WRONG_TASK:
                task.setId(TASK_ID + 1);
                break;
            case WRONG_TASK_WORKFLOW:
                task.setWorkflowInstanceId(WORKFLOW_ID + 1);
                break;
            case MISSING_HOST:
                task.setHost(null);
                break;
            case EMPTY_HOST:
                task.setHost("");
                break;
            case DIFFERENT_HOST:
                task.setHost("another-worker:1234");
                break;
            case MISSING_WORKFLOW:
                when(workflowInstanceDao.queryOptionalById(WORKFLOW_ID)).thenReturn(Optional.empty());
                break;
            case WRONG_WORKFLOW:
                workflow.setId(WORKFLOW_ID + 1);
                break;
            case UNFINISHED_WORKFLOW:
                workflow.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
                break;
            default:
                throw new IllegalArgumentException(invalidIdentity.name());
        }

        assertRejectedSuccess();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotAcknowledgeAReportWithoutItsExecutorHost(final String host) {
        final TaskExecutorSuccessLifecycleEvent event = successEvent(false);
        event.setTaskInstanceHost(host);

        assertThatThrownBy(() -> recoverSuccess(event)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldNotAcknowledgeWhenTheDatabaseCannotConfirmCompletion(final boolean workflowQueryFails) {
        final DataAccessResourceFailureException failure =
                new DataAccessResourceFailureException("synthetic completion lookup failure");
        if (workflowQueryFails) {
            when(workflowInstanceDao.queryOptionalById(WORKFLOW_ID)).thenThrow(failure);
        } else {
            when(taskInstanceDao.queryOptionalById(TASK_ID)).thenThrow(failure);
        }

        assertThatThrownBy(() -> recoverSuccess(successEvent(false))).isSameAs(failure);

        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    @Test
    void shouldPersistRequestedResultAlertBeforeAcknowledgingSuccess() {
        final TaskExecutorSuccessLifecycleEvent event = successEvent(true);

        recoverSuccess(event);

        final InOrder inOrder = inOrder(workflowAlertManager, physicalOperator);
        inOrder.verify(workflowAlertManager).sendTaskResultAlert(workflow, task, event.getTaskAlertInfo());
        inOrder.verify(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(
                any(TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldLeaveSuccessUnacknowledgedUntilRequestedAlertPersistenceRecovers() {
        final TaskExecutorSuccessLifecycleEvent event = successEvent(true);
        final DataAccessResourceFailureException failure =
                new DataAccessResourceFailureException("synthetic result alert failure");
        doThrow(failure).doNothing().when(workflowAlertManager)
                .sendTaskResultAlert(workflow, task, event.getTaskAlertInfo());

        assertThatThrownBy(() -> recoverSuccess(event)).isSameAs(failure);
        verifyNoInteractions(physicalOperator, logicOperator);

        recoverSuccess(event);

        verify(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldReleasePendingWorkerEventWhenAFallbackAckIsRetried() {
        final TaskExecutorEventBus workerBus = new TaskExecutorEventBus();
        final ITaskExecutor executor = mock(ITaskExecutor.class);
        when(executor.getId()).thenReturn(TASK_ID);
        when(executor.getTaskExecutorEventBus()).thenReturn(workerBus);
        final TaskExecutorRepository executors = new TaskExecutorRepository();
        executors.put(executor);
        final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                "terminal-ack-test", (host, event) -> {
                }, executors);
        final TaskExecutorSuccessLifecycleEvent event = successEvent(false);
        reporter.reportTaskExecutorLifecycleEvent(event);
        final AtomicInteger ackAttempts = new AtomicInteger();
        doAnswer(invocation -> {
            if (ackAttempts.incrementAndGet() == 1) {
                throw new IllegalStateException("synthetic ACK transport failure");
            }
            reporter.receiveTaskExecutorLifecycleEventACK(invocation.getArgument(0));
            return null;
        }).when(physicalOperator).ackPhysicalTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));

        recoverSuccess(event);
        assertThat(reporter.getEventChannels()).containsKey(TASK_ID);
        assertThat(workerBus.isEmpty()).isTrue();

        recoverSuccess(event);

        assertThat(ackAttempts).hasValue(2);
        assertThat(reporter.getEventChannels()).isEmpty();
        assertThat(workerBus.poll()).hasValueSatisfying(finalizeEvent -> assertThat(finalizeEvent.getType())
                .isEqualTo(TaskExecutorLifecycleEventType.FINALIZE));
    }

    private void recoverSuccess(final TaskExecutorSuccessLifecycleEvent event) {
        recovery.recover(event, TaskExecutionStatus.SUCCESS, event.getTaskInstanceHost());
    }

    private void recoverFailed(final TaskExecutorFailedLifecycleEvent event) {
        recovery.recover(event, TaskExecutionStatus.FAILURE, event.getTaskInstanceHost());
    }

    private void recoverKilled(final TaskExecutorKilledLifecycleEvent event) {
        recovery.recover(event, TaskExecutionStatus.KILL, event.getTaskInstanceHost());
    }

    private void recoverPaused(final TaskExecutorPausedLifecycleEvent event) {
        recovery.recover(event, TaskExecutionStatus.PAUSE, event.getTaskInstanceHost());
    }

    private void assertRejectedSuccess() {
        assertThatThrownBy(() -> recoverSuccess(successEvent(false)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(physicalOperator, logicOperator, workflowAlertManager);
    }

    private TaskExecutorSuccessLifecycleEvent successEvent(final boolean needAlert) {
        return TaskExecutorSuccessLifecycleEvent.builder()
                .taskInstanceId(TASK_ID)
                .workflowInstanceId(WORKFLOW_ID)
                .taskInstanceHost(WORKER_HOST)
                .type(TaskExecutorLifecycleEventType.SUCCESS)
                .endTime(1_791_964_800_123L)
                .varPool(Collections.emptyList())
                .needAlert(needAlert)
                .taskAlertInfo(new TaskAlertInfo())
                .build();
    }

    private void deliver(final TaskExecutorLifecycleEventType type) {
        switch (type) {
            case SUCCESS:
                recoverSuccess(successEvent(false));
                return;
            case FAILED:
                recoverFailed(TaskExecutorFailedLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).endTime(1_791_964_800_123L).build());
                return;
            case KILLED:
                recoverKilled(TaskExecutorKilledLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).endTime(1_791_964_800_123L).build());
                return;
            case PAUSED:
                recoverPaused(TaskExecutorPausedLifecycleEvent.builder()
                        .taskInstanceId(TASK_ID).workflowInstanceId(WORKFLOW_ID).taskInstanceHost(WORKER_HOST)
                        .type(type).build());
                return;
            default:
                throw new IllegalArgumentException(type.name());
        }
    }

    private TaskExecutionStatus taskState(final TaskExecutorLifecycleEventType type) {
        switch (type) {
            case SUCCESS:
                return TaskExecutionStatus.SUCCESS;
            case FAILED:
                return TaskExecutionStatus.FAILURE;
            case KILLED:
                return TaskExecutionStatus.KILL;
            case PAUSED:
                return TaskExecutionStatus.PAUSE;
            default:
                throw new IllegalArgumentException(type.name());
        }
    }
}
