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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.util.concurrent.Uninterruptibles;

class MasterCoordinatorTest {

    @Test
    void roleChangesStopPreviousWorkersAndCancelFailoverCleanup() throws Exception {
        final TaskGroupCoordinator taskGroup = new TaskGroupCoordinator();
        taskGroup.initialDelayMillis = 0;
        final TaskGroupDao taskGroupDao = mock(TaskGroupDao.class);
        ReflectionTestUtils.setField(taskGroup, "taskGroupDao", taskGroupDao);
        ReflectionTestUtils.setField(taskGroup, "taskGroupQueueDao", mock(TaskGroupQueueDao.class));

        final WorkflowSerialCoordinator serial = new WorkflowSerialCoordinator();
        final SerialCommandDao serialCommandDao = mock(SerialCommandDao.class);
        ReflectionTestUtils.setField(serial, "serialCommandDao", serialCommandDao);

        final BlockingQueue<BlockedPoll> taskGroupPolls = new LinkedBlockingQueue<>();
        final BlockingQueue<BlockedPoll> serialPolls = new LinkedBlockingQueue<>();
        when(taskGroupDao.queryAllTaskGroups()).thenAnswer(invocation -> blockPoll(taskGroupPolls));
        when(serialCommandDao.fetchSerialCommands(1000)).thenAnswer(invocation -> blockPoll(serialPolls));

        // Deliver events through the real HA subscription and Master listener.
        final Registry registry = mock(Registry.class);
        final AtomicReference<SubscribeListener> subscription = new AtomicReference<>();
        final AtomicReference<String> selector = new AtomicReference<>();
        doAnswer(invocation -> {
            subscription.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(anyString(), any(SubscribeListener.class));
        when(registry.acquireLock(anyString())).thenReturn(true);
        when(registry.exists(anyString())).thenAnswer(invocation -> selector.get() != null);
        doAnswer(invocation -> {
            selector.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(anyString(), anyString(), anyBoolean());

        final MasterConfig config = mock(MasterConfig.class);
        when(config.getMasterAddress()).thenReturn("master-test:5678");
        final MasterCoordinator master = new MasterCoordinator(registry, config, taskGroup,
                mock(IFailoverCoordinator.class), serial);

        final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        final List<ScheduledFuture<?>> scheduled = new ArrayList<>();
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
                    final ScheduledFuture<?> future = mock(ScheduledFuture.class);
                    scheduled.add(future);
                    return future;
                });

        final List<BlockedPoll> polls = new ArrayList<>();
        try {
            runHaCallback(scheduler, master::start);
            for (int activation = 0; activation < 3; activation++) {
                final BlockedPoll taskGroupPoll = takePoll(taskGroupPolls);
                polls.add(taskGroupPoll);
                final BlockedPoll serialPoll = takePoll(serialPolls);
                polls.add(serialPoll);
                assertEquals(activation + 1, scheduled.size());
                if (activation < 2) {
                    runHaCallback(scheduler,
                            () -> subscription.get().notify(removeEvent(selector.getAndSet(null))));
                    assertFalse(master.isActive());
                    verify(scheduled.get(activation)).cancel(true);
                    assertTrue(taskGroupPoll.worker.isAlive() && serialPoll.worker.isAlive(),
                            "standby should return before the blocked DAO calls do");
                    runHaCallback(scheduler,
                            () -> subscription.get().notify(removeEvent("other-master")));
                }
            }

            assertEquals(polls.size(), polls.stream().map(poll -> poll.worker).distinct().count(),
                    "each activation should reach the DAO with two new worker threads");

            // Release the earlier workers while the latest activation is still running.
            final List<BlockedPoll> previousPolls = polls.subList(0, 4);
            previousPolls.forEach(poll -> poll.release.countDown());
            for (BlockedPoll poll : previousPolls) {
                poll.awaitWorkerExit();
            }
            assertTrue(taskGroupPolls.isEmpty() && serialPolls.isEmpty(), "an old worker polled again");
            assertTrue(master.isActive());

            runHaCallback(scheduler, master::close);
            verify(scheduled.get(2)).cancel(true);
            for (BlockedPoll poll : polls.subList(4, 6)) {
                assertTrue(poll.worker.isAlive(), "Master close should return while the DAO is still blocked");
                poll.release.countDown();
                poll.awaitWorkerExit();
            }
            assertTrue(taskGroupPolls.isEmpty() && serialPolls.isEmpty());
            assertEquals(3, scheduled.size(), "returning DAO calls must not schedule any more failover work");
        } finally {
            polls.forEach(poll -> poll.release.countDown());
            taskGroup.close();
            serial.close();
            taskGroupPolls.forEach(poll -> poll.release.countDown());
            serialPolls.forEach(poll -> poll.release.countDown());
        }
    }

    private static List<?> blockPoll(BlockingQueue<BlockedPoll> polls) {
        final BlockedPoll poll = new BlockedPoll();
        polls.add(poll);
        // Model a DAO that does not return when interrupted.
        Uninterruptibles.awaitUninterruptibly(poll.release, 30, TimeUnit.SECONDS);
        Thread.interrupted();
        return Collections.emptyList();
    }

    private static BlockedPoll takePoll(BlockingQueue<BlockedPoll> polls) throws InterruptedException {
        final BlockedPoll poll = polls.poll(5, TimeUnit.SECONDS);
        assertNotNull(poll, "the new worker should reach the DAO before the previous worker exits");
        return poll;
    }

    private static Event removeEvent(String identity) {
        return new Event("/coordinator", "/coordinator", identity, Event.Type.REMOVE);
    }

    private static void runHaCallback(ScheduledExecutorService scheduler, Runnable callback) {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (MockedStatic<MasterThreadFactory> factory = mockStatic(MasterThreadFactory.class)) {
                factory.when(MasterThreadFactory::getDefaultSchedulerThreadExecutor).thenReturn(scheduler);
                callback.run();
            }
        });
    }

    private static final class BlockedPoll {

        private final Thread worker = Thread.currentThread();
        private final CountDownLatch release = new CountDownLatch(1);

        private void awaitWorkerExit() throws InterruptedException {
            worker.join(TimeUnit.SECONDS.toMillis(5));
            assertFalse(worker.isAlive(), "the stopped worker should exit after its DAO call returns");
        }
    }
}
