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

import org.apache.dolphinscheduler.common.Constants;
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.NodeManager;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.RpcOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.rhea.util.concurrent.DistributedLock;
import com.alipay.sofa.jraft.util.Endpoint;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "raft")
public class RaftRegistry implements Registry {

    private final Map<String, DistributedLock<byte[]>> distributedLockMap = new ConcurrentHashMap<>();

    private final RheaKVStore kvStore;

    private final RaftRegistryProperties properties;

    private SubscribeListenerManager subscribeListenerManager;

    private static final String REGISTRY_DOLPHINSCHEDULER_WORKER_GROUPS = "worker-groups";

    private static final String API_TYPE = "api";

    public RaftRegistry(RaftRegistryProperties properties) {
        this.properties = properties;
        //init RheaKVStore
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true) // use a fake pd
                .config();
        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs((int) properties.getElectionTimeout().toMillis());
        final Endpoint serverAddress = new Endpoint(properties.getServerAddress(), properties.getServerPort());
        final StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.Memory)
                .withRaftDataPath(properties.getLogStorageDir())
                .withServerAddress(serverAddress)
                .withCommonNodeOptions(nodeOptions)
                .withKvRpcCoreThreads(properties.getRpcCoreThreads())
                .config();
        RpcOptions rpcOptions = new RpcOptions();
        rpcOptions.setCallbackExecutorCorePoolSize(properties.getRpcCoreThreads());
        rpcOptions.setRpcTimeoutMillis((int) properties.getRpcTimeoutMillis().toMillis());
        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured()
                .withClusterName(properties.getClusterName())
                .withUseParallelCompress(true)
                .withInitialServerList(properties.getServerAddressList())
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .withRpcOptions(rpcOptions)
                .config();
        this.kvStore = new DefaultRheaKVStore();
        this.kvStore.init(opts);
        log.info("kvStore started...");
        if (!properties.getModule().equalsIgnoreCase(API_TYPE)) {
            this.subscribeListenerManager = new SubscribeListenerManager(properties, kvStore);
            subscribeListenerManager.start();
        }
    }

    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        return subscribeListenerManager.addSubscribeListener(path, listener);
    }

    @Override
    public void unsubscribe(String path) {
        subscribeListenerManager.removeSubscribeListener(path);
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        final String groupId = properties.getClusterName() + "--1";
        final Node node = NodeManager.getInstance().get(groupId, new PeerId((properties.getServerAddress()), properties.getServerPort()));
        node.addReplicatorStateListener(new RaftConnectionStateListener(listener));
    }

    @Override
    public String get(String key) {
        return readUtf8(kvStore.bGet(key));
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        kvStore.bPut(key, writeUtf8(value));
        if (key.startsWith(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH)) {
            addWorkerGroup(key);
        }
    }

    private void addWorkerGroup(String key) {
        List<String> workerGroupList = getWorkerGroups();
        String workerGroup = key.substring(key.indexOf(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS)
                + Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS.length() + 1, key.lastIndexOf(Constants.SINGLE_SLASH));
        if (!workerGroupList.contains(workerGroup)) {
            workerGroupList.add(workerGroup);
            kvStore.bPut(REGISTRY_DOLPHINSCHEDULER_WORKER_GROUPS, writeUtf8(JSONUtils.toJsonString(workerGroupList)));
        }
    }

    private List<String> getWorkerGroups() {
        final String storedWorkerGroup = readUtf8(kvStore.bGet(REGISTRY_DOLPHINSCHEDULER_WORKER_GROUPS));
        if (StringUtils.isEmpty(storedWorkerGroup)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(JSONUtils.toList(storedWorkerGroup, String.class));
    }

    @Override
    public void delete(String key) {
        kvStore.bDelete(key);
        final DistributedLock<byte[]> distributedLock = distributedLockMap.get(key);
        if (distributedLock != null) {
            distributedLock.unlock();
        }
        distributedLockMap.remove(key);
    }

    @Override
    public Collection<String> children(String key) {
        List<String> children = new ArrayList<>();
        if (key.equals(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS)) {
            //get all worker groups
            children = getWorkerGroups();

        } else {
            final List<KVEntry> result = kvStore.bScan(key, key + Constants.SINGLE_SLASH + Constants.RANDOM_STRING);
            if (result.isEmpty()) {
                return new ArrayList<>();
            }
            for (final KVEntry kv : result) {
                final String entryKey = readUtf8(kv.getKey());
                if (StringUtils.isEmpty(readUtf8(kv.getValue())) || StringUtils.isEmpty(entryKey)) {
                    continue;
                }
                String child = entryKey.substring(entryKey.lastIndexOf(Constants.SINGLE_SLASH) + 1);
                children.add(child);
            }
        }
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
        subscribeListenerManager.close();
        kvStore.shutdown();
        log.info("Closed raft registry...");
    }

}
