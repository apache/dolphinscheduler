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

package org.apache.dolphinscheduler.alert.utils;

import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_BASE_SLEEP_TIME_MS;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_CONNECTION_TIMEOUT_MS;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_DIGEST;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_LIST;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_MAX_RETRY;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_MAX_SLEEP_MS;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_ROOT;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_SESSION_TIMEOUT_MS;
import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.alert.exception.AlertException;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClient {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private CuratorFramework zkClient;

    public void init() {
        this.zkClient = buildClient();
        initStateLister();
    }

    private CuratorFramework buildClient() {
        logger.info("zookeeper registry center init, server lists is: [{}]", PropertyUtils.getString(ZOOKEEPER_LIST));

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(PropertyUtils.getString(ZOOKEEPER_LIST,"localhost:2181"))
                .retryPolicy(new ExponentialBackoffRetry(PropertyUtils.getInt(ZOOKEEPER_BASE_SLEEP_TIME_MS, 100)
                        , PropertyUtils.getInt(ZOOKEEPER_MAX_RETRY, 10), PropertyUtils.getInt(ZOOKEEPER_MAX_SLEEP_MS, 30000)));

        //these has default value
        if (0 != PropertyUtils.getInt(ZOOKEEPER_SESSION_TIMEOUT_MS, 60000)) {
            builder.sessionTimeoutMs(PropertyUtils.getInt(ZOOKEEPER_SESSION_TIMEOUT_MS, 60000));
        }

        if (0 != PropertyUtils.getInt(ZOOKEEPER_CONNECTION_TIMEOUT_MS, 30000)) {
            builder.connectionTimeoutMs(PropertyUtils.getInt(ZOOKEEPER_CONNECTION_TIMEOUT_MS, 30000));
        }
        if (StringUtils.isNotBlank(PropertyUtils.getString(ZOOKEEPER_DIGEST, ""))) {
            builder.authorization("digest", PropertyUtils.getString(ZOOKEEPER_DIGEST, "").getBytes(StandardCharsets.UTF_8)).aclProvider(new ACLProvider() {

                @Override
                public List<org.apache.zookeeper.data.ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(final String path) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });
        }

        zkClient = builder.build();
        zkClient.start();
        try {
            logger.info("trying to connect zookeeper server list:{}", PropertyUtils.getString(ZOOKEEPER_LIST));
            zkClient.blockUntilConnected(30, TimeUnit.SECONDS);

        } catch (final Exception ex) {
            throw new AlertException(ex);
        }
        return zkClient;
    }

    public void initStateLister() {
        checkNotNull(zkClient);

        zkClient.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.LOST) {
                logger.error("connection lost from zookeeper");
            } else if (newState == ConnectionState.RECONNECTED) {
                logger.info("reconnected to zookeeper");
            } else if (newState == ConnectionState.SUSPENDED) {
                logger.warn("connection SUSPENDED to zookeeper");
            } else if (newState == ConnectionState.CONNECTED) {
                logger.info("connected to zookeeper server list:[{}]", PropertyUtils.getString(ZOOKEEPER_LIST));
            }
        });
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }

    public InterProcessMutex getAlertLockPath() {
        String alertLockPath = PropertyUtils.getString(ZOOKEEPER_ROOT, "/dolphinscheduler") + ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS;
        return new InterProcessMutex(zkClient, alertLockPath);

    }

    public void release(InterProcessLock mutex) {
        if (mutex != null) {
            try {
                mutex.release();
            } catch (Exception e) {
                if ("instance must be started before calling this method".equals(e.getMessage())) {
                    logger.warn("lock release");
                } else {
                    logger.error("lock release failed", e);
                }

            }
        }

    }
}
