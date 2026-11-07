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

package org.apache.dolphinscheduler.task.executor.eventbus;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskExecutorLifecycleEventRemoteReporterTest {

    private static final int TASK_ID = 7;
    private final TaskExecutorRepository repository = new TaskExecutorRepository();
    private TaskExecutorLifecycleEventRemoteReporter reporter;

    @BeforeEach
    void setUp() {
        final ITaskExecutor executor = mock(ITaskExecutor.class);
        final TaskExecutionContext context = new TaskExecutionContext();
        context.setWorkflowInstanceHost("master:5678");
        when(executor.getId()).thenReturn(TASK_ID);
        when(executor.getTaskExecutionContext()).thenReturn(context);
        repository.put(executor);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (reporter != null) {
            reporter.close();
            reporter.join(5000);
            assertFalse(reporter.isAlive());
        }
    }

    @Test
    void shouldRetryAfterExecutorRemovalAndUseReassignedHost() {
        final List<String> destinations = new CopyOnWriteArrayList<>();
        reporter = reporter(Duration.ofHours(1), (host, event) -> {
            event.setLatestReportTime(System.currentTimeMillis());
            destinations.add(host);
            if (host.equals("new-master:5678")) {
                reporter.receiveTaskExecutorLifecycleEventACK(
                        new TaskExecutorLifecycleEventAck(TASK_ID, event.getType()));
            }
        });
        reporter.reportTaskExecutorLifecycleEvent(successEvent());
        repository.remove(TASK_ID);
        reporter.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> destinations.contains("master:5678"));
        assertTrue(reporter.getEventChannels().containsKey(TASK_ID));
        assertTrue(reporter.onWorkflowInstanceHostChanged(TASK_ID, "new-master:5678"));
        await().atMost(Duration.ofSeconds(5)).until(() -> reporter.getEventChannels().isEmpty());
        assertTrue(destinations.contains("new-master:5678"));
        assertFalse(reporter.onWorkflowInstanceHostChanged(TASK_ID, "another-master:5678"));
        // Duplicate and late ACKs are harmless after the pending report is removed.
        reporter.receiveTaskExecutorLifecycleEventACK(
                new TaskExecutorLifecycleEventAck(TASK_ID, TaskExecutorLifecycleEventType.SUCCESS));
        assertTrue(reporter.getEventChannels().isEmpty());
    }

    @Test
    void shouldAcceptTerminalEventAfterAcknowledgedChannelIsRemoved() {
        reporter = reporter(Duration.ofHours(6), (host, event) -> {
        });
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorDispatchedLifecycleEvent.builder()
                .taskInstanceId(TASK_ID).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.DISPATCHED)
                .build());
        reporter.receiveTaskExecutorLifecycleEventACK(
                new TaskExecutorLifecycleEventAck(TASK_ID, TaskExecutorLifecycleEventType.DISPATCHED));
        assertTrue(reporter.getEventChannels().isEmpty());
        assertTrue(repository.contains(TASK_ID));

        final TaskExecutorSuccessLifecycleEvent result = successEvent();
        reporter.reportTaskExecutorLifecycleEvent(result);

        assertSame(result, reporter.getEventChannels().get(TASK_ID).peek());
    }

    @Test
    void shouldNotCreateChannelWithoutExecutor() {
        reporter = reporter(Duration.ofHours(6), (host, event) -> {
        });
        repository.remove(TASK_ID);

        assertThrows(IllegalArgumentException.class, () -> reporter.reportTaskExecutorLifecycleEvent(successEvent()));

        assertFalse(repository.contains(TASK_ID));
        assertTrue(reporter.getEventChannels().isEmpty());
    }

    @Test
    void shouldExpireFinishedChannelEvenWhenAnEarlierEventIsUnacknowledged() {
        final AtomicInteger attempts = new AtomicInteger();
        reporter = reporter(Duration.ofSeconds(1), (host, event) -> {
            event.setLatestReportTime(System.currentTimeMillis());
            attempts.incrementAndGet();
            throw new IllegalStateException("master unavailable");
        });
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorStartedLifecycleEvent.builder()
                .taskInstanceId(TASK_ID).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.RUNNING).build());
        reporter.reportTaskExecutorLifecycleEvent(successEvent());
        repository.remove(TASK_ID);
        reporter.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> reporter.getEventChannels().isEmpty());
        assertEquals(1, attempts.get());
    }

    @Test
    void shouldExpireFromTerminalEventCreationTime() {
        final AtomicInteger attempts = new AtomicInteger();
        reporter = reporter(Duration.ofHours(6), (host, event) -> {
            event.setLatestReportTime(System.currentTimeMillis());
            attempts.incrementAndGet();
        });
        final long oldCreateTime = System.currentTimeMillis() - Duration.ofHours(7).toMillis();
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorDispatchedLifecycleEvent.builder()
                .taskInstanceId(TASK_ID).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.DISPATCHED)
                .eventCreateTime(oldCreateTime).build());
        final TaskExecutorSuccessLifecycleEvent result = successEvent();
        result.setEventCreateTime(oldCreateTime);
        reporter.reportTaskExecutorLifecycleEvent(result);
        repository.remove(TASK_ID);
        reporter.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> reporter.getEventChannels().isEmpty());
        assertEquals(0, attempts.get());
    }

    @Test
    void shouldPreserveEarlierEventsUntilTerminalResultIsAcknowledged() {
        final List<TaskExecutorLifecycleEventType> deliveredEvents = new CopyOnWriteArrayList<>();
        final AtomicBoolean acknowledge = new AtomicBoolean();
        reporter = reporter(Duration.ofHours(6), (host, event) -> {
            final boolean shouldAcknowledge = acknowledge.get();
            event.setLatestReportTime(System.currentTimeMillis());
            deliveredEvents.add(event.getType());
            if (shouldAcknowledge) {
                reporter.receiveTaskExecutorLifecycleEventACK(
                        new TaskExecutorLifecycleEventAck(TASK_ID, event.getType()));
            }
        });
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorDispatchedLifecycleEvent.builder()
                .taskInstanceId(TASK_ID).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.DISPATCHED)
                .eventCreateTime(System.currentTimeMillis() - Duration.ofHours(7).toMillis()).build());
        reporter.reportTaskExecutorLifecycleEvent(successEvent());
        repository.remove(TASK_ID);
        reporter.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> !deliveredEvents.isEmpty());
        assertEquals(TaskExecutorLifecycleEventType.DISPATCHED,
                reporter.getEventChannels().get(TASK_ID).peek().getType());
        acknowledge.set(true);
        reporter.onWorkflowInstanceHostChanged(TASK_ID, "new-master:5678");

        await().atMost(Duration.ofSeconds(5)).until(() -> reporter.getEventChannels().isEmpty());
        assertEquals(Arrays.asList(TaskExecutorLifecycleEventType.DISPATCHED, TaskExecutorLifecycleEventType.DISPATCHED,
                TaskExecutorLifecycleEventType.SUCCESS), deliveredEvents);
    }

    @Test
    void shouldNotExtendTerminalEventDeadlineWhenMasterChanges() {
        reporter =
                reporter(Duration.ofSeconds(1), (host, event) -> event.setLatestReportTime(System.currentTimeMillis()));
        reporter.reportTaskExecutorLifecycleEvent(successEvent());
        repository.remove(TASK_ID);
        reporter.start();

        await().pollInterval(Duration.ofMillis(10)).atMost(Duration.ofSeconds(5)).until(() -> {
            reporter.onWorkflowInstanceHostChanged(TASK_ID, "new-master:5678");
            return reporter.getEventChannels().isEmpty();
        });
    }

    @Test
    void shouldNotExpireAnExecutingTask() {
        reporter =
                reporter(Duration.ofMillis(50), (host, event) -> event.setLatestReportTime(System.currentTimeMillis()));
        reporter.reportTaskExecutorLifecycleEvent(TaskExecutorStartedLifecycleEvent.builder()
                .taskInstanceId(TASK_ID).workflowInstanceId(101).type(TaskExecutorLifecycleEventType.RUNNING)
                .eventCreateTime(System.currentTimeMillis() - Duration.ofHours(7).toMillis()).build());
        reporter.start();

        await().during(Duration.ofMillis(200)).atMost(Duration.ofSeconds(5))
                .until(() -> reporter.getEventChannels().containsKey(TASK_ID));
        reporter.receiveTaskExecutorLifecycleEventACK(
                new TaskExecutorLifecycleEventAck(TASK_ID, TaskExecutorLifecycleEventType.RUNNING));
        assertTrue(reporter.getEventChannels().isEmpty());
    }

    @Test
    void shouldRejectInvalidRetentionDurations() {
        for (final Duration duration : new Duration[]{null, Duration.ZERO, Duration.ofSeconds(-1)}) {
            assertThrows(IllegalArgumentException.class, () -> reporter(duration, (host, event) -> {
            }));
        }
    }

    private TaskExecutorLifecycleEventRemoteReporter reporter(final Duration duration,
                                                              final ITaskExecutorEventRemoteReporterClient client) {
        return new TaskExecutorLifecycleEventRemoteReporter("test-reporter", client, repository, duration);
    }

    private TaskExecutorSuccessLifecycleEvent successEvent() {
        return TaskExecutorSuccessLifecycleEvent.builder().taskInstanceId(TASK_ID).workflowInstanceId(101)
                .taskInstanceHost("worker:1234").type(TaskExecutorLifecycleEventType.SUCCESS).build();
    }
}
