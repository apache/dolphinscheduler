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

package org.apache.dolphinscheduler.server.master.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.registry.api.metadata.ServerMetadataCache;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterMetadataConfiguration;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.Collections;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class ServerMetadataRpcTest {

    @Test
    void fetchesTypedMasterAndWorkerSnapshots() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MasterHeartBeat master = MasterHeartBeat.builder().host("localhost").port(port).processId(10).startupTime(1)
                .reportTime(100).serverStatus(ServerStatus.BUSY).cpuUsage(0.75).build();
        WorkerHeartBeat worker = WorkerHeartBeat.builder().host("localhost").port(port).processId(20).startupTime(2)
                .reportTime(200).serverStatus(ServerStatus.NORMAL).workerGroup("analytics").workerHostWeight(80)
                .threadPoolUsage(0.5).build();
        String address = "localhost:" + port;
        RegistryClient registry = mock(RegistryClient.class);
        when(registry.getServerNodeSet(RegistryNodeType.MASTER)).thenReturn(Collections.singleton(address));
        when(registry.getServerNodeSet(RegistryNodeType.WORKER)).thenReturn(Collections.singleton(address));
        when(registry.get(RegistryNodeType.MASTER.getRegistryPath() + "/" + address))
                .thenReturn(JSONUtils.toJsonString(ServerRegistration.fromHeartBeat(master)));
        when(registry.get(RegistryNodeType.WORKER.getRegistryPath() + "/" + address))
                .thenReturn(JSONUtils.toJsonString(ServerRegistration.fromHeartBeat(worker)));
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("local-master:5678");
        config.setHeartbeatFetchInterval(Duration.ofMillis(50));
        try (
                RpcServer server = new RpcServer(NettyServerConfig.builder().serverName("MetadataRpcTest")
                        .listenPort(port).build());
                ServerMetadataCache cache = new MasterMetadataConfiguration()
                        .masterMetadataCache(registry, config, mock(MasterRegistryClient.class))) {
            server.registerServerMethodInvokerProvider((IMasterMetadataService) () -> master);
            server.registerServerMethodInvokerProvider((IWorkerMetadataService) () -> worker);
            server.start();
            cache.start();
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                MasterHeartBeat masterSnapshot = JSONUtils.parseObject(
                        cache.getServerList(RegistryNodeType.MASTER).get(0).getHeartBeatInfo(), MasterHeartBeat.class);
                assertNotNull(masterSnapshot);
                assertEquals(ServerStatus.BUSY, masterSnapshot.getServerStatus());
                assertEquals(0.75, masterSnapshot.getCpuUsage());
                WorkerHeartBeat workerSnapshot = JSONUtils.parseObject(
                        cache.getServerList(RegistryNodeType.WORKER).get(0).getHeartBeatInfo(), WorkerHeartBeat.class);
                assertNotNull(workerSnapshot);
                assertEquals("analytics", workerSnapshot.getWorkerGroup());
                assertEquals(80, workerSnapshot.getWorkerHostWeight());
                assertEquals(0.5, workerSnapshot.getThreadPoolUsage());
            });
        }
    }
}
