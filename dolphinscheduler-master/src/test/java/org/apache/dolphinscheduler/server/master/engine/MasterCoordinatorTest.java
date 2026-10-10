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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.SerialCommandDiscardHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.SerialCommandPriorityHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.SerialCommandWaitHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
class MasterCoordinatorTest {

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
    void closeShouldStopBothWorkersBeforeWaitingForEither() throws Exception {
        TaskGroupCoordinator taskGroupCoordinator = new TaskGroupCoordinator();
        TaskGroupDao taskGroupDao = Mockito.mock(TaskGroupDao.class);
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupDao", taskGroupDao);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> scheduled = Mockito.mock(ScheduledFuture.class);
        Mockito.doReturn(scheduled).when(scheduler).scheduleWithFixedDelay(
                Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
        ExecutorService electionExecutor = Executors.newFixedThreadPool(1, runnable -> {
            Thread worker = new Thread(() -> {
                // The listener schedules failover work on the election worker, not the test thread.
                try (MockedStatic<MasterThreadFactory> factory = Mockito.mockStatic(MasterThreadFactory.class)) {
                    factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor).thenReturn(scheduler);
                    runnable.run();
                }
            }, "test-master-election");
            worker.setDaemon(true);
            return worker;
        });
        Registry registry = Mockito.mock(Registry.class);
        Mockito.when(registry.acquireLock(Mockito.anyString())).thenReturn(true);
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("master-0:5678");
        MasterCoordinator masterCoordinator;
        try (
                MockedStatic<ThreadUtils> threadUtils =
                        Mockito.mockStatic(ThreadUtils.class, Mockito.CALLS_REAL_METHODS)) {
            threadUtils.when(() -> ThreadUtils.newDaemonFixedThreadExecutor("HA-Election-%d", 1))
                    .thenReturn(electionExecutor);
            masterCoordinator = new MasterCoordinator(registry, config, taskGroupCoordinator,
                    Mockito.mock(IFailoverCoordinator.class), workflowSerialCoordinator);
        }
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
                masterCoordinator.close();
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            } finally {
                closed.countDown();
            }
        });
        closer.setDaemon(true);
        try {
            masterCoordinator.start();
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
            masterCoordinator.close();
            // Also release Serial if a regression in the Master entry point omitted it.
            workflowSerialCoordinator.close();
            Assertions.assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
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

}
