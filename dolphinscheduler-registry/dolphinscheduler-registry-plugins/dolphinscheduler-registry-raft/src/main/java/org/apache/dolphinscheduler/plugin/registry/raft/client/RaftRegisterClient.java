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

package org.apache.dolphinscheduler.plugin.registry.raft.client;

import static com.alipay.sofa.jraft.util.BytesUtil.readUtf8;
import static com.alipay.sofa.jraft.util.BytesUtil.writeUtf8;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.plugin.registry.raft.IRaftConnectionStateManager;
import org.apache.dolphinscheduler.plugin.registry.raft.IRaftLockManager;
import org.apache.dolphinscheduler.plugin.registry.raft.IRaftSubscribeDataManager;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftConnectionStateManager;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftLockManager;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftSubscribeDataManager;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;

@Slf4j
public class RaftRegisterClient implements IRaftRegisterClient {

    private final RheaKVStore rheaKvStore;
    private final RaftRegistryProperties raftRegistryProperties;
    private final IRaftConnectionStateManager raftConnectionStateManager;
    private final IRaftSubscribeDataManager raftSubscribeDataManager;
    private final IRaftLockManager raftLockManager;
    private volatile boolean started;
    private static final String MASTER_MODULE = "master";
    public RaftRegisterClient(RaftRegistryProperties raftRegistryProperties) {
        this.raftRegistryProperties = raftRegistryProperties;
        this.rheaKvStore = new DefaultRheaKVStore();
        this.raftConnectionStateManager = new RaftConnectionStateManager(raftRegistryProperties);
        this.raftSubscribeDataManager = new RaftSubscribeDataManager(raftRegistryProperties, rheaKvStore);
        this.raftLockManager = new RaftLockManager(rheaKvStore, raftRegistryProperties);

        initRheakv();
    }

    private void initRheakv() {
        final List<RegionRouteTableOptions> regionRouteTableOptionsList = MultiRegionRouteTableOptionsConfigured
                .newConfigured()
                .withInitialServerList(-1L /* default id */, raftRegistryProperties.getServerAddressList())
                .config();
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .withRegionRouteTableOptionsList(regionRouteTableOptionsList)
                .config();
        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured() //
                .withClusterName(raftRegistryProperties.getClusterName()) //
                .withPlacementDriverOptions(pdOpts) //
                .config();
        this.rheaKvStore.init(opts);
    }

    @Override
    public void start() {
        if (this.started) {
            log.info("RaftRegisterClient is already started");
            return;
        }
        if (raftRegistryProperties.getModule().equals(MASTER_MODULE)) {
            raftSubscribeDataManager.start();
        }
        raftConnectionStateManager.start();
        this.started = true;
    }

    @Override
    public boolean isConnectivity() {
        return raftConnectionStateManager.getConnectionState() == ConnectionState.CONNECTED;
    }

    @Override
    public void subscribeConnectionStateChange(ConnectionListener connectionStateListener) {
        raftConnectionStateManager.addConnectionListener(connectionStateListener);
    }

    @Override
    public void subscribeRaftRegistryDataChange(String path, SubscribeListener listener) {
        raftSubscribeDataManager.addDataSubscribeListener(path, listener);
    }

    @Override
    public String getRegistryDataByKey(String key) {
        String value = readUtf8(rheaKvStore.bGet(key));
        if (value == null) {
            throw new RegistryException("key does not exist");
        }
        return value;
    }

    @Override
    public void putRegistryData(String key, String value, boolean deleteOnDisconnect) {
        rheaKvStore.bPut(key, writeUtf8(value));
    }

    @Override
    public void deleteRegistryDataByKey(String key) {
        rheaKvStore.bDelete(key);
    }

    @Override
    public Collection<String> getRegistryDataChildren(String key) {
        String basePath = null;
        if (key.startsWith(RegistryNodeType.MASTER.getRegistryPath())) {
            basePath = RegistryNodeType.MASTER.getRegistryPath();
        } else if (key.startsWith(RegistryNodeType.WORKER.getRegistryPath())) {
            basePath = RegistryNodeType.WORKER.getRegistryPath();
        } else if (key.startsWith(RegistryNodeType.ALERT_SERVER.getRegistryPath())) {
            basePath = RegistryNodeType.ALERT_SERVER.getRegistryPath();
        } else {
            throw new UnsupportedOperationException("unsupported get registry data children by key:" + key);
        }
        List<KVEntry> kvEntries = rheaKvStore.bScan(basePath + Constants.SINGLE_SLASH,
                basePath + Constants.SINGLE_SLASH + Constants.RAFT_END_KEY);
        return getRegisterList(kvEntries);
    }

    @Override
    public boolean existRaftRegistryDataKey(String key) {
        return rheaKvStore.bContainsKey(key);
    }

    private Collection<String> getRegisterList(List<KVEntry> kvEntries) {
        if (kvEntries == null || kvEntries.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> registerList = new ArrayList<>();
        for (KVEntry kvEntry : kvEntries) {
            String entryKey = readUtf8(kvEntry.getKey());
            String childKey = entryKey.substring(entryKey.lastIndexOf(Constants.SINGLE_SLASH) + 1);
            registerList.add(childKey);
        }
        return registerList;
    }

    @Override
    public boolean acquireRaftRegistryLock(String lockKey) {
        try {
            return raftLockManager.acquireLock(lockKey);
        } catch (Exception ex) {
            log.error("acquire raft registry lock error", ex);
            raftLockManager.releaseLock(lockKey);
            throw new RegistryException("acquire raft register lock error: " + lockKey, ex);
        }
    }

    @Override
    public boolean acquireRaftRegistryLock(String lockKey, long timeout) {
        try {
            return raftLockManager.acquireLock(lockKey, timeout);
        } catch (Exception ex) {
            log.error("acquire raft registry lock error", ex);
            raftLockManager.releaseLock(lockKey);
            throw new RegistryException("acquire raft register lock error: " + lockKey, ex);
        }
    }

    @Override
    public boolean releaseRaftRegistryLock(String lockKey) {
        try {
            return raftLockManager.releaseLock(lockKey);
        } catch (Exception ex) {
            log.error("release raft registry lock error", ex);
            throw new RegistryException("release raft registry lock error, lockKey:" + lockKey, ex);
        }
    }

    @Override
    public void close() {
        log.info("start close raft register client");
        if (rheaKvStore != null) {
            rheaKvStore.shutdown();
        }
        this.started = false;
        log.info("closed raft register client");
    }
}
