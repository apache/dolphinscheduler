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

package org.apache.dolphinscheduler.api.cluster;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.cluster.AbstractCluster;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("apiClusterManager")
public class ClusterManager implements AutoCloseable {

    @Getter
    private final MasterCluster masterCluster = new MasterCluster();
    @Getter
    private final WorkerCluster workerCluster = new WorkerCluster();
    @Getter
    private final AlertCluster alertCluster = new AlertCluster();
    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private ApiConfig apiConfig;

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("ApiHeartbeatTimer-%d"));
    private final ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("ApiHeartbeatFetch-%d"));

    private List<AbstractCluster<? extends BaseHeartBeat>> clusters() {
        return Arrays.asList(masterCluster, workerCluster, alertCluster);
    }

    @PostConstruct
    public void start() {
        clusters().forEach(cluster -> cluster.start(registryClient, apiConfig.getHeartbeatFetchInterval(), executor));
        timer.scheduleWithFixedDelay(() -> clusters().forEach(AbstractCluster::refreshHeartbeats), 0,
                apiConfig.getHeartbeatFetchInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    public List<Server> listServer(RegistryNodeType type) {
        AbstractCluster<? extends BaseHeartBeat> cluster;
        switch (type) {
            case MASTER:
                cluster = masterCluster;
                break;
            case WORKER:
                cluster = workerCluster;
                break;
            case ALERT_SERVER:
                cluster = alertCluster;
                break;
            default:
                return registryClient.getServerList(type);
        }
        List<Server> result = new ArrayList<>();
        for (AbstractCluster.Node<? extends BaseHeartBeat> node : cluster.getNodes()) {
            ServerRegistration registration = node.getRegistration();
            Server server = new Server();
            server.setId(registration.getProcessId());
            server.setHost(registration.getHost());
            server.setPort(registration.getPort());
            server.setCreateTime(new Date(registration.getStartupTime()));
            server.setServerDirectory(type.getRegistryPath() + "/" + registration.getAddress());
            server.setMetadataStale(node.isStale());
            BaseHeartBeat heartbeat = node.getMetadata();
            if (heartbeat != null) {
                server.setHeartBeatInfo(JSONUtils.toJsonString(heartbeat));
                server.setLastHeartbeatTime(new Date(heartbeat.getReportTime()));
            }
            result.add(server);
        }
        return result;
    }

    @Override
    @PreDestroy
    public void close() {
        clusters().forEach(AbstractCluster::close);
        timer.shutdownNow();
        executor.shutdownNow();
    }
}
