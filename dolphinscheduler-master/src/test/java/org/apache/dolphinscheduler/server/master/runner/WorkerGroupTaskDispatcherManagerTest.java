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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkerGroupTaskDispatcherManagerTest {

    @InjectMocks
    private WorkerGroupTaskDispatcherManager workerGroupTaskDispatcherManager;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @Test
    void addTaskToWorkerGroup_NewWorkerGroup_ShouldAddTask() {
        String workerGroup = "newGroup";
        long delayTimeMills = 1000;

        workerGroupTaskDispatcherManager.addTaskToWorkerGroup(workerGroup, taskExecutionRunnable, delayTimeMills);

        ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap =
                workerGroupTaskDispatcherManager.getDispatchWorkerMap();

        assertTrue(dispatchWorkerMap.containsKey(workerGroup));
    }

    @Test
    void addTaskToWorkerGroup_ExistingWorkerGroup_ShouldAddTask() {
        String workerGroup = "existingGroup";
        long delayTimeMills = 1000;

        WorkerGroupTaskDispatcher mockDispatcher = mock(WorkerGroupTaskDispatcher.class);

        ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap = new ConcurrentHashMap<>();
        dispatchWorkerMap.put(workerGroup, mockDispatcher);

        ReflectionTestUtils.setField(workerGroupTaskDispatcherManager, "dispatchWorkerMap", dispatchWorkerMap);
        doNothing().when(mockDispatcher).start();
        workerGroupTaskDispatcherManager.addTaskToWorkerGroup(workerGroup, taskExecutionRunnable, delayTimeMills);

        verify(mockDispatcher, times(1)).addTaskToWorkerGroupQueue(taskExecutionRunnable, delayTimeMills);
    }

    @Test
    void close_ShouldCloseAllWorkerGroups() throws Exception {
        WorkerGroupTaskDispatcher mockDispatcher1 = mock(WorkerGroupTaskDispatcher.class);
        WorkerGroupTaskDispatcher mockDispatcher2 = mock(WorkerGroupTaskDispatcher.class);

        ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap = new ConcurrentHashMap<>();
        dispatchWorkerMap.put("group1", mockDispatcher1);
        dispatchWorkerMap.put("group2", mockDispatcher2);

        ReflectionTestUtils.setField(workerGroupTaskDispatcherManager, "dispatchWorkerMap", dispatchWorkerMap);

        workerGroupTaskDispatcherManager.close();

        verify(mockDispatcher1, times(1)).close();
        verify(mockDispatcher2, times(1)).close();
    }
}
