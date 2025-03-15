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

import static java.time.Duration.ofSeconds;
import static org.apache.dolphinscheduler.common.thread.ThreadUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.Arrays;
import java.util.List;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupTaskDispatcherManagerTest {

    @InjectMocks
    private WorkerGroupTaskDispatcherManager manager;

    @Mock
    private ITaskExecutionRunnable taskExecutionRunnable;

    @Mock
    private ITaskExecutorClient taskExecutorClient;

    @Test
    public void testAddTaskToExistingWorkerGroup_ShouldReturnTrue() {
        String workerGroupName = "testGroup";
        manager.addWorkerGroup(workerGroupName);

        boolean result = manager.add(workerGroupName, taskExecutionRunnable, 0L);

        assertTrue(result);
    }

    @Test
    public void testAddTaskToNonExistingWorkerGroup_ShouldReturnFalse() {
        String workerGroupName = "nonExistingGroup";
        boolean result = manager.add(workerGroupName, taskExecutionRunnable, 0L);
        assertFalse(result);
    }

    @Test
    public void testAddNewWorkerGroup_ShouldAddGroup() {
        String workerGroupName = "newGroup";
        manager.addWorkerGroup(workerGroupName);
        assertFalse(manager.getDispatchWorkerMap().isEmpty());
    }

    @Test
    public void testDeleteWorkerGroup_ShouldMapEmpty() throws Exception {
        manager.addWorkerGroup("testGroup");
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(taskExecutionRunnable.getTaskInstance()).thenReturn(taskInstance);
        manager.add("testGroup", taskExecutionRunnable, 0);
        sleep(1000);
        manager.deleteWorkerGroup("testGroup");

        Awaitility.await()
                .untilAsserted(() -> assertEquals(0, manager.getDispatchWorkerMap().size()));
    }

    @Test
    public void testOnWorkerGroupAdd_ShouldAddWorkerGroups() {
        WorkerGroup group1 = new WorkerGroup();
        WorkerGroup group2 = new WorkerGroup();
        group1.setName("testGroup1");
        group2.setName("testGroup2");
        List<WorkerGroup> workerGroups = Arrays.asList(group1, group2);
        manager.onWorkerGroupAdd(workerGroups);
        assertEquals(2, manager.getDispatchWorkerMap().size());
    }

    @Test
    public void testOnWorkerGroupDelete_ShouldDeleteWorkerGroups() {
        WorkerGroup group1 = new WorkerGroup();
        WorkerGroup group2 = new WorkerGroup();
        group1.setName("testGroup1");
        group2.setName("testGroup2");
        List<WorkerGroup> workerGroups = Arrays.asList(group1, group2);
        workerGroups.forEach(workerGroup -> manager.addWorkerGroup(workerGroup.getName()));

        manager.onWorkerGroupDelete(workerGroups);

        Awaitility.await()
                .atMost(ofSeconds(6))
                .untilAsserted(() -> assertEquals(0, manager.getDispatchWorkerMap().size()));

    }

    @Test
    public void testOnCloseWorkerGroupTaskDispatcherManager() throws Exception {
        WorkerGroup group1 = new WorkerGroup();
        WorkerGroup group2 = new WorkerGroup();
        group1.setName("testGroup1");
        group2.setName("testGroup2");
        List<WorkerGroup> workerGroups = Arrays.asList(group1, group2);
        workerGroups.forEach(workerGroup -> manager.addWorkerGroup(workerGroup.getName()));

        manager.close();
        workerGroups.forEach(workerGroup -> {
            assertEquals(DispatchWorkerStatus.DELETE_SUCCESS,
                    manager.getDispatchWorkerMap().get(workerGroup.getName()).getStatus());
        });
    }
}
