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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

    @Test
    void previousWorkerStopsPollingAfterRestart() throws Exception {
        final CountDownLatch oldWorkerEnteredDao = new CountDownLatch(1);
        final CountDownLatch releaseOldWorker = new CountDownLatch(1);
        final CountDownLatch newWorkerEnteredDao = new CountDownLatch(1);
        final AtomicReference<Thread> oldWorker = new AtomicReference<>();
        final AtomicReference<Thread> newWorker = new AtomicReference<>();
        final AtomicInteger oldWorkerPolls = new AtomicInteger();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            final Thread worker = Thread.currentThread();
            if (oldWorker.compareAndSet(null, worker)) {
                oldWorkerPolls.incrementAndGet();
                oldWorkerEnteredDao.countDown();
                // Simulate a DAO call that keeps waiting and consumes the stop interrupt.
                Uninterruptibles.awaitUninterruptibly(releaseOldWorker, 30, TimeUnit.SECONDS);
                Thread.interrupted();
            } else if (worker == oldWorker.get()) {
                oldWorkerPolls.incrementAndGet();
            } else {
                newWorker.set(worker);
                newWorkerEnteredDao.countDown();
            }
            return Collections.emptyList();
        });

        try {
            workflowSerialCoordinator.start();
            assertTrue(oldWorkerEnteredDao.await(5, TimeUnit.SECONDS), "old worker did not reach the DAO");
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.start();
            assertTrue(newWorkerEnteredDao.await(5, TimeUnit.SECONDS), "new worker should not wait for the old DAO");
            assertTrue(oldWorker.get().isAlive(), "old DAO is still blocked");

            releaseOldWorker.countDown();
            await().atMost(Duration.ofSeconds(10))
                    .until(() -> oldWorkerPolls.get() > 1 || !oldWorker.get().isAlive());
            assertEquals(1, oldWorkerPolls.get(), "old worker entered another polling round after restart");
        } finally {
            workflowSerialCoordinator.close();
            releaseOldWorker.countDown();
            for (Thread worker : new Thread[]{oldWorker.get(), newWorker.get()}) {
                if (worker != null) {
                    worker.interrupt();
                    worker.join(5000);
                }
            }
        }
    }
}
