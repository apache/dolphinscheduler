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

package org.apache.dolphinscheduler.task.executor.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;
import org.apache.dolphinscheduler.task.executor.container.ExclusiveThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorContainerConfig;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorLifecycleEventRemoteReporter;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class TaskExecutorFinalizationTest {

    @Test
    void shouldReleaseSuccessfulExecutorBeforeAck() {
        assertReleasedBeforeAck(TaskExecutorLifecycleEventType.SUCCESS);
    }

    @Test
    void shouldReleaseFailedExecutorBeforeAck() {
        assertReleasedBeforeAck(TaskExecutorLifecycleEventType.FAILED);
    }

    @Test
    void shouldReleaseKilledExecutorBeforeAck() {
        assertReleasedBeforeAck(TaskExecutorLifecycleEventType.KILLED);
    }

    @Test
    void shouldReleasePausedExecutorBeforeAck() {
        assertReleasedBeforeAck(TaskExecutorLifecycleEventType.PAUSED);
    }

    @Test
    void shouldKeepExecutorIfReporterCannotAcceptItsResult() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        final ITaskExecutorLifecycleEventReporter reporter = mock(ITaskExecutorLifecycleEventReporter.class);
        doThrow(new IllegalStateException("cannot enqueue result")).when(reporter)
                .reportTaskExecutorLifecycleEvent(any());
        try (final SingleSlotContainer container = new SingleSlotContainer()) {
            container.dispatch(executor);
            repository.put(executor);
            final TaskExecutorLifecycleEventListener listener =
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter);

            assertThrows(IllegalStateException.class,
                    () -> listener.onTaskExecutorSuccessLifecycleEvent(TaskExecutorSuccessLifecycleEvent.of(executor)));

            assertTrue(repository.contains(7));
            assertEquals(1.0, container.slotUsage());
            verify(executor, never()).finalizeTask();
        }
    }

    @Test
    void shouldRetrySuccessfulResultAfterEnqueueFailure() {
        assertRetriedAfterEnqueueFailure(TaskExecutorState.SUCCEEDED);
    }

    @Test
    void shouldRetryFailedResultAfterEnqueueFailure() {
        assertRetriedAfterEnqueueFailure(TaskExecutorState.FAILED);
    }

    @Test
    void shouldRetryKilledResultAfterEnqueueFailure() {
        assertRetriedAfterEnqueueFailure(TaskExecutorState.KILLED);
    }

    @Test
    void shouldRetryPausedResultAfterEnqueueFailure() {
        assertRetriedAfterEnqueueFailure(TaskExecutorState.PAUSED);
    }

    @Test
    void shouldNotReplayResultAfterFinalizationFailure() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        doThrow(new IllegalStateException("cannot finalize task")).when(executor).finalizeTask();
        try (
                final SingleSlotContainer container = new SingleSlotContainer();
                final TaskExecutorLifecycleEventRemoteReporter reporter =
                        spy(new TaskExecutorLifecycleEventRemoteReporter(
                                "test-reporter", (host, event) -> {
                                }, repository, Duration.ofHours(6)));
                final TaskExecutorEventBusCoordinator coordinator =
                        new TaskExecutorEventBusCoordinator("test-coordinator", repository)) {
            container.dispatch(executor);
            repository.put(executor);
            coordinator.registerTaskExecutorLifecycleEventListener(
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter));
            executor.getTaskExecutorEventBus().publish(TaskExecutorSuccessLifecycleEvent.of(executor));
            coordinator.start();

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(executor).finalizeTask());
            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertTrue(executor.getTaskExecutorEventBus().isEmpty());
                verify(reporter).reportTaskExecutorLifecycleEvent(any());
                verify(executor).finalizeTask();
            });
            assertFalse(repository.contains(7));
            assertTrue(reporter.getEventChannels().containsKey(7));
        }
    }

    @Test
    void shouldNotRetryNonTerminalEventAfterEnqueueFailure() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        final ITaskExecutorLifecycleEventReporter reporter = mock(ITaskExecutorLifecycleEventReporter.class);
        doThrow(new IllegalStateException("cannot enqueue event")).when(reporter)
                .reportTaskExecutorLifecycleEvent(any());
        try (
                final SingleSlotContainer container = new SingleSlotContainer();
                final TaskExecutorEventBusCoordinator coordinator =
                        new TaskExecutorEventBusCoordinator("test-coordinator", repository)) {
            container.dispatch(executor);
            repository.put(executor);
            coordinator.registerTaskExecutorLifecycleEventListener(
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter));
            executor.getTaskExecutorEventBus().publish(TaskExecutorStartedLifecycleEvent.of(executor));
            coordinator.start();

            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> verify(reporter).reportTaskExecutorLifecycleEvent(any()));
            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertTrue(executor.getTaskExecutorEventBus().isEmpty());
                verify(reporter).reportTaskExecutorLifecycleEvent(any());
            });
            assertTrue(repository.contains(7));
            verify(executor, never()).finalizeTask();
        }
    }

    private void assertRetriedAfterEnqueueFailure(final TaskExecutorState state) {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        when(executor.isStarted()).thenReturn(true);
        when(executor.trackTaskExecutorState()).thenReturn(state);
        final AtomicBoolean acceptResult = new AtomicBoolean();
        final List<IReportableTaskExecutorLifecycleEvent> attempts = new CopyOnWriteArrayList<>();
        try (
                final SingleSlotContainer container = new SingleSlotContainer();
                final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                        "test-reporter", (host, event) -> {
                        }, repository, Duration.ofHours(6));
                final TaskExecutorEventBusCoordinator coordinator =
                        new TaskExecutorEventBusCoordinator("test-coordinator", repository)) {
            final ITaskExecutorLifecycleEventReporter failingReporter = mock(ITaskExecutorLifecycleEventReporter.class);
            doAnswer(invocation -> {
                final IReportableTaskExecutorLifecycleEvent event = invocation.getArgument(0);
                attempts.add(event);
                if (!acceptResult.get()) {
                    throw new IllegalStateException("cannot enqueue result");
                }
                reporter.reportTaskExecutorLifecycleEvent(event);
                return null;
            }).when(failingReporter).reportTaskExecutorLifecycleEvent(any());
            container.dispatch(executor);
            repository.put(executor);
            coordinator.registerTaskExecutorLifecycleEventListener(
                    new TaskExecutorLifecycleEventListener(() -> container, repository, failingReporter));
            coordinator.start();
            container.start(executor);

            // The worker has stopped tracking, but the coordinator must retain the result for another attempt.
            await().atMost(Duration.ofSeconds(5)).until(() -> attempts.size() >= 2);
            assertEquals(0, container.getTaskExecutorWorkers().getWorkerById(0).getFiredTaskExecutorSize());
            assertTrue(repository.contains(7));
            assertEquals(1.0, container.slotUsage());
            verify(executor, never()).finalizeTask();
            final IReportableTaskExecutorLifecycleEvent originalEvent = attempts.get(0);
            final long eventCreateTime = originalEvent.getEventCreateTime();
            final long endTime = executor.getTaskExecutionContext().getEndTime();

            acceptResult.set(true);
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertFalse(repository.contains(7));
                assertEquals(0.0, container.slotUsage());
                verify(executor).finalizeTask();
            });
            for (final IReportableTaskExecutorLifecycleEvent event : attempts) {
                assertSame(originalEvent, event);
                assertEquals(eventCreateTime, event.getEventCreateTime());
            }
            assertEquals(endTime, executor.getTaskExecutionContext().getEndTime());
            verify(executor).trackTaskExecutorState();
            verify(executor, never()).start();
            assertSame(originalEvent, reporter.getEventChannels().get(7).peek());
            assertTrue(executor.getTaskExecutorEventBus().isEmpty());

            final ITaskExecutor nextExecutor = executor(8);
            when(nextExecutor.isStarted()).thenReturn(false, true);
            when(nextExecutor.trackTaskExecutorState()).thenReturn(TaskExecutorState.RUNNING);
            container.dispatch(nextExecutor);
            container.start(nextExecutor);
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(nextExecutor).start());
            assertEquals(1.0, container.slotUsage());
        }
    }

    private void assertReleasedBeforeAck(final TaskExecutorLifecycleEventType type) {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        try (
                final SingleSlotContainer container = new SingleSlotContainer();
                final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                        "test-reporter", (host, event) -> {
                        }, repository, Duration.ofHours(1))) {
            container.dispatch(executor);
            repository.put(executor);
            final TaskExecutorLifecycleEventListener listener =
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter);
            switch (type) {
                case SUCCESS:
                    listener.onTaskExecutorSuccessLifecycleEvent(TaskExecutorSuccessLifecycleEvent.of(executor));
                    break;
                case FAILED:
                    listener.onTaskExecutorFailLifecycleEvent(TaskExecutorFailedLifecycleEvent.of(executor));
                    break;
                case KILLED:
                    listener.onTaskExecutorKilledLifecycleEvent(TaskExecutorKilledLifecycleEvent.of(executor));
                    break;
                case PAUSED:
                    listener.onTaskExecutorPausedLifecycleEvent(TaskExecutorPausedLifecycleEvent.of(executor));
                    break;
                default:
                    throw new IllegalArgumentException(type.name());
            }
            assertFalse(repository.contains(7));
            assertEquals(0.0, container.slotUsage());
            assertTrue(container.getTaskExecutorAssignmentTable().isEmpty());
            assertTrue(reporter.getEventChannels().containsKey(7));
            verify(executor).finalizeTask();

            // The only slot is usable while the first result is still unacknowledged.
            final ITaskExecutor nextExecutor = executor(8);
            when(nextExecutor.isStarted()).thenReturn(false, true);
            when(nextExecutor.trackTaskExecutorState()).thenReturn(TaskExecutorState.RUNNING);
            container.dispatch(nextExecutor);
            repository.put(nextExecutor);
            container.start(nextExecutor);
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(nextExecutor).start());
            assertEquals(1.0, container.slotUsage());
            reporter.receiveTaskExecutorLifecycleEventACK(new TaskExecutorLifecycleEventAck(7, type));
            reporter.receiveTaskExecutorLifecycleEventACK(new TaskExecutorLifecycleEventAck(7, type));
            assertTrue(reporter.getEventChannels().isEmpty());
            assertEquals(1.0, container.slotUsage());
            assertTrue(repository.contains(8));
            verify(executor).finalizeTask();
            verify(nextExecutor, never()).finalizeTask();
            assertTrue(executor.getTaskExecutorEventBus().isEmpty());
        }
    }

    private ITaskExecutor executor(final int id) {
        final TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId(id);
        context.setWorkflowInstanceId(101);
        context.setWorkflowInstanceHost("master:5678");
        context.setHost("worker:1234");
        final ITaskExecutor executor = mock(ITaskExecutor.class);
        when(executor.getId()).thenReturn(id);
        when(executor.getTaskExecutionContext()).thenReturn(context);
        when(executor.getTaskExecutorEventBus()).thenReturn(new TaskExecutorEventBus());
        return executor;
    }

    private static class SingleSlotContainer extends ExclusiveThreadTaskExecutorContainer implements AutoCloseable {

        private SingleSlotContainer() {
            super(TaskExecutorContainerConfig.builder().containerName("test-container").taskExecutorThreadPoolSize(1)
                    .build());
        }

        @Override
        public void close() {
            taskExecutorThreadPool.shutdownNow();
        }
    }
}
