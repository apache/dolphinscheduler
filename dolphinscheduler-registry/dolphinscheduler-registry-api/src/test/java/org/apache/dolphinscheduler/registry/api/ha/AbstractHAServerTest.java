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

package org.apache.dolphinscheduler.registry.api.ha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(15)
class AbstractHAServerTest {

    private static final String SELECTOR_PATH = "/coordinator";
    private static final String ADDRESS = "master-0:5678";

    private final AtomicReference<String> owner = new AtomicReference<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AtomicReference<SubscribeListener> subscription = new AtomicReference<>();
    private Registry registry;
    private AbstractHAServer server;

    @BeforeEach
    void setUp() {
        registry = mock(Registry.class);
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenReturn(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> owner.get());
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(SELECTOR_PATH), anyString(), eq(true));
        doAnswer(invocation -> {
            subscription.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), any(SubscribeListener.class));
        server = new AbstractHAServer(registry, SELECTOR_PATH, ADDRESS) {
        };
    }

    @Test
    void testInitialElectionWritesAddressAndTimestamp() {
        server.start();

        assertTrue(server.isActive());
        assertTrue(owner.get().startsWith(ADDRESS + "#"));
        assertTrue(Long.parseLong(owner.get().substring(ADDRESS.length() + 1)) > 0);
        verify(registry).put(SELECTOR_PATH, owner.get(), true);
    }

    @Test
    void testDoesNotAdoptLegacySelectorAtSameAddress() {
        // The predecessor's selector can remain until its Registry lease or session expires.
        owner.set(ADDRESS);

        server.start();

        assertFalse(server.isActive());
        assertEquals(ADDRESS, owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotAdoptPredecessorWithEarlierTimestamp() {
        String predecessor = ADDRESS + "#0";
        owner.set(predecessor);

        server.start();

        assertFalse(server.isActive());
        assertEquals(predecessor, owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testRecheckRecognizesCurrentProcess() {
        server.start();
        String identity = owner.get();

        assertTrue(server.participateElection());

        assertEquals(identity, owner.get());
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotReplaceExistingPeerSelector() {
        owner.set("master-1:5678#0");

        server.start();

        assertFalse(server.isActive());
        assertEquals("master-1:5678#0", owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }
    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
        executor.shutdownNow();
    }

    @Test
    void testPeerOwnershipDemotesActiveServerEvenWithEmptyRemoveData() {
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        server.addServerStatusChangeListener(listener);
        server.start();
        owner.set("peer");

        notifyChange(Event.Type.REMOVE, "");

        verify(listener, timeout(2000)).changeToStandBy();
        assertFalse(server.isActive());
    }

    @Test
    void testDelayedOwnRemoveDoesNotDemoteCurrentOwner() {
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        server.addServerStatusChangeListener(listener);
        server.start();

        clearInvocations(registry);
        notifyChange(Event.Type.REMOVE, owner.get());

        verify(registry, timeout(2000).times(2)).get(SELECTOR_PATH);
        verify(listener, never()).changeToStandBy();
        assertTrue(server.isActive());
    }

    @Test
    void testStartupRechecksOwnershipAfterElection() throws Exception {
        CountDownLatch written = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        doAnswer(invocation -> {
            written.countDown();
            awaitUninterruptibly(resume);
            return true;
        }).when(registry).releaseLock(SELECTOR_PATH + "-lock");
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        server.addServerStatusChangeListener(listener);
        Future<?> startup = executor.submit(server::start);
        try {
            assertTrue(written.await(2, TimeUnit.SECONDS));
            String removedOwner = owner.getAndSet("peer");
            notifyChange(Event.Type.REMOVE, removedOwner);
        } finally {
            resume.countDown();
        }
        startup.get(2, TimeUnit.SECONDS);

        assertFalse(server.isActive());
        verify(listener, never()).changeToActive();
    }

    @Test
    void testStartupAndWatchCallbacksUseOneConsumer() throws Exception {
        CountDownLatch activating = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        Set<Thread> callbackThreads = Collections.synchronizedSet(new HashSet<>());
        server.addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
                callbackThreads.add(Thread.currentThread());
                activating.countDown();
                awaitUninterruptibly(resume);
            }

            @Override
            public void changeToStandBy() {
                callbackThreads.add(Thread.currentThread());
                stopped.countDown();
            }
        });
        Future<?> startup = executor.submit(server::start);
        try {
            assertTrue(activating.await(2, TimeUnit.SECONDS));
            assertFalse(server.isActive());
            owner.set("peer");
            executor.submit(() -> notifyChange(Event.Type.REMOVE, "")).get(2, TimeUnit.SECONDS);
        } finally {
            resume.countDown();
        }
        startup.get(2, TimeUnit.SECONDS);
        assertTrue(stopped.await(2, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        assertEquals(1, callbackThreads.size());
    }

    @Test
    void testCloseDuringElectionPreventsActivation() throws Exception {
        CountDownLatch electing = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenAnswer(invocation -> {
            electing.countDown();
            try {
                resume.await();
            } catch (InterruptedException ex) {
                interrupted.countDown();
                awaitUninterruptibly(resume);
            }
            return true;
        });
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        server.addServerStatusChangeListener(listener);
        Future<?> startup = executor.submit(server::start);
        Future<?> closing = null;
        try {
            assertTrue(electing.await(2, TimeUnit.SECONDS));
            closing = executor.submit(server::close);
            assertTrue(interrupted.await(2, TimeUnit.SECONDS));
        } finally {
            resume.countDown();
        }
        closing.get(2, TimeUnit.SECONDS);
        assertThrows(ExecutionException.class, () -> startup.get(2, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        verify(listener, never()).changeToActive();
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testListenerCanCloseItsOwnServer() throws Exception {
        CountDownLatch closed = new CountDownLatch(1);
        server.addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
            }

            @Override
            public void changeToStandBy() {
                server.close();
                closed.countDown();
            }
        });
        server.start();
        owner.set("peer");

        notifyChange(Event.Type.REMOVE, "");

        assertTrue(closed.await(2, TimeUnit.SECONDS));
        server.close();
        assertFalse(server.isActive());
    }

    @Test
    void testTransientRegistryFailureRetriesWithoutAnotherNotification() throws Exception {
        when(registry.acquireLock(SELECTOR_PATH + "-lock"))
                .thenThrow(new IllegalStateException("temporary failure")).thenReturn(true);
        Future<?> startup = executor.submit(server::start);

        startup.get(8, TimeUnit.SECONDS);

        assertTrue(server.isActive());
        verify(registry, times(2)).acquireLock(SELECTOR_PATH + "-lock");
    }

    @Test
    void testPartialActivationIsStoppedBeforeRetry() throws Exception {
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger stops = new AtomicInteger();
        CountDownLatch rolledBack = new CountDownLatch(1);
        server.addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
                if (starts.incrementAndGet() == 1) {
                    throw new IllegalStateException("start failure");
                }
                assertEquals(1, stops.get());
            }

            @Override
            public void changeToStandBy() {
                stops.incrementAndGet();
                rolledBack.countDown();
            }
        });
        Future<?> startup = executor.submit(server::start);
        assertTrue(rolledBack.await(2, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        notifyChange(Event.Type.UPDATE, "");
        startup.get(2, TimeUnit.SECONDS);
        assertTrue(server.isActive());
    }

    @Test
    void testNotificationAfterCloseCannotReactivateServer() {
        server.start();
        server.close();
        owner.set(null);

        notifyChange(Event.Type.REMOVE, "");

        assertFalse(server.isActive());
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testCloseReportsListenerShutdownFailureAndStillNotifiesOtherListeners() {
        AbstractServerStatusChangeListener failing = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        AbstractServerStatusChangeListener other = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        doThrow(new IllegalStateException("stop failed")).when(failing).changeToStandBy();
        server.addServerStatusChangeListener(failing);
        server.addServerStatusChangeListener(other);
        server.start();
        try {
            assertThrows(IllegalStateException.class, server::close);
            assertFalse(server.isActive());
            verify(other).changeToStandBy();
        } finally {
            server = null;
        }
    }

    @Test
    void testOwnershipLostDuringActivationIsNotPublishedAsActive() {
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        doAnswer(invocation -> {
            owner.set("peer");
            return null;
        }).when(listener).changeToActive();
        server.addServerStatusChangeListener(listener);

        server.start();

        assertFalse(server.isActive());
        verify(listener).changeToStandBy();
    }

    @Test
    void testLockContentionDoesNotDemoteTheCurrentOwner() {
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        server.addServerStatusChangeListener(listener);
        server.start();
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenReturn(false);
        clearInvocations(registry);

        notifyChange(Event.Type.UPDATE, "");

        verify(registry, timeout(2000).atLeastOnce()).get(SELECTOR_PATH);
        assertTrue(server.isActive());
        verify(listener, never()).changeToStandBy();
        verify(registry, never()).releaseLock(anyString());
        verify(registry, never()).acquireLock(anyString());
    }

    @Test
    void testLostOwnershipStopsWorkersBeforeWaitingForElectionLock() throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        CountDownLatch electing = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        AbstractServerStatusChangeListener listener = mock(AbstractServerStatusChangeListener.class,
                org.mockito.Mockito.CALLS_REAL_METHODS);
        doAnswer(invocation -> {
            stopped.countDown();
            return null;
        }).when(listener).changeToStandBy();
        server.addServerStatusChangeListener(listener);
        server.start();
        owner.set(null);
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenAnswer(invocation -> {
            electing.countDown();
            awaitUninterruptibly(resume);
            return false;
        });
        try {
            notifyChange(Event.Type.REMOVE, "");
            assertTrue(stopped.await(2, TimeUnit.SECONDS));
            assertTrue(electing.await(2, TimeUnit.SECONDS));
            assertFalse(server.isActive());
        } finally {
            resume.countDown();
        }
    }

    private static void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                    return;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void notifyChange(Event.Type type, String data) {
        subscription.get().notify(Event.builder().watchedPath(SELECTOR_PATH).eventPath(SELECTOR_PATH)
                .eventData(data).type(type).build());
    }

}
