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
package org.apache.dolphinscheduler.service.zk;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

/**
 * zk base operator
 */
@Component
public class ZookeeperOperator implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperOperator.class);

    @Autowired
    private CuratorZookeeperClient zookeeperClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        registerListener();
    }

    /**
     * this method is for sub class,
     */
    protected void registerListener(){}

    public String get(final String key) {
        try {
            return new String(zookeeperClient.getZkClient().getData().forPath(key), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("get key : {}", key, ex);
        }
        return null;
    }

    public List<String> getChildrenKeys(final String key) {
        List<String> values;
        try {
            values = zookeeperClient.getZkClient().getChildren().forPath(key);
            return values;
        } catch (InterruptedException ex) {
            logger.error("getChildrenKeys key : {} InterruptedException", key);
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            logger.error("getChildrenKeys key : {}", key, ex);
            throw new RuntimeException(ex);
        }
    }

    public boolean hasChildren(final String key){
        Stat stat ;
        try {
            stat = zookeeperClient.getZkClient().checkExists().forPath(key);
            return stat.getNumChildren() >= 1;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean isExisted(final String key) {
        try {
            return zookeeperClient.getZkClient().checkExists().forPath(key) != null;
        } catch (Exception ex) {
            logger.error("isExisted key : {}", key, ex);
        }
        return false;
    }

    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                zookeeperClient.getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (Exception ex) {
            logger.error("persist key : {} , value : {}", key, value, ex);
        }
    }

    public void update(final String key, final String value) {
        try {

            CuratorOp check = zookeeperClient.getZkClient().transactionOp().check().forPath(key);
            CuratorOp setData = zookeeperClient.getZkClient().transactionOp().setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            zookeeperClient.getZkClient().transaction().forOperations(check, setData);

        } catch (Exception ex) {
            logger.error("update key : {} , value : {}", key, value, ex);
        }
    }

    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                try {
                    zookeeperClient.getZkClient().delete().deletingChildrenIfNeeded().forPath(key);
                } catch (KeeperException.NoNodeException ignore) {
                    //NOP
                }
            }
            zookeeperClient.getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            logger.error("persistEphemeral key : {} , value : {}", key, value, ex);
        }
    }

    public void persistEphemeral(String key, String value, boolean overwrite) {
        try {
            if (overwrite) {
                persistEphemeral(key, value);
            } else {
                if (!isExisted(key)) {
                    zookeeperClient.getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (final Exception ex) {
            logger.error("persistEphemeral key : {} , value : {}, overwrite : {}", key, value, overwrite, ex);
        }
    }

    public void persistEphemeralSequential(final String key, String value) {
        try {
            zookeeperClient.getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            logger.error("persistEphemeralSequential key : {}", key, ex);
        }
    }

    public void remove(final String key) {
        try {
            if (isExisted(key)) {
                zookeeperClient.getZkClient().delete().deletingChildrenIfNeeded().forPath(key);
            }
        } catch (KeeperException.NoNodeException ignore) {
            //NOP
        } catch (final Exception ex) {
            logger.error("remove key : {}", key, ex);
        }
    }

    public CuratorFramework getZkClient() {
        return zookeeperClient.getZkClient();
    }

    public ZookeeperConfig getZookeeperConfig() {
        return zookeeperClient.getZookeeperConfig();
    }

    public void close() {
        CloseableUtils.closeQuietly(zookeeperClient.getZkClient());
    }
}