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
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.registry.api.cluster.AbstractCluster;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MasterClusters extends AbstractCluster<MasterServerMetadata>
        implements
            IClusters<MasterServerMetadata>,
            IClusters.IClustersChangeListener<MasterServerMetadata> {

    private String localAddress;
    private Supplier<MasterHeartBeat> localHeartbeat;

    public MasterClusters() {
        super(RegistryNodeType.MASTER);
    }

    void setLocalHeartbeat(String address, Supplier<MasterHeartBeat> heartbeat) {
        localAddress = address;
        localHeartbeat = heartbeat;
    }

    @Override
    protected MasterHeartBeat fetchHeartbeat(String address) {
        return address.equals(localAddress) ? localHeartbeat.get()
                : Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata();
    }

    @Override
    protected MasterServerMetadata parseHeartbeat(BaseHeartBeat heartbeat) {
        return MasterServerMetadata.parseFromHeartBeat((MasterHeartBeat) heartbeat);
    }

    @Override
    protected MasterServerMetadata initialMetadata(ServerRegistration registration) {
        return MasterServerMetadata.builder().address(registration.getAddress()).processId(registration.getProcessId())
                .serverStartupTime(registration.getStartupTime()).build();
    }

    private final List<IClustersChangeListener<MasterServerMetadata>> masterClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public List<MasterServerMetadata> getServers() {
        return UnmodifiableList.unmodifiableList(getValues());
    }

    @Override
    public Optional<MasterServerMetadata> getServer(final String address) {
        return getValue(address);
    }

    public List<MasterServerMetadata> getNormalServers() {
        List<MasterServerMetadata> normalMasterServers = getValues()
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
    public void onServerAdded(final MasterServerMetadata masterServer) {
        putServer(masterServer.toRegistration(), masterServer);
        onAdded(masterServer);
    }

    @Override
    protected void onAdded(MasterServerMetadata masterServer) {
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerAdded(masterServer);
        }
    }

    @Override
    public void onServerRemove(final MasterServerMetadata masterServer) {
        removeServer(masterServer.toRegistration());
    }

    @Override
    protected void onRemoved(MasterServerMetadata masterServer) {
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerRemove(masterServer);
        }
    }

    @Override
    public void onServerUpdate(final MasterServerMetadata masterServer) {
        MasterServerMetadata previous = putServer(masterServer.toRegistration(), masterServer);
        onUpdated(previous, masterServer, false);
    }

    @Override
    protected void onUpdated(MasterServerMetadata previous, MasterServerMetadata masterServer, boolean stale) {
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerUpdate(masterServer);
        }
    }

}
