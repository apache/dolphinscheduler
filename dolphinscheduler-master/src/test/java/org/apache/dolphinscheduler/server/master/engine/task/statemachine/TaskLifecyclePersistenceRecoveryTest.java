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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusFireWorker;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.WorkerGroupDispatcherCoordinator;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFatalLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRetryLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.AbstractTaskLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskDispatchLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskDispatchedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskFailedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskFatalLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskKilledLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskPausedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskRunningLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.handler.TaskRuntimeContextChangedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

class TaskLifecyclePersistenceRecoveryTest {

    private enum EventType {
        DISPATCHED, RUNNING, PAUSED, KILLED, FAILED, FATAL, CONTEXT, DELAY
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void shouldKeepSharedFieldsUnchangedUntilPersistenceRecovers(final EventType type) {
        final Fixture fixture = new Fixture(type);
        final List<Object> original = fields(fixture.shared);
        fixture.fail.set(true);

        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(fields(fixture.shared)).isEqualTo(original);
        assertThat(fields(fixture.persisted)).isEqualTo(original);
        verifyNoInteractions(fixture.client, fixture.graph, fixture.dispatcher);
        verify(fixture.bus, never()).publish(any(AbstractLifecycleEvent.class));

        fixture.fail.set(false);
        fixture.deliver.run();

        assertThat(fixture.attempts).hasValue(2);
        fixture.assertPersistedResult();
        fixture.assertAck(type == EventType.FATAL || type == EventType.DELAY ? 0 : 1);
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"DISPATCHED", "PAUSED", "KILLED"})
    void shouldRetryTheDatabaseUpdateInsteadOfAcknowledgingAnUnpersistedState(final EventType type) {
        final Fixture fixture = new Fixture(type);
        fixture.fail.set(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);

        fixture.fail.set(false);
        fixture.deliver.run();

        assertThat(fixture.attempts).hasValue(2);
        fixture.assertPersistedResult();
        fixture.assertAck(1);
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void shouldNotAcknowledgeOrAdvanceWhilePersistenceKeepsFailing(final EventType type) {
        final Fixture fixture = new Fixture(type);
        fixture.fail.set(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(fixture.attempts).hasValue(2);
        verifyNoInteractions(fixture.client, fixture.graph, fixture.dispatcher);
        verify(fixture.bus, never()).publish(any(AbstractLifecycleEvent.class));
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void shouldPersistNormallyWithoutReplacingTheSharedInstanceOrUnrelatedFields(final EventType type) {
        final Fixture fixture = new Fixture(type);
        when(fixture.dao.updateById(any(TaskInstance.class))).thenAnswer(invocation -> {
            final TaskInstance candidate = invocation.getArgument(0);
            assertThat(candidate).isNotSameAs(fixture.shared);
            BeanUtils.copyProperties(candidate, fixture.persisted);
            // A field outside this event's update must not be overwritten by copying the whole candidate back.
            fixture.shared.setName("updated-name");
            return true;
        });
        fixture.deliver.run();
        fixture.assertPersistedResult();
        assertThat(fixture.task.getTaskInstance()).isSameAs(fixture.shared);
        assertThat(fixture.shared.getName()).isEqualTo("updated-name");
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void shouldPreserveExistingBehaviorWhenUpdateReturnsFalse(final EventType type) {
        final Fixture fixture = new Fixture(type);
        when(fixture.dao.updateById(any(TaskInstance.class))).thenReturn(false);
        fixture.deliver.run();
        assertThat(fixture.shared.getState()).isEqualTo(fixture.expectedState());
        fixture.assertAck(type == EventType.FATAL || type == EventType.DELAY ? 0 : 1);
        verify(fixture.dao, never()).queryById(any());
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"DISPATCHED", "PAUSED", "KILLED"})
    void shouldAcknowledgeAgainWithoutRepeatingPersistenceAfterAckFailure(final EventType type) {
        final Fixture fixture = new Fixture(type);
        doThrow(new IllegalStateException("synthetic ACK failure")).doNothing().when(fixture.client)
                .ackTaskExecutorLifecycleEvent(eq(fixture.task), any());
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(IllegalStateException.class);
        fixture.assertPersistedResult();
        fixture.deliver.run();
        assertThat(fixture.attempts).hasValue(1);
        fixture.assertAck(2);
        verify(fixture.bus, times(type == EventType.DISPATCHED ? 0 : 1)).publish(any(AbstractLifecycleEvent.class));
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"FAILED", "FATAL"})
    void shouldScheduleTaskRetryOnlyAfterFailureIsPersisted(final EventType type) {
        final Fixture fixture = new Fixture(type);
        fixture.shared.setMaxRetryTimes(1);
        when(fixture.task.isTaskInstanceCanRetry()).thenReturn(true);
        fixture.fail.set(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        verify(fixture.bus, never()).publish(any(AbstractLifecycleEvent.class));
        fixture.fail.set(false);
        fixture.deliver.run();
        verify(fixture.bus).publish(any(TaskRetryLifecycleEvent.class));
        verifyNoInteractions(fixture.graph);
        fixture.assertPersistedResult();
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"FAILED", "FATAL"})
    void shouldKeepConditionSuccessorsEligibleAfterPersistenceRecovers(final EventType type) {
        final Fixture fixture = new Fixture(type);
        when(fixture.graph.isAllSuccessorsAreConditionTask(fixture.task)).thenReturn(true);
        fixture.fail.set(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        fixture.fail.set(false);
        fixture.deliver.run();
        verify(fixture.graph, never()).markTaskExecutionChainFailure(fixture.task);
        verify(fixture.bus).publish(any(AbstractLifecycleEvent.class));
        fixture.assertPersistedResult();
    }

    @Test
    void shouldKeepExistingAppLinkWhenRuntimeContextIsEmpty() {
        final Fixture fixture = new Fixture(EventType.CONTEXT);
        fixture.bind(new TaskRuntimeContextChangedLifecycleEventHandler(fixture.client),
                new TaskRuntimeContextChangedEvent(fixture.task, ""));
        fixture.deliver.run();
        assertThat(fixture.shared.getAppLink()).isEqualTo("old-context");
        assertThat(fixture.persisted.getAppLink()).isEqualTo("old-context");
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"PAUSED", "DISPATCHED"})
    void shouldRecoverThroughTheRealWorkflowEventBus(final EventType type) {
        final Fixture fixture = new Fixture(type);
        final IWorkflowExecuteContext context = mock(IWorkflowExecuteContext.class);
        final WorkflowInstance workflowInstance = fixture.workflow.getWorkflowInstance();
        when(context.getWorkflowInstance()).thenReturn(workflowInstance);
        when(fixture.workflow.getWorkflowExecuteContext()).thenReturn(context);
        when(fixture.workflow.getId()).thenReturn(1);
        final WorkflowEventBusFireWorker worker = new WorkflowEventBusFireWorker();
        worker.registerWorkflowEventBus(fixture.workflow);
        worker.registerEventHandler(fixture.handler);
        worker.registerEventHandler(new ILifecycleEventHandler<AbstractLifecycleEvent>() {

            @Override
            public void handle(final IWorkflowExecution workflow, final AbstractLifecycleEvent event) {
                // Drain the downstream event; the task event's retry and persistence are under test.
            }

            @Override
            public ILifecycleEventType matchEventType() {
                return WorkflowLifecycleEventType.TOPOLOGY_LOGICAL_TRANSACTION_WITH_TASK_FINISH;
            }
        });
        fixture.bus.publish(fixture.event);
        fixture.fail.set(true);
        worker.fireAllRegisteredEvent();
        assertThat(fixture.attempts).hasValue(1);
        fixture.assertAck(0);
        assertThat(fixture.bus.isEmpty()).isFalse();
        fixture.fail.set(false);
        worker.fireAllRegisteredEvent();
        assertThat(fixture.attempts).hasValue(2);
        fixture.assertPersistedResult();
        fixture.assertAck(1);
        assertThat(fixture.bus.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"PAUSED", "KILLED"})
    void shouldRetryResourceReleaseBeforePersistingTheTerminalState(final EventType type) {
        final Fixture fixture = new Fixture(type);
        final TaskGroupQueueDao queueDao = fixture.useRealTaskGroupCoordinator();
        when(queueDao.queryByTaskInstanceId(1))
                .thenThrow(new DataAccessResourceFailureException("synthetic task group query failure"))
                .thenReturn(Collections.emptyList());
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(fixture.shared.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(fixture.attempts).hasValue(0);
        fixture.assertAck(0);
        fixture.deliver.run();
        verify(queueDao, times(2)).queryByTaskInstanceId(1);
        fixture.assertPersistedResult();
        fixture.assertAck(1);
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"PAUSED", "KILLED"})
    void shouldRetryAQueueDeletionFailureBeforePersistingTheTerminalState(final EventType type) {
        final Fixture fixture = new Fixture(type);
        final TaskGroupQueueDao queueDao = fixture.useRealTaskGroupCoordinator();
        final TaskGroupQueue queue = new TaskGroupQueue();
        queue.setId(10);
        when(queueDao.queryByTaskInstanceId(1)).thenReturn(Collections.singletonList(queue));
        when(queueDao.deleteById(queue))
                .thenThrow(new DataAccessResourceFailureException("synthetic task group deletion failure"))
                .thenReturn(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(fixture.shared.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(fixture.attempts).hasValue(0);
        fixture.assertAck(0);
        fixture.deliver.run();
        verify(queueDao, times(2)).deleteById(queue);
        fixture.assertPersistedResult();
        fixture.assertAck(1);
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"PAUSED", "KILLED"})
    void shouldNotReleaseTheSameQueueEntryAgainWhenTheTaskUpdateIsRetried(final EventType type) {
        final Fixture fixture = new Fixture(type);
        final TaskGroupQueueDao queueDao = fixture.useRealTaskGroupCoordinator();
        final TaskGroupQueue queue = new TaskGroupQueue();
        queue.setId(10);
        final AtomicBoolean allocated = new AtomicBoolean(true);
        when(queueDao.queryByTaskInstanceId(1)).thenAnswer(invocation -> allocated.get()
                ? Collections.singletonList(queue)
                : Collections.emptyList());
        when(queueDao.deleteById(queue)).thenAnswer(invocation -> allocated.getAndSet(false));
        fixture.fail.set(true);
        assertThatThrownBy(fixture.deliver::run).isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(allocated).isFalse();
        fixture.fail.set(false);
        fixture.deliver.run();
        verify(queueDao, times(2)).queryByTaskInstanceId(1);
        verify(queueDao).deleteById(queue);
        fixture.assertPersistedResult();
        fixture.assertAck(1);
    }

    private static List<Object> fields(final TaskInstance instance) {
        return Arrays.asList(instance.getState(), instance.getHost(), instance.getStartTime(),
                instance.getEndTime(), instance.getLogPath(), instance.getAppLink(), instance.getVarPool());
    }

    private static final class Fixture {

        private static final Date EVENT_TIME = new Date(1_000L);
        private final TaskInstanceDao dao = mock(TaskInstanceDao.class);
        private final ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
        private final TaskExecutorClient client = mock(TaskExecutorClient.class);
        private final WorkerGroupDispatcherCoordinator dispatcher = mock(WorkerGroupDispatcherCoordinator.class);
        private final ITaskExecution task = mock(ITaskExecution.class);
        private final IWorkflowExecution workflow = mock(IWorkflowExecution.class);
        private final IWorkflowExecutionGraph graph = mock(IWorkflowExecutionGraph.class);
        private final WorkflowEventBus bus = spy(new WorkflowEventBus());
        private final TaskInstance shared = new TaskInstance();
        private final TaskInstance persisted = new TaskInstance();
        private final AtomicBoolean fail = new AtomicBoolean();
        private final AtomicInteger attempts = new AtomicInteger();
        private final EventType type;
        private final TaskStateActionFactory factory;
        private Runnable deliver;
        private AbstractTaskLifecycleEventHandler<?> handler;
        private AbstractTaskLifecycleEvent event;

        private Fixture(final EventType type) {
            this.type = type;
            shared.setId(1);
            shared.setName("task-persistence-recovery");
            shared.setState(type == EventType.DISPATCHED || type == EventType.DELAY
                    ? TaskExecutionStatus.SUBMITTED_SUCCESS
                    : type == EventType.RUNNING ? TaskExecutionStatus.DISPATCH : TaskExecutionStatus.RUNNING_EXECUTION);
            shared.setHost("old-worker:1234");
            shared.setLogPath("old-log");
            shared.setAppLink("old-context");
            shared.setVarPool("[]");
            shared.setFirstSubmitTime(new Date());
            shared.setDelayTime(10);
            BeanUtils.copyProperties(shared, persisted);
            final WorkflowInstance workflowInstance = new WorkflowInstance();
            workflowInstance.setId(1);
            workflowInstance.setVarPool("[]");
            when(task.getTaskInstance()).thenReturn(shared);
            when(task.getId()).thenReturn(1);
            when(task.getWorkflowEventBus()).thenReturn(bus);
            when(task.getWorkflowExecutionGraph()).thenReturn(graph);
            when(workflow.getWorkflowInstance()).thenReturn(workflowInstance);
            when(workflow.getWorkflowEventBus()).thenReturn(bus);
            when(dao.updateById(any(TaskInstance.class))).thenAnswer(invocation -> {
                attempts.incrementAndGet();
                if (fail.get()) {
                    throw new DataAccessResourceFailureException("synthetic task update failure");
                }
                BeanUtils.copyProperties(invocation.getArgument(0), persisted);
                return true;
            });
            final List<ITaskStateAction> actions = new ArrayList<>();
            final List<AbstractTaskStateAction> realActions = Arrays.asList(new TaskSubmittedStateAction(),
                    new TaskDispatchStateAction(), new TaskRunningStateAction(), new TaskPauseStateAction(),
                    new TaskKillStateAction(), new TaskFailureStateAction(), new TaskDelayExecutionStateAction());
            for (TaskExecutionStatus state : TaskExecutionStatus.values()) {
                final AbstractTaskStateAction real = realActions.stream().filter(a -> a.matchState() == state)
                        .findFirst().orElse(null);
                if (real != null) {
                    real.taskInstanceDao = dao;
                    real.taskGroupCoordinator = taskGroup;
                    if (real instanceof TaskSubmittedStateAction) {
                        // Also initialize the shadowing field on the unmodified implementation for the red test.
                        ReflectionTestUtils.setField(real, "taskInstanceDao", dao);
                        ReflectionTestUtils.setField(real, "workerGroupDispatcherCoordinator", dispatcher);
                    }
                    actions.add(real);
                } else {
                    final ITaskStateAction action = mock(ITaskStateAction.class);
                    when(action.matchState()).thenReturn(state);
                    actions.add(action);
                }
            }
            factory = new TaskStateActionFactory(actions);
            switch (type) {
                case DISPATCHED:
                    bind(new TaskDispatchedLifecycleEventHandler(client),
                            new TaskDispatchedLifecycleEvent(task, "new-worker:1234"));
                    break;
                case RUNNING:
                    bind(new TaskRunningLifecycleEventHandler(client),
                            new TaskRunningLifecycleEvent(task, "new-log", EVENT_TIME));
                    break;
                case PAUSED:
                    bind(new TaskPausedLifecycleEventHandler(client), TaskPausedLifecycleEvent.of(task));
                    break;
                case KILLED:
                    bind(new TaskKilledLifecycleEventHandler(client), new TaskKilledLifecycleEvent(task, EVENT_TIME));
                    break;
                case FAILED:
                    bind(new TaskFailedLifecycleEventHandler(client), new TaskFailedLifecycleEvent(task, EVENT_TIME));
                    break;
                case FATAL:
                    bind(new TaskFatalLifecycleEventHandler(), new TaskFatalLifecycleEvent(task, EVENT_TIME));
                    break;
                case CONTEXT:
                    bind(new TaskRuntimeContextChangedLifecycleEventHandler(client),
                            new TaskRuntimeContextChangedEvent(task, "new-context"));
                    break;
                case DELAY:
                    bind(new TaskDispatchLifecycleEventHandler(), TaskDispatchLifecycleEvent.of(task));
                    break;
                default:
                    throw new IllegalArgumentException(type.name());
            }
        }

        private <T extends AbstractTaskLifecycleEvent> void bind(final AbstractTaskLifecycleEventHandler<T> handler,
                                                                 final T event) {
            this.handler = handler;
            this.event = event;
            ReflectionTestUtils.setField(handler, "taskStateActionFactory", factory);
            deliver = () -> handler.handle(workflow, event);
        }

        private TaskGroupQueueDao useRealTaskGroupCoordinator() {
            shared.setTaskGroupId(1);
            final TaskGroupQueueDao queueDao = mock(TaskGroupQueueDao.class);
            final TaskGroupCoordinator coordinator = new TaskGroupCoordinator();
            ReflectionTestUtils.setField(coordinator, "taskGroupQueueDao", queueDao);
            ((AbstractTaskStateAction) factory
                    .getTaskStateAction(TaskExecutionStatus.RUNNING_EXECUTION)).taskGroupCoordinator = coordinator;
            return queueDao;
        }

        private TaskExecutionStatus expectedState() {
            switch (type) {
                case DISPATCHED:
                    return TaskExecutionStatus.DISPATCH;
                case PAUSED:
                    return TaskExecutionStatus.PAUSE;
                case KILLED:
                    return TaskExecutionStatus.KILL;
                case FAILED:
                case FATAL:
                    return TaskExecutionStatus.FAILURE;
                case DELAY:
                    return TaskExecutionStatus.DELAY_EXECUTION;
                default:
                    return TaskExecutionStatus.RUNNING_EXECUTION;
            }
        }

        private void assertPersistedResult() {
            assertThat(shared.getState()).isEqualTo(expectedState());
            assertThat(fields(persisted)).isEqualTo(fields(shared));
            if (type == EventType.DISPATCHED) {
                assertThat(shared.getHost()).isEqualTo("new-worker:1234");
            } else if (type == EventType.RUNNING) {
                assertThat(shared.getStartTime()).isEqualTo(EVENT_TIME);
                assertThat(shared.getLogPath()).isEqualTo("new-log");
            } else if (type == EventType.CONTEXT) {
                assertThat(shared.getAppLink()).isEqualTo("new-context");
            } else if (type == EventType.KILLED || type == EventType.FAILED || type == EventType.FATAL) {
                assertThat(shared.getEndTime()).isEqualTo(EVENT_TIME);
            } else if (type == EventType.PAUSED) {
                assertThat(shared.getEndTime()).isNull();
                verify(graph).markTaskExecutionChainPause(task);
            } else if (type == EventType.DELAY) {
                verify(dispatcher).dispatchTask(eq(task), anyLong());
            }
        }

        private void assertAck(final int count) {
            verify(client, times(count)).ackTaskExecutorLifecycleEvent(eq(task),
                    any(ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck.class));
        }
    }
}
