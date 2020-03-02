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
package org.apache.dolphinscheduler.server.worker.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.Constants.COMMA;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.SLASH;


/**
 *  worker registry
 */
public class WorkerRegistry {

    private final Logger logger = LoggerFactory.getLogger(WorkerRegistry.class);

    /**
     *  zookeeper registry center
     */
    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     *  port
     */
    private final int port;

    /**
     * heartbeat interval
     */
    private final long heartBeatInterval;

    /**
     * heartbeat executor
     */
    private final ScheduledExecutorService heartBeatExecutor;

    /**
     * worker start time
     */
    private final String startTime;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * construct
     *
     * @param zookeeperRegistryCenter zookeeperRegistryCenter
     * @param port port
     * @param heartBeatInterval heartBeatInterval
     */
    public WorkerRegistry(ZookeeperRegistryCenter zookeeperRegistryCenter, int port, long heartBeatInterval){
        this(zookeeperRegistryCenter, port, heartBeatInterval, DEFAULT_WORKER_GROUP);
    }

    /**
     *  construct
     * @param zookeeperRegistryCenter zookeeperRegistryCenter
     * @param port port
     */
    public WorkerRegistry(ZookeeperRegistryCenter zookeeperRegistryCenter, int port, long heartBeatInterval, String workerGroup){
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
        this.port = port;
        this.heartBeatInterval = heartBeatInterval;
        this.workerGroup = workerGroup;
        this.startTime = DateUtils.dateToString(new Date());
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     *  registry
     */
    public void registry() {
        String address = Constants.LOCAL_ADDRESS;
        String localNodePath = getWorkerPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().persistEphemeral(localNodePath, "");
        zookeeperRegistryCenter.getZookeeperCachedOperator().getZkClient().getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if(newState == ConnectionState.LOST){
                    logger.error("worker : {} connection lost from zookeeper", address);
                } else if(newState == ConnectionState.RECONNECTED){
                    logger.info("worker : {} reconnected to zookeeper", address);
                    zookeeperRegistryCenter.getZookeeperCachedOperator().persistEphemeral(localNodePath, "");
                } else if(newState == ConnectionState.SUSPENDED){
                    logger.warn("worker : {} connection SUSPENDED ", address);
                }
            }
        });
        this.heartBeatExecutor.scheduleAtFixedRate(new HeartBeatTask(), heartBeatInterval, heartBeatInterval, TimeUnit.SECONDS);
        logger.info("worker node : {} registry to ZK successfully with heartBeatInterval : {}s", address, heartBeatInterval);

    }

    /**
     *  remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        String localNodePath = getWorkerPath();
        zookeeperRegistryCenter.getZookeeperCachedOperator().remove(localNodePath);
        this.heartBeatExecutor.shutdownNow();
        logger.info("worker node : {} unRegistry to ZK.", address);
    }

    /**
     *  get worker path
     * @return
     */
    private String getWorkerPath() {
        String address = getLocalAddress();
        StringBuilder builder = new StringBuilder(100);
        String workerPath = this.zookeeperRegistryCenter.getWorkerPath();
        builder.append(workerPath).append(SLASH);
        if(StringUtils.isEmpty(workerGroup)){
            workerGroup = DEFAULT_WORKER_GROUP;
        }
        //trim and lower case is need
        builder.append(workerGroup.trim().toLowerCase()).append(SLASH);
        builder.append(address);
        return builder.toString();
    }

    /**
     *  get local address
     * @return
     */
    private String getLocalAddress(){
        return Constants.LOCAL_ADDRESS + ":" + port;
    }

    /**
     * hear beat task
     */
    class HeartBeatTask implements Runnable{

        @Override
        public void run() {
            try {
                StringBuilder builder = new StringBuilder(100);
                builder.append(OSUtils.cpuUsage()).append(COMMA);
                builder.append(OSUtils.memoryUsage()).append(COMMA);
                builder.append(OSUtils.loadAverage()).append(COMMA);
                builder.append(startTime).append(COMMA);
                builder.append(DateUtils.dateToString(new Date()));
                String workerPath = getWorkerPath();
                zookeeperRegistryCenter.getZookeeperCachedOperator().update(workerPath, builder.toString());
            } catch (Throwable ex){
                logger.error("error write worker heartbeat info", ex);
            }
        }
    }
}
