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
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MasterClusters extends AbstractClusterSubscribeListener<MasterServerMetadata>
        implements
            IClusters<MasterServerMetadata> {

    /**
     * Master address -> MasterServer
     **/
    private final Map<String, MasterServerMetadata> masterServerMap = new ConcurrentHashMap<>();

    private final List<IClustersChangeListener<MasterServerMetadata>> masterClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public List<MasterServerMetadata> getServers() {
        return UnmodifiableList.unmodifiableList(new ArrayList<>(masterServerMap.values()));
    }

    @Override
    public Optional<MasterServerMetadata> getServer(final String address) {
        return Optional.ofNullable(masterServerMap.get(address));
    }

    public List<MasterServerMetadata> getNormalServers() {
        List<MasterServerMetadata> normalMasterServers = masterServerMap.values()
                .stream()
                .filter(masterServer -> masterServer.getServerStatus() == ServerStatus.NORMAL)
                .collect(Collectors.toList());
        return UnmodifiableList.unmodifiableList(normalMasterServers);
    }

    @Override
    public void registerListener(final IClustersChangeListener<MasterServerMetadata> listener) {
        masterClusterChangeListeners.add(listener);
    }

    @Override
    MasterServerMetadata parseServerFromHeartbeat(final String masterHeartBeatJson) {
        MasterHeartBeat masterHeartBeat = JSONUtils.parseObject(masterHeartBeatJson, MasterHeartBeat.class);
        if (masterHeartBeat == null) {
            return null;
        }
        return MasterServerMetadata.parseFromHeartBeat(masterHeartBeat);
    }

    @Override
    public void onServerAdded(final MasterServerMetadata masterServer) {
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerAdded(masterServer);
        }
    }

    @Override
    public void onServerRemove(final MasterServerMetadata masterServer) {
        masterServerMap.remove(masterServer.getAddress());
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerRemove(masterServer);
        }
    }

    @Override
    public void onServerUpdate(final MasterServerMetadata masterServer) {
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerUpdate(masterServer);
        }
    }

}
