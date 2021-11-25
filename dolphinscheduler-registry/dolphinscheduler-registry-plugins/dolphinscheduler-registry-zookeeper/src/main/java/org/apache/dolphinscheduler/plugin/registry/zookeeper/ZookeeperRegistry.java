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

import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.BASE_SLEEP_TIME;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.BLOCK_UNTIL_CONNECTED_WAIT_MS;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.CONNECTION_TIMEOUT_MS;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.DIGEST;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.MAX_RETRIES;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.NAME_SPACE;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.SERVERS;
import static org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperConfiguration.SESSION_TIMEOUT_MS;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.curator.RetryPolicy;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Strings;

public final class ZookeeperRegistry implements Registry {

    private CuratorFramework client;

    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    private static final ThreadLocal<Map<String, InterProcessMutex>> threadLocalLockMap = new ThreadLocal<>();

    private static RetryPolicy buildRetryPolicy(Map<String, String> registerData) {
        int baseSleepTimeMs = BASE_SLEEP_TIME.getParameterValue(registerData.get(BASE_SLEEP_TIME.getName()));
        int maxRetries = MAX_RETRIES.getParameterValue(registerData.get(MAX_RETRIES.getName()));
        int maxSleepMs = baseSleepTimeMs * maxRetries;
        return new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries, maxSleepMs);
    }

    private static void buildDigest(CuratorFrameworkFactory.Builder builder, String digest) {
        builder.authorization(DIGEST.getName(), digest.getBytes(StandardCharsets.UTF_8))
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

    @Override
    public void start(Map<String, String> config) {
        CuratorFrameworkFactory.Builder builder =
            CuratorFrameworkFactory.builder()
                                   .connectString(SERVERS.getParameterValue(config.get(SERVERS.getName())))
                                   .retryPolicy(buildRetryPolicy(config))
                                   .namespace(NAME_SPACE.getParameterValue(config.get(NAME_SPACE.getName())))
                                   .sessionTimeoutMs(SESSION_TIMEOUT_MS.getParameterValue(config.get(SESSION_TIMEOUT_MS.getName())))
                                   .connectionTimeoutMs(CONNECTION_TIMEOUT_MS.getParameterValue(config.get(CONNECTION_TIMEOUT_MS.getName())));

        String digest = DIGEST.getParameterValue(config.get(DIGEST.getName()));
        if (!Strings.isNullOrEmpty(digest)) {
            buildDigest(builder, digest);
        }
        client = builder.build();

        client.start();
        try {
            if (!client.blockUntilConnected(BLOCK_UNTIL_CONNECTED_WAIT_MS.getParameterValue(config.get(BLOCK_UNTIL_CONNECTED_WAIT_MS.getName())), MILLISECONDS)) {
                client.close();
                throw new RegistryException("zookeeper connect timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("zookeeper connect error", e);
        }
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        client.getConnectionStateListenable().addListener(new ZookeeperConnectionStateListener(listener));
    }

    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        final TreeCache treeCache = treeCacheMap.computeIfAbsent(path, $ -> new TreeCache(client, path));
        treeCache.getListenable().addListener(($, event) -> listener.notify(new EventAdaptor(event, path)));
        try {
            treeCache.start();
        } catch (Exception e) {
            treeCacheMap.remove(path);
            throw new RegistryException("Failed to subscribe listener for key: " + path, e);
        }
        return true;
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
        InterProcessMutex interProcessMutex = new InterProcessMutex(client, key);
        try {
            interProcessMutex.acquire();
            if (null == threadLocalLockMap.get()) {
                threadLocalLockMap.set(new HashMap<>(3));
            }
            threadLocalLockMap.get().put(key, interProcessMutex);
            return true;
        } catch (Exception e) {
            try {
                interProcessMutex.release();
                throw new RegistryException("zookeeper get lock error", e);
            } catch (Exception exception) {
                throw new RegistryException("zookeeper release lock error", e);
            }
        }
    }

    @Override
    public boolean releaseLock(String key) {
        if (null == threadLocalLockMap.get().get(key)) {
            return false;
        }
        try {
            threadLocalLockMap.get().get(key).release();
            threadLocalLockMap.get().remove(key);
            if (threadLocalLockMap.get().isEmpty()) {
                threadLocalLockMap.remove();
            }
        } catch (Exception e) {
            throw new RegistryException("zookeeper release lock error", e);
        }
        return true;
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
