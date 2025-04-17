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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalTaskDispatchWaitingQueueLooperTest {

    @InjectMocks
    private GlobalTaskDispatchWaitingQueueLooper globalTaskDispatchWaitingQueueLooper;

    @Mock
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Mock
    private WorkerGroupTaskDispatcherManager workerGroupTaskDispatcherManager;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @Test
    void testTaskExecutionRunnableStatusIsSubmittedNoWorkerGroup() {

        when(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).thenReturn(taskExecutionRunnable);
        TaskInstance taskInstance = mock(TaskInstance.class);
        when(taskInstance.getState()).thenReturn(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(mock(TaskExecutionContext.class));
        globalTaskDispatchWaitingQueueLooper.doDispatch();

        verify(workerGroupTaskDispatcherManager, times(1)).addTaskToWorkerGroup(any(),
                any(ITaskExecutionRunnable.class),
                anyLong());

    }

}
