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

package org.apache.dolphinscheduler.server.master.registry;

import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SelfFailoverMonitor;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.metrics.MasterServerMetrics;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>DolphinScheduler master register client, used to connect to registry and hand the registry events.
 * <p>The Master registers stable process identity and serves heartbeat snapshots on demand.
 */
@Component
@Slf4j
public class MasterRegistryClient implements AutoCloseable, IMasterMetadataService {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private MasterServerLoadProtection masterServerLoadProtection;

    @Autowired
    private MetricsProvider metricsProvider;

    @Autowired
    private MasterCoordinator masterCoordinator;

    private final int processId = OSUtils.getProcessID();

    private SelfFailoverMonitor selfFailoverMonitor;

    public void start() {
        try {
            // master registry
            registry();
            registryClient.addConnectionStateListener(new MasterConnectionStateListener(registryClient));
        } catch (Exception e) {
            throw new RegistryException("Master registry client start up error", e);
        }
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    @Override
    public void close() {
        // TODO unsubscribe MasterRegistryDataListener
        if (selfFailoverMonitor != null) {
            selfFailoverMonitor.close();
        }
        if (registryClient.isConnected()) {
            deregister();
        }
        log.info("Closed MasterRegistryClient");
    }

    /**
     * Registry the current master server itself to registry.
     */
    void registry() {
        log.info("Master node : {} registering to registry center", masterConfig.getMasterAddress());
        String masterRegistryPath = masterConfig.getMasterRegistryPath();
        ServerIdentity identity = ServerIdentity.fromHeartBeat(getServerMetadata());

        // remove before persist
        registryClient.remove(masterRegistryPath);
        registryClient.persistEphemeral(masterRegistryPath,
                JSONUtils.toJsonString(identity));

        while (!registryClient.checkNodeExists(NetUtils.getHost(), RegistryNodeType.MASTER)) {
            log.warn("The current master server node:{} cannot find in registry", NetUtils.getHost());
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        selfFailoverMonitor = new SelfFailoverMonitor(registryClient, identity,
                masterConfig.getMaxHeartbeatInterval());
        selfFailoverMonitor.start();
        log.info("Master node : {} registered to registry center successfully", masterConfig.getMasterAddress());

    }

    public void deregister() {
        try {
            registryClient.remove(masterConfig.getMasterRegistryPath());
            log.info("Master node : {} unRegistry to register center.", masterConfig.getMasterAddress());
            if (selfFailoverMonitor != null) {
                selfFailoverMonitor.close();
            }
            registryClient.close();
        } catch (Exception e) {
            log.error("MasterServer remove registry path exception ", e);
        }
    }

    @Override
    public MasterHeartBeat getServerMetadata() {
        MasterServerMetrics.incMasterHeartbeatRequestCount();
        SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
        return MasterHeartBeat.builder()
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .jvmCpuUsage(systemMetrics.getJvmCpuUsagePercentage())
                .cpuUsage(systemMetrics.getSystemCpuUsagePercentage())
                .jvmMemoryUsage(systemMetrics.getJvmMemoryUsedPercentage())
                .jvmHeapUsed(systemMetrics.getJvmHeapUsed())
                .jvmHeapMax(systemMetrics.getJvmHeapMax())
                .jvmNonHeapUsed(systemMetrics.getJvmNonHeapUsed())
                .jvmNonHeapMax(systemMetrics.getJvmNonHeapMax())
                .memoryUsage(systemMetrics.getSystemMemoryUsedPercentage())
                .diskUsage(systemMetrics.getDiskUsedPercentage())
                .processId(processId)
                .serverStatus(
                        masterServerLoadProtection.isOverload(systemMetrics) ? ServerStatus.BUSY : ServerStatus.NORMAL)
                .host(NetUtils.getHost())
                .port(masterConfig.getListenPort())
                .isCoordinator(masterCoordinator.isActive())
                .build();
    }

    public boolean isAvailable() {
        return registryClient.isConnected();
    }
}
