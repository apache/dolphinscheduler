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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
    void closeMustWaitForOldWorkerBeforeRestart() throws Exception {
        CountDownLatch inDao = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        CountDownLatch releaseDao = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            if (oldWorker.compareAndSet(null, Thread.currentThread())) {
                Thread.interrupted();
                inDao.countDown();
                boolean wasInterrupted = false;
                while (releaseDao.getCount() != 0) {
                    try {
                        releaseDao.await();
                    } catch (InterruptedException ex) {
                        wasInterrupted = true;
                        interrupted.countDown();
                    }
                }
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            return Collections.emptyList();
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> closing = null;
        Future<?> restarting = null;
        try {
            workflowSerialCoordinator.start();
            assertTrue(inDao.await(2, TimeUnit.SECONDS));
            closing = executor.submit(workflowSerialCoordinator::close);
            assertTrue(interrupted.await(2, TimeUnit.SECONDS));
            Future<?> pendingClose = closing;
            assertThrows(TimeoutException.class, () -> pendingClose.get(200, TimeUnit.MILLISECONDS));
            restarting = executor.submit(workflowSerialCoordinator::start);
            Future<?> pendingRestart = restarting;
            assertThrows(TimeoutException.class, () -> pendingRestart.get(200, TimeUnit.MILLISECONDS));
            releaseDao.countDown();
            closing.get(2, TimeUnit.SECONDS);
            restarting.get(2, TimeUnit.SECONDS);
            assertFalse(oldWorker.get().isAlive());
        } finally {
            releaseDao.countDown();
            if (closing != null) {
                closing.get(2, TimeUnit.SECONDS);
            }
            if (restarting != null) {
                restarting.get(2, TimeUnit.SECONDS);
            }
            workflowSerialCoordinator.close();
            executor.shutdownNow();
        }
    }

}
