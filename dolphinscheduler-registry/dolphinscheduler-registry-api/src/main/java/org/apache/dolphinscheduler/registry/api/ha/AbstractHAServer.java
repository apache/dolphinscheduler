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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;
    private final String selectorPath;
    private final String serverIdentify;
    private final boolean demoteOnOwnRemoval;
    private volatile ServerStatus serverStatus = ServerStatus.STAND_BY;
    private volatile LifecycleState lifecycle = LifecycleState.CREATED;
    private final ScheduledThreadPoolExecutor electionExecutor;
    private final ExecutorService registryExecutor;
    private final CompletableFuture<Void> startupResult = new CompletableFuture<>();
    private final Object electionStateLock = new Object();

    // Guarded by electionStateLock. REMOVE registration and ACTIVE admission share this lock.
    private long removalVersion;
    private long coveredRemovalVersion = -1;
    private long lastOwnRemovalVersion = -1;
    private ScheduledFuture<?> ownershipRecheck;
    // Remains present while recovery is scheduled, queued, or running in the Registry worker.
    private ElectionRequest ownershipRecovery;

    // Owned by the role worker. A completed check keeps its slot until its result is consumed.
    private final Deque<ElectionRequest> pendingElections = new ArrayDeque<>();
    private OwnershipCheck inFlightCheck;

    // Guarded by this monitor. A listener can close this server from the role worker.
    private boolean notifyingListeners;
    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    private static final long DEFAULT_RETRY_INTERVAL = 5_000;
    private static final int DEFAULT_MAX_RETRY_TIMES = 20;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this(registry, selectorPath, serverIdentify, false);
    }

    protected AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                               final boolean demoteOnOwnRemoval) {
        this(registry, selectorPath, serverIdentify,
                new ScheduledThreadPoolExecutor(1, ThreadUtils.newDaemonThreadFactory("HA-Election-%d")),
                demoteOnOwnRemoval);
    }

    @VisibleForTesting
    AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                     final ScheduledThreadPoolExecutor electionExecutor) {
        this(registry, selectorPath, serverIdentify, electionExecutor, false);
    }

    @VisibleForTesting
    AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                     final ScheduledThreadPoolExecutor electionExecutor, final boolean demoteOnOwnRemoval) {
        this.electionExecutor = electionExecutor;
        this.registryExecutor = Executors.newSingleThreadExecutor(ThreadUtils.newDaemonThreadFactory("HA-Registry-%d"));
        this.demoteOnOwnRemoval = demoteOnOwnRemoval;
        electionExecutor.setRemoveOnCancelPolicy(true);
        electionExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.registry = registry;
        this.selectorPath = checkNotNull(selectorPath);
        // Include the creation time to distinguish restarts at the same address.
        this.serverIdentify = checkNotNull(serverIdentify) + "#" + System.currentTimeMillis();
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        try {
            registry.subscribe(selectorPath, new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    if (Event.Type.REMOVE.equals(event.getType())) {
                        synchronized (electionStateLock) {
                            if (lifecycle == LifecycleState.CLOSED) {
                                return;
                            }
                            long version = ++removalVersion;
                            RoleChangeEventType type = RoleChangeEventType.OTHER_SELECTOR_REMOVED;
                            if (serverIdentify.equals(event.getEventData())) {
                                lastOwnRemovalVersion = version;
                                type = RoleChangeEventType.OWN_SELECTOR_REMOVED;
                            }
                            ElectionRequest request = new ElectionRequest(type, version);
                            electionExecutor.execute(() -> processRoleChange(request));
                        }
                    }
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.PATH_ONLY;
                }
            });
            synchronized (electionStateLock) {
                if (lifecycle == LifecycleState.CLOSED) {
                    throw new RejectedExecutionException("HA server was closed before startup was admitted");
                }
                ElectionRequest initial = new ElectionRequest(RoleChangeEventType.INITIAL_ELECTION, removalVersion);
                electionExecutor.execute(() -> processRoleChange(initial));
                lifecycle = LifecycleState.STARTED;
            }
            startupResult.get();
        } catch (InterruptedException e) {
            close();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting HA server", e);
        } catch (ExecutionException e) {
            close();
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalStateException("Failed to start HA server", e.getCause());
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    private void processRoleChange(ElectionRequest request) {
        try {
            if (isObsolete(request)) {
                return;
            }
            if (demoteOnOwnRemoval && request.type == RoleChangeEventType.OWN_SELECTOR_REMOVED && isActive()) {
                statusChange(ServerStatus.STAND_BY, request.version);
                scheduleOwnershipRecheck();
            }
            // A demotion listener may close this server before its request is queued.
            if (lifecycle == LifecycleState.STARTED) {
                pendingElections.addLast(request);
            }
            startNextCheck();
        } catch (Throwable e) {
            failRoleChange(e);
        } finally {
            if (lifecycle == LifecycleState.CLOSED) {
                startupResult.complete(null);
            }
        }
    }

    private boolean isObsolete(ElectionRequest request) {
        synchronized (electionStateLock) {
            return lifecycle != LifecycleState.STARTED || request.version <= coveredRemovalVersion
                    || (request.type == RoleChangeEventType.INITIAL_ELECTION && startupResult.isDone())
                    || (request.type == RoleChangeEventType.OWNERSHIP_RECHECK && request != ownershipRecovery);
        }
    }

    private void startNextCheck() {
        while (inFlightCheck == null && !pendingElections.isEmpty() && lifecycle == LifecycleState.STARTED) {
            ElectionRequest request = pendingElections.removeFirst();
            if (isObsolete(request)) {
                continue;
            }
            final OwnershipCheck check;
            synchronized (electionStateLock) {
                if (lifecycle != LifecycleState.STARTED) {
                    return;
                }
                check = new OwnershipCheck(request, removalVersion, lastOwnRemovalVersion);
                inFlightCheck = check;
            }
            try {
                // The whole lock scope, including finally release, stays on the same Registry thread.
                CompletableFuture.supplyAsync(() -> checkOwnership(check.version), registryExecutor)
                        .whenComplete((result, failure) -> {
                            synchronized (electionStateLock) {
                                if (lifecycle != LifecycleState.CLOSED) {
                                    electionExecutor.execute(() -> completeCheck(check, result, failure));
                                }
                            }
                        });
            } catch (RejectedExecutionException e) {
                inFlightCheck = null;
                if (lifecycle != LifecycleState.CLOSED) {
                    throw e;
                }
            }
        }
    }

    private void completeCheck(OwnershipCheck check, ElectionResult result, Throwable failure) {
        if (inFlightCheck != check) {
            return;
        }
        // A stale result must also release its slot; cancellation alone never releases a running check.
        inFlightCheck = null;
        try {
            if (isObsolete(check.request)) {
                return;
            }
            if (failure != null) {
                Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
                if (!(cause instanceof Exception)) {
                    failRoleChange(cause);
                    return;
                }
                retryRoleChange(check, cause);
                return;
            }
            boolean accepted = false;
            if (result != ElectionResult.ABORTED) {
                accepted = statusChange(result == ElectionResult.ELECTED ? ServerStatus.ACTIVE : ServerStatus.STAND_BY,
                        check.version);
            }
            if (accepted) {
                synchronized (electionStateLock) {
                    // A REMOVE during the listener is newer work, not a reason to undo this admitted decision.
                    coveredRemovalVersion = Math.max(coveredRemovalVersion, check.version);
                    if (removalVersion <= coveredRemovalVersion) {
                        cancelOwnershipRecheck();
                    } else {
                        scheduleOwnershipRecheck();
                    }
                }
                // Any admitted ownership decision may supersede the original startup retry.
                startupResult.complete(null);
            } else {
                scheduleOwnershipRecheck();
                if (check.request.type == RoleChangeEventType.INITIAL_ELECTION) {
                    // The logical startup hands unfinished reconciliation to the persistent recovery.
                    startupResult.complete(null);
                }
            }
        } catch (Throwable e) {
            failRoleChange(e);
        } finally {
            if (lifecycle == LifecycleState.CLOSED) {
                startupResult.complete(null);
            } else {
                startNextCheck();
            }
        }
    }

    private void retryRoleChange(OwnershipCheck check, Throwable cause) {
        ElectionRequest request = check.request;
        final boolean unresolvedOwn;
        synchronized (electionStateLock) {
            unresolvedOwn = coveredRemovalVersion < check.ownVersion;
        }
        if (unresolvedOwn && isActive()) {
            // Retain OWN responsibility even when an aborted OWN check handed work to RECHECK.
            statusChange(ServerStatus.STAND_BY, check.version);
            scheduleOwnershipRecheck();
        } else if (request.type == RoleChangeEventType.OWNERSHIP_RECHECK) {
            scheduleOwnershipRecheck();
        } else {
            synchronized (electionStateLock) {
                if (request.type == RoleChangeEventType.OWN_SELECTOR_REMOVED && ownershipRecovery != null) {
                    return;
                }
            }
            if (++request.failedAttempts >= DEFAULT_MAX_RETRY_TIMES) {
                IllegalStateException failure = new IllegalStateException("Could not complete HA election after "
                        + DEFAULT_MAX_RETRY_TIMES + " attempt(s)", cause);
                if (request.type == RoleChangeEventType.INITIAL_ELECTION) {
                    startupResult.completeExceptionally(failure);
                    close();
                } else {
                    log.error("Failed to reconcile HA ownership for {}", serverIdentify, failure);
                }
                return;
            }
            synchronized (electionStateLock) {
                if (lifecycle == LifecycleState.STARTED) {
                    electionExecutor.schedule(() -> processRoleChange(request), DEFAULT_RETRY_INTERVAL,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
        if (lifecycle == LifecycleState.STARTED) {
            log.warn("Failed to check HA ownership for {}, retrying in {}ms",
                    serverIdentify, DEFAULT_RETRY_INTERVAL, cause);
        }
    }

    private void scheduleOwnershipRecheck() {
        synchronized (electionStateLock) {
            if (lifecycle != LifecycleState.STARTED || removalVersion <= coveredRemovalVersion) {
                return;
            }
            if (ownershipRecovery == null || ownershipRecovery.version <= coveredRemovalVersion) {
                cancelOwnershipRecheck();
                ownershipRecovery = new ElectionRequest(RoleChangeEventType.OWNERSHIP_RECHECK, removalVersion);
            }
            ElectionRequest recovery = ownershipRecovery;
            if (ownershipRecheck == null && !pendingElections.contains(recovery)
                    && (inFlightCheck == null || inFlightCheck.request != recovery)) {
                ownershipRecheck = electionExecutor.schedule(() -> {
                    synchronized (electionStateLock) {
                        if (ownershipRecovery != recovery) {
                            return;
                        }
                        ownershipRecheck = null;
                    }
                    processRoleChange(recovery);
                }, DEFAULT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
            }
        }
    }

    // Called under electionStateLock. An old queued timer cannot clear a newer recovery's handle.
    private void cancelOwnershipRecheck() {
        if (ownershipRecheck != null) {
            ownershipRecheck.cancel(false);
            ownershipRecheck = null;
        }
        ownershipRecovery = null;
    }

    private void failRoleChange(Throwable cause) {
        log.error("Failed to process HA role change for {}", serverIdentify, cause);
        startupResult.completeExceptionally(cause);
        close();
    }

    @Override
    public void close() {
        synchronized (electionStateLock) {
            lifecycle = LifecycleState.CLOSED;
            electionExecutor.shutdown();
            registryExecutor.shutdown();
            cancelOwnershipRecheck();
        }
        synchronized (this) {
            // External close waits for admitted listeners. Self-close lets the current callback finish the receipt.
            if (!notifyingListeners) {
                startupResult.complete(null);
            }
        }
    }

    @Override
    public boolean isActive() {
        return lifecycle == LifecycleState.STARTED && ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public boolean participateElection() {
        // Preserve the synchronous API, including calls from a role listener; never wait on our own event queue.
        int failedAttempts = 0;
        while (lifecycle != LifecycleState.CLOSED) {
            final long checkedVersion;
            synchronized (electionStateLock) {
                checkedVersion = removalVersion;
            }
            try {
                return checkOwnership(checkedVersion) == ElectionResult.ELECTED;
            } catch (Exception e) {
                if (++failedAttempts == DEFAULT_MAX_RETRY_TIMES) {
                    throw new IllegalStateException("Could not complete HA election after " + DEFAULT_MAX_RETRY_TIMES
                            + " attempt(s)", e);
                }
                ThreadUtils.sleep(DEFAULT_RETRY_INTERVAL);
            }
        }
        return false;
    }

    private ElectionResult checkOwnership(long checkedVersion) {
        if (!canCheckOwnership(checkedVersion)) {
            return ElectionResult.ABORTED;
        }
        final String electionLock = selectorPath + "-lock";
        if (!registry.acquireLock(electionLock)) {
            throw new RegistryException("Could not acquire election lock " + electionLock);
        }
        try {
            if (!canCheckOwnership(checkedVersion)) {
                return ElectionResult.ABORTED;
            }
            if (registry.exists(selectorPath)) {
                return serverIdentify.equals(registry.get(selectorPath))
                        ? ElectionResult.ELECTED
                        : ElectionResult.NOT_ELECTED;
            }
            if (!canCheckOwnership(checkedVersion)) {
                return ElectionResult.NOT_ELECTED;
            }
            registry.put(selectorPath, serverIdentify, true);
            return ElectionResult.ELECTED;
        } finally {
            registry.releaseLock(electionLock);
        }
    }

    private boolean canCheckOwnership(long checkedVersion) {
        synchronized (electionStateLock) {
            return lifecycle != LifecycleState.CLOSED && checkedVersion == removalVersion;
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

    private synchronized boolean statusChange(ServerStatus targetStatus, long checkedVersion) {
        final ServerStatus originStatus;
        synchronized (electionStateLock) {
            if (lifecycle != LifecycleState.STARTED
                    || (targetStatus == ServerStatus.ACTIVE && checkedVersion != removalVersion)) {
                return false;
            }
            originStatus = serverStatus;
            serverStatus = targetStatus;
        }
        notifyingListeners = true;
        try {
            serverStatusChangeListeners.forEach(listener -> {
                if (lifecycle != LifecycleState.CLOSED) {
                    listener.change(originStatus, targetStatus);
                }
            });
        } catch (Exception ex) {
            log.error("Trigger ServerStatusChangeListener from {} -> {} error", originStatus, targetStatus, ex);
        } finally {
            notifyingListeners = false;
        }
        return true;
    }

    private enum LifecycleState {
        CREATED,
        STARTED,
        CLOSED
    }

    private enum RoleChangeEventType {
        INITIAL_ELECTION,
        OWN_SELECTOR_REMOVED,
        OTHER_SELECTOR_REMOVED,
        OWNERSHIP_RECHECK
    }

    private enum ElectionResult {
        ELECTED,
        NOT_ELECTED,
        ABORTED
    }

    private static final class ElectionRequest {

        private final RoleChangeEventType type;
        private final long version;
        private int failedAttempts;

        private ElectionRequest(RoleChangeEventType type, long version) {
            this.type = type;
            this.version = version;
        }
    }

    private static final class OwnershipCheck {

        private final ElectionRequest request;
        private final long version;
        private final long ownVersion;

        private OwnershipCheck(ElectionRequest request, long version, long ownVersion) {
            this.request = request;
            this.version = version;
            this.ownVersion = ownVersion;
        }
    }
}
