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

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * worker registry
 */
@Service
public class WorkerRegistryClient {

    private final Logger logger = LoggerFactory.getLogger(WorkerRegistryClient.class);

    /**
     * worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

    /**
     * worker manager
     */
    @Autowired
    private WorkerManagerThread workerManagerThread;

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    @Autowired
    private RegistryClient registryClient;

    /**
     * worker startup time, ms
     */
    private long startupTime;

    private Set<String> workerGroups;

    @PostConstruct
    public void initWorkRegistry() {
        this.workerGroups = workerConfig.getGroups();
        this.startupTime = System.currentTimeMillis();
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     * registry
     */
    public void registry() {
        String address = NetUtils.getAddr(workerConfig.getListenPort());
        Set<String> workerZkPaths = getWorkerZkPaths();
        int workerHeartbeatInterval = workerConfig.getHeartbeatInterval();

        HeartBeatTask heartBeatTask = new HeartBeatTask(startupTime,
                workerConfig.getMaxCpuLoadAvg(),
                workerConfig.getReservedMemory(),
                workerConfig.getHostWeight(),
                workerZkPaths,
                Constants.WORKER_TYPE,
                registryClient,
                workerConfig.getExecThreads(),
                workerManagerThread.getThreadPoolQueueSize()
        );

        for (String workerZKPath : workerZkPaths) {
            // remove before persist
            registryClient.remove(workerZKPath);
            registryClient.persistEphemeral(workerZKPath, heartBeatTask.getHeartBeatInfo());
            logger.info("worker node : {} registry to ZK {} successfully", address, workerZKPath);
        }

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.WORKER)) {
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting master failover remove
        ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);

        // delete dead server
        registryClient.handleDeadServer(workerZkPaths, NodeType.WORKER, Constants.DELETE_OP);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, workerHeartbeatInterval, workerHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("worker node : {} heartbeat interval {} s", address, workerHeartbeatInterval);
    }

    /**
     * remove registry info
     */
    public void unRegistry() throws IOException {
        try {
            String address = getLocalAddress();
            Set<String> workerZkPaths = getWorkerZkPaths();
            for (String workerZkPath : workerZkPaths) {
                registryClient.remove(workerZkPath);
                logger.info("worker node : {} unRegistry from ZK {}.", address, workerZkPath);
            }
        } catch (Exception ex) {
            logger.error("remove worker zk path exception", ex);
        }

        this.heartBeatExecutor.shutdownNow();
        logger.info("heartbeat executor shutdown");

        registryClient.close();
        logger.info("registry client closed");
    }

    /**
     * get worker path
     */
    public Set<String> getWorkerZkPaths() {
        Set<String> workerPaths = Sets.newHashSet();
        String address = getLocalAddress();

        for (String workGroup : this.workerGroups) {
            StringJoiner workerPathJoiner = new StringJoiner(SINGLE_SLASH);
            workerPathJoiner.add(REGISTRY_DOLPHINSCHEDULER_WORKERS);
            if (StringUtils.isEmpty(workGroup)) {
                workGroup = DEFAULT_WORKER_GROUP;
            }
            // trim and lower case is need
            workerPathJoiner.add(workGroup.trim().toLowerCase());
            workerPathJoiner.add(address);
            workerPaths.add(workerPathJoiner.toString());
        }
        return workerPaths;
    }

    public void handleDeadServer(Set<String> nodeSet, NodeType nodeType, String opType) {
        registryClient.handleDeadServer(nodeSet, nodeType, opType);
    }

    /**
     * get local address
     */
    private String getLocalAddress() {
        return NetUtils.getAddr(workerConfig.getListenPort());
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

}
