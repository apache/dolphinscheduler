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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class RegistryClientTest {

    @Test
    void masterDiscoveryNeedsOnlyStableRegistration() {
        assertStableRegistration(RegistryNodeType.MASTER);
    }

    @Test
    void workerDiscoveryNeedsOnlyStableRegistration() {
        assertStableRegistration(RegistryNodeType.WORKER);
    }

    private void assertStableRegistration(RegistryNodeType type) {
        Registry registry = mock(Registry.class);
        when(registry.exists(anyString())).thenReturn(true);
        ServerRegistration registration = new ServerRegistration();
        registration.setHost("localhost");
        registration.setPort(1234);
        registration.setProcessId(10);
        registration.setStartupTime(100);
        when(registry.children(type.getRegistryPath())).thenReturn(Collections.singletonList("localhost:1234"));
        when(registry.get(type.getRegistryPath() + "/localhost:1234"))
                .thenReturn(JSONUtils.toJsonString(registration));
        Server server = new RegistryClient(registry).getServerList(type).get(0);
        assertEquals("localhost", server.getHost());
        assertEquals(1234, server.getPort());
        assertEquals(10, server.getId());
        assertEquals(100, server.getCreateTime().getTime());
        assertNull(server.getLastHeartbeatTime());
    }
}
