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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private final List<ExecutorService> callers = new ArrayList<>();
    private final AtomicInteger checks = new AtomicInteger();
    private final AtomicReference<RuntimeException> electionFailure = new AtomicReference<>();
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
        server = newServer(ADDRESS);
        server.addServerStatusChangeListener(statusListener);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            checks.incrementAndGet();
            RuntimeException failure = electionFailure.get();
            if (failure != null) {
                throw failure;
            }
            return true;
        });
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> owner.get());
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(SELECTOR_PATH), anyString(), eq(true));
        doAnswer(invocation -> {
            subscriber.set(invocation.getArgument(1));
            return null;
        }).when(registry).subscribe(eq(SELECTOR_PATH), any());
    }

    @AfterEach
    void tearDown() throws Exception {
        servers.forEach(AbstractHAServer::close);
        callers.forEach(ExecutorService::shutdownNow);
        for (AbstractHAServer target : servers) {
            worker(target).join(5000);
            assertFalse(worker(target).isAlive());
            assertTrue(((ExecutorService) field(target, "registryWorker")).awaitTermination(5, TimeUnit.SECONDS));
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
        follower.start();
        assertFalse(follower.isActive());
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotAdoptPreviousProcessWithSameAddress() {
        // A legacy selector may survive its process until its lease/session expires.
        owner.set(ADDRESS);
        server.start();
        assertFalse(server.isActive());
        verify(statusListener, never()).changeToActive();
        verify(registry, never()).put(anyString(), anyString(), anyBoolean());
    }

    @Test
    void testDoesNotAdoptPredecessorWithEarlierTimestamp() {
        owner.set(ADDRESS + "#0");
        server.start();
        assertFalse(server.isActive());
        assertEquals(ADDRESS + "#0", owner.get());
    }

    @Test
    void testEmptyRemoveDemotesFormerLeaderWhenPeerOwnsSelector() {
        server.start();
        owner.set("master-1#peer");
        notifyRemove("");
        awaitIdle(2);
        assertFalse(server.isActive());
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testDelayedRemoveDoesNotRestartCurrentOwner() {
        server.start();
        notifyRemove(owner.get());
        awaitIdle(2);
        assertTrue(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testOwnRemovalCanReelectWithoutAnotherPeer() {
        server.start();
        String identity = owner.getAndSet(null);
        notifyRemove(identity);
        awaitIdle(2);
        assertTrue(server.isActive());
        assertEquals(identity, owner.get());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, never()).changeToStandBy();
    }

    @Test
    void testAddAndUpdateDoNotTriggerElection() {
        server.start();
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "peer", Event.Type.ADD));
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, "peer", Event.Type.UPDATE));
        assertEquals(1, checks.get());
    }

    @Test
    void testUnacquiredLockIsNotReleased() {
        when(registry.acquireLock(ELECTION_LOCK)).thenReturn(false);
        // Only the synchronous caller is mocked; worker retry timing is unchanged.
        try (MockedStatic<ThreadUtils> sleep = mockStatic(ThreadUtils.class)) {
            assertThrows(IllegalStateException.class, () -> server.participateElection());
        }
        verify(registry, times(20)).acquireLock(ELECTION_LOCK);
        verify(registry, never()).releaseLock(anyString());
    }

    @Test
    void testAcquisitionErrorDoesNotReleaseUnacquiredLock() {
        when(registry.acquireLock(ELECTION_LOCK)).thenThrow(new RegistryException("acquire failed"));
        try (MockedStatic<ThreadUtils> sleep = mockStatic(ThreadUtils.class)) {
            assertThrows(IllegalStateException.class, () -> server.participateElection());
        }
        verify(registry, never()).releaseLock(anyString());
    }

    @Test
    void testSubscriptionFailureReachesTheCaller() {
        doThrow(new RegistryException("subscription failed")).when(registry).subscribe(eq(SELECTOR_PATH), any());
        assertThrows(RegistryException.class, () -> server.start());
        assertFalse(worker(server).isAlive());
        verify(registry, never()).acquireLock(anyString());
    }

    @Test
    void testSynchronousWatchCannotActivateBeforeSubscriptionFailure() {
        doAnswer(invocation -> {
            ((SubscribeListener) invocation.getArgument(1)).notify(
                    new Event(SELECTOR_PATH, SELECTOR_PATH, "peer", Event.Type.REMOVE));
            throw new RegistryException("subscription failed after callback");
        }).when(registry).subscribe(eq(SELECTOR_PATH), any());
        assertThrows(RegistryException.class, () -> server.start());
        verify(statusListener, never()).changeToActive();
        verify(registry, never()).acquireLock(anyString());
    }

    @Test
    void testInitialFailureReachesCallerWithoutStrandingReceipt() {
        // Automatic retries must exhaust the finite budget before startup fails.
        electionFailure.set(new RegistryException("all attempts failed"));
        assertThrows(IllegalStateException.class, () -> server.start());
        assertEquals(20, checks.get());
        assertFalse(server.isActive());
    }

    @Test
    void testRemoveInvalidatesEarlierStartupPromotion() throws Exception {
        String identity = (String) field(server, "serverIdentify");
        owner.set(identity);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                entered.countDown();
                await(release);
                return identity;
            }
            return owner.get();
        });
        Future<?> startup = caller().submit(server::start);
        try {
            await(entered);
            owner.set("master-1#peer");
            notifyRemove(identity);
            assertFalse(startup.isDone());
        } finally {
            release.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        awaitIdle(2);
        assertFalse(server.isActive());
        // Final convergence alone would miss a short-lived stale ACTIVE and its Coordinator start.
        verify(statusListener, never()).changeToActive();
    }

    @Test
    void testOwnRecoverySurvivesPeerFailureDuringStartup() throws Exception {
        String identity = (String) field(server, "serverIdentify");
        owner.set(identity);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (first.getAndSet(false)) {
                entered.countDown();
                await(release);
                return identity;
            }
            return owner.get();
        });
        Future<?> startup = caller().submit(server::start);
        try {
            await(entered);
            notifyRemove(identity);
            electionFailure.set(new RegistryException("exhausted read retries"));
        } finally {
            release.countDown();
        }
        awaitCondition(() -> checks.get() >= 2);
        assertFalse(startup.isDone(), "the superseding own check must retain startup and recovery");
        notifyRemove("peer");
        awaitCondition(() -> checks.get() >= 3);
        assertFalse(startup.isDone(), "an ordinary failure must not discard the pending own recovery");
        electionFailure.set(null);
        startup.get(10, TimeUnit.SECONDS);
        verify(statusListener, timeout(5000).times(1)).changeToActive();
        assertTrue(server.isActive());
    }

    @Test
    void testConfirmedOwnershipLossSurvivesLaterRemovalAndReadFailure() throws Exception {
        server.start();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            entered.countDown();
            await(release);
            return "peer";
        });
        owner.set("peer");
        notifyRemove("previous-peer");
        try {
            await(entered);
            notifyRemove("another-peer");
            electionFailure.set(new RegistryException("later check exhausted retries"));
        } finally {
            release.countDown();
        }
        awaitIdle(3);
        assertFalse(server.isActive(), "a newer unread request must not erase confirmed ownership loss");
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testOrdinaryRemoveFailureDoesNotStrandStartup() throws Exception {
        String identity = (String) field(server, "serverIdentify");
        owner.set(identity);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            entered.countDown();
            await(release);
            return identity;
        });
        Future<?> startup = caller().submit(server::start);
        try {
            await(entered);
            notifyRemove("previous-peer");
            electionFailure.set(new RegistryException("replacement check exhausted retries"));
        } finally {
            release.countDown();
        }
        try {
            awaitIdle(2);
            assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> startup.get(1, TimeUnit.SECONDS),
                    "startup must fail when no pending check can finish it");
            verify(statusListener, never()).changeToActive();
        } finally {
            server.close();
        }
    }

    @Test
    void testAlertDemotesAfterFirstOwnCheckFailureBeforeRetryDelay() throws Exception {
        server = newServer(ADDRESS, false, 5000);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        CountDownLatch failed = new CountDownLatch(1);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (firstRead.getAndSet(false)) {
                failed.countDown();
                throw new RegistryException("first own-selector read failed");
            }
            return owner.get();
        });
        notifyRemove(owner.get());
        await(failed);
        // Alert cannot remain ACTIVE while the Registry worker sleeps through an entire retry batch.
        verify(statusListener, timeout(1000)).changeToStandBy();
        assertFalse(server.isActive());
    }

    @Test
    void testNewRemovalIsCheckedBeforeEarlierOrdinaryRetryDelay() throws Exception {
        server = newServer(ADDRESS, false, 5000);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        CountDownLatch failed = new CountDownLatch(1);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (firstRead.getAndSet(false)) {
                failed.countDown();
                throw new RegistryException("earlier ordinary read failed");
            }
            return owner.get();
        });
        notifyRemove("previous-peer");
        await(failed);
        notifyRemove("another-peer");
        // Fresh intent must not wait behind the earlier check's five-second backoff.
        verify(registry, timeout(1000).times(3)).acquireLock(ELECTION_LOCK);
        assertTrue(server.isActive());
    }

    @Test
    void testAlertOwnRemovalRetainsReconcileFirstPolicy() throws Exception {
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
            // Default Alert policy waits for a conclusive ownership check before withdrawing.
            assertFalse(stopped.await(250, TimeUnit.MILLISECONDS));
            assertTrue(server.isActive());
        } finally {
            release.countDown();
        }
        await(stopped);
        awaitIdle(3);
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testAlertDoesNotTreatEarlierOrdinaryFailureAsOwnRemovalCheck() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean firstRead = new AtomicBoolean(true);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (firstRead.getAndSet(false)) {
                entered.countDown();
                await(release);
                throw new RegistryException("earlier ordinary read failed");
            }
            return owner.get();
        });
        server.start();
        String identity = owner.get();
        notifyRemove("previous-peer");
        try {
            await(entered);
            // A late OWN arrives after this ordinary check started; the selector still belongs to us.
            notifyRemove(identity);
            awaitCondition(() -> {
                synchronized (worker(server)) {
                    return field(server, "pendingRequest") != null;
                }
            });
            assertTrue(server.isActive());
        } finally {
            release.countDown();
        }
        awaitIdle(3);
        assertTrue(server.isActive());
        // Real Alert closes on STAND_BY, so it must first get a check that actually covers this OWN.
        verify(statusListener, never()).changeToStandBy();
        verify(statusListener, times(1)).changeToActive();
    }

    @Test
    void testMasterDemotionListenerCanCloseBeforeAnotherRegistryCheck() throws Exception {
        server = newServer("master-policy", true);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        String identity = owner.get();
        doAnswer(invocation -> {
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();
        notifyRemove(identity);
        worker(server).join(5000);
        assertFalse(worker(server).isAlive());
        assertFalse(server.isActive());
        assertEquals(1, checks.get(), "a self-closing listener must not submit another Registry call");
    }

    @Test
    void testRemovalDuringStartupCallbackIsNotDiscarded() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            await(release);
            return null;
        }).when(statusListener).changeToActive();
        Future<?> startup = caller().submit(server::start);
        try {
            await(entered);
            owner.set("peer");
            notifyRemove("previous-peer");
            assertFalse(startup.isDone());
        } finally {
            release.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        awaitIdle(2);
        assertFalse(server.isActive());
        verify(statusListener, times(1)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testRegistryChecksAndRoleCallbacksUseSeparateWorkers() {
        AtomicReference<Thread> lockThread = new AtomicReference<>();
        doAnswer(invocation -> {
            lockThread.set(Thread.currentThread());
            return true;
        })
                .when(registry).acquireLock(ELECTION_LOCK);
        doAnswer(invocation -> {
            assertSame(lockThread.get(), Thread.currentThread());
            return null;
        })
                .when(registry).releaseLock(ELECTION_LOCK);
        doAnswer(invocation -> {
            assertSame(worker(server), Thread.currentThread());
            assertNotSame(lockThread.get(), Thread.currentThread());
            return null;
        })
                .when(statusListener).changeToActive();
        server.start();
        assertNotSame(worker(server), lockThread.get());
    }

    @Test
    void testListenerFailureDoesNotStrandStartupOrLaterEvents() {
        doThrow(new IllegalStateException("listener failed")).when(statusListener).changeToActive();
        server.start();
        assertTrue(server.isActive());
        owner.set("peer");
        notifyRemove("");
        awaitIdle(2);
        verify(statusListener).changeToStandBy();
    }

    @Test
    void testSynchronousElectionFromListenerDoesNotWaitForItself() {
        doAnswer(invocation -> {
            assertTrue(server.participateElection());
            return null;
        })
                .when(statusListener).changeToActive();
        server.start();
        assertTrue(server.isActive());
        verify(registry, times(2)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testOwnFailureDemotesAndRecoversWithoutNewRemove() {
        server = newServer(ADDRESS, false, 5000);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        String identity = owner.get();
        electionFailure.set(new RegistryException("exhausted read retries"));
        notifyRemove(identity);
        awaitCondition(() -> !server.isActive() && !bus(server).isEmpty());
        electionFailure.set(null);
        // Let the production five-second delayed event recover; do not inject another notification.
        awaitCondition(() -> server.isActive(), 10);
        verify(statusListener, timeout(5000).times(2)).changeToActive();
        verify(statusListener, times(1)).changeToStandBy();
    }

    @Test
    void testSuccessfulPeerEventCoversDelayedOwnRecovery() {
        server = newServer(ADDRESS, false, 5000);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        String identity = owner.get();
        electionFailure.set(new RegistryException("exhausted read retries"));
        notifyRemove(identity);
        awaitCondition(() -> !server.isActive() && !bus(server).isEmpty());
        electionFailure.set(null);
        notifyRemove("peer");
        awaitCondition(() -> server.isActive() && bus(server).isEmpty());
        verify(statusListener, timeout(5000).times(2)).changeToActive();
    }

    @Test
    void testOrdinaryRemoveRetainsFiniteRetryPolicy() {
        server.start();
        electionFailure.set(new RegistryException("exhausted read retries"));
        notifyRemove("peer");
        awaitIdle(21);
        assertEquals(21, checks.get(), "initial election plus twenty bounded ordinary attempts");
        assertTrue(server.isActive());
        assertTrue(bus(server).isEmpty());
        electionFailure.set(null);
        owner.set("peer");
        notifyRemove("peer");
        awaitIdle(3);
        assertFalse(server.isActive());
        assertTrue(worker(server).isAlive(), "a failed check must not kill the event worker");
    }

    @Test
    void testNewRemovalResetsBudgetAndOlderFailureDoesNotConsumeIt() throws Exception {
        server.start();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            if (reads.incrementAndGet() == 4) {
                entered.countDown();
                await(release);
            }
            throw new RegistryException("ownership read failed");
        });
        notifyRemove("previous-peer");
        try {
            await(entered);
            // This notification owns a fresh budget; the fourth old attempt is still in flight.
            notifyRemove("another-peer");
        } finally {
            release.countDown();
        }
        awaitIdle(25);
        assertEquals(25, checks.get(), "initial election, four older attempts and twenty fresh attempts");
        assertEquals(24, reads.get());
        assertTrue(server.isActive());
    }

    @Test
    void testCloseDoesNotInterruptInFlightRegistryCall() throws Exception {
        server.start();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> {
            entered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                interrupted.countDown();
                awaitUninterruptibly(release);
                Thread.currentThread().interrupt();
            }
            return owner.get();
        });
        notifyRemove("peer");
        try {
            await(entered);
            server.close();
            worker(server).join(5000);
            assertFalse(worker(server).isAlive());
            assertFalse(interrupted.await(250, TimeUnit.MILLISECONDS));
            assertFalse(((ExecutorService) field(server, "registryWorker")).isTerminated());
        } finally {
            release.countDown();
        }
        assertTrue(((ExecutorService) field(server, "registryWorker")).awaitTermination(5, TimeUnit.SECONDS));
        verify(registry, times(2)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testCloseCancelsOwnRemovalRecovery() throws Exception {
        server = newServer(ADDRESS, false, 5000);
        server.addServerStatusChangeListener(statusListener);
        server.start();
        electionFailure.set(new RegistryException("exhausted read retries"));
        notifyRemove(owner.get());
        awaitCondition(() -> !server.isActive() && !bus(server).isEmpty());
        server.close();
        worker(server).join(5000);
        assertFalse(worker(server).isAlive());
        assertTrue(bus(server).isEmpty());
    }

    @Test
    void testCloseRejectsLateResultAndLateNotification() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(registry.acquireLock(ELECTION_LOCK)).thenAnswer(invocation -> {
            entered.countDown();
            awaitUninterruptibly(release);
            return true;
        });
        Future<?> startup = caller().submit(server::start);
        try {
            await(entered);
            server.close();
            assertThrows(Exception.class, () -> startup.get(5, TimeUnit.SECONDS));
            notifyRemove("peer");
            worker(server).join(5000);
            assertTrue(bus(server).isEmpty());
            // The role worker can exit while the Registry implementation is still executing.
            assertFalse(((ExecutorService) field(server, "registryWorker")).isTerminated());
        } finally {
            release.countDown();
        }
        worker(server).join(5000);
        assertTrue(((ExecutorService) field(server, "registryWorker")).awaitTermination(5, TimeUnit.SECONDS));
        verify(statusListener, never()).changeToActive();
        verify(registry, times(1)).releaseLock(ELECTION_LOCK);
        verify(registry, never()).put(anyString(), anyString(), anyBoolean());
    }

    @Test
    void testCloseBeforeStartRejectsStartup() {
        server.close();
        assertThrows(IllegalStateException.class, () -> server.start());
    }

    @Test
    void testRepeatedStartDoesNotCloseRunningServer() {
        server.start();
        assertThrows(IllegalStateException.class, () -> server.start());
        assertTrue(server.isActive());
    }

    @Test
    void testCloseFromInitialListenerCompletesReceiptAfterCallback() throws Exception {
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            server.close();
            closed.countDown();
            awaitUninterruptibly(release);
            return null;
        }).when(statusListener).changeToActive();
        Future<?> startup = caller().submit(server::start);
        try {
            await(closed);
            assertFalse(startup.isDone(), "startup must not return before its admitted callback finishes");
        } finally {
            release.countDown();
        }
        startup.get(5, TimeUnit.SECONDS);
        assertFalse(server.isActive());
    }

    @Test
    void testCloseFromDemotionListenerDoesNotDeadlock() throws Exception {
        server.start();
        doAnswer(invocation -> {
            server.close();
            return null;
        }).when(statusListener).changeToStandBy();
        owner.set("peer");
        notifyRemove("peer");
        worker(server).join(5000);
        assertFalse(worker(server).isAlive());
        assertFalse(server.isActive());
    }

    @Test
    void testExternalCloseDoesNotInterruptAdmittedListener() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                interrupted.countDown();
                awaitUninterruptibly(release);
                Thread.currentThread().interrupt();
            }
            return null;
        }).when(statusListener).changeToActive();
        Future<?> startup = caller().submit(server::start);
        Future<?> closer = null;
        try {
            await(entered);
            closer = caller().submit(server::close);
            awaitCondition(() -> !server.isActive());
            assertFalse(interrupted.await(250, TimeUnit.MILLISECONDS));
            assertFalse(closer.isDone(), "external close still waits for the admitted callback");
        } finally {
            release.countDown();
        }
        closer.get(5, TimeUnit.SECONDS);
        startup.get(5, TimeUnit.SECONDS);
    }

    @Test
    void testExternalCloseWaitsForAdmittedListener() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            awaitUninterruptibly(release);
            return null;
        }).when(statusListener).changeToActive();
        Future<?> startup = caller().submit(server::start);
        Future<?> closer = null;
        try {
            await(entered);
            closer = caller().submit(server::close);
            awaitCondition(() -> !server.isActive());
            assertFalse(closer.isDone());
            assertFalse(startup.isDone());
        } finally {
            release.countDown();
        }
        closer.get(5, TimeUnit.SECONDS);
        startup.get(5, TimeUnit.SECONDS);
    }

    @Test
    void testRepeatedExternalCloseWaitsForSameAdmittedListener() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch secondCloseStarted = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        doAnswer(invocation -> {
            entered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                interrupted.set(true);
                awaitUninterruptibly(release);
                Thread.currentThread().interrupt();
            }
            return null;
        }).when(statusListener).changeToActive();
        Future<?> startup = caller().submit(server::start);
        Future<?> firstClose = null;
        Future<?> secondClose = null;
        try {
            await(entered);
            firstClose = caller().submit(server::close);
            awaitCondition(() -> !server.isActive());
            secondClose = caller().submit(() -> {
                secondCloseStarted.countDown();
                server.close();
            });
            await(secondCloseStarted);
            final Future<?> first = firstClose;
            final Future<?> second = secondClose;
            // Seeing CLOSED must not let either caller bypass the admitted listener's barrier.
            assertThrows(TimeoutException.class, () -> first.get(250, TimeUnit.MILLISECONDS));
            assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS));
            assertFalse(interrupted.get());
            assertFalse(startup.isDone());
        } finally {
            release.countDown();
        }
        firstClose.get(5, TimeUnit.SECONDS);
        secondClose.get(5, TimeUnit.SECONDS);
        startup.get(5, TimeUnit.SECONDS);
        assertFalse(interrupted.get());
        assertFalse(server.isActive());
    }

    @Test
    void testClosedServerRetainsLastRoleButIsNotActive() {
        server.start();
        server.close();
        assertEquals(HAServer.ServerStatus.ACTIVE, server.getServerStatus());
        assertFalse(server.isActive());
    }

    private AbstractHAServer newServer(String address) {
        return newServer(address, false);
    }

    private AbstractHAServer newServer(String address, boolean demoteOnOwnRemoval) {
        return newServer(address, demoteOnOwnRemoval, 10);
    }

    private AbstractHAServer newServer(String address, boolean demoteOnOwnRemoval, long retryIntervalMillis) {
        AbstractHAServer result = new AbstractHAServer(registry, SELECTOR_PATH, address,
                demoteOnOwnRemoval, retryIntervalMillis) {
        };
        servers.add(result);
        return result;
    }

    private ExecutorService caller() {
        ExecutorService result = Executors.newSingleThreadExecutor();
        callers.add(result);
        return result;
    }

    private void notifyRemove(String value) {
        subscriber.get().notify(new Event(SELECTOR_PATH, SELECTOR_PATH, value, Event.Type.REMOVE));
    }

    private Thread worker(AbstractHAServer target) {
        return (Thread) field(target, "roleChangeEventWorker");
    }

    private HAServerRoleChangeEventBus bus(AbstractHAServer target) {
        return (HAServerRoleChangeEventBus) field(target, "roleChangeEventBus");
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

    private void awaitIdle(int expectedChecks) {
        awaitCondition(() -> {
            synchronized (worker(server)) {
                // Read the queue first: take() can remove an expired event while this monitor is held.
                if (checks.get() < expectedChecks || field(server, "inFlightRequest") != null
                        || field(server, "pendingRequest") != null || !bus(server).isEmpty()) {
                    return false;
                }
                boolean waitingForEvent = Arrays.stream(worker(server).getStackTrace())
                        .anyMatch(frame -> frame.getClassName().equals(
                                "org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus")
                                && frame.getMethodName().equals("take"));
                Thread.State state = worker(server).getState();
                boolean waiting = state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
                return (waitingForEvent && waiting) || state == Thread.State.TERMINATED;
            }
        });
    }

    private void awaitCondition(BooleanSupplier condition) {
        awaitCondition(condition, 5);
    }

    private void awaitCondition(BooleanSupplier condition, int seconds) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.yield();
        }
        assertTrue(condition.getAsBoolean(), () -> "checks=" + checks.get() + ", busEmpty=" + bus(server).isEmpty()
                + ", inFlight=" + field(server, "inFlightRequest") + ", pending=" + field(server, "pendingRequest")
                + ", role=" + worker(server).getState() + ", stack=" + Arrays.toString(worker(server).getStackTrace()));
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (latch.getCount() != 0 && System.nanoTime() < deadline) {
            try {
                latch.await(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        assertEquals(0, latch.getCount());
    }
}
