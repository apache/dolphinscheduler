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

package org.apache.dolphinscheduler.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MonitorHeartbeatQueryTest {

    private final RegistryClient registry = mock(RegistryClient.class);
    private final MonitorServiceImpl monitor = spy(new MonitorServiceImpl());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(monitor, "registryClient", registry);
    }

    @AfterEach
    void tearDown() {
        monitor.close();
    }

    @Test
    void eachRequestDiscoversMembersAndQueriesCurrentHeartbeat() {
        AtomicInteger report = new AtomicInteger(100);
        when(registry.getServerList(RegistryNodeType.WORKER))
                .thenAnswer(invocation -> Collections.singletonList(server(1234)));
        doAnswer(invocation -> heartbeat(1234, report.getAndIncrement()))
                .when(monitor).fetchHeartbeat(any(), anyString());
        assertEquals(100, monitor.listServer(RegistryNodeType.WORKER).get(0).getLastHeartbeatTime().getTime());
        assertEquals(101, monitor.listServer(RegistryNodeType.WORKER).get(0).getLastHeartbeatTime().getTime());
        verify(registry, times(2)).getServerList(RegistryNodeType.WORKER);
        verify(monitor, times(2)).fetchHeartbeat(RegistryNodeType.WORKER, "localhost:1234");
    }

    @Test
    void failedNodeDoesNotHideHealthyNodesOrLeaveRegistrationDataAsHeartbeat() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Arrays.asList(server(1234), server(1235)));
        doAnswer(invocation -> {
            if ("localhost:1235".equals(invocation.getArgument(1))) {
                throw new IllegalStateException("unreachable");
            }
            return heartbeat(1234, 100);
        }).when(monitor).fetchHeartbeat(any(), anyString());
        List<Server> servers = monitor.listServer(RegistryNodeType.WORKER);
        assertFalse(servers.get(0).isMetadataStale());
        assertEquals(0.25,
                JSONUtils.parseObject(servers.get(0).getHeartBeatInfo(), WorkerHeartBeat.class).getCpuUsage());
        assertTrue(servers.get(1).isMetadataStale());
        assertNull(servers.get(1).getHeartBeatInfo());
        assertNull(servers.get(1).getLastHeartbeatTime());
        assertEquals(1235, servers.get(1).getPort());
    }

    @Test
    void rejectsHeartbeatFromAnotherProcessInstance() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Collections.singletonList(server(1234)));
        WorkerHeartBeat anotherProcess = heartbeat(1234, 100);
        anotherProcess.setStartupTime(2);
        doAnswer(invocation -> anotherProcess).when(monitor).fetchHeartbeat(any(), anyString());
        Server server = monitor.listServer(RegistryNodeType.WORKER).get(0);
        assertTrue(server.isMetadataStale());
        assertNull(server.getHeartBeatInfo());
    }

    @Test
    void queriesNodesConcurrentlyWithinOneRequest() {
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(Arrays.asList(server(1234), server(1235)));
        CountDownLatch started = new CountDownLatch(2);
        doAnswer(invocation -> {
            started.countDown();
            assertTrue(started.await(3, TimeUnit.SECONDS));
            return heartbeat("localhost:1234".equals(invocation.getArgument(1)) ? 1234 : 1235, 100);
        }).when(monitor).fetchHeartbeat(any(), anyString());
        assertTrue(monitor.listServer(RegistryNodeType.WORKER).stream().noneMatch(Server::isMetadataStale));
    }

    @Test
    void queriesMoreNodesThanTheExecutorQueueCanHold() {
        List<Server> registrations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            registrations.add(server(1234 + i));
        }
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(registrations);
        doAnswer(invocation -> heartbeat(Integer.parseInt(((String) invocation.getArgument(1)).split(":")[1]), 100))
                .when(monitor).fetchHeartbeat(any(), anyString());
        List<Server> result = monitor.listServer(RegistryNodeType.WORKER);
        assertEquals(100, result.size());
        assertTrue(result.stream().noneMatch(Server::isMetadataStale));
        verify(monitor, times(100)).fetchHeartbeat(any(), anyString());
    }

    @Test
    void boundsAdmissionAndFailsWorkerGroupsWithoutRunningQueriesOnTheCaller() throws Exception {
        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) ReflectionTestUtils.getField(monitor, "heartbeatQueryExecutor");
        CountDownLatch started = new CountDownLatch(8);
        CountDownLatch release = new CountDownLatch(1);
        try {
            for (int i = 0; i < 8; i++) {
                executor.execute(() -> {
                    started.countDown();
                    try {
                        release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertTrue(started.await(3, TimeUnit.SECONDS));
            for (int i = 0; i < 64; i++) {
                executor.execute(() -> {
                });
            }
            when(registry.getServerList(RegistryNodeType.WORKER))
                    .thenAnswer(invocation -> Collections.singletonList(server(1234)));
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                for (int i = 0; i < 5; i++) {
                    Server unavailable = monitor.listServer(RegistryNodeType.WORKER).get(0);
                    assertTrue(unavailable.isMetadataStale());
                    assertNull(unavailable.getHeartBeatInfo());
                    assertNull(unavailable.getLastHeartbeatTime());
                    assertEquals(64, executor.getQueue().size());
                }
                WorkerGroupServiceImpl groups = new WorkerGroupServiceImpl();
                WorkerGroupDao workerGroupDao = mock(WorkerGroupDao.class);
                when(workerGroupDao.queryAllWorkerGroup()).thenReturn(Collections.emptyList());
                ReflectionTestUtils.setField(groups, "monitorService", monitor);
                ReflectionTestUtils.setField(groups, "workerGroupDao", workerGroupDao);
                User admin = new User();
                admin.setUserType(UserType.ADMIN_USER);
                ServiceException options = assertThrows(ServiceException.class, () -> groups.queryAllGroup(admin));
                ServiceException paging = assertThrows(ServiceException.class,
                        () -> groups.queryAllGroupPaging(admin, 1, 10, null));
                assertEquals(Status.QUERY_WORKER_GROUP_FAIL.getCode(), options.getCode());
                assertEquals(Status.QUERY_WORKER_GROUP_FAIL.getCode(), paging.getCode());
                assertTrue(options.getMessage().contains("localhost:1234"));
                assertTrue(paging.getMessage().contains("localhost:1234"));
            });
            verify(monitor, never()).fetchHeartbeat(any(), anyString());
        } finally {
            release.countDown();
        }
    }

    @Test
    void sharesOneDeadlineRemovesQueuedWorkAndDiscardsLateResults() throws Exception {
        List<Server> registrations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            registrations.add(server(1234 + i));
        }
        when(registry.getServerList(RegistryNodeType.WORKER)).thenAnswer(invocation -> registrations.stream()
                .map(registration -> server(registration.getPort())).collect(Collectors.toList()));
        CountDownLatch started = new CountDownLatch(8);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(8);
        doAnswer(invocation -> {
            int port = Integer.parseInt(((String) invocation.getArgument(1)).split(":")[1]);
            if (port == 1235) {
                return heartbeat(port, 100);
            }
            started.countDown();
            try {
                assertTrue(release.await(15, TimeUnit.SECONDS));
                return heartbeat(port, 100);
            } finally {
                finished.countDown();
            }
        }).when(monitor).fetchHeartbeat(any(), anyString());
        FutureTask<List<Server>> request = new FutureTask<>(() -> monitor.listServer(RegistryNodeType.WORKER));
        FutureTask<List<Server>> queuedRequest = new FutureTask<>(() -> monitor.listServer(RegistryNodeType.WORKER));
        Thread caller = new Thread(request);
        Thread queuedCaller = new Thread(queuedRequest);
        caller.start();
        try {
            assertTrue(started.await(3, TimeUnit.SECONDS));
            queuedCaller.start();
            List<Server> result = request.get(7, TimeUnit.SECONDS);
            List<Server> queuedResult = queuedRequest.get(2, TimeUnit.SECONDS);
            assertEquals(100, result.size());
            assertFalse(result.get(1).isMetadataStale());
            assertEquals(99, result.stream().filter(Server::isMetadataStale).count());
            assertNull(result.get(0).getHeartBeatInfo());
            assertNull(result.get(0).getLastHeartbeatTime());
            assertTrue(queuedResult.stream().allMatch(Server::isMetadataStale));
            assertTrue(queuedResult.stream().allMatch(server -> server.getHeartBeatInfo() == null));
            ThreadPoolExecutor executor =
                    (ThreadPoolExecutor) ReflectionTestUtils.getField(monitor, "heartbeatQueryExecutor");
            assertTrue(executor.getQueue().isEmpty());
            release.countDown();
            assertTrue(finished.await(3, TimeUnit.SECONDS));
            monitor.close();
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
            assertTrue(result.get(0).isMetadataStale());
            assertNull(result.get(0).getHeartBeatInfo());
            assertNull(result.get(0).getLastHeartbeatTime());
            verify(monitor, times(9)).fetchHeartbeat(any(), anyString());
        } finally {
            release.countDown();
            caller.interrupt();
            caller.join(3000);
            queuedCaller.interrupt();
            queuedCaller.join(3000);
        }
    }

    @Test
    void interruptionRemovesQueuedWorkAndPreservesTheInterruptFlag() throws Exception {
        List<Server> registrations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            registrations.add(server(1234 + i));
        }
        when(registry.getServerList(RegistryNodeType.WORKER)).thenReturn(registrations);
        CountDownLatch started = new CountDownLatch(8);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            started.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return heartbeat(1234, 100);
        }).when(monitor).fetchHeartbeat(any(), anyString());
        AtomicBoolean interrupted = new AtomicBoolean();
        FutureTask<List<Server>> request = new FutureTask<>(() -> {
            List<Server> result = monitor.listServer(RegistryNodeType.WORKER);
            interrupted.set(Thread.currentThread().isInterrupted());
            return result;
        });
        Thread caller = new Thread(request);
        caller.start();
        try {
            assertTrue(started.await(3, TimeUnit.SECONDS));
            caller.interrupt();
            assertTrue(request.get(2, TimeUnit.SECONDS).stream().allMatch(Server::isMetadataStale));
            assertTrue(interrupted.get());
            ThreadPoolExecutor executor =
                    (ThreadPoolExecutor) ReflectionTestUtils.getField(monitor, "heartbeatQueryExecutor");
            assertTrue(executor.getQueue().isEmpty());
        } finally {
            release.countDown();
            caller.join(3000);
        }
    }

    private Server server(int port) {
        Server server = new Server();
        server.setId(10);
        server.setHost("localhost");
        server.setPort(port);
        server.setCreateTime(new Date(1));
        server.setHeartBeatInfo("{\"host\":\"localhost\"}");
        return server;
    }

    private WorkerHeartBeat heartbeat(int port, int report) {
        return WorkerHeartBeat.builder().host("localhost").port(port).processId(10).startupTime(1).reportTime(report)
                .cpuUsage(0.25).build();
    }
}
