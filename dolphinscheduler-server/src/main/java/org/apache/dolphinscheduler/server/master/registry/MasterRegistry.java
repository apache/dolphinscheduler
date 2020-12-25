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
package org.apache.dolphinscheduler.server.master.registry;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import static org.apache.dolphinscheduler.remote.utils.Constants.COMMA;

/**
 *  master registry
 */
@Service
public class MasterRegistry {

    private final Logger logger = LoggerFactory.getLogger(MasterRegistry.class);

    /**
     *  zookeeper registry center
     */
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    /**
     * worker start time
     */
    private String startTime;


    @PostConstruct
    public void init(){
        this.startTime = DateUtils.dateToString(new Date());
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     *  registry
     */
    public void registry() {
        String address = OSUtils.getHost();
        String localNodePath = getMasterPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().persistEphemeral(localNodePath, "");
        zookeeperRegistryCenter.getZookeeperCachedOperator().getZkClient().getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if(newState == ConnectionState.LOST){
                    logger.error("master : {} connection lost from zookeeper", address);
                } else if(newState == ConnectionState.RECONNECTED){
                    logger.info("master : {} reconnected to zookeeper", address);
                    zookeeperRegistryCenter.getZookeeperCachedOperator().persistEphemeral(localNodePath, "");
                } else if(newState == ConnectionState.SUSPENDED){
                    logger.warn("master : {} connection SUSPENDED ", address);
                }
            }
        });
        int masterHeartbeatInterval = masterConfig.getMasterHeartbeatInterval();
        HeartBeatTask heartBeatTask = new HeartBeatTask(startTime,
                masterConfig.getMasterReservedMemory(),
                masterConfig.getMasterMaxCpuloadAvg(),
                Sets.newHashSet(getMasterPath()),
                zookeeperRegistryCenter);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, masterHeartbeatInterval, masterHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("master node : {} registry to ZK successfully with heartBeatInterval : {}s", address, masterHeartbeatInterval);
    }

    /**
     *  remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        String localNodePath = getMasterPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().remove(localNodePath);
        logger.info("master node : {} unRegistry to ZK.", address);
    }

    /**
     *  get master path
     * @return
     */
    private String getMasterPath() {
        String address = getLocalAddress();
        String localNodePath = this.zookeeperRegistryCenter.getMasterPath() + "/" + address;
        return localNodePath;
    }

    /**
     *  get local address
     * @return
     */
    private String getLocalAddress(){
        return OSUtils.getHost() + Constants.COLON + masterConfig.getListenPort();
    }

}
