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

package org.apache.dolphinscheduler.server.master.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskGroupCoordinatorTest {

    @InjectMocks
    private TaskGroupCoordinator taskGroupCoordinator;

    @Mock
    private TaskGroupDao taskGroupDao;

    @Mock
    private TaskGroupQueueDao taskGroupQueueDao;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Test
    void amendTaskGroupUseSize_recountsUnderTheRowLockAndWritesOnlyUseSize() {
        final TaskGroup staleSnapshot = TaskGroup.builder().id(1).name("group").groupSize(2).useSize(2).build();
        final TaskGroup lockedTaskGroup = TaskGroup.builder().id(1).name("group").groupSize(2).useSize(2).build();
        runTransactionCallbacks();
        when(taskGroupDao.queryAllTaskGroups()).thenReturn(Lists.newArrayList(staleSnapshot));
        when(taskGroupDao.queryByIdForUpdate(1)).thenReturn(lockedTaskGroup);
        when(taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(1)).thenReturn(1);

        taskGroupCoordinator.amendTaskGroupUseSize();

        // The count must be taken after the lock: an allocation committed before the lock is then included.
        final InOrder inOrder = Mockito.inOrder(taskGroupDao, taskGroupQueueDao);
        inOrder.verify(taskGroupDao).queryByIdForUpdate(1);
        inOrder.verify(taskGroupQueueDao).countUsingTaskGroupQueueByGroupId(1);
        inOrder.verify(taskGroupDao).amendTaskGroupUseSize(1, 1);
        // Writing back the whole entity would overwrite a use_size committed by a concurrent allocation and any
        // group_size edit made after the snapshot was read.
        verify(taskGroupDao, never()).updateById(any(TaskGroup.class));
    }

    @Test
    void amendTaskGroupUseSize_usesTheLockedUseSizeInsteadOfTheStaleSnapshot() {
        // The snapshot says 2, but an allocation that committed since then moved it to the real count.
        final TaskGroup staleSnapshot = TaskGroup.builder().id(1).name("group").groupSize(2).useSize(2).build();
        final TaskGroup lockedTaskGroup = TaskGroup.builder().id(1).name("group").groupSize(2).useSize(1).build();
        runTransactionCallbacks();
        when(taskGroupDao.queryAllTaskGroups()).thenReturn(Lists.newArrayList(staleSnapshot));
        when(taskGroupDao.queryByIdForUpdate(1)).thenReturn(lockedTaskGroup);
        when(taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(1)).thenReturn(1);

        taskGroupCoordinator.amendTaskGroupUseSize();

        verify(taskGroupDao, never()).amendTaskGroupUseSize(any(), anyInt());
    }

    @Test
    void amendTaskGroupUseSize_skipsATaskGroupDeletedAfterTheSnapshot() {
        final TaskGroup staleSnapshot = TaskGroup.builder().id(1).name("group").groupSize(2).useSize(2).build();
        runTransactionCallbacks();
        when(taskGroupDao.queryAllTaskGroups()).thenReturn(Lists.newArrayList(staleSnapshot));
        when(taskGroupDao.queryByIdForUpdate(1)).thenReturn(null);

        taskGroupCoordinator.amendTaskGroupUseSize();

        verify(taskGroupQueueDao, never()).countUsingTaskGroupQueueByGroupId(any());
        verify(taskGroupDao, never()).amendTaskGroupUseSize(any(), anyInt());
    }

    @SuppressWarnings("unchecked")
    private void runTransactionCallbacks() {
        when(transactionTemplate.execute(any())).thenAnswer(
                invocation -> ((TransactionCallback<Object>) invocation.getArgument(0)).doInTransaction(null));
    }

    @Test
    void start() {
        // Get the Lock from Registry
        taskGroupCoordinator.start();
        assertTrue(taskGroupCoordinator.isStarted());

        taskGroupCoordinator.close();
        assertFalse(taskGroupCoordinator.isStarted());
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
                assertThrows(IllegalArgumentException.class,
                        () -> taskGroupCoordinator.acquireTaskGroupSlot(null, null));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        TaskDefinition taskDefinition = new TaskDefinition();
        // TaskGroupId is NULL
        TaskInstance taskInstance = new TaskInstance();
        illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance, taskDefinition));
        assertEquals("The current TaskInstance does not use task group", illegalArgumentException.getMessage());

        // TaskGroup not exist
        taskInstance.setTaskGroupId(1);
        taskInstance.setId(1);
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(null);
        illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance, taskDefinition));
        assertEquals("The current TaskGroup: 1 does not exist", illegalArgumentException.getMessage());

        // TaskGroup exist
        when(taskGroupDao.queryById(taskInstance.getTaskGroupId())).thenReturn(new TaskGroup());
        Assertions.assertDoesNotThrow(() -> taskGroupCoordinator.acquireTaskGroupSlot(taskInstance, taskDefinition));

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
        assertEquals("The TaskInstance is null", illegalArgumentException.getMessage());

        // TaskGroupId is NULL
        TaskInstance taskInstance = new TaskInstance();
        assertDoesNotThrow(() -> taskGroupCoordinator.releaseTaskGroupSlot(taskInstance));

        // Release TaskGroupQueue
        taskInstance.setId(1);
        taskInstance.setTaskGroupId(1);
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        List<TaskGroupQueue> taskGroupQueues = Lists.newArrayList(taskGroupQueue);
        when(taskGroupQueueDao.queryByTaskInstanceId(taskInstance.getId())).thenReturn(taskGroupQueues);
        taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);

        verify(taskGroupQueueDao, Mockito.times(1)).deleteById(taskGroupQueue);

    }
}
