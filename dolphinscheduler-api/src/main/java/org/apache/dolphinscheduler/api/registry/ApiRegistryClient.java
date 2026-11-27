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

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.ApiServerHeartBeat;
import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.extract.common.IApiMetadataService;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApiRegistryClient implements IApiMetadataService, AutoCloseable {

    private final RegistryClient registryClient;
    private final MetricsProvider metricsProvider;
    private final RpcServer rpcServer;
    private final ServerIdentity identity = new ServerIdentity();
    private int httpPort;
    private boolean started;
    private boolean closed;

    @Autowired
    public ApiRegistryClient(RegistryClient registryClient, ApiConfig config, MetricsProvider metricsProvider) {
        this(registryClient, config, metricsProvider,
                new RpcServer(NettyServerConfig.builder().serverName("ApiMetadataRpcServer")
                        .listenPort(config.getRpcPort()).workerThread(2).build()));
    }

    ApiRegistryClient(RegistryClient registryClient, ApiConfig config, MetricsProvider metricsProvider,
                      RpcServer rpcServer) {
        this.registryClient = registryClient;
        this.metricsProvider = metricsProvider;
        this.rpcServer = rpcServer;
        identity.setHost(NetUtils.getHost());
        identity.setPort(config.getRpcPort());
        identity.setProcessId(OSUtils.getProcessID());
        identity.setStartupTime(System.currentTimeMillis());
    }

    @EventListener
    public synchronized void start(ApplicationReadyEvent event) {
        if (started || closed || !(event.getApplicationContext() instanceof WebServerApplicationContext)) {
            return;
        }
        httpPort = ((WebServerApplicationContext) event.getApplicationContext()).getWebServer().getPort();
        // Export only API metadata, including in the standalone server's shared Spring context.
        rpcServer.registerServerMethodInvokerProvider(this);
        rpcServer.start();
        started = true;
        registryClient.addConnectionStateListener(state -> {
            if (state == ConnectionState.RECONNECTED) {
                register();
            }
        });
        register();
    }

    private synchronized void register() {
        if (started && !closed) {
            registryClient.persistEphemeral(RegistryNodeType.API.getRegistryPath() + "/" + identity.getAddress(),
                    JSONUtils.toJsonString(identity));
        }
    }

    @Override
    public ApiServerHeartBeat getServerMetadata() {
        SystemMetrics metrics = metricsProvider.getSystemMetrics();
        return ApiServerHeartBeat.builder()
                .host(identity.getHost()).port(identity.getPort())
                .processId(identity.getProcessId()).startupTime(identity.getStartupTime())
                .reportTime(System.currentTimeMillis()).httpPort(httpPort)
                .cpuUsage(metrics.getSystemCpuUsagePercentage())
                .memoryUsage(metrics.getSystemMemoryUsedPercentage())
                .jvmCpuUsage(metrics.getJvmCpuUsagePercentage())
                .jvmMemoryUsage(metrics.getJvmMemoryUsedPercentage())
                .diskUsage(metrics.getDiskUsedPercentage())
                .serverStatus(ServerStatus.NORMAL).build();
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            rpcServer.close();
        }
        // The registry session owns the ephemeral identity, just as for the other services.
    }
}
