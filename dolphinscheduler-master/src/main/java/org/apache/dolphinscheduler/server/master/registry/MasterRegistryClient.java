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

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.service.FailoverService;
import org.apache.dolphinscheduler.server.master.task.MasterHeartBeatTask;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_NODE;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

/**
 * <p>DolphinScheduler master register client, used to connect to registry and hand the registry events.
 * <p>When the Master node startup, it will register in registry center. And start a {@link MasterHeartBeatTask} to update its metadata in registry.
 */
@Component
public class MasterRegistryClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryClient.class);

    @Autowired
    private FailoverService failoverService;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private MasterConnectStrategy masterConnectStrategy;

    private MasterHeartBeatTask masterHeartBeatTask;

    public void start() {
        try {
            this.masterHeartBeatTask = new MasterHeartBeatTask(masterConfig, registryClient);
            // master registry
            registry();
            registryClient.addConnectionStateListener(
                    new MasterConnectionStateListener(masterConfig, registryClient, masterConnectStrategy));
            registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_NODE, new MasterRegistryDataListener());
        } catch (Exception e) {
            throw new RegistryException("Master registry client start up error", e);
        }
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    @Override
    public void close() {
        // TODO unsubscribe MasterRegistryDataListener
        deregister();
    }

    /**
     * remove master node path
     *
     * @param path node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeMasterNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);

        if (StringUtils.isEmpty(path)) {
            logger.error("server down error: empty path: {}, nodeType:{}", path, nodeType);
            return;
        }

        String serverHost = registryClient.getHostByEventDataPath(path);
        if (StringUtils.isEmpty(serverHost)) {
            logger.error("server down error: unknown path: {}, nodeType:{}", path, nodeType);
            return;
        }

        try {
            if (!registryClient.exists(path)) {
                logger.info("path: {} not exists", path);
            }
            // failover server
            if (failover) {
                failoverService.failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed, host:{}", nodeType, serverHost, e);
        }
    }

    /**
     * remove worker node path
     *
     * @param path     node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeWorkerNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);
        try {
            String serverHost = null;
            if (!StringUtils.isEmpty(path)) {
                serverHost = registryClient.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    logger.error("server down error: unknown path: {}", path);
                    return;
                }
                if (!registryClient.exists(path)) {
                    logger.info("path: {} not exists", path);
                }
            }
            // failover server
            if (failover) {
                failoverService.failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed", nodeType, e);
        }
    }

    /**
     * Registry the current master server itself to registry.
     */
    void registry() {
        logger.info("Master node : {} registering to registry center", masterConfig.getMasterAddress());
        String masterRegistryPath = masterConfig.getMasterRegistryPath();

        // remove before persist
        registryClient.remove(masterRegistryPath);
        registryClient.persistEphemeral(masterRegistryPath, JSONUtils.toJsonString(masterHeartBeatTask.getHeartBeat()));

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.MASTER)) {
            logger.warn("The current master server node:{} cannot find in registry", NetUtils.getHost());
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting master failover remove
        ThreadUtils.sleep(SLEEP_TIME_MILLIS);

        masterHeartBeatTask.start();
        logger.info("Master node : {} registered to registry center successfully", masterConfig.getMasterAddress());

    }

    public void deregister() {
        try {
            registryClient.remove(masterConfig.getMasterRegistryPath());
            logger.info("Master node : {} unRegistry to register center.", masterConfig.getMasterAddress());
            if (masterHeartBeatTask != null) {
                masterHeartBeatTask.shutdown();
            }
            registryClient.close();
        } catch (Exception e) {
            logger.error("MasterServer remove registry path exception ", e);
        }
    }

}
