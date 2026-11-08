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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeast;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AbstractHAServerTest {

    private static final String SELECTOR_PATH = "/coordinator";
    private static final String ELECTION_LOCK = SELECTOR_PATH + "-lock";
    private static final String ADDRESS = "master-0:5678";

    private final List<AbstractHAServer> servers = new ArrayList<>();
    private final List<ScheduledThreadPoolExecutor> electionExecutors = new ArrayList<>();
    private final AtomicReference<Runnable> retryAction = new AtomicReference<>(() -> {
    });
    private final AtomicReference<Runnable> beforeRecovery = new AtomicReference<>(() -> {
    });
    private ScheduledThreadPoolExecutor electionExecutor;
    private long recoveryDelayMillis = TimeUnit.DAYS.toMillis(1);
    private final AtomicReference<Throwable> workerFailure = new AtomicReference<>();

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
        electionExecutor = electionExecutors.get(0);
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

    @AfterEach
    void tearDown() throws Exception {
        servers.forEach(AbstractHAServer::close);
        for (ExecutorService executor : electionExecutors) {
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
        assertNoWorkerFailure();
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
    void testDoesNotAdoptPredecessorWithEarlierTimestamp() {
        // Seed an earlier incarnation explicitly. Millisecond timestamps do not guarantee
        // different identities for two same-address instances created in the same millisecond.
        String previousOwner = ADDRESS + "#1";
        owner.set(previousOwner);
        server.start();
        assertFalse(server.isActive());
        verify(statusListener, never()).changeToActive();
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));

        owner.set(null);
        remove(previousOwner);
        assertTrue(server.isActive());
        assertNotEquals(previousOwner, owner.get());
        verify(statusListener).changeToActive();
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
        drain();
        assertFalse(server.isActive());
        verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testUnacquiredLockIsNotReleased() {
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false);
        assertThrows(IllegalStateException.class, server::start);
        assertFalse(server.isActive());
        verify(registry, never()).releaseLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testLockContentionRetriesWithoutDemotingCurrentOwner() {
        server.start();
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false, true);
        remove("");
        assertTrue(server.isActive());
        verify(statusListener, never()).changeToStandBy();
        verify(registry, times(3)).acquireLock(ELECTION_LOCK);
        verify(registry, times(2)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testOwnRemovalDemotesWhileEarlierElectionIsBlocked() throws Exception {
        enableMasterDemotion();
        server.start();

        CountDownLatch oldResultReady = new CountDownLatch(1);
        CountDownLatch releaseOldElection = new CountDownLatch(1);
        CountDownLatch demoted = new CountDownLatch(1);
        doAnswer(invocation -> {
            demoted.countDown();
            return null;
        }).when(statusListener).changeToStandBy();
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            // Hold the earlier check after it has already read this instance as the owner.
            oldResultReady.countDown();
            await(releaseOldElection);
            return true;
        });
        try {
            notifyRemove("earlier-peer");
            assertTrue(oldResultReady.await(5, TimeUnit.SECONDS));
            String removedOwner = owner.getAndSet("master-1:5678#peer-instance");
            notifyRemove(removedOwner);

            assertTrue(demoted.await(5, TimeUnit.SECONDS));
            assertFalse(server.isActive());

            releaseOldElection.countDown();
            drain();
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToActive();
            verify(statusListener, times(1)).changeToStandBy();
        } finally {
            releaseOldElection.countDown();
        }
    }

    @Test
    void testOwnRemovalDefersElectionUntilOldWorkersExit() throws Exception {
        enableMasterDemotion();
        recoveryDelayMillis = 0;
        server.start();
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch allowStop = new CountDownLatch(1);
        CountDownLatch checkedPeer = new CountDownLatch(1);
        server.addServerStatusChangeListener((origin, target) -> {
            if (origin == HAServer.ServerStatus.ACTIVE && target == HAServer.ServerStatus.STAND_BY) {
                stopping.countDown();
                await(allowStop);
            } else if (origin == HAServer.ServerStatus.STAND_BY && target == HAServer.ServerStatus.STAND_BY) {
                checkedPeer.countDown();
            }
        });
        try {
            notifyRemove(owner.getAndSet(null));
            assertTrue(stopping.await(5, TimeUnit.SECONDS));
            // Drain the election queue while the demotion listener is still waiting for an old worker.
            drain();
            verify(registry, times(1)).acquireLock(ELECTION_LOCK);
            verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));

            // A peer can take the empty selector. No further REMOVE is needed to discover it after drain.
            owner.set("master-1:5678#peer-instance");
            allowStop.countDown();
            assertTrue(checkedPeer.await(5, TimeUnit.SECONDS));
            drain();
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            allowStop.countDown();
        }
    }

    @Test
    void testRemovalDuringMissingSelectorReadPreventsNewClaim() throws Exception {
        enableMasterDemotion();
        server.start();
        String previousOwner = owner.getAndSet(null);
        CountDownLatch readMissing = new CountDownLatch(1);
        CountDownLatch returnRead = new CountDownLatch(1);
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch allowStop = new CountDownLatch(1);
        CountDownLatch releasedLock = new CountDownLatch(1);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            readMissing.countDown();
            await(returnRead);
            return false;
        });
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            releasedLock.countDown();
            return true;
        });
        doAnswer(invocation -> {
            stopping.countDown();
            await(allowStop);
            return null;
        }).when(statusListener).changeToStandBy();
        try {
            notifyRemove("earlier-peer");
            assertTrue(readMissing.await(5, TimeUnit.SECONDS));
            notifyRemove(previousOwner);
            assertTrue(stopping.await(5, TimeUnit.SECONDS));
            returnRead.countDown();
            assertTrue(releasedLock.await(5, TimeUnit.SECONDS));
            verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
            owner.set("master-1:5678#peer-instance");
            when(registry.exists(SELECTOR_PATH)).thenReturn(true);
            allowStop.countDown();
            drain();
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToActive();
        } finally {
            returnRead.countDown();
            allowStop.countDown();
        }
    }

    @Test
    void testQueuedOwnRemovalDoesNotDemoteRestoredLeaderAgain() throws Exception {
        enableMasterDemotion();
        server.start();
        CountDownLatch releaseElection = new CountDownLatch(1);
        CountDownLatch demoted = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        electionExecutor.execute(() -> await(releaseElection));
        doAnswer(invocation -> {
            demoted.countDown();
            return null;
        }).when(statusListener).changeToStandBy();
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (reads.incrementAndGet() == 2) {
                throw new IllegalStateException("temporary failure in the queued own REMOVE check");
            }
            return owner.get();
        });
        retryAction.set(() -> verify(statusListener, times(1)).changeToStandBy());
        try {
            // An earlier queued check will restore A before this own REMOVE's check encounters its read failure.
            notifyRemove("earlier-peer");
            notifyRemove(owner.get());
            assertTrue(demoted.await(5, TimeUnit.SECONDS));
            synchronized (server) {
                // The demotion listener and its recovery scheduling must both finish before the queue is released.
            }
            releaseElection.countDown();
            drain();
            assertTrue(server.isActive());
            verify(statusListener, times(1)).changeToStandBy();
            verify(statusListener, times(2)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            releaseElection.countDown();
        }
    }

    @Test
    void testCloseWhileIndependentDemotionIsQueued() throws Exception {
        enableMasterDemotion();
        server.start();
        String previousOwner = owner.get();
        synchronized (server) {
            // Hold the publication monitor so the independent demotion remains queued until close finishes.
            notifyRemove(previousOwner);
            server.close();
        }
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        notifyRemove(previousOwner);
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testCloseWaitsForIndependentDemotionListener() throws Exception {
        enableMasterDemotion();
        recoveryDelayMillis = 0;
        server.start();
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch allowStop = new CountDownLatch(1);
        doAnswer(invocation -> {
            stopping.countDown();
            await(allowStop);
            return null;
        }).when(statusListener).changeToStandBy();
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            notifyRemove(owner.get());
            assertTrue(stopping.await(5, TimeUnit.SECONDS));
            Future<?> close = caller.submit(server::close);
            // The election worker can exit, but close still waits for the independently running listener.
            assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(close.isDone());
            allowStop.countDown();
            close.get(5, TimeUnit.SECONDS);
            notifyRemove(owner.get());
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToActive();
            verify(statusListener, times(1)).changeToStandBy();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            allowStop.countDown();
            caller.shutdownNow();
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testConfirmedOwnershipLossSurvivesLaterRemovalAndReadFailures() throws Exception {
        recoveryDelayMillis = 0;
        server.start();
        String previousOwner = owner.getAndSet("master-1:5678#peer-instance");
        CountDownLatch peerObserved = new CountDownLatch(1);
        CountDownLatch returnPeerResult = new CountDownLatch(1);
        CountDownLatch recoveryStarted = new CountDownLatch(1);
        CountDownLatch allowRecovery = new CountDownLatch(1);
        AtomicBoolean firstRelease = new AtomicBoolean(true);
        AtomicInteger remainingFailures = new AtomicInteger();
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstRelease.compareAndSet(true, false)) {
                peerObserved.countDown();
                await(returnPeerResult);
            }
            return true;
        });
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (remainingFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("reads fail after a confirmed peer owner");
            }
            return owner.get() != null;
        });
        beforeRecovery.set(() -> {
            recoveryStarted.countDown();
            await(allowRecovery);
        });
        try {
            notifyRemove(previousOwner);
            assertTrue(peerObserved.await(5, TimeUnit.SECONDS));
            // A second REMOVE makes the peer result old, but A has not acquired ownership since that read.
            notifyRemove(owner.getAndSet(null));
            remainingFailures.set(20);
            returnPeerResult.countDown();
            assertTrue(recoveryStarted.await(5, TimeUnit.SECONDS));
            assertEquals(0, remainingFailures.get());
            assertFalse(server.isActive(), "confirmed loss must be published despite the later REMOVE");
            verify(statusListener, times(1)).changeToStandBy();

            // Keep the recovery associated with the later REMOVE, even though the loss was published.
            allowRecovery.countDown();
            drain();
            assertTrue(server.isActive());
            verify(statusListener, times(2)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            returnPeerResult.countDown();
            allowRecovery.countDown();
        }
    }

    @Test
    void testIndependentDemotionKeepsRunningRecoveryTracked() throws Exception {
        enableMasterDemotion();
        recoveryDelayMillis = 0;
        server.start();
        CountDownLatch oldResultReady = new CountDownLatch(1);
        CountDownLatch returnOldResult = new CountDownLatch(1);
        CountDownLatch recoveryStarted = new CountDownLatch(1);
        CountDownLatch allowRecovery = new CountDownLatch(1);
        CountDownLatch demoted = new CountDownLatch(1);
        AtomicBoolean firstRelease = new AtomicBoolean(true);
        AtomicInteger remainingFailures = new AtomicInteger();
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstRelease.compareAndSet(true, false)) {
                oldResultReady.countDown();
                await(returnOldResult);
            }
            return true;
        });
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (remainingFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("queued check cannot verify ownership");
            }
            return true;
        });
        beforeRecovery.set(() -> {
            // The Future is already running. A successor, if incorrectly scheduled, stays visible in the queue.
            recoveryDelayMillis = TimeUnit.DAYS.toMillis(1);
            recoveryStarted.countDown();
            await(allowRecovery);
        });
        doAnswer(invocation -> {
            demoted.countDown();
            return null;
        }).when(statusListener).changeToStandBy();
        try {
            notifyRemove("earlier-peer");
            assertTrue(oldResultReady.await(5, TimeUnit.SECONDS));
            notifyRemove("later-peer");
            remainingFailures.set(20);
            returnOldResult.countDown();
            assertTrue(recoveryStarted.await(5, TimeUnit.SECONDS));
            assertTrue(server.isActive());

            notifyRemove(owner.get());
            assertTrue(demoted.await(5, TimeUnit.SECONDS));
            synchronized (server) {
                // Wait until the independent demotion has also finished its recovery scheduling.
            }
            allowRecovery.countDown();
            drain();
            assertTrue(server.isActive());
            verify(statusListener, times(1)).changeToStandBy();
            verify(statusListener, times(2)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty(), "no untracked successor recovery remains");
            server.close();
            assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        } finally {
            returnOldResult.countDown();
            allowRecovery.countDown();
        }
    }

    @Test
    void testOwnRemovalAbortedBeforeReadDoesNotCloseCurrentOwner() throws Exception {
        server.start();
        CountDownLatch acquiringLock = new CountDownLatch(1);
        CountDownLatch allowLock = new CountDownLatch(1);
        AtomicBoolean firstAcquire = new AtomicBoolean(true);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstAcquire.compareAndSet(true, false)) {
                acquiringLock.countDown();
                await(allowLock);
            }
            return true;
        });
        doAnswer(invocation -> {
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();
        try {
            // The selector still belongs to A when notifications from older ownership periods arrive.
            notifyRemove(owner.get());
            assertTrue(acquiringLock.await(5, TimeUnit.SECONDS));
            notifyRemove("earlier-peer");
            Future<?> drained = electionExecutor.submit(() -> {
            });
            allowLock.countDown();
            drained.get(5, TimeUnit.SECONDS);
            assertTrue(server.isActive());
            assertFalse(electionExecutor.isShutdown());
            verify(statusListener, never()).changeToStandBy();
            verify(statusListener, times(1)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            allowLock.countDown();
        }
    }

    @Test
    void testMissingSelectorDemotesBeforeFailedPeerCheck() throws Exception {
        recoveryDelayMillis = 0;
        server.start();
        String previousOwner = owner.getAndSet(null);
        CountDownLatch readMissing = new CountDownLatch(1);
        CountDownLatch returnRead = new CountDownLatch(1);
        CountDownLatch recoveryStarted = new CountDownLatch(1);
        CountDownLatch allowRecovery = new CountDownLatch(1);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        AtomicInteger remainingFailures = new AtomicInteger();
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (firstRead.compareAndSet(true, false)) {
                readMissing.countDown();
                await(returnRead);
                return false;
            }
            if (remainingFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("peer check cannot verify ownership");
            }
            return owner.get() != null;
        });
        beforeRecovery.set(() -> {
            recoveryStarted.countDown();
            await(allowRecovery);
        });
        try {
            notifyRemove(previousOwner);
            assertTrue(readMissing.await(5, TimeUnit.SECONDS));
            // A newer REMOVE aborts the own-removal check before it can claim the missing selector.
            notifyRemove("earlier-peer");
            remainingFailures.set(20);
            returnRead.countDown();
            assertTrue(recoveryStarted.await(5, TimeUnit.SECONDS));
            assertEquals(0, remainingFailures.get());
            assertFalse(server.isActive(), "a missing selector must still stop the former leader");
            verify(statusListener, times(1)).changeToStandBy();
            verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));

            // Retain the existing recovery; no additional Registry event is needed after reads recover.
            allowRecovery.countDown();
            drain();
            assertTrue(server.isActive());
            verify(statusListener, times(2)).changeToActive();
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            returnRead.countDown();
            allowRecovery.countDown();
        }
    }

    @Test
    void testCloseAfterMissingSelectorCancelsRecovery() throws Exception {
        server.start();
        String previousOwner = owner.getAndSet(null);
        CountDownLatch readMissing = new CountDownLatch(1);
        CountDownLatch returnRead = new CountDownLatch(1);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            readMissing.countDown();
            await(returnRead);
            return false;
        });
        doAnswer(invocation -> {
            // Alert closes its HA server from the standby listener.
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();
        try {
            notifyRemove(previousOwner);
            assertTrue(readMissing.await(5, TimeUnit.SECONDS));
            notifyRemove("earlier-peer");
            returnRead.countDown();
            assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToStandBy();
            verify(statusListener, times(1)).changeToActive();
            verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
            assertTrue(electionExecutor.getQueue().isEmpty());
        } finally {
            returnRead.countDown();
        }
    }

    @Test
    void testOwnRemovalDemotesBeforeRetryingFailedOwnershipRead() {
        server.start();
        String removedOwner = owner.get();
        owner.set("master-1:5678#peer-instance");
        when(registry.exists(SELECTOR_PATH))
                .thenThrow(new IllegalStateException("registry unavailable"))
                .thenReturn(true);
        retryAction.set(() -> assertFalse(server.isActive(), "demotion must precede the first retry"));
        remove(removedOwner);
        assertFalse(server.isActive());
        verify(statusListener).changeToStandBy();
        verify(registry, times(3)).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testAcquisitionErrorDoesNotReleaseUnacquiredLock() {
        when(registry.acquireLock(ELECTION_LOCK)).thenThrow(new IllegalStateException("lock unavailable"));
        assertThrows(IllegalStateException.class, server::start);
        assertTrue(electionExecutor.isShutdown());
        assertFalse(server.isActive());
        verify(registry, never()).releaseLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testTransientElectionErrorKeepsRoleUntilOwnershipDecision() {
        server.start();
        when(registry.exists(SELECTOR_PATH))
                .thenThrow(new IllegalStateException("temporary registry failure"))
                .thenReturn(true);
        retryAction.set(() -> {
            assertTrue(server.isActive());
            verify(statusListener, never()).changeToStandBy();
        });
        remove("");
        assertTrue(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testExhaustedRetriesPreserveOriginalRole() {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new IllegalStateException("registry unavailable"));
        // Asynchronous callback failures are logged by the worker, not thrown on the Registry thread.
        remove("");
        // An empty REMOVE payload does not confirm the loss of this instance's selector.
        assertTrue(server.isActive());
        verify(statusListener, never()).changeToStandBy();
        verify(registry, times(21)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testRemoveInvalidatesEarlierStartupPromotion() throws Exception {
        CountDownLatch startupElectionFinished = new CountDownLatch(1);
        CountDownLatch allowStartupToReturn = new CountDownLatch(1);
        AtomicBoolean firstRelease = new AtomicBoolean(true);
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstRelease.getAndSet(false)) {
                // Pause after election, before publication; the notification must queue behind it.
                startupElectionFinished.countDown();
                assertTrue(allowStartupToReturn.await(5, TimeUnit.SECONDS));
            }
            return true;
        });
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            Future<?> startup = caller.submit(server::start);
            assertTrue(startupElectionFinished.await(5, TimeUnit.SECONDS));
            String removedOwner = owner.get();
            String peerOwner = "master-1:5678#peer-instance";
            owner.set(peerOwner);
            notifyRemove(removedOwner);
            allowStartupToReturn.countDown();
            startup.get(5, TimeUnit.SECONDS);
            drain();
            assertFalse(server.isActive());
            verify(statusListener, never()).changeToActive();
            verify(statusListener, never()).changeToStandBy();
            assertTrue(electionExecutor.getQueue().isEmpty(), "a definitive result cancels recovery");

            owner.set(null);
            remove(peerOwner);
            assertTrue(server.isActive());
            assertEquals(removedOwner, owner.get());
            verify(statusListener).changeToActive();
        } finally {
            allowStartupToReturn.countDown();
            caller.shutdownNow();
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testDeferredPromotionRecoversWithoutAnotherRemove() throws Exception {
        recoveryDelayMillis = 0;
        CountDownLatch activated = new CountDownLatch(1);
        doAnswer(invocation -> {
            activated.countDown();
            return null;
        }).when(statusListener).changeToActive();

        // The queued check exhausts 20 attempts; the first scheduled check also fails.
        deferStartupPromotion(21);

        assertTrue(activated.await(5, TimeUnit.SECONDS));
        drain();
        assertTrue(server.isActive());
        verify(statusListener).changeToActive();
        verify(registry, times(23)).exists(SELECTOR_PATH);
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testOwnRemovalDemotionRecoversWithoutAnotherRemove() throws Exception {
        verifyOwnRemovalDemotionRecovers();
    }

    @Test
    void testIndependentDemotionRecoversWithoutAnotherRemove() throws Exception {
        enableMasterDemotion();
        verifyOwnRemovalDemotionRecovers();
    }

    private void verifyOwnRemovalDemotionRecovers() throws Exception {
        recoveryDelayMillis = 0;
        server.start();
        String currentOwner = owner.get();
        CountDownLatch reactivated = new CountDownLatch(1);
        doAnswer(invocation -> {
            reactivated.countDown();
            return null;
        }).when(statusListener).changeToActive();
        AtomicInteger remainingFailures = new AtomicInteger(20);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (remainingFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("registry unavailable");
            }
            return true;
        });

        // This notification belongs to an earlier selector. The current selector still belongs to A.
        notifyRemove(currentOwner);

        assertTrue(reactivated.await(5, TimeUnit.SECONDS));
        drain();
        assertEquals(currentOwner, owner.get());
        assertTrue(server.isActive());
        verify(statusListener).changeToStandBy();
        verify(statusListener, times(2)).changeToActive();
        // Independent demotion may defer a check. Assert recovery after all injected failures, not its scheduling
        // order.
        assertEquals(0, remainingFailures.get());
        verify(registry, atLeast(22)).exists(SELECTOR_PATH);
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testIndependentDemotionRechecksAfterLateOwnRemoval() throws Exception {
        enableMasterDemotion();
        recoveryDelayMillis = 0;
        server.start();

        String currentOwner = owner.get();
        CountDownLatch reactivated = new CountDownLatch(1);
        doAnswer(invocation -> {
            reactivated.countDown();
            return null;
        }).when(statusListener).changeToActive();

        // The current selector was reacquired by this instance before the old event arrived.
        notifyRemove(currentOwner);

        assertTrue(reactivated.await(5, TimeUnit.SECONDS));
        drain();
        assertTrue(server.isActive());
        assertEquals(currentOwner, owner.get());
        verify(statusListener, times(1)).changeToStandBy();
        verify(statusListener, times(2)).changeToActive();
    }

    @Test
    void testCloseCancelsDeferredPromotionRecovery() throws Exception {
        deferStartupPromotion(20);
        drain();

        assertFalse(server.isActive());
        assertEquals(1, electionExecutor.getQueue().size());
        server.close();
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        verify(statusListener, never()).changeToActive();
        verify(registry, times(21)).exists(SELECTOR_PATH);
    }

    private void deferStartupPromotion(int readFailures) throws Exception {
        CountDownLatch startupElectionFinished = new CountDownLatch(1);
        CountDownLatch allowStartupToReturn = new CountDownLatch(1);
        AtomicBoolean firstRelease = new AtomicBoolean(true);
        AtomicInteger remainingFailures = new AtomicInteger();
        when(registry.releaseLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (firstRelease.getAndSet(false)) {
                startupElectionFinished.countDown();
                await(allowStartupToReturn);
            }
            return true;
        });
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (remainingFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("registry unavailable");
            }
            return owner.get() != null;
        });
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            Future<?> startup = caller.submit(server::start);
            assertTrue(startupElectionFinished.await(5, TimeUnit.SECONDS));
            remainingFailures.set(readFailures);
            // A delayed predecessor notification arrives while A already holds the new selector.
            notifyRemove("predecessor#1");
            allowStartupToReturn.countDown();
            startup.get(5, TimeUnit.SECONDS);
        } finally {
            allowStartupToReturn.countDown();
            caller.shutdownNow();
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testCallbacksReturnWhileRoleListenerIsBlockedAndRecheckCurrentOwner() throws Exception {
        server.start();
        String ownIdentity = owner.get();
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch allowStop = new CountDownLatch(1);
        AtomicReference<Thread> listenerThread = new AtomicReference<>();
        server.addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
            }

            @Override
            public void changeToStandBy() {
                listenerThread.set(Thread.currentThread());
                stopping.countDown();
                await(allowStop);
            }
        });
        try {
            owner.set("master-1:5678#peer-instance");
            notifyRemove("");
            assertTrue(stopping.await(5, TimeUnit.SECONDS));
            assertNotEquals(Thread.currentThread(), listenerThread.get());
            // Queue while this instance appears to own the key, then change the owner again.
            // The queued request must re-read the Registry, not publish a captured ACTIVE decision.
            owner.set(ownIdentity);
            notifyRemove("");
            owner.set("master-2:5678#peer-instance");
            allowStop.countDown();
            drain();
            assertFalse(server.isActive());
            verify(statusListener, times(1)).changeToActive();
        } finally {
            allowStop.countDown();
        }
    }

    @Test
    void testElectionAndPublicationRunOnSameWorker() {
        AtomicReference<Thread> electionThread = new AtomicReference<>();
        AtomicReference<Thread> publicationThread = new AtomicReference<>();
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            electionThread.set(Thread.currentThread());
            return true;
        });
        server.addServerStatusChangeListener((origin, target) -> publicationThread.set(Thread.currentThread()));
        server.start();
        assertNotEquals(Thread.currentThread(), electionThread.get());
        assertEquals(electionThread.get(), publicationThread.get());
        owner.set("master-1:5678#peer-instance");
        remove("");
        assertEquals(electionThread.get(), publicationThread.get());
    }

    @Test
    void testCloseDiscardsPendingRequestAndLateElectionResult() throws Exception {
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch allowElection = new CountDownLatch(1);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            acquired.countDown();
            assertTrue(allowElection.await(5, TimeUnit.SECONDS));
            return true;
        });
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            Future<?> startup = caller.submit(server::start);
            assertTrue(acquired.await(5, TimeUnit.SECONDS));
            notifyRemove("");
            server.close();
            // A notification after shutdown is ignored, including the submit/shutdown race.
            notifyRemove("");
            allowElection.countDown();
            startup.get(5, TimeUnit.SECONDS);
            assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(server.isActive());
            verify(statusListener, never()).changeToActive();
            verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        } finally {
            allowElection.countDown();
            caller.shutdownNow();
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testCloseFromListenerDoesNotDeadlockOrActivateQueuedWork() throws Exception {
        server.start();
        CountDownLatch closed = new CountDownLatch(1);
        server.addServerStatusChangeListener(new AbstractServerStatusChangeListener() {

            @Override
            public void changeToActive() {
            }

            @Override
            public void changeToStandBy() {
                // Alert closes its HA server from the demotion listener on the election worker.
                server.close();
                owner.set(null);
                notifyRemove("");
                closed.countDown();
            }
        });
        AbstractServerStatusChangeListener subsequentListener = mock(AbstractServerStatusChangeListener.class);
        server.addServerStatusChangeListener(subsequentListener);
        owner.set("master-1:5678#peer-instance");
        notifyRemove("");
        assertTrue(closed.await(5, TimeUnit.SECONDS));
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        org.mockito.Mockito.verifyNoInteractions(subsequentListener);
    }

    @Test
    void testCloseDoesNotStrandQueuedStartupFuture() throws Exception {
        CountDownLatch workerBlocked = new CountDownLatch(1);
        CountDownLatch releaseWorker = new CountDownLatch(1);
        electionExecutor.execute(() -> {
            workerBlocked.countDown();
            await(releaseWorker);
        });
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            assertTrue(workerBlocked.await(5, TimeUnit.SECONDS));
            Future<?> startup = caller.submit(server::start);
            // Observe the submitted Future in the queue, not just subscribe() before submission.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (electionExecutor.getQueue().isEmpty() && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertEquals(1, electionExecutor.getQueue().size());
            server.close();
            releaseWorker.countDown();
            startup.get(5, TimeUnit.SECONDS);
            assertFalse(server.isActive());
            verify(registry, never()).acquireLock(ELECTION_LOCK);
        } finally {
            releaseWorker.countDown();
            caller.shutdownNow();
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testCloseBeforeStartupSubmissionRejectsWithoutWaiting() {
        doAnswer(invocation -> {
            // Close after subscription but before the startup Future can be submitted.
            server.close();
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
        assertThrows(java.util.concurrent.RejectedExecutionException.class, server::start);
        verify(registry, never()).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testExternalCloseWaitsForEnteredListener() throws Exception {
        server.start();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch releaseListener = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        server.addServerStatusChangeListener((origin, target) -> {
            entered.countDown();
            await(releaseListener);
        });
        Thread closer = new Thread(() -> {
            try {
                server.close();
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });
        closer.setDaemon(true);
        try {
            owner.set("master-1:5678#peer-instance");
            notifyRemove("");
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            closer.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (closer.isAlive() && closer.getState() != Thread.State.BLOCKED
                    && System.nanoTime() < deadline) {
                Thread.yield();
            }
            // The listener holds the publication monitor; external close must wait for it.
            assertEquals(Thread.State.BLOCKED, closer.getState());
            releaseListener.countDown();
            closer.join(5000);
            assertFalse(closer.isAlive());
            assertNull(failure.get());
        } finally {
            releaseListener.countDown();
            closer.join(5000);
        }
    }

    private void remove(String previousOwner) {
        notifyRemove(previousOwner);
        drain();
    }

    private void notifyRemove(String previousOwner) {
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, previousOwner, Event.Type.REMOVE));
    }

    private void drain() {
        try {
            electionExecutor.submit(() -> {
            }).get(5, TimeUnit.SECONDS);
            assertNoWorkerFailure();
        } catch (Exception e) {
            throw new AssertionError("Election worker did not finish", e);
        }
    }

    private void assertNoWorkerFailure() {
        if (workerFailure.get() != null) {
            throw new AssertionError("Election worker failed", workerFailure.get());
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private AbstractHAServer newServer() {
        return newServer(ADDRESS);
    }

    private void enableMasterDemotion() {
        server = newServer(ADDRESS, true);
        electionExecutor = electionExecutors.get(electionExecutors.size() - 1);
        server.addServerStatusChangeListener(statusListener);
    }

    private AbstractHAServer newServer(String address) {
        return newServer(address, false);
    }

    private AbstractHAServer newServer(String address, boolean masterDemotion) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(() -> {
                // Static mocks are thread-local, so install the retry delay stub on the election worker.
                try (
                        MockedStatic<ThreadUtils> threadUtils =
                                mockStatic(ThreadUtils.class, CALLS_REAL_METHODS)) {
                    threadUtils.when(() -> ThreadUtils.sleep(anyLong())).thenAnswer(invocation -> {
                        retryAction.get().run();
                        return null;
                    });
                    runnable.run();
                }
            }, "test-ha-election");
            thread.setUncaughtExceptionHandler(
                    (failedThread, failure) -> workerFailure.compareAndSet(null, failure));
            return thread;
        }) {

            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                if (delay > 0) {
                    // Control the timer without sleeps or wall-clock assumptions in the tests.
                    return super.schedule(() -> {
                        beforeRecovery.get().run();
                        command.run();
                    }, recoveryDelayMillis, TimeUnit.MILLISECONDS);
                }
                return super.schedule(command, delay, unit);
            }
        };
        electionExecutors.add(executor);
        AbstractHAServer result = new AbstractHAServer(registry, SELECTOR_PATH, address, executor, masterDemotion) {
        };
        servers.add(result);
        return result;
    }
}
