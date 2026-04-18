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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.apache.dolphinscheduler.server.master.config.TaskDispatchPolicy;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.exception.dispatch.NoAvailableWorkerException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.WorkerGroupNotFoundException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class WorkerGroupDispatcherTest {

    private WorkerGroupDispatcher dispatcher;
    private ITaskExecutorClient taskExecutorClient;

    @BeforeEach
    void setUp() {
        taskExecutorClient = mock(ITaskExecutorClient.class);
        dispatcher =
                new WorkerGroupDispatcher("TestGroup", taskExecutorClient, new TaskDispatchPolicy());
    }

    @AfterEach
    void tearDown() {
        dispatcher.close();
        dispatcher.interrupt();
    }

    @Test
    void dispatchTask() {
        ITaskExecution taskExecution = mock(ITaskExecution.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecution, 0);
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(taskExecutorClient, times(1)).dispatch(taskExecution));
    }

    @Test
    void dispatchTask_withDelay() {
        ITaskExecution taskExecution = mock(ITaskExecution.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecution, 2000);
        await()
                .atLeast(Duration.ofMillis(1500))
                .untilAsserted(() -> verify(taskExecutorClient, times(1)).dispatch(taskExecution));
    }

    @Test
    void dispatchTask_withOneDelayTaskAnotherIsDispatchRetryTask() throws TaskDispatchException {
        final ITaskExecution delayTaskExecution = mock(ITaskExecution.class);
        final TaskInstance delayTaskInstance = mock(TaskInstance.class);
        when(delayTaskExecution.getTaskInstance()).thenReturn(delayTaskInstance);
        when(delayTaskExecution.getTaskExecutionContext()).thenReturn(TaskExecutionContext.builder().build());
        when(delayTaskExecution.getId()).thenReturn(1);

        final ITaskExecution dispatchRetryTaskExecution = mock(ITaskExecution.class);
        final TaskInstance dispatchRetryTaskInstance = mock(TaskInstance.class);
        when(dispatchRetryTaskExecution.getTaskInstance()).thenReturn(dispatchRetryTaskInstance);
        when(dispatchRetryTaskExecution.getTaskExecutionContext())
                .thenReturn(TaskExecutionContext.builder().dispatchFailTimes(1).build());
        when(dispatchRetryTaskExecution.getId()).thenReturn(2);
        dispatcher.start();

        dispatcher.dispatchTask(delayTaskExecution, 2000);
        dispatcher.dispatchTask(dispatchRetryTaskExecution, 100);
        await()
                .atLeast(Duration.ofMillis(1500))
                .untilAsserted(() -> verify(taskExecutorClient, times(2)).dispatch(any()));

        InOrder inOrder = inOrder(taskExecutorClient);
        inOrder.verify(taskExecutorClient).dispatch(dispatchRetryTaskExecution);
        inOrder.verify(taskExecutorClient).dispatch(delayTaskExecution);
    }

    @Test
    void dispatchTask_HasBeenRemoved() {
        ITaskExecution taskExecution = mock(ITaskExecution.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());

        dispatcher.dispatchTask(taskExecution, 0);
        dispatcher.removeTask(taskExecution);

        dispatcher.start();
        await()
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(taskExecutorClient, times(0)).dispatch(taskExecution));
    }

    @Test
    void dispatch_TaskDispatchFails_RetryLogicWorks() throws TaskDispatchException {
        // Arrange
        ITaskExecution taskExecution = mock(ITaskExecution.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());

        doThrow(new RuntimeException()).when(taskExecutorClient).dispatch(any());
        dispatcher.start();

        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(taskExecutorClient, times(2)).dispatch(taskExecution));
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis());
        WorkerGroupNotFoundException ex = new WorkerGroupNotFoundException("no worker group");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, atLeast(2)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus(), never())
                            .publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 500);
        WorkerGroupNotFoundException ex = new WorkerGroupNotFoundException("worker group not found");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus()).publish(
                            argThat(event -> event instanceof TaskFailedLifecycleEvent &&
                                    ((TaskFailedLifecycleEvent) event)
                                            .getTaskExecution() == taskExecution));
                });
    }

    @Test
    void dispatchTask_WorkerGroupNotFound_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMinutes(1));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 100);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            countDownLatch.countDown();
            throw new WorkerGroupNotFoundException("Worker group 'TestGroup' does not exist");
        }).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
        verify(taskExecution.getWorkflowEventBus(), never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis());
        NoAvailableWorkerException ex = new NoAvailableWorkerException("no worker");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, atLeast(2)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus(), never())
                            .publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 500);
        NoAvailableWorkerException ex = new NoAvailableWorkerException("no worker");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus()).publish(
                            argThat(event -> event instanceof TaskFailedLifecycleEvent &&
                                    ((TaskFailedLifecycleEvent) event)
                                            .getTaskExecution() == taskExecution));
                });
    }

    @Test
    void dispatchTask_NoAvailableWorker_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMinutes(1));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 100);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            countDownLatch.countDown();
            throw new NoAvailableWorkerException("no worker");
        }).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
        verify(taskExecution.getWorkflowEventBus(), never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutDisabled_ShouldKeepRetrying() throws TaskDispatchException {
        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis());
        TaskDispatchException ex = new TaskDispatchException("generic dispatch error");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, atLeast(2)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus(), never())
                            .publish(any(TaskFailedLifecycleEvent.class));
                });
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutEnabledAndExceeded_ShouldPublishFailedEvent() throws TaskDispatchException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMillis(200));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 500);
        TaskDispatchException ex = new TaskDispatchException("generic dispatch error");
        doThrow(ex).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(taskExecutorClient, times(1)).dispatch(taskExecution);
                    verify(taskExecution.getWorkflowEventBus()).publish(
                            argThat(event -> event instanceof TaskFailedLifecycleEvent &&
                                    ((TaskFailedLifecycleEvent) event)
                                            .getTaskExecution() == taskExecution));
                });
    }

    @Test
    void dispatchTask_GenericTaskDispatchException_TimeoutEnabledButNotExceeded_ShouldNotPublishAnyFailureEvent() throws TaskDispatchException, InterruptedException {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofMinutes(1));

        dispatcher = new WorkerGroupDispatcher("TestGroup", taskExecutorClient, taskDispatchPolicy);

        ITaskExecution taskExecution =
                mockTaskExecutionWithFirstDispatchTime(System.currentTimeMillis() - 100);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            countDownLatch.countDown();
            throw new TaskDispatchException("Generic dispatch error");
        }).when(taskExecutorClient).dispatch(taskExecution);

        dispatcher.start();
        dispatcher.dispatchTask(taskExecution, 0);

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
        verify(taskExecution.getWorkflowEventBus(), never()).publish(any(TaskFailedLifecycleEvent.class));
    }

    // Helper to mock TaskExecution with firstDispatchTime
    private ITaskExecution mockTaskExecutionWithFirstDispatchTime(long firstDispatchTime) {
        ITaskExecution taskExecution = mock(ITaskExecution.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        WorkflowInstance workflowInstance = mock(WorkflowInstance.class);
        WorkflowEventBus eventBus = mock(WorkflowEventBus.class);

        TaskExecutionContext context = mock(TaskExecutionContext.class);
        when(context.getFirstDispatchTime()).thenReturn(firstDispatchTime);

        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecution.getWorkflowInstance()).thenReturn(workflowInstance);
        when(taskExecution.getWorkflowEventBus()).thenReturn(eventBus);
        when(taskExecution.getId()).thenReturn(ThreadLocalRandom.current().nextInt(1000, 9999));
        when(taskExecution.getTaskExecutionContext()).thenReturn(context);

        return taskExecution;
    }
}
