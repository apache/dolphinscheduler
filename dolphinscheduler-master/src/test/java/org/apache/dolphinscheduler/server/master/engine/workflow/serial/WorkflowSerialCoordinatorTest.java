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
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    void restartShouldWaitForPreviousFetchToReturn() throws Exception {
        verifyRestartDuringFetch(false);
    }

    @Test
    void closeShouldCancelPendingRestart() throws Exception {
        verifyRestartDuringFetch(true);
    }

    private void verifyRestartDuringFetch(boolean cancelRestart) throws Exception {
        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch releaseFetch = new CountDownLatch(1);
        CountDownLatch restarted = new CountDownLatch(1);
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        AtomicReference<Thread> firstThread = new AtomicReference<>();
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
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
                return Collections.singletonList(SerialCommandDto.builder()
                        .workflowDefinitionCode(1L).workflowDefinitionVersion(1).build());
            } else {
                if (firstThread.get() == Thread.currentThread()) {
                    workerFailure.set(new AssertionError("Old polling loop resumed"));
                }
                restarted.countDown();
            }
            return Collections.emptyList();
        });
        WorkflowDefinitionLog definition =
                new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(1L, 1)).thenReturn(definition);
        try {
            workflowSerialCoordinator.start();
            Assertions.assertTrue(fetching.await(5, TimeUnit.SECONDS));
            workflowSerialCoordinator.close();
            workflowSerialCoordinator.start();
            Assertions.assertSame(firstThread.get(), ReflectionTestUtils.getField(workflowSerialCoordinator,
                    "internalThread"), "Keep the old run until its DAO call returns");
            if (cancelRestart) {
                workflowSerialCoordinator.close();
            }
            releaseFetch.countDown();
            firstThread.get().join(5000);
            Assertions.assertFalse(firstThread.get().isAlive());
            if (cancelRestart) {
                Assertions.assertEquals(1L, restarted.getCount());
                Assertions.assertNull(ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread"));
            } else {
                Assertions.assertTrue(restarted.await(5, TimeUnit.SECONDS));
            }
            Assertions.assertNull(workerFailure.get());
            // The old fetch returned a nonempty batch, but cancellation must discard it.
            Mockito.verifyNoInteractions(serialCommandWaitHandler);
        } finally {
            Thread latestThread;
            synchronized (workflowSerialCoordinator) {
                latestThread = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
                workflowSerialCoordinator.close();
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

    @Test
    void roleChangesShouldWaitForInFlightHandlerAndDiscardRemainingGroups() throws Exception {
        MasterCoordinator.MasterCoordinatorListener listener = new MasterCoordinator.MasterCoordinatorListener(
                Mockito.mock(ITaskGroupCoordinator.class), Mockito.mock(IFailoverCoordinator.class),
                workflowSerialCoordinator);
        CountDownLatch handling = new CountDownLatch(1);
        CountDownLatch releaseHandler = new CountDownLatch(1);
        CountDownLatch replacementHandled = new CountDownLatch(1);
        CountDownLatch releaseReplacement = new CountDownLatch(1);
        AtomicReference<Thread> oldThread = new AtomicReference<>();
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        AtomicInteger fetches = new AtomicInteger();
        AtomicInteger handled = new AtomicInteger();
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        Mockito.when(workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(definition);
        SerialCommandDto first = SerialCommandDto.builder().workflowDefinitionCode(1L)
                .workflowDefinitionVersion(1).build();
        SerialCommandDto second = SerialCommandDto.builder().workflowDefinitionCode(2L)
                .workflowDefinitionVersion(1).build();
        Mockito.when(serialCommandDao.fetchSerialCommands(Mockito.anyInt())).thenAnswer(invocation -> {
            if (fetches.incrementAndGet() == 1) {
                return Arrays.asList(first, second);
            }
            return Collections.singletonList(first);
        });
        Mockito.doAnswer(invocation -> {
            if (handled.incrementAndGet() == 1) {
                oldThread.set(Thread.currentThread());
                handling.countDown();
                // A handler already executing cannot be forcibly canceled. Its successor must wait.
                while (true) {
                    try {
                        if (!releaseHandler.await(5, TimeUnit.SECONDS)) {
                            workerFailure.set(new AssertionError("Handler was not released"));
                        }
                        break;
                    } catch (InterruptedException ignored) {
                        // Model an in-flight database request ignoring interruption.
                    }
                }
            } else {
                if (Thread.currentThread() == oldThread.get()) {
                    workerFailure.set(new AssertionError("Canceled run handled another group"));
                }
                replacementHandled.countDown();
                // Keep the successor at the observation point instead of relying on the polling interval.
                try {
                    releaseReplacement.await();
                } catch (InterruptedException ignored) {
                    // Test cleanup closes the successor before releasing this latch.
                }
            }
            return null;
        }).when(serialCommandWaitHandler).handle(Mockito.any());
        try {
            listener.changeToActive();
            Assertions.assertTrue(handling.await(5, TimeUnit.SECONDS));
            listener.changeToStandBy();
            listener.changeToActive();
            listener.changeToStandBy();
            listener.changeToActive();
            Assertions.assertSame(oldThread.get(), ReflectionTestUtils.getField(workflowSerialCoordinator,
                    "internalThread"));
            Assertions.assertEquals(1, fetches.get());
            releaseHandler.countDown();
            Assertions.assertTrue(replacementHandled.await(5, TimeUnit.SECONDS));
            oldThread.get().join(5000);
            Assertions.assertFalse(oldThread.get().isAlive());
            Assertions.assertNull(workerFailure.get());
            Assertions.assertEquals(2, handled.get());
        } finally {
            Thread latestThread;
            synchronized (workflowSerialCoordinator) {
                latestThread = (Thread) ReflectionTestUtils.getField(workflowSerialCoordinator, "internalThread");
                listener.changeToStandBy();
            }
            releaseHandler.countDown();
            releaseReplacement.countDown();
            if (latestThread != null) {
                latestThread.join(5000);
                Assertions.assertFalse(latestThread.isAlive());
            }
            if (oldThread.get() != null) {
                oldThread.get().join(5000);
                Assertions.assertFalse(oldThread.get().isAlive());
            }
        }
    }

}
