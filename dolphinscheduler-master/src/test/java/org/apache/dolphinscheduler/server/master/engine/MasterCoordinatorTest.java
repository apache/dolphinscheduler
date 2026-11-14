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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.util.concurrent.Uninterruptibles;

class MasterCoordinatorTest {

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false"})
    void stopCancelsRestartThroughHaCallbacks(boolean shutdownMaster, boolean sameCallbackThread) throws Exception {
        TaskGroupCoordinator taskGroupCoordinator = new TaskGroupCoordinator();
        taskGroupCoordinator.initialDelayMillis = 0;
        WorkflowSerialCoordinator serialCoordinator = new WorkflowSerialCoordinator();
        TaskGroupDao taskGroupDao = mock(TaskGroupDao.class);
        SerialCommandDao serialCommandDao = mock(SerialCommandDao.class);
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupDao", taskGroupDao);
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskGroupQueueDao", mock(TaskGroupQueueDao.class));
        ReflectionTestUtils.setField(taskGroupCoordinator, "taskInstanceDao", mock(TaskInstanceDao.class));
        ReflectionTestUtils.setField(taskGroupCoordinator, "workflowInstanceDao", mock(WorkflowInstanceDao.class));
        ReflectionTestUtils.setField(serialCoordinator, "serialCommandDao", serialCommandDao);

        CountDownLatch enteredDao = new CountDownLatch(2);
        CountDownLatch releaseDao = new CountDownLatch(1);
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> {
            enteredDao.countDown();
            Uninterruptibles.awaitUninterruptibly(releaseDao, 30, TimeUnit.SECONDS);
            Thread.interrupted();
            return Collections.emptyList();
        });
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> {
            enteredDao.countDown();
            Uninterruptibles.awaitUninterruptibly(releaseDao, 30, TimeUnit.SECONDS);
            Thread.interrupted();
            return Collections.emptyList();
        });

        // Deliver events through the real HA subscription, including its listener monitor.
        Registry registry = mock(Registry.class);
        AtomicReference<SubscribeListener> subscription = new AtomicReference<>();
        AtomicReference<String> selector = new AtomicReference<>();
        doAnswer(invocation -> {
            subscription.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(anyString(), any(SubscribeListener.class));
        when(registry.acquireLock(anyString())).thenReturn(true);
        when(registry.exists(anyString())).thenAnswer(invocation -> selector.get() != null);
        when(registry.get(anyString())).thenAnswer(invocation -> selector.get());
        doAnswer(invocation -> {
            selector.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(anyString(), anyString(), anyBoolean());
        MasterConfig masterConfig = mock(MasterConfig.class);
        when(masterConfig.getMasterAddress()).thenReturn("master-test:5678");
        MasterCoordinator master = new MasterCoordinator(registry, masterConfig, taskGroupCoordinator,
                mock(IFailoverCoordinator.class), serialCoordinator);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        List<ScheduledFuture<?>> scheduled = new ArrayList<>();
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
                    ScheduledFuture<?> future = mock(ScheduledFuture.class);
                    scheduled.add(future);
                    return future;
                });
        FutureTask<Void> restart = new FutureTask<>(() -> {
            withScheduler(scheduler, () -> subscription.get().notify(removeEvent("previous-master")));
            return null;
        });
        FutureTask<Void> stop = new FutureTask<>(() -> {
            if (shutdownMaster) {
                master.close();
            } else {
                subscription.get().notify(removeEvent(selector.getAndSet(null)));
            }
            return null;
        });
        Thread starter = new Thread(() -> {
            restart.run();
            if (sameCallbackThread) {
                // Also model a Registry that delivers consecutive watch events on one thread.
                stop.run();
            }
        }, "ha-reactivation-test");
        Thread closer = new Thread(stop, "ha-stop-test");
        Thread oldTaskGroup = null;
        Thread oldSerial = null;
        Thread taskGroupWaiter = null;
        Thread serialWaiter = null;
        try {
            withScheduler(scheduler, master::start);
            assertTrue(enteredDao.await(5, TimeUnit.SECONDS));
            oldTaskGroup = (Thread) ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread");
            oldSerial = (Thread) ReflectionTestUtils.getField(serialCoordinator, "internalThread");
            subscription.get().notify(removeEvent(selector.getAndSet(null)));
            assertFalse(master.isActive());
            verify(scheduled.get(0)).cancel(true);

            starter.start();
            await().atMost(Duration.ofSeconds(5))
                    .until(() -> starter.getState() == Thread.State.WAITING || restart.isDone());
            if (!sameCallbackThread) {
                closer.start();
            }
            // Both DAO calls are still blocked. Neither standby nor shutdown may wait for them.
            stop.get(2, TimeUnit.SECONDS);
            restart.get(2, TimeUnit.SECONDS);
            assertTrue(oldTaskGroup.isAlive());
            assertTrue(oldSerial.isAlive());
            if (!shutdownMaster) {
                assertFalse(master.isActive());
            }
            int schedulesAtStop = scheduled.size();
            assertEquals(2, schedulesAtStop);
            verify(scheduled.get(1)).cancel(true);
            taskGroupWaiter = (Thread) ReflectionTestUtils.getField(taskGroupCoordinator, "restartThread");
            serialWaiter = (Thread) ReflectionTestUtils.getField(serialCoordinator, "restartThread");
            releaseDao.countDown();
            oldTaskGroup.join(3000);
            oldSerial.join(3000);
            taskGroupWaiter.join(3000);
            serialWaiter.join(3000);
            assertFalse(oldTaskGroup.isAlive());
            assertFalse(oldSerial.isAlive());
            assertFalse(taskGroupWaiter.isAlive());
            assertFalse(serialWaiter.isAlive());
            assertSame(oldTaskGroup, ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread"));
            assertSame(oldSerial, ReflectionTestUtils.getField(serialCoordinator, "internalThread"));
            verify(taskGroupDao).queryAllTaskGroups();
            verify(serialCommandDao).fetchSerialCommands(1000);
            assertEquals(schedulesAtStop, scheduled.size(), "DAO completion must not schedule failover again");
        } finally {
            releaseDao.countDown();
            starter.join(5000);
            closer.join(5000);
            master.close();
            // Clean up both workers even when testing the old Master.close() implementation.
            taskGroupCoordinator.close();
            serialCoordinator.close();
            for (Thread thread : new Thread[]{oldTaskGroup, oldSerial, taskGroupWaiter, serialWaiter,
                    (Thread) ReflectionTestUtils.getField(taskGroupCoordinator, "internalThread"),
                    (Thread) ReflectionTestUtils.getField(serialCoordinator, "internalThread")}) {
                if (thread != null) {
                    thread.join(7000);
                    assertFalse(thread.isAlive());
                }
            }
            assertFalse(starter.isAlive());
            assertFalse(closer.isAlive());
        }
    }

    private static Event removeEvent(String identity) {
        return new Event("/coordinator", "/coordinator", identity, Event.Type.REMOVE);
    }

    private static void withScheduler(ScheduledExecutorService scheduler, Runnable action) {
        // Mockito static mocks are thread-local; install this on each actual HA callback thread.
        try (MockedStatic<MasterThreadFactory> factory = mockStatic(MasterThreadFactory.class)) {
            factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor).thenReturn(scheduler);
            action.run();
        }
    }
}
