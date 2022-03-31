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

package org.apache.dolphinscheduler.service.registry;

import static org.apache.dolphinscheduler.common.Constants.ADD_OP;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.DELETE_OP;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperRegistry;
import org.apache.dolphinscheduler.registry.api.RegistryProperties;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.test.TestingServer;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RegistryClientTest {
    List<String> workerGroups;
    private RegistryClient registryClient;

    @Before
    public void before() throws Exception {
        workerGroups = Lists.newArrayList("flink", "hadoop");
        TestingServer server = new TestingServer(true);
        RegistryProperties p = new RegistryProperties();
        p.getZookeeper().setConnectString(server.getConnectString());
        ZookeeperRegistry registry = new ZookeeperRegistry(p);
        registry.start();
        registryClient = new RegistryClient(registry);
        registryClient.afterConstruct();
    }

    @Test
    public void testPersistEphemeralAndGet() {
        Server master = createServer(NodeType.MASTER, "192.168.1.1");
        String workerPath = this.getMasterPath(master);
        String heartBeatInfo = "heartBeatInfo";
        registryClient.persistEphemeral(workerPath, heartBeatInfo);
        String storedHeartBeatInfo = registryClient.get(workerPath);
        Assert.assertEquals(storedHeartBeatInfo, heartBeatInfo);
    }

    @Test
    public void testGetServerList() {
        List<Server> masters = createMasters();
        persistMasters(masters);

        List<Server> reaultMasters = registryClient.getServerList(NodeType.MASTER);
        Assert.assertEquals(masters.size(), reaultMasters.size());

        List<Server> workers = createWorks();
        persistWorkers(workers);
        List<Server> reaultWorders = registryClient.getServerList(NodeType.WORKER);
        Assert.assertEquals(workers.size() * workerGroups.size(), reaultWorders.size());
    }

    @Test
    public void testGetServerMaps() {
        List<Server> masters = createMasters();
        persistMasters(masters);
        Map<String, String> masterMaps = registryClient.getServerMaps(NodeType.MASTER, true);
        masters.forEach(master -> {
            Assert.assertTrue(masterMaps.containsKey(master.getHost() + ":" + master.getPort()));
        });
        Map<String, String> masterMaps2 = registryClient.getServerMaps(NodeType.MASTER, false);
        masters.forEach(master -> {
            Assert.assertTrue(masterMaps2.containsKey(master.getHost() + ":" + master.getPort()));
        });


        List<Server> workers = createWorks();
        persistWorkers(workers);
        Map<String, String> workerMaps = registryClient.getServerMaps(NodeType.WORKER, true);
        workers.forEach(worker -> {
            Assert.assertTrue(workerMaps.containsKey(worker.getHost() + ":" + worker.getPort()));
        });

        Map<String, String> workerMaps2 = registryClient.getServerMaps(NodeType.WORKER, false);
        workers.forEach(worker -> {
            Assert.assertFalse(workerMaps2.containsKey(worker.getHost() + ":" + worker.getPort()));
            workerGroups.forEach(g -> {
                Assert.assertTrue(workerMaps2.containsKey(g + "/" + worker.getHost() + ":" + worker.getPort()));
            });
        });
    }

    @Test
    public void testCheckNodeExists() {
        List<Server> masters = createMasters();
        persistMasters(masters);
        Server checkMaster = masters.get(0);
        boolean exists = registryClient.checkNodeExists(checkMaster.getHost() + ":" + checkMaster.getPort(), NodeType.MASTER);
        Assert.assertTrue(exists);
        exists = registryClient.checkNodeExists("192.168.2.1:" + checkMaster.getPort(), NodeType.MASTER);
        Assert.assertFalse(exists);
        exists = registryClient.checkNodeExists(checkMaster.getHost() + ":" + checkMaster.getPort(), NodeType.WORKER);
        Assert.assertFalse(exists);
    }

    @Test
    public void testHandleDeadServer() {
        List<Server> masters = createMasters();
        persistMasters(masters);
        Server deadMaster = masters.get(0);
        String path = getMasterPath(deadMaster);

        registryClient.handleDeadServer(Lists.newArrayList(path), NodeType.MASTER, ADD_OP);
        boolean result = registryClient.checkIsDeadServer(path, "master");
        Assert.assertTrue(result);

        registryClient.handleDeadServer(Lists.newArrayList(path), NodeType.MASTER, DELETE_OP);
        result = registryClient.checkIsDeadServer(path, "master");
        Assert.assertFalse(result);
    }

    private void persistMasters(List<Server> masters) {
        masters.forEach(server -> {
            String masterPath = this.getMasterPath(server);
            HeartBeatTask heartBeatTask = new HeartBeatTask(System.currentTimeMillis(),
                80,
                2048,
                Sets.newHashSet(masterPath),
                Constants.MASTER_TYPE,
                registryClient);
            registryClient.persistEphemeral(masterPath, heartBeatTask.getHeartBeatInfo());
        });
    }

    private void persistWorkers(List<Server> workers) {
        workers.forEach(server -> {
            Set<String> workerPaths = this.getWorkerPaths(server);
            workerPaths.forEach(p -> {
                HeartBeatTask heartBeatTask = new HeartBeatTask(System.currentTimeMillis(),
                    80,
                    2048,
                    10,
                    workerPaths,
                    Constants.WORKER_TYPE,
                    registryClient,
                    5,
                    1
                );

                registryClient.persistEphemeral(p, heartBeatTask.getHeartBeatInfo());
            });
        });
    }

    private List<Server> createMasters() {
        Server s1 = createServer(NodeType.MASTER, "192.168.1.1");
        Server s2 = createServer(NodeType.MASTER, "192.168.1.2");
        return Lists.newArrayList(s1, s2);
    }

    private List<Server> createWorks() {
        Server s1 = createServer(NodeType.WORKER, "192.168.1.3");
        Server s2 = createServer(NodeType.WORKER, "192.168.1.4");
        return Lists.newArrayList(s1, s2);
    }

    private Server createServer(NodeType nodeType, String host) {
        Server server = new Server();
        server.setId(Mockito.anyInt());
        server.setHost(host);
        server.setResInfo("");
        server.setZkDirectory("");
        server.setLastHeartbeatTime(new Date());
        server.setCreateTime(new Date());
        switch (nodeType) {
            case MASTER:
                server.setPort(1234);
            case WORKER:
                server.setPort(1235);
            default:
                break;
        }
        return server;
    }

    private String getMasterPath(Server server) {
        String address = server.getHost() + ":" + server.getPort();
        return REGISTRY_DOLPHINSCHEDULER_MASTERS + "/" + address;
    }

    private Set<String> getWorkerPaths(Server server) {
        Set<String> workerPaths = Sets.newHashSet();
        String address = server.getHost() + ":" + server.getPort();
        for (String workGroup : workerGroups) {
            StringJoiner workerPathJoiner = new StringJoiner(SINGLE_SLASH);
            workerPathJoiner.add(REGISTRY_DOLPHINSCHEDULER_WORKERS);
            if (StringUtils.isEmpty(workGroup)) {
                workGroup = DEFAULT_WORKER_GROUP;
            }
            // trim and lower case is need
            workerPathJoiner.add(workGroup.trim().toLowerCase());
            workerPathJoiner.add(address);
            workerPaths.add(workerPathJoiner.toString());
        }
        return workerPaths;
    }

}
