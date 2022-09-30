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

package org.apache.dolphinscheduler.server.worker.config;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;

import java.time.Duration;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "worker")
public class WorkerConfig implements Validator {

    private Logger logger = LoggerFactory.getLogger(WorkerConfig.class);

    private int listenPort = 1234;
    private int execThreads = 10;
    private Duration heartbeatInterval = Duration.ofSeconds(10);
    private int hostWeight = 100;
    private boolean tenantAutoCreate = true;
    private boolean tenantDistributedUser = false;
    private int maxCpuLoadAvg = -1;
    private double reservedMemory = 0.3;
    private String alertListenHost = "localhost";
    private int alertListenPort = 50052;
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    /**
     * This field doesn't need to set at config file, it will be calculated by workerIp:listenPort
     */
    private String workerAddress;
    private String workerRegistryPath;

    @Override
    public boolean supports(Class<?> clazz) {
        return WorkerConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        WorkerConfig workerConfig = (WorkerConfig) target;
        if (workerConfig.getExecThreads() <= 0) {
            errors.rejectValue("exec-threads", null, "should be a positive value");
        }
        if (workerConfig.getHeartbeatInterval().getSeconds() <= 0) {
            errors.rejectValue("heartbeat-interval", null, "shoule be a valid duration");
        }
        if (workerConfig.getMaxCpuLoadAvg() <= 0) {
            workerConfig.setMaxCpuLoadAvg(Runtime.getRuntime().availableProcessors() * 2);
        }
        workerConfig.setWorkerAddress(NetUtils.getAddr(workerConfig.getListenPort()));

        workerConfig.setWorkerRegistryPath(REGISTRY_DOLPHINSCHEDULER_WORKERS + "/" + workerConfig.getWorkerAddress());
        printConfig();
    }

    private void printConfig() {
        logger.info("Worker config: listenPort -> {}", listenPort);
        logger.info("Worker config: execThreads -> {}", execThreads);
        logger.info("Worker config: heartbeatInterval -> {}", heartbeatInterval);
        logger.info("Worker config: hostWeight -> {}", hostWeight);
        logger.info("Worker config: tenantAutoCreate -> {}", tenantAutoCreate);
        logger.info("Worker config: tenantDistributedUser -> {}", tenantDistributedUser);
        logger.info("Worker config: maxCpuLoadAvg -> {}", maxCpuLoadAvg);
        logger.info("Worker config: reservedMemory -> {}", reservedMemory);
        logger.info("Worker config: alertListenHost -> {}", alertListenHost);
        logger.info("Worker config: alertListenPort -> {}", alertListenPort);
        logger.info("Worker config: registryDisconnectStrategy -> {}", registryDisconnectStrategy);
        logger.info("Worker config: workerAddress -> {}", registryDisconnectStrategy);
        logger.info("Worker config: workerRegistryPath: {}", workerRegistryPath);
    }
}
