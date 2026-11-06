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
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import javax.annotation.PreDestroy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClusterManager implements AutoCloseable {

    @Getter
    private final MasterClusters masterClusters = new MasterClusters();
    @Getter
    private final WorkerClusters workerClusters = new WorkerClusters();
    @Autowired
    private MasterSlotManager masterSlotManager;
    @Autowired
    private WorkerGroupChangeNotifier workerGroupChangeNotifier;
    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private MasterConfig masterConfig;
    // Local snapshots are read after startup; eager wiring cycles through MasterCoordinator and FailoverCoordinator.
    @Lazy
    @Autowired
    private MasterRegistryClient masterRegistryClient;

    private final Set<BaseServerMetadata> inFlight = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Runnable> pendingEvents = new ArrayList<>();
    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatTimer-%d"));
    private ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("MasterHeartbeatFetch-%d"));
    private LongSupplier nanoTime = System::nanoTime;
    private long intervalMillis;
    private boolean initializing;
    private boolean reconciling;
    private boolean reconcileRequired;
    private boolean closed;

    public void start() {
        intervalMillis = masterConfig.getHeartbeatFetchInterval().toMillis();
        masterClusters.registerListener(new MasterSlotChangeListenerAdaptor(masterSlotManager, masterClusters));
        synchronized (this) {
            initializing = true;
        }
        for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
            registryClient.subscribe(type.getRegistryPath(), new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    onRegistryEvent(type, event);
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.CHILDREN_ONLY;
                }
            });
        }
        registryClient.addConnectionStateListener(state -> {
            synchronized (this) {
                if (!closed && state == ConnectionState.RECONNECTED) {
                    reconcileRequired = true;
                }
            }
        });
        synchronized (this) {
            reconciling = true;
        }
        reconcile();
        timer.scheduleWithFixedDelay(this::refreshHeartbeats, 0, intervalMillis, TimeUnit.MILLISECONDS);
        workerGroupChangeNotifier.subscribeWorkerGroupsChange(workerClusters);
        workerGroupChangeNotifier.start();
        log.info("ClusterManager started...");
    }

    private void reconcile() {
        try {
            Map<RegistryNodeType, Map<String, ServerRegistration>> snapshot = new EnumMap<>(RegistryNodeType.class);
            for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
                Map<String, ServerRegistration> registrations = new HashMap<>();
                // Failed or partial reads must not be interpreted as departed members.
                for (String address : registryClient.getServerNodeSet(type)) {
                    ServerRegistration registration = JSONUtils.parseObject(
                            registryClient.get(type.getRegistryPath() + "/" + address), ServerRegistration.class);
                    if (registration == null || !address.equals(registration.getAddress())) {
                        throw new IllegalStateException("Invalid registration: " + address);
                    }
                    registrations.put(address, registration);
                }
                snapshot.put(type, registrations);
            }
            synchronized (this) {
                if (!closed) {
                    snapshot.forEach(this::applySnapshot);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to reconcile server registrations", ex);
            synchronized (this) {
                reconcileRequired = true;
            }
        } finally {
            synchronized (this) {
                reconciling = false;
                initializing = false;
                if (!closed) {
                    pendingEvents.forEach(Runnable::run);
                }
                pendingEvents.clear();
            }
        }
    }

    private synchronized void onRegistryEvent(RegistryNodeType type, Event event) {
        if (closed) {
            return;
        }
        ServerRegistration registration = JSONUtils.parseObject(event.getEventData(), ServerRegistration.class);
        if (registration == null) {
            log.error("Invalid registration event: {}", event);
            return;
        }
        Runnable change = () -> {
            if (event.getType() == Event.Type.REMOVE) {
                remove(type, registration);
            } else {
                add(type, registration);
            }
        };
        if (initializing) {
            pendingEvents.add(change);
        } else {
            change.run();
        }
    }

    private void applySnapshot(RegistryNodeType type, Map<String, ServerRegistration> registrations) {
        for (BaseServerMetadata server : servers(type)) {
            if (!registrations.containsKey(server.getAddress())) {
                remove(type, server);
            }
        }
        registrations.values().forEach(registration -> add(type, registration));
    }

    private List<? extends BaseServerMetadata> servers(RegistryNodeType type) {
        return type == RegistryNodeType.MASTER ? masterClusters.getServers() : workerClusters.getServers();
    }

    private BaseServerMetadata server(RegistryNodeType type, String address) {
        return type == RegistryNodeType.MASTER ? masterClusters.getServer(address).orElse(null)
                : workerClusters.getServer(address).orElse(null);
    }

    private boolean matches(BaseServerMetadata server, ServerRegistration registration) {
        return server != null && server.getProcessId() == registration.getProcessId()
                && server.getServerStartupTime() == registration.getStartupTime()
                && server.getAddress().equals(registration.getAddress());
    }

    private void add(RegistryNodeType type, ServerRegistration registration) {
        BaseServerMetadata previous = server(type, registration.getAddress());
        if (matches(previous, registration)) {
            return;
        }
        if (previous != null) {
            remove(type, previous);
        }
        if (type == RegistryNodeType.MASTER) {
            masterClusters.onServerAdded(MasterServerMetadata.builder().address(registration.getAddress())
                    .processId(registration.getProcessId()).serverStartupTime(registration.getStartupTime()).build());
        } else {
            workerClusters.onServerAdded(WorkerServerMetadata.builder().address(registration.getAddress())
                    .processId(registration.getProcessId()).serverStartupTime(registration.getStartupTime())
                    .workerGroup(null).serverStatus(ServerStatus.BUSY).build());
        }
    }

    private void remove(RegistryNodeType type, ServerRegistration registration) {
        BaseServerMetadata current = server(type, registration.getAddress());
        if (matches(current, registration)) {
            remove(type, current);
        }
    }

    private void remove(RegistryNodeType type, BaseServerMetadata server) {
        if (type == RegistryNodeType.MASTER) {
            masterClusters.onServerRemove((MasterServerMetadata) server);
        } else {
            workerClusters.onServerRemove((WorkerServerMetadata) server);
        }
    }

    synchronized void refreshHeartbeats() {
        if (closed) {
            return;
        }
        if (reconcileRequired && !reconciling) {
            reconcileRequired = false;
            reconciling = true;
            initializing = true;
            executor.execute(this::reconcile);
        }
        for (WorkerServerMetadata worker : workerClusters.getServers()) {
            if (!worker.isMetadataStale() && isStale(worker)) {
                worker.setMetadataStale(true);
                workerClusters.onServerUpdate(worker);
            }
        }
        for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
            for (BaseServerMetadata server : servers(type)) {
                if (inFlight.add(server)) {
                    executor.execute(() -> refresh(type, server));
                }
            }
        }
    }

    private boolean isStale(WorkerServerMetadata worker) {
        return worker.getHeartbeatProgressNanos() == null || nanoTime.getAsLong()
                - worker.getHeartbeatProgressNanos() >= TimeUnit.MILLISECONDS.toNanos(3 * intervalMillis);
    }

    private void refresh(RegistryNodeType type, BaseServerMetadata expected) {
        try {
            synchronized (this) {
                if (closed || server(type, expected.getAddress()) != expected) {
                    return;
                }
            }
            BaseHeartBeat heartbeat = fetchHeartbeat(type, expected.getAddress());
            ServerRegistration registration = ServerRegistration.fromHeartBeat(heartbeat);
            if (!matches(expected, registration)
                    || (type == RegistryNodeType.MASTER && !(heartbeat instanceof MasterHeartBeat))
                    || (type == RegistryNodeType.WORKER && !(heartbeat instanceof WorkerHeartBeat))) {
                throw new IllegalStateException("Heartbeat does not match the registered instance");
            }
            synchronized (this) {
                if (closed || server(type, expected.getAddress()) != expected) {
                    return;
                }
                if (type == RegistryNodeType.MASTER) {
                    masterClusters.onServerUpdate(MasterServerMetadata.parseFromHeartBeat((MasterHeartBeat) heartbeat));
                } else {
                    WorkerServerMetadata previous = (WorkerServerMetadata) expected;
                    WorkerServerMetadata updated = WorkerServerMetadata.parseFromHeartBeat((WorkerHeartBeat) heartbeat);
                    updated.setHeartbeatProgressNanos(previous.getHeartbeatProgressNanos() == null
                            || previous.getHeartbeatReportTime() != heartbeat.getReportTime()
                                    ? nanoTime.getAsLong()
                                    : previous.getHeartbeatProgressNanos());
                    updated.setMetadataStale(isStale(updated));
                    workerClusters.onServerUpdate(updated);
                }
            }
        } catch (Exception ex) {
            log.debug("Failed to fetch {} heartbeat from {}", type, expected.getAddress(), ex);
        } finally {
            synchronized (this) {
                inFlight.remove(expected);
            }
        }
    }

    BaseHeartBeat fetchHeartbeat(RegistryNodeType type, String address) {
        if (type == RegistryNodeType.MASTER) {
            return address.equals(masterConfig.getMasterAddress()) ? masterRegistryClient.getServerMetadata()
                    : Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata();
        }
        return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    @Override
    @PreDestroy
    public synchronized void close() {
        closed = true;
        timer.shutdownNow();
        executor.shutdownNow();
        inFlight.clear();
        pendingEvents.clear();
    }
}
