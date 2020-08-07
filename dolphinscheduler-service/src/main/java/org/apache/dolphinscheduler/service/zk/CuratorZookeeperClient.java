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
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

/**
 * Shared Curator zookeeper client
 */
@Component
public class CuratorZookeeperClient implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(CuratorZookeeperClient.class);

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    private CuratorFramework zkClient;


    @Override
    public void afterPropertiesSet() throws Exception {
        this.zkClient = buildClient();
        initStateLister();
    }

    private CuratorFramework buildClient() {
        logger.info("zookeeper registry center init, server lists is: {}.", zookeeperConfig.getServerList());

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().ensembleProvider(new DefaultEnsembleProvider(checkNotNull(zookeeperConfig.getServerList(),"zookeeper quorum can't be null")))
                .retryPolicy(new ExponentialBackoffRetry(zookeeperConfig.getBaseSleepTimeMs(), zookeeperConfig.getMaxRetries(), zookeeperConfig.getMaxSleepMs()));

        //these has default value
        if (0 != zookeeperConfig.getSessionTimeoutMs()) {
            builder.sessionTimeoutMs(zookeeperConfig.getSessionTimeoutMs());
        }
        if (0 != zookeeperConfig.getConnectionTimeoutMs()) {
            builder.connectionTimeoutMs(zookeeperConfig.getConnectionTimeoutMs());
        }
        if (StringUtils.isNotBlank(zookeeperConfig.getDigest())) {
            builder.authorization("digest", zookeeperConfig.getDigest().getBytes(StandardCharsets.UTF_8)).aclProvider(new ACLProvider() {

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
        zkClient = builder.build();
        zkClient.start();
        try {
            zkClient.blockUntilConnected();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
        return zkClient;
    }

    public void initStateLister() {
        checkNotNull(zkClient);

        zkClient.getConnectionStateListenable().addListener((client, newState) -> {
            if(newState == ConnectionState.LOST){
                logger.error("connection lost from zookeeper");
            } else if(newState == ConnectionState.RECONNECTED){
                logger.info("reconnected to zookeeper");
            } else if(newState == ConnectionState.SUSPENDED){
                logger.warn("connection SUSPENDED to zookeeper");
            }
        });
    }

    public ZookeeperConfig getZookeeperConfig() {
        return zookeeperConfig;
    }

    public void setZookeeperConfig(ZookeeperConfig zookeeperConfig) {
        this.zookeeperConfig = zookeeperConfig;
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }
}