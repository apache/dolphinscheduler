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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusFireWorker;
import org.apache.dolphinscheduler.server.master.engine.task.client.PhysicalTaskExecutorClientDelegator;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskSuccessLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class TaskSuccessPersistenceRecoveryTest {

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ITaskGroupCoordinator taskGroupCoordinator;

    @Mock
    private TaskExecutorClient taskExecutorClient;

    @Mock
    private WorkflowAlertManager workflowAlertManager;

    @Mock
    private ITaskExecution taskExecution;

    @Mock
    private IWorkflowExecution workflowExecution;

    private WorkflowEventBus workflowEventBus;

    private final AtomicBoolean failPersistence = new AtomicBoolean();

    private final AtomicInteger persistenceAttempts = new AtomicInteger();

    private final PersistentTaskSnapshot persistentTaskSnapshot = new PersistentTaskSnapshot();

    private TaskInstance taskInstance;

    private TaskSuccessLifecycleEvent successEvent;

    private TestableTaskSuccessLifecycleEventHandler handler;

    private TaskStateActionFactory taskStateActionFactory;

    @BeforeEach
    void setUp() {
        taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("terminal-state-recovery");
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setVarPool("[]");
        persistentTaskSnapshot.copyFrom(taskInstance);

        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("terminal-state-recovery-workflow");
        workflowInstance.setVarPool("[]");

        workflowEventBus = spy(new WorkflowEventBus());

        lenient().when(taskExecution.getId()).thenReturn(1);
        lenient().when(taskExecution.getName()).thenReturn(taskInstance.getName());
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        lenient().when(taskExecution.getWorkflowEventBus()).thenReturn(workflowEventBus);
        lenient().when(workflowExecution.getWorkflowInstance()).thenReturn(workflowInstance);
        lenient().when(workflowExecution.getWorkflowEventBus()).thenReturn(workflowEventBus);

        lenient().when(taskInstanceDao.updateById(any(TaskInstance.class))).thenAnswer(invocation -> {
            persistenceAttempts.incrementAndGet();
            if (failPersistence.get()) {
                throw new DataAccessResourceFailureException("synthetic database write failure");
            }
            persistentTaskSnapshot.copyFrom(invocation.getArgument(0));
            return true;
        });

        final TaskRunningStateAction runningStateAction = new TaskRunningStateAction();
        runningStateAction.taskGroupCoordinator = taskGroupCoordinator;
        runningStateAction.taskInstanceDao = taskInstanceDao;

        final TaskSuccessStateAction successStateAction = new TaskSuccessStateAction();
        successStateAction.taskGroupCoordinator = taskGroupCoordinator;
        successStateAction.taskInstanceDao = taskInstanceDao;

        taskStateActionFactory = new TaskStateActionFactory(
                createStateActions(runningStateAction, successStateAction));

        handler = new TestableTaskSuccessLifecycleEventHandler(taskExecutorClient, workflowAlertManager);
        handler.setTaskStateActionFactory(taskStateActionFactory);

        successEvent = TaskSuccessLifecycleEvent.builder()
                .taskExecution(taskExecution)
                .endTime(new Date(1_791_964_800_123L))
                .varPool(Collections.emptyList())
                .needAlert(false)
                .build();
    }

    @Test
    void shouldPersistAndAcknowledgeSuccessWithoutFailure() {
        handler.handle(workflowExecution, successEvent);

        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(persistentTaskSnapshot.endTime).isEqualTo(successEvent.getEndTime());
        assertThat(persistenceAttempts).hasValue(1);
        verify(workflowEventBus).publish(any(AbstractLifecycleEvent.class));
        verifyAck(1);
    }

    @Test
    void shouldKeepInMemoryStateRetryableWhenFirstPersistenceFails() {
        failPersistence.set(true);

        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(taskInstance.getEndTime()).isNull();
        assertThat(taskInstance.getVarPool()).isEqualTo("[]");
        assertThat(persistenceAttempts).hasValue(1);
        verifyAck(0);
    }

    @Test
    void shouldPersistAndAcknowledgeWhenSameSuccessEventIsReportedAfterRecovery() {
        failPersistence.set(true);
        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);

        failPersistence.set(false);
        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(2);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(persistentTaskSnapshot.endTime).isEqualTo(successEvent.getEndTime());
        verify(workflowEventBus).publish(any(AbstractLifecycleEvent.class));
        verifyAck(1);
    }

    @Test
    void shouldNotAcknowledgeRepeatedSuccessReportsWhilePersistenceKeepsFailing() {
        failPersistence.set(true);

        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);
        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(persistenceAttempts).hasValue(2);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        verifyAck(0);
    }

    @Test
    void shouldAcknowledgeDuplicateSuccessAfterItWasPersisted() {
        handler.handle(workflowExecution, successEvent);
        clearInvocations(taskExecutorClient, workflowEventBus);

        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(1);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        verify(workflowEventBus, never()).publish(any(AbstractLifecycleEvent.class));
        verifyAck(1);
    }

    @Test
    void shouldRetryRecognizedConnectionFailureThroughWorkflowEventBusAndPersistAfterRecovery() {
        final IWorkflowExecuteContext workflowExecuteContext = mock(IWorkflowExecuteContext.class);
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        when(workflowExecuteContext.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecution.getWorkflowExecuteContext()).thenReturn(workflowExecuteContext);
        when(workflowExecution.getId()).thenReturn(1);
        when(workflowExecution.getName()).thenReturn("terminal-state-recovery-workflow");

        final WorkflowEventBusFireWorker fireWorker = new WorkflowEventBusFireWorker();
        fireWorker.registerWorkflowEventBus(workflowExecution);
        fireWorker.registerEventHandler(handler);
        fireWorker.registerEventHandler(new NoOpWorkflowTopologyEventHandler());
        workflowEventBus.publish(successEvent);

        failPersistence.set(true);
        fireWorker.fireAllRegisteredEvent();

        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(persistenceAttempts).hasValue(1);
        assertThat(workflowEventBus.isEmpty()).isFalse();
        verifyAck(0);

        failPersistence.set(false);
        fireWorker.fireAllRegisteredEvent();

        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(persistentTaskSnapshot.endTime).isEqualTo(successEvent.getEndTime());
        assertThat(persistenceAttempts).hasValue(2);
        assertThat(workflowEventBus.isEmpty()).isTrue();
        verifyAck(1);
    }

    @Test
    void shouldPreserveAndMergeOutputVariablesAcrossPersistenceFailureAndRetransmission() {
        final String originalTaskVarPool = serializeProperties(
                property("existing", "old"),
                property("result", "before"));
        final String originalWorkflowVarPool = serializeProperties(property("workflow", "kept"));
        taskInstance.setVarPool(originalTaskVarPool);
        persistentTaskSnapshot.copyFrom(taskInstance);
        workflowExecution.getWorkflowInstance().setVarPool(originalWorkflowVarPool);
        successEvent = successEventWithVariables(
                property("result", "after"),
                property("output", "new"));

        failPersistence.set(true);
        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(taskInstance.getEndTime()).isNull();
        assertThat(taskInstance.getVarPool()).isEqualTo(originalTaskVarPool);
        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(persistentTaskSnapshot.varPool).isEqualTo(originalTaskVarPool);
        assertThat(workflowExecution.getWorkflowInstance().getVarPool()).isEqualTo(originalWorkflowVarPool);
        verify(workflowEventBus, never()).publish(any(AbstractLifecycleEvent.class));
        verifyAck(0);

        failPersistence.set(false);
        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(2);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(taskInstance.getEndTime()).isEqualTo(successEvent.getEndTime());
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of(
                "existing", "old",
                "result", "after",
                "output", "new"));
        assertVarPool(persistentTaskSnapshot.varPool, ImmutableMap.of(
                "existing", "old",
                "result", "after",
                "output", "new"));
        assertVarPool(workflowExecution.getWorkflowInstance().getVarPool(), ImmutableMap.of(
                "workflow", "kept",
                "existing", "old",
                "result", "after",
                "output", "new"));
        verify(workflowEventBus).publish(any(WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.class));
        verifyAck(1);

        clearInvocations(taskExecutorClient, workflowEventBus);
        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(2);
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of(
                "existing", "old",
                "result", "after",
                "output", "new"));
        assertVarPool(workflowExecution.getWorkflowInstance().getVarPool(), ImmutableMap.of(
                "workflow", "kept",
                "existing", "old",
                "result", "after",
                "output", "new"));
        verify(workflowEventBus, never()).publish(any(AbstractLifecycleEvent.class));
        verifyAck(1);
    }

    @Test
    void shouldPersistThenPublishTopologyThenAcknowledge() {
        taskInstance.setVarPool(serializeProperties(property("existing", "old")));
        persistentTaskSnapshot.copyFrom(taskInstance);
        successEvent = successEventWithVariables(property("output", "new"));
        final List<String> actions = new ArrayList<>();

        when(taskInstanceDao.updateById(any(TaskInstance.class))).thenAnswer(invocation -> {
            persistenceAttempts.incrementAndGet();
            final TaskInstance taskInstanceToUpdate = invocation.getArgument(0);
            assertThat(taskInstanceToUpdate).isNotSameAs(taskInstance);
            assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
            assertThat(taskInstance.getEndTime()).isNull();
            assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old"));
            assertThat(taskInstanceToUpdate.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
            assertThat(taskInstanceToUpdate.getEndTime()).isEqualTo(successEvent.getEndTime());
            assertVarPool(taskInstanceToUpdate.getVarPool(), ImmutableMap.of("existing", "old", "output", "new"));
            actions.add("persist-start");
            persistentTaskSnapshot.copyFrom(taskInstanceToUpdate);
            actions.add("persisted");
            return true;
        });
        doAnswer(invocation -> {
            assertThat((Object) invocation.getArgument(0))
                    .isInstanceOf(WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.class);
            assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
            assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
            actions.add("topology");
            return invocation.callRealMethod();
        }).when(workflowEventBus).publish(any(AbstractLifecycleEvent.class));
        doAnswer(invocation -> {
            assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
            assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
            assertThat(taskInstance.getEndTime()).isEqualTo(successEvent.getEndTime());
            assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old", "output", "new"));
            actions.add("ack");
            return null;
        }).when(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));

        handler.handle(workflowExecution, successEvent);

        assertThat(actions).containsExactly("persist-start", "persisted", "topology", "ack");
    }

    @Test
    void shouldKeepPersistedSuccessWhenRealAckClientFailsAndAcknowledgeRetransmission() {
        taskInstance.setTaskType("SHELL");
        taskInstance.setHost("worker.test:1234");
        taskInstance.setVarPool(serializeProperties(property("existing", "old")));
        persistentTaskSnapshot.copyFrom(taskInstance);
        successEvent = successEventWithVariables(property("output", "new"));

        final PhysicalTaskExecutorClientDelegator delegator = mock(PhysicalTaskExecutorClientDelegator.class);
        final TaskExecutorClient realTaskExecutorClient = new TaskExecutorClient();
        ReflectionTestUtils.setField(realTaskExecutorClient, "physicalTaskExecutorClientDelegator", delegator);
        doThrow(new RuntimeException("synthetic ACK send failure"))
                .doNothing()
                .when(delegator)
                .ackTaskExecutorLifecycleEvent(
                        eq(taskExecution),
                        any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
        handler = new TestableTaskSuccessLifecycleEventHandler(realTaskExecutorClient, workflowAlertManager);
        handler.setTaskStateActionFactory(taskStateActionFactory);

        handler.handle(workflowExecution, successEvent);

        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old", "output", "new"));
        assertThat(persistenceAttempts).hasValue(1);
        verify(workflowEventBus).publish(any(WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.class));

        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(1);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old", "output", "new"));
        verify(workflowEventBus, times(1))
                .publish(any(WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.class));
        verify(delegator, times(2)).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    @Test
    void shouldConvergeWhenFirstUpdateCommitsButThrowsToCaller() {
        taskInstance.setVarPool(serializeProperties(property("existing", "old")));
        persistentTaskSnapshot.copyFrom(taskInstance);
        successEvent = successEventWithVariables(property("output", "new"));
        final AtomicInteger localAttempts = new AtomicInteger();
        when(taskInstanceDao.updateById(any(TaskInstance.class))).thenAnswer(invocation -> {
            persistenceAttempts.incrementAndGet();
            localAttempts.incrementAndGet();
            persistentTaskSnapshot.copyFrom(invocation.getArgument(0));
            if (localAttempts.get() == 1) {
                throw new DataAccessResourceFailureException("synthetic uncertain commit result");
            }
            return true;
        });

        assertThatThrownBy(() -> handler.handle(workflowExecution, successEvent))
                .isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(persistentTaskSnapshot.state).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(taskInstance.getEndTime()).isNull();
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old"));
        verify(workflowEventBus, never()).publish(any(AbstractLifecycleEvent.class));
        verifyAck(0);

        handler.handle(workflowExecution, successEvent);

        assertThat(persistenceAttempts).hasValue(2);
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        assertThat(taskInstance.getEndTime()).isEqualTo(successEvent.getEndTime());
        assertVarPool(taskInstance.getVarPool(), ImmutableMap.of("existing", "old", "output", "new"));
        assertVarPool(persistentTaskSnapshot.varPool, ImmutableMap.of("existing", "old", "output", "new"));
        verify(workflowEventBus).publish(any(WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent.class));
        verifyAck(1);
    }

    private TaskSuccessLifecycleEvent successEventWithVariables(final Property... properties) {
        return TaskSuccessLifecycleEvent.builder()
                .taskExecution(taskExecution)
                .endTime(new Date(1_791_964_800_123L))
                .varPool(Arrays.asList(properties))
                .needAlert(false)
                .build();
    }

    private static Property property(final String name, final String value) {
        return new Property(name, Direct.OUT, DataType.VARCHAR, value);
    }

    private static String serializeProperties(final Property... properties) {
        return VarPoolUtils.serializeVarPool(Arrays.asList(properties));
    }

    private static void assertVarPool(final String actualJson, final Map<String, String> expectedValues) {
        final Map<String, Property> propertiesByName = VarPoolUtils.deserializeVarPool(actualJson)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Function.identity()));
        assertThat(propertiesByName).containsOnlyKeys(expectedValues.keySet());
        expectedValues.forEach((name, value) -> {
            final Property property = propertiesByName.get(name);
            assertThat(property.getValue()).as("value of %s", name).isEqualTo(value);
            assertThat(property.getDirect()).as("direct of %s", name).isEqualTo(Direct.OUT);
            assertThat(property.getType()).as("type of %s", name).isEqualTo(DataType.VARCHAR);
        });
    }

    private List<ITaskStateAction> createStateActions(final TaskRunningStateAction runningStateAction,
                                                      final TaskSuccessStateAction successStateAction) {
        return Arrays.stream(TaskExecutionStatus.values())
                .map(state -> {
                    if (state == TaskExecutionStatus.RUNNING_EXECUTION) {
                        return runningStateAction;
                    }
                    if (state == TaskExecutionStatus.SUCCESS) {
                        return successStateAction;
                    }
                    final ITaskStateAction stateAction = mock(ITaskStateAction.class);
                    when(stateAction.matchState()).thenReturn(state);
                    return stateAction;
                })
                .collect(Collectors.toList());
    }

    private void verifyAck(final int expectedInvocations) {
        if (expectedInvocations == 0) {
            verify(taskExecutorClient, never()).ackTaskExecutorLifecycleEvent(
                    eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
            return;
        }
        verify(taskExecutorClient).ackTaskExecutorLifecycleEvent(
                eq(taskExecution), any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
    }

    private static final class PersistentTaskSnapshot {

        private TaskExecutionStatus state;

        private Date endTime;

        private String varPool;

        private void copyFrom(final TaskInstance source) {
            state = source.getState();
            endTime = source.getEndTime() == null ? null : new Date(source.getEndTime().getTime());
            varPool = source.getVarPool();
        }

    }

    private static final class NoOpWorkflowTopologyEventHandler
            implements
                ILifecycleEventHandler<AbstractLifecycleEvent> {

        @Override
        public void handle(final IWorkflowExecution workflowExecution, final AbstractLifecycleEvent event) {
            // The task success transition is the behavior under test; draining its downstream event keeps the bus
            // finite.
        }

        @Override
        public ILifecycleEventType matchEventType() {
            return WorkflowLifecycleEventType.TOPOLOGY_LOGICAL_TRANSACTION_WITH_TASK_FINISH;
        }
    }

    private static final class TestableTaskSuccessLifecycleEventHandler extends TaskSuccessLifecycleEventHandler {

        private TestableTaskSuccessLifecycleEventHandler(final TaskExecutorClient taskExecutorClient,
                                                         final WorkflowAlertManager workflowAlertManager) {
            super(taskExecutorClient, workflowAlertManager);
        }

        private void setTaskStateActionFactory(final TaskStateActionFactory taskStateActionFactory) {
            this.taskStateActionFactory = taskStateActionFactory;
        }
    }
}
