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

package org.apache.dolphinscheduler.server.master.engine.workflow.serial;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.ha.AbstractHAServer;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowSerialCoordinatorTest {

    @InjectMocks
    private WorkflowSerialCoordinator workflowSerialCoordinator;

    @Mock
    private SerialCommandDao serialCommandDao;

    @Mock
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Mock
    private SerialCommandWaitHandler serialCommandWaitHandler;

    @Mock
    private SerialCommandDiscardHandler serialCommandDiscardHandler;

    @Mock
    private SerialCommandPriorityHandler serialCommandPriorityHandler;

    @Test
    void startWhenAlreadyStartedShouldThrow() {
        workflowSerialCoordinator.start();
        assertThrows(IllegalStateException.class, () -> workflowSerialCoordinator.start());
        workflowSerialCoordinator.close();
    }

    @Test
    void startAfterCloseShouldNotThrow() {
        // The master hands the coordinator role back and forth without restarting the
        // JVM -- MasterCoordinatorListener calls close() on changeToStandBy() and
        // start() on changeToActive() against this same instance -- so the coordinator
        // has to be restartable in place. Before the fix, close() left internalThread
        // set and this second start() threw "InternalThread is already started".
        workflowSerialCoordinator.start();
        workflowSerialCoordinator.close();

        assertDoesNotThrow(() -> workflowSerialCoordinator.start());
        workflowSerialCoordinator.close();
    }

    @Test
    void closeShouldBeIdempotent() {
        workflowSerialCoordinator.start();
        workflowSerialCoordinator.close();

        assertDoesNotThrow(() -> workflowSerialCoordinator.close());
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
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
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
                return Collections.singletonList(SerialCommandDto.builder()
                        .workflowDefinitionCode(1L).workflowDefinitionVersion(1).build());
            }
            if (Thread.currentThread() == firstThread.get()) {
                failure.compareAndSet(null, new AssertionError("Canceled worker resumed polling"));
            }
            restarted.countDown();
            return Collections.emptyList();
        });
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(1L, 1)).thenReturn(definition);
        Thread closer = new Thread(() -> {
            try {
                workflowSerialCoordinator.close();
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
                workflowSerialCoordinator.start();
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            }
        });
        closer.setDaemon(true);
        starter.setDaemon(true);
        try {
            workflowSerialCoordinator.start();
            Assertions.assertTrue(fetching.await(5, TimeUnit.SECONDS));
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
            Assertions.assertTrue(restarted.await(5, TimeUnit.SECONDS));
            closer.join(5000);
            starter.join(5000);
            Assertions.assertFalse(closer.isAlive());
            Assertions.assertFalse(starter.isAlive());
            Assertions.assertNull(failure.get());
            // A nonempty batch fetched before cancellation must not be processed after it returns.
            Mockito.verifyNoInteractions(serialCommandWaitHandler);
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.start();
            workflowSerialCoordinator.close();
        } finally {
            // Release external work before waiting for close; otherwise cleanup itself would deadlock.
            releaseFetch.countDown();
            closer.join(5000);
            starter.join(5000);
            Assertions.assertFalse(closer.isAlive());
            Assertions.assertFalse(starter.isAlive());
            workflowSerialCoordinator.close();
        }
    }

    @Test
    void workerShouldNotCloseItself() throws Exception {
        CountDownLatch checked = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
            try {
                Assertions.assertThrows(IllegalStateException.class, () -> workflowSerialCoordinator.close());
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                checked.countDown();
            }
            return Collections.emptyList();
        });
        try {
            workflowSerialCoordinator.start();
            Assertions.assertTrue(checked.await(5, TimeUnit.SECONDS));
            Assertions.assertNull(failure.get());
            // Rejection must leave lifecycle state unchanged so the owner can still close the worker.
            Assertions.assertThrows(IllegalStateException.class, () -> workflowSerialCoordinator.start());
        } finally {
            workflowSerialCoordinator.close();
        }
    }

    @Test
    void queuedRoleChangesShouldDrainOldWorkerBeforeReactivation() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        AtomicReference<String> owner = new AtomicReference<>();
        AtomicReference<SubscribeListener> subscriber = new AtomicReference<>();
        AbstractHAServer server = new AbstractHAServer(registry, "/coordinator", "master-0:5678") {
        };
        MasterCoordinator.MasterCoordinatorListener listener = new MasterCoordinator.MasterCoordinatorListener(
                Mockito.mock(ITaskGroupCoordinator.class), Mockito.mock(IFailoverCoordinator.class),
                workflowSerialCoordinator) {

            @Override
            public void changeToActive() {
                // Static mocks are thread-local: intercept the factory on the HA worker itself.
                try (MockedStatic<MasterThreadFactory> factory = Mockito.mockStatic(MasterThreadFactory.class)) {
                    factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor)
                            .thenReturn(Mockito.mock(ScheduledExecutorService.class));
                    super.changeToActive();
                }
            }
        };
        server.addServerStatusChangeListener(listener);
        Mockito.when(registry.acquireLock("/coordinator-lock")).thenReturn(true);
        Mockito.when(registry.exists("/coordinator")).thenAnswer(invocation -> owner.get() != null);
        Mockito.when(registry.get("/coordinator")).thenAnswer(invocation -> owner.get());
        Mockito.doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(Mockito.eq("/coordinator"), Mockito.anyString(), Mockito.eq(true));
        Mockito.doAnswer(invocation -> {
            subscriber.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(Mockito.eq("/coordinator"), Mockito.any());

        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch canceled = new CountDownLatch(1);
        CountDownLatch releaseFetch = new CountDownLatch(1);
        CountDownLatch restarted = new CountDownLatch(1);
        CountDownLatch callbacksReturned = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(1L, 1)).thenReturn(definition);
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                fetching.countDown();
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
                return Collections.singletonList(SerialCommandDto.builder()
                        .workflowDefinitionCode(1L).workflowDefinitionVersion(1).build());
            }
            if (Thread.currentThread() == oldWorker.get()) {
                failure.compareAndSet(null, new AssertionError("Canceled worker resumed polling"));
            }
            restarted.countDown();
            return Collections.emptyList();
        });
        Thread callbacks = new Thread(() -> {
            try {
                owner.set("master-1:5678#peer");
                subscriber.get().notify(new Event("/coordinator", "/coordinator", "", Event.Type.REMOVE));
                Assertions.assertTrue(canceled.await(5, TimeUnit.SECONDS));
                // The HA worker is draining the old DAO call. A second notification must still return.
                owner.set(null);
                subscriber.get().notify(new Event("/coordinator", "/coordinator", "", Event.Type.REMOVE));
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                callbacksReturned.countDown();
            }
        });
        callbacks.setDaemon(true);
        try {
            server.start();
            Assertions.assertTrue(fetching.await(5, TimeUnit.SECONDS));
            callbacks.start();
            Assertions.assertTrue(callbacksReturned.await(5, TimeUnit.SECONDS));
            Assertions.assertNull(failure.get());
            Assertions.assertFalse(server.isActive());
            Assertions.assertTrue(oldWorker.get().isAlive());
            Assertions.assertEquals(1L, restarted.getCount());
            releaseFetch.countDown();
            Assertions.assertTrue(restarted.await(5, TimeUnit.SECONDS));
            Assertions.assertFalse(oldWorker.get().isAlive());
            Assertions.assertTrue(server.isActive());
            Assertions.assertNull(failure.get());
            Mockito.verifyNoInteractions(serialCommandWaitHandler);
        } finally {
            releaseFetch.countDown();
            callbacks.join(5000);
            Assertions.assertFalse(callbacks.isAlive());
            server.close();
            // Also cancel the listener's scheduled failover task, including when an assertion fails.
            listener.changeToStandBy();
        }
    }

    @Test
    void demotionShouldStopBothWorkersBeforeWaitingForEither() throws Exception {
        TaskGroupCoordinator taskGroupCoordinator = new TaskGroupCoordinator();
        TaskGroupDao taskGroupDao = Mockito.mock(TaskGroupDao.class);
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupDao", taskGroupDao);
        MasterCoordinator.MasterCoordinatorListener listener = new MasterCoordinator.MasterCoordinatorListener(
                taskGroupCoordinator, Mockito.mock(IFailoverCoordinator.class), workflowSerialCoordinator);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> scheduled = Mockito.mock(ScheduledFuture.class);
        Mockito.doReturn(scheduled).when(scheduler).scheduleWithFixedDelay(
                Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
        CountDownLatch taskGroupFetching = new CountDownLatch(1);
        CountDownLatch serialFetching = new CountDownLatch(1);
        CountDownLatch taskGroupCanceled = new CountDownLatch(1);
        CountDownLatch serialCanceled = new CountDownLatch(1);
        CountDownLatch releaseTaskGroup = new CountDownLatch(1);
        CountDownLatch releaseSerial = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch failoverCanceled = new CountDownLatch(1);
        Mockito.when(scheduled.cancel(true)).thenAnswer(invocation -> {
            failoverCanceled.countDown();
            return true;
        });
        AtomicReference<Thread> taskGroupWorker = new AtomicReference<>();
        AtomicReference<Thread> serialWorker = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Mockito.when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            taskGroupWorker.set(Thread.currentThread());
            taskGroupFetching.countDown();
            awaitDatabaseResponse(releaseTaskGroup, taskGroupCanceled, failure);
            return Collections.emptyList();
        });
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
            serialWorker.set(Thread.currentThread());
            serialFetching.countDown();
            awaitDatabaseResponse(releaseSerial, serialCanceled, failure);
            return Collections.singletonList(SerialCommandDto.builder()
                    .workflowDefinitionCode(1L).workflowDefinitionVersion(1).build());
        });
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(1L, 1)).thenReturn(definition);
        Thread closer = new Thread(() -> {
            try {
                listener.changeToStandBy();
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                closed.countDown();
            }
        });
        closer.setDaemon(true);
        try (MockedStatic<MasterThreadFactory> factory = Mockito.mockStatic(MasterThreadFactory.class)) {
            factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor).thenReturn(scheduler);
            listener.changeToActive();
            Assertions.assertTrue(serialFetching.await(5, TimeUnit.SECONDS));
            // Keep the real one-minute TaskGroup startup delay; both lifecycle implementations participate.
            Assertions.assertTrue(taskGroupFetching.await(90, TimeUnit.SECONDS));
            closer.start();
            Assertions.assertTrue(taskGroupCanceled.await(5, TimeUnit.SECONDS));
            Assertions.assertTrue(serialCanceled.await(5, TimeUnit.SECONDS));
            releaseSerial.countDown();
            serialWorker.get().join(5000);
            Assertions.assertFalse(serialWorker.get().isAlive());
            // A stop request alone must not make a new generation eligible to start.
            assertThrows(IllegalStateException.class, workflowSerialCoordinator::start);
            Mockito.verifyNoInteractions(serialCommandWaitHandler);
            // Serial has stopped while TaskGroup is still blocked; close must still join TaskGroup.
            Assertions.assertTrue(taskGroupWorker.get().isAlive());
            Assertions.assertEquals(1L, closed.getCount());
            Assertions.assertTrue(failoverCanceled.await(5, TimeUnit.SECONDS));
            Mockito.verify(scheduled).cancel(true);
            releaseTaskGroup.countDown();
            Assertions.assertTrue(closed.await(5, TimeUnit.SECONDS));
            Assertions.assertFalse(taskGroupWorker.get().isAlive());
            Assertions.assertNull(failure.get());
        } finally {
            releaseSerial.countDown();
            releaseTaskGroup.countDown();
            closer.join(5000);
            listener.changeToStandBy();
        }
    }

    private static void awaitDatabaseResponse(CountDownLatch release, CountDownLatch canceled,
                                              AtomicReference<Throwable> failure) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(100);
        while (release.getCount() != 0) {
            try {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0 || !release.await(remaining, TimeUnit.NANOSECONDS)) {
                    failure.compareAndSet(null, new AssertionError("Blocked DAO was not released"));
                    return;
                }
            } catch (InterruptedException ignored) {
                // JDBC may ignore cancellation; only the test-controlled response releases the call.
                canceled.countDown();
            }
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
