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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.service.AlertHAServer;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

class AlertRegistryClientTest {

    private final AlertRegistryClient client = new AlertRegistryClient();
    private final RegistryClient registry = mock(RegistryClient.class);
    private final MetricsProvider metrics = mock(MetricsProvider.class);
    private final AlertHAServer ha = mock(AlertHAServer.class);

    @BeforeEach
    void setUp() {
        AlertConfig config = new AlertConfig();
        config.setPort(50052);
        config.setAlertServerAddress("localhost:50052");
        ReflectionTestUtils.setField(client, "registryClient", registry);
        ReflectionTestUtils.setField(client, "alertConfig", config);
        ReflectionTestUtils.setField(client, "metricsProvider", metrics);
        ReflectionTestUtils.setField(client, "alertHAServer", ha);
        when(metrics.getSystemMetrics()).thenReturn(SystemMetrics.builder().systemCpuUsagePercentage(0.2).build());
    }

    @Test
    void registersStableIdentityAndDoesNotWriteOnHeartbeatRequests() {
        PrometheusMeterRegistry meters = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Metrics.addRegistry(meters);
        try {
            client.start();
            when(ha.isActive()).thenReturn(true);
            AlertServerHeartBeat heartbeat = client.getServerMetadata();
            assertTrue(heartbeat.isActive());
            assertEquals(0.2, heartbeat.getCpuUsage());
            when(ha.isActive()).thenReturn(false);
            assertFalse(client.getServerMetadata().isActive());
            ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
            verify(registry).persistEphemeral(path.capture(), json.capture());
            assertEquals("/nodes/alert-server/localhost:50052", path.getValue());
            ServerIdentity identity = JSONUtils.parseObject(json.getValue(), ServerIdentity.class);
            assertTrue(identity.matches(heartbeat));
            assertFalse(JSONUtils.parseObject(json.getValue()).has("reportTime"));
            assertFalse(JSONUtils.parseObject(json.getValue()).has("cpuUsage"));
            assertFalse(JSONUtils.parseObject(json.getValue()).has("active"));
            assertEquals(2.0, meters.get("ds.alert.heartbeat.request.count").counter().count());
            when(metrics.getSystemMetrics()).thenThrow(new IllegalStateException("metrics unavailable"));
            assertThrows(IllegalStateException.class, client::getServerMetadata);
            assertEquals(3.0, meters.get("ds.alert.heartbeat.request.count").counter().count());
            assertTrue(meters.scrape().contains("ds_alert_heartbeat_request_count_total"));
            assertFalse(meters.scrape().contains("ds_alert_heartbeat_count_total"));
        } finally {
            Metrics.removeRegistry(meters);
            meters.close();
        }
    }

    @Test
    void registrationAndHeartbeatUseRpcPortAndAdvertisedHost() {
        AlertConfig config = (AlertConfig) ReflectionTestUtils.getField(client, "alertConfig");
        config.setAlertServerAddress("advertised.example:50053");
        client.start();
        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(registry).persistEphemeral(path.capture(), json.capture());
        assertEquals("/nodes/alert-server/advertised.example:50052", path.getValue());
        ServerIdentity identity = JSONUtils.parseObject(json.getValue(), ServerIdentity.class);
        assertEquals(50052, identity.getPort());
        assertTrue(identity.matches(client.getServerMetadata()));
    }

    @Test
    void reconnectRestoresSameRegistrationAndClosePreventsReregistration() {
        client.start();
        ArgumentCaptor<ConnectionListener> listener = ArgumentCaptor.forClass(ConnectionListener.class);
        verify(registry).addConnectionStateListener(listener.capture());
        listener.getValue().onUpdate(ConnectionState.SUSPENDED);
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(registry, times(2)).persistEphemeral(anyString(), json.capture());
        assertEquals(json.getAllValues().get(0), json.getAllValues().get(1));
        client.close();
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        verify(registry, times(2)).persistEphemeral(anyString(), anyString());
    }

    @Test
    void failedInitialRegistrationFailsStartup() {
        doThrow(new IllegalStateException("disconnected")).when(registry).persistEphemeral(anyString(), anyString());
        assertThrows(IllegalStateException.class, client::start);
    }
}
