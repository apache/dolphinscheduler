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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Serializes HA role changes while a separate worker performs blocking Registry checks.
 *
 * <p>The role worker owns role changes and listener callbacks. The Registry worker performs one
 * check and returns its result as an event. An unresolved own-selector removal remains represented
 * by an in-flight, pending or queued request until a current decision covers it or the server closes.
 *
 * <p>The role-worker monitor protects request admission, election state and ACTIVE admission.
 * The {@code this} monitor serializes admitted callbacks with external close. When both are needed,
 * acquire {@code this} before the role-worker monitor. Release the latter before Registry calls,
 * listener callbacks or waiting for workers to exit.
 *
 * <p>{@code STARTED} opens event admission; it does not mean {@link #start()} has returned.
 * Startup waits for an accepted ownership decision and any admitted callbacks to finish.
 * Closing can skip listeners that have not started. Register listeners before starting this server.
 * A listener must not wait for role-worker progress; calling the synchronous
 * {@link #participateElection()} from a listener also delays role handling.
 */
@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private static final long DEFAULT_RETRY_INTERVAL = 5_000;
    private static final int DEFAULT_MAX_RETRY_TIMES = 20;

    private final Registry registry;
    private final String selectorPath;
    private final String serverIdentify;
    private final boolean demoteOnOwnRemoval;
    private final long retryIntervalMillis;
    private final ExecutorService registryWorker = ThreadUtils.newDaemonFixedThreadExecutor("HA-Registry-%d", 1);
    private volatile ServerStatus serverStatus = ServerStatus.STAND_BY;
    private final List<ServerStatusChangeListener> serverStatusChangeListeners;
    private final HAServerRoleChangeEventBus roleChangeEventBus = new HAServerRoleChangeEventBus();
    private final RoleChangeEventWorker roleChangeEventWorker = new RoleChangeEventWorker();
    private final CompletableFuture<Void> startupResult = new CompletableFuture<>();
    private volatile LifecycleState lifecycle = LifecycleState.CREATED;
    // Guarded by roleChangeEventWorker, together with ACTIVE admission and close.
    private long removalVersion;
    private int ordinaryFailures;
    // A physical check keeps its slot until the role worker consumes its completion.
    private HAServerRoleChangeRequest inFlightRequest;
    private HAServerRoleChangeRequest pendingRequest;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this(registry, selectorPath, serverIdentify, false);
    }

    protected AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                               final boolean demoteOnOwnRemoval) {
        this(registry, selectorPath, serverIdentify, demoteOnOwnRemoval, DEFAULT_RETRY_INTERVAL);
    }

    @VisibleForTesting
    AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                     final boolean demoteOnOwnRemoval, final long retryIntervalMillis) {
        this.registry = registry;
        this.demoteOnOwnRemoval = demoteOnOwnRemoval;
        this.retryIntervalMillis = retryIntervalMillis;
        this.selectorPath = checkNotNull(selectorPath);
        // Include the creation time to distinguish restarts at the same address.
        this.serverIdentify = checkNotNull(serverIdentify) + "#" + System.currentTimeMillis();
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.CREATED) {
                throw new IllegalStateException("HA server has already been started or closed");
            }
            lifecycle = LifecycleState.STARTED;
        }
        try {
            registry.subscribe(selectorPath, new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    if (Event.Type.REMOVE.equals(event.getType())) {
                        synchronized (roleChangeEventWorker) {
                            if (lifecycle == LifecycleState.STARTED) {
                                // Register REMOVE before an earlier check can publish ACTIVE.
                                removalVersion++;
                                ordinaryFailures = 0;
                                roleChangeEventBus.publish(new HAServerRoleChangeRequest(removalVersion,
                                        serverIdentify.equals(event.getEventData()), 0));
                            }
                        }
                    }
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.PATH_ONLY;
                }
            });
            synchronized (roleChangeEventWorker) {
                if (lifecycle == LifecycleState.CLOSED) {
                    throw new IllegalStateException("HA server was closed while subscribing");
                }
                roleChangeEventBus.publish(new HAServerRoleChangeRequest(removalVersion, false, 0));
                roleChangeEventWorker.start();
            }
            // A newer REMOVE may supersede the first check; wait for a current decision and its callbacks.
            startupResult.get();
        } catch (InterruptedException e) {
            close();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting HA server", e);
        } catch (ExecutionException e) {
            close();
            throw new IllegalStateException("Failed to start HA server", e.getCause());
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    private void processRoleChange(HAServerRoleChangeEvent event) {
        if (event instanceof HAServerRoleChangeRequest) {
            HAServerRoleChangeRequest request = (HAServerRoleChangeRequest) event;
            synchronized (roleChangeEventWorker) {
                if (lifecycle != LifecycleState.STARTED || (!request.isOwnSelectorRemoved()
                        && request.getRemovalVersion() < removalVersion)) {
                    return;
                }
            }
            // Master stops on its own REMOVE without waiting for a blocked Registry call.
            // Alert retains its existing reconcile-first policy.
            if (demoteOnOwnRemoval && request.isOwnSelectorRemoved()) {
                statusChange(ServerStatus.STAND_BY, request.getRemovalVersion());
            }
            synchronized (roleChangeEventWorker) {
                if (lifecycle == LifecycleState.STARTED) {
                    boolean ownRemoval = request.isOwnSelectorRemoved()
                            || (pendingRequest != null && pendingRequest.isOwnSelectorRemoved());
                    pendingRequest = new HAServerRoleChangeRequest(removalVersion, ownRemoval, 0);
                }
            }
        } else {
            processElectionCompleted((HAServerElectionCompletedEvent) event);
        }
        submitPendingElection();
    }

    private void submitPendingElection() {
        final HAServerRoleChangeRequest request;
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.STARTED || inFlightRequest != null || pendingRequest == null) {
                return;
            }
            // Coalesce notifications, but keep own-removal recovery until a current check covers it.
            request = new HAServerRoleChangeRequest(removalVersion,
                    pendingRequest.isOwnSelectorRemoved() || roleChangeEventBus.hasOwnRemovalRequests(), 0);
            pendingRequest = null;
            inFlightRequest = request;
        }
        try {
            registryWorker.execute(() -> {
                HAServerElectionCompletedEvent completed;
                try {
                    // Return every attempt so an OWN failure can withdraw before retry backoff.
                    completed = new HAServerElectionCompletedEvent(request, checkOwnership(), null);
                } catch (Throwable failure) {
                    completed = new HAServerElectionCompletedEvent(request, false, failure);
                }
                synchronized (roleChangeEventWorker) {
                    if (lifecycle == LifecycleState.STARTED) {
                        roleChangeEventBus.publish(completed);
                    }
                }
            });
        } catch (RuntimeException e) {
            processElectionCompleted(new HAServerElectionCompletedEvent(request, false, e));
        }
    }

    private void processElectionCompleted(HAServerElectionCompletedEvent event) {
        HAServerRoleChangeRequest request = event.getRequest();
        final boolean ownRemoval;
        synchronized (roleChangeEventWorker) {
            if (request != inFlightRequest) {
                return;
            }
            inFlightRequest = null;
            if (lifecycle != LifecycleState.STARTED) {
                return;
            }
            ownRemoval = request.isOwnSelectorRemoved()
                    || (pendingRequest != null && pendingRequest.isOwnSelectorRemoved())
                    || roleChangeEventBus.hasOwnRemovalRequests();
        }
        Throwable failure = event.getFailure();
        if (failure != null) {
            processElectionFailure(request, ownRemoval, failure);
            return;
        }
        final boolean accepted;
        synchronized (this) {
            ServerStatus targetStatus = event.isElected() ? ServerStatus.ACTIVE : ServerStatus.STAND_BY;
            accepted = statusChange(targetStatus, request.getRemovalVersion());
            synchronized (roleChangeEventWorker) {
                if (accepted && request.getRemovalVersion() == removalVersion) {
                    // Keep the confirmed startup receipt behind the same listener barrier as external close.
                    startupResult.complete(null);
                }
            }
        }
        synchronized (roleChangeEventWorker) {
            // Never clear completion events or a REMOVE that arrived during the callbacks.
            if (accepted && request.getRemovalVersion() == removalVersion) {
                pendingRequest = null;
                ordinaryFailures = 0;
                roleChangeEventBus.discardRequestsThrough(request.getRemovalVersion());
                return;
            }
        }
        if (ownRemoval) {
            retryOwnRemoval();
        }
    }

    private void processElectionFailure(HAServerRoleChangeRequest request, boolean ownRemoval, Throwable failure) {
        if (!(failure instanceof Exception)) {
            startupResult.completeExceptionally(failure);
            log.error("Failed to check HA ownership for {}", serverIdentify, failure);
            close();
            return;
        }
        if (ownRemoval) {
            // Later OWN intent retains recovery, but does not reinterpret an earlier ordinary failure.
            if (request.isOwnSelectorRemoved()) {
                statusChange(ServerStatus.STAND_BY, request.getRemovalVersion());
            }
            retryOwnRemoval();
            log.warn("Failed to reconcile removed HA selector for {}, will retry", serverIdentify, failure);
            return;
        }
        final boolean failedStartup;
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.STARTED || request.getRemovalVersion() != removalVersion) {
                return;
            }
            // Fold duplicate requests into this retry, retaining its finite attempt budget.
            pendingRequest = null;
            roleChangeEventBus.discardRequestsThrough(request.getRemovalVersion());
            if (++ordinaryFailures < DEFAULT_MAX_RETRY_TIMES) {
                roleChangeEventBus.publish(new HAServerRoleChangeRequest(removalVersion, false, retryIntervalMillis));
                return;
            }
            failedStartup = !startupResult.isDone();
            if (failedStartup) {
                startupResult.completeExceptionally(failure);
            }
        }
        log.error("Could not reconcile HA ownership for {} after {} attempts",
                serverIdentify, DEFAULT_MAX_RETRY_TIMES, failure);
        if (failedStartup) {
            close();
        }
    }

    private void retryOwnRemoval() {
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.STARTED) {
                return;
            }
            if (pendingRequest != null) {
                pendingRequest = new HAServerRoleChangeRequest(removalVersion, true, 0);
            } else if (!roleChangeEventBus.hasOwnRemovalRequests()) {
                roleChangeEventBus.publish(new HAServerRoleChangeRequest(removalVersion, true, retryIntervalMillis));
            }
        }
    }

    private synchronized boolean statusChange(ServerStatus targetStatus, long checkedVersion) {
        final ServerStatus originStatus;
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.STARTED
                    || (targetStatus == ServerStatus.ACTIVE && checkedVersion != removalVersion)) {
                return false;
            }
            originStatus = serverStatus;
            serverStatus = targetStatus;
        }
        if (originStatus != targetStatus) {
            try {
                serverStatusChangeListeners.forEach(listener -> {
                    if (lifecycle != LifecycleState.CLOSED) {
                        listener.change(originStatus, targetStatus);
                    }
                });
            } catch (Exception e) {
                log.error("Trigger ServerStatusChangeListener from {} -> {} error", originStatus, targetStatus, e);
            }
        }
        return true;
    }

    @Override
    public void close() {
        synchronized (roleChangeEventWorker) {
            if (lifecycle != LifecycleState.CLOSED) {
                lifecycle = LifecycleState.CLOSED;
                roleChangeEventBus.clear();
                registryWorker.shutdown();
                if (roleChangeEventWorker.isAlive()) {
                    // Wake take() without interrupting an admitted listener or Registry lock scope.
                    roleChangeEventBus.publish(new HAServerClosedEvent());
                }
            }
        }
        synchronized (this) {
            // Wait for admitted callbacks, but never wait for the current listener to finish itself.
            if (Thread.currentThread() != roleChangeEventWorker) {
                startupResult.completeExceptionally(new IllegalStateException("HA server was closed during startup"));
            }
        }
    }

    @Override
    public boolean isActive() {
        return lifecycle == LifecycleState.STARTED && ServerStatus.ACTIVE.equals(serverStatus);
    }

    @Override
    public boolean participateElection() {
        for (int attempt = 0; attempt < DEFAULT_MAX_RETRY_TIMES; attempt++) {
            if (lifecycle == LifecycleState.CLOSED) {
                return false;
            }
            try {
                return checkOwnership();
            } catch (Exception e) {
                if (attempt + 1 == DEFAULT_MAX_RETRY_TIMES) {
                    throw new IllegalStateException("Could not complete HA election after "
                            + DEFAULT_MAX_RETRY_TIMES + " attempt(s)", e);
                }
                log.warn("Participate election failed for {}, will retry after {}ms",
                        serverIdentify, retryIntervalMillis, e);
                ThreadUtils.sleep(retryIntervalMillis);
            }
        }
        return false;
    }

    private boolean checkOwnership() {
        if (lifecycle == LifecycleState.CLOSED) {
            return false;
        }
        final String electionLock = selectorPath + "-lock";
        if (!registry.acquireLock(electionLock)) {
            throw new RegistryException("Could not acquire election lock " + electionLock);
        }
        try {
            if (lifecycle == LifecycleState.CLOSED) {
                return false;
            }
            if (registry.exists(selectorPath)) {
                return serverIdentify.equals(registry.get(selectorPath));
            }
            if (lifecycle == LifecycleState.CLOSED) {
                return false;
            }
            registry.put(selectorPath, serverIdentify, true);
            return true;
        } finally {
            // Keep acquire and release on this same worker (or synchronous API caller).
            registry.releaseLock(electionLock);
        }
    }

    @Override
    public void addServerStatusChangeListener(ServerStatusChangeListener listener) {
        serverStatusChangeListeners.add(listener);
    }

    @Override
    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    private enum LifecycleState {
        CREATED,
        STARTED,
        CLOSED
    }

    private final class RoleChangeEventWorker extends BaseDaemonThread {

        private RoleChangeEventWorker() {
            super("HA-RoleChangeEventWorker");
        }

        @Override
        public void run() {
            try {
                while (lifecycle == LifecycleState.STARTED) {
                    HAServerRoleChangeEvent event = roleChangeEventBus.take();
                    if (lifecycle == LifecycleState.CLOSED) {
                        break;
                    }
                    processRoleChange(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                startupResult.completeExceptionally(new IllegalStateException("HA role worker stopped during startup"));
                if (lifecycle != LifecycleState.CLOSED) {
                    close();
                }
                roleChangeEventBus.clear();
            }
        }
    }
}
