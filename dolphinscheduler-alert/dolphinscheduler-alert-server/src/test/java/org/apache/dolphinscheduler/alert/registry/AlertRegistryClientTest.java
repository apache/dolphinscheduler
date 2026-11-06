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

package org.apache.dolphinscheduler.alert.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.service.AlertHAServer;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AlertRegistryClientTest {

    private final AlertRegistryClient client = new AlertRegistryClient();
    private final RegistryClient registry = mock(RegistryClient.class);
    private final MetricsProvider metrics = mock(MetricsProvider.class);
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
    private Runnable update;

    @BeforeEach
    void setUp() {
        AlertConfig config = new AlertConfig();
        config.setPort(50052);
        config.setAlertServerAddress("localhost:50052");
        ReflectionTestUtils.setField(client, "registryClient", registry);
        ReflectionTestUtils.setField(client, "alertConfig", config);
        ReflectionTestUtils.setField(client, "metricsProvider", metrics);
        ReflectionTestUtils.setField(client, "alertHAServer", mock(AlertHAServer.class));
        ReflectionTestUtils.setField(client, "registryUpdater", executor);
        when(metrics.getSystemMetrics()).thenReturn(SystemMetrics.builder().systemCpuUsagePercentage(0.2).build());
        doReturn(future).when(executor).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(1L),
                eq(TimeUnit.SECONDS));
        client.start();
        ArgumentCaptor<Runnable> action = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleWithFixedDelay(action.capture(), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        update = action.getValue();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void updatesRegistrationAtConfiguredIntervalWithStableIdentity() {
        update.run();
        update.run();
        verify(registry).persistEphemeral(eq("/nodes/alert-server/localhost:50052"), anyString());
        ReflectionTestUtils.setField(client, "lastRegistrationTime", 0L);
        update.run();
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(registry, times(2)).persistEphemeral(anyString(), json.capture());
        AlertServerHeartBeat first = JSONUtils.parseObject(json.getAllValues().get(0), AlertServerHeartBeat.class);
        AlertServerHeartBeat second = JSONUtils.parseObject(json.getAllValues().get(1), AlertServerHeartBeat.class);
        assertEquals(0.2, first.getCpuUsage());
        assertEquals(50052, first.getPort());
        assertEquals(first.getStartupTime(), second.getStartupTime());
        assertEquals(first.getProcessId(), second.getProcessId());
    }

    @Test
    void failedWriteIsRetriedOnNextTick() {
        doThrow(new IllegalStateException("disconnected")).doNothing().when(registry)
                .persistEphemeral(anyString(), anyString());
        update.run();
        update.run();
        update.run();
        verify(registry, times(2)).persistEphemeral(anyString(), anyString());
    }

    @Test
    void closeCancelsUpdatesAndPreventsReregistration() {
        client.close();
        update.run();
        verify(future).cancel(false);
        verify(executor).shutdown();
        verify(registry, never()).persistEphemeral(anyString(), anyString());
    }
}
