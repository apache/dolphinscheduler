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
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;

    private final String selectorPath;

    private final String serverIdentify;

    private final String electionIdentity;

    private volatile ServerStatus serverStatus;

    private volatile boolean closed;

    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    private static final long DEFAULT_RETRY_INTERVAL = 5_000;

    private static final int DEFAULT_MAX_RETRY_TIMES = 20;

    public AbstractHAServer(final Registry registry, final String selectorPath, final String serverIdentify) {
        this.registry = registry;
        this.selectorPath = checkNotNull(selectorPath);
        this.serverIdentify = checkNotNull(serverIdentify);
        // An address can be reused while the previous process still owns an ephemeral node.
        this.electionIdentity = serverIdentify + "#" + UUID.randomUUID();
        this.serverStatus = ServerStatus.STAND_BY;
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        if (closed) {
            return;
        }
        registry.subscribe(selectorPath, new SubscribeListener() {

            @Override
            public void notify(Event event) {
                if (Event.Type.REMOVE.equals(event.getType())) {
                    reconcileElection();
                }
            }

            @Override
            public SubscribeScope getSubscribeScope() {
                return SubscribeScope.PATH_ONLY;
            }
        });

        reconcileElection();
    }

    private synchronized void reconcileElection() {
        if (closed) {
            return;
        }
        // Serialize election and publication with callbacks, including callbacks during startup.
        // REMOVE may be delayed or have no previous value, so consult current ownership instead.
        boolean elected = participateElection();
        // A demotion listener may close the entire server (for example, AlertServer).
        if (closed) {
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
    public boolean isActive() {
        return ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public synchronized boolean participateElection() {
        final String electionLock = selectorPath + "-lock";
        // If meet exception during participate election, will retry.
        // This can avoid the situation that the server is not elected as leader due to network jitter.
        for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES; i++) {
            if (closed) {
                return false;
            }
            boolean lockAcquired = false;
            try {
                try {
                    lockAcquired = registry.acquireLock(electionLock);
                    if (lockAcquired) {
                        if (closed) {
                            return false;
                        }
                        if (!registry.exists(selectorPath)) {
                            if (closed) {
                                return false;
                            }
                            registry.put(selectorPath, electionIdentity, true);
                            return true;
                        }
                        return electionIdentity.equals(registry.get(selectorPath));
                    }
                    return false;
                } finally {
                    if (lockAcquired) {
                        registry.releaseLock(electionLock);
                    }
                }
            } catch (Exception e) {
                // Do not keep coordinator services active while ownership cannot be verified.
                statusChange(ServerStatus.STAND_BY);
                if (closed) {
                    return false;
                }
                log.error("Participate election error, meet an exception, will retry after {}ms",
                        DEFAULT_RETRY_INTERVAL, e);
                ThreadUtils.sleep(DEFAULT_RETRY_INTERVAL);
            }
        }
        throw new IllegalStateException(
                "Participate election failed after retry " + DEFAULT_MAX_RETRY_TIMES + " times");
    }

    @Override
    public void close() {
        // Publish shutdown before waiting for an in-flight election to release the monitor.
        closed = true;
        synchronized (this) {
            // Consumers close their own services; notifying listeners here could recurse.
            serverStatus = ServerStatus.STAND_BY;
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

    private synchronized void statusChange(ServerStatus targetStatus) {
        if (closed) {
            return;
        }
        final ServerStatus originStatus = serverStatus;
        serverStatus = targetStatus;
        try {
            serverStatusChangeListeners.forEach(listener -> listener.change(originStatus, targetStatus));
        } catch (Exception ex) {
            log.error("Trigger ServerStatusChangeListener from {} -> {} error", originStatus, targetStatus, ex);
        }
    }
}
