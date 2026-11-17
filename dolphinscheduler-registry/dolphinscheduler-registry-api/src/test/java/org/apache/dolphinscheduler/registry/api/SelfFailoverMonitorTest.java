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

package org.apache.dolphinscheduler.registry.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SelfFailoverMonitorTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final IStoppable stoppable = mock(IStoppable.class);
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
    private final ServerIdentity identity = new ServerIdentity();
    private SelfFailoverMonitor monitor;
    private Runnable check;
    private String path;

    @BeforeEach
    void setUp() {
        identity.setHost("localhost");
        identity.setPort(1234);
        identity.setProcessId(10);
        identity.setStartupTime(100);
        path = RegistryUtils.getFailoveredNodePath("localhost:1234", 100, 10);
        when(registry.getStoppable()).thenReturn(stoppable);
        doReturn(future).when(executor).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(1000L),
                eq(TimeUnit.MILLISECONDS));
        monitor = new SelfFailoverMonitor(registry, identity, Duration.ofSeconds(1), executor);
        monitor.start();
        ArgumentCaptor<Runnable> action = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleWithFixedDelay(action.capture(), eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));
        check = action.getValue();
    }

    @Test
    void missingMarkDoesNotStopProcessOrWriteRegistry() {
        check.run();
        verify(registry).exists(path);
        verify(stoppable, never()).stop(anyString());
        verify(registry, never()).persistEphemeral(anyString(), anyString());
    }

    @Test
    void matchingMarkStopsProcessWithoutAnyHeartbeatRequests() {
        when(registry.exists(path)).thenReturn(true);
        when(registry.getStoppable()).thenReturn(stoppable);
        check.run();
        verify(stoppable).stop("The current process has been failovered: " + path);
    }

    @Test
    void registryFailureDoesNotDisableSubsequentChecks() {
        when(registry.exists(path)).thenThrow(new RegistryException("disconnected")).thenReturn(true);
        when(registry.getStoppable()).thenReturn(stoppable);
        check.run();
        check.run();
        verify(stoppable).stop("The current process has been failovered: " + path);
    }

    @Test
    void closeCancelsChecksAndRejectsQueuedWork() {
        monitor.close();
        check.run();
        verify(future).cancel(false);
        verify(executor).shutdown();
        verify(executor, never()).shutdownNow();
        verify(registry, never()).exists(anyString());
    }

    @Test
    void selfStopDoesNotInterruptServerShutdown() throws Exception {
        ScheduledExecutorService actual = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean interrupted = new AtomicBoolean(true);
        CountDownLatch stopped = new CountDownLatch(1);
        SelfFailoverMonitor actualMonitor =
                new SelfFailoverMonitor(registry, identity, Duration.ofSeconds(1), actual);
        when(registry.exists(path)).thenReturn(true);
        when(registry.getStoppable()).thenReturn(reason -> {
            actualMonitor.close();
            interrupted.set(Thread.currentThread().isInterrupted());
            stopped.countDown();
        });
        try {
            actualMonitor.start();
            assertTrue(stopped.await(5, TimeUnit.SECONDS));
            assertFalse(interrupted.get());
        } finally {
            actualMonitor.close();
            actual.shutdownNow();
        }
    }
}
