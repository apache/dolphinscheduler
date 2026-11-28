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

package org.apache.dolphinscheduler.task.executor;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.container.ExclusiveThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorContainerConfig;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorLifecycleEventRemoteReporter;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorCapacityExceededException;
import org.apache.dolphinscheduler.task.executor.listener.TaskExecutorLifecycleEventListener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class TaskEngineDispatchTest {

    @Test
    void shouldNotCancelAnOlderRegistrationWhenNoSlotCanBeAllocated() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        try (final Container container = new Container()) {
            final TaskEngine engine = engine(repository, container);
            final Executor original = new Executor(7);
            engine.submitTask(original);
            for (int id = 8; id < 11; id++) {
                engine.submitTask(new Executor(id));
            }
            // Finalization removes the repository entry before it releases the container registration.
            repository.remove(7);

            assertThrows(TaskExecutorCapacityExceededException.class, () -> engine.submitTask(new Executor(7)));

            assertEquals(4, registeredExecutors(container));
            assertEquals(1.0, container.slotUsage());
            assertEquals(true, container.getTaskExecutorAssignmentTable().isTaskExecutorRegistered(original));
        }
    }

    @Test
    void shouldNotReplaceCompletedExecutorWhileItsResultIsPending() throws Exception {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final TaskExecutorLifecycleEventRemoteReporter reporter = new TaskExecutorLifecycleEventRemoteReporter(
                "test-reporter", (host, event) -> event.setLatestReportTime(System.currentTimeMillis()), repository);
        final TaskExecutorEventBusCoordinator coordinator = new TaskExecutorEventBusCoordinator("test", repository);
        try (final Container container = new Container()) {
            coordinator.registerTaskExecutorLifecycleEventListener(
                    new TaskExecutorLifecycleEventListener(() -> container, repository, reporter));
            final TaskEngine engine = new TaskEngine(TaskEngineBuilder.builder()
                    .engineName("test-engine").taskExecutorRepository(repository)
                    .taskExecutorContainerDelegator(() -> container)
                    .taskExecutorEventBusCoordinator(coordinator).build());
            engine.start();
            reporter.start();
            final Executor original = new Executor(7) {

                @Override
                protected TaskExecutorState doTrackTaskPluginStatus() {
                    return TaskExecutorState.SUCCEEDED;
                }
            };
            engine.submitTask(original);
            await().atMost(Duration.ofSeconds(5)).until(() -> container.getTaskExecutorWorkers().getWorkers().stream()
                    .allMatch(worker -> worker.getFiredTaskExecutorSize() == 0)
                    && reporter.getEventChannels().containsKey(7));

            engine.submitTask(new Executor(7));

            assertSame(original, repository.get(7).get());
            assertEquals(1, registeredExecutors(container));
        } finally {
            reporter.close();
            reporter.interrupt();
            reporter.join(5000);
            coordinator.close();
        }
    }

    @Test
    void shouldNotReplaceAnAcceptedExecutorOnDuplicateDispatch() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        try (final Container container = new Container()) {
            final TaskEngine engine = engine(repository, container);
            final Executor original = new Executor(7);
            final Executor duplicate = new Executor(7);
            engine.submitTask(original);
            await().atMost(Duration.ofSeconds(5)).until(() -> original.starts.get() == 1);

            engine.submitTask(duplicate);

            assertSame(original, repository.get(7).get());
            assertEquals(1, registeredExecutors(container));
            assertEquals(0, duplicate.starts.get());
        }
    }

    @Test
    void shouldAcceptOnlyOneOfConcurrentDuplicateDispatches() throws Exception {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        final ExecutorService requests = Executors.newFixedThreadPool(4);
        try (final Container container = new Container()) {
            final TaskEngine engine = engine(repository, container);
            final CountDownLatch start = new CountDownLatch(1);
            final List<Executor> executors = new ArrayList<>();
            final List<Future<?>> submissions = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                final Executor executor = new Executor(7);
                executors.add(executor);
                submissions.add(requests.submit(() -> {
                    start.await();
                    engine.submitTask(executor);
                    return null;
                }));
            }
            start.countDown();
            for (final Future<?> submission : submissions) {
                submission.get(5, TimeUnit.SECONDS);
            }

            assertEquals(1, registeredExecutors(container));
            await().atMost(Duration.ofSeconds(5)).untilAsserted(
                    () -> assertEquals(1, executors.stream().mapToInt(executor -> executor.starts.get()).sum()));
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void shouldReleaseAdmissionWhenStartingExecutorFails() {
        final TaskExecutorRepository repository = new TaskExecutorRepository();
        try (final Container container = new Container() {

            private boolean fail = true;

            @Override
            public void start(final ITaskExecutor executor) {
                if (fail) {
                    fail = false;
                    throw new IllegalStateException("cannot start executor");
                }
                super.start(executor);
            }
        }) {
            final TaskEngine engine = engine(repository, container);

            assertThrows(IllegalStateException.class, () -> engine.submitTask(new Executor(7)));
            assertFalse(repository.contains(7));
            assertEquals(0, registeredExecutors(container));
            assertEquals(0.0, container.slotUsage());

            final Executor retry = new Executor(7);
            engine.submitTask(retry);
            assertSame(retry, repository.get(7).get());
            await().atMost(Duration.ofSeconds(5)).until(() -> retry.starts.get() == 1);
            assertEquals(1, registeredExecutors(container));
        }
    }

    private TaskEngine engine(final TaskExecutorRepository repository, final ITaskExecutorContainer container) {
        return new TaskEngine(TaskEngineBuilder.builder()
                .engineName("test-engine")
                .taskExecutorRepository(repository)
                .taskExecutorContainerDelegator(() -> container)
                .taskExecutorEventBusCoordinator(mock(ITaskExecutorEventBusCoordinator.class))
                .build());
    }

    private int registeredExecutors(final Container container) {
        return container.getTaskExecutorWorkers().getWorkers().stream()
                .mapToInt(worker -> worker.getRegisteredTaskExecutorSize()).sum();
    }

    private static class Container extends ExclusiveThreadTaskExecutorContainer implements AutoCloseable {

        private Container() {
            super(TaskExecutorContainerConfig.builder().containerName("test-container")
                    .taskExecutorThreadPoolSize(4).build());
        }

        @Override
        public void close() {
            taskExecutorThreadPool.shutdownNow();
        }
    }

    private static class Executor extends AbstractTaskExecutor {

        private final AtomicInteger starts = new AtomicInteger();

        private Executor(final int taskId) {
            super(context(taskId), new TaskExecutorEventBus());
        }

        private static TaskExecutionContext context(final int taskId) {
            final TaskExecutionContext context = new TaskExecutionContext();
            context.setTaskInstanceId(taskId);
            context.setWorkflowInstanceId(101);
            context.setWorkflowInstanceHost("master:5678");
            return context;
        }

        @Override
        protected void initializeTaskPlugin() {
        }

        @Override
        protected void doTriggerTaskPlugin() {
            starts.incrementAndGet();
        }

        @Override
        protected TaskExecutorState doTrackTaskPluginStatus() {
            return TaskExecutorState.RUNNING;
        }

        @Override
        public void pause() {
        }

        @Override
        public void kill() {
        }

        @Override
        public void finalizeTask() {
        }
    }
}
