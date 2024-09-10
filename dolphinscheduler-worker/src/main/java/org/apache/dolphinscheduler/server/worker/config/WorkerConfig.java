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

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

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
    private Duration maxHeartbeatInterval = Duration.ofSeconds(10);
    private int hostWeight = 100;
    private WorkerServerLoadProtection serverLoadProtection = new WorkerServerLoadProtection();
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    /**
     * This field doesn't need to set at config file, it will be calculated by workerIp:listenPort
     */
    private String workerAddress;
    private String workerRegistryPath;

    private TaskExecuteThreadsFullPolicy taskExecuteThreadsFullPolicy = TaskExecuteThreadsFullPolicy.REJECT;

    private TenantConfig tenantConfig = new TenantConfig();

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
        if (workerConfig.getMaxHeartbeatInterval().getSeconds() <= 0) {
            errors.rejectValue("max-heartbeat-interval", null, "shoule be a valid duration");
        }
        if (StringUtils.isEmpty(workerConfig.getWorkerAddress())) {
            workerConfig.setWorkerAddress(NetUtils.getAddr(workerConfig.getListenPort()));
        }

        workerConfig.setWorkerRegistryPath(
                RegistryNodeType.WORKER.getRegistryPath() + "/" + workerConfig.getWorkerAddress());
        printConfig();
    }

    private void printConfig() {
        String config =
                "\n****************************Worker Configuration**************************************" +
                        "\n  listen-port -> " + listenPort +
                        "\n  exec-threads -> " + execThreads +
                        "\n  max-heartbeat-interval -> " + maxHeartbeatInterval +
                        "\n  host-weight -> " + hostWeight +
                        "\n  tenantConfig -> " + tenantConfig +
                        "\n  server-load-protection -> " + serverLoadProtection +
                        "\n  registry-disconnect-strategy -> " + registryDisconnectStrategy +
                        "\n  task-execute-threads-full-policy: " + taskExecuteThreadsFullPolicy +
                        "\n  address -> " + workerAddress +
                        "\n  registry-path: " + workerRegistryPath +
                        "\n****************************Worker Configuration**************************************";
        log.info(config);
    }
}
