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

package org.apache.dolphinscheduler.plugin.register.zookeeper;

import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.BASE_SLEEP_TIME;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.BLOCK_UNTIL_CONNECTED_WAIT_MS;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.CONNECTION_TIMEOUT_MS;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.DIGEST;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.MAX_RETRIES;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.NAME_SPACE;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.SERVERS;
import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.SESSION_TIMEOUT_MS;

import org.apache.dolphinscheduler.spi.register.ListenerManager;
import org.apache.dolphinscheduler.spi.register.RegisterException;
import org.apache.dolphinscheduler.spi.register.DataChangeEvent;
import org.apache.dolphinscheduler.spi.register.Register;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;

public class ZookeeperRegister implements Register {

    private CuratorFramework client;

    private Map<String, TreeCache> treeCacheMap = new HashMap<>();

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
    public void init(Map<String, String> registerData) {

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(SERVERS.getParameterValue(registerData.get(SERVERS.getName())))
                .retryPolicy(buildRetryPolicy(registerData))
                .namespace(NAME_SPACE.getParameterValue(registerData.get(NAME_SPACE.getName())))
                .sessionTimeoutMs(SESSION_TIMEOUT_MS.getParameterValue(registerData.get(SESSION_TIMEOUT_MS.getName())))
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS.getParameterValue(registerData.get(CONNECTION_TIMEOUT_MS.getName())));

        String digest = DIGEST.getParameterValue(registerData.get(DIGEST.getName()));
        if (!Strings.isNullOrEmpty(digest)) {
            buildDigest(builder, digest);
        }
        client = builder.build();
        client.start();
        try {
            if (!client.blockUntilConnected(BLOCK_UNTIL_CONNECTED_WAIT_MS.getParameterValue(registerData.get(BLOCK_UNTIL_CONNECTED_WAIT_MS.getName())), TimeUnit.MILLISECONDS)) {
                client.close();
                throw new RegisterException("zookeeper connect timeout");
            }
        } catch (Exception e) {

            throw new RegisterException("zookeeper connect error", e);
        }

        //test
        try {
            client.create().forPath("/kris", "123456".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
       // super.init(registerData);
    }

    @Override
    public void subscribe(String path, SubscribeListener subscribeListener) {
        if (null != treeCacheMap.get(path)) {
            return;
        }
        TreeCache treeCache = new TreeCache(client, path);
        TreeCacheListener treeCacheListener = (client, event) -> {
            TreeCacheEvent.Type type = event.getType();
            DataChangeEvent eventType = null;
            String dataPath = null;
            switch (type) {
                case NODE_ADDED:

                    dataPath = event.getData().getPath();
                    eventType = DataChangeEvent.ADD;
                    break;
                case NODE_UPDATED:
                    eventType = DataChangeEvent.UPDATE;
                    dataPath = event.getData().getPath();

                    break;
                case NODE_REMOVED:
                    eventType = DataChangeEvent.REMOVE;
                    dataPath = event.getData().getPath();
                    break;
            }
            if (null != eventType && null != dataPath) {
                ListenerManager.dataChange(dataPath, eventType);
            }
        };
        treeCache.getListenable().addListener(treeCacheListener);
        treeCacheMap.put(path, treeCache);
        try {
            treeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ListenerManager.addListener(path, subscribeListener);
    }


    @Override
    public void unsubscribe(String path) {
        TreeCache treeCache = treeCacheMap.get(path);
        treeCache.close();
        ListenerManager.removeListener(path);
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {

        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            throw new RegisterException("zookeeper remove error", e);
        }
    }

    @Override
    public boolean isExisted(String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (Exception e) {
            throw new RegisterException("zookeeper check key is existed error", e);
        }
    }

    @Override
    public void persist(String key, String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (Exception e) {
            throw new RegisterException("zookeeper persist error", e);
        }
    }

    @Override
    public void update(String key, String value) {
        try {
            TransactionOp transactionOp = client.transactionOp();
            client.transaction().forOperations(transactionOp.check().forPath(key), transactionOp.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RegisterException("zookeeper update error", e);
        }
    }

    @Override
    public List<String> getChildren(String key) {
        try {
            return client.getChildren().forPath(key);
        } catch (Exception e) {
            throw new RegisterException("zookeeper get children error", e);
        }
    }

    @Override
    public String getData(String key) {
        return null;
    }

    public boolean delete(String nodePath) throws Exception {
        client.delete()
                .guaranteed()
                .deletingChildrenIfNeeded()
                .withVersion(4)
                .forPath(nodePath);

        return true;

    }
}
