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

import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MasterClusters extends AbstractClusterSubscribeListener<MasterServer> implements IClusters<MasterServer> {

    /**
     * Master address -> MasterServer
     **/
    private static final Map<String, MasterServer> masterServerMap = new ConcurrentHashMap<>();

    private static final List<IClustersChangeListener<MasterServer>> masterClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public List<MasterServer> getServers() {
        return UnmodifiableList.unmodifiableList(new ArrayList<>(masterServerMap.values()));
    }

    @Override
    public void registerListener(IClustersChangeListener<MasterServer> listener) {
        masterClusterChangeListeners.add(listener);
    }

    @Override
    MasterServer parseServerFromHeartbeat(String masterHeartBeatJson) {
        MasterHeartBeat masterHeartBeat = JSONUtils.parseObject(masterHeartBeatJson, MasterHeartBeat.class);
        if (masterHeartBeat == null) {
            return null;
        }
        return MasterServer.parseFromHeartBeat(masterHeartBeat);
    }

    @Override
    public void onServerAdded(MasterServer masterServer) {
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServer> listener : masterClusterChangeListeners) {
            listener.onServerAdded(masterServer);
        }
    }

    @Override
    public void onServerRemove(MasterServer masterServer) {
        masterServerMap.remove(masterServer.getAddress());
        for (IClustersChangeListener<MasterServer> listener : masterClusterChangeListeners) {
            listener.onServerRemove(masterServer);
        }
    }

    @Override
    public void onServerUpdate(MasterServer masterServer) {
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServer> listener : masterClusterChangeListeners) {
            listener.onServerUpdate(masterServer);
        }
    }

}
