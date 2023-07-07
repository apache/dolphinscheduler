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

import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.service.FailoverService;
import org.apache.dolphinscheduler.server.master.task.MasterHeartBeatTask;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>DolphinScheduler master register client, used to connect to registry and hand the registry events.
 * <p>When the Master node startup, it will register in registry center. And start a {@link MasterHeartBeatTask} to update its metadata in registry.
 */
@Component
@Slf4j
public class MasterRegistryClient implements AutoCloseable {

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
            registryClient.subscribe(RegistryNodeType.ALL_SERVERS.getRegistryPath(), new MasterRegistryDataListener());
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
     *这段代码是用于从注册表中移除主节点路径的。以下是代码的解释：
     *
     * 整个方法的目的是删除一个主节点路径，并在遇到错误或者开启了失败重试的情况下进行相关处理。
     * @param path     node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeMasterNodePath(String path, RegistryNodeType nodeType, boolean failover) {
        //记录一条信息，表示正在删除指定的节点。
        log.info("{} node deleted : {}", nodeType, path);
        //如果路径参数（path）为空，该方法就不会继续执行，并且会记录一条错误信息。
        if (StringUtils.isEmpty(path)) {
            log.error("server down error: empty path: {}, nodeType:{}", path, nodeType);
            return;
        }
        //通过路径（path）获取主机名（serverHost）。
        String serverHost = registryClient.getHostByEventDataPath(path);
        //如果获取的主机名为空，该方法会停止执行，并记录一条错误信息。
        if (StringUtils.isEmpty(serverHost)) {
            log.error("server down error: unknown path: {}, nodeType:{}", path, nodeType);
            return;
        }

        try {
            //如果指定的路径不存在，就会记录一条信息。
            if (!registryClient.exists(path)) {
                log.info("path: {} not exists", path);
            }
            // failover server
            //如果开启了失败重试（failover），就调用 failoverService.failoverServerWhenDown(serverHost, nodeType) 方法，根据服务器主机名和节点类型实施失败重试。
            if (failover) {
                failoverService.failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            //如果在执行过程中发生异常，就会捕获这个异常，并记录一条包含节点类型、主机名和异常信息的错误信息。
            log.error("{} server failover failed, host:{}", nodeType, serverHost, e);
        }
    }

    /**
     * remove worker node path
     *
     * @param path     node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeWorkerNodePath(String path, RegistryNodeType nodeType, boolean failover) {
        log.info("{} node deleted : {}", nodeType, path);
        try {
            String serverHost = null;
            if (!StringUtils.isEmpty(path)) {
                serverHost = registryClient.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    log.error("server down error: unknown path: {}", path);
                    return;
                }
                if (!registryClient.exists(path)) {
                    log.info("path: {} not exists", path);
                }
            }
            // failover server
            if (failover) {
                failoverService.failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            log.error("{} server failover failed", nodeType, e);
        }
    }

    /**
     * Registry the current master server itself to registry.
     */
    void registry() {
        log.info("Master node : {} registering to registry center", masterConfig.getMasterAddress());
        String masterRegistryPath = masterConfig.getMasterRegistryPath();

        // remove before persist
        registryClient.remove(masterRegistryPath);
        registryClient.persistEphemeral(masterRegistryPath, JSONUtils.toJsonString(masterHeartBeatTask.getHeartBeat()));

        while (!registryClient.checkNodeExists(NetUtils.getHost(), RegistryNodeType.MASTER)) {
            log.warn("The current master server node:{} cannot find in registry", NetUtils.getHost());
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting master failover remove
        ThreadUtils.sleep(SLEEP_TIME_MILLIS);

        masterHeartBeatTask.start();
        log.info("Master node : {} registered to registry center successfully", masterConfig.getMasterAddress());

    }

    public void deregister() {
        try {
            registryClient.remove(masterConfig.getMasterRegistryPath());
            log.info("Master node : {} unRegistry to register center.", masterConfig.getMasterAddress());
            if (masterHeartBeatTask != null) {
                masterHeartBeatTask.shutdown();
            }
            registryClient.close();
        } catch (Exception e) {
            log.error("MasterServer remove registry path exception ", e);
        }
    }

}
