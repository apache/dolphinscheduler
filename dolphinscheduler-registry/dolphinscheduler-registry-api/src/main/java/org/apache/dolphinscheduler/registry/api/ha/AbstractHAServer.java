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

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;

    private final String selectorPath;

    private final String serverIdentify;

    private volatile ServerStatus serverStatus;

    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    private final RoleChangeEventBusWorker roleChangeWorker;

    static final long DEFAULT_RETRY_INTERVAL = 5_000;

    static final int DEFAULT_MAX_RETRY_TIMES = 20;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this.registry = registry;
        this.selectorPath = checkNotNull(selectorPath);
        this.serverIdentify = checkNotNull(serverIdentify) + "#" + System.currentTimeMillis();
        this.serverStatus = ServerStatus.STAND_BY;
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
        this.roleChangeWorker = new RoleChangeEventBusWorker(this, new RoleChangeEventBus());
    }

    @Override
    public void start() {
        registry.subscribe(selectorPath, new SubscribeListener() {

            @Override
            public void notify(Event event) {
                roleChangeWorker.publish(0);
            }

            @Override
            public SubscribeScope getSubscribeScope() {
                return SubscribeScope.PATH_ONLY;
            }
        });
        roleChangeWorker.start();
    }

    /**
     * Reconcile one event and return whether an unclaimed selector needs a delayed retry.
     */
    boolean reconcileRole() {
        if (!ownsSelector()) {
            changeToStandBy();
            if (!registry.exists(selectorPath)) {
                tryParticipateElection();
            }
        }
        if (roleChangeWorker.isClosed()) {
            return false;
        }
        if (ownsSelector()) {
            if (serverStatus != ServerStatus.ACTIVE) {
                changeToActive();
            }
        } else {
            changeToStandBy();
        }
        return !isActive() && !registry.exists(selectorPath);
    }

    @Override
    public boolean isActive() {
        return ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public boolean participateElection() {
        for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES && !roleChangeWorker.isClosed(); i++) {
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
            if (roleChangeWorker.isClosed()) {
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

    private void changeToActive() {
        try {
            notifyListeners(ServerStatus.STAND_BY, ServerStatus.ACTIVE);
            boolean owner = !roleChangeWorker.isClosed() && ownsSelector();
            synchronized (roleChangeWorker) {
                if (!roleChangeWorker.isClosed() && owner) {
                    serverStatus = ServerStatus.ACTIVE;
                }
            }
        } finally {
            if (serverStatus != ServerStatus.ACTIVE) {
                notifyStandBy();
            }
        }
    }

    void changeToStandBy() {
        if (serverStatus == ServerStatus.ACTIVE) {
            serverStatus = ServerStatus.STAND_BY;
            notifyStandBy();
        }
    }

    private void notifyStandBy() {
        try {
            notifyListeners(ServerStatus.ACTIVE, ServerStatus.STAND_BY);
        } catch (RuntimeException ex) {
            // Failed cleanup must not be followed by another activation.
            roleChangeWorker.close();
            throw ex;
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
        roleChangeWorker.close();
    }
}
