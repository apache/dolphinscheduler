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
import org.apache.dolphinscheduler.eventbus.AbstractDelayEvent;
import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;

    private final String selectorPath;

    private final String serverIdentify;

    private volatile ServerStatus serverStatus;

    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    private final RoleChangeEventBus roleChangeEventBus = new RoleChangeEventBus();

    private final CompletableFuture<Void> initialElection = new CompletableFuture<>();

    private volatile boolean closed;

    private Thread roleChangeThread;

    private RuntimeException shutdownFailure;

    // Only accessed by the role-change thread, including rollback of partial activation.
    private boolean listenersActive;

    private static final long DEFAULT_RETRY_INTERVAL = 5_000;

    private static final int DEFAULT_MAX_RETRY_TIMES = 20;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this.registry = registry;
        this.selectorPath = checkNotNull(selectorPath);
        this.serverIdentify = checkNotNull(serverIdentify) + "#" + System.currentTimeMillis();
        this.serverStatus = ServerStatus.STAND_BY;
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        synchronized (this) {
            if (closed || roleChangeThread != null) {
                throw new IllegalStateException("HA server is already started or closed");
            }
            roleChangeThread = ThreadUtils.newDaemonThreadFactory("HA-RoleChange-%d")
                    .newThread(this::processRoleChanges);
            roleChangeThread.start();
        }
        try {
            registry.subscribe(selectorPath, new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    publishRoleChange(0);
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.PATH_ONLY;
                }
            });
            publishRoleChange(0);
            initialElection.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            close();
            throw new IllegalStateException("Interrupted while starting HA server", ex);
        } catch (ExecutionException | RuntimeException ex) {
            close();
            throw new IllegalStateException("Failed to start HA server", ex);
        }
    }

    private void publishRoleChange(long delay) {
        if (!closed) {
            roleChangeEventBus.publish(new RoleChangeEvent(delay));
        }
    }

    private void processRoleChanges() {
        int failures = 0;
        try {
            while (!closed) {
                roleChangeEventBus.take();
                if (closed) {
                    break;
                }
                try {
                    if (!ownsSelector()) {
                        // Stop the old run before an election can block on the Registry lock.
                        statusChange(ServerStatus.STAND_BY);
                        if (!registry.exists(selectorPath)) {
                            tryParticipateElection();
                        }
                    }
                    if (closed) {
                        break;
                    }
                    // Notifications only request reconciliation; their payload may already be stale.
                    boolean active = ownsSelector();
                    statusChange(active ? ServerStatus.ACTIVE : ServerStatus.STAND_BY);
                    failures = 0;
                    initialElection.complete(null);
                    if (!isActive() && !registry.exists(selectorPath)) {
                        publishRoleChange(DEFAULT_RETRY_INTERVAL);
                    }
                } catch (Exception ex) {
                    log.error("Reconcile HA role failed, will retry after {}ms", DEFAULT_RETRY_INTERVAL, ex);
                    try {
                        statusChange(ServerStatus.STAND_BY);
                    } catch (Exception stopException) {
                        log.error("Stop HA listeners failed", stopException);
                    }
                    if (++failures >= DEFAULT_MAX_RETRY_TIMES) {
                        initialElection.completeExceptionally(ex);
                    }
                    publishRoleChange(DEFAULT_RETRY_INTERVAL);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                statusChange(ServerStatus.STAND_BY);
            } catch (RuntimeException ex) {
                shutdownFailure = ex;
                log.error("Stop HA listeners during shutdown failed", ex);
            } finally {
                initialElection.completeExceptionally(new IllegalStateException("HA server is closed"));
            }
        }
    }

    @Override
    public boolean isActive() {
        return ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public boolean participateElection() {
        for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES && !closed; i++) {
            try {
                return tryParticipateElection();
            } catch (Exception ex) {
                log.error("Participate election error, will retry after {}ms", DEFAULT_RETRY_INTERVAL, ex);
                ThreadUtils.sleep(DEFAULT_RETRY_INTERVAL);
            }
        }
        throw new IllegalStateException("Participate election failed or HA server is closed");
    }

    private boolean tryParticipateElection() {
        final String electionLock = selectorPath + "-lock";
        if (!registry.acquireLock(electionLock)) {
            return false;
        }
        try {
            if (closed) {
                return false;
            }
            if (!registry.exists(selectorPath)) {
                registry.put(selectorPath, serverIdentify, true);
                return true;
            }
            return serverIdentify.equals(registry.get(selectorPath));
        } finally {
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

    private void statusChange(ServerStatus targetStatus) {
        if (targetStatus == ServerStatus.STAND_BY) {
            serverStatus = ServerStatus.STAND_BY;
            if (listenersActive) {
                notifyListeners(ServerStatus.ACTIVE, ServerStatus.STAND_BY);
                listenersActive = false;
            }
        } else if (serverStatus != ServerStatus.ACTIVE && !closed) {
            if (listenersActive) {
                statusChange(ServerStatus.STAND_BY);
            }
            listenersActive = true;
            notifyListeners(ServerStatus.STAND_BY, ServerStatus.ACTIVE);
            boolean owner = !closed && ownsSelector();
            synchronized (this) {
                if (!closed && owner) {
                    serverStatus = ServerStatus.ACTIVE;
                }
            }
            if (serverStatus != ServerStatus.ACTIVE) {
                statusChange(ServerStatus.STAND_BY);
            }
        }
    }

    private boolean ownsSelector() {
        return registry.exists(selectorPath) && serverIdentify.equals(registry.get(selectorPath));
    }

    private void notifyListeners(ServerStatus originStatus, ServerStatus targetStatus) {
        RuntimeException failure = null;
        for (ServerStatusChangeListener listener : serverStatusChangeListeners) {
            try {
                listener.change(originStatus, targetStatus);
            } catch (RuntimeException ex) {
                if (failure == null) {
                    failure = ex;
                } else if (failure != ex) {
                    failure.addSuppressed(ex);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void close() {
        final Thread thread;
        synchronized (this) {
            closed = true;
            initialElection.completeExceptionally(new IllegalStateException("HA server is closed"));
            thread = roleChangeThread;
        }
        // A listener (notably Alert) can close its own HA server during demotion.
        if (thread == null || thread == Thread.currentThread()) {
            return;
        }
        thread.interrupt();
        Uninterruptibles.joinUninterruptibly(thread);
        if (shutdownFailure != null) {
            throw new IllegalStateException("Failed to stop HA listeners", shutdownFailure);
        }
    }

    private static final class RoleChangeEvent extends AbstractDelayEvent {

        private RoleChangeEvent(long delay) {
            super(delay);
        }
    }

    private static final class RoleChangeEventBus extends AbstractDelayEventBus<RoleChangeEvent> {
    }
}
