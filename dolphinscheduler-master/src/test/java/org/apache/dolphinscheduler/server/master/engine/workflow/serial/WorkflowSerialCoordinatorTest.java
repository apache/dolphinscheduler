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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void restartWaitsForPreviousWorker(boolean initiallyInterrupted, boolean interruptWhileWaiting) throws Exception {
        CountDownLatch oldWorkerEnteredDao = new CountDownLatch(1);
        CountDownLatch releaseOldWorker = new CountDownLatch(1);
        CountDownLatch newWorkerEnteredDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicReference<Thread> newWorker = new AtomicReference<>();
        AtomicReference<Boolean> newWorkerInterrupted = new AtomicReference<>();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
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
            workflowSerialCoordinator.start();
            assertFalse(oldWorker.get().isAlive(), "start() returned before the old worker exited");
            return Thread.currentThread().isInterrupted();
        });
        Thread starter = new Thread(restart, "coordinator-restart-test");
        try {
            workflowSerialCoordinator.start();
            assertTrue(oldWorkerEnteredDao.await(5, TimeUnit.SECONDS));
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.close();
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
            workflowSerialCoordinator.close();
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

    @Test
    void closeDuringRestartStopsTheReplacementWorker() throws Exception {
        CountDownLatch enteredDao = new CountDownLatch(1);
        CountDownLatch releaseDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                enteredDao.countDown();
                Uninterruptibles.awaitUninterruptibly(releaseDao, 10, TimeUnit.SECONDS);
            }
            return Collections.emptyList();
        });
        FutureTask<Void> restart = new FutureTask<>(() -> {
            workflowSerialCoordinator.start();
            assertFalse(oldWorker.get().isAlive());
            return null;
        });
        FutureTask<Void> stop = new FutureTask<>(() -> {
            workflowSerialCoordinator.close();
            return null;
        });
        Thread starter = new Thread(restart, "coordinator-restart-test");
        Thread closer = new Thread(stop, "coordinator-close-test");
        try {
            workflowSerialCoordinator.start();
            assertTrue(enteredDao.await(5, TimeUnit.SECONDS));
            workflowSerialCoordinator.close();
            starter.start();
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> starter.getState() == Thread.State.WAITING || restart.isDone());
            assertFalse(restart.isDone());
            closer.start();
            // Lifecycle calls remain serialized: this close waits behind the pending restart.
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> closer.getState() == Thread.State.BLOCKED || stop.isDone());
            assertFalse(stop.isDone());
            releaseDao.countDown();
            restart.get(5, TimeUnit.SECONDS);
            stop.get(5, TimeUnit.SECONDS);
            Thread replacement = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
            assertNotSame(oldWorker.get(), replacement);
            replacement.join(5000);
            assertFalse(replacement.isAlive());
        } finally {
            releaseDao.countDown();
            starter.join(5000);
            closer.join(5000);
            assertFalse(starter.isAlive());
            assertFalse(closer.isAlive());
            workflowSerialCoordinator.close();
            if (oldWorker.get() != null) {
                oldWorker.get().join(7000);
                assertFalse(oldWorker.get().isAlive());
            }
            Thread worker = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
            if (worker != null) {
                worker.join(5000);
                assertFalse(worker.isAlive());
            }
        }
    }
}
