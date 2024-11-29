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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.config.WorkerServerLoadProtection;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerHeartBeatTask extends BaseHeartBeatTask<WorkerHeartBeat> {

    private final WorkerConfig workerConfig;
    private final RegistryClient registryClient;

    private final MetricsProvider metricsProvider;

    private final int processId;

    private final ITaskExecutorContainer taskExecutorContainer;

    public WorkerHeartBeatTask(@NonNull WorkerConfig workerConfig,
                               @NonNull MetricsProvider metricsProvider,
                               @NonNull RegistryClient registryClient,
                               @NonNull ITaskExecutorContainer taskExecutorContainer) {
        super("WorkerHeartBeatTask", workerConfig.getMaxHeartbeatInterval().toMillis());
        this.metricsProvider = metricsProvider;
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.taskExecutorContainer = taskExecutorContainer;
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public WorkerHeartBeat getHeartBeat() {
        SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
        ServerStatus serverStatus = getServerStatus(systemMetrics, workerConfig, taskExecutorContainer);

        return WorkerHeartBeat.builder()
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .jvmCpuUsage(systemMetrics.getJvmCpuUsagePercentage())
                .cpuUsage(systemMetrics.getSystemCpuUsagePercentage())
                .jvmMemoryUsage(systemMetrics.getJvmMemoryUsedPercentage())
                .memoryUsage(systemMetrics.getSystemMemoryUsedPercentage())
                .diskUsage(systemMetrics.getDiskUsedPercentage())
                .processId(processId)
                .workerHostWeight(workerConfig.getHostWeight())
                .threadPoolUsage(taskExecutorContainer.slotUsage())
                .serverStatus(serverStatus)
                .host(NetUtils.getHost())
                .port(workerConfig.getListenPort())
                .build();
    }

    @Override
    public void writeHeartBeat(final WorkerHeartBeat workerHeartBeat) {
        final String failoverNodePath = RegistryUtils.getFailoverFinishedNodePath(workerHeartBeat);
        if (registryClient.exists(failoverNodePath)) {
            log.warn("The worker: {} is under {}, means it has been failover will close myself",
                    workerHeartBeat,
                    failoverNodePath);
            registryClient
                    .getStoppable()
                    .stop("The worker exist: " + failoverNodePath + ", means it has been failover will close myself");
            return;
        }
        String workerHeartBeatJson = JSONUtils.toJsonString(workerHeartBeat);
        String workerRegistryPath = workerConfig.getWorkerRegistryPath();
        registryClient.persistEphemeral(workerRegistryPath, workerHeartBeatJson);
        WorkerServerMetrics.incWorkerHeartbeatCount();
        log.debug(
                "Success write worker group heartBeatInfo into registry, workerRegistryPath: {} workerHeartBeatInfo: {}",
                workerRegistryPath,
                workerHeartBeatJson);
    }

    private ServerStatus getServerStatus(SystemMetrics systemMetrics,
                                         WorkerConfig workerConfig,
                                         ITaskExecutorContainer taskExecutorContainer) {
        if (taskExecutorContainer.slotUsage() == 1) {
            return ServerStatus.BUSY;
        }
        WorkerServerLoadProtection serverLoadProtection = workerConfig.getServerLoadProtection();
        return serverLoadProtection.isOverload(systemMetrics) ? ServerStatus.BUSY : ServerStatus.NORMAL;
    }
}
