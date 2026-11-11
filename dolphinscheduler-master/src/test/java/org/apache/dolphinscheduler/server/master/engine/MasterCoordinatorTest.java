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

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.ha.ServerStatusChangeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.SerialCommandWaitHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class MasterCoordinatorTest {

    private final ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
    private final IWorkflowSerialCoordinator serial = mock(IWorkflowSerialCoordinator.class);
    private final IFailoverCoordinator failover = mock(IFailoverCoordinator.class);

    @Test
    void closeClosesBothCoordinators() {
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("master-0:5678");
        MasterCoordinator coordinator =
                new MasterCoordinator(mock(Registry.class), config, taskGroup, failover, serial);
        coordinator.close();

        InOrder order = inOrder(taskGroup, serial);
        order.verify(taskGroup).close();
        order.verify(serial).close();
    }

    @Test
    void demotionCancelsFailoverAndClosesBothCoordinators() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> scheduled = mock(ScheduledFuture.class);
        doReturn(scheduled).when(scheduler).scheduleWithFixedDelay(
                any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        MasterCoordinator.MasterCoordinatorListener listener =
                new MasterCoordinator.MasterCoordinatorListener(taskGroup, failover, serial);

        try (MockedStatic<MasterThreadFactory> factory = mockStatic(MasterThreadFactory.class)) {
            factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor).thenReturn(scheduler);
            listener.changeToActive();
        }
        listener.changeToStandBy();

        InOrder order = inOrder(taskGroup, serial, scheduled);
        order.verify(scheduled).cancel(true);
        order.verify(taskGroup).close();
        order.verify(serial).close();
    }

    @Test
    void demotionClosesSerialAfterTaskGroupFetchReturns() throws Exception {
        CountDownLatch taskGroupFetching = new CountDownLatch(1);
        CountDownLatch taskGroupClosing = new CountDownLatch(1);
        CountDownLatch releaseTaskGroupFetch = new CountDownLatch(1);
        CountDownLatch serialFetching = new CountDownLatch(1);
        CountDownLatch releaseSerialFetch = new CountDownLatch(1);
        AtomicReference<Thread> serialWorker = new AtomicReference<>();
        AtomicInteger serialGroupsHandled = new AtomicInteger();
        CountDownLatch serialHandled = new CountDownLatch(1);
        AtomicReference<Throwable> demotionFailure = new AtomicReference<>();

        TaskGroupCoordinator taskGroupCoordinator = new TaskGroupCoordinator(0) {

            @Override
            public synchronized void close() {
                taskGroupClosing.countDown();
                super.close();
            }
        };
        TaskGroupDao taskGroupDao = mock(TaskGroupDao.class);
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            taskGroupFetching.countDown();
            // Simulate a JDBC call that does not return on interruption.
            awaitUninterruptibly(releaseTaskGroupFetch, 20, TimeUnit.SECONDS);
            return Collections.emptyList();
        });
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupDao", taskGroupDao);

        WorkflowSerialCoordinator serialCoordinator = new WorkflowSerialCoordinator();
        SerialCommandDao serialCommandDao = mock(SerialCommandDao.class);
        when(serialCommandDao.fetchSerialCommands(anyInt())).thenAnswer(invocation -> {
            serialWorker.set(Thread.currentThread());
            serialFetching.countDown();
            awaitUninterruptibly(releaseSerialFetch, 20, TimeUnit.SECONDS);
            return Collections.singletonList(SerialCommandDto.builder()
                    .workflowDefinitionCode(1L).workflowDefinitionVersion(1).build());
        });
        WorkflowDefinitionLogDao definitionDao = mock(WorkflowDefinitionLogDao.class);
        WorkflowDefinitionLog definition = new WorkflowDefinitionLog();
        definition.setExecutionType(WorkflowExecutionTypeEnum.SERIAL_WAIT);
        when(definitionDao.queryByDefinitionCodeAndVersion(1L, 1)).thenReturn(definition);
        SerialCommandWaitHandler waitHandler = mock(SerialCommandWaitHandler.class);
        doAnswer(invocation -> {
            serialGroupsHandled.incrementAndGet();
            serialHandled.countDown();
            return null;
        }).when(waitHandler).handle(any());
        ReflectionTestUtils.setField(serialCoordinator, "serialCommandDao", serialCommandDao);
        ReflectionTestUtils.setField(serialCoordinator, "workflowDefinitionLogDao", definitionDao);
        ReflectionTestUtils.setField(serialCoordinator, "serialCommandWaitHandler", waitHandler);

        MasterCoordinator.MasterCoordinatorListener listener =
                new MasterCoordinator.MasterCoordinatorListener(taskGroupCoordinator, failover, serialCoordinator);
        Thread demotion = new Thread(listener::changeToStandBy);
        demotion.setDaemon(true);
        demotion.setUncaughtExceptionHandler((thread, failure) -> demotionFailure.set(failure));
        try {
            taskGroupCoordinator.start();
            serialCoordinator.start();
            assertTrue(taskGroupFetching.await(5, TimeUnit.SECONDS));
            assertTrue(serialFetching.await(5, TimeUnit.SECONDS));
            demotion.start();
            assertTrue(taskGroupClosing.await(5, TimeUnit.SECONDS));

            // Sequential close reaches Serial only after TaskGroup drains. Database conflicts are tested separately.
            releaseSerialFetch.countDown();
            assertTrue(serialHandled.await(5, TimeUnit.SECONDS));
            assertEquals(1, serialGroupsHandled.get());
            assertTrue(serialWorker.get().isAlive(), "Serial is still waiting for its close call");
            assertTrue(demotion.isAlive(), "TaskGroup must still be closing");
            releaseTaskGroupFetch.countDown();
            demotion.join(5000);
            assertFalse(serialWorker.get().isAlive(), "Serial must exit once both close calls finish");
        } finally {
            releaseSerialFetch.countDown();
            releaseTaskGroupFetch.countDown();
            demotion.join(5000);
            taskGroupCoordinator.close();
            serialCoordinator.close();
        }
        assertFalse(demotion.isAlive());
        assertNull(demotionFailure.get());
    }
    @RepeatedTest(value = 3)
    void closeDuringDemotionShouldDiscardPendingReactivation() throws Exception {
        CountDownLatch taskGroupFetching = new CountDownLatch(1);
        CountDownLatch taskGroupClosing = new CountDownLatch(1);
        CountDownLatch releaseTaskGroupFetch = new CountDownLatch(1);
        CountDownLatch serialFetching = new CountDownLatch(1);
        CountDownLatch serialStopping = new CountDownLatch(1);
        CountDownLatch releaseSerialFetch = new CountDownLatch(1);
        AtomicReference<Thread> taskGroupWorker = new AtomicReference<>();
        AtomicReference<Thread> serialWorker = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger taskGroupStarts = new AtomicInteger();
        AtomicInteger serialStarts = new AtomicInteger();
        TaskGroupCoordinator taskGroupCoordinator = new TaskGroupCoordinator(0) {

            @Override
            public synchronized void start() {
                super.start();
                taskGroupStarts.incrementAndGet();
            }

            @Override
            public synchronized void close() {
                taskGroupClosing.countDown();
                super.close();
            }
        };
        TaskGroupDao taskGroupDao = mock(TaskGroupDao.class);
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            taskGroupWorker.set(Thread.currentThread());
            taskGroupFetching.countDown();
            try {
                assertTrue(awaitUninterruptibly(releaseTaskGroupFetch, 20, TimeUnit.SECONDS));
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
                throw ex;
            }
            return Collections.emptyList();
        });
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupDao", taskGroupDao);
        WorkflowSerialCoordinator serialCoordinator = new WorkflowSerialCoordinator() {

            @Override
            public synchronized void start() {
                super.start();
                serialStarts.incrementAndGet();
            }
        };
        SerialCommandDao serialCommandDao = mock(SerialCommandDao.class);
        when(serialCommandDao.fetchSerialCommands(anyInt())).thenAnswer(invocation -> {
            serialWorker.set(Thread.currentThread());
            serialFetching.countDown();
            try {
                try {
                    assertTrue(releaseSerialFetch.await(20, TimeUnit.SECONDS));
                } catch (InterruptedException expected) {
                    serialStopping.countDown();
                    assertTrue(awaitUninterruptibly(releaseSerialFetch, 20, TimeUnit.SECONDS));
                    Thread.currentThread().interrupt();
                }
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
                throw ex;
            }
            return Collections.emptyList();
        });
        ReflectionTestUtils.setField(serialCoordinator, "serialCommandDao", serialCommandDao);
        Registry registry = mock(Registry.class);
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("master-0:5678");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MasterCoordinator coordinator =
                new MasterCoordinator(registry, config, taskGroupCoordinator, failover, serialCoordinator) {

                    @Override
                    public void addServerStatusChangeListener(ServerStatusChangeListener listener) {
                        super.addServerStatusChangeListener((origin, current) -> {
                            // Static mocks are thread-local; bind the owned scheduler on the HA callback thread.
                            try (MockedStatic<MasterThreadFactory> factory = mockStatic(MasterThreadFactory.class)) {
                                factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor)
                                        .thenReturn(scheduler);
                                listener.change(origin, current);
                            }
                        });
                    }
                };
        String selectorPath = (String) ReflectionTestUtils.getField(coordinator, "selectorPath");
        AtomicReference<String> owner = new AtomicReference<>();
        AtomicReference<SubscribeListener> subscriber = new AtomicReference<>();
        when(registry.acquireLock(selectorPath + "-lock")).thenReturn(true);
        when(registry.exists(selectorPath)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(selectorPath)).thenAnswer(invocation -> owner.get());
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(selectorPath), anyString(), eq(true));
        doAnswer(invocation -> {
            subscriber.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(selectorPath), any());
        ScheduledThreadPoolExecutor elections =
                (ScheduledThreadPoolExecutor) ReflectionTestUtils.getField(coordinator, "electionExecutor");
        Thread closer = new Thread(coordinator::close);
        closer.setDaemon(true);
        closer.setUncaughtExceptionHandler((thread, ex) -> failure.compareAndSet(null, ex));
        try {
            coordinator.start();
            assertTrue(taskGroupFetching.await(5, TimeUnit.SECONDS));
            assertTrue(serialFetching.await(5, TimeUnit.SECONDS));
            Object listener = ReflectionTestUtils.getField(coordinator, "masterCoordinatorListener");
            Future<?> failoverFuture = (Future<?>) ReflectionTestUtils.getField(listener, "failoverCoordinatorFuture");
            assertNotNull(failoverFuture);
            String identity = owner.get();
            owner.set("master-1:5678#peer");
            subscriber.get().notify(new Event(selectorPath, selectorPath, identity, Event.Type.REMOVE));
            assertTrue(taskGroupClosing.await(5, TimeUnit.SECONDS));
            assertFalse(coordinator.isActive());
            // Ownership becomes available while demotion still drains the old TaskGroup.
            owner.set(null);
            subscriber.get().notify(new Event(selectorPath, selectorPath, "", Event.Type.REMOVE));
            // The single role worker cannot check ownership until the current stop callback finishes.
            assertFalse(elections.getQueue().isEmpty());
            closer.start();
            // Close cancels queued work before waiting for the same worker's in-progress callback.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!elections.isShutdown() && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertTrue(elections.isShutdown());
            assertFalse(elections.isTerminated());
            assertTrue(taskGroupWorker.get().isAlive());
            assertTrue(serialWorker.get().isAlive());
            assertTrue(closer.isAlive());
            releaseTaskGroupFetch.countDown();
            assertTrue(serialStopping.await(5, TimeUnit.SECONDS));
            assertTrue(closer.isAlive(), "Master close must still wait for the Serial fetch");
            releaseSerialFetch.countDown();
            closer.join(5000);
            assertFalse(closer.isAlive());
            assertTrue(elections.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(taskGroupWorker.get().isAlive());
            assertFalse(serialWorker.get().isAlive());
            assertFalse(coordinator.isActive());
            assertNull(ReflectionTestUtils.getField(coordinator, "ownershipRecheck"));
            assertSame(failoverFuture, ReflectionTestUtils.getField(listener, "failoverCoordinatorFuture"));
            assertTrue(failoverFuture.isCancelled());
            // A late Registry notification after close cannot reopen election or worker scheduling.
            subscriber.get().notify(new Event(selectorPath, selectorPath, "", Event.Type.REMOVE));
            assertEquals(1, taskGroupStarts.get());
            assertEquals(1, serialStarts.get());
            verify(registry, times(1)).put(eq(selectorPath), anyString(), eq(true));
            assertNull(failure.get());
        } finally {
            releaseTaskGroupFetch.countDown();
            releaseSerialFetch.countDown();
            closer.join(5000);
            try {
                coordinator.close();
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(5, TimeUnit.SECONDS));
            }
            assertFalse(closer.isAlive());
        }
    }

}
