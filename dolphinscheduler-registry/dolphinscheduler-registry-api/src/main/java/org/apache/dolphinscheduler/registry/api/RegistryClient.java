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

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegistryClient {

    private static final String EMPTY = "";
    private IStoppable stoppable;

    private final Registry registry;

    public RegistryClient(Registry registry) {
        this.registry = registry;
        if (!registry.exists(RegistryNodeType.MASTER.getRegistryPath())) {
            registry.put(RegistryNodeType.MASTER.getRegistryPath(), EMPTY, false);
        }
        if (!registry.exists(RegistryNodeType.WORKER.getRegistryPath())) {
            registry.put(RegistryNodeType.WORKER.getRegistryPath(), EMPTY, false);
        }
        if (!registry.exists(RegistryNodeType.ALERT_SERVER.getRegistryPath())) {
            registry.put(RegistryNodeType.ALERT_SERVER.getRegistryPath(), EMPTY, false);
        }
        if (!registry.exists(RegistryNodeType.FAILOVER_FINISH_NODES.getRegistryPath())) {
            registry.put(RegistryNodeType.FAILOVER_FINISH_NODES.getRegistryPath(), EMPTY, false);
        }
        cleanHistoryFailoverFinishedNodes();
    }

    public boolean isConnected() {
        return registry.isConnected();

    }

    public void connectUntilTimeout(@NonNull Duration duration) throws RegistryException {
        registry.connectUntilTimeout(duration);
    }

    public List<Server> getServerList(RegistryNodeType registryNodeType) {
        Map<String, String> serverMaps = getServerMaps(registryNodeType);

        List<Server> serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverMaps.entrySet()) {
            String serverPath = entry.getKey();
            String heartBeatJson = entry.getValue();
            if (StringUtils.isEmpty(heartBeatJson)) {
                log.error("The heartBeatJson is empty, serverPath: {}", serverPath);
                continue;
            }
            Server server = new Server();
            switch (registryNodeType) {
                case MASTER:
                    MasterHeartBeat masterHeartBeat = JSONUtils.parseObject(heartBeatJson, MasterHeartBeat.class);
                    server.setCreateTime(new Date(masterHeartBeat.getStartupTime()));
                    server.setLastHeartbeatTime(new Date(masterHeartBeat.getReportTime()));
                    server.setId(masterHeartBeat.getProcessId());
                    server.setHost(masterHeartBeat.getHost());
                    server.setPort(masterHeartBeat.getPort());
                    break;
                case WORKER:
                    WorkerHeartBeat workerHeartBeat = JSONUtils.parseObject(heartBeatJson, WorkerHeartBeat.class);
                    server.setCreateTime(new Date(workerHeartBeat.getStartupTime()));
                    server.setLastHeartbeatTime(new Date(workerHeartBeat.getReportTime()));
                    server.setId(workerHeartBeat.getProcessId());
                    server.setHost(workerHeartBeat.getHost());
                    server.setPort(workerHeartBeat.getPort());
                    break;
                case ALERT_SERVER:
                    AlertServerHeartBeat alertServerHeartBeat =
                            JSONUtils.parseObject(heartBeatJson, AlertServerHeartBeat.class);
                    server.setCreateTime(new Date(alertServerHeartBeat.getStartupTime()));
                    server.setLastHeartbeatTime(new Date(alertServerHeartBeat.getReportTime()));
                    server.setId(alertServerHeartBeat.getProcessId());
                    server.setHost(alertServerHeartBeat.getHost());
                    server.setPort(alertServerHeartBeat.getPort());
                    break;
                default:
                    log.warn("unknown registry node type: {}", registryNodeType);
            }

            server.setResInfo(heartBeatJson);
            // todo: add host, port in heartBeat Info, so that we don't need to parse this again
            server.setZkDirectory(registryNodeType.getRegistryPath() + "/" + serverPath);
            serverList.add(server);
        }
        return serverList;
    }

    public Optional<Server> getRandomServer(final RegistryNodeType registryNodeType) {
        final List<Server> serverList = getServerList(registryNodeType);
        if (CollectionUtils.isEmpty(serverList)) {
            return Optional.empty();
        }
        final Server server = serverList.get(RandomUtils.nextInt(0, serverList.size()));
        return Optional.ofNullable(server);
    }

    /**
     * Return server host:port -> value
     */
    public Map<String, String> getServerMaps(RegistryNodeType nodeType) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            Collection<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                serverMap.putIfAbsent(server, get(nodeType.getRegistryPath() + Constants.SINGLE_SLASH + server));
            }
        } catch (Exception e) {
            log.error("get server list failed", e);
        }

        return serverMap;
    }

    public boolean checkNodeExists(String host, RegistryNodeType nodeType) {
        return getServerMaps(nodeType).keySet()
                .stream()
                .anyMatch(it -> it.contains(host));
    }

    public void close() throws IOException {
        registry.close();
    }

    public void persistEphemeral(String key, String value) {
        registry.put(key, value, true);
    }

    public void persist(String key, String value) {
        log.info("persist key: {}, value: {}", key, value);
        registry.put(key, value, false);
    }

    public void remove(String key) {
        registry.delete(key);
    }

    public String get(String key) {
        return registry.get(key);
    }

    public void subscribe(String path, SubscribeListener listener) {
        registry.subscribe(path, listener);
    }

    public void addConnectionStateListener(ConnectionListener listener) {
        registry.addConnectionStateListener(listener);
    }

    public boolean exists(String key) {
        return registry.exists(key);
    }

    public boolean getLock(String key) {
        if (!registry.isConnected()) {
            throw new IllegalStateException("The registry is not connected");
        }
        return registry.acquireLock(key);
    }

    public boolean releaseLock(String key) {
        return registry.releaseLock(key);
    }

    public void setStoppable(IStoppable stoppable) {
        this.stoppable = stoppable;
    }

    public IStoppable getStoppable() {
        return stoppable;
    }

    public Collection<String> getChildrenKeys(final String key) {
        return registry.children(key);
    }

    public Set<String> getServerNodeSet(RegistryNodeType nodeType) {
        try {
            return new HashSet<>(getServerNodes(nodeType));
        } catch (Exception e) {
            throw new RegistryException("Failed to get server node: " + nodeType, e);
        }
    }

    private Collection<String> getServerNodes(RegistryNodeType nodeType) {
        return getChildrenKeys(nodeType.getRegistryPath());
    }

    private void cleanHistoryFailoverFinishedNodes() {
        // Clean the history failover finished nodes
        // which failover is before the current time minus 1 week
        final Collection<String> failoverFinishedNodes =
                registry.children(RegistryNodeType.FAILOVER_FINISH_NODES.getRegistryPath());
        if (CollectionUtils.isEmpty(failoverFinishedNodes)) {
            return;
        }
        for (final String failoverFinishedNode : failoverFinishedNodes) {
            try {
                final String failoverFinishTime = registry.get(failoverFinishedNode);
                if (System.currentTimeMillis() - Long.parseLong(failoverFinishTime) > TimeUnit.DAYS.toMillis(7)) {
                    registry.delete(failoverFinishedNode);
                    log.info(
                            "Clear the failover finished node: {} which failover time is before the current time minus 1 week",
                            failoverFinishedNode);
                }
            } catch (Exception ex) {
                log.error("Failed to clean the failoverFinishedNode: {}", failoverFinishedNode, ex);
            }
        }
    }
}
