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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class MasterCoordinatorTest {

    @Test
    void demotionClosesCoordinatorsInOrder() {
        ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
        IWorkflowSerialCoordinator serial = mock(IWorkflowSerialCoordinator.class);
        IFailoverCoordinator failover = mock(IFailoverCoordinator.class);
        MasterCoordinator.MasterCoordinatorListener listener =
                new MasterCoordinator.MasterCoordinatorListener(taskGroup, failover, serial);
        listener.changeToActive();

        listener.changeToStandBy();

        InOrder order = inOrder(taskGroup, serial);
        order.verify(taskGroup).start();
        order.verify(serial).start();
        order.verify(taskGroup).close();
        order.verify(serial).close();
    }

    @Test
    void demotionDoesNotWaitForRunningFailoverTask() throws Exception {
        ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
        IWorkflowSerialCoordinator serial = mock(IWorkflowSerialCoordinator.class);
        IFailoverCoordinator failover = mock(IFailoverCoordinator.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            while (release.getCount() != 0) {
                try {
                    release.await();
                } catch (InterruptedException ex) {
                    interrupted.countDown();
                }
            }
            return null;
        }).when(failover).cleanHistoryFailoverFinishedMarks();
        MasterCoordinator.MasterCoordinatorListener listener =
                new MasterCoordinator.MasterCoordinatorListener(taskGroup, failover, serial);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> stopping = null;
        try {
            listener.changeToActive();
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            stopping = executor.submit(listener::changeToStandBy);
            stopping.get(2, TimeUnit.SECONDS);
            assertTrue(interrupted.await(2, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            if (stopping != null) {
                stopping.get(2, TimeUnit.SECONDS);
            } else {
                listener.changeToStandBy();
            }
            executor.shutdownNow();
        }
    }

    @Test
    void demotionAttemptsAllCleanupAndPreservesFailures() {
        ITaskGroupCoordinator taskGroup = mock(ITaskGroupCoordinator.class);
        IWorkflowSerialCoordinator serial = mock(IWorkflowSerialCoordinator.class);
        IFailoverCoordinator failover = mock(IFailoverCoordinator.class);
        IllegalStateException taskGroupFailure = new IllegalStateException("Task group close failed");
        IllegalStateException serialFailure = new IllegalStateException("Serial coordinator close failed");
        doThrow(taskGroupFailure).when(taskGroup).close();
        doThrow(serialFailure).when(serial).close();
        MasterCoordinator.MasterCoordinatorListener listener =
                new MasterCoordinator.MasterCoordinatorListener(taskGroup, failover, serial);
        listener.changeToActive();
        try {
            RuntimeException failure = assertThrows(RuntimeException.class, listener::changeToStandBy);

            assertSame(taskGroupFailure, failure);
            assertArrayEquals(new Throwable[]{serialFailure}, failure.getSuppressed());
            verify(taskGroup).close();
            verify(serial).close();
        } finally {
            doNothing().when(taskGroup).close();
            doNothing().when(serial).close();
            listener.changeToStandBy();
        }
    }
}
