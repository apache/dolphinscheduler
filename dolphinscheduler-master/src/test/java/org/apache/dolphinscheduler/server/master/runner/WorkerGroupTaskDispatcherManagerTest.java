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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupTaskDispatcherManagerTest {

    @InjectMocks
    private WorkerGroupTaskDispatcherManager manager;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @Mock
    private TaskInstance taskInstance;

    @Test
    public void testAddTaskToWorkerGroupTaskToWorkerGroupQueueTaskToNonExistingWorkerGroup_ShouldReturnFalse() {
        Mockito.when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        String workerGroupName = "nonExistingGroup";
        boolean result = manager.addTaskToWorkerGroup(workerGroupName, taskExecutionRunnable, 0L);
        assertFalse(result);
    }

    @Test
    public void testOnWorkerGroupAdd_ShouldAddTaskToWorkerGroupTaskToWorkerGroupQueueWorkerGroups() {
        WorkerGroup group1 = new WorkerGroup();
        WorkerGroup group2 = new WorkerGroup();
        group1.setName("testGroup1");
        group2.setName("testGroup2");
        List<WorkerGroup> workerGroups = Arrays.asList(group1, group2);
        manager.onWorkerGroupAdd(workerGroups);
        assertEquals(2, manager.getDispatchWorkerMap().size());
    }
}
