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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  master registry
 */
public class MasterRegistry {

    private final Logger logger = LoggerFactory.getLogger(MasterRegistry.class);

    /**
     *  zookeeper registry center
     */
    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     *  port
     */
    private final int port;

    /**
     *  construct
     * @param zookeeperRegistryCenter zookeeperRegistryCenter
     * @param port port
     */
    public MasterRegistry(ZookeeperRegistryCenter zookeeperRegistryCenter, int port){
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
        this.port = port;
    }

    /**
     *  registry
     */
    public void registry() {
        String address = Constants.LOCAL_ADDRESS;
        String localNodePath = getWorkerPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().persist(localNodePath, "");
        zookeeperRegistryCenter.getZookeeperCachedOperator().getZkClient().getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if(newState == ConnectionState.LOST){
                    logger.error("master : {} connection lost from zookeeper", address);
                } else if(newState == ConnectionState.RECONNECTED){
                    logger.info("master : {} reconnected to zookeeper", address);
                    zookeeperRegistryCenter.getZookeeperCachedOperator().persist(localNodePath, "");
                } else if(newState == ConnectionState.SUSPENDED){
                    logger.warn("master : {} connection SUSPENDED ", address);
                }
            }
        });
        logger.info("master node : {} registry to ZK successfully.", address);
    }

    /**
     *  remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        String localNodePath = getWorkerPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().remove(localNodePath);
        logger.info("worker node : {} unRegistry to ZK.", address);
    }

    /**
     *  get worker path
     * @return
     */
    private String getWorkerPath() {
        String address = getLocalAddress();
        String localNodePath = this.zookeeperRegistryCenter.getWorkerPath() + "/" + address;
        return localNodePath;
    }

    /**
     *  get local address
     * @return
     */
    private String getLocalAddress(){
        return Constants.LOCAL_ADDRESS + ":" + port;
    }
}
