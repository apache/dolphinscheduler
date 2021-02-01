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
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_PROPERTIES_PATH;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_ROOT;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_SESSION_TIMEOUT_MS;
import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.alert.exception.AlertException;
import org.apache.dolphinscheduler.common.utils.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClient {

    private static final Properties properties = new Properties();
    private final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    private CuratorFramework zkClient;

    public void init() {
        this.zkClient = buildClient();
        checkNotNull(zkClient);
        zkClient.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.LOST) {
                logger.error("connection lost from zookeeper");
            } else if (newState == ConnectionState.RECONNECTED) {
                logger.info("reconnected to zookeeper");
            } else if (newState == ConnectionState.SUSPENDED) {
                logger.warn("connection SUSPENDED to zookeeper");
            } else if (newState == ConnectionState.CONNECTED) {
                logger.info("connected to zookeeper server list:[{}]", properties.getProperty(ZOOKEEPER_LIST));
            }
        });
    }

    private CuratorFramework buildClient() {

        /**
         * init properties
         */
        String[] propertyFiles = new String[]{ZOOKEEPER_PROPERTIES_PATH};

        for (String fileName : propertyFiles) {
            InputStream fis = null;
            try {
                fis = ZookeeperClient.class.getResourceAsStream(fileName);
                properties.load(fis);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                if (fis != null) {
                    IOUtils.closeQuietly(fis);
                }
                System.exit(1);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }


        logger.info("zookeeper registry center init, server lists is: [{}]", properties.getProperty(ZOOKEEPER_LIST));

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(properties.getProperty(ZOOKEEPER_LIST, "localhost:2181"))
                .retryPolicy(new ExponentialBackoffRetry(Integer.valueOf(properties.getProperty(ZOOKEEPER_BASE_SLEEP_TIME_MS, "100"))
                        , Integer.valueOf(properties.getProperty(ZOOKEEPER_MAX_RETRY, "10")),
                        Integer.valueOf(properties.getProperty(ZOOKEEPER_MAX_SLEEP_MS, "30000"))));

        //these has default value
        if (0 != Integer.valueOf(properties.getProperty(ZOOKEEPER_SESSION_TIMEOUT_MS, "60000"))) {
            builder.sessionTimeoutMs(Integer.valueOf(properties.getProperty(ZOOKEEPER_SESSION_TIMEOUT_MS, "60000")));
        }

        if (0 != Integer.valueOf(properties.getProperty(ZOOKEEPER_CONNECTION_TIMEOUT_MS, "30000"))) {
            builder.connectionTimeoutMs(Integer.valueOf(properties.getProperty(ZOOKEEPER_CONNECTION_TIMEOUT_MS, "30000")));
        }
        if (StringUtils.isNotBlank(properties.getProperty(ZOOKEEPER_DIGEST, ""))) {
            builder.authorization("digest", properties.getProperty(ZOOKEEPER_DIGEST, "").getBytes(StandardCharsets.UTF_8)).aclProvider(new ACLProvider() {

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
            logger.info("trying to connect zookeeper server list:{}", properties.getProperty(ZOOKEEPER_LIST));
            zkClient.blockUntilConnected(30, TimeUnit.SECONDS);

        } catch (final Exception ex) {
            throw new AlertException(ex);
        }
        return zkClient;
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }

    public InterProcessMutex getAlertLockPath() {
        String alertLockPath = properties.getProperty(ZOOKEEPER_ROOT, "/dolphinscheduler") + ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS;
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
