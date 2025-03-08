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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DispatchWorkerTest {

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @Mock
    private PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue;

    @InjectMocks
    private DispatchWorker dispatchWorker;

    @Test
    public void dispatch_TaskStatusEligible_ShouldDispatchTask() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);

        when(workerGroupQueue.take()).thenReturn(new DelayEntry<>(0, taskExecutionRunnable));
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskInstance.getState()).thenReturn(TaskExecutionStatus.SUBMITTED_SUCCESS);

        dispatchWorker.dispatch();

        verify(workerGroupQueue, times(1)).take();
        verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
    }

    @Test
    public void dispatch_TaskDispatchFails_ShouldRetryTask() throws TaskDispatchException {
        ITaskExecutionRunnable taskExecutionRunnable = mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);

        when(workerGroupQueue.take()).thenReturn(new DelayEntry<>(0, taskExecutionRunnable));
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskInstance.getState()).thenReturn(TaskExecutionStatus.SUBMITTED_SUCCESS);
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setDispatchFailTimes(1);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(taskExecutionContext);
        doThrow(new RuntimeException("Dispatch failed")).when(taskExecutorClient).dispatch(taskExecutionRunnable);

        dispatchWorker.dispatch();

        verify(workerGroupQueue, times(1)).take();
        verify(taskExecutorClient, times(1)).dispatch(taskExecutionRunnable);
        verify(workerGroupQueue, times(1)).add(any(DelayEntry.class));
    }
}
