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

import static com.alipay.sofa.jraft.util.BytesUtil.readUtf8;
import static com.alipay.sofa.jraft.util.BytesUtil.writeUtf8;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RocksDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.rhea.util.concurrent.DistributedLock;
import com.alipay.sofa.jraft.util.Endpoint;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "raft")
public class RaftRegistry implements Registry {

    private final Map<String, DistributedLock<byte[]>> distributedLockMap = new ConcurrentHashMap<>();

    private RheaKVStore kvStore;

    private RaftRegistryProperties properties;

    private EphemeralNodeManager ephemeralNodeManager;

    public RaftRegistry(RaftRegistryProperties properties) {
        this.properties = properties;
        //init RheaKVStore
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true) // use a fake pd
                .config();
        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs((int) properties.getElectionTimeout().toMillis());
        nodeOptions.setSnapshotIntervalSecs((int) properties.getSnapshotInterval().getSeconds());
        final StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.RocksDB)
                .withRocksDBOptions(RocksDBOptionsConfigured.newConfigured().withDbPath(properties.getDbStorageDir()).config())
                .withRaftDataPath(properties.getLogStorageDir())
                .withServerAddress(new Endpoint(properties.getServerAddress(), properties.getServerPort()))
                .withCommonNodeOptions(nodeOptions)
                .config();
        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured()
                .withClusterName(properties.getClusterName())
                .withUseParallelCompress(true)
                .withInitialServerList(properties.getServerAddressList())
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .config();
        this.kvStore = new DefaultRheaKVStore();
        this.kvStore.init(opts);
        log.info("kvStore started...");
        this.ephemeralNodeManager = new EphemeralNodeManager(properties, kvStore);
    }

    @PostConstruct
    public void start() {
        ephemeralNodeManager.start();
    }

    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        return ephemeralNodeManager.addSubscribeListener(path, listener);
    }

    @Override
    public void unsubscribe(String path) {
        ephemeralNodeManager.removeSubscribeListener(path);
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        ephemeralNodeManager.addConnectionListener(listener);
    }

    @Override
    public String get(String key) {
        return readUtf8(kvStore.bGet(key));
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        readUtf8(kvStore.bGetAndPut(key, writeUtf8(value)));
        ephemeralNodeManager.putHandler(key, value);
    }

    @Override
    public void delete(String key) {
        kvStore.bDelete(key);
        final DistributedLock<byte[]> distributedLock = distributedLockMap.get(key);
        if (distributedLock != null) {
            distributedLock.unlock();
        }
        distributedLockMap.remove(key);
        ephemeralNodeManager.deleteHandler(key);

    }

    @Override
    public Collection<String> children(String key) {
        final String result = readUtf8(kvStore.bGet(key));
        if (StringUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
        final List<String> children = JSONUtils.toList(result, String.class);
        children.sort(Comparator.reverseOrder());
        return children;
    }

    @Override
    public boolean exists(String key) {
        return kvStore.bContainsKey(key);
    }

    @Override
    public boolean acquireLock(String key) {
        final DistributedLock<byte[]> distributedLock = kvStore.getDistributedLock(key, properties.getDistributedLockTimeout().toMillis(), TimeUnit.MILLISECONDS);
        final boolean lock = distributedLock.tryLock();
        if (lock) {
            distributedLockMap.put(key, distributedLock);
        }
        return lock;
    }

    @Override
    public boolean releaseLock(String key) {
        final DistributedLock<byte[]> distributedLock = distributedLockMap.get(key);
        if (distributedLock != null) {
            distributedLock.unlock();
        }
        return true;
    }

    @Override
    public void close() {
        ephemeralNodeManager.close();
        kvStore.shutdown();
        log.info("Closed raft registry...");
    }

}
