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
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class WorkerClusters extends AbstractClusterSubscribeListener<WorkerServer>
        implements
            IClusters<WorkerServer>,
            WorkerGroupChangeNotifier.WorkerGroupListener {

    // WorkerIdentifier(workerAddress) -> worker
    private static final Map<String, WorkerServer> workerMapping = new ConcurrentHashMap<>();

    // WorkerGroup -> WorkerIdentifier(workerAddress)
    private static final Map<String, List<String>> workerGroupMapping = new ConcurrentHashMap<>();

    private static final List<IClustersChangeListener<WorkerServer>> workerClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public List<WorkerServer> getServers() {
        return UnmodifiableList.unmodifiableList(new ArrayList<>(workerMapping.values()));
    }

    public List<String> getWorkerServerAddressByGroup(String workerGroup) {
        if (WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)) {
            return UnmodifiableList.unmodifiableList(new ArrayList<>(workerMapping.keySet()));
        }
        return workerGroupMapping.getOrDefault(workerGroup, Collections.emptyList());
    }

    public List<String> getNormalWorkerServerAddressByGroup(String workerGroup) {
        List<String> normalWorkerAddresses = getWorkerServerAddressByGroup(workerGroup)
                .stream()
                .map(workerMapping::get)
                .filter(Objects::nonNull)
                .filter(workerServer -> workerServer.getServerStatus() == ServerStatus.NORMAL)
                .map(WorkerServer::getAddress)
                .collect(Collectors.toList());
        return UnmodifiableList.unmodifiableList(normalWorkerAddresses);
    }

    public boolean containsWorkerGroup(String workerGroup) {
        return WorkerGroupUtils.getDefaultWorkerGroup().equals(workerGroup)
                || workerGroupMapping.containsKey(workerGroup);
    }

    @Override
    public void registerListener(IClustersChangeListener<WorkerServer> listener) {
        workerClusterChangeListeners.add(listener);
    }

    @Override
    public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            workerGroupMapping.remove(workerGroup.getName());
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
            List<String> activeWorkers = WorkerGroupUtils.getWorkerAddressListFromWorkerGroup(workerGroup)
                    .stream()
                    .map(workerMapping::get)
                    .filter(Objects::nonNull)
                    .map(WorkerServer::getAddress)
                    .collect(Collectors.toList());
            workerGroupMapping.put(workerGroup.getName(), activeWorkers);
        }
    }

    @Override
    WorkerServer parseServerFromHeartbeat(String serverHeartBeatJson) {
        WorkerHeartBeat workerHeartBeat = JSONUtils.parseObject(serverHeartBeatJson, WorkerHeartBeat.class);
        if (workerHeartBeat == null) {
            return null;
        }
        return WorkerServer.parseFromHeartBeat(workerHeartBeat);
    }

    @Override
    public void onServerAdded(WorkerServer workerServer) {
        workerMapping.put(workerServer.getAddress(), workerServer);
        for (IClustersChangeListener<WorkerServer> listener : workerClusterChangeListeners) {
            listener.onServerAdded(workerServer);
        }
    }

    @Override
    public void onServerRemove(WorkerServer workerServer) {
        workerMapping.remove(workerServer.getAddress(), workerServer);
        for (IClustersChangeListener<WorkerServer> listener : workerClusterChangeListeners) {
            listener.onServerRemove(workerServer);
        }
    }

    @Override
    public void onServerUpdate(WorkerServer workerServer) {
        workerMapping.put(workerServer.getAddress(), workerServer);
        for (IClustersChangeListener<WorkerServer> listener : workerClusterChangeListeners) {
            listener.onServerUpdate(workerServer);
        }
    }
}
