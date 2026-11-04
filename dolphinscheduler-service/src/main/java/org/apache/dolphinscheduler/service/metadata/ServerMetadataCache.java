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

package org.apache.dolphinscheduler.service.metadata;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;

import lombok.extern.slf4j.Slf4j;

/** Registry membership and RPC metadata have independent lifecycles. */
@Slf4j
public class ServerMetadataCache implements AutoCloseable {

    private final RegistryClient registryClient;
    private final long refreshIntervalMillis;
    private final BiFunction<RegistryNodeType, String, BaseHeartBeat> metadataQuery;
    private final ScheduledThreadPoolExecutor executor;
    private final Map<RegistryNodeType, Map<String, Entry>> entries = new EnumMap<>(RegistryNodeType.class);
    private final Map<RegistryNodeType, List<SubscribeListener>> listeners = new EnumMap<>(RegistryNodeType.class);
    private final List<Runnable> pendingEvents = new ArrayList<>();
    private final LongSupplier nanoTime;
    private boolean reconciling;
    private boolean initializing;
    private boolean closed;

    public ServerMetadataCache(RegistryClient registryClient, Duration refreshInterval,
                               BiFunction<RegistryNodeType, String, BaseHeartBeat> metadataQuery) {
        this(registryClient, refreshInterval, metadataQuery, newExecutor(), System::nanoTime);
    }

    ServerMetadataCache(RegistryClient registryClient, Duration refreshInterval,
                        BiFunction<RegistryNodeType, String, BaseHeartBeat> metadataQuery,
                        ScheduledThreadPoolExecutor executor, LongSupplier nanoTime) {
        this.registryClient = registryClient;
        this.refreshIntervalMillis = refreshInterval.toMillis();
        if (refreshIntervalMillis <= 0) {
            throw new IllegalArgumentException("Metadata refresh interval must be positive");
        }
        this.metadataQuery = metadataQuery;
        this.executor = executor;
        this.nanoTime = nanoTime;
        this.executor.setRemoveOnCancelPolicy(true);
        for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
            entries.put(type, new HashMap<>());
            listeners.put(type, new ArrayList<>());
        }
    }

    private static ScheduledThreadPoolExecutor newExecutor() {
        return new ScheduledThreadPoolExecutor(8, ThreadUtils.newDaemonThreadFactory("ServerMetadataRefresh-%d"));
    }

    public static BaseHeartBeat queryMetadata(RegistryNodeType type, String address) {
        if (type == RegistryNodeType.MASTER) {
            return Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata();
        }
        return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    public synchronized void addListener(RegistryNodeType type, SubscribeListener listener) {
        listeners.get(type).add(listener);
    }

    public void start() {
        synchronized (this) {
            initializing = true;
        }
        for (RegistryNodeType type : entries.keySet()) {
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
        reconcile();
        registryClient.addConnectionStateListener(state -> {
            synchronized (this) {
                if (!closed && state == ConnectionState.RECONNECTED) {
                    executor.execute(this::reconcile);
                }
            }
        });
    }

    private void reconcile() {
        synchronized (this) {
            if (closed || reconciling) {
                return;
            }
            reconciling = true;
            initializing = true;
        }
        try {
            Map<RegistryNodeType, Map<String, ServerRegistration>> snapshot = new EnumMap<>(RegistryNodeType.class);
            for (RegistryNodeType type : entries.keySet()) {
                Map<String, ServerRegistration> servers = new HashMap<>();
                // Read strictly: a failed registry read must not look like an empty cluster.
                for (String address : registryClient.getServerNodeSet(type)) {
                    String value = registryClient.get(type.getRegistryPath() + "/" + address);
                    ServerRegistration registration = JSONUtils.parseObject(value, ServerRegistration.class);
                    if (registration == null || !address.equals(registration.getAddress())) {
                        throw new IllegalStateException("Invalid registration: " + address);
                    }
                    servers.put(address, registration);
                }
                snapshot.put(type, servers);
            }
            synchronized (this) {
                if (closed) {
                    return;
                }
                snapshot.forEach((type, servers) -> {
                    for (Entry entry : new ArrayList<>(entries.get(type).values())) {
                        if (!servers.containsKey(entry.registration.getAddress())) {
                            remove(type, entry.registration);
                        }
                    }
                    servers.values().forEach(registration -> add(type, registration));
                });
            }
        } catch (Exception ex) {
            log.error("Failed to reconcile server registrations", ex);
            synchronized (this) {
                if (!closed) {
                    executor.schedule(this::reconcile, refreshIntervalMillis, TimeUnit.MILLISECONDS);
                }
            }
        } finally {
            synchronized (this) {
                reconciling = false;
                initializing = false;
                pendingEvents.forEach(Runnable::run);
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

    private void add(RegistryNodeType type, ServerRegistration registration) {
        String address = registration.getAddress();
        Entry previous = entries.get(type).get(address);
        if (previous != null && previous.registration.equals(registration)) {
            return;
        }
        if (previous != null) {
            remove(type, previous.registration);
        }
        Entry entry = new Entry(registration);
        entries.get(type).put(address, entry);
        publish(type, entry, Event.Type.ADD);
        // One fixed-delay task per member: requests for the same instance never overlap or accumulate.
        entry.future = executor.scheduleWithFixedDelay(() -> refresh(type, entry),
                ThreadLocalRandom.current().nextLong(refreshIntervalMillis), refreshIntervalMillis,
                TimeUnit.MILLISECONDS);
    }

    private void remove(RegistryNodeType type, ServerRegistration registration) {
        Entry entry = entries.get(type).get(registration.getAddress());
        if (entry == null || !entry.registration.equals(registration)) {
            return;
        }
        entries.get(type).remove(registration.getAddress());
        entry.future.cancel(false);
        publish(type, entry, Event.Type.REMOVE);
    }

    private void refresh(RegistryNodeType type, Entry entry) {
        try {
            BaseHeartBeat metadata = metadataQuery.apply(type, entry.registration.getAddress());
            if (!entry.registration.matches(metadata)
                    || (type == RegistryNodeType.MASTER && !(metadata instanceof MasterHeartBeat))
                    || (type == RegistryNodeType.WORKER && !(metadata instanceof WorkerHeartBeat))) {
                throw new IllegalStateException("Metadata does not match the registered instance");
            }
            synchronized (this) {
                if (closed || entries.get(type).get(entry.registration.getAddress()) != entry) {
                    return;
                }
                if (entry.metadata == null || entry.metadata.getReportTime() != metadata.getReportTime()) {
                    entry.lastProgressNanos = nanoTime.getAsLong();
                }
                entry.metadata = metadata;
                publish(type, entry, Event.Type.UPDATE);
            }
        } catch (Exception ex) {
            log.debug("Failed to fetch {} metadata from {}", type, entry.registration.getAddress(), ex);
        } finally {
            synchronized (this) {
                if (!closed && entries.get(type).get(entry.registration.getAddress()) == entry
                        && type == RegistryNodeType.WORKER && isStale(entry)) {
                    publish(type, entry, Event.Type.UPDATE);
                }
            }
        }
    }

    private boolean isStale(Entry entry) {
        return entry.metadata == null || nanoTime.getAsLong() - entry.lastProgressNanos >= TimeUnit.MILLISECONDS
                .toNanos(3 * refreshIntervalMillis);
    }

    private void publish(RegistryNodeType type, Entry entry, Event.Type eventType) {
        String data = entry.metadata == null ? JSONUtils.toJsonString(entry.registration)
                : JSONUtils.toJsonString(entry.metadata);
        if (type == RegistryNodeType.WORKER && isStale(entry)) {
            WorkerHeartBeat unavailable = JSONUtils.parseObject(data, WorkerHeartBeat.class);
            unavailable.setServerStatus(ServerStatus.BUSY);
            data = JSONUtils.toJsonString(unavailable);
        }
        Event event = Event.builder().watchedPath(type.getRegistryPath())
                .eventPath(type.getRegistryPath() + "/" + entry.registration.getAddress())
                .eventData(data).type(eventType).build();
        for (SubscribeListener listener : listeners.get(type)) {
            try {
                listener.notify(event);
            } catch (Exception ex) {
                log.error("Failed to notify metadata listener for {}", event, ex);
            }
        }
    }

    public synchronized List<Server> getServerList(RegistryNodeType type) {
        if (!entries.containsKey(type)) {
            return registryClient.getServerList(type);
        }
        List<Server> servers = new ArrayList<>();
        for (Entry entry : entries.get(type).values()) {
            Server server = new Server();
            server.setHost(entry.registration.getHost());
            server.setPort(entry.registration.getPort());
            server.setId(entry.registration.getProcessId());
            server.setCreateTime(new Date(entry.registration.getStartupTime()));
            server.setServerDirectory(type.getRegistryPath() + "/" + entry.registration.getAddress());
            server.setMetadataStale(isStale(entry));
            if (entry.metadata != null) {
                server.setHeartBeatInfo(JSONUtils.toJsonString(entry.metadata));
                server.setLastHeartbeatTime(new Date(entry.metadata.getReportTime()));
            }
            servers.add(server);
        }
        return servers;
    }

    @Override
    public synchronized void close() {
        closed = true;
        executor.shutdownNow();
        entries.values().forEach(Map::clear);
        pendingEvents.clear();
    }

    private static final class Entry {

        private final ServerRegistration registration;
        private BaseHeartBeat metadata;
        private long lastProgressNanos;
        private ScheduledFuture<?> future;

        Entry(ServerRegistration registration) {
            this.registration = registration;
        }
    }
}
