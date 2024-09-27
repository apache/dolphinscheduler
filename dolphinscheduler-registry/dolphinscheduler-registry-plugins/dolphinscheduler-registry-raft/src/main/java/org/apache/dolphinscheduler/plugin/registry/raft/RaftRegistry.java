/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.registry.raft;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.raft.client.IRaftRegistryClient;
import org.apache.dolphinscheduler.plugin.registry.raft.client.RaftRegistryClient;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.time.Duration;
import java.util.Collection;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RaftRegistry implements Registry {

    private static final long RECONNECT_WAIT_TIME_MS = 50L;
    private final IRaftRegistryClient raftRegistryClient;
    public RaftRegistry(RaftRegistryProperties raftRegistryProperties) {
        this.raftRegistryClient = new RaftRegistryClient(raftRegistryProperties);
    }

    @Override
    public void start() {
        raftRegistryClient.start();
    }

    @Override
    public boolean isConnected() {
        return raftRegistryClient.isConnectivity();
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        try {
            long startTimeMillis = System.currentTimeMillis();
            long endTimeMillis = timeout.toMillis() > 0 ? startTimeMillis + timeout.toMillis() : Long.MAX_VALUE;

            while (System.currentTimeMillis() < endTimeMillis) {
                if (raftRegistryClient.isConnectivity()) {
                    return;
                }
            }
            ThreadUtils.sleep(RECONNECT_WAIT_TIME_MS);
        } catch (Exception ex) {
            throw new RegistryException("connect to raft cluster timeout", ex);
        }
    }

    @Override
    public void subscribe(String path, SubscribeListener listener) {
        checkNotNull(path);
        checkNotNull(listener);
        raftRegistryClient.subscribeRaftRegistryDataChange(path, listener);
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        checkNotNull(listener);
        raftRegistryClient.subscribeConnectionStateChange(listener);
    }

    @Override
    public String get(String key) {
        checkNotNull(key);
        return raftRegistryClient.getRegistryDataByKey(key);
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        checkNotNull(key);
        raftRegistryClient.putRegistryData(key, value, deleteOnDisconnect);
    }

    @Override
    public void delete(String key) {
        checkNotNull(key);
        raftRegistryClient.deleteRegistryDataByKey(key);
    }

    @Override
    public Collection<String> children(String key) {
        checkNotNull(key);
        return raftRegistryClient.getRegistryDataChildren(key);
    }

    @Override
    public boolean exists(String key) {
        checkNotNull(key);
        return raftRegistryClient.existRaftRegistryDataKey(key);
    }

    @Override
    public boolean acquireLock(String key) {
        checkNotNull(key);
        return raftRegistryClient.acquireRaftRegistryLock(key);
    }

    @Override
    public boolean acquireLock(String key, long timeout) {
        checkNotNull(key);
        return raftRegistryClient.acquireRaftRegistryLock(key, timeout);
    }

    @Override
    public boolean releaseLock(String key) {
        checkNotNull(key);
        return raftRegistryClient.releaseRaftRegistryLock(key);
    }

    @Override
    public void close() {
        raftRegistryClient.close();
    }

}
