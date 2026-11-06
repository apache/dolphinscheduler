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
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterClusters
        implements
            IClusters<MasterServerMetadata>,
            IClusters.IClustersChangeListener<MasterServerMetadata>,
            SubscribeListener,
            AutoCloseable {

    /**
     * Master address -> MasterServer
     **/
    private final Map<String, MasterServerMetadata> masterServerMap = new ConcurrentHashMap<>();

    private final List<IClustersChangeListener<MasterServerMetadata>> masterClusterChangeListeners =
            new CopyOnWriteArrayList<>();

    private final Set<String> fetching = ConcurrentHashMap.newKeySet();
    private boolean closed;

    @Lazy
    @Autowired
    private MasterRegistryClient masterRegistryClient;

    @Autowired
    private MasterConfig masterConfig;

    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatTimer-%d"));
    private final ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatFetch-%d"));

    @PostConstruct
    public void start() {
        timer.scheduleWithFixedDelay(this::refreshHeartbeats, 0,
                masterConfig.getHeartbeatFetchInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

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
    public synchronized void onServerAdded(final MasterServerMetadata masterServer) {
        if (closed) {
            return;
        }
        MasterServerMetadata current = masterServerMap.get(masterServer.getAddress());
        if (current != null) {
            if (current.getProcessId() == masterServer.getProcessId()
                    && current.getServerStartupTime() == masterServer.getServerStartupTime()) {
                return;
            }
            onServerRemove(current);
        }
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerAdded(masterServer);
        }
    }

    @Override
    public synchronized void onServerRemove(final MasterServerMetadata masterServer) {
        MasterServerMetadata current = masterServerMap.get(masterServer.getAddress());
        if (closed || current == null || current.getProcessId() != masterServer.getProcessId()
                || current.getServerStartupTime() != masterServer.getServerStartupTime()
                || !masterServerMap.remove(masterServer.getAddress(), current)) {
            return;
        }
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerRemove(current);
        }
    }

    @Override
    public synchronized void onServerUpdate(final MasterServerMetadata masterServer) {
        if (closed) {
            return;
        }
        masterServerMap.put(masterServer.getAddress(), masterServer);
        for (IClustersChangeListener<MasterServerMetadata> listener : masterClusterChangeListeners) {
            listener.onServerUpdate(masterServer);
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
        MasterServerMetadata server = MasterServerMetadata.fromRegistration(registration);
        if (event.getType() == Event.Type.ADD) {
            onServerAdded(server);
        } else if (event.getType() == Event.Type.REMOVE) {
            onServerRemove(server);
        }
    }

    MasterHeartBeat fetchHeartbeat(String address) {
        return address.equals(masterConfig.getMasterAddress()) ? masterRegistryClient.getServerMetadata()
                : Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata();
    }

    public synchronized void refreshHeartbeats() {
        if (closed) {
            return;
        }
        for (String address : getServerAddresses()) {
            MasterServerMetadata expected = masterServerMap.get(address);
            if (expected != null && fetching.add(address)) {
                executor.execute(() -> {
                    try {
                        synchronized (this) {
                            if (closed || masterServerMap.get(address) != expected) {
                                return;
                            }
                        }
                        MasterHeartBeat heartbeat = fetchHeartbeat(address);
                        synchronized (this) {
                            if (!closed && masterServerMap.get(address) == expected
                                    && expected.toRegistration().matches(heartbeat)) {
                                onServerUpdate(MasterServerMetadata.parseFromHeartBeat(heartbeat));
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Failed to fetch Master heartbeat from {}", address, ex);
                    } finally {
                        fetching.remove(address);
                    }
                });
            }
        }
    }

    @Override
    @PreDestroy
    public synchronized void close() {
        closed = true;
        timer.shutdownNow();
        executor.shutdownNow();
        masterServerMap.clear();
        fetching.clear();
    }
}
