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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.util.concurrent.Uninterruptibles;

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

    @ParameterizedTest
    @CsvSource({"false, false", "true, false", "false, true"})
    void restartWaitsForPreviousWorker(boolean initiallyInterrupted, boolean interruptWaiter) throws Exception {
        CountDownLatch oldWorkerEnteredDao = new CountDownLatch(1);
        CountDownLatch releaseOldWorker = new CountDownLatch(1);
        CountDownLatch newWorkerEnteredDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicReference<Thread> newWorker = new AtomicReference<>();
        AtomicReference<Boolean> workersOverlapped = new AtomicReference<>();
        AtomicReference<Boolean> newWorkerInterrupted = new AtomicReference<>();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                oldWorkerEnteredDao.countDown();
                // Model a DAO call that does not finish when close() interrupts its caller.
                Uninterruptibles.awaitUninterruptibly(releaseOldWorker, 30, TimeUnit.SECONDS);
                // A driver may consume the interrupt; shutdown must still skip the polling delay.
                Thread.interrupted();
            } else {
                newWorker.set(Thread.currentThread());
                workersOverlapped.set(oldWorker.get().isAlive());
                newWorkerInterrupted.set(Thread.currentThread().isInterrupted());
                newWorkerEnteredDao.countDown();
            }
            return Collections.emptyList();
        });
        FutureTask<Boolean> restart = new FutureTask<>(() -> {
            if (initiallyInterrupted) {
                Thread.currentThread().interrupt();
            }
            workflowSerialCoordinator.start();
            return Thread.currentThread().isInterrupted();
        });
        Thread starter = new Thread(restart, "coordinator-restart-test");
        Thread waiter = null;
        try {
            workflowSerialCoordinator.start();
            assertTrue(oldWorkerEnteredDao.await(5, TimeUnit.SECONDS));
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.close();
            assertTrue(oldWorker.get().isAlive(), "close() should request stop without waiting for the DAO");

            starter.start();
            // The HA callback must return while the predecessor is still blocked.
            assertEquals(initiallyInterrupted, restart.get(2, TimeUnit.SECONDS));
            assertEquals(1, newWorkerEnteredDao.getCount());
            assertThrows(IllegalStateException.class, () -> workflowSerialCoordinator.start());
            waiter = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "restartThread");
            if (interruptWaiter) {
                waiter.interrupt();
            }
            assertSame(oldWorker.get(), ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread"));
            releaseOldWorker.countDown();
            // Less than the normal five-second polling delay: stop must not add that delay.
            assertTrue(newWorkerEnteredDao.await(3, TimeUnit.SECONDS));
            assertNotSame(oldWorker.get(), newWorker.get());
            assertFalse(workersOverlapped.get(), "the replacement must observe its predecessor already terminated");
            assertFalse(newWorkerInterrupted.get(), "the new worker must not inherit the waiter interrupt");
        } finally {
            releaseOldWorker.countDown();
            starter.join(5000);
            workflowSerialCoordinator.close();
            if (waiter != null) {
                waiter.join(5000);
                assertFalse(waiter.isAlive());
            }
            if (oldWorker.get() != null) {
                oldWorker.get().join(7000);
                assertFalse(oldWorker.get().isAlive());
            }
            Thread worker = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
            if (worker != null) {
                worker.join(5000);
                assertFalse(worker.isAlive());
            }
            assertFalse(starter.isAlive());
        }
    }

    @Test
    void closeWakesPollingWorker() throws Exception {
        workflowSerialCoordinator.start();
        Thread worker = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
        try {
            await().atMost(Duration.ofSeconds(5)).until(() -> worker.getState() == Thread.State.TIMED_WAITING);
            workflowSerialCoordinator.close();
            worker.join(3000);
            assertFalse(worker.isAlive(), "close() should interrupt the sleep");
        } finally {
            workflowSerialCoordinator.close();
            worker.join(5000);
            assertFalse(worker.isAlive());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void closeCancelsPendingRestart(boolean startAgain) throws Exception {
        CountDownLatch enteredDao = new CountDownLatch(1);
        CountDownLatch releaseDao = new CountDownLatch(1);
        CountDownLatch replacementEnteredDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicReference<Thread> replacement = new AtomicReference<>();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                enteredDao.countDown();
                // The JDBC driver can ignore or consume interrupts while waiting for a response.
                Uninterruptibles.awaitUninterruptibly(releaseDao, 30, TimeUnit.SECONDS);
                Thread.interrupted();
            } else {
                replacement.set(Thread.currentThread());
                replacementEnteredDao.countDown();
            }
            return Collections.emptyList();
        });
        FutureTask<Void> restart = new FutureTask<>(() -> {
            workflowSerialCoordinator.start();
            return null;
        });
        FutureTask<Void> stop = new FutureTask<>(() -> {
            workflowSerialCoordinator.close();
            return null;
        });
        Thread starter = new Thread(restart, "coordinator-restart-test");
        Thread closer = new Thread(stop, "coordinator-close-test");
        Thread restartWaiter = null;
        try {
            workflowSerialCoordinator.start();
            assertTrue(enteredDao.await(5, TimeUnit.SECONDS));
            workflowSerialCoordinator.close();
            starter.start();
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> starter.getState() == Thread.State.WAITING || restart.isDone());
            closer.start();
            // Cancellation must finish BEFORE the old DAO is released, not after a replacement starts.
            stop.get(2, TimeUnit.SECONDS);
            restart.get(2, TimeUnit.SECONDS);
            assertTrue(oldWorker.get().isAlive());
            assertEquals(1, replacementEnteredDao.getCount());
            restartWaiter = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "restartThread");
            // Repeated role changes must reuse one waiter and retain only the latest intent.
            for (int i = 0; i < 3; i++) {
                workflowSerialCoordinator.start();
                assertSame(restartWaiter, ReflectionTestUtils.getField(workflowSerialCoordinator, "restartThread"));
                workflowSerialCoordinator.close();
            }
            if (startAgain) {
                workflowSerialCoordinator.start();
                assertSame(restartWaiter, ReflectionTestUtils.getField(workflowSerialCoordinator, "restartThread"),
                        "a fresh request should reuse the existing predecessor waiter");
            }
            releaseDao.countDown();
            oldWorker.get().join(3000);
            assertFalse(oldWorker.get().isAlive(), "stop must skip the next polling delay");
            restartWaiter.join(3000);
            assertFalse(restartWaiter.isAlive());
            if (startAgain) {
                assertTrue(replacementEnteredDao.await(3, TimeUnit.SECONDS));
                assertNotSame(oldWorker.get(), replacement.get());
            } else {
                assertSame(oldWorker.get(), ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread"),
                        "a cancelled restart must not create even a short-lived replacement");
                assertEquals(1, replacementEnteredDao.getCount());
                verify(serialCommandDao).fetchSerialCommands(1000);
            }
        } finally {
            releaseDao.countDown();
            starter.join(5000);
            closer.join(5000);
            workflowSerialCoordinator.close();
            if (restartWaiter != null) {
                restartWaiter.join(5000);
            }
            if (oldWorker.get() != null) {
                oldWorker.get().join(7000);
            }
            Thread worker = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
            if (worker != null) {
                worker.join(5000);
                assertFalse(worker.isAlive());
            }
            assertFalse(starter.isAlive());
            assertFalse(closer.isAlive());
        }
    }

}
