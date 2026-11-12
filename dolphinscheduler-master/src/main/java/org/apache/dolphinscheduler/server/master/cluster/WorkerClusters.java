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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkerClusters
        implements
            IClusters<WorkerServerMetadata>,
            IClusters.IClustersChangeListener<WorkerServerMetadata>,
            SubscribeListener,
            AutoCloseable,
            WorkerGroupChangeNotifier.WorkerGroupListener {

    // WorkerIdentifier(workerAddress) -> worker
    private final Map<String, WorkerServerMetadata> workerMapping = new ConcurrentHashMap<>();

    // WorkerGroup from db -> WorkerIdentifier(workerAddress)
    private final Map<String, List<String>> dbWorkerGroupMapping = new ConcurrentHashMap<>();

    // WorkerGroup from config -> WorkerIdentifier(workerAddress)
    private final Map<String, List<String>> configWorkerGroupMapping = new ConcurrentHashMap<>();

    private final List<IClustersChangeListener<WorkerServerMetadata>> workerClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    private final Map<String, ServerIdentity> registeredServers = new ConcurrentHashMap<>();
    private volatile boolean closed;

    @Autowired
    private MasterConfig masterConfig;

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("WorkerHeartbeatTimer-%d"));

    @PostConstruct
    public void start() {
        timer.scheduleWithFixedDelay(this::refreshHeartbeats, 0,
                masterConfig.getHeartbeatFetchInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<WorkerServerMetadata> getServers() {
        return UnmodifiableList.unmodifiableList(new ArrayList<>(workerMapping.values()));
    }

    @Override
    public List<String> getServerAddresses() {
        return new ArrayList<>(registeredServers.keySet());
    }

    @Override
    public Optional<ServerIdentity> getServerIdentity(String address) {
        return Optional.ofNullable(registeredServers.get(address));
    }

    @Override
    public synchronized void onServerAdded(ServerIdentity identity) {
        if (closed || identity.equals(registeredServers.get(identity.getAddress()))) {
            return;
        }
        ServerIdentity previous = registeredServers.get(identity.getAddress());
        if (previous != null) {
            onServerRemove(previous);
        }
        registeredServers.put(identity.getAddress(), identity);
        for (IClustersChangeListener<WorkerServerMetadata> listener : workerClusterChangeListeners) {
            listener.onServerAdded(identity);
        }
    }

    @Override
    public synchronized void onServerRemove(ServerIdentity identity) {
        if (closed || !registeredServers.remove(identity.getAddress(), identity)) {
            return;
        }
        WorkerServerMetadata workerServer = workerMapping.remove(identity.getAddress());
        if (workerServer != null && workerServer.getWorkerGroup() != null) {
            synchronized (configWorkerGroupMapping) {
                List<String> removeWorkerGroupAddrList = configWorkerGroupMapping.get(workerServer.getWorkerGroup());
                if (removeWorkerGroupAddrList != null
                        && removeWorkerGroupAddrList.contains(workerServer.getAddress())) {
                    removeWorkerGroupAddrList.remove(workerServer.getAddress());
                    if (removeWorkerGroupAddrList.isEmpty()) {
                        configWorkerGroupMapping.remove(workerServer.getWorkerGroup());
                    }
                }
            }
        }
        for (IClustersChangeListener<WorkerServerMetadata> listener : workerClusterChangeListeners) {
            listener.onServerRemove(identity);
        }
    }

    @Override
    public Optional<WorkerServerMetadata> getServer(final String address) {
        return Optional.ofNullable(workerMapping.get(address));
    }

    public List<String> getDbWorkerServerAddressByGroup(String workerGroup) {
        if (WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)) {
            return UnmodifiableList.unmodifiableList(new ArrayList<>(workerMapping.keySet()));
        }
        return dbWorkerGroupMapping.getOrDefault(workerGroup, Collections.emptyList());
    }

    public List<String> getConfigWorkerServerAddressByGroup(String workerGroup) {
        if (WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)) {
            return UnmodifiableList.unmodifiableList(new ArrayList<>(workerMapping.keySet()));
        }
        return configWorkerGroupMapping.getOrDefault(workerGroup, Collections.emptyList());
    }

    public List<String> getNormalWorkerServerAddressByGroup(String workerGroup) {
        List<String> dbWorkerAddresses = getDbWorkerServerAddressByGroup(workerGroup)
                .stream()
                .map(workerMapping::get)
                .filter(Objects::nonNull)
                .filter(workerServer -> workerServer.getServerStatus() == ServerStatus.NORMAL)
                .map(WorkerServerMetadata::getAddress)
                .collect(Collectors.toList());
        List<String> configWorkerAddresses = getConfigWorkerServerAddressByGroup(workerGroup)
                .stream()
                .map(workerMapping::get)
                .filter(Objects::nonNull)
                .filter(workerServer -> workerServer.getServerStatus() == ServerStatus.NORMAL)
                .map(WorkerServerMetadata::getAddress)
                .collect(Collectors.toList());
        dbWorkerAddresses.removeAll(configWorkerAddresses);
        dbWorkerAddresses.addAll(configWorkerAddresses);
        return UnmodifiableList.unmodifiableList(dbWorkerAddresses);
    }

    public boolean containsWorkerGroup(String workerGroup) {
        return WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)
                || dbWorkerGroupMapping.containsKey(workerGroup)
                || configWorkerGroupMapping.containsKey(workerGroup);
    }

    @Override
    public void registerListener(IClustersChangeListener<WorkerServerMetadata> listener) {
        workerClusterChangeListeners.add(listener);
    }

    @Override
    public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
        synchronized (dbWorkerGroupMapping) {
            for (WorkerGroup workerGroup : workerGroups) {
                dbWorkerGroupMapping.remove(workerGroup.getName());
            }
        }
    }

    @Override
    public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
        // The logic of adding WorkerGroup is the same as updating WorkerGroup
        // Both need to change the WorkerGroup mapping to the latest
        onWorkerGroupChange(workerGroups);
    }

    @Override
    public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            List<String> workerAddresses = WorkerGroupUtils.getWorkerAddressListFromWorkerGroup(workerGroup);
            synchronized (dbWorkerGroupMapping) {
                dbWorkerGroupMapping.put(workerGroup.getName(), workerAddresses);
            }
        }
    }

    @Override
    public synchronized void onServerUpdate(WorkerServerMetadata workerServer) {
        if (closed) {
            return;
        }
        WorkerServerMetadata previous = workerMapping.put(workerServer.getAddress(), workerServer);
        synchronized (configWorkerGroupMapping) {
            if (previous != null && previous.getWorkerGroup() != null
                    && !Objects.equals(previous.getWorkerGroup(), workerServer.getWorkerGroup())) {
                List<String> addresses = configWorkerGroupMapping.get(previous.getWorkerGroup());
                if (addresses != null) {
                    addresses.remove(workerServer.getAddress());
                    if (addresses.isEmpty()) {
                        configWorkerGroupMapping.remove(previous.getWorkerGroup());
                    }
                }
            }
            if (workerServer.getWorkerGroup() != null) {
                List<String> addresses = configWorkerGroupMapping.computeIfAbsent(workerServer.getWorkerGroup(),
                        group -> new ArrayList<>());
                if (!addresses.contains(workerServer.getAddress())) {
                    addresses.add(workerServer.getAddress());
                }
            }
        }
        for (IClustersChangeListener<WorkerServerMetadata> listener : workerClusterChangeListeners) {
            listener.onServerUpdate(workerServer);
        }
    }

    @Override
    public SubscribeScope getSubscribeScope() {
        return SubscribeScope.CHILDREN_ONLY;
    }

    @Override
    public synchronized void notify(Event event) {
        ServerIdentity identity = JSONUtils.parseObject(event.getEventData(), ServerIdentity.class);
        if (identity == null) {
            log.error("Invalid registration event: {}", event);
            return;
        }
        if (event.getType() == Event.Type.ADD) {
            onServerAdded(identity);
        } else if (event.getType() == Event.Type.REMOVE) {
            onServerRemove(identity);
        }
    }

    WorkerHeartBeat fetchHeartbeat(String address) {
        return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    public void refreshHeartbeats() {
        for (ServerIdentity expected : new ArrayList<>(registeredServers.values())) {
            if (closed) {
                return;
            }
            try {
                WorkerHeartBeat heartbeat = fetchHeartbeat(expected.getAddress());
                synchronized (this) {
                    if (closed || registeredServers.get(expected.getAddress()) != expected) {
                        continue;
                    }
                    if (heartbeat == null) {
                        logHeartbeatFailure(expected.getAddress(), "empty heartbeat");
                    } else if (!expected.matches(heartbeat)) {
                        logHeartbeatFailure(expected.getAddress(),
                                "unexpected heartbeat identity " + ServerIdentity.fromHeartBeat(heartbeat));
                    } else {
                        onServerUpdate(WorkerServerMetadata.parseFromHeartBeat(heartbeat));
                    }
                }
            } catch (Exception ex) {
                log.debug("Failed to fetch Worker heartbeat from {}", expected.getAddress(), ex);
                if (!closed && registeredServers.get(expected.getAddress()) == expected) {
                    logHeartbeatFailure(expected.getAddress(), "RPC failed: " + ex.getMessage());
                }
            }
        }
    }

    private void logHeartbeatFailure(String address, String reason) {
        WorkerServerMetadata metadata = workerMapping.get(address);
        String age = metadata == null ? "never received"
                : (System.currentTimeMillis() - metadata.getHeartbeatReceivedTime()) + "ms";
        log.warn("Failed to fetch Worker heartbeat from {}: reason={}, heartbeatAge={}", address, reason, age);
    }

    @Override
    @PreDestroy
    public synchronized void close() {
        closed = true;
        timer.shutdownNow();
        workerMapping.clear();
        registeredServers.clear();
    }
}
