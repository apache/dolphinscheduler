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

package org.apache.dolphinscheduler.alert.registry;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.metrics.AlertServerMetrics;
import org.apache.dolphinscheduler.alert.service.AlertHAServer;
import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.extract.alert.IAlertMetadataService;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertRegistryClient implements AutoCloseable, IAlertMetadataService {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private AlertConfig alertConfig;

    @Autowired
    private MetricsProvider metricsProvider;

    private final int processId = OSUtils.getProcessID();
    private final long startupTime = System.currentTimeMillis();
    private boolean closed;

    @Autowired
    private AlertHAServer alertHAServer;

    public synchronized void start() {
        log.info("AlertRegistryClient starting...");
        registryClient.addConnectionStateListener(state -> {
            if (state == ConnectionState.RECONNECTED) {
                register();
            }
        });
        register();
        log.info("AlertRegistryClient started...");
    }

    private synchronized void register() {
        if (closed) {
            return;
        }
        Host host = new Host(Host.of(alertConfig.getAlertServerAddress()).getIp(), alertConfig.getPort());
        ServerIdentity identity = new ServerIdentity();
        identity.setHost(host.getIp());
        identity.setPort(host.getPort());
        identity.setProcessId(processId);
        identity.setStartupTime(startupTime);
        registryClient.persistEphemeral(RegistryNodeType.ALERT_SERVER.getRegistryPath() + "/" + host.getAddress(),
                JSONUtils.toJsonString(identity));
    }

    @Override
    public AlertServerHeartBeat getServerMetadata() {
        AlertServerMetrics.incAlertHeartbeatRequestCount();
        SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
        return AlertServerHeartBeat.builder()
                .processId(processId)
                .startupTime(startupTime)
                .reportTime(System.currentTimeMillis())
                .jvmCpuUsage(systemMetrics.getJvmCpuUsagePercentage())
                .cpuUsage(systemMetrics.getSystemCpuUsagePercentage())
                .memoryUsage(systemMetrics.getSystemMemoryUsedPercentage())
                .jvmMemoryUsage(systemMetrics.getJvmMemoryUsedPercentage())
                .diskUsage(systemMetrics.getDiskUsedPercentage())
                .serverStatus(ServerStatus.NORMAL)
                .isActive(alertHAServer.isActive())
                .host(Host.of(alertConfig.getAlertServerAddress()).getIp())
                .port(alertConfig.getPort())
                .build();
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        log.info("AlertRegistryClient closing...");
        closed = true;
        log.info("AlertRegistryClient closed...");
    }

    public boolean isAvailable() {
        return registryClient.isConnected();
    }
}
