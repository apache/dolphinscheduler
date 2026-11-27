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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
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
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.util.concurrent.Uninterruptibles;

class AlertHandoffTest {

    private static final String SELECTOR_PATH = RegistryNodeType.ALERT_HA_LEADER.getRegistryPath();

    private final AtomicReference<String> owner = new AtomicReference<>();
    private final ConcurrentSkipListMap<Integer, Alert> pending = new ConcurrentSkipListMap<>();
    private final List<Node> nodes = new ArrayList<>();
    private final List<CountDownLatch> latches = new ArrayList<>();

    @AfterEach
    void tearDown() throws InterruptedException {
        latches.forEach(CountDownLatch::countDown);
        for (Node node : nodes) {
            node.ha.close();
            node.bootstrap.close();
            node.fetcher.interrupt();
            node.loop.interrupt();
            node.fetcher.join(2_000);
            node.loop.join(2_000);
            node.executor.shutdownNow();
            assertFalse(node.fetcher.isAlive());
            assertFalse(node.loop.isAlive());
        }
    }

    @Test
    void demotedLeaderDoesNotSendQueuedAlertAlreadySentByNewLeader() throws Exception {
        Alert first = addAlert(1);
        Alert queued = addAlert(2);
        Node oldLeader = newNode();
        CountDownLatch sending = latch();
        CountDownLatch finishSend = latch();
        oldLeader.onSend = event -> {
            if (event.getId() == first.getId()) {
                sending.countDown();
                Uninterruptibles.awaitUninterruptibly(finishSend);
            }
        };
        oldLeader.start();
        assertTrue(sending.await(8, TimeUnit.SECONDS));
        await().atMost(Duration.ofSeconds(2)).until(() -> oldLeader.queue.size() == 1);
        Node newLeader = newNode();
        newLeader.start();

        handoffTo(newLeader);
        verify(newLeader.sender, timeout(8_000)).sendEvent(queued);
        await().atMost(Duration.ofSeconds(2)).until(pending::isEmpty);
        finishSend.countDown();

        verify(oldLeader.sender, after(2_000).never()).sendEvent(queued);
        handoffTo(oldLeader);
        Alert next = addAlert(3);
        verify(oldLeader.sender, timeout(8_000)).sendEvent(next);
        verify(oldLeader.sender, never()).sendEvent(queued);
    }

    @Test
    void reactivationReloadsUnsentRowsBelowPreviousOffset() throws Exception {
        Alert first = addAlert(1);
        Alert queued = addAlert(2);
        Node node = newNode();
        CountDownLatch sending = latch();
        CountDownLatch finishSend = latch();
        node.onSend = event -> {
            if (event.getId() == first.getId()) {
                sending.countDown();
                Uninterruptibles.awaitUninterruptibly(finishSend);
            }
        };
        node.start();
        assertTrue(sending.await(8, TimeUnit.SECONDS));
        await().atMost(Duration.ofSeconds(2)).until(() -> node.queue.size() == 1);

        owner.set("peer");
        node.notifyChange();
        await().atMost(Duration.ofSeconds(2)).until(() -> !node.ha.isActive());
        finishSend.countDown();
        verify(node.sender, after(2_000).never()).sendEvent(queued);
        handoffTo(node);

        verify(node.sender, timeout(8_000)).sendEvent(queued);
        await().atMost(Duration.ofSeconds(2)).until(pending::isEmpty);
    }

    @Test
    void queryFromPreviousActivationCannotEnqueueAfterReactivation() throws Exception {
        Alert alert = addAlert(1);
        Node oldLeader = newNode();
        CountDownLatch querying = latch();
        CountDownLatch finishQuery = latch();
        AtomicBoolean firstQuery = new AtomicBoolean(true);
        oldLeader.fetch = offset -> {
            List<Alert> result = new ArrayList<>(pending.tailMap(offset, false).values());
            if (firstQuery.compareAndSet(true, false)) {
                querying.countDown();
                Uninterruptibles.awaitUninterruptibly(finishQuery);
            }
            return result;
        };
        oldLeader.start();
        assertTrue(querying.await(8, TimeUnit.SECONDS));
        Node newLeader = newNode();
        newLeader.start();
        handoffTo(newLeader);
        verify(newLeader.sender, timeout(8_000)).sendEvent(alert);
        await().atMost(Duration.ofSeconds(2)).until(pending::isEmpty);

        handoffTo(oldLeader);
        finishQuery.countDown();
        Alert next = addAlert(2);
        verify(oldLeader.sender, timeout(8_000)).sendEvent(next);
        verify(oldLeader.sender, never()).sendEvent(alert);
    }

    @Test
    void taskSubmittedBeforeDemotionCannotStartSendingAfterReactivation() throws Exception {
        Alert alert = addAlert(1);
        CountDownLatch submitted = latch();
        CountDownLatch execute = latch();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()) {

            @Override
            protected void beforeExecute(Thread thread, Runnable task) {
                submitted.countDown();
                Uninterruptibles.awaitUninterruptibly(execute);
            }
        };
        Node oldLeader = new Node(executor);
        nodes.add(oldLeader);
        oldLeader.start();
        assertTrue(submitted.await(8, TimeUnit.SECONDS));
        Node newLeader = newNode();
        newLeader.start();
        handoffTo(newLeader);
        verify(newLeader.sender, timeout(8_000)).sendEvent(alert);
        await().atMost(Duration.ofSeconds(2)).until(pending::isEmpty);

        handoffTo(oldLeader);
        execute.countDown();
        Alert next = addAlert(2);
        verify(oldLeader.sender, timeout(8_000)).sendEvent(next);
        verify(oldLeader.sender, never()).sendEvent(alert);
    }

    @Test
    void reactivationWaitsForPreviouslyStartedLocalSendBeforeReloading() throws Exception {
        Alert alert = addAlert(1);
        Node node = newNode();
        CountDownLatch sending = latch();
        CountDownLatch finishSend = latch();
        node.onSend = event -> {
            if (event.getId() == alert.getId()) {
                sending.countDown();
                Uninterruptibles.awaitUninterruptibly(finishSend);
            }
        };
        node.start();
        assertTrue(sending.await(8, TimeUnit.SECONDS));
        owner.set("peer");
        node.notifyChange();
        await().atMost(Duration.ofSeconds(2)).until(() -> !node.ha.isActive());
        handoffTo(node);

        verify(node.sender, after(6_000)).sendEvent(alert);
        assertTrue(pending.containsKey(alert.getId()));
        finishSend.countDown();
        Alert next = addAlert(2);
        verify(node.sender, timeout(8_000)).sendEvent(next);
        verify(node.sender).sendEvent(alert);
    }

    private Alert addAlert(int id) {
        Alert alert = new Alert();
        alert.setId(id);
        pending.put(id, alert);
        return alert;
    }

    private CountDownLatch latch() {
        CountDownLatch latch = new CountDownLatch(1);
        latches.add(latch);
        return latch;
    }

    private Node newNode() {
        Node node = new Node(new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()));
        nodes.add(node);
        return node;
    }

    private void handoffTo(Node leader) {
        owner.set((String) ReflectionTestUtils.getField(leader.ha, "serverIdentify"));
        nodes.forEach(Node::notifyChange);
        await().atMost(Duration.ofSeconds(2)).until(() -> nodes.stream()
                .allMatch(node -> node.ha.isActive() == (node == leader)));
    }

    private class Node {

        private final AlertServer server = new AlertServer();
        private final AtomicReference<SubscribeListener> subscription = new AtomicReference<>();
        private final AlertSender sender = mock(AlertSender.class);
        private final AlertHAServer ha;
        private final AlertEventPendingQueue queue;
        private final AlertEventFetcher fetcher;
        private final AlertEventLoop loop;
        private final AlertBootstrapService bootstrap;
        private final ThreadPoolExecutor executor;
        private IntFunction<List<Alert>> fetch = offset -> new ArrayList<>(pending.tailMap(offset, false).values());
        private Consumer<Alert> onSend = event -> {
        };

        private Node(ThreadPoolExecutor executor) {
            this.executor = executor;
            Registry registry = mock(Registry.class);
            when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
            when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> owner.get());
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
            config.setAlertServerAddress("localhost:" + (50052 + nodes.size()));
            config.setSenderParallelism(1);
            ha = new AlertHAServer(registry, config);
            queue = new AlertEventPendingQueue(config);
            AlertDao dao = mock(AlertDao.class);
            when(dao.listPendingAlerts(anyInt())).thenAnswer(invocation -> fetch.apply(invocation.getArgument(0)));
            doAnswer(invocation -> {
                Alert event = invocation.getArgument(0);
                onSend.accept(event);
                pending.remove(event.getId());
                return null;
            }).when(sender).sendEvent(any(Alert.class));
            fetcher = new AlertEventFetcher(ha, dao, queue);
            AlertSenderThreadPoolFactory factory = mock(AlertSenderThreadPoolFactory.class);
            when(factory.getThreadPool()).thenReturn(executor);
            loop = new AlertEventLoop(queue, factory, sender, ha);
            AlertRpcServer rpc = mock(AlertRpcServer.class);
            AlertRegistryClient client = mock(AlertRegistryClient.class);
            bootstrap = new AlertBootstrapService(rpc, client, ha, fetcher, loop, queue);
            ReflectionTestUtils.setField(server, "alertPluginManager", mock(AlertPluginManager.class));
            ReflectionTestUtils.setField(server, "alertRpcServer", rpc);
            ReflectionTestUtils.setField(server, "alertRegistryClient", client);
            ReflectionTestUtils.setField(server, "alertHAServer", ha);
            ReflectionTestUtils.setField(server, "alertBootstrapService", bootstrap);
        }

        private void start() {
            server.run();
        }

        private void notifyChange() {
            subscription.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, owner.get(), Event.Type.UPDATE));
        }
    }
}
