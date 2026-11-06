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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.engine.ITaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.failover.FailoverCoordinator;
import org.apache.dolphinscheduler.server.master.failover.TaskFailover;
import org.apache.dolphinscheduler.server.master.failover.WorkflowFailover;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

class ClusterManagerHeartbeatTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final ExecutorService executor = mock(ExecutorService.class);
    private final ScheduledThreadPoolExecutor timer = mock(ScheduledThreadPoolExecutor.class);
    private final AtomicLong ticker = new AtomicLong();
    private final Map<RegistryNodeType, SubscribeListener> watches = new EnumMap<>(RegistryNodeType.class);
    private final Queue<Runnable> jobs = new ArrayDeque<>();
    private BiFunction<RegistryNodeType, String, BaseHeartBeat> query;
    private final ClusterManager owner = spy(new ClusterManager());

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
            jobs.add(invocation.getArgument(0));
            return null;
        }).when(executor).execute(any(Runnable.class));
        ReflectionTestUtils.setField(owner, "registryClient", registry);
        ReflectionTestUtils.setField(owner, "timer", timer);
        ReflectionTestUtils.setField(owner, "executor", executor);
        ReflectionTestUtils.setField(owner, "nanoTime", (LongSupplier) ticker::get);
        doAnswer(invocation -> query.apply(invocation.getArgument(0), invocation.getArgument(1)))
                .when(owner).fetchHeartbeat(any(), anyString());
        MasterConfig config = new MasterConfig();
        ReflectionTestUtils.setField(owner, "masterConfig", config);
        ReflectionTestUtils.setField(owner, "masterSlotManager", mock(MasterSlotManager.class));
        ReflectionTestUtils.setField(owner, "workerGroupChangeNotifier", mock(WorkerGroupChangeNotifier.class));
    }

    @AfterEach
    void tearDown() {
        owner.close();
    }

    private void poll() {
        owner.refreshHeartbeats();
        while (!jobs.isEmpty()) {
            jobs.remove().run();
        }
    }

    private WorkerHeartBeat worker(long startupTime) {
        return WorkerHeartBeat.builder().host("localhost").port(1234).processId(10).startupTime(startupTime)
                .reportTime(100).serverStatus(ServerStatus.NORMAL).workerGroup("analytics").build();
    }

    private void register(RegistryNodeType type, BaseHeartBeat heartbeat) {
        ServerRegistration registration = ServerRegistration.fromHeartBeat(heartbeat);
        when(registry.getServerNodeSet(type)).thenReturn(Collections.singleton(registration.getAddress()));
        when(registry.get(type.getRegistryPath() + "/" + registration.getAddress()))
                .thenReturn(JSONUtils.toJsonString(registration));
    }

    private void notify(RegistryNodeType type, Event.Type eventType, BaseHeartBeat heartbeat) {
        watches.get(type).notify(Event.builder().type(eventType)
                .eventData(JSONUtils.toJsonString(ServerRegistration.fromHeartBeat(heartbeat))).build());
    }

    @Test
    void startsWithoutCircularBeanReferences() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getDefaultListableBeanFactory().setAllowCircularReferences(false);
            context.getBeanFactory().registerSingleton("registryClient", registry);
            context.getBeanFactory().registerSingleton("registry", mock(Registry.class));
            MasterConfig config = mock(MasterConfig.class);
            when(config.getMasterAddress()).thenReturn("localhost:5678");
            context.getBeanFactory().registerSingleton("masterConfig", config);
            context.getBeanFactory().registerSingleton("masterSlotManager", mock(MasterSlotManager.class));
            context.getBeanFactory().registerSingleton("workerGroupChangeNotifier",
                    mock(WorkerGroupChangeNotifier.class));
            context.getBeanFactory().registerSingleton("masterServerLoadProtection",
                    mock(MasterServerLoadProtection.class));
            context.getBeanFactory().registerSingleton("metricsProvider", mock(MetricsProvider.class));
            context.getBeanFactory().registerSingleton("iTaskGroupCoordinator", mock(ITaskGroupCoordinator.class));
            context.getBeanFactory().registerSingleton("iWorkflowSerialCoordinator",
                    mock(IWorkflowSerialCoordinator.class));
            context.getBeanFactory().registerSingleton("iWorkflowRepository", mock(IWorkflowRepository.class));
            context.getBeanFactory().registerSingleton("taskFailover", mock(TaskFailover.class));
            context.getBeanFactory().registerSingleton("workflowInstanceDao", mock(WorkflowInstanceDao.class));
            context.getBeanFactory().registerSingleton("workflowFailover", mock(WorkflowFailover.class));
            context.register(ClusterManager.class, MasterRegistryClient.class, MasterCoordinator.class,
                    FailoverCoordinator.class);
            context.refresh();
            assertEquals(context.getBean(ClusterManager.class),
                    ReflectionTestUtils.getField(context.getBean(FailoverCoordinator.class), "clusterManager"));
        }
    }

    @Test
    void registrationIsUnknownUntilFirstHeartbeat() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
        assertEquals(Collections.singletonList("localhost:1234"),
                owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics"));
        verify(registry, never()).persistEphemeral(anyString(), anyString());
    }

    @Test
    void failedAndUnchangedHeartbeatsExpireWithoutRemovingMembers() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        poll();
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        poll();
        assertEquals(1, owner.getWorkerClusters().getServers().size());
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        query = (type, address) -> {
            throw new IllegalStateException("unreachable");
        };
        poll();
        assertEquals(1, owner.getWorkerClusters().getServers().size());
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        assertEquals(ServerStatus.NORMAL, heartbeat.getServerStatus());
        WorkerHeartBeat recovered = worker(1);
        recovered.setReportTime(101);
        query = (type, address) -> recovered;
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
        assertEquals(Collections.singletonList("localhost:1234"),
                owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics"));
    }

    @Test
    void responseAfterRemovalCannotResurrectWorker() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        query = (type, address) -> {
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
            return heartbeat;
        };
        poll();
        assertTrue(owner.getWorkerClusters().getServers().isEmpty());
    }

    @Test
    void oldResponseAndRemovalCannotAffectRestartedWorker() {
        WorkerHeartBeat previous = worker(1);
        WorkerHeartBeat restarted = worker(2);
        register(RegistryNodeType.WORKER, previous);
        owner.start();
        query = (type, address) -> {
            notify(RegistryNodeType.WORKER, Event.Type.ADD, restarted);
            return previous;
        };
        poll();
        notify(RegistryNodeType.WORKER, Event.Type.REMOVE, previous);
        assertEquals(2, workerView().getServerStartupTime());
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
        query = (type, address) -> restarted;
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
        assertEquals(Collections.singletonList("localhost:1234"),
                owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics"));
    }

    @Test
    void rejectsHeartbeatFromDifferentProcess() {
        register(RegistryNodeType.WORKER, worker(1));
        query = (type, address) -> worker(2);
        owner.start();
        poll();
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
    }

    @Test
    void removalDuringInitialListingIsAppliedAfterSnapshot() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        when(registry.getServerNodeSet(RegistryNodeType.WORKER)).thenAnswer(invocation -> {
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
            return Collections.singleton("localhost:1234");
        });
        owner.start();
        assertTrue(owner.getWorkerClusters().getServers().isEmpty());
    }

    @Test
    void failedReconciliationKeepsMembersAndRetries() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        when(registry.getServerNodeSet(RegistryNodeType.WORKER)).thenThrow(new IllegalStateException("disconnected"));
        ArgumentCaptor<ConnectionListener> listener = ArgumentCaptor.forClass(ConnectionListener.class);
        verify(registry).addConnectionStateListener(listener.capture());
        listener.getValue().onUpdate(ConnectionState.RECONNECTED);
        poll();
        assertEquals(1, owner.getWorkerClusters().getServers().size());
        doReturn(Collections.emptySet()).when(registry).getServerNodeSet(RegistryNodeType.WORKER);
        poll();
        assertTrue(owner.getWorkerClusters().getServers().isEmpty());
    }

    @Test
    void closeDiscardsInFlightResponseAndFurtherEvents() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        query = (type, address) -> {
            owner.close();
            return heartbeat;
        };
        poll();
        notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
        notify(RegistryNodeType.WORKER, Event.Type.ADD, worker(2));
        assertEquals(1, workerView().getServerStartupTime());
        assertNull(workerView().getHeartbeatProgressNanos());
        verify(executor).shutdownNow();
        verify(timer).shutdownNow();
    }

    @Test
    void removeAndReaddOfSameIdentityStillDiscardsOldResponse() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        query = (type, address) -> {
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
            notify(RegistryNodeType.WORKER, Event.Type.ADD, heartbeat);
            return heartbeat;
        };
        poll();
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
    }

    @Test
    void masterRpcFailureKeepsLastKnownSchedulingStatus() {
        MasterHeartBeat heartbeat = MasterHeartBeat.builder().host("localhost").port(5678).processId(10)
                .startupTime(1).reportTime(100).serverStatus(ServerStatus.NORMAL).build();
        register(RegistryNodeType.MASTER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        poll();
        query = (type, address) -> {
            throw new IllegalStateException("unreachable");
        };
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        poll();
        assertEquals(1, owner.getMasterClusters().getNormalServers().size());
    }

    @Test
    void slowRpcDoesNotPreventExpirationOrQueueDuplicateRequests() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        poll();
        owner.refreshHeartbeats();
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        owner.refreshHeartbeats();
        assertEquals(1, jobs.size());
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        heartbeat.setReportTime(101);
        jobs.remove().run();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
    }

    private WorkerServerMetadata workerView() {
        return owner.getWorkerClusters().getServer("localhost:1234").get();
    }
}
