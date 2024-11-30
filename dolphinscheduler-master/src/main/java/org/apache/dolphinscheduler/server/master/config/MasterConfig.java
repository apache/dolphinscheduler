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

package org.apache.dolphinscheduler.server.master.config;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.cluster.loadbalancer.WorkerLoadBalancerConfigurationProperties;

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
@ConfigurationProperties(prefix = "master")
@Slf4j
public class MasterConfig implements Validator {

    /**
     * The master RPC server listen port.
     */
    private int listenPort = 5678;

    private int workflowEventBusFireThreadCount = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private LogicTaskConfig logicTaskConfig = new LogicTaskConfig();

    /**
     * Master heart beat task execute interval.
     */
    private Duration maxHeartbeatInterval = Duration.ofSeconds(10);

    private MasterServerLoadProtection serverLoadProtection = new MasterServerLoadProtection();

    private Duration workerGroupRefreshInterval = Duration.ofSeconds(10L);

    private CommandFetchStrategy commandFetchStrategy = new CommandFetchStrategy();

    private WorkerLoadBalancerConfigurationProperties workerLoadBalancerConfigurationProperties =
            new WorkerLoadBalancerConfigurationProperties();

    /**
     * The IP address and listening port of the master server in the format 'ip:listenPort'.
     */
    private String masterAddress;

    /**
     * The registry path for the master server in the format '/nodes/master/ip:listenPort'.
     */
    private String masterRegistryPath;

    @Override
    public boolean supports(Class<?> clazz) {
        return MasterConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MasterConfig masterConfig = (MasterConfig) target;
        if (masterConfig.getListenPort() <= 0) {
            errors.rejectValue("listen-port", null, "is invalidated");
        }

        if (masterConfig.getWorkflowEventBusFireThreadCount() <= 0) {
            errors.rejectValue("workflow-event-bus-fire-thread-count", null, "should be a positive value");
        }

        if (masterConfig.getMaxHeartbeatInterval().toMillis() < 0) {
            errors.rejectValue("max-heartbeat-interval", null, "should be a valid duration");
        }

        if (masterConfig.getWorkerGroupRefreshInterval().getSeconds() < 10) {
            errors.rejectValue("worker-group-refresh-interval", null, "should >= 10s");
        }
        if (StringUtils.isEmpty(masterConfig.getMasterAddress())) {
            masterConfig.setMasterAddress(NetUtils.getAddr(masterConfig.getListenPort()));
        }
        commandFetchStrategy.validate(errors);
        workerLoadBalancerConfigurationProperties.validate(errors);

        masterConfig.setMasterRegistryPath(
                RegistryNodeType.MASTER.getRegistryPath() + "/" + masterConfig.getMasterAddress());
        printConfig();
    }

    private void printConfig() {
        String config =
                "\n****************************Master Configuration**************************************" +
                        "\n  listen-port -> " + listenPort +
                        "\n  workflow-event-bus-fire-thread-count -> " + workflowEventBusFireThreadCount +
                        "\n  logic-task-config -> " + logicTaskConfig +
                        "\n  max-heartbeat-interval -> " + maxHeartbeatInterval +
                        "\n  server-load-protection -> " + serverLoadProtection +
                        "\n  master-address -> " + masterAddress +
                        "\n  master-registry-path: " + masterRegistryPath +
                        "\n  worker-group-refresh-interval: " + workerGroupRefreshInterval +
                        "\n  command-fetch-strategy: " + commandFetchStrategy +
                        "\n  worker-load-balancer-configuration-properties: "
                        + workerLoadBalancerConfigurationProperties +
                        "\n****************************Master Configuration**************************************";
        log.info(config);
    }
}
