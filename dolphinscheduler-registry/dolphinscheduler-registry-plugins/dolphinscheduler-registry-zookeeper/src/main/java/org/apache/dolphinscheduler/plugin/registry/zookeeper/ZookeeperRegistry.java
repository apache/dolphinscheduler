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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.commons.lang3.time.DurationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Strings;

@Slf4j
final class ZookeeperRegistry implements Registry {

    private final ZookeeperRegistryProperties.ZookeeperProperties properties;
    private final CuratorFramework client;

    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    private static final ThreadLocal<Map<String, InterProcessMutex>> threadLocalLockMap = new ThreadLocal<>();

    ZookeeperRegistry(ZookeeperRegistryProperties registryProperties) {
        properties = registryProperties.getZookeeper();

        final ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
                (int) properties.getRetryPolicy().getBaseSleepTime().toMillis(),
                properties.getRetryPolicy().getMaxRetries(),
                (int) properties.getRetryPolicy().getMaxSleep().toMillis());

        CuratorFrameworkFactory.Builder builder =
                CuratorFrameworkFactory.builder()
                        .connectString(properties.getConnectString())
                        .retryPolicy(retryPolicy)
                        .namespace(properties.getNamespace())
                        .sessionTimeoutMs(DurationUtils.toMillisInt(properties.getSessionTimeout()))
                        .connectionTimeoutMs(DurationUtils.toMillisInt(properties.getConnectionTimeout()));

        final String digest = properties.getDigest();
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes(StandardCharsets.UTF_8))
                    .aclProvider(new ACLProvider() {

                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
    }

    @Override
    public void start() {
        client.start();
        try {
            if (!client.blockUntilConnected(DurationUtils.toMillisInt(properties.getBlockUntilConnected()),
                    MILLISECONDS)) {
                client.close();
                throw new RegistryException("zookeeper connect failed in : " + properties.getConnectString() + "ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("Zookeeper registry start failed", e);
        }
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        client.getConnectionStateListenable().addListener(new ZookeeperConnectionStateListener(listener));
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        try {
            if (!client.blockUntilConnected(DurationUtils.toMillisInt(timeout), MILLISECONDS)) {
                throw new RegistryException(
                        String.format("Cannot connect to registry in %s s", timeout.getSeconds()));
            }
        } catch (RegistryException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException(
                    String.format("Cannot connect to registry in %s s", timeout.getSeconds()), e);
        }
    }

    @Override
    public void subscribe(String path, SubscribeListener listener) {
        final TreeCache treeCache = treeCacheMap.computeIfAbsent(path, $ -> new TreeCache(client, path));
        treeCache.getListenable().addListener(($, event) -> listener.notify(new EventAdaptor(event, path)));
        try {
            treeCache.start();
        } catch (Exception e) {
            treeCacheMap.remove(path);
            throw new RegistryException("Failed to subscribe listener for key: " + path, e);
        }
    }

    @Override
    public void unsubscribe(String path) {
        CloseableUtils.closeQuietly(treeCacheMap.get(path));
    }

    @Override
    public String get(String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RegistryException("zookeeper get data error", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (Exception e) {
            throw new RegistryException("zookeeper check key is existed error", e);
        }
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        final CreateMode mode = deleteOnDisconnect ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;

        try {
            client.create()
                    .orSetData()
                    .creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    @Override
    public List<String> children(String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception e) {
            throw new RegistryException("zookeeper get children error", e);
        }
    }

    @Override
    public void delete(String nodePath) {
        try {
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(nodePath);
        } catch (KeeperException.NoNodeException ignored) {
            // Is already deleted or does not exist
        } catch (Exception e) {
            throw new RegistryException("Failed to delete registry key: " + nodePath, e);
        }
    }

    @Override
    public boolean acquireLock(String key) {
        Map<String, InterProcessMutex> processMutexMap = threadLocalLockMap.get();
        if (null == processMutexMap) {
            processMutexMap = new HashMap<>();
            threadLocalLockMap.set(processMutexMap);
        }
        InterProcessMutex interProcessMutex = null;
        try {
            interProcessMutex =
                    Optional.ofNullable(processMutexMap.get(key)).orElse(new InterProcessMutex(client, key));
            if (interProcessMutex.isAcquiredInThisProcess()) {
                // Since etcd/jdbc cannot implement a reentrant lock, we need to check if the lock is already acquired
                // If it is already acquired, return true directly
                // This means you only need to release once when you acquire multiple times
                return true;
            }
            interProcessMutex.acquire();
            processMutexMap.put(key, interProcessMutex);
            return true;
        } catch (Exception e) {
            try {
                if (interProcessMutex != null) {
                    interProcessMutex.release();
                }
                throw new RegistryException(String.format("zookeeper get lock: %s error", key), e);
            } catch (Exception exception) {
                throw new RegistryException(String.format("zookeeper get lock: %s error", key), e);
            }
        }
    }

    @Override
    public boolean acquireLock(String key, long timeout) {
        Map<String, InterProcessMutex> processMutexMap = threadLocalLockMap.get();
        if (null == processMutexMap) {
            processMutexMap = new HashMap<>();
            threadLocalLockMap.set(processMutexMap);
        }
        InterProcessMutex interProcessMutex = null;
        try {
            interProcessMutex =
                    Optional.ofNullable(processMutexMap.get(key)).orElse(new InterProcessMutex(client, key));
            if (interProcessMutex.isAcquiredInThisProcess()) {
                return true;
            }
            if (interProcessMutex.acquire(timeout, MILLISECONDS)) {
                processMutexMap.put(key, interProcessMutex);
                return true;
            }
            return false;
        } catch (Exception e) {
            try {
                if (interProcessMutex != null) {
                    interProcessMutex.release();
                }
                throw new RegistryException(String.format("zookeeper get lock: %s error", key), e);
            } catch (Exception exception) {
                throw new RegistryException(String.format("zookeeper get lock: %s error", key), e);
            }
        }
    }

    @Override
    public boolean releaseLock(String key) {
        Map<String, InterProcessMutex> processMutexMap = threadLocalLockMap.get();
        if (processMutexMap == null) {
            return true;
        }
        InterProcessMutex interProcessMutex = processMutexMap.get(key);
        if (null == interProcessMutex) {
            return false;
        }
        try {
            interProcessMutex.release();
            processMutexMap.remove(key);
            if (processMutexMap.isEmpty()) {
                threadLocalLockMap.remove();
            }
        } catch (Exception e) {
            throw new RegistryException("zookeeper release lock error", e);
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public void close() {
        treeCacheMap.values().forEach(CloseableUtils::closeQuietly);
        CloseableUtils.closeQuietly(client);
    }

    static final class EventAdaptor extends Event {

        public EventAdaptor(TreeCacheEvent event, String key) {
            key(key);

            switch (event.getType()) {
                case NODE_ADDED:
                    type(Type.ADD);
                    break;
                case NODE_UPDATED:
                    type(Type.UPDATE);
                    break;
                case NODE_REMOVED:
                    type(Type.REMOVE);
                    break;
                default:
                    break;
            }

            final ChildData data = event.getData();
            if (data != null) {
                path(data.getPath());
                data(new String(data.getData()));
            }
        }
    }
}
