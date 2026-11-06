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

package org.apache.dolphinscheduler.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MonitorHeartbeatQueryTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final MonitorServiceImpl monitor = spy(new MonitorServiceImpl());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(monitor, "registryClient", registry);
    }

    @AfterEach
    void tearDown() {
        monitor.close();
    }

    @Test
    void eachRequestDiscoversMembersAndQueriesCurrentHeartbeat() {
        AtomicInteger report = new AtomicInteger(100);
        when(registry.getServerList(RegistryNodeType.WORKER))
                .thenAnswer(invocation -> Collections.singletonList(server(1234)));
        doAnswer(invocation -> heartbeat(1234, report.getAndIncrement()))
                .when(monitor).fetchHeartbeat(any(), anyString());
        assertEquals(100, monitor.listServer(RegistryNodeType.WORKER).get(0).getLastHeartbeatTime().getTime());
        assertEquals(101, monitor.listServer(RegistryNodeType.WORKER).get(0).getLastHeartbeatTime().getTime());
        verify(registry, times(2)).getServerList(RegistryNodeType.WORKER);
        verify(monitor, times(2)).fetchHeartbeat(RegistryNodeType.WORKER, "localhost:1234");
    }

    @Test
    void failedNodeDoesNotHideHealthyNodesOrLeaveRegistrationDataAsHeartbeat() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Arrays.asList(server(1234), server(1235)));
        doAnswer(invocation -> {
            if ("localhost:1235".equals(invocation.getArgument(1))) {
                throw new IllegalStateException("unreachable");
            }
            return heartbeat(1234, 100);
        }).when(monitor).fetchHeartbeat(any(), anyString());
        List<Server> servers = monitor.listServer(RegistryNodeType.WORKER);
        assertFalse(servers.get(0).isMetadataStale());
        assertEquals(0.25,
                JSONUtils.parseObject(servers.get(0).getHeartBeatInfo(), WorkerHeartBeat.class).getCpuUsage());
        assertTrue(servers.get(1).isMetadataStale());
        assertNull(servers.get(1).getHeartBeatInfo());
        assertNull(servers.get(1).getLastHeartbeatTime());
        assertEquals(1235, servers.get(1).getPort());
    }

    @Test
    void rejectsHeartbeatFromAnotherProcessInstance() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Collections.singletonList(server(1234)));
        WorkerHeartBeat anotherProcess = heartbeat(1234, 100);
        anotherProcess.setStartupTime(2);
        doAnswer(invocation -> anotherProcess).when(monitor).fetchHeartbeat(any(), anyString());
        Server server = monitor.listServer(RegistryNodeType.WORKER).get(0);
        assertTrue(server.isMetadataStale());
        assertNull(server.getHeartBeatInfo());
    }

    @Test
    void queriesNodesConcurrentlyWithinOneRequest() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Arrays.asList(server(1234), server(1235)));
        CountDownLatch started = new CountDownLatch(2);
        doAnswer(invocation -> {
            started.countDown();
            assertTrue(started.await(3, TimeUnit.SECONDS));
            return heartbeat("localhost:1234".equals(invocation.getArgument(1)) ? 1234 : 1235, 100);
        }).when(monitor).fetchHeartbeat(any(), anyString());
        assertTrue(monitor.listServer(RegistryNodeType.WORKER).stream().noneMatch(Server::isMetadataStale));
    }

    private Server server(int port) {
        Server server = new Server();
        server.setId(10);
        server.setHost("localhost");
        server.setPort(port);
        server.setCreateTime(new Date(1));
        server.setHeartBeatInfo("{\"host\":\"localhost\"}");
        return server;
    }

    private WorkerHeartBeat heartbeat(int port, int report) {
        return WorkerHeartBeat.builder().host("localhost").port(port).processId(10).startupTime(1).reportTime(report)
                .cpuUsage(0.25).build();
    }
}
