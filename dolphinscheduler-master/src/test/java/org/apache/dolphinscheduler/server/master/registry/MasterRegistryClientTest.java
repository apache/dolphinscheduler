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

package org.apache.dolphinscheduler.server.master.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@ExtendWith(MockitoExtension.class)
class MasterRegistryClientTest {

    @InjectMocks
    private MasterRegistryClient client;
    @Mock
    private RegistryClient registryClient;
    @Mock
    private MetricsProvider metricsProvider;
    @Mock
    private MasterServerLoadProtection masterServerLoadProtection;
    @Mock
    private MasterCoordinator masterCoordinator;
    @Spy
    private MasterConfig masterConfig = new MasterConfig();

    @Test
    void buildsFreshHeartbeatOnEveryRequestWithoutRegistryWrites() {
        PrometheusMeterRegistry meters = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Metrics.addRegistry(meters);
        try {
            when(metricsProvider.getSystemMetrics()).thenReturn(
                    SystemMetrics.builder().systemCpuUsagePercentage(0.2).build(),
                    SystemMetrics.builder().systemCpuUsagePercentage(0.8).build());
            when(masterServerLoadProtection.isOverload(any())).thenReturn(false, true);
            MasterHeartBeat first = client.getServerMetadata();
            MasterHeartBeat second = client.getServerMetadata();
            assertNotSame(first, second);
            assertEquals(0.2, first.getCpuUsage());
            assertEquals(0.8, second.getCpuUsage());
            assertEquals(ServerStatus.NORMAL, first.getServerStatus());
            assertEquals(ServerStatus.BUSY, second.getServerStatus());
            verify(registryClient, never()).persistEphemeral(anyString(), anyString());
            assertEquals(2.0, meters.get("ds.master.heartbeat.request.count").counter().count());
            when(metricsProvider.getSystemMetrics()).thenThrow(new IllegalStateException("metrics unavailable"));
            assertThrows(IllegalStateException.class, client::getServerMetadata);
            assertEquals(3.0, meters.get("ds.master.heartbeat.request.count").counter().count());
            assertTrue(meters.scrape().contains("ds_master_heartbeat_request_count_total"));
            assertFalse(meters.scrape().contains("ds_master_heartbeat_count_total"));
        } finally {
            Metrics.removeRegistry(meters);
            meters.close();
        }
    }

    @Test
    void registrationContainsOnlyStableIdentity() {
        masterConfig.setMasterAddress(NetUtils.getAddr(5678));
        masterConfig.setMasterRegistryPath("/nodes/master/" + masterConfig.getMasterAddress());
        when(metricsProvider.getSystemMetrics()).thenReturn(new SystemMetrics());
        when(registryClient.checkNodeExists(anyString(), any())).thenReturn(true);
        try {
            client.start();
            client.getServerMetadata();
            client.getServerMetadata();
            ArgumentCaptor<String> registration = ArgumentCaptor.forClass(String.class);
            verify(registryClient).persistEphemeral(anyString(), registration.capture());
            assertFalse(registration.getValue().contains("cpuUsage"));
            assertFalse(registration.getValue().contains("reportTime"));
            assertEquals(5678, JSONUtils.parseObject(registration.getValue(), MasterHeartBeat.class).getPort());
        } finally {
            client.close();
        }
    }
}
