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

package org.apache.dolphinscheduler.registry.api.cluster;

import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Membership and heartbeat lifecycle for one node type. The service owns the shared executors. */
@Slf4j
public abstract class AbstractCluster<T> implements AutoCloseable {

    private final RegistryNodeType nodeType;
    private final Map<String, Member<T>> members = new HashMap<>();
    private final List<Runnable> pendingEvents = new ArrayList<>();
    private RegistryClient registryClient;
    private ExecutorService executor;
    private long staleAfterNanos;
    private LongSupplier nanoTime = System::nanoTime;
    private boolean reconciling;
    private boolean reconcileRequired;
    private boolean closed;

    protected AbstractCluster(RegistryNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public void start(RegistryClient registryClient, Duration interval, ExecutorService executor) {
        synchronized (this) {
            this.registryClient = registryClient;
            this.executor = executor;
            this.staleAfterNanos = interval.toNanos() * 3;
            reconciling = true;
        }
        registryClient.subscribe(nodeType.getRegistryPath(), new SubscribeListener() {

            @Override
            public void notify(Event event) {
                onRegistryEvent(event);
            }

            @Override
            public SubscribeScope getSubscribeScope() {
                return SubscribeScope.CHILDREN_ONLY;
            }
        });
        registryClient.addConnectionStateListener(state -> {
            synchronized (AbstractCluster.this) {
                if (!closed && state == ConnectionState.RECONNECTED) {
                    reconcileRequired = true;
                }
            }
        });
        reconcile();
    }

    private void reconcile() {
        try {
            Map<String, ServerRegistration> snapshot = new HashMap<>();
            // Failed or partial reads must not be interpreted as departed members.
            for (String address : registryClient.getServerNodeSet(nodeType)) {
                ServerRegistration registration = JSONUtils.parseObject(
                        registryClient.get(nodeType.getRegistryPath() + "/" + address), ServerRegistration.class);
                if (registration == null || !address.equals(registration.getAddress())) {
                    throw new IllegalStateException("Invalid registration: " + address);
                }
                snapshot.put(address, registration);
            }
            synchronized (this) {
                if (!closed) {
                    for (Member<T> member : new ArrayList<>(members.values())) {
                        if (!snapshot.containsKey(member.registration.getAddress())) {
                            removeServer(member.registration);
                        }
                    }
                    snapshot.values().forEach(this::addRegistration);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to reconcile {} registrations", nodeType, ex);
            synchronized (this) {
                reconcileRequired = true;
            }
        } finally {
            synchronized (this) {
                reconciling = false;
                if (!closed) {
                    pendingEvents.forEach(Runnable::run);
                }
                pendingEvents.clear();
            }
        }
    }

    private synchronized void onRegistryEvent(Event event) {
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
                removeServer(registration);
            } else {
                addRegistration(registration);
            }
        };
        if (reconciling) {
            pendingEvents.add(change);
        } else {
            change.run();
        }
    }

    private void addRegistration(ServerRegistration registration) {
        Member<T> previous = members.get(registration.getAddress());
        if (previous != null && previous.registration.equals(registration)) {
            return;
        }
        if (previous != null) {
            removeServer(previous.registration);
        }
        T metadata = initialMetadata(registration);
        members.put(registration.getAddress(), new Member<>(registration, metadata));
        notifyChange(() -> onAdded(metadata));
    }

    protected synchronized T putServer(ServerRegistration registration, T metadata) {
        Member<T> member = members.get(registration.getAddress());
        if (member == null || !member.registration.equals(registration)) {
            member = new Member<>(registration, null);
            members.put(registration.getAddress(), member);
        }
        T previous = member.metadata;
        member.metadata = metadata;
        return previous;
    }

    protected synchronized void removeServer(ServerRegistration registration) {
        Member<T> member = members.get(registration.getAddress());
        if (member != null && member.registration.equals(registration)) {
            members.remove(registration.getAddress());
            notifyChange(() -> onRemoved(member.metadata));
        }
    }

    public synchronized void refreshHeartbeats() {
        if (closed) {
            return;
        }
        if (reconcileRequired && !reconciling) {
            reconcileRequired = false;
            reconciling = true;
            executor.execute(this::reconcile);
        }
        for (Member<T> member : members.values()) {
            if (!member.stale && isStale(member)) {
                member.stale = true;
                notifyChange(() -> onUpdated(member.metadata, member.metadata, true));
            }
            if (!member.inFlight) {
                member.inFlight = true;
                executor.execute(() -> refresh(member));
            }
        }
    }

    private void refresh(Member<T> expected) {
        String address = expected.registration.getAddress();
        try {
            synchronized (this) {
                if (closed || members.get(address) != expected) {
                    return;
                }
            }
            BaseHeartBeat heartbeat = fetchHeartbeat(address);
            if (!expected.registration.matches(heartbeat)) {
                throw new IllegalStateException("Heartbeat does not match the registered instance");
            }
            T metadata = parseHeartbeat(heartbeat);
            synchronized (this) {
                if (closed || members.get(address) != expected) {
                    return;
                }
                if (expected.progressNanos == null || expected.reportTime != heartbeat.getReportTime()) {
                    expected.progressNanos = nanoTime.getAsLong();
                }
                expected.reportTime = heartbeat.getReportTime();
                T previous = expected.metadata;
                expected.metadata = metadata;
                expected.stale = isStale(expected);
                notifyChange(() -> onUpdated(previous, metadata, expected.stale));
            }
        } catch (Exception ex) {
            log.debug("Failed to fetch {} heartbeat from {}", nodeType, address, ex);
        } finally {
            synchronized (this) {
                expected.inFlight = false;
            }
        }
    }

    private boolean isStale(Member<T> member) {
        return member.progressNanos == null || nanoTime.getAsLong() - member.progressNanos >= staleAfterNanos;
    }

    protected synchronized List<T> getValues() {
        return members.values().stream().map(member -> member.metadata).collect(Collectors.toList());
    }

    protected synchronized Optional<T> getValue(String address) {
        Member<T> member = members.get(address);
        return member == null ? Optional.empty() : Optional.ofNullable(member.metadata);
    }

    public synchronized List<Node<T>> getNodes() {
        return members.values().stream()
                .map(member -> new Node<>(member.registration, member.metadata, isStale(member)))
                .collect(Collectors.toList());
    }

    protected T initialMetadata(ServerRegistration registration) {
        return null;
    }

    protected abstract BaseHeartBeat fetchHeartbeat(String address);

    protected abstract T parseHeartbeat(BaseHeartBeat heartbeat);

    protected void onAdded(T metadata) {
    }

    protected void onRemoved(T metadata) {
    }

    protected void onUpdated(T previous, T metadata, boolean stale) {
    }

    private void notifyChange(Runnable change) {
        try {
            change.run();
        } catch (Exception ex) {
            log.error("Failed to notify {} cluster change", nodeType, ex);
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        members.clear();
        pendingEvents.clear();
    }

    @Getter
    public static final class Node<T> {

        private final ServerRegistration registration;
        private final T metadata;
        private final boolean stale;

        private Node(ServerRegistration registration, T metadata, boolean stale) {
            this.registration = registration;
            this.metadata = metadata;
            this.stale = stale;
        }
    }

    private static final class Member<T> {

        private final ServerRegistration registration;
        private T metadata;
        private long reportTime;
        private Long progressNanos;
        private boolean stale = true;
        private boolean inFlight;

        private Member(ServerRegistration registration, T metadata) {
            this.registration = registration;
            this.metadata = metadata;
        }
    }
}
