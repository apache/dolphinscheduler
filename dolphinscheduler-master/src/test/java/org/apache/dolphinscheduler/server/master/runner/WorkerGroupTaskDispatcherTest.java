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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupTaskDispatcherTest {

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @InjectMocks
    private WorkerGroupTaskDispatcher workerGroupTaskDispatcher;

    @BeforeEach
    public void setUp() {
        workerGroupTaskDispatcher = new WorkerGroupTaskDispatcher("testWorkerGroup", taskExecutorClient);
    }

    @Test
    public void testDispatch_Success() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);

        workerGroupTaskDispatcher.add(taskExecutionRunnable, 0L);

        workerGroupTaskDispatcher.start();
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            workerGroupTaskDispatcher.close();
            verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
        });
    }

    @Test
    public void testDispatch_FailureAndRetry() throws Exception {

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setDispatchFailTimes(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(taskExecutionContext);
        doThrow(new RuntimeException("Dispatch failed")).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        workerGroupTaskDispatcher.add(taskExecutionRunnable, 0L);

        workerGroupTaskDispatcher.start();
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            workerGroupTaskDispatcher.close();
            verify(taskExecutorClient, times(2)).dispatch(taskExecutionRunnable);
        });

    }

    @Test
    public void testDispatch_TaskStatusCheck() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);

        workerGroupTaskDispatcher.add(taskExecutionRunnable, 0L);

        workerGroupTaskDispatcher.start();
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            workerGroupTaskDispatcher.close();
        });

        verify(taskExecutorClient, times(0)).dispatch(taskExecutionRunnable);
    }

    @Test
    public void testClose_QueueEmpty() throws Exception {
        workerGroupTaskDispatcher.start();
        workerGroupTaskDispatcher.close();
        await().atMost(Duration.ofSeconds(1)).until(
                () -> workerGroupTaskDispatcher.getStatus().equals(DispatchWorkerStatus.DELETE_SUCCESS));

    }

    @Test
    public void testClose_QueueNotEmpty() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        workerGroupTaskDispatcher.add(taskExecutionRunnable, 1000);
        workerGroupTaskDispatcher.start();
        workerGroupTaskDispatcher.close();
        assertEquals(DispatchWorkerStatus.DELETING, workerGroupTaskDispatcher.getStatus());
    }

}
