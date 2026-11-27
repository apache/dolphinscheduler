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

package org.apache.dolphinscheduler.alert;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.registry.AlertRegistryClient;
import org.apache.dolphinscheduler.alert.rpc.AlertRpcServer;
import org.apache.dolphinscheduler.alert.service.AlertBootstrapService;
import org.apache.dolphinscheduler.alert.service.AlertEventFetcher;
import org.apache.dolphinscheduler.alert.service.AlertEventLoop;
import org.apache.dolphinscheduler.alert.service.AlertEventPendingQueue;
import org.apache.dolphinscheduler.alert.service.AlertHAServer;
import org.apache.dolphinscheduler.alert.service.AlertSender;
import org.apache.dolphinscheduler.alert.service.AlertSenderThreadPoolFactory;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class AlertServerTest {

    private static final String SELECTOR_PATH = RegistryNodeType.ALERT_HA_LEADER.getRegistryPath();

    private final AtomicReference<String> owner = new AtomicReference<>();
    private final AtomicReference<SubscribeListener> subscription = new AtomicReference<>();
    private final AtomicReference<Alert> pendingAlert = new AtomicReference<>();
    private final AtomicInteger failOnRead = new AtomicInteger();
    private final AlertRpcServer rpcServer = mock(AlertRpcServer.class);
    private final AlertRegistryClient registryClient = mock(AlertRegistryClient.class);
    private final AlertSender sender = mock(AlertSender.class);
    private final AlertDao alertDao = mock(AlertDao.class);
    private final AlertServer server = new AlertServer();

    private AlertHAServer haServer;
    private AlertEventFetcher fetcher;
    private AlertEventLoop eventLoop;
    private ThreadPoolExecutor senderExecutor;

    @BeforeEach
    void setUp() {
        Registry registry = mock(Registry.class);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (failOnRead.getAndUpdate(remaining -> Math.max(0, remaining - 1)) == 1) {
                throw new IllegalStateException("Temporary selector read failure");
            }
            return owner.get();
        });
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenReturn(true);
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(SELECTOR_PATH), anyString(), eq(true));
        doAnswer(invocation -> {
            subscription.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), any(SubscribeListener.class));

        AlertConfig config = new AlertConfig();
        config.setAlertServerAddress("localhost:50052");
        config.setSenderParallelism(1);
        haServer = new AlertHAServer(registry, config);
        AlertEventPendingQueue queue = new AlertEventPendingQueue(config);
        fetcher = new AlertEventFetcher(haServer, alertDao, queue);
        AlertSenderThreadPoolFactory threadPoolFactory = new AlertSenderThreadPoolFactory(config);
        senderExecutor = threadPoolFactory.getThreadPool();
        eventLoop = new AlertEventLoop(queue, threadPoolFactory, sender);
        AlertBootstrapService bootstrap = new AlertBootstrapService(rpcServer, registryClient, haServer, fetcher,
                eventLoop);
        when(alertDao.listPendingAlerts(anyInt())).thenAnswer(invocation -> {
            Alert alert = pendingAlert.get();
            return alert != null && alert.getId() > (int) invocation.getArgument(0)
                    ? Collections.singletonList(alert)
                    : Collections.emptyList();
        });
        ReflectionTestUtils.setField(server, "alertPluginManager", mock(AlertPluginManager.class));
        ReflectionTestUtils.setField(server, "alertRpcServer", rpcServer);
        ReflectionTestUtils.setField(server, "alertRegistryClient", registryClient);
        ReflectionTestUtils.setField(server, "alertHAServer", haServer);
        ReflectionTestUtils.setField(server, "alertBootstrapService", bootstrap);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        server.close();
        haServer.close();
        fetcher.shutdown();
        eventLoop.shutdown();
        fetcher.interrupt();
        eventLoop.interrupt();
        fetcher.join(2_000);
        eventLoop.join(2_000);
        senderExecutor.shutdownNow();
        ServerLifeCycleManager.toRunning();
        assertFalse(fetcher.isAlive());
        assertFalse(eventLoop.isAlive());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void transientSelectorReadFailureDoesNotStopAlertServer(int readNumber) {
        server.run();
        assertTrue(haServer.isActive());
        Alert first = new Alert();
        first.setId(1);
        pendingAlert.set(first);
        verify(sender, timeout(8_000)).sendEvent(first);
        String selectorOwner = owner.get();

        // JDBC Registry can deliver the selector's own ADD notification after activation.
        failOnRead.set(readNumber);
        subscription.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, selectorOwner, Event.Type.ADD));

        await().atMost(Duration.ofSeconds(2)).until(() -> !haServer.isActive());
        assertEquals(selectorOwner, owner.get());
        Alert second = new Alert();
        second.setId(2);
        pendingAlert.set(second);
        await().atMost(Duration.ofSeconds(8)).until(haServer::isActive);
        verify(sender, timeout(8_000)).sendEvent(second);
        assertFalse(ServerLifeCycleManager.isStopped());
        assertTrue(fetcher.isAlive());
        assertTrue(eventLoop.isAlive());
        verify(rpcServer, never()).close();
        verify(registryClient, never()).close();
        verify(sender).sendEvent(first);
    }

    @Test
    void standbyStartsFetchingOnlyAfterTakingOwnership() {
        owner.set("peer");
        Alert alert = new Alert();
        alert.setId(1);
        pendingAlert.set(alert);
        server.run();

        assertFalse(haServer.isActive());
        await().atMost(Duration.ofSeconds(2)).until(() -> fetcher.getState() == Thread.State.TIMED_WAITING);
        verify(alertDao, never()).listPendingAlerts(anyInt());

        owner.set(null);
        subscription.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "peer", Event.Type.REMOVE));

        await().atMost(Duration.ofSeconds(2)).until(haServer::isActive);
        verify(sender, timeout(8_000)).sendEvent(alert);
        assertFalse(ServerLifeCycleManager.isStopped());
    }
}
