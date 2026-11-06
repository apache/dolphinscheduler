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
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertRegistryClient implements AutoCloseable {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private AlertConfig alertConfig;

    @Autowired
    private MetricsProvider metricsProvider;

    private final ScheduledExecutorService registryUpdater =
            ThreadUtils.newSingleDaemonScheduledExecutorService("AlertRegistryUpdate");

    private final int processId = OSUtils.getProcessID();
    private final long startupTime = System.currentTimeMillis();
    private ScheduledFuture<?> registrationFuture;
    private long lastRegistrationTime;
    private boolean closed;

    @Autowired
    private AlertHAServer alertHAServer;

    public synchronized void start() {
        log.info("AlertRegistryClient starting...");
        registrationFuture = registryUpdater.scheduleWithFixedDelay(this::updateRegistration, 0, 1, TimeUnit.SECONDS);
        log.info("AlertRegistryClient started...");
    }

    private synchronized void updateRegistration() {
        if (closed || System.currentTimeMillis() - lastRegistrationTime < alertConfig.getMaxHeartbeatInterval()
                .toMillis()) {
            return;
        }
        try {
            String path = RegistryNodeType.ALERT_SERVER.getRegistryPath() + "/" + alertConfig.getAlertServerAddress();
            registryClient.persistEphemeral(path, JSONUtils.toJsonString(getHeartBeat()));
            lastRegistrationTime = System.currentTimeMillis();
            AlertServerMetrics.incAlertHeartbeatCount();
        } catch (Exception ex) {
            log.error("Failed to update Alert Server registration", ex);
        }
    }

    private AlertServerHeartBeat getHeartBeat() {
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
                .host(NetUtils.getHost())
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
        if (registrationFuture != null) {
            registrationFuture.cancel(false);
        }
        registryUpdater.shutdown();
        log.info("AlertRegistryClient closed...");
    }

    public boolean isAvailable() {
        return registryClient.isConnected();
    }
}
