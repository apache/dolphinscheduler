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

package org.apache.dolphinscheduler.alert;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.registry.AlertHeartbeatTask;
import org.apache.dolphinscheduler.alert.registry.AlertRegistryClient;
import org.apache.dolphinscheduler.alert.rpc.AlertRpcServer;
import org.apache.dolphinscheduler.alert.service.AlertBootstrapService;
import org.apache.dolphinscheduler.alert.service.AlertHAServer;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class AlertServerHATest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testDemotionClosesAlertServerWithoutReactivatingClosedServices(boolean heartbeatShutdownFails) throws Exception {
        Registry registry = mock(Registry.class);
        AlertConfig config = mock(AlertConfig.class);
        when(config.getAlertServerAddress()).thenReturn("alert-0:50052");
        AlertHAServer haServer = new AlertHAServer(registry, config);
        AlertBootstrapService bootstrapService = mock(AlertBootstrapService.class);
        AlertRpcServer rpcServer = mock(AlertRpcServer.class);
        AlertRegistryClient registryClient = spy(new AlertRegistryClient());
        AlertHeartbeatTask heartbeatTask = mock(AlertHeartbeatTask.class);
        if (heartbeatShutdownFails) {
            doThrow(new IllegalStateException("heartbeat shutdown failed")).when(heartbeatTask).shutdown();
        }
        ReflectionTestUtils.setField(registryClient, "registryClient", new RegistryClient(registry));
        ReflectionTestUtils.setField(registryClient, "alertHeartbeatTask", heartbeatTask);
        doNothing().when(registryClient).start();
        AlertServer alertServer = new AlertServer();
        ReflectionTestUtils.setField(alertServer, "alertHAServer", haServer);
        ReflectionTestUtils.setField(alertServer, "alertBootstrapService", bootstrapService);
        ReflectionTestUtils.setField(alertServer, "alertRpcServer", rpcServer);
        ReflectionTestUtils.setField(alertServer, "alertRegistryClient", registryClient);
        ReflectionTestUtils.setField(alertServer, "alertPluginManager", mock(AlertPluginManager.class));

        String selectorPath = RegistryNodeType.ALERT_HA_LEADER.getRegistryPath();
        AtomicReference<SubscribeListener> subscriber = new AtomicReference<>();
        doAnswer(invocation -> {
            subscriber.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(selectorPath), org.mockito.ArgumentMatchers.any());
        when(registry.acquireLock(anyString())).thenReturn(true);
        try (
                MockedStatic<ServerLifeCycleManager> lifecycle = mockStatic(ServerLifeCycleManager.class);
                MockedStatic<ThreadUtils> ignored = mockStatic(ThreadUtils.class)) {
            lifecycle.when(ServerLifeCycleManager::toStopped).thenReturn(true);
            alertServer.run();
            assertTrue(haServer.isActive());
            verify(bootstrapService).start();

            // AlertServer's real demotion listener closes the whole server, rather than pausing it.
            // A transient election error must not retry and restart these closed services.
            when(registry.exists(selectorPath))
                    .thenThrow(new IllegalStateException("temporary registry failure"))
                    .thenReturn(false);
            Event removal = new Event(selectorPath, selectorPath, "", Event.Type.REMOVE);
            subscriber.get().notify(removal);
            subscriber.get().notify(removal);
            haServer.start();
            assertFalse(haServer.isActive());
            verify(bootstrapService, times(1)).start();
            verify(bootstrapService, times(1)).close();
            verify(rpcServer).close();
            verify(registryClient).close();
            verify(heartbeatTask).shutdown();
            verify(registry).close();
            verify(registry, times(2)).acquireLock(anyString());
        }
    }
}
