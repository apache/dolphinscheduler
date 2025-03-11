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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupTaskDispatchManagerTest {

    @InjectMocks
    private WorkerGroupTaskDispatchManager workerGroupTaskDispatchManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddTaskToExistingWorkerGroup() {
        String workerGroup = "testWorkerGroup";
        ITaskExecutionRunnable task = mock(ITaskExecutionRunnable.class);
        long delay = 1000L;

        workerGroupTaskDispatchManager.add(workerGroup, task, delay);

        // not have workerGroup queue，cannot add
        Assertions.assertEquals(0,
                workerGroupTaskDispatchManager.getDispatchWorkerMap().size());
    }

    @Test
    public void testAddTaskToNonExistingWorkerGroup() {
        String workerGroup = "nonExistingWorkerGroup";
        ITaskExecutionRunnable task = mock(ITaskExecutionRunnable.class);
        long delay = 1000L;
        workerGroupTaskDispatchManager.addWorkerGroup(workerGroup);
        workerGroupTaskDispatchManager.add(workerGroup, task, delay);

        Assertions.assertTrue(
                workerGroupTaskDispatchManager.getWorkerGroupPriorityDelayQueueMap().containsKey(workerGroup));
    }

    @Test
    public void testStopWorkerGroup() throws Exception {
        String workerGroup = "testWorkerGroup";
        ITaskExecutionRunnable task = mock(ITaskExecutionRunnable.class);
        long delay = 1000L;

        workerGroupTaskDispatchManager.addWorkerGroup(workerGroup);
        workerGroupTaskDispatchManager.add(workerGroup, task, delay);
        DispatchWorker dispatchWorker =
                workerGroupTaskDispatchManager.getDispatchWorkerMap().get(workerGroup);
        Assertions.assertTrue(
                workerGroupTaskDispatchManager.getWorkerGroupPriorityDelayQueueMap().get(workerGroup).size() > 0);
    }

    @Test
    public void testAddWorkerGroup() {
        String workerGroup = "newWorkerGroup";

        workerGroupTaskDispatchManager.addWorkerGroup(workerGroup);

        Assertions.assertTrue(
                workerGroupTaskDispatchManager.getWorkerGroupPriorityDelayQueueMap().containsKey(workerGroup));
        Assertions.assertTrue(workerGroupTaskDispatchManager.getWorkerGroupPriorityDelayQueueMap()
                .containsKey(workerGroup));
    }

    @Test
    public void testClose() throws Exception {
        String workerGroup = "testWorkerGroup";
        DispatchWorker looper = mock(DispatchWorker.class);

        workerGroupTaskDispatchManager.getDispatchWorkerMap().put(workerGroup, looper);

        workerGroupTaskDispatchManager.deleteWorkerGroup(workerGroup);

        verify(looper).close();
    }
}
