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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.SerialCommandWaitHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class MasterCoordinatorTest {

    private final ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
    private final IWorkflowSerialCoordinator serial = mock(IWorkflowSerialCoordinator.class);
    private final IFailoverCoordinator failover = mock(IFailoverCoordinator.class);

    @Test
    void closeSignalsBothCoordinatorsBeforeWaiting() {
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("master-0:5678");
        MasterCoordinator coordinator =
                new MasterCoordinator(mock(Registry.class), config, taskGroup, failover, serial);
        coordinator.close();

        InOrder order = inOrder(taskGroup, serial);
        order.verify(taskGroup).requestStop();
        order.verify(serial).requestStop();
        order.verify(taskGroup).close();
        order.verify(serial).close();
    }

    @Test
    void demotionSignalsBothCoordinatorsAndCancelsFailoverBeforeWaiting() {
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
        order.verify(taskGroup).requestStop();
        order.verify(serial).requestStop();
        order.verify(scheduled).cancel(true);
        order.verify(taskGroup).close();
        order.verify(serial).close();
    }

    @Test
    void demotionStopsSerialWhileTaskGroupIsStillClosing() throws Exception {
        CountDownLatch taskGroupFetching = new CountDownLatch(1);
        CountDownLatch taskGroupClosing = new CountDownLatch(1);
        CountDownLatch releaseTaskGroupFetch = new CountDownLatch(1);
        CountDownLatch serialFetching = new CountDownLatch(1);
        CountDownLatch releaseSerialFetch = new CountDownLatch(1);
        AtomicReference<Thread> serialWorker = new AtomicReference<>();
        AtomicInteger serialGroupsHandled = new AtomicInteger();
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

            // Return new work to Serial while TaskGroup still holds up demotion.
            releaseSerialFetch.countDown();
            serialWorker.get().join(5000);
            assertEquals(0, serialGroupsHandled.get(), "Serial must not handle fetched work while TaskGroup closes");
            assertFalse(serialWorker.get().isAlive(), "Serial must exit before TaskGroup's DAO returns");
            assertTrue(demotion.isAlive(), "TaskGroup must still be closing");
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
}
