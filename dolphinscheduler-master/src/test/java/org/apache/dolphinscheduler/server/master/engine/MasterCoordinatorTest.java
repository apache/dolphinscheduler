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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

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
}
