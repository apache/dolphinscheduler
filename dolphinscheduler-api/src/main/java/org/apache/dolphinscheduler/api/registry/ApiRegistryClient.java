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

package org.apache.dolphinscheduler.api.registry;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_APIS;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

@Service
@ConditionalOnProperty(prefix = "register",name = "enabled",havingValue = "true")
public class ApiRegistryClient implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(ApiRegistryClient.class);

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private RegistryClient registryClient;

    private ScheduledExecutorService heartBeatExecutor;

    private long startupTime;

    private String apiAddress;

    public void init() {
        this.apiAddress = apiConfig.getApiAddress();
        this.startupTime = System.currentTimeMillis();
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    public void registry() {
        logger.info("Api node : {} registering to registry center", apiAddress);
        String localNodePath = getCurrentNodePath();
        Duration apiHeartbeatInterval = apiConfig.getHeartbeatInterval();

        HeartBeatTask heartBeatTask = new HeartBeatTask(startupTime,
                                                        Sets.newHashSet(localNodePath),
                                                        Constants.API_TYPE,
                                                        registryClient,
                                                        apiConfig.getHeartbeatErrorThreshold());

        // remove before persist
        registryClient.remove(localNodePath);
        registryClient.persistEphemeral(localNodePath, heartBeatTask.getHeartBeatInfo());

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.API)) {
            logger.warn("The current api server node:{} cannot find in registry", NetUtils.getHost());
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting api failover remove
        ThreadUtils.sleep(SLEEP_TIME_MILLIS);

        // delete dead server
        registryClient.handleDeadServer(Collections.singleton(localNodePath), NodeType.API, Constants.DELETE_OP);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, 0L, apiHeartbeatInterval.getSeconds(), TimeUnit.SECONDS);
        logger.info("Api node : {} registered to registry center successfully with heartBeatInterval : {}s", apiAddress, apiHeartbeatInterval);
    }

    private String getCurrentNodePath() {
        return REGISTRY_DOLPHINSCHEDULER_APIS + "/" + apiAddress;
    }

    public String getApiAddress() {
        return this.apiAddress;
    }

    public void deregister() {
        try {
            String localNodePath = getCurrentNodePath();
            registryClient.remove(localNodePath);
            logger.info("Api node : {} unRegistry to register center.", apiAddress);
            heartBeatExecutor.shutdown();
            logger.info("ApiServer heartbeat executor shutdown");
            registryClient.close();
        } catch (Exception e) {
            logger.error("ApiServer remove registry path exception ", e);
        }
    }

    @Override
    public void close() throws IOException {
        deregister();
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }
}
