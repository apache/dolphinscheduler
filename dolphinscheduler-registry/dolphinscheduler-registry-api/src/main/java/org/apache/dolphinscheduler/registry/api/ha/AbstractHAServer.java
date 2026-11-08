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

import java.util.List;
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

    private volatile ServerStatus serverStatus;

    private final ScheduledThreadPoolExecutor electionExecutor;

    private final ExecutorService demotionExecutor;

    private final Object electionStateLock = new Object();

    // Guarded by electionStateLock. Only the election worker clears it before checking ownership.
    private boolean removeReceived;

    // Guarded by electionStateLock. Prevents promotion before the queued demotion completes.
    private boolean demotionPending;

    // Guarded by electionStateLock.
    private ScheduledFuture<?> promotionRecovery;

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
                     final ScheduledThreadPoolExecutor electionExecutor, final boolean demoteOnOwnRemoval) {
        this.electionExecutor = electionExecutor;
        demotionExecutor = demoteOnOwnRemoval
                ? Executors.newSingleThreadExecutor(ThreadUtils.newDaemonThreadFactory("HA-Demotion-%d"))
                : null;
        electionExecutor.setRemoveOnCancelPolicy(true);
        this.registry = registry;
        this.selectorPath = checkNotNull(selectorPath);
        // Include the creation time to distinguish restarts at the same address.
        this.serverIdentify = checkNotNull(serverIdentify) + "#" + System.currentTimeMillis();
        this.serverStatus = ServerStatus.STAND_BY;
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        try {
            registry.subscribe(selectorPath, new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    if (Event.Type.REMOVE.equals(event.getType())) {
                        enqueueElection(serverIdentify.equals(event.getEventData()));
                    }
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.PATH_ONLY;
                }
            });
            // Preserve startup completion and failure semantics; callbacks only enqueue work.
            electionExecutor.submit(() -> reconcileElection(false, DEFAULT_MAX_RETRY_TIMES)).get();
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

    private void enqueueElection(boolean ownSelectorRemoved) {
        synchronized (electionStateLock) {
            if (electionExecutor.isShutdown()) {
                return;
            }
            removeReceived = true;
            if (ownSelectorRemoved && demotionExecutor != null && serverStatus == ServerStatus.ACTIVE
                    && !demotionPending) {
                // Stop without waiting for an earlier Registry call or blocking the watch callback.
                demotionExecutor.execute(this::demoteAfterOwnRemoval);
                demotionPending = true;
            }
        }
        try {
            electionExecutor.execute(() -> {
                try {
                    reconcileElection(ownSelectorRemoved, DEFAULT_MAX_RETRY_TIMES);
                } catch (Exception e) {
                    log.error("Failed to reconcile HA ownership for {}", serverIdentify, e);
                }
            });
        } catch (RejectedExecutionException e) {
            // The Registry can deliver callbacks concurrently with close().
            if (!electionExecutor.isShutdown()) {
                throw e;
            }
        }
    }

    private synchronized void demoteAfterOwnRemoval() {
        try {
            if (isActive()) {
                statusChange(ServerStatus.STAND_BY, false);
            }
        } finally {
            synchronized (electionStateLock) {
                demotionPending = false;
                // A fresh result may have been deferred while the demotion was queued.
                schedulePromotionRecovery();
            }
        }
    }

    private void reconcileElection(boolean ownSelectorRemoved, int maxAttempts) {
        // A fresh ownership check accounts for REMOVE events already received.
        synchronized (electionStateLock) {
            if (demotionPending || electionExecutor.isShutdown()) {
                return;
            }
            removeReceived = false;
        }
        ElectionResult result = participateElection(ownSelectorRemoved, maxAttempts);
        if (result == ElectionResult.ABORTED) {
            synchronized (electionStateLock) {
                if (!demotionPending) {
                    schedulePromotionRecovery();
                }
            }
            return;
        }
        statusChange(result == ElectionResult.ELECTED ? ServerStatus.ACTIVE : ServerStatus.STAND_BY, true);
    }

    // Called under electionStateLock so close() can cancel the only pending recovery.
    private void schedulePromotionRecovery() {
        if (promotionRecovery == null && !electionExecutor.isShutdown()) {
            promotionRecovery = electionExecutor.schedule(this::recoverPromotion,
                    DEFAULT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private void recoverPromotion() {
        synchronized (electionStateLock) {
            promotionRecovery = null;
        }
        try {
            // One attempt per scheduled run leaves the worker free for watch callbacks between retries.
            reconcileElection(false, 1);
        } catch (Exception e) {
            log.warn("Failed to recheck deferred HA promotion for {}, retrying in {}ms",
                    serverIdentify, DEFAULT_RETRY_INTERVAL, e);
            synchronized (electionStateLock) {
                schedulePromotionRecovery();
            }
        }
    }

    private void cancelPromotionRecovery() {
        if (promotionRecovery != null) {
            promotionRecovery.cancel(false);
            promotionRecovery = null;
        }
    }

    @Override
    public void close() {
        synchronized (electionStateLock) {
            // Cancel delayed recovery, but let queued startup futures finish as no-ops.
            electionExecutor.shutdown();
            if (demotionExecutor != null) {
                demotionExecutor.shutdown();
            }
            cancelPromotionRecovery();
        }
        synchronized (this) {
            // Wait for any publication already in progress, without joining the event worker:
            // an Alert listener can close this server from that worker itself.
        }
    }

    @Override
    public boolean isActive() {
        return ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public boolean participateElection() {
        return participateElection(false, DEFAULT_MAX_RETRY_TIMES) == ElectionResult.ELECTED;
    }

    private ElectionResult participateElection(boolean ownSelectorRemoved, int maxAttempts) {
        final String electionLock = selectorPath + "-lock";
        int failedAttempts = 0;
        while (canParticipateElection()) {
            try {
                if (!registry.acquireLock(electionLock)) {
                    throw new RegistryException("Could not acquire election lock " + electionLock);
                }
                try {
                    if (!canParticipateElection()) {
                        return ElectionResult.ABORTED;
                    }
                    if (registry.exists(selectorPath)) {
                        if (serverIdentify.equals(registry.get(selectorPath))) {
                            return ElectionResult.ELECTED;
                        }
                        return ElectionResult.NOT_ELECTED;
                    }
                    // A missing selector confirms ownership loss even if a newer REMOVE prevents a claim.
                    if (!canParticipateElection()) {
                        return ElectionResult.NOT_ELECTED;
                    }
                    registry.put(selectorPath, serverIdentify, true);
                    return ElectionResult.ELECTED;
                } finally {
                    registry.releaseLock(electionLock);
                }
            } catch (Exception e) {
                if (ownSelectorRemoved && (demotionExecutor != null || isActive())) {
                    if (demotionExecutor == null && isActive()) {
                        statusChange(ServerStatus.STAND_BY, false);
                    }
                    synchronized (electionStateLock) {
                        // A delayed own REMOVE can leave our current selector in place.
                        schedulePromotionRecovery();
                    }
                }
                if (++failedAttempts == maxAttempts) {
                    throw new IllegalStateException("Could not complete HA election after " + maxAttempts
                            + " attempt(s)", e);
                }
                if (!electionExecutor.isShutdown()) {
                    log.warn("Failed to check HA ownership for {}, retrying in {}ms",
                            serverIdentify, DEFAULT_RETRY_INTERVAL, e);
                    ThreadUtils.sleep(DEFAULT_RETRY_INTERVAL);
                }
            }
        }
        return ElectionResult.ABORTED;
    }

    private boolean canParticipateElection() {
        synchronized (electionStateLock) {
            return !electionExecutor.isShutdown() && !removeReceived && !demotionPending;
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

    // Use the same monitor as external close() so it waits for ongoing status updates and listener calls.
    private synchronized void statusChange(ServerStatus targetStatus, boolean fromElection) {
        final ServerStatus originStatus;
        synchronized (electionStateLock) {
            if (electionExecutor.isShutdown()) {
                return;
            }
            // A later REMOVE invalidates a positive result, but cannot restore ownership already lost.
            if (fromElection && (demotionPending || (targetStatus == ServerStatus.ACTIVE && removeReceived))) {
                if (!demotionPending) {
                    // The demotion worker schedules its own recheck after the old workers exit.
                    schedulePromotionRecovery();
                }
                return;
            }
            originStatus = serverStatus;
            serverStatus = targetStatus;
            // Direct demotion keeps an existing recheck tracked until that task clears its own handle.
            if (fromElection) {
                if (removeReceived) {
                    schedulePromotionRecovery();
                } else {
                    cancelPromotionRecovery();
                }
            }
        }
        try {
            serverStatusChangeListeners.forEach(listener -> {
                // A listener may close this server; do not invoke subsequent listeners after that.
                if (!electionExecutor.isShutdown()) {
                    listener.change(originStatus, targetStatus);
                }
            });
        } catch (Exception ex) {
            log.error("Trigger ServerStatusChangeListener from {} -> {} error", originStatus, targetStatus, ex);
        }
    }

    // An aborted check contains no ownership decision to publish.
    private enum ElectionResult {
        ELECTED,
        NOT_ELECTED,
        ABORTED
    }
}
