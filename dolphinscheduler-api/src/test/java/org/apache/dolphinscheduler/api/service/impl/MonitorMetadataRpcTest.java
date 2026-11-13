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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.alert.IAlertMetadataService;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.net.ServerSocket;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MonitorMetadataRpcTest {

    @Test
    void apiFetchesMasterWorkerAndAlertSnapshotsOverRpc() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MasterHeartBeat master = MasterHeartBeat.builder().host("localhost").port(port).processId(10).startupTime(1)
                .reportTime(100).serverStatus(ServerStatus.BUSY).cpuUsage(0.75).build();
        WorkerHeartBeat worker = WorkerHeartBeat.builder().host("localhost").port(port).processId(20).startupTime(2)
                .reportTime(200).serverStatus(ServerStatus.NORMAL).workerGroup("analytics").build();
        AlertServerHeartBeat alert = AlertServerHeartBeat.builder().host("localhost").port(port).processId(30)
                .startupTime(3).reportTime(300).isActive(true).cpuUsage(0.25).serverStatus(ServerStatus.NORMAL).build();
        RegistryClient registry = mock(RegistryClient.class);
        register(registry, RegistryNodeType.MASTER, master);
        register(registry, RegistryNodeType.WORKER, worker);
        register(registry, RegistryNodeType.ALERT_SERVER, alert);
        try (
                RpcServer server = new RpcServer(NettyServerConfig.builder().serverName("ApiMetadataRpcTest")
                        .listenPort(port).build());
                MonitorServiceImpl manager = new MonitorServiceImpl()) {
            server.registerServerMethodInvokerProvider((IMasterMetadataService) () -> master);
            server.registerServerMethodInvokerProvider((IWorkerMetadataService) () -> worker);
            server.registerServerMethodInvokerProvider((IAlertMetadataService) () -> alert);
            server.start();
            ReflectionTestUtils.setField(manager, "registryClient", registry);

            assertFalse(manager.listServer(RegistryNodeType.MASTER).get(0).isMetadataStale());
            assertFalse(manager.listServer(RegistryNodeType.WORKER).get(0).isMetadataStale());
            assertEquals(0.75, JSONUtils.parseObject(manager.listServer(RegistryNodeType.MASTER).get(0)
                    .getHeartBeatInfo(), MasterHeartBeat.class).getCpuUsage());
            assertEquals("analytics", JSONUtils.parseObject(manager.listServer(RegistryNodeType.WORKER).get(0)
                    .getHeartBeatInfo(), WorkerHeartBeat.class).getWorkerGroup());
            Server alertServer = manager.listServer(RegistryNodeType.ALERT_SERVER).get(0);
            assertFalse(alertServer.isMetadataStale());
            AlertServerHeartBeat snapshot =
                    JSONUtils.parseObject(alertServer.getHeartBeatInfo(), AlertServerHeartBeat.class);
            assertTrue(snapshot.isActive());
            assertEquals(0.25, snapshot.getCpuUsage());
        }
    }

    private void register(RegistryClient registry, RegistryNodeType type, BaseHeartBeat heartbeat) {
        ServerIdentity identity = ServerIdentity.fromHeartBeat(heartbeat);
        Server server = new Server();
        server.setHost(identity.getHost());
        server.setPort(identity.getPort());
        server.setId(identity.getProcessId());
        server.setCreateTime(new java.util.Date(identity.getStartupTime()));
        when(registry.getServerList(type)).thenReturn(Collections.singletonList(server));
    }
}
