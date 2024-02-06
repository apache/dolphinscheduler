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

package org.apache.dolphinscheduler.server.master.runner.taskgroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskGroupCoordinatorTest {

    @InjectMocks
    private TaskGroupCoordinator taskGroupCoordinator;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private TaskGroupDao taskGroupDao;

    @Mock
    private TaskGroupQueueDao taskGroupQueueDao;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ProcessInstanceDao processInstanceDao;

    @Test
    void start() throws InterruptedException {
        // Get the Lock from Registry
        taskGroupCoordinator.start();
        Thread.sleep(1_000);
        verify(registryClient, Mockito.times(1))
                .getLock(RegistryNodeType.MASTER_TASK_GROUP_COORDINATOR_LOCK.getRegistryPath());
        verify(registryClient, Mockito.times(1))
                .releaseLock(RegistryNodeType.MASTER_TASK_GROUP_COORDINATOR_LOCK.getRegistryPath());

    }

    @Test
    void needAcquireTaskGroupSlot() {
        // TaskInstance is null
        IllegalArgumentException illegalArgumentException =
                assertThrows(IllegalArgumentException.class, () -> taskGroupCoordinator.needAcquireTaskGroupSlot(null));
        assertEquals("The TaskInstance is null", illegalArgumentException.getMessage());

        // TaskGroupId < 0
        TaskInstance taskInstance = new TaskInstance();
        assertFalse(taskGroupCoordinator.needAcquireTaskGroupSlot(taskInstance));

        // TaskGroup not exist
        taskInstance.setTaskGroupId(1);
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(null);
        assertFalse(taskGroupCoordinator.needAcquireTaskGroupSlot(taskInstance));

        // TaskGroup is closed
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setStatus(Flag.NO);
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(taskGroup);
        assertFalse(taskGroupCoordinator.needAcquireTaskGroupSlot(taskInstance));

        // TaskGroup is open
        taskGroup.setStatus(Flag.YES);
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(taskGroup);
        assertTrue(taskGroupCoordinator.needToReleaseTaskGroupSlot(taskInstance));

    }

    @Test
    void acquireTaskGroupSlot() {
        // TaskInstance is NULL
        IllegalArgumentException illegalArgumentException =
                assertThrows(IllegalArgumentException.class, () -> taskGroupCoordinator.acquireTaskGroupSlot(null));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        // TaskGroupId is NULL
        TaskInstance taskInstance = new TaskInstance();
        illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        // TaskGroup not exist
        taskInstance.setTaskGroupId(1);
        taskInstance.setId(1);
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(null);
        illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance));
        assertEquals("The current TaskGroup: 1 does not exist", illegalArgumentException.getMessage());

        // TaskGroup exist
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(new TaskGroup());
        Assertions.assertDoesNotThrow(() -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance));

    }

    @Test
    void needToReleaseTaskGroupSlot() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.needToReleaseTaskGroupSlot(null));
        assertEquals("The TaskInstance is null", illegalArgumentException.getMessage());

        TaskInstance taskInstance = new TaskInstance();
        assertFalse(taskGroupCoordinator.needToReleaseTaskGroupSlot(taskInstance));

        taskInstance.setTaskGroupId(1);
        assertTrue(taskGroupCoordinator.needToReleaseTaskGroupSlot(taskInstance));
    }

    @Test
    void releaseTaskGroupSlot() {
        // TaskInstance is NULL
        IllegalArgumentException illegalArgumentException =
                assertThrows(IllegalArgumentException.class, () -> taskGroupCoordinator.releaseTaskGroupSlot(null));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        // TaskGroupId is NULL
        TaskInstance taskInstance = new TaskInstance();
        illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.releaseTaskGroupSlot(taskInstance));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        // Release TaskGroupQueue
        taskInstance.setId(1);
        taskInstance.setTaskGroupId(1);
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        List<TaskGroupQueue> taskGroupQueues = Lists.newArrayList(taskGroupQueue);
        when(taskGroupQueueDao.queryByTaskInstanceId(taskInstance.getId())).thenReturn(taskGroupQueues);
        taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);

        assertEquals(Flag.NO.getCode(), taskGroupQueue.getInQueue());
        assertEquals(TaskGroupQueueStatus.RELEASE, taskGroupQueue.getStatus());
        verify(taskGroupQueueDao, Mockito.times(1)).updateById(taskGroupQueue);

    }
}
