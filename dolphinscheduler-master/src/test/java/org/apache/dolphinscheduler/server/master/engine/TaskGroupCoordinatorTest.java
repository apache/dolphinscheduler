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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Test
    void restartShouldWaitForPreviousFetchToReturn() throws Exception {
        verifyRestartDuringFetch(false);
    }

    @Test
    void closeShouldCancelPendingRestart() throws Exception {
        verifyRestartDuringFetch(true);
    }

    private void verifyRestartDuringFetch(boolean cancelRestart) throws Exception {
        taskGroupCoordinator = Mockito.spy(taskGroupCoordinator);
        Mockito.doNothing().when(taskGroupCoordinator).pauseBeforeStart();
        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch releaseFetch = new CountDownLatch(1);
        CountDownLatch restarted = new CountDownLatch(1);
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        AtomicReference<Thread> firstThread = new AtomicReference<>();
        Mockito.when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            if (firstThread.compareAndSet(null, Thread.currentThread())) {
                fetching.countDown();
                // JDBC calls can finish after interrupt. Keep the previous run inside its DAO call.
                boolean released = false;
                while (!released) {
                    try {
                        released = releaseFetch.await(5, TimeUnit.SECONDS);
                        if (!released) {
                            workerFailure.set(new AssertionError("Test did not release the blocked DAO"));
                            return Collections.emptyList();
                        }
                    } catch (InterruptedException ignored) {
                        // Model a driver that does not cancel its request on interrupt.
                    }
                }
                TaskGroup staleGroup = new TaskGroup();
                staleGroup.setId(1);
                staleGroup.setUseSize(1);
                return Collections.singletonList(staleGroup);
            } else {
                if (firstThread.get() == Thread.currentThread()) {
                    workerFailure.set(new AssertionError("Old polling loop resumed"));
                }
                restarted.countDown();
            }
            return Collections.emptyList();
        });
        try {
            taskGroupCoordinator.start();
            Assertions.assertTrue(fetching.await(5, TimeUnit.SECONDS));
            taskGroupCoordinator.close();
            taskGroupCoordinator.start();
            Assertions.assertSame(firstThread.get(), ReflectionTestUtils.getField(taskGroupCoordinator,
                    "internalThread"), "Keep the old run until its DAO call returns");
            if (cancelRestart) {
                taskGroupCoordinator.close();
            }
            releaseFetch.countDown();
            firstThread.get().join(5000);
            Assertions.assertFalse(firstThread.get().isAlive());
            if (cancelRestart) {
                Assertions.assertEquals(1L, restarted.getCount());
                Assertions.assertNull(ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread"));
            } else {
                Assertions.assertTrue(restarted.await(5, TimeUnit.SECONDS));
            }
            Assertions.assertNull(workerFailure.get());
            // The old fetch returned a nonempty batch, but cancellation must discard it.
            Mockito.verify(taskGroupQueueDao, Mockito.never()).countUsingTaskGroupQueueByGroupId(Mockito.anyInt());
        } finally {
            Thread latestThread;
            synchronized (taskGroupCoordinator) {
                latestThread = (Thread) ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread");
                taskGroupCoordinator.close();
            }
            releaseFetch.countDown();
            if (latestThread != null) {
                latestThread.join(5000);
                Assertions.assertFalse(latestThread.isAlive());
            }
            if (firstThread.get() != null) {
                firstThread.get().join(5000);
                Assertions.assertFalse(firstThread.get().isAlive());
            }
        }
    }

}
