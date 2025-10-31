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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkerGroupDispatcherCoordinatorTest {

    @InjectMocks
    private WorkerGroupDispatcherCoordinator workerGroupDispatcherCoordinator;

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @Test
    void addTaskToWorkerGroup_NewWorkerGroup_ShouldAddTask() {
        String workerGroup = "newGroup";
        long delayTimeMills = 1000;

        ITaskExecutionRunnable taskExecutionRunnable = Mockito.mock(ITaskExecutionRunnable.class);
        TaskInstance taskInstance = mock(TaskInstance.class);
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);

        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        when(taskInstance.getWorkerGroup()).thenReturn(workerGroup);
        when(taskExecutionRunnable.getTaskExecutionContext()).thenReturn(taskExecutionContext);
        when(taskExecutionContext.getDispatchFailTimes()).thenReturn(1);

        assertFalse(workerGroupDispatcherCoordinator.existWorkerGroup(workerGroup));

        workerGroupDispatcherCoordinator.dispatchTask(taskExecutionRunnable, delayTimeMills);

        assertTrue(workerGroupDispatcherCoordinator.existWorkerGroup(workerGroup));
    }

}
