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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

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
    private final List<ExecutorService> registryExecutors = new ArrayList<>();
    private final List<ExecutorService> callers = new ArrayList<>();
    private ScheduledThreadPoolExecutor electionExecutor;
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
        callers.forEach(ExecutorService::shutdownNow);
        for (ExecutorService executor : electionExecutors) {
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
        for (ExecutorService executor : registryExecutors) {
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
        for (ExecutorService caller : callers) {
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
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
        try (MockedStatic<ThreadUtils> threadUtils = mockStatic(ThreadUtils.class, CALLS_REAL_METHODS)) {
            threadUtils.when(() -> ThreadUtils.sleep(anyLong())).thenAnswer(invocation -> null);
            assertThrows(IllegalStateException.class, server::participateElection);
        }
        verify(registry, times(20)).acquireLock(ELECTION_LOCK);
        verify(registry, never()).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testAcquisitionErrorDoesNotReleaseUnacquiredLock() {
        when(registry.acquireLock(ELECTION_LOCK)).thenThrow(new RegistryException("unavailable"));
        try (MockedStatic<ThreadUtils> threadUtils = mockStatic(ThreadUtils.class, CALLS_REAL_METHODS)) {
            threadUtils.when(() -> ThreadUtils.sleep(anyLong())).thenAnswer(invocation -> null);
            assertThrows(IllegalStateException.class, server::participateElection);
        }
        verify(registry, never()).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testInitialRetryWaitsForTheLogicalStartup() throws Exception {
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable")).thenReturn(false);
        ExecutorService caller = newCaller();
        Future<?> startup = caller.submit(server::start);
        awaitCondition(() -> org.mockito.Mockito.mockingDetails(registry).getInvocations().stream()
                .anyMatch(invocation -> invocation.getMethod().getName().equals("exists")));
        drain();
        assertFalse(startup.isDone(), "scheduling a retry must not complete startup");

        advanceRetry();
        startup.get(5, TimeUnit.SECONDS);
        assertTrue(server.isActive());
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testInitialRetryExhaustionReachesTheCaller() throws Exception {
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false);
        ExecutorService caller = newCaller();
        Future<?> startup = caller.submit(server::start);
        awaitCondition(() -> !electionExecutor.getQueue().isEmpty());
        drain();
        for (int i = 1; i < 20; i++) {
            assertFalse(startup.isDone());
            advanceRetry();
        }
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> startup.get(5, TimeUnit.SECONDS));
        assertTrue(failure.getCause() instanceof IllegalStateException);
        assertFalse(server.isActive());
        verify(registry, times(20)).acquireLock(ELECTION_LOCK);
        verify(registry, never()).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testSynchronousWatchWaitsForSubscriptionAndInitialAdmission() throws Exception {
        owner.set("master-1#peer");
        CountDownLatch subscribed = new CountDownLatch(1);
        CountDownLatch releaseSubscribe = new CountDownLatch(1);
        doAnswer(invocation -> {
            SubscribeListener listener = invocation.getArgument(1);
            subscriber.set(listener);
            listener.notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "", Event.Type.REMOVE));
            subscribed.countDown();
            await(releaseSubscribe);
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
        Future<?> startup = newCaller().submit(server::start);
        try {
            await(subscribed);
            electionExecutor.submit(() -> {
            }).get(5, TimeUnit.SECONDS);
            assertFalse(startup.isDone(), "watch cannot complete startup before subscription succeeds");
            verify(registry, never()).acquireLock(ELECTION_LOCK);
        } finally {
            releaseSubscribe.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        assertFalse(server.isActive());
    }

    @Test
    void testSubscriptionFailureReachesTheCaller() {
        doAnswer(invocation -> {
            throw new RegistryException("subscription failed");
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
        assertThrows(RegistryException.class, server::start);
        assertFalse(server.isActive());
    }

    @Test
    void testRemoveInvalidatesEarlierStartupPromotion() throws Exception {
        CountDownLatch resultReady = new CountDownLatch(1);
        CountDownLatch releaseResult = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                resultReady.countDown();
                await(releaseResult);
            }
            return null;
        }).when(registry).releaseLock(ELECTION_LOCK);
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(resultReady.await(5, TimeUnit.SECONDS));
            owner.set("master-1#peer");
            notifyRemove(owner.get());
        } finally {
            releaseResult.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        drain();
        assertFalse(server.isActive());
        verify(statusListener, never()).changeToActive();
        assertTrue(electionExecutor.getQueue().isEmpty(), "a fresh definitive result cancels recovery");
    }

    @Test
    void testDeferredStartupRecoversWithoutAnotherRemove() throws Exception {
        CountDownLatch resultReady = new CountDownLatch(1);
        CountDownLatch releaseResult = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                resultReady.countDown();
                await(releaseResult);
            }
            return null;
        }).when(registry).releaseLock(ELECTION_LOCK);
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(resultReady.await(5, TimeUnit.SECONDS));
            notifyRemove("");
            // The queued REMOVE exhausts its finite attempts; the persistent recheck must survive it.
            when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        } finally {
            releaseResult.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        drain();
        assertFalse(server.isActive());
        for (int i = 0; i < 21; i++) {
            advanceRetry();
        }
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        for (int i = 0; i < 3 && !server.isActive(); i++) {
            advanceRetry();
        }
        assertTrue(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testDefaultPolicyWaitsForEarlierCheckBeforeDemoting() throws Exception {
        server.start();
        String ownIdentity = owner.get();
        CountDownLatch readEntered = new CountDownLatch(1);
        CountDownLatch releaseRead = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                readEntered.countDown();
                await(releaseRead);
                return ownIdentity;
            }
            return owner.get();
        });
        notifyRemove("");
        try {
            assertTrue(readEntered.await(5, TimeUnit.SECONDS));
            owner.set("master-1#peer");
            notifyRemove(ownIdentity);
            // The default Alert policy verifies ownership before closing its resources.
            assertTrue(server.isActive());
            verify(statusListener, never()).changeToStandBy();
        } finally {
            releaseRead.countDown();
        }
        drain();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testUnreadOwnRemovalDoesNotDemoteCurrentOwner() throws Exception {
        server.start();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch releaseAcquire = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                acquired.countDown();
                await(releaseAcquire);
            }
            return true;
        });
        notifyRemove(owner.get());
        try {
            assertTrue(acquired.await(5, TimeUnit.SECONDS));
            notifyRemove("");
        } finally {
            releaseAcquire.countDown();
        }
        drain();
        assertTrue(server.isActive());
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testConfirmedOwnershipLossSurvivesLaterRemovalAndReadFailure() throws Exception {
        server.start();
        owner.set("master-1#peer");
        CountDownLatch resultReady = new CountDownLatch(1);
        CountDownLatch releaseResult = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                resultReady.countDown();
                await(releaseResult);
            }
            return null;
        }).when(registry).releaseLock(ELECTION_LOCK);
        notifyRemove("");
        try {
            assertTrue(resultReady.await(5, TimeUnit.SECONDS));
            when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
            notifyRemove("");
        } finally {
            releaseResult.countDown();
        }
        drain();
        assertFalse(server.isActive(), "a confirmed loss is not discarded because a newer check cannot finish");
        verify(statusListener, times(1)).changeToStandBy();
        assertFalse(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testMissingSelectorDemotesWithoutClaimAfterNewRemoval() throws Exception {
        server.start();
        owner.set(null);
        CountDownLatch missing = new CountDownLatch(1);
        CountDownLatch releaseMissing = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                missing.countDown();
                await(releaseMissing);
                return false;
            }
            throw new RegistryException("unavailable");
        });
        notifyRemove("");
        try {
            assertTrue(missing.await(5, TimeUnit.SECONDS));
            notifyRemove("");
        } finally {
            releaseMissing.countDown();
        }
        drain();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testOwnRemovalReadFailureDemotesAndRecoversAfterTwentyFailures() throws Exception {
        server.start();
        AtomicInteger failures = new AtomicInteger();
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (failures.getAndIncrement() < 20) {
                throw new RegistryException("unavailable");
            }
            return true;
        });
        remove(owner.get());
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        for (int i = 1; i < 20; i++) {
            advanceRetry();
            assertFalse(server.isActive());
        }
        assertEquals(20, failures.get());
        // No new watch notification is delivered when reads become available again.
        advanceRetry();
        assertTrue(server.isActive());
        verify(statusListener, times(2)).changeToActive();
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testPeerFailureDoesNotCancelOwnRemovalRecovery() throws Exception {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove(owner.get());
        assertFalse(server.isActive());
        remove("");
        assertEquals(2, electionExecutor.getQueue().size(),
                "persistent recovery and finite peer retry remain distinct");
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        advanceRetry();
        assertTrue(server.isActive());
        // A finite peer continuation may still be queued; it must not restart the current owner.
        while (!electionExecutor.getQueue().isEmpty()) {
            advanceRetry();
        }
        verify(statusListener, times(2)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testFreshPeerOwnershipCancelsOwnRemovalRecovery() throws Exception {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove(owner.get());
        assertFalse(server.isActive());
        owner.set("master-1#peer");
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        remove("");
        assertFalse(server.isActive());
        assertTrue(electionExecutor.getQueue().isEmpty());
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testTransientPeerErrorPreservesRoleUntilDecision() throws Exception {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable")).thenReturn(true);
        remove("");
        assertTrue(server.isActive());
        verify(statusListener, never()).changeToStandBy();
        owner.set("master-1#peer");
        advanceRetry();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testOrdinaryRemoveRetainsFiniteRetryPolicy() throws Exception {
        server.start();
        org.mockito.Mockito.clearInvocations(registry);
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove("");
        for (int i = 1; i < 20; i++) {
            advanceRetry();
        }
        assertTrue(server.isActive());
        verify(registry, times(20)).exists(SELECTOR_PATH);
        verify(statusListener, never()).changeToStandBy();
        assertTrue(electionExecutor.getQueue().isEmpty(), "ordinary failures do not create persistent recovery");
    }

    @Test
    void testRegistryChecksAndReleaseUseDedicatedWorker() throws Exception {
        AtomicReference<Thread> electionThread = new AtomicReference<>();
        AtomicReference<Thread> publicationThread = new AtomicReference<>();
        AtomicReference<Thread> releaseThread = new AtomicReference<>();
        doAnswer(invocation -> {
            releaseThread.set(Thread.currentThread());
            return true;
        }).when(registry).releaseLock(ELECTION_LOCK);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            electionThread.set(Thread.currentThread());
            return true;
        });
        server.addServerStatusChangeListener((origin, target) -> publicationThread.set(Thread.currentThread()));
        server.start();
        assertNotEquals(Thread.currentThread(), electionThread.get());
        assertNotEquals(electionThread.get(), publicationThread.get());
        assertEquals(electionThread.get(), releaseThread.get());
        Thread initialThread = electionThread.get();
        Thread initialRoleThread = publicationThread.get();
        owner.set("master-1#peer");
        remove("");
        assertEquals(initialThread, electionThread.get());
        assertEquals(initialRoleThread, publicationThread.get());
        assertEquals(initialThread, releaseThread.get());
    }

    @Test
    void testCallbacksReturnWhileListenerIsBlocked() throws Exception {
        server.start();
        CountDownLatch stopEntered = new CountDownLatch(1);
        CountDownLatch releaseStop = new CountDownLatch(1);
        doAnswer(invocation -> {
            stopEntered.countDown();
            await(releaseStop);
            return null;
        }).when(statusListener).changeToStandBy();
        owner.set("master-1#peer");
        notifyRemove("");
        try {
            assertTrue(stopEntered.await(5, TimeUnit.SECONDS));
            owner.set(null);
            notifyRemove("");
            verify(statusListener, times(1)).changeToActive();
        } finally {
            releaseStop.countDown();
        }
        drain();
        assertTrue(server.isActive());
        verify(statusListener, times(2)).changeToActive();
    }

    @Test
    void testListenerFailureDoesNotStrandStartupOrLaterEvents() throws Exception {
        doAnswer(invocation -> {
            throw new IllegalStateException("listener failure");
        }).when(statusListener).changeToActive();
        server.start();
        assertTrue(server.isActive());
        owner.set("master-1#peer");
        remove("");
        assertFalse(server.isActive());
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testSynchronousElectionFromListenerDoesNotWaitForItself() {
        doAnswer(invocation -> {
            assertTrue(server.participateElection());
            return null;
        }).when(statusListener).changeToActive();
        server.start();
        assertTrue(server.isActive());
    }

    @Test
    void testCloseCancelsInitialDelayedRetry() throws Exception {
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        Future<?> startup = newCaller().submit(server::start);
        awaitCondition(() -> org.mockito.Mockito.mockingDetails(registry).getInvocations().stream()
                .anyMatch(invocation -> invocation.getMethod().getName().equals("exists")));
        drain();
        assertFalse(startup.isDone());
        server.close();
        startup.get(5, TimeUnit.SECONDS);
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(electionExecutor.getQueue().isEmpty());
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testCloseCancelsOwnRemovalRecovery() throws Exception {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove(owner.get());
        assertFalse(electionExecutor.getQueue().isEmpty());
        server.close();
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        assertTrue(electionExecutor.getQueue().isEmpty());
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testCloseRejectsLateResultAndLateNotification() throws Exception {
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch releaseAcquire = new CountDownLatch(1);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            acquired.countDown();
            await(releaseAcquire);
            return true;
        });
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(acquired.await(5, TimeUnit.SECONDS));
            notifyRemove("");
            server.close();
            notifyRemove("");
        } finally {
            releaseAcquire.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        verify(statusListener, never()).changeToActive();
        verify(registry, times(1)).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testCloseDoesNotStrandQueuedStartup() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch releaseWorker = new CountDownLatch(1);
        electionExecutor.execute(() -> {
            entered.countDown();
            await(releaseWorker);
        });
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            awaitCondition(() -> !electionExecutor.getQueue().isEmpty());
            server.close();
        } finally {
            releaseWorker.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        assertFalse(server.isActive());
        verify(registry, never()).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testCloseBeforeInitialSubmissionStillRejects() {
        doAnswer(invocation -> {
            server.close();
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
        assertThrows(RejectedExecutionException.class, server::start);
        verify(registry, never()).acquireLock(ELECTION_LOCK);
    }

    @Test
    void testCloseFromInitialListenerCompletesReceiptAfterCallback() throws Exception {
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch releaseListener = new CountDownLatch(1);
        doAnswer(invocation -> {
            server.close();
            closed.countDown();
            await(releaseListener);
            return null;
        }).when(statusListener).changeToActive();
        AbstractServerStatusChangeListener later = mock(AbstractServerStatusChangeListener.class);
        server.addServerStatusChangeListener(later);
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(closed.await(5, TimeUnit.SECONDS));
            assertFalse(startup.isDone(), "self-close must let the admitted startup callback finish");
            assertFalse(server.isActive());
            assertEquals(HAServer.ServerStatus.ACTIVE, server.getServerStatus());
        } finally {
            releaseListener.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        org.mockito.Mockito.verifyNoInteractions(later);
    }

    @Test
    void testCloseFromDemotionListenerDoesNotDeadlock() throws Exception {
        server.start();
        CountDownLatch closed = new CountDownLatch(1);
        doAnswer(invocation -> {
            // Alert shuts down its own HA server from this callback.
            server.close();
            notifyRemove("");
            closed.countDown();
            return null;
        }).when(statusListener).changeToStandBy();
        AbstractServerStatusChangeListener later = mock(AbstractServerStatusChangeListener.class);
        server.addServerStatusChangeListener(later);
        owner.set("master-1#peer");
        notifyRemove("");
        assertTrue(closed.await(5, TimeUnit.SECONDS));
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        org.mockito.Mockito.verifyNoInteractions(later);
    }

    @Test
    void testExternalCloseWaitsForAdmittedListener() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch releaseListener = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            await(releaseListener);
            return null;
        }).when(statusListener).changeToActive();
        AbstractServerStatusChangeListener later = mock(AbstractServerStatusChangeListener.class);
        server.addServerStatusChangeListener(later);
        Future<?> startup = newCaller().submit(server::start);
        Thread closer = new Thread(server::close, "test-ha-close");
        closer.setDaemon(true);
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            closer.start();
            awaitCondition(() -> closer.getState() == Thread.State.BLOCKED);
            assertFalse(startup.isDone(), "external close cannot complete an in-flight callback early");
        } finally {
            releaseListener.countDown();
        }
        closer.join(5000);
        assertFalse(closer.isAlive());
        startup.get(5, TimeUnit.SECONDS);
        assertFalse(server.isActive());
        org.mockito.Mockito.verifyNoInteractions(later);
    }

    @Test
    void testClosedServerRetainsLastRoleButIsNotActive() {
        server.start();
        server.close();
        assertEquals(HAServer.ServerStatus.ACTIVE, server.getServerStatus());
        assertFalse(server.isActive());
    }

    @Test
    void testInterruptedStartupClosesAndRestoresInterrupt() throws Exception {
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                server.start();
            } catch (Throwable e) {
                failure.set(e);
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "test-ha-interrupted-start");
        caller.setDaemon(true);
        caller.start();
        awaitCondition(() -> org.mockito.Mockito.mockingDetails(registry).getInvocations().stream()
                .anyMatch(invocation -> invocation.getMethod().getName().equals("exists")));
        drain();
        caller.interrupt();
        caller.join(5000);
        assertFalse(caller.isAlive());
        assertTrue(failure.get() instanceof IllegalStateException);
        assertTrue(interrupted.get());
        assertFalse(server.isActive());
    }

    @Test
    void testOwnRemovalFailureSharesPendingRecovery() throws Exception {
        CountDownLatch resultReady = new CountDownLatch(1);
        CountDownLatch releaseResult = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                resultReady.countDown();
                await(releaseResult);
            }
            return null;
        }).when(registry).releaseLock(ELECTION_LOCK);
        Future<?> startup = newCaller().submit(server::start);
        try {
            assertTrue(resultReady.await(5, TimeUnit.SECONDS));
            notifyRemove(owner.get());
            when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        } finally {
            releaseResult.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        drain();
        assertFalse(server.isActive());
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        advanceRetry();
        assertTrue(server.isActive());
        // A successful recovery has handled that own REMOVE. A leftover retry must not demote it again.
        doAnswer(invocation -> {
            throw new RegistryException("later read failure without a new REMOVE");
        }).when(registry).exists(SELECTOR_PATH);
        if (!electionExecutor.getQueue().isEmpty()) {
            advanceRetry();
        }
        assertTrue(server.isActive(), "a covered own REMOVE must not cause a new demotion");
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testMasterOwnRemovalDemotesWhileEarlierRegistryCheckIsBlocked() throws Exception {
        server.close();
        server = newServer(ADDRESS, true);
        electionExecutor = electionExecutors.get(electionExecutors.size() - 1);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        String identity = owner.get();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            stopped.countDown();
            return null;
        }).when(statusListener).changeToStandBy();
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                entered.countDown();
                await(release);
                return identity;
            }
            return owner.get();
        });
        notifyRemove("previous-peer");
        try {
            await(entered);
            owner.set("master-1#peer");
            notifyRemove(identity);
            await(stopped);
            assertFalse(server.isActive(), "Master withdrawal must not wait for the blocked Registry call");
            // A second logical request must not enter the Registry while the first is still running.
            verify(registry, times(2)).acquireLock(ELECTION_LOCK);
        } finally {
            release.countDown();
        }
        drain();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testMasterDemotionListenerCanCloseWithoutStartingAnotherCheck() throws Exception {
        server.close();
        server = newServer(ADDRESS, true);
        electionExecutor = electionExecutors.get(electionExecutors.size() - 1);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        assertTrue(server.isActive());
        doAnswer(invocation -> {
            // The eager demotion callback runs before its OWN request is queued for Registry I/O.
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();

        notifyRemove(owner.get());
        assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
        ExecutorService io = (ExecutorService) field(server, "registryExecutor");
        assertTrue(io.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        verify(statusListener, times(1)).changeToActive();
        verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        assertTrue(((java.util.Deque<?>) field(server, "pendingElections")).isEmpty());
    }

    @Test
    void testCoveredFiniteOwnRetryCannotDemoteAfterHealthyWatch() throws Exception {
        server.start();
        String identity = owner.get();
        owner.set("master-1#peer");
        remove("");
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove(identity);
        Runnable oldRetry = electionExecutor.getQueue().peek();
        assertTrue(oldRetry instanceof ScheduledFuture);
        owner.set(identity);
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        remove("");
        assertTrue(server.isActive());
        doAnswer(invocation -> {
            throw new RegistryException("old OWN must not read again");
        }).when(registry).exists(SELECTOR_PATH);
        assertTrue(electionExecutor.remove(oldRetry));
        electionExecutor.execute(oldRetry);
        drain();
        assertTrue(server.isActive());
        verify(statusListener, times(2)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testHealthyWatchCompletesStartupBeforeOldInitialRetry() throws Exception {
        AtomicBoolean fail = new AtomicBoolean(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (fail.get()) {
                throw new RegistryException("unavailable");
            }
            return owner.get() != null;
        });
        Future<?> startup = newCaller().submit(server::start);
        awaitCondition(() -> !electionExecutor.getQueue().isEmpty());
        drain();
        Runnable initialRetry = electionExecutor.getQueue().peek();
        assertFalse(startup.isDone());
        fail.set(false);
        remove("");
        startup.get(5, TimeUnit.SECONDS);
        fail.set(true);
        assertTrue(electionExecutor.remove(initialRetry));
        electionExecutor.execute(initialRetry);
        drain();
        assertTrue(server.isActive());
        assertFalse(electionExecutor.isShutdown());
        verify(registry, times(2)).exists(SELECTOR_PATH);
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testOwnResponsibilitySurvivesAbortedCheck() throws Exception {
        server.start();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                acquired.countDown();
                await(release);
            }
            return true;
        });
        notifyRemove(owner.get());
        try {
            await(acquired);
            // A later OTHER aborts OWN before its read. Its failure still carries the unresolved OWN fact.
            notifyRemove("previous-peer");
            when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        } finally {
            release.countDown();
        }
        drain();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        advanceRetry();
        assertTrue(server.isActive());
        verify(statusListener, times(2)).changeToActive();
    }

    @Test
    void testLaterOwnDoesNotEraseFailedCheckOwnSnapshot() throws Exception {
        server.start();
        String identity = owner.get();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                entered.countDown();
                await(release);
            }
            throw new RegistryException("unavailable");
        });
        notifyRemove(identity);
        try {
            await(entered);
            // The new OWN must not erase the earlier OWN already included in the running check.
            notifyRemove(identity);
        } finally {
            release.countDown();
        }
        drain();
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToStandBy();
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        advanceRetry();
        assertTrue(server.isActive());
    }

    @Test
    void testRemoveDuringStartupListenerDoesNotReviveInitialRetry() throws Exception {
        AtomicBoolean fail = new AtomicBoolean(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (fail.get()) {
                throw new RegistryException("unavailable");
            }
            return owner.get() != null;
        });
        Future<?> startup = newCaller().submit(server::start);
        awaitCondition(() -> !electionExecutor.getQueue().isEmpty());
        drain();
        Runnable initialRetry = electionExecutor.getQueue().peek();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            await(release);
            return null;
        }).when(statusListener).changeToActive();
        fail.set(false);
        notifyRemove("previous-peer");
        try {
            await(entered);
            notifyRemove("another-peer");
            fail.set(true);
        } finally {
            release.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        drain();
        assertTrue(electionExecutor.remove(initialRetry));
        electionExecutor.execute(initialRetry);
        drain();
        assertTrue(server.isActive());
        assertFalse(electionExecutor.isShutdown());
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testCoveredPendingRecoveryCannotReadAgain() throws Exception {
        server.start();
        when(registry.exists(SELECTOR_PATH)).thenThrow(new RegistryException("unavailable"));
        remove(owner.get());
        assertFalse(server.isActive());
        Runnable recovery = electionExecutor.getQueue().peek();
        doReturn(true).when(registry).exists(SELECTOR_PATH);
        CountDownLatch resultReady = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                resultReady.countDown();
                await(release);
            }
            return true;
        }).when(registry).releaseLock(ELECTION_LOCK);
        notifyRemove("previous-peer");
        try {
            await(resultReady);
            // Recovery has left its timer and waits behind real I/O. A healthy result must cancel that responsibility
            // too.
            assertTrue(electionExecutor.remove(recovery));
            electionExecutor.execute(recovery);
            electionExecutor.submit(() -> {
            }).get(5, TimeUnit.SECONDS);
            verify(registry, times(3)).acquireLock(ELECTION_LOCK);
        } finally {
            release.countDown();
        }
        drain();
        assertTrue(server.isActive());
        verify(registry, times(3)).acquireLock(ELECTION_LOCK);
        verify(statusListener, times(2)).changeToActive();
        assertTrue(electionExecutor.getQueue().isEmpty());
    }

    @Test
    void testCloseDoesNotWaitForOrReplaceRunningRegistryCheck() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            entered.countDown();
            await(release);
            return true;
        });
        Future<?> startup = newCaller().submit(server::start);
        ExecutorService io = (ExecutorService) field(server, "registryExecutor");
        try {
            await(entered);
            notifyRemove("");
            electionExecutor.submit(() -> {
            }).get(5, TimeUnit.SECONDS);
            server.close();
            startup.get(5, TimeUnit.SECONDS);
            assertTrue(electionExecutor.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(io.isTerminated(), "closing the Future must not pretend the I/O exited");
            verify(registry, times(1)).acquireLock(ELECTION_LOCK);
        } finally {
            release.countDown();
        }
        assertTrue(io.awaitTermination(5, TimeUnit.SECONDS));
        verify(registry).releaseLock(ELECTION_LOCK);
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testSynchronousWatchCannotActivateBeforeSubscriptionFailure() throws Exception {
        doAnswer(invocation -> {
            SubscribeListener listener = invocation.getArgument(1);
            subscriber.set(listener);
            listener.notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "", Event.Type.REMOVE));
            electionExecutor.submit(() -> {
            }).get(5, TimeUnit.SECONDS);
            throw new RegistryException("subscription failed after its synchronous watch");
        }).when(registry).subscribe(eq(SELECTOR_PATH), org.mockito.ArgumentMatchers.any());
        assertThrows(RegistryException.class, server::start);
        verify(registry, never()).acquireLock(ELECTION_LOCK);
        verify(statusListener, never()).changeToActive();
    }

    private ExecutorService newCaller() {
        ExecutorService caller = Executors.newSingleThreadExecutor();
        callers.add(caller);
        return caller;
    }

    @Test
    void testCoveredQueuedOwnRemovalDoesNotDemote() throws Exception {
        server.start();
        String identity = owner.get();
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        electionExecutor.execute(() -> {
            blocked.countDown();
            await(release);
        });
        await(blocked);
        AtomicInteger checks = new AtomicInteger();
        try {
            owner.set(null);
            when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> {
                if (checks.incrementAndGet() == 1) {
                    return false;
                }
                throw new RegistryException("a covered notification must not read again");
            });
            // Both notifications arrive before the first check; that check covers both.
            notifyRemove("previous-peer");
            notifyRemove(identity);
        } finally {
            release.countDown();
        }
        drain();
        assertTrue(server.isActive(), "a covered OWN must not cause a second decision");
        assertEquals(1, checks.get());
        verify(statusListener, never()).changeToStandBy();
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
            ExecutorService io = (ExecutorService) field(server, "registryExecutor");
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!electionExecutor.isShutdown() && System.nanoTime() < deadline) {
                electionExecutor.submit(() -> {
                }).get(5, TimeUnit.SECONDS);
                io.submit(() -> {
                }).get(5, TimeUnit.SECONDS);
                if (electionExecutor.submit(() -> field(server, "inFlightCheck") == null)
                        .get(5, TimeUnit.SECONDS)) {
                    return;
                }
            }
            assertTrue(electionExecutor.isShutdown(), "HA workers did not finish their admitted checks");
        } catch (java.util.concurrent.CancellationException | RejectedExecutionException e) {
            assertTrue(electionExecutor.isShutdown());
        } catch (Exception e) {
            throw new AssertionError("HA workers did not finish", e);
        }
    }

    private Object field(AbstractHAServer target, String name) {
        try {
            Field field = AbstractHAServer.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private void advanceRetry() throws Exception {
        drain();
        // Move an existing scheduled task onto its actual worker; no sleeping or caller-thread execution.
        Runnable task = electionExecutor.getQueue().peek();
        assertTrue(task instanceof ScheduledFuture, "a delayed continuation must be pending");
        assertTrue(electionExecutor.remove(task));
        electionExecutor.execute(task);
        drain();
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void awaitCondition(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.yield();
        }
        assertTrue(condition.getAsBoolean());
    }

    private AbstractHAServer newServer() {
        return newServer(ADDRESS);
    }

    private AbstractHAServer newServer(String address) {
        return newServer(address, false);
    }

    private AbstractHAServer newServer(String address, boolean demoteOnOwnRemoval) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                runnable -> new Thread(runnable, "test-ha-election")) {

            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                // Tests advance existing delayed tasks explicitly, keeping production scheduling unchanged.
                return super.schedule(command, delay > 0 ? 1 : 0, delay > 0 ? TimeUnit.DAYS : unit);
            }
        };
        electionExecutors.add(executor);
        AbstractHAServer result = new AbstractHAServer(registry, SELECTOR_PATH, address, executor, demoteOnOwnRemoval) {
        };
        registryExecutors.add((ExecutorService) field(result, "registryExecutor"));
        servers.add(result);
        return result;
    }
}
