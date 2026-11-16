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
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.cluster.ClusterManager;
import org.apache.dolphinscheduler.server.master.cluster.MasterClusters;
import org.apache.dolphinscheduler.server.master.cluster.MasterServerMetadata;
import org.apache.dolphinscheduler.server.master.cluster.MasterSlotManager;
import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;
import org.apache.dolphinscheduler.server.master.cluster.WorkerGroupChangeNotifier;
import org.apache.dolphinscheduler.server.master.cluster.WorkerServerMetadata;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.Collections;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        Server masterRegistration = new Server();
        masterRegistration.setHeartBeatInfo(JSONUtils.toJsonString(ServerIdentity.fromHeartBeat(master)));
        Server workerRegistration = new Server();
        workerRegistration.setHeartBeatInfo(JSONUtils.toJsonString(ServerIdentity.fromHeartBeat(worker)));
        when(registry.getServerList(RegistryNodeType.MASTER)).thenReturn(Collections.singletonList(masterRegistration));
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Collections.singletonList(workerRegistration));
        MasterConfig config = new MasterConfig();
        config.setMasterAddress("local-master:5678");
        config.setHeartbeatFetchInterval(Duration.ofMillis(50));
        try (
                RpcServer server = new RpcServer(NettyServerConfig.builder().serverName("MetadataRpcTest")
                        .listenPort(port).build());
                MasterClusters masters = new MasterClusters();
                WorkerClusters workers = new WorkerClusters()) {
            ClusterManager manager = new ClusterManager();
            ReflectionTestUtils.setField(manager, "masterClusters", masters);
            ReflectionTestUtils.setField(manager, "workerClusters", workers);
            ReflectionTestUtils.setField(manager, "registryClient", registry);
            ReflectionTestUtils.setField(masters, "masterConfig", config);
            ReflectionTestUtils.setField(workers, "masterConfig", config);
            ReflectionTestUtils.setField(masters, "masterRegistryClient", mock(MasterRegistryClient.class));
            ReflectionTestUtils.setField(manager, "masterSlotManager", mock(MasterSlotManager.class));
            ReflectionTestUtils.setField(manager, "workerGroupChangeNotifier", mock(WorkerGroupChangeNotifier.class));
            server.registerServerMethodInvokerProvider((IMasterMetadataService) () -> master);
            server.registerServerMethodInvokerProvider((IWorkerMetadataService) () -> worker);
            server.start();
            masters.start();
            workers.start();
            manager.start();
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                MasterServerMetadata masterSnapshot = manager.getMasterClusters().getServer(address).orElse(null);
                assertNotNull(masterSnapshot);
                assertEquals(ServerStatus.BUSY, masterSnapshot.getServerStatus());
                assertEquals(0.75, masterSnapshot.getCpuUsage());
                WorkerServerMetadata workerSnapshot = manager.getWorkerClusters().getServer(address).orElse(null);
                assertNotNull(workerSnapshot);
                assertEquals("analytics", workerSnapshot.getWorkerGroup());
                assertEquals(80, workerSnapshot.getWorkerWeight());
                assertEquals(0.5, workerSnapshot.getTaskThreadPoolUsage());
            });
        }
    }
}
