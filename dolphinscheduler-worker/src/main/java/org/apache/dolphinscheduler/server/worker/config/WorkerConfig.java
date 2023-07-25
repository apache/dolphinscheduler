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

import static org.apache.dolphinscheduler.common.constants.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "worker")
@Slf4j
public class WorkerConfig implements Validator {

    private int listenPort = 1234;
    private int execThreads = 10;
    private Duration heartbeatInterval = Duration.ofSeconds(10);
    private int hostWeight = 100;
    private boolean tenantAutoCreate = true;
    private boolean tenantDistributedUser = false;
    private int maxCpuLoadAvg = -1;
    private double reservedMemory = 0.1;
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    private NettyClientConfig workerRpcClientConfig = new NettyClientConfig();
    private NettyServerConfig workerRpcServerConfig = new NettyServerConfig();

    /**
     * This field doesn't need to set at config file, it will be calculated by workerIp:listenPort
     */
    private String workerAddress;
    private String workerRegistryPath;

    private TaskExecuteThreadsFullPolicy taskExecuteThreadsFullPolicy = TaskExecuteThreadsFullPolicy.REJECT;

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
        if (StringUtils.isEmpty(workerConfig.getWorkerAddress())) {
            workerConfig.setWorkerAddress(NetUtils.getAddr(workerConfig.getListenPort()));
        }

        workerConfig.setWorkerRegistryPath(REGISTRY_DOLPHINSCHEDULER_WORKERS + "/" + workerConfig.getWorkerAddress());
        printConfig();
    }

    private void printConfig() {
        log.info("Worker config: listenPort -> {}", listenPort);
        log.info("Worker config: execThreads -> {}", execThreads);
        log.info("Worker config: heartbeatInterval -> {}", heartbeatInterval);
        log.info("Worker config: hostWeight -> {}", hostWeight);
        log.info("Worker config: tenantAutoCreate -> {}", tenantAutoCreate);
        log.info("Worker config: tenantDistributedUser -> {}", tenantDistributedUser);
        log.info("Worker config: maxCpuLoadAvg -> {}", maxCpuLoadAvg);
        log.info("Worker config: reservedMemory -> {}", reservedMemory);
        log.info("Worker config: registryDisconnectStrategy -> {}", registryDisconnectStrategy);
        log.info("Worker config: workerAddress -> {}", workerAddress);
        log.info("Worker config: workerRegistryPath: {}", workerRegistryPath);
        log.info("Worker config: taskExecuteThreadsFullPolicy: {}", taskExecuteThreadsFullPolicy);
    }
}
