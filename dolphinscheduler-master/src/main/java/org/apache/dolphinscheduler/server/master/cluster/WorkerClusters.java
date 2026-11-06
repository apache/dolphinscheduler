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
import org.apache.dolphinscheduler.common.model.ServerRegistration;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
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

    private final Set<String> fetching = ConcurrentHashMap.newKeySet();
    private boolean closed;

    private LongSupplier nanoTime = System::nanoTime;

    @Autowired
    private MasterConfig masterConfig;

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("WorkerHeartbeatTimer-%d"));
    private final ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("WorkerHeartbeatFetch-%d"));

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
    public synchronized void onServerAdded(WorkerServerMetadata workerServer) {
        if (closed) {
            return;
        }
        WorkerServerMetadata current = workerMapping.get(workerServer.getAddress());
        if (current != null) {
            if (current.getProcessId() == workerServer.getProcessId()
                    && current.getServerStartupTime() == workerServer.getServerStartupTime()) {
                return;
            }
            onServerRemove(current);
        }
        workerMapping.put(workerServer.getAddress(), workerServer);
        if (workerServer.getWorkerGroup() != null) {
            synchronized (configWorkerGroupMapping) {
                List<String> addWorkerGroupAddrList = configWorkerGroupMapping.get(workerServer.getWorkerGroup());
                if (addWorkerGroupAddrList == null) {
                    List<String> newWorkerGroupAddrList = new ArrayList<>();
                    newWorkerGroupAddrList.add(workerServer.getAddress());
                    configWorkerGroupMapping.put(workerServer.getWorkerGroup(), newWorkerGroupAddrList);
                } else if (!addWorkerGroupAddrList.contains(workerServer.getAddress())) {
                    addWorkerGroupAddrList.add(workerServer.getAddress());
                    configWorkerGroupMapping.put(workerServer.getWorkerGroup(), addWorkerGroupAddrList);
                }
            }
        }
        for (IClustersChangeListener<WorkerServerMetadata> listener : workerClusterChangeListeners) {
            listener.onServerAdded(workerServer);
        }
    }

    @Override
    public synchronized void onServerRemove(WorkerServerMetadata removedServer) {
        WorkerServerMetadata workerServer = workerMapping.get(removedServer.getAddress());
        if (closed || workerServer == null || workerServer.getProcessId() != removedServer.getProcessId()
                || workerServer.getServerStartupTime() != removedServer.getServerStartupTime()
                || !workerMapping.remove(workerServer.getAddress(), workerServer)) {
            return;
        }
        if (workerServer.getWorkerGroup() != null) {
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
            listener.onServerRemove(workerServer);
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
        ServerRegistration registration = JSONUtils.parseObject(event.getEventData(), ServerRegistration.class);
        if (registration == null) {
            log.error("Invalid registration event: {}", event);
            return;
        }
        WorkerServerMetadata server = WorkerServerMetadata.fromRegistration(registration);
        if (event.getType() == Event.Type.ADD) {
            onServerAdded(server);
        } else if (event.getType() == Event.Type.REMOVE) {
            onServerRemove(server);
        }
    }

    WorkerHeartBeat fetchHeartbeat(String address) {
        return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    public synchronized void refreshHeartbeats() {
        if (closed) {
            return;
        }
        long staleAfterNanos = masterConfig.getHeartbeatFetchInterval().toNanos() * 3;
        for (String address : getServerAddresses()) {
            WorkerServerMetadata expected = workerMapping.get(address);
            if (expected == null) {
                continue;
            }
            if (!expected.isMetadataStale() && isStale(expected, staleAfterNanos)) {
                expected.setMetadataStale(true);
                onServerUpdate(expected);
            }
            if (fetching.add(address)) {
                executor.execute(() -> {
                    try {
                        synchronized (this) {
                            if (closed || workerMapping.get(address) != expected) {
                                return;
                            }
                        }
                        WorkerHeartBeat heartbeat = fetchHeartbeat(address);
                        synchronized (this) {
                            if (!closed && workerMapping.get(address) == expected
                                    && expected.toRegistration().matches(heartbeat)) {
                                WorkerServerMetadata updated = WorkerServerMetadata.parseFromHeartBeat(heartbeat);
                                updated.setHeartbeatProgressNanos(expected.getHeartbeatProgressNanos() == null
                                        || expected.getHeartbeatReportTime() != heartbeat.getReportTime()
                                                ? nanoTime.getAsLong()
                                                : expected.getHeartbeatProgressNanos());
                                updated.setMetadataStale(isStale(updated, staleAfterNanos));
                                onServerUpdate(updated);
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Failed to fetch Worker heartbeat from {}", address, ex);
                    } finally {
                        fetching.remove(address);
                    }
                });
            }
        }
    }

    private boolean isStale(WorkerServerMetadata worker, long staleAfterNanos) {
        return worker.getHeartbeatProgressNanos() == null
                || nanoTime.getAsLong() - worker.getHeartbeatProgressNanos() >= staleAfterNanos;
    }

    @Override
    @PreDestroy
    public synchronized void close() {
        closed = true;
        timer.shutdownNow();
        executor.shutdownNow();
        workerMapping.clear();
        fetching.clear();
    }
}
