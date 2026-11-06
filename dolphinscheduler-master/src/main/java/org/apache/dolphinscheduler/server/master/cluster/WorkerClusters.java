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
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.cluster.AbstractCluster;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class WorkerClusters extends AbstractCluster<WorkerServerMetadata>
        implements
            IClusters<WorkerServerMetadata>,
            IClusters.IClustersChangeListener<WorkerServerMetadata>,
            WorkerGroupChangeNotifier.WorkerGroupListener {

    public WorkerClusters() {
        super(RegistryNodeType.WORKER);
    }

    @Override
    protected WorkerHeartBeat fetchHeartbeat(String address) {
        return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    @Override
    protected WorkerServerMetadata parseHeartbeat(BaseHeartBeat heartbeat) {
        return WorkerServerMetadata.parseFromHeartBeat((WorkerHeartBeat) heartbeat);
    }

    @Override
    protected WorkerServerMetadata initialMetadata(ServerRegistration registration) {
        return WorkerServerMetadata.builder().address(registration.getAddress()).processId(registration.getProcessId())
                .serverStartupTime(registration.getStartupTime()).workerGroup(null).serverStatus(ServerStatus.BUSY)
                .build();
    }

    // WorkerGroup from db -> WorkerIdentifier(workerAddress)
    private final Map<String, List<String>> dbWorkerGroupMapping = new ConcurrentHashMap<>();

    // WorkerGroup from config -> WorkerIdentifier(workerAddress)
    private final Map<String, List<String>> configWorkerGroupMapping = new ConcurrentHashMap<>();

    private final List<IClustersChangeListener<WorkerServerMetadata>> workerClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public List<WorkerServerMetadata> getServers() {
        return UnmodifiableList.unmodifiableList(getValues());
    }

    @Override
    public Optional<WorkerServerMetadata> getServer(final String address) {
        return getValue(address);
    }

    public List<String> getDbWorkerServerAddressByGroup(String workerGroup) {
        if (WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)) {
            return UnmodifiableList.unmodifiableList(
                    getValues().stream().map(WorkerServerMetadata::getAddress).collect(Collectors.toList()));
        }
        return dbWorkerGroupMapping.getOrDefault(workerGroup, Collections.emptyList());
    }

    public List<String> getConfigWorkerServerAddressByGroup(String workerGroup) {
        if (WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)) {
            return UnmodifiableList.unmodifiableList(
                    getValues().stream().map(WorkerServerMetadata::getAddress).collect(Collectors.toList()));
        }
        return configWorkerGroupMapping.getOrDefault(workerGroup, Collections.emptyList());
    }

    public List<String> getNormalWorkerServerAddressByGroup(String workerGroup) {
        List<String> dbWorkerAddresses = getDbWorkerServerAddressByGroup(workerGroup)
                .stream()
                .map(address -> getValue(address).orElse(null))
                .filter(Objects::nonNull)
                .filter(workerServer -> workerServer.getServerStatus() == ServerStatus.NORMAL)
                .map(WorkerServerMetadata::getAddress)
                .collect(Collectors.toList());
        List<String> configWorkerAddresses = getConfigWorkerServerAddressByGroup(workerGroup)
                .stream()
                .map(address -> getValue(address).orElse(null))
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
    public void onServerAdded(WorkerServerMetadata workerServer) {
        putServer(workerServer.toRegistration(), workerServer);
        onAdded(workerServer);
    }

    @Override
    protected void onAdded(WorkerServerMetadata workerServer) {
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
    public void onServerRemove(WorkerServerMetadata removedServer) {
        removeServer(removedServer.toRegistration());
    }

    @Override
    protected void onRemoved(WorkerServerMetadata workerServer) {
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
    public void onServerUpdate(WorkerServerMetadata workerServer) {
        WorkerServerMetadata previous = putServer(workerServer.toRegistration(), workerServer);
        onUpdated(previous, workerServer, false);
    }

    @Override
    protected void onUpdated(WorkerServerMetadata previous, WorkerServerMetadata workerServer, boolean stale) {
        workerServer.setMetadataStale(stale);
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
}
