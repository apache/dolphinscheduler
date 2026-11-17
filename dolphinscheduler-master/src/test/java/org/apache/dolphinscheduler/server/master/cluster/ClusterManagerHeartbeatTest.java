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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
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

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class ClusterManagerHeartbeatTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final ScheduledThreadPoolExecutor masterTimer = mock(ScheduledThreadPoolExecutor.class);
    private final ScheduledThreadPoolExecutor workerTimer = mock(ScheduledThreadPoolExecutor.class);
    private final Map<RegistryNodeType, SubscribeListener> watches = new EnumMap<>(RegistryNodeType.class);
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
        ReflectionTestUtils.setField(owner, "registryClient", registry);
        ReflectionTestUtils.setField(masterClusters, "timer", masterTimer);
        ReflectionTestUtils.setField(workerClusters, "timer", workerTimer);
        ReflectionTestUtils.setField(owner, "masterClusters", masterClusters);
        ReflectionTestUtils.setField(owner, "workerClusters", workerClusters);
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
    }

    private WorkerHeartBeat worker(long startupTime) {
        return WorkerHeartBeat.builder().host("localhost").port(1234).processId(10).startupTime(startupTime)
                .reportTime(100).serverStatus(ServerStatus.NORMAL).workerGroup("analytics").build();
    }

    private void register(RegistryNodeType type, BaseHeartBeat heartbeat) {
        ServerIdentity identity = ServerIdentity.fromHeartBeat(heartbeat);
        Server server = new Server();
        server.setHost(identity.getHost());
        server.setPort(identity.getPort());
        server.setId(identity.getProcessId());
        server.setCreateTime(new Date(identity.getStartupTime()));
        server.setHeartBeatInfo(JSONUtils.toJsonString(identity));
        when(registry.getServerList(type)).thenReturn(Collections.singletonList(server));
    }

    private void notify(RegistryNodeType type, Event.Type eventType, BaseHeartBeat heartbeat) {
        watches.get(type).notify(Event.builder().type(eventType)
                .eventData(JSONUtils.toJsonString(ServerIdentity.fromHeartBeat(heartbeat))).build());
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
        assertFalse(workerClusters.getServer("localhost:1234").isPresent());
        assertEquals(Collections.singletonList("localhost:1234"), workerClusters.getServerAddresses());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
        query = (type, address) -> null;
        poll();
        assertTrue(workerClusters.getServers().isEmpty());
        query = (type, address) -> heartbeat;
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
        assertEquals(Collections.singletonList("localhost:1234"),
                owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics"));
        verify(registry, never()).persistEphemeral(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"exception", "empty", "foreign"})
    void failedHeartbeatKeepsMetadataTimeAndSchedulingEligibility(String failure) {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        IClusters.IClustersChangeListener<WorkerServerMetadata> listener =
                mock(IClusters.IClustersChangeListener.class);
        workerClusters.registerListener(listener);
        query = (type, address) -> heartbeat;
        owner.start();
        verify(listener).onServerAdded(ServerIdentity.fromHeartBeat(heartbeat));
        verify(listener, never()).onServerUpdate(any());
        poll();
        WorkerServerMetadata snapshot = workerView();
        verify(listener).onServerUpdate(snapshot);
        assertTrue(snapshot.getHeartbeatReceivedTime() > 0);
        clearInvocations(listener);
        query = (type, address) -> {
            switch (failure) {
                case "empty":
                    return null;
                case "foreign":
                    return worker(2);
                default:
                    throw new IllegalStateException("unreachable");
            }
        };
        poll();
        assertSame(snapshot, workerView());
        assertEquals(snapshot.getHeartbeatReceivedTime(), workerView().getHeartbeatReceivedTime());
        assertTrue(workerClusters.getServerIdentity("localhost:1234").isPresent());
        assertEquals(Collections.singletonList("localhost:1234"),
                workerClusters.getNormalWorkerServerAddressByGroup("analytics"));
        verifyNoInteractions(listener);
        query = (type, address) -> heartbeat;
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
        assertTrue(workerView().getHeartbeatReceivedTime() >= snapshot.getHeartbeatReceivedTime());
        verify(listener).onServerUpdate(workerView());
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
        assertEquals(2, workerClusters.getServerIdentity("localhost:1234").get().getStartupTime());
        assertFalse(workerClusters.getServer("localhost:1234").isPresent());
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
        assertFalse(workerClusters.getServer("localhost:1234").isPresent());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = RegistryNodeType.class, names = {"MASTER", "WORKER"})
    void removalDuringInitialListingIsAppliedAfterSnapshot(RegistryNodeType type) throws InterruptedException {
        BaseHeartBeat heartbeat = type == RegistryNodeType.MASTER
                ? MasterHeartBeat.builder().host("localhost").port(5678).processId(10).startupTime(1).build()
                : worker(1);
        register(type, heartbeat);
        java.util.List<Server> snapshot = registry.getServerList(type);
        Thread[] removal = new Thread[1];
        when(registry.getServerList(type)).thenAnswer(invocation -> {
            removal[0] = new Thread(() -> notify(type, Event.Type.REMOVE, heartbeat));
            removal[0].start();
            return snapshot;
        });
        owner.start();
        removal[0].join(3000);
        assertFalse(removal[0].isAlive());
        assertTrue((type == RegistryNodeType.MASTER ? masterClusters : workerClusters).getServerAddresses().isEmpty());
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
        assertFalse(workerClusters.getServer("localhost:1234").isPresent());
        assertTrue(owner.getWorkerClusters().getNormalWorkerServerAddressByGroup("analytics").isEmpty());
    }

    @Test
    void masterRegistrationExistsBeforeAndAfterHeartbeatFailure() {
        MasterHeartBeat heartbeat = MasterHeartBeat.builder().host("localhost").port(5678).processId(10)
                .startupTime(1).reportTime(100).serverStatus(ServerStatus.NORMAL).build();
        register(RegistryNodeType.MASTER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        assertTrue(masterClusters.getServers().isEmpty());
        assertEquals(Collections.singletonList("localhost:5678"), masterClusters.getServerAddresses());
        poll();
        assertEquals(1, masterClusters.getNormalServers().size());
        MasterServerMetadata snapshot = masterClusters.getServer("localhost:5678").get();
        query = (type, address) -> {
            throw new IllegalStateException("unreachable");
        };
        poll();
        assertSame(snapshot, masterClusters.getServer("localhost:5678").get());
        assertEquals(1, masterClusters.getNormalServers().size());
        assertTrue(masterClusters.getServerIdentity("localhost:5678").isPresent());
    }

    @Test
    void synchronousFetchDoesNotBlockRegistryRemoval() throws InterruptedException {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        CountDownLatch requested = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        query = (type, address) -> {
            requested.countDown();
            try {
                assertTrue(release.await(3, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            return heartbeat;
        };
        Thread polling = new Thread(workerClusters::refreshHeartbeats);
        polling.start();
        try {
            assertTrue(requested.await(3, TimeUnit.SECONDS));
            notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
            assertTrue(workerClusters.getServerAddresses().isEmpty());
        } finally {
            release.countDown();
            polling.join(3000);
        }
        assertFalse(polling.isAlive());
        assertTrue(workerClusters.getServers().isEmpty());
    }

    @Test
    void nullHeartbeatDoesNotPublishRegistryRemoval() {
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        IClusters.IClustersChangeListener<WorkerServerMetadata> listener =
                mock(IClusters.IClustersChangeListener.class);
        workerClusters.registerListener(listener);
        owner.start();
        query = (type, address) -> heartbeat;
        poll();
        WorkerServerMetadata snapshot = workerView();
        clearInvocations(listener);
        query = (type, address) -> null;
        poll();
        assertSame(snapshot, workerView());
        assertTrue(workerClusters.getServerIdentity("localhost:1234").isPresent());
        verifyNoInteractions(listener);
        notify(RegistryNodeType.WORKER, Event.Type.REMOVE, heartbeat);
        verify(listener).onServerRemove(ServerIdentity.fromHeartBeat(heartbeat));
        assertTrue(workerClusters.getServerAddresses().isEmpty());
        assertTrue(workerClusters.getServers().isEmpty());
        assertTrue(workerClusters.getConfigWorkerServerAddressByGroup("analytics").isEmpty());
    }

    @Test
    void unregisterBeforeFirstHeartbeatStillPublishesRemoval() {
        MasterHeartBeat heartbeat = MasterHeartBeat.builder().host("localhost").port(5678).processId(10)
                .startupTime(1).build();
        register(RegistryNodeType.MASTER, heartbeat);
        IClusters.IClustersChangeListener<MasterServerMetadata> listener =
                mock(IClusters.IClustersChangeListener.class);
        masterClusters.registerListener(listener);
        owner.start();
        assertTrue(masterClusters.getServers().isEmpty());
        notify(RegistryNodeType.MASTER, Event.Type.REMOVE, heartbeat);
        verify(listener).onServerRemove(ServerIdentity.fromHeartBeat(heartbeat));
        verify(listener, never()).onServerUpdate(any());
        assertTrue(masterClusters.getServerAddresses().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"MASTER,exception", "MASTER,empty", "MASTER,foreign", "WORKER,exception", "WORKER,empty",
            "WORKER,foreign"})
    void everyFailedPollWarnsWithAddressReasonAndAge(RegistryNodeType type, String failure) {
        BaseHeartBeat heartbeat = type == RegistryNodeType.MASTER
                ? MasterHeartBeat.builder().host("localhost").port(5678).processId(10).startupTime(1)
                        .serverStatus(ServerStatus.NORMAL).build()
                : worker(1);
        register(type, heartbeat);
        query = (role, address) -> heartbeat;
        owner.start();
        poll();
        Class<?> clusterType = type == RegistryNodeType.MASTER ? MasterClusters.class : WorkerClusters.class;
        Logger logger = (Logger) LoggerFactory.getLogger(clusterType);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        query = (role, address) -> {
            switch (failure) {
                case "empty":
                    return null;
                case "foreign":
                    return type == RegistryNodeType.MASTER
                            ? MasterHeartBeat.builder().host("localhost").port(5678).processId(11).startupTime(2)
                                    .build()
                            : worker(2);
                default:
                    throw new IllegalStateException("unreachable");
            }
        };
        try {
            poll();
            poll();
            assertEquals(2, appender.list.stream().filter(event -> event.getLevel() == Level.WARN).count());
            String message = appender.list.stream().filter(event -> event.getLevel() == Level.WARN).findFirst().get()
                    .getFormattedMessage();
            assertTrue(message.contains(ServerIdentity.fromHeartBeat(heartbeat).getAddress()));
            assertTrue(message.contains("heartbeatAge="));
            assertTrue(message.contains("ms"));
            assertTrue(message.contains(failure.equals("exception") ? "unreachable"
                    : failure.equals("empty") ? "empty heartbeat" : "unexpected heartbeat identity"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void failedPollWarnsImmediatelyForEachNodeWithoutAnySuccessfulHeartbeat() {
        WorkerHeartBeat heartbeat = worker(1);
        WorkerHeartBeat second = worker(1);
        second.setPort(1235);
        register(RegistryNodeType.WORKER, heartbeat);
        owner.start();
        notify(RegistryNodeType.WORKER, Event.Type.ADD, second);
        Logger logger = (Logger) LoggerFactory.getLogger(WorkerClusters.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        AtomicInteger requests = new AtomicInteger();
        query = (type, address) -> {
            if (requests.getAndIncrement() > 0) {
                assertEquals(1, appender.list.stream().filter(event -> event.getLevel() == Level.WARN).count());
            }
            return null;
        };
        try {
            poll();
            assertEquals(2, appender.list.stream().filter(event -> event.getLevel() == Level.WARN).count());
            String message = appender.list.stream().filter(event -> event.getLevel() == Level.WARN)
                    .map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            assertTrue(message.contains("localhost:1234"));
            assertTrue(message.contains("localhost:1235"));
            assertTrue(message.contains("heartbeatAge=never received"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void fetchesNodesOnCallingThreadAndContinuesAfterFailure() {
        WorkerHeartBeat first = worker(1);
        WorkerHeartBeat second = worker(1);
        second.setPort(1235);
        register(RegistryNodeType.WORKER, first);
        owner.start();
        notify(RegistryNodeType.WORKER, Event.Type.ADD, second);
        Thread caller = Thread.currentThread();
        AtomicInteger requests = new AtomicInteger();
        query = (type, address) -> {
            assertSame(caller, Thread.currentThread());
            requests.incrementAndGet();
            if (address.equals("localhost:1234")) {
                throw new IllegalStateException("unreachable");
            }
            return second;
        };
        poll();
        assertEquals(2, requests.get());
        assertEquals(2, workerClusters.getServerAddresses().size());
        assertEquals(Collections.singletonList("localhost:1235"),
                workerClusters.getNormalWorkerServerAddressByGroup("analytics"));
    }

    @Test
    void closingMasterPollingDoesNotStopWorkerPolling() {
        masterClusters.start();
        workerClusters.start();
        verify(masterTimer).scheduleWithFixedDelay(any(Runnable.class), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(3000L), org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
        verify(workerTimer).scheduleWithFixedDelay(any(Runnable.class), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(3000L), org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
        WorkerHeartBeat heartbeat = worker(1);
        register(RegistryNodeType.WORKER, heartbeat);
        query = (type, address) -> heartbeat;
        owner.start();
        masterClusters.close();
        poll();
        assertEquals(ServerStatus.NORMAL, workerView().getServerStatus());
    }

    private WorkerServerMetadata workerView() {
        return owner.getWorkerClusters().getServer("localhost:1234").get();
    }
}
