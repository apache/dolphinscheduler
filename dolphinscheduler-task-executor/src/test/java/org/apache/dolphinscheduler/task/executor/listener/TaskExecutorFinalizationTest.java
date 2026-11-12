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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;
import org.apache.dolphinscheduler.task.executor.container.ExclusiveThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorContainerConfig;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorLifecycleEventRemoteReporter;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class TaskExecutorFinalizationTest {

    @Test
    void shouldReleaseSuccessfulExecutorWhenMasterIsUnavailable() throws InterruptedException {
        assertReleasedWhenMasterIsUnavailable(TaskExecutorState.SUCCEEDED, TaskExecutorLifecycleEventType.SUCCESS);
    }

    @Test
    void shouldReleaseFailedExecutorWhenMasterIsUnavailable() throws InterruptedException {
        assertReleasedWhenMasterIsUnavailable(TaskExecutorState.FAILED, TaskExecutorLifecycleEventType.FAILED);
    }

    @Test
    void shouldReleaseKilledExecutorWhenMasterIsUnavailable() throws InterruptedException {
        assertReleasedWhenMasterIsUnavailable(TaskExecutorState.KILLED, TaskExecutorLifecycleEventType.KILLED);
    }

    @Test
    void shouldReleasePausedExecutorWhenMasterIsUnavailable() throws InterruptedException {
        assertReleasedWhenMasterIsUnavailable(TaskExecutorState.PAUSED, TaskExecutorLifecycleEventType.PAUSED);
    }

    private void assertReleasedWhenMasterIsUnavailable(final TaskExecutorState state,
                                                       final TaskExecutorLifecycleEventType type) throws InterruptedException {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ITaskExecutor executor = executor(7);
        when(executor.isStarted()).thenReturn(true);
        when(executor.trackTaskExecutorState()).thenReturn(state);
        final AtomicReference<TaskExecutorLifecycleEventType> reportedType = new AtomicReference<>();
        final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                "test-reporter", (host, event) -> {
                    event.setLatestReportTime(System.currentTimeMillis());
                    reportedType.set(event.getType());
                    throw new IllegalStateException("master unavailable");
                }, repository, Duration.ofHours(6));
        try (
                final SingleSlotContainer container = new SingleSlotContainer();
                final TaskExecutorEventBusCoordinator coordinator =
                        new TaskExecutorEventBusCoordinator("test-coordinator", repository)) {
            container.dispatch(executor);
            repository.put(executor);
            coordinator.registerTaskExecutorLifecycleEventListener(
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter));
            reporter.start();
            coordinator.start();
            container.start(executor);

            // Remote failure must not prevent local acceptance and executor cleanup.
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertEquals(type, reportedType.get());
                assertFalse(repository.contains(7));
                assertEquals(0.0, container.slotUsage());
                verify(executor).finalizeTask();
            });
            assertTrue(container.getTaskExecutorAssignmentTable().isEmpty());
            assertEquals(0, container.getTaskExecutorWorkers().getWorkerById(0).getFiredTaskExecutorSize());
            assertEquals(type, reporter.getEventChannels().get(7).peek().getType());
            verify(executor).trackTaskExecutorState();
            verify(executor, never()).start();

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
        } finally {
            reporter.close();
            reporter.join(5000);
            assertFalse(reporter.isAlive());
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
