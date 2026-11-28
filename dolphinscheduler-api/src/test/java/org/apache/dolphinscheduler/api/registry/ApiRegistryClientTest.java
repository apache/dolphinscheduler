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

package org.apache.dolphinscheduler.api.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.common.model.ApiServerHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryClient;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

class ApiRegistryClientTest {

    @Test
    void registersStableIdentityAfterRpcStartsAndReturnsLiveMetadata() {
        RegistryClient registry = mock(RegistryClient.class);
        RpcServer rpc = mock(RpcServer.class);
        MetricsProvider metrics = mock(MetricsProvider.class);
        when(metrics.getSystemMetrics()).thenReturn(SystemMetrics.builder()
                .systemCpuUsagePercentage(0.25).systemMemoryUsedPercentage(0.5).build());
        ApiConfig config = new ApiConfig();
        config.setRpcPort(12346);
        try (ApiRegistryClient api = new ApiRegistryClient(registry, config, metrics, rpc)) {
            api.start(readyEvent(12345));
            api.start(readyEvent(12345));
            ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
            InOrder order = inOrder(rpc, registry);
            order.verify(rpc).registerServerMethodInvokerProvider(api);
            order.verify(rpc).start();
            order.verify(registry).addConnectionStateListener(any());
            order.verify(registry).persistEphemeral(startsWith("/nodes/api/"), json.capture());
            ServerIdentity identity = JSONUtils.parseObject(json.getValue(), ServerIdentity.class);
            assertEquals(12346, identity.getPort());
            assertFalse(json.getValue().contains("cpuUsage"));
            ApiServerHeartBeat heartbeat = api.getServerMetadata();
            assertTrue(identity.matches(heartbeat));
            assertEquals(12345, heartbeat.getHttpPort());
            assertEquals(0.25, heartbeat.getCpuUsage());
            assertEquals(0.5, heartbeat.getMemoryUsage());
            verify(rpc, times(1)).start();
        }
        verify(rpc).close();
    }

    @Test
    void reconnectDoesNotReregisterAfterClose() {
        RegistryClient registry = mock(RegistryClient.class);
        RpcServer rpc = mock(RpcServer.class);
        ApiRegistryClient api = new ApiRegistryClient(registry, new ApiConfig(), mock(MetricsProvider.class), rpc);
        api.start(readyEvent(12345));
        ArgumentCaptor<ConnectionListener> listener = ArgumentCaptor.forClass(ConnectionListener.class);
        verify(registry).addConnectionStateListener(listener.capture());
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        api.close();
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        api.close();
        verify(registry, times(2)).persistEphemeral(anyString(), anyString());
        verify(rpc, times(1)).close();
    }

    @Test
    void rpcBindFailureDoesNotPublishIdentity() {
        RegistryClient registry = mock(RegistryClient.class);
        RpcServer rpc = mock(RpcServer.class);
        doThrow(new IllegalStateException("port in use")).when(rpc).start();
        try (
                ApiRegistryClient api =
                        new ApiRegistryClient(registry, new ApiConfig(), mock(MetricsProvider.class), rpc)) {
            assertThrows(IllegalStateException.class, () -> api.start(readyEvent(12345)));
            verify(registry, never()).persistEphemeral(anyString(), anyString());
        }
    }

    private ApplicationReadyEvent readyEvent(int port) {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        ServletWebServerApplicationContext context = mock(ServletWebServerApplicationContext.class);
        WebServer webServer = mock(WebServer.class);
        when(event.getApplicationContext()).thenReturn(context);
        when(context.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(port);
        return event;
    }
}
