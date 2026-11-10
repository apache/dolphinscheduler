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

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.ha.AbstractHAServer;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
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

    @RepeatedTest(value = 3)
    void closeDuringPriorityRpcShouldSkipRemainingGroups() throws Exception {
        CountDownLatch rpcStarted = new CountDownLatch(1);
        CountDownLatch stopReceived = new CountDownLatch(1);
        CountDownLatch releaseRpc = new CountDownLatch(1);
        AtomicReference<Thread> worker = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        IWorkflowControlClient controlClient = Mockito.mock(IWorkflowControlClient.class);
        WorkflowInstanceDao instanceDao = Mockito.mock(WorkflowInstanceDao.class);
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("master-0:5678");
        Mockito.when(instanceDao.queryById(Mockito.anyInt())).thenAnswer(invocation -> {
            WorkflowInstance instance = new WorkflowInstance();
            instance.setId(invocation.getArgument(0));
            instance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
            instance.setHost(config.getMasterAddress());
            return instance;
        });
        Mockito.doAnswer(invocation -> {
            worker.set(Thread.currentThread());
            rpcStarted.countDown();
            try {
                try {
                    Assertions.assertTrue(releaseRpc.await(10, TimeUnit.SECONDS));
                } catch (InterruptedException expected) {
                    stopReceived.countDown();
                    // An admitted RPC may finish after interruption; close must still wait for it.
                    Assertions.assertTrue(awaitUninterruptibly(releaseRpc, 10, TimeUnit.SECONDS));
                    Thread.currentThread().interrupt();
                }
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
                throw ex;
            }
            return null;
        }).when(controlClient).stopWorkflowInstance(Mockito.any());
        SerialCommandPriorityHandler priorityHandler = Mockito.spy(new SerialCommandPriorityHandler());
        ReflectionTestUtils.setField(priorityHandler, "workflowInstanceDao", instanceDao);
        ReflectionTestUtils.setField(priorityHandler, "workflowControlClient", controlClient);
        ReflectionTestUtils.setField(priorityHandler, "masterConfig", config);
        ReflectionTestUtils.setField(workflowSerialCoordinator, "serialCommandPriorityHandler", priorityHandler);
        // A PRIORITY poll can still see a running predecessor after the newest instance was launched.
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenReturn(Arrays.asList(
                SerialCommandDto.builder().workflowDefinitionCode(1L).workflowDefinitionVersion(1)
                        .workflowInstanceId(101).state(SerialCommandDto.State.LAUNCHED).build(),
                SerialCommandDto.builder().workflowDefinitionCode(1L).workflowDefinitionVersion(1)
                        .workflowInstanceId(102).state(SerialCommandDto.State.LAUNCHED).build(),
                SerialCommandDto.builder().workflowDefinitionCode(2L).workflowDefinitionVersion(1)
                        .workflowInstanceId(201).state(SerialCommandDto.State.LAUNCHED).build(),
                SerialCommandDto.builder().workflowDefinitionCode(2L).workflowDefinitionVersion(1)
                        .workflowInstanceId(202).state(SerialCommandDto.State.LAUNCHED).build()));
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_PRIORITY);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(Mockito.anyLong(), Mockito.eq(1)))
                .thenReturn(definition);
        Thread closer = new Thread(() -> {
            try {
                workflowSerialCoordinator.close();
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            }
        });
        closer.setDaemon(true);
        try {
            workflowSerialCoordinator.start();
            Assertions.assertTrue(rpcStarted.await(5, TimeUnit.SECONDS));
            closer.start();
            Assertions.assertTrue(stopReceived.await(5, TimeUnit.SECONDS));
            Assertions.assertTrue(closer.isAlive(), "close must wait for the admitted RPC");
            Assertions.assertTrue(worker.get().isAlive());
            releaseRpc.countDown();
            closer.join(5000);
            Assertions.assertFalse(closer.isAlive());
            Assertions.assertFalse(worker.get().isAlive());
            Assertions.assertNull(failure.get());
            // Regardless of group iteration order, only the first group may have entered its Handler.
            Mockito.verify(priorityHandler, Mockito.times(1)).handle(Mockito.any());
            Mockito.verify(controlClient, Mockito.times(1)).stopWorkflowInstance(Mockito.any());
            Mockito.verify(serialCommandDao, Mockito.times(1)).fetchSerialCommands(Mockito.anyInt());
            Mockito.verifyNoInteractions(serialCommandWaitHandler, serialCommandDiscardHandler);
        } finally {
            releaseRpc.countDown();
            closer.join(5000);
            workflowSerialCoordinator.close();
            Assertions.assertFalse(closer.isAlive());
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
