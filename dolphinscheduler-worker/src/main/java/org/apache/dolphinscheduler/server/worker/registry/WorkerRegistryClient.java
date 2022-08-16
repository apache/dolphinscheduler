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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

/**
 * worker registry
 */
@Service
public class WorkerRegistryClient implements AutoCloseable {

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

    @Autowired
    private WorkerConnectStrategy workerConnectStrategy;

    /**
     * worker startup time, ms
     */
    private long startupTime;

    private Set<String> workerGroups;

    @PostConstruct
    public void initWorkRegistry() {
        this.workerGroups = workerConfig.getGroups();
        this.startupTime = System.currentTimeMillis();
        this.heartBeatExecutor =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    public void start() {
        try {
            registry();
            registryClient.addConnectionStateListener(
                    new WorkerConnectionStateListener(workerConfig, registryClient, workerConnectStrategy));
        } catch (Exception ex) {
            throw new RegistryException("Worker registry client start up error", ex);
        }
    }

    /**
     * registry
     */
    private void registry() {
        String address = NetUtils.getAddr(workerConfig.getListenPort());
        Set<String> workerZkPaths = getWorkerZkPaths();
        long workerHeartbeatInterval = workerConfig.getHeartbeatInterval().getSeconds();

        HeartBeatTask heartBeatTask = new HeartBeatTask(startupTime,
                workerConfig.getMaxCpuLoadAvg(),
                workerConfig.getReservedMemory(),
                workerConfig.getHostWeight(),
                workerZkPaths,
                registryClient,
                workerConfig.getExecThreads(),
                workerManagerThread.getThreadPoolQueueSize());

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

        this.heartBeatExecutor.scheduleWithFixedDelay(heartBeatTask, workerHeartbeatInterval, workerHeartbeatInterval,
                TimeUnit.SECONDS);
        logger.info("worker node : {} heartbeat interval {} s", address, workerHeartbeatInterval);
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
            if (Strings.isNullOrEmpty(workGroup)) {
                workGroup = DEFAULT_WORKER_GROUP;
            }
            // trim and lower case is need
            workerPathJoiner.add(workGroup.trim().toLowerCase());
            workerPathJoiner.add(address);
            workerPaths.add(workerPathJoiner.toString());
        }
        return workerPaths;
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

    @Override
    public void close() throws IOException {
        if (heartBeatExecutor != null) {
            heartBeatExecutor.shutdownNow();
            logger.info("Heartbeat executor shutdown");
        }
        registryClient.close();
        logger.info("registry client closed");
    }

}
