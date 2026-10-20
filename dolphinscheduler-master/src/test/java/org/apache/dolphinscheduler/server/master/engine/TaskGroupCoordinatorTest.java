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
    void closeShouldWaitForPreviousFetchBeforeRestart() throws Exception {
        verifyCloseDuringFetch(false);
    }

    @Test
    void interruptedCloseShouldFinishWaitingAndRestoreInterrupt() throws Exception {
        verifyCloseDuringFetch(true);
    }

    private void verifyCloseDuringFetch(boolean interruptCloser) throws Exception {
        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch canceled = new CountDownLatch(1);
        CountDownLatch releaseFetch = new CountDownLatch(1);
        CountDownLatch restarted = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Thread> firstThread = new AtomicReference<>();
        Mockito.when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            if (firstThread.compareAndSet(null, Thread.currentThread())) {
                fetching.countDown();
                // Model a JDBC request that returns only after the server responds, despite interruption.
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while (releaseFetch.getCount() != 0) {
                    try {
                        long remaining = deadline - System.nanoTime();
                        if (remaining <= 0 || !releaseFetch.await(remaining, TimeUnit.NANOSECONDS)) {
                            failure.compareAndSet(null, new AssertionError("Blocked DAO was not released"));
                            return Collections.emptyList();
                        }
                    } catch (InterruptedException ignored) {
                        canceled.countDown();
                    }
                }
                TaskGroup staleGroup = new TaskGroup();
                staleGroup.setId(1);
                staleGroup.setUseSize(1);
                return Collections.singletonList(staleGroup);
            }
            if (Thread.currentThread() == firstThread.get()) {
                failure.compareAndSet(null, new AssertionError("Canceled worker resumed polling"));
            }
            restarted.countDown();
            return Collections.emptyList();
        });
        Thread closer = new Thread(() -> {
            try {
                taskGroupCoordinator.close();
                Assertions.assertFalse(firstThread.get().isAlive(), "close must finish the old worker");
                Assertions.assertEquals(interruptCloser, Thread.currentThread().isInterrupted());
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                closed.countDown();
            }
        });
        Thread starter = new Thread(() -> {
            try {
                taskGroupCoordinator.start();
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            }
        });
        closer.setDaemon(true);
        starter.setDaemon(true);
        try {
            taskGroupCoordinator.start();
            // Allow the coordinator's normal one-minute startup delay before observing the DAO call.
            Assertions.assertTrue(fetching.await(90, TimeUnit.SECONDS));
            closer.start();
            Assertions.assertTrue(canceled.await(5, TimeUnit.SECONDS));
            if (interruptCloser) {
                closer.interrupt();
            }
            starter.start();
            // The start call must wait on the lifecycle monitor while close drains the old request.
            awaitBlocked(starter);
            Assertions.assertEquals(1L, closed.getCount());
            Assertions.assertEquals(1L, restarted.getCount());
            releaseFetch.countDown();
            Assertions.assertTrue(closed.await(5, TimeUnit.SECONDS));
            // A new worker also observes the normal startup delay.
            Assertions.assertTrue(restarted.await(90, TimeUnit.SECONDS));
            closer.join(5000);
            starter.join(5000);
            Assertions.assertFalse(closer.isAlive());
            Assertions.assertFalse(starter.isAlive());
            Assertions.assertNull(failure.get());
            // A nonempty batch fetched before cancellation must not be processed after it returns.
            Mockito.verify(taskGroupQueueDao, Mockito.never()).countUsingTaskGroupQueueByGroupId(Mockito.anyInt());
            taskGroupCoordinator.close();
            taskGroupCoordinator.close();
            taskGroupCoordinator.start();
            taskGroupCoordinator.close();
        } finally {
            // Release external work before waiting for close; otherwise cleanup itself would deadlock.
            releaseFetch.countDown();
            closer.join(5000);
            starter.join(5000);
            Assertions.assertFalse(closer.isAlive());
            Assertions.assertFalse(starter.isAlive());
            taskGroupCoordinator.close();
        }
    }

    @Test
    void workerShouldNotCloseItself() throws Exception {
        CountDownLatch checked = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Mockito.when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            try {
                Assertions.assertThrows(IllegalStateException.class, () -> taskGroupCoordinator.close());
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                checked.countDown();
            }
            return Collections.emptyList();
        });
        try {
            taskGroupCoordinator.start();
            // Exercise the actual worker after its normal one-minute startup delay.
            Assertions.assertTrue(checked.await(90, TimeUnit.SECONDS));
            Assertions.assertNull(failure.get());
            // Rejection must leave lifecycle state unchanged so the owner can still close the worker.
            Assertions.assertThrows(IllegalStateException.class, () -> taskGroupCoordinator.start());
        } finally {
            taskGroupCoordinator.close();
        }
    }

    private static void awaitBlocked(Thread thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.isAlive() && thread.getState() != Thread.State.BLOCKED
                && System.nanoTime() < deadline) {
            Thread.yield();
        }
        Assertions.assertEquals(Thread.State.BLOCKED, thread.getState());
    }

}
