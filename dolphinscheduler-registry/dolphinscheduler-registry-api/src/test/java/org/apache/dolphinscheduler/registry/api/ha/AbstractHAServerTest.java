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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final List<ExecutorService> electionExecutors = new ArrayList<>();
    private final AtomicReference<Runnable> retryAction = new AtomicReference<>(() -> {
    });
    private ExecutorService electionExecutor;
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
        // Exception-driven demotion is deliberately outside this minimal candidate.
        assertTrue(server.isActive());
        verify(statusListener, never()).changeToStandBy();
        verify(registry, times(21)).releaseLock(ELECTION_LOCK);
    }

    @Test
    void testRemoveCannotBeOverwrittenByEarlierStartupElection() throws Exception {
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
            owner.set("master-1:5678#peer-instance");
            notifyRemove("");
            allowStartupToReturn.countDown();
            startup.get(5, TimeUnit.SECONDS);
            drain();
            assertFalse(server.isActive());
            verify(statusListener).changeToActive();
            verify(statusListener).changeToStandBy();
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
            ThreadPoolExecutor executor = (ThreadPoolExecutor) electionExecutor;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (executor.getQueue().isEmpty() && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertEquals(1, executor.getQueue().size());
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

    private AbstractHAServer newServer(String address) {
        ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), runnable -> {
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
                });
        electionExecutors.add(executor);
        AbstractHAServer result = new AbstractHAServer(registry, SELECTOR_PATH, address, executor) {
        };
        servers.add(result);
        return result;
    }
}
