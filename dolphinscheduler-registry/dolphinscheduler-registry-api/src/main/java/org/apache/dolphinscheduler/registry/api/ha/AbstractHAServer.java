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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractHAServer implements HAServer {

    private final Registry registry;

    private final String serverPath;

    private ServerStatus serverStatus;

    private final List<ServerStatusChangeListener> serverStatusChangeListeners;

    public AbstractHAServer(Registry registry, String serverPath) {
        this.registry = registry;
        this.serverPath = serverPath;
        this.serverStatus = ServerStatus.STAND_BY;
        this.serverStatusChangeListeners = Lists.newArrayList(new DefaultServerStatusChangeListener());
    }

    @Override
    public void start() {
        registry.subscribe(serverPath, event -> {
            if (Event.Type.REMOVE.equals(event.type())) {
                if (isActive() && !participateElection()) {
                    statusChange(ServerStatus.STAND_BY);
                }
            }
        });
        ScheduledExecutorService electionSelectionThread =
                ThreadUtils.newSingleDaemonScheduledExecutorService("election-selection-thread");
        electionSelectionThread.schedule(() -> {
            if (isActive()) {
                return;
            }
            if (participateElection()) {
                statusChange(ServerStatus.ACTIVE);
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public boolean isActive() {
        return ServerStatus.ACTIVE.equals(getServerStatus());
    }

    @Override
    public boolean participateElection() {
        return registry.acquireLock(serverPath, 3_000);
    }

    @Override
    public void addServerStatusChangeListener(ServerStatusChangeListener listener) {
        serverStatusChangeListeners.add(listener);
    }

    @Override
    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    @Override
    public void shutdown() {
        if (isActive()) {
            registry.releaseLock(serverPath);
        }
    }

    private void statusChange(ServerStatus targetStatus) {
        synchronized (this) {
            ServerStatus originStatus = serverStatus;
            serverStatus = targetStatus;
            serverStatusChangeListeners.forEach(listener -> listener.change(originStatus, serverStatus));
        }
    }
}
