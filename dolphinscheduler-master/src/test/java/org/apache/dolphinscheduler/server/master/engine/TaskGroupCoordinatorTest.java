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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

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

    @ParameterizedTest
    @CsvSource({"false, false", "true, false", "false, true"})
    void restartWaitsForPreviousWorker(boolean initiallyInterrupted, boolean interruptWhileWaiting) throws Exception {
        taskGroupCoordinator.initialDelayMillis = 0;
        CountDownLatch oldWorkerEnteredDao = new CountDownLatch(1);
        CountDownLatch releaseOldWorker = new CountDownLatch(1);
        CountDownLatch newWorkerEnteredDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicReference<Thread> newWorker = new AtomicReference<>();
        AtomicReference<Boolean> newWorkerInterrupted = new AtomicReference<>();
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                oldWorkerEnteredDao.countDown();
                // Model a DAO call that does not finish when close() interrupts its caller.
                Uninterruptibles.awaitUninterruptibly(releaseOldWorker, 10, TimeUnit.SECONDS);
                // A driver may consume the interrupt; shutdown must still skip the polling delay.
                Thread.interrupted();
            } else {
                newWorker.set(Thread.currentThread());
                newWorkerInterrupted.set(Thread.currentThread().isInterrupted());
                newWorkerEnteredDao.countDown();
            }
            return Collections.emptyList();
        });
        FutureTask<Boolean> restart = new FutureTask<>(() -> {
            if (initiallyInterrupted) {
                Thread.currentThread().interrupt();
            }
            taskGroupCoordinator.start();
            assertFalse(oldWorker.get().isAlive(), "start() returned before the old worker exited");
            return Thread.currentThread().isInterrupted();
        });
        Thread starter = new Thread(restart, "coordinator-restart-test");
        try {
            taskGroupCoordinator.start();
            assertTrue(oldWorkerEnteredDao.await(5, TimeUnit.SECONDS));
            taskGroupCoordinator.close();
            taskGroupCoordinator.close();
            assertTrue(oldWorker.get().isAlive(), "close() should request stop without waiting for the DAO");

            starter.start();
            // Observe the caller actually waiting, rather than merely submitting a task.
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> starter.getState() == Thread.State.WAITING || restart.isDone());
            assertFalse(restart.isDone(), "restart must wait while the previous worker is blocked");
            assertEquals(1, newWorkerEnteredDao.getCount(), "a second worker must not enter the DAO yet");
            if (interruptWhileWaiting) {
                starter.interrupt();
                await().atMost(Duration.ofSeconds(5)).until(() -> restart.isDone()
                        || (!starter.isInterrupted() && starter.getState() == Thread.State.WAITING));
                assertFalse(restart.isDone(), "interrupting the starter must not bypass the wait");
                assertEquals(1, newWorkerEnteredDao.getCount());
            }

            releaseOldWorker.countDown();
            // Less than the normal five-second polling delay: stop must not add that delay.
            assertEquals(initiallyInterrupted || interruptWhileWaiting, restart.get(3, TimeUnit.SECONDS));
            assertTrue(newWorkerEnteredDao.await(5, TimeUnit.SECONDS));
            assertNotSame(oldWorker.get(), newWorker.get());
            assertFalse(newWorkerInterrupted.get(), "the new worker must not inherit the starter interrupt");
            assertFalse(oldWorker.get().isAlive());
        } finally {
            releaseOldWorker.countDown();
            starter.join(5000);
            assertFalse(starter.isAlive(), "restart caller did not finish after the DAO was released");
            taskGroupCoordinator.close();
            if (oldWorker.get() != null) {
                oldWorker.get().join(7000);
                assertFalse(oldWorker.get().isAlive());
            }
            if (newWorker.get() != null) {
                newWorker.get().join(5000);
                assertFalse(newWorker.get().isAlive());
            }
        }
    }

    @Test
    void closeWakesInitiallySleepingWorker() throws Exception {
        taskGroupCoordinator.start();
        Thread worker = (Thread) ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread");
        try {
            await().atMost(Duration.ofSeconds(5)).until(() -> worker.getState() == Thread.State.TIMED_WAITING);
            taskGroupCoordinator.close();
            worker.join(3000);
            assertFalse(worker.isAlive(), "close() should interrupt the sleep");
            verify(taskGroupDao, Mockito.never()).queryAllTaskGroups();
        } finally {
            taskGroupCoordinator.close();
            worker.join(5000);
            assertFalse(worker.isAlive());
        }
    }

    @Test
    void standbyNotifiesSerialWhileTaskGroupDaoIsBlocked() throws Exception {
        taskGroupCoordinator.initialDelayMillis = 0;
        CountDownLatch enteredDao = new CountDownLatch(1);
        CountDownLatch releaseDao = new CountDownLatch(1);
        AtomicReference<Thread> worker = new AtomicReference<>();
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            worker.set(Thread.currentThread());
            enteredDao.countDown();
            Uninterruptibles.awaitUninterruptibly(releaseDao, 10, TimeUnit.SECONDS);
            return Collections.emptyList();
        });
        IWorkflowSerialCoordinator serialCoordinator = Mockito.mock(IWorkflowSerialCoordinator.class);
        MasterCoordinator.MasterCoordinatorListener listener = new MasterCoordinator.MasterCoordinatorListener(
                taskGroupCoordinator, Mockito.mock(IFailoverCoordinator.class), serialCoordinator);
        try {
            taskGroupCoordinator.start();
            assertTrue(enteredDao.await(5, TimeUnit.SECONDS));
            // Keep the existing sequential close calls: neither should wait for the TaskGroup DAO.
            listener.changeToStandBy();
            verify(serialCoordinator).close();
            assertTrue(worker.get().isAlive());
        } finally {
            releaseDao.countDown();
            taskGroupCoordinator.close();
            if (worker.get() != null) {
                worker.get().join(5000);
                assertFalse(worker.get().isAlive());
            }
        }
    }
}
