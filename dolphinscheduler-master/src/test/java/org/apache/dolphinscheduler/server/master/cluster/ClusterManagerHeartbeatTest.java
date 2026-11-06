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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
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
import java.util.Date;
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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

class ClusterManagerHeartbeatTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final ExecutorService masterExecutor = mock(ExecutorService.class);
    private final ExecutorService workerExecutor = mock(ExecutorService.class);
    private final ScheduledThreadPoolExecutor masterTimer = mock(ScheduledThreadPoolExecutor.class);
    private final ScheduledThreadPoolExecutor workerTimer = mock(ScheduledThreadPoolExecutor.class);
    private final AtomicLong ticker = new AtomicLong();
    private final Map<RegistryNodeType, SubscribeListener> watches = new EnumMap<>(RegistryNodeType.class);
    private final Queue<Runnable> jobs = new ArrayDeque<>();
    private BiFunction<RegistryNodeType, String, BaseHeartBeat> query;
    private final ClusterManager owner = new ClusterManager();
    private final MasterClusters masterClusters = spy(new MasterClusters());
    private final WorkerClusters workerClusters = spy(new WorkerClusters());

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
        for (ExecutorService executor : new ExecutorService[]{masterExecutor, workerExecutor}) {
            doAnswer(invocation -> {
                jobs.add(invocation.getArgument(0));
                return null;
            }).when(executor).execute(any(Runnable.class));
        }
        ReflectionTestUtils.setField(owner, "registryClient", registry);
        ReflectionTestUtils.setField(masterClusters, "timer", masterTimer);
        ReflectionTestUtils.setField(masterClusters, "executor", masterExecutor);
        ReflectionTestUtils.setField(workerClusters, "timer", workerTimer);
        ReflectionTestUtils.setField(workerClusters, "executor", workerExecutor);
        ReflectionTestUtils.setField(owner, "masterClusters", masterClusters);
        ReflectionTestUtils.setField(owner, "workerClusters", workerClusters);
        ReflectionTestUtils.setField(workerClusters, "nanoTime", (LongSupplier) ticker::get);
        doAnswer(invocation -> query.apply(RegistryNodeType.MASTER, invocation.getArgument(0)))
                .when(masterClusters).fetchHeartbeat(anyString());
        doAnswer(invocation -> query.apply(RegistryNodeType.WORKER, invocation.getArgument(0)))
                .when(workerClusters).fetchHeartbeat(anyString());
        MasterConfig config = new MasterConfig();
        ReflectionTestUtils.setField(masterClusters, "masterConfig", config);
        ReflectionTestUtils.setField(workerClusters, "masterConfig", config);
        ReflectionTestUtils.setField(masterClusters, "masterRegistryClient", mock(MasterRegistryClient.class));
        ReflectionTestUtils.setField(owner, "masterSlotManager", mock(MasterSlotManager.class));
        ReflectionTestUtils.setField(owner, "workerGroupChangeNotifier", mock(WorkerGroupChangeNotifier.class));
    }

    @AfterEach
    void tearDown() {
        masterClusters.close();
        workerClusters.close();
    }

    private void poll() {
        masterClusters.refreshHeartbeats();
        workerClusters.refreshHeartbeats();
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
        Server server = new Server();
        server.setHost(registration.getHost());
        server.setPort(registration.getPort());
        server.setId(registration.getProcessId());
        server.setCreateTime(new Date(registration.getStartupTime()));
        server.setHeartBeatInfo(JSONUtils.toJsonString(registration));
        when(registry.getServerList(type)).thenReturn(Collections.singletonList(server));
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
            when(config.getHeartbeatFetchInterval()).thenReturn(java.time.Duration.ofSeconds(1));
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
            context.register(ClusterManager.class, MasterClusters.class, WorkerClusters.class,
                    MasterRegistryClient.class, MasterCoordinator.class, FailoverCoordinator.class);
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
    void removalDuringInitialListingIsAppliedAfterSnapshot() throws InterruptedException {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        java.util.List<Server> snapshot = registry.getServerList(RegistryNodeType.WORKER);
        Thread[] removal = new Thread[1];
        when(registry.getServerList(RegistryNodeType.WORKER)).thenAnswer(invocation -> {
            removal[0] = new Thread(() -> notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat));
            removal[0].start();
            return snapshot;
        });
        owner.start();
        removal[0].join(3000);
        assertTrue(owner.getWorkerClusters().getServers().isEmpty());
    }

    @Test
    void registryDataUpdateDoesNotReplaceTheRpcSnapshot() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        poll();
        WorkerServerMetadata snapshot = workerView();
        notify(RegistryNodeType.WORKER, Event.Type.UPDATE, heartbeat);
        assertEquals(snapshot, workerView());
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
    }

    @Test
    void closeDiscardsInFlightResponseAndFurtherEvents() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        query = (type, address) -> {
            masterClusters.close();
            workerClusters.close();
            return heartbeat;
        };
        poll();
        notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
        notify(RegistryNodeType.WORKER, Event.Type.ADD, worker(2));
        assertTrue(owner.getWorkerClusters().getServers().isEmpty());
        verify(masterExecutor).shutdownNow();
        verify(workerExecutor).shutdownNow();
        verify(masterTimer).shutdownNow();
        verify(workerTimer).shutdownNow();
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
        masterClusters.refreshHeartbeats();
        workerClusters.refreshHeartbeats();
        ticker.set(TimeUnit.SECONDS.toNanos(4));
        masterClusters.refreshHeartbeats();
        workerClusters.refreshHeartbeats();
        assertEquals(1, jobs.size());
        assertEquals(ServerStatus.BUSY, workerView().getServerStatus());
        heartbeat.setReportTime(101);
        jobs.remove().run();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
    }

    @Test
    void closingMasterPollingDoesNotStopWorkerPolling() {
        masterClusters.start();
        workerClusters.start();
        verify(masterTimer).scheduleWithFixedDelay(any(Runnable.class), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(1000L), org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
        verify(workerTimer).scheduleWithFixedDelay(any(Runnable.class), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(1000L), org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        masterClusters.close();
        poll();
        verify(masterExecutor).shutdownNow();
        verify(workerExecutor, never()).shutdownNow();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
    }

    private WorkerServerMetadata workerView() {
        return owner.getWorkerClusters().getServer("localhost:1234").get();
    }
}
