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

package org.apache.dolphinscheduler.registry.api.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ServerMetadataCacheTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
    private final AtomicLong ticker = new AtomicLong();
    private final Map<RegistryNodeType, SubscribeListener> watches = new EnumMap<>(RegistryNodeType.class);
    private final List<Runnable> polls = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private BiFunction<RegistryNodeType, String, BaseHeartBeat> query;
    private ServerMetadataCache cache;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            String path = invocation.getArgument(0);
            RegistryNodeType type = path.equals(RegistryNodeType.MASTER.getRegistryPath())
                    ? RegistryNodeType.MASTER
                    : RegistryNodeType.WORKER;
            watches.put(type, invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(anyString(), any());
        doAnswer(invocation -> {
            polls.add(invocation.getArgument(0));
            return mock(ScheduledFuture.class);
        }).when(executor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());
        cache = new ServerMetadataCache(registry, Duration.ofSeconds(1),
                (type, address) -> query.apply(type, address), executor, ticker::get);
        SubscribeListener listener = new SubscribeListener() {

            @Override
            public void notify(Event event) {
                events.add(event);
            }

            @Override
            public SubscribeScope getSubscribeScope() {
                return SubscribeScope.CHILDREN_ONLY;
            }
        };
        cache.addListener(RegistryNodeType.MASTER, listener);
        cache.addListener(RegistryNodeType.WORKER, listener);
    }

    @AfterEach
    void tearDown() {
        cache.close();
    }

    @Test
    void registrationDoesNotMakeWorkerSchedulableUntilMetadataArrives() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        query = (type, address) -> metadata;
        cache.start();
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).get(0).isMetadataStale());
        assertNull(cache.getServerList(RegistryNodeType.WORKER).get(0).getHeartBeatInfo());
        assertEquals(ServerStatus.BUSY, workerFromEvent().getServerStatus());
        polls.get(0).run();
        Server server = cache.getServerList(RegistryNodeType.WORKER).get(0);
        assertFalse(server.isMetadataStale());
        assertEquals(metadata.getReportTime(), server.getLastHeartbeatTime().getTime());
        assertEquals("analytics", workerFromEvent().getWorkerGroup());
        assertEquals(ServerStatus.NORMAL, workerFromEvent().getServerStatus());
        verify(registry, never()).persistEphemeral(anyString(), anyString());
    }

    @Test
    void rpcFailureExpiresWorkerMetadataWithoutRemovingMembership() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        query = (type, address) -> metadata;
        cache.start();
        polls.get(0).run();
        query = (type, address) -> {
            throw new IllegalStateException("unreachable");
        };
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        polls.get(0).run();
        assertEquals(1, cache.getServerList(RegistryNodeType.WORKER).size());
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).get(0).isMetadataStale());
        assertEquals(ServerStatus.BUSY, workerFromEvent().getServerStatus());
        assertTrue(events.stream().noneMatch(event -> event.getType() == Event.Type.REMOVE));
        // Expiration must not mutate the producer's snapshot.
        assertEquals(ServerStatus.NORMAL, metadata.getServerStatus());
        WorkerHeartBeat recovered = worker(1);
        recovered.setReportTime(101);
        query = (type, address) -> recovered;
        polls.get(0).run();
        assertFalse(cache.getServerList(RegistryNodeType.WORKER).get(0).isMetadataStale());
        assertEquals(ServerStatus.NORMAL, workerFromEvent().getServerStatus());
    }

    @Test
    void stalledSamplingExpiresEvenWhenRpcKeepsSucceeding() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        query = (type, address) -> metadata;
        cache.start();
        polls.get(0).run();
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        polls.get(0).run();
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).get(0).isMetadataStale());
        assertEquals(ServerStatus.BUSY, workerFromEvent().getServerStatus());
    }

    @Test
    void masterRpcFailureKeepsLastKnownSchedulingStatus() {
        MasterHeartBeat metadata = MasterHeartBeat.builder().host("localhost").port(5678).processId(10)
                .startupTime(1).reportTime(100).serverStatus(ServerStatus.NORMAL).build();
        register(RegistryNodeType.MASTER, metadata);
        query = (type, address) -> metadata;
        cache.start();
        polls.get(0).run();
        query = (type, address) -> {
            throw new IllegalStateException("unreachable");
        };
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        polls.get(0).run();
        assertEquals(1, cache.getServerList(RegistryNodeType.MASTER).size());
        assertEquals(ServerStatus.NORMAL, JSONUtils.parseObject(events.get(events.size() - 1).getEventData(),
                MasterHeartBeat.class).getServerStatus());
        assertTrue(cache.getServerList(RegistryNodeType.MASTER).get(0).isMetadataStale());
    }

    @Test
    void responseAfterRemovalCannotResurrectWorker() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        cache.start();
        query = (type, address) -> {
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, metadata);
            return metadata;
        };
        polls.get(0).run();
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).isEmpty());
        assertEquals(Event.Type.REMOVE, events.get(events.size() - 1).getType());
    }

    @Test
    void responseAndRemovalFromPreviousProcessCannotAffectRestartedWorker() {
        WorkerHeartBeat previous = worker(1);
        WorkerHeartBeat restarted = worker(2);
        register(RegistryNodeType.WORKER, previous);
        cache.start();
        query = (type, address) -> {
            notify(RegistryNodeType.WORKER, Event.Type.ADD, restarted);
            return previous;
        };
        polls.get(0).run();
        notify(RegistryNodeType.WORKER, Event.Type.REMOVE, previous);
        Server server = cache.getServerList(RegistryNodeType.WORKER).get(0);
        assertEquals(2, server.getCreateTime().getTime());
        assertNull(server.getHeartBeatInfo());
        query = (type, address) -> restarted;
        polls.get(1).run();
        assertFalse(cache.getServerList(RegistryNodeType.WORKER).get(0).isMetadataStale());
    }

    @Test
    void rejectsMetadataFromDifferentProcess() {
        register(RegistryNodeType.WORKER, worker(1));
        query = (type, address) -> worker(2);
        cache.start();
        polls.get(0).run();
        assertNull(cache.getServerList(RegistryNodeType.WORKER).get(0).getHeartBeatInfo());
    }

    @Test
    void registrationEventDuringInitialListingIsAppliedAfterSnapshot() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        when(registry.getServerNodeSet(RegistryNodeType.WORKER)).thenAnswer(invocation -> {
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, metadata);
            return Collections.singleton("localhost:1234");
        });
        cache.start();
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).isEmpty());
        assertEquals(Event.Type.REMOVE, events.get(events.size() - 1).getType());
    }

    @Test
    void failedRegistryReconciliationDoesNotRemoveMembersAndIsRetried() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        cache.start();
        when(registry.getServerNodeSet(RegistryNodeType.WORKER)).thenThrow(new IllegalStateException("disconnected"));
        ArgumentCaptor<ConnectionListener> listener = ArgumentCaptor.forClass(ConnectionListener.class);
        verify(registry).addConnectionStateListener(listener.capture());
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        ArgumentCaptor<Runnable> reconcile = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(reconcile.capture());
        reconcile.getValue().run();
        assertEquals(1, cache.getServerList(RegistryNodeType.WORKER).size());
        assertTrue(events.stream().noneMatch(event -> event.getType() == Event.Type.REMOVE));
        verify(executor).schedule(any(Runnable.class), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void closeDiscardsInFlightResponse() {
        WorkerHeartBeat metadata = worker(1);
        register(RegistryNodeType.WORKER, metadata);
        cache.start();
        query = (type, address) -> {
            cache.close();
            return metadata;
        };
        polls.get(0).run();
        assertTrue(cache.getServerList(RegistryNodeType.WORKER).isEmpty());
        verify(executor).shutdownNow();
    }

    private WorkerHeartBeat worker(long startupTime) {
        return WorkerHeartBeat.builder().host("localhost").port(1234).processId(10).startupTime(startupTime)
                .reportTime(100).serverStatus(ServerStatus.NORMAL).cpuUsage(0.25).workerGroup("analytics").build();
    }

    private void register(RegistryNodeType type, BaseHeartBeat metadata) {
        ServerRegistration registration = ServerRegistration.fromHeartBeat(metadata);
        when(registry.getServerNodeSet(type)).thenReturn(Collections.singleton(registration.getAddress()));
        when(registry.get(type.getRegistryPath() + "/" + registration.getAddress()))
                .thenReturn(JSONUtils.toJsonString(registration));
    }

    private void notify(RegistryNodeType type, Event.Type eventType, BaseHeartBeat metadata) {
        watches.get(type).notify(Event.builder().type(eventType)
                .eventData(JSONUtils.toJsonString(ServerRegistration.fromHeartBeat(metadata))).build());
    }

    private WorkerHeartBeat workerFromEvent() {
        return JSONUtils.parseObject(events.get(events.size() - 1).getEventData(), WorkerHeartBeat.class);
    }
}
