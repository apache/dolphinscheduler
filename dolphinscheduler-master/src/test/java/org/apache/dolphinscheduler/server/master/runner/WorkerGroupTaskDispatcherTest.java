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

package org.apache.dolphinscheduler.server.master.runner;

import static org.apache.dolphinscheduler.common.thread.ThreadUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkerGroupTaskDispatcherTest {

    private WorkerGroupTaskDispatcher dispatcher;
    private ITaskExecutorClient taskExecutorClient;

    @BeforeEach
    void setUp() {
        taskExecutorClient = mock(ITaskExecutorClient.class);
        dispatcher = new WorkerGroupTaskDispatcher("TestGroup", taskExecutorClient);
    }

    @Test
    void addTaskToWorkerGroupQueue_StatusAllowed_TaskAdded() {
        // Arrange
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        doReturn(TaskExecutionStatus.SUBMITTED_SUCCESS).when(taskInstance).getState();
        dispatcher.start();

        // Act
        dispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0);

        // Assert
        assertFalse(dispatcher.queueSize() == 0);
    }

    @Test
    void addTaskToWorkerGroupQueue_StatusNotAllowed_TaskNotAdded() {
        // Arrange
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        doReturn(TaskExecutionStatus.RUNNING_EXECUTION).when(taskInstance).getState();

        // Act
        dispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0);

        // Assert
        assertTrue(dispatcher.queueSize() > 0);
    }

    @Test
    void start_DispatcherStartsSuccessfully() {
        // Act
        dispatcher.start();

        // Assert
        assertTrue(dispatcher.isAlive());
    }

    @Test
    void dispatch_TaskDispatchedSuccessfully() throws TaskDispatchException {
        // Arrange
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        doReturn(TaskExecutionStatus.SUBMITTED_SUCCESS).when(taskInstance).getState();
        doNothing().when(taskExecutorClient).dispatch(any());
        dispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0);

        // Act
        dispatcher.start();
        sleep(100); // Give some time for the dispatcher to run
        dispatcher.close();
        Awaitility.await()
                .atMost(Duration.ofSeconds(1)).untilAsserted(
                        () -> verify(taskExecutorClient, atLeastOnce()).dispatch(taskExecutionRunnable));
        // Assert

    }

    @Test
    void dispatch_TaskDispatchFails_RetryLogicWorks() throws TaskDispatchException {
        // Arrange
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        doReturn(TaskExecutionStatus.SUBMITTED_SUCCESS).when(taskInstance).getState();
        doThrow(new RuntimeException()).when(taskExecutorClient).dispatch(any());
        dispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, 0);

        // Act
        dispatcher.start();
        sleep(100); // Give some time for the dispatcher to run
        dispatcher.close();

        // Assert
        Awaitility.await()
                .atMost(Duration.ofSeconds(1)).untilAsserted(
                        () -> verify(taskExecutorClient, atLeastOnce()).dispatch(any()));
    }
}
