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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AbstractHAServerTest {

    private static final String SELECTOR_PATH = "/coordinator";
    private static final String ELECTION_LOCK = SELECTOR_PATH + "-lock";
    private static final String ADDRESS = "master-0:5678";

    private Registry registry;
    private AtomicReference<String> owner;
    private AtomicReference<SubscribeListener> subscriber;
    private AbstractServerStatusChangeListener statusListener;
    private AbstractHAServer server;

    @BeforeEach
    void setUp() {
        registry = mock(Registry.class);
        owner = new AtomicReference<>();
        subscriber = new AtomicReference<>();
        statusListener = mock(AbstractServerStatusChangeListener.class, CALLS_REAL_METHODS);
        server = newServer();
        server.addServerStatusChangeListener(statusListener);
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> owner.get());
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(SELECTOR_PATH), anyString(), eq(true));
        doAnswer(invocation -> {
            subscriber.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testInitialLeaderAndFollower() {
        server.start();
        assertTrue(server.isActive());
        verify(statusListener).changeToActive();
        assertEquals(SubscribeListener.SubscribeScope.PATH_ONLY, subscriber.get().getSubscribeScope());

        AbstractHAServer follower = newServer("master-1:5678");
        AbstractServerStatusChangeListener followerListener =
                mock(AbstractServerStatusChangeListener.class, CALLS_REAL_METHODS);
        follower.addServerStatusChangeListener(followerListener);
        follower.start();
        assertFalse(follower.isActive());
        verify(followerListener, never()).changeToActive();
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotAdoptPreviousProcessWithSameAddress() {
        // A legacy selector may survive its process until its lease/session expires.
        owner.set(ADDRESS);
        server.start();
        assertFalse(server.isActive());
        verify(statusListener, never()).changeToActive();
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));

        owner.set(null);
        remove(ADDRESS);
        assertTrue(server.isActive());
        assertNotEquals(ADDRESS, owner.get());
        verify(statusListener).changeToActive();
    }

    @Test
    void testSameAddressInstancesHaveDifferentOwnership() {
        server.start();
        String previousOwner = owner.get();
        AbstractHAServer replacement = newServer();
        AbstractServerStatusChangeListener replacementListener =
                mock(AbstractServerStatusChangeListener.class, CALLS_REAL_METHODS);
        replacement.addServerStatusChangeListener(replacementListener);
        replacement.start();
        assertFalse(replacement.isActive());
        verify(replacementListener, never()).changeToActive();

        owner.set(null);
        remove(previousOwner);
        assertTrue(replacement.isActive());
        assertNotEquals(previousOwner, owner.get());
        verify(replacementListener).changeToActive();
    }

    @Test
    void testEmptyRemoveDemotesFormerLeaderWhenPeerOwnsSelector() {
        server.start();
        owner.set("master-1:5678#peer-instance");
        remove("");
        assertFalse(server.isActive());
        verify(statusListener).changeToActive();
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testDelayedRemoveDoesNotRestartCurrentOwner() {
        server.start();
        // The old notification arrives after this instance has already acquired the current key.
        remove(owner.get());
        remove("");
        assertTrue(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testOwnRemovalCanReelectWithoutAnotherPeer() {
        server.start();
        String previousOwner = owner.get();
        owner.set(null);
        remove(previousOwner);
        assertTrue(server.isActive());
        verify(registry, times(2)).put(eq(SELECTOR_PATH), anyString(), eq(true));
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testAddAndUpdateDoNotTriggerElection() {
        owner.set("master-1:5678#peer-instance");
        server.start();
        owner.set(null);
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "", Event.Type.ADD));
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "", Event.Type.UPDATE));
        assertFalse(server.isActive());
        verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testUnacquiredLockIsNotReleased() {
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false);
        server.start();
        assertFalse(server.isActive());
        verify(registry, never()).releaseLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testActiveServerDemotesWhenLockCannotBeAcquired() {
        server.start();
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false);
        remove("");
        assertFalse(server.isActive());
        verify(statusListener).changeToStandBy();
        verify(registry, times(1)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testElectionErrorDemotesBeforeRetryAndReleasesAcquiredLock() {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new IllegalStateException("registry unavailable"));
        try (MockedStatic<ThreadUtils> threadUtils = mockStatic(ThreadUtils.class)) {
            // Verify safety at each retry boundary, without spending real time on retry sleeps.
            threadUtils.when(() -> ThreadUtils.sleep(5_000)).thenAnswer(invocation -> {
                assertFalse(server.isActive());
                verify(statusListener, times(1)).changeToStandBy();
                return null;
            });
            assertThrows(IllegalStateException.class, () -> remove(""));
        }
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        verify(registry, times(21)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testTransientElectionErrorStopsAndRestartsCoordinator() {
        server.start();
        when(registry.exists(SELECTOR_PATH))
                .thenThrow(new IllegalStateException("temporary registry failure"))
                .thenReturn(true);
        try (MockedStatic<ThreadUtils> threadUtils = mockStatic(ThreadUtils.class)) {
            threadUtils.when(() -> ThreadUtils.sleep(5_000)).thenAnswer(invocation -> {
                assertFalse(server.isActive());
                verify(statusListener).changeToStandBy();
                return null;
            });
            remove("");
        }
        assertTrue(server.isActive());
        verify(statusListener, times(2)).changeToActive();
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testAcquisitionErrorDoesNotReleaseUnacquiredLock() {
        when(registry.acquireLock(ELECTION_LOCK)).thenThrow(new IllegalStateException("lock unavailable"));
        try (MockedStatic<ThreadUtils> ignored = mockStatic(ThreadUtils.class)) {
            assertThrows(IllegalStateException.class, server::start);
        }
        assertFalse(server.isActive());
        verify(registry, never()).releaseLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testShutdownDuringLockAcquisitionDoesNotClaimSelector() {
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            // Shutdown is requested before the blocking acquisition returns to the election.
            server.close();
            return true;
        });
        server.start();
        assertFalse(server.isActive());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
        verify(registry).releaseLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testTerminalDemotionListenerPreventsRetryAndReactivation() {
        doAnswer(invocation -> {
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();
        server.start();
        when(registry.exists(SELECTOR_PATH))
                .thenThrow(new IllegalStateException("temporary registry failure"))
                .thenReturn(true);
        try (MockedStatic<ThreadUtils> threadUtils = mockStatic(ThreadUtils.class)) {
            remove("");
            // AlertServer shuts down on demotion, unlike restartable Master coordinators.
            threadUtils.verifyNoInteractions();
        }
        remove("");
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
        verify(registry, times(2)).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testClosePreventsRestartAndFurtherElection() {
        server.start();
        server.close();
        remove("");
        server.start();
        assertFalse(server.participateElection());
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        verify(registry, times(1)).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testRemoveCannotBeOverwrittenByEarlierStartupElection() throws Exception {
        CountDownLatch startupElectionFinished = new CountDownLatch(1);
        CountDownLatch allowStartupToReturn = new CountDownLatch(1);
        CountDownLatch callbackStarted = new CountDownLatch(1);
        AtomicBoolean firstRelease = new AtomicBoolean(true);
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstRelease.getAndSet(false)) {
                // Pause after the successful election but before startup publishes ACTIVE.
                startupElectionFinished.countDown();
                assertTrue(allowStartupToReturn.await(5, TimeUnit.SECONDS));
            }
            return true;
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> startup = executor.submit(server::start);
            assertTrue(startupElectionFinished.await(5, TimeUnit.SECONDS));
            String previousOwner = owner.get();
            owner.set("master-1:5678#peer-instance");
            Future<?> callback = executor.submit(() -> {
                callbackThread.set(Thread.currentThread());
                callbackStarted.countDown();
                remove(previousOwner);
            });
            assertTrue(callbackStarted.await(5, TimeUnit.SECONDS));
            // Wait for actual monitor contention (fixed code), or completion (old code).
            // This forces the relevant ordering rather than relying on a sleep or scheduler luck.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!callback.isDone() && !isBlockedOnServer(callbackThread.get())
                    && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertTrue(callback.isDone() || isBlockedOnServer(callbackThread.get()));
            allowStartupToReturn.countDown();
            startup.get(5, TimeUnit.SECONDS);
            callback.get(5, TimeUnit.SECONDS);
            assertFalse(server.isActive());
            verify(statusListener).changeToActive();
            verify(statusListener).changeToStandBy();
        } finally {
            allowStartupToReturn.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private boolean isBlockedOnServer(Thread thread) {
        ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(thread.getId());
        if (threadInfo == null || threadInfo.getThreadState() != Thread.State.BLOCKED) {
            return false;
        }
        LockInfo lockInfo = threadInfo.getLockInfo();
        return lockInfo != null && lockInfo.getIdentityHashCode() == System.identityHashCode(server);
    }

    private void remove(String previousOwner) {
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, previousOwner, Event.Type.REMOVE));
    }

    private AbstractHAServer newServer() {
        return newServer(ADDRESS);
    }

    private AbstractHAServer newServer(String address) {
        return new AbstractHAServer(registry, SELECTOR_PATH, address) {

            @Override
            public void close() {
                super.close();
            }
        };
    }
}
