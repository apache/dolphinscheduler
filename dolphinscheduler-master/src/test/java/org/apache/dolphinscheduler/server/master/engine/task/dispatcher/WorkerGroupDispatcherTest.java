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

package org.apache.dolphinscheduler.server.master.engine.task.dispatcher;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.TaskDispatchPolicy;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.NoAvailableWorkerException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.WorkerGroupNotFoundException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class WorkerGroupDispatcherTest {

    private WorkerGroupDispatcher dispatcher;
    private ITaskExecutorClient taskExecutorClient;

    @BeforeEach
    void setUp() {
        taskExecutorClient = mock(ITaskExecutorClient.class);
        final MasterConfig masterConfig = new MasterConfig();
        dispatcher =
                new WorkerGroupDispatcher("TestGroup", taskExecutorClient, masterConfig.getTaskDispatchPolicy());
    }

    @Test
    void dispatchTask() {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecutionRunnable, 0);
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable));
    }

    @Test
    void dispatchTask_withDelay() {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecutionRunnable, 2000);
        await()
                .atLeast(Duration.ofMillis(1500))
                .untilAsserted(() -> verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable));
    }

    @Test
    void dispatchTask_withOneDelayTaskAnotherIsDispatchRetryTask() throws TaskDispatchException {
        final ITaskExecutionRunnable delayTaskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        final TaskInstance delayTaskInstance = mock(TaskInstance.class);
        when(delayTaskExecutionRunnable.getTaskInstance()).thenReturn(delayTaskInstance);
        when(delayTaskExecutionRunnable.getTaskExecutionContext()).thenReturn(TaskExecutionContext.builder().build());
        when(delayTaskExecutionRunnable.getId()).thenReturn(1);

        final ITaskExecutionRunnable dispatchRetryTaskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        final TaskInstance dispatchRetryTaskInstance = mock(TaskInstance.class);
        when(dispatchRetryTaskExecutionRunnable.getTaskInstance()).thenReturn(dispatchRetryTaskInstance);
        when(dispatchRetryTaskExecutionRunnable.getTaskExecutionContext())
                .thenReturn(TaskExecutionContext.builder().dispatchFailTimes(1).build());
        when(dispatchRetryTaskExecutionRunnable.getId()).thenReturn(2);
        dispatcher.start();

        dispatcher.dispatchTask(delayTaskExecutionRunnable, 2000);
        dispatcher.dispatchTask(dispatchRetryTaskExecutionRunnable, 100);
        await()
                .atLeast(Duration.ofMillis(1500))
                .untilAsserted(() -> verify(taskExecutorClient, times(2)).dispatch(any()));

        InOrder inOrder = inOrder(taskExecutorClient);
        inOrder.verify(taskExecutorClient).dispatch(dispatchRetryTaskExecutionRunnable);
        inOrder.verify(taskExecutorClient).dispatch(delayTaskExecutionRunnable);
    }

    @Test
    void dispatchTask_HasBeenRemoved() {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());

        dispatcher.dispatchTask(taskExecutionRunnable, 0);
        dispatcher.removeTask(taskExecutionRunnable);

        dispatcher.start();
        await()
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(taskExecutorClient, times(0)).dispatch(taskExecutionRunnable));
    }

    @Test
    void dispatch_TaskDispatchFails_RetryLogicWorks() throws TaskDispatchException {
        // Arrange
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());

        doThrow(new RuntimeException()).when(taskExecutorClient).dispatch(any());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        await()
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(taskExecutorClient, times(2)).dispatch(taskExecutionRunnable));
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        // Given
        ITaskExecutionRunnable task = mockTaskExecutionRunnableWithFirstDispatchTime(System.currentTimeMillis());
        WorkerGroupNotFoundException ex = new WorkerGroupNotFoundException("no worker group");
        doThrow(ex).when(taskExecutorClient).dispatch(task);

        dispatcher.start();
        dispatcher.dispatchTask(task, 0);

        // When & Then
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    // Ensure it's retrying
                    verify(taskExecutorClient, atLeast(2)).dispatch(task);

                    // Ensure NO event has been published during this time
                    WorkflowEventBus eventBus = task.getWorkflowEventBus();
                    verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        // Given
        TaskDispatchPolicy dispatchTimeoutCheckerConfig = new TaskDispatchPolicy();
        dispatchTimeoutCheckerConfig.setDispatchTimeoutFailedEnabled(true);
        dispatchTimeoutCheckerConfig.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, dispatchTimeoutCheckerConfig);

        ITaskExecutionRunnable taskExecutionRunnable = mockTaskExecutionRunnableWithFirstDispatchTime(
                System.currentTimeMillis() - 500);

        WorkerGroupNotFoundException ex = new WorkerGroupNotFoundException("worker group not found");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        // Then
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
                    WorkflowEventBus eventBus = taskExecutionRunnable.getWorkflowEventBus();
                    verify(eventBus).publish(argThat(evt -> evt instanceof TaskFailedLifecycleEvent &&
                            ((TaskFailedLifecycleEvent) evt).getTaskExecutionRunnable() == taskExecutionRunnable));
                });
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        // Given: Dispatcher configured with a 5-minute timeout (enabled)
        TaskDispatchPolicy dispatchTimeoutCheckerConfig = new TaskDispatchPolicy();
        dispatchTimeoutCheckerConfig.setDispatchTimeoutFailedEnabled(true);
        dispatchTimeoutCheckerConfig.setMaxTaskDispatchDuration(Duration.ofMinutes(5));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, dispatchTimeoutCheckerConfig);

        // Mock task with first dispatch time set to 100ms ago → well within timeout window
        ITaskExecutionRunnable taskExecutionRunnable = mockTaskExecutionRunnableWithFirstDispatchTime(
                System.currentTimeMillis() - 100);

        // Use CountDownLatch to reliably detect actual dispatch invocation
        CountDownLatch dispatchCalled = new CountDownLatch(1);

        // Stub client to throw WorkerGroupNotFoundException and signal the latch
        doAnswer(invocation -> {
            dispatchCalled.countDown(); // Confirm dispatch was attempted
            throw new WorkerGroupNotFoundException("Worker group 'TestGroup' does not exist");
        }).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        // When: Start dispatcher and dispatch the task
        dispatcher.start();
        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        // Wait up to 1 second for the dispatch attempt to complete
        boolean dispatched = dispatchCalled.await(1000, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(dispatched, "Expected dispatch() to be called within 1 second");

        // Then: Verify NO failure events are published because timeout has NOT been exceeded
        WorkflowEventBus eventBus = taskExecutionRunnable.getWorkflowEventBus();
        verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        // Given
        ITaskExecutionRunnable task = mockTaskExecutionRunnableWithFirstDispatchTime(System.currentTimeMillis());
        NoAvailableWorkerException ex = new NoAvailableWorkerException("no worker");
        doThrow(ex).when(taskExecutorClient).dispatch(task);

        dispatcher.start();
        dispatcher.dispatchTask(task, 0);

        // When & Then
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    // Ensure it's retrying
                    verify(taskExecutorClient, atLeast(2)).dispatch(task);

                    // Ensure NO event has been published during this time
                    WorkflowEventBus eventBus = task.getWorkflowEventBus();
                    verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        // Given: enable timeout (200ms), task already waited 500ms
        TaskDispatchPolicy dispatchTimeoutCheckerConfig = new TaskDispatchPolicy();
        dispatchTimeoutCheckerConfig.setDispatchTimeoutFailedEnabled(true);
        dispatchTimeoutCheckerConfig.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, dispatchTimeoutCheckerConfig);

        ITaskExecutionRunnable taskExecutionRunnable =
                mockTaskExecutionRunnableWithFirstDispatchTime(System.currentTimeMillis() - 500);

        NoAvailableWorkerException ex = new NoAvailableWorkerException("no worker");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        // Then
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
                    WorkflowEventBus eventBus = taskExecutionRunnable.getWorkflowEventBus();
                    verify(eventBus).publish(argThat(evt -> evt instanceof TaskFailedLifecycleEvent &&
                            ((TaskFailedLifecycleEvent) evt).getTaskExecutionRunnable() == taskExecutionRunnable));
                });
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        // Given: Configure dispatcher with a 5-minute dispatch timeout (enabled)
        TaskDispatchPolicy dispatchTimeoutCheckerConfig = new TaskDispatchPolicy();
        dispatchTimeoutCheckerConfig.setDispatchTimeoutFailedEnabled(true);
        dispatchTimeoutCheckerConfig.setMaxTaskDispatchDuration(Duration.ofMinutes(5));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, dispatchTimeoutCheckerConfig);

        // Mock task with first dispatch time set to 100ms ago → ensures it's NOT timed out yet
        ITaskExecutionRunnable taskExecutionRunnable = mockTaskExecutionRunnableWithFirstDispatchTime(
                System.currentTimeMillis() - 100);

        // Use CountDownLatch to reliably detect when dispatch is actually invoked (avoids timing flakiness)
        CountDownLatch dispatchCalled = new CountDownLatch(1);

        // Stub the client to throw NoAvailableWorkerException on dispatch and signal the latch
        doAnswer(invocation -> {
            dispatchCalled.countDown(); // Signal that dispatch was attempted
            throw new NoAvailableWorkerException("no worker");
        }).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        // When: Start dispatcher and trigger task dispatch
        dispatcher.start();
        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        // Wait up to 1 second for the dispatch attempt to occur (ensures async execution completes)
        boolean dispatched = dispatchCalled.await(1000, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(dispatched, "Expected dispatch() to be called within 1 second");

        // Then: Verify NO failure events are published since timeout has NOT been exceeded
        WorkflowEventBus eventBus = taskExecutionRunnable.getWorkflowEventBus();
        verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        // Given
        ITaskExecutionRunnable task = mockTaskExecutionRunnableWithFirstDispatchTime(System.currentTimeMillis());
        TaskDispatchException ex = new TaskDispatchException("generic dispatch error");
        doThrow(ex).when(taskExecutorClient).dispatch(task);

        dispatcher.start();
        dispatcher.dispatchTask(task, 0);

        // When & Then
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    // Ensure it's retrying
                    verify(taskExecutorClient, atLeast(2)).dispatch(task);

                    // Ensure NO event has been published during this time
                    WorkflowEventBus eventBus = task.getWorkflowEventBus();
                    verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        // Given
        TaskDispatchPolicy dispatchTimeoutCheckerConfig = new TaskDispatchPolicy();
        dispatchTimeoutCheckerConfig.setDispatchTimeoutFailedEnabled(true);
        dispatchTimeoutCheckerConfig.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, dispatchTimeoutCheckerConfig);

        ITaskExecutionRunnable taskExecutionRunnable = mockTaskExecutionRunnableWithFirstDispatchTime(
                System.currentTimeMillis() - 500);

        TaskDispatchException ex = new TaskDispatchException("generic dispatch error");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecutionRunnable, 0);

        // Then
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
                    WorkflowEventBus eventBus = taskExecutionRunnable.getWorkflowEventBus();
                    verify(eventBus).publish(argThat(evt -> evt instanceof TaskFailedLifecycleEvent &&
                            ((TaskFailedLifecycleEvent) evt).getTaskExecutionRunnable() == taskExecutionRunnable));
                });
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        // Given: Dispatcher configured with a 5-minute dispatch timeout (enabled)
        TaskDispatchPolicy config = new TaskDispatchPolicy();
        config.setDispatchTimeoutFailedEnabled(true);
        config.setMaxTaskDispatchDuration(Duration.ofMinutes(5));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, config);

        // Mock task with first dispatch time set to 100ms ago → well within timeout window
        ITaskExecutionRunnable task = mockTaskExecutionRunnableWithFirstDispatchTime(
                System.currentTimeMillis() - 100);

        // Use CountDownLatch to reliably detect when dispatch is actually invoked
        CountDownLatch dispatchCalled = new CountDownLatch(1);

        // Stub client to throw a generic TaskDispatchException and signal the latch
        doAnswer(invocation -> {
            dispatchCalled.countDown(); // Confirm dispatch attempt occurred
            throw new TaskDispatchException("Generic dispatch error");
        }).when(taskExecutorClient).dispatch(task);

        // When: Start dispatcher and trigger task dispatch
        dispatcher.start();
        dispatcher.dispatchTask(task, 0);

        // Wait up to 1 second for the dispatch attempt to complete (handles async execution)
        boolean dispatched = dispatchCalled.await(1000, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(dispatched, "Expected dispatch() to be called within 1 second");

        // Then: Verify NO failure events are published because timeout has NOT been exceeded
        WorkflowEventBus eventBus = task.getWorkflowEventBus();
        verify(eventBus, never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    private ITaskExecutionRunnable mockTaskExecutionRunnableWithFirstDispatchTime(long firstDispatchTime) {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        WorkflowInstance workflowInstance = mock(WorkflowInstance.class);
        WorkflowEventBus eventBus = mock(WorkflowEventBus.class);

        TaskExecutionContext context = mock(TaskExecutionContext.class);
        when(context.getFirstDispatchTime()).thenReturn(firstDispatchTime);

        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(taskExecutionRunnable.getWorkflowEventBus()).thenReturn(eventBus);
        when(taskExecutionRunnable.getId()).thenReturn(ThreadLocalRandom.current().nextInt(1000, 9999));
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(context);

        return taskExecutionRunnable;
    }
}
