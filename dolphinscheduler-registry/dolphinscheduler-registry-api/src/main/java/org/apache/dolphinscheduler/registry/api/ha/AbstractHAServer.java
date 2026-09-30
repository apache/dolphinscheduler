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
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;

    private final String selectorPath;

    private final String serverIdentify;

    private volatile ServerStatus serverStatus;

    private final ExecutorService electionExecutor;

    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    private static final long DEFAULT_RETRY_INTERVAL = 5_000;

    private static final int DEFAULT_MAX_RETRY_TIMES = 20;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this(registry, selectorPath, serverIdentify,
                ThreadUtils.newDaemonFixedThreadExecutor("HA-Election-%d", 1));
    }

    @VisibleForTesting
    AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify,
                     final ExecutorService electionExecutor) {
        this.electionExecutor = electionExecutor;
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
                        enqueueElection();
                    }
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.PATH_ONLY;
                }
            });
            // Preserve startup completion and failure semantics; callbacks only enqueue work.
            electionExecutor.submit(this::reconcileElection).get();
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

    private void enqueueElection() {
        if (electionExecutor.isShutdown()) {
            return;
        }
        try {
            electionExecutor.execute(() -> {
                try {
                    reconcileElection();
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

    private void reconcileElection() {
        // REMOVE is a request to recheck ownership, not a role decision to replay later.
        boolean elected = participateElection();
        if (electionExecutor.isShutdown()) {
            return;
        }
        if (elected) {
            statusChange(ServerStatus.ACTIVE);
        } else {
            statusChange(ServerStatus.STAND_BY);
            log.info("Server {} is standby", serverIdentify);
        }
    }

    @Override
    public void close() {
        // Let queued startup futures finish as no-ops. shutdownNow would strand their callers.
        electionExecutor.shutdown();
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
        final String electionLock = selectorPath + "-lock";
        // If meet exception during participate election, will retry.
        // This can avoid the situation that the server is not elected as leader due to network jitter.
        for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES; i++) {
            if (electionExecutor.isShutdown()) {
                return false;
            }
            try {
                if (!registry.acquireLock(electionLock)) {
                    return false;
                }
                try {
                    if (electionExecutor.isShutdown()) {
                        return false;
                    }
                    boolean selectorExists = registry.exists(selectorPath);
                    if (electionExecutor.isShutdown()) {
                        return false;
                    }
                    if (selectorExists) {
                        return serverIdentify.equals(registry.get(selectorPath));
                    }
                    registry.put(selectorPath, serverIdentify, true);
                    return true;
                } finally {
                    registry.releaseLock(electionLock);
                }
            } catch (Exception e) {
                log.error("Participate election error, meet an exception, will retry after {}ms",
                        DEFAULT_RETRY_INTERVAL, e);
                if (electionExecutor.isShutdown()) {
                    return false;
                }
                ThreadUtils.sleep(DEFAULT_RETRY_INTERVAL);
            }
        }
        throw new IllegalStateException(
                "Participate election failed after retry " + DEFAULT_MAX_RETRY_TIMES + " times");
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
    private synchronized void statusChange(ServerStatus targetStatus) {
        if (electionExecutor.isShutdown()) {
            return;
        }
        final ServerStatus originStatus = serverStatus;
        serverStatus = targetStatus;
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
}
