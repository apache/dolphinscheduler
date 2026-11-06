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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMetrics;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;
import org.apache.dolphinscheduler.extract.alert.IAlertMetadataService;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IMasterMetadataService;
import org.apache.dolphinscheduler.extract.master.IWorkflowExecutorQueryClient;
import org.apache.dolphinscheduler.extract.master.dto.WorkflowExecutorDTO;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowExecutorQueryRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowExecutorQueryResponse;
import org.apache.dolphinscheduler.extract.worker.ITaskExecutorQueryClient;
import org.apache.dolphinscheduler.extract.worker.IWorkerMetadataService;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutorQueryRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutorQueryResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.task.executor.dto.TaskExecutorDTO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
@Slf4j
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService, AutoCloseable {

    @Autowired
    private DatabaseMonitor databaseMonitor;

    @Autowired
    private RegistryClient registryClient;

    private final ExecutorService heartbeatQueryExecutor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("ApiHeartbeatQuery-%d"));

    /**
     * query database state
     *
     * @param loginUser login user
     * @return data base state
     */
    @Override
    public List<DatabaseMetrics> queryDatabaseState(User loginUser) {
        return Lists.newArrayList(databaseMonitor.getDatabaseMetrics());
    }

    @Override
    public List<Server> listServer(RegistryNodeType nodeType) {
        List<Server> servers = registryClient.getServerList(nodeType);
        if (nodeType != RegistryNodeType.MASTER && nodeType != RegistryNodeType.WORKER
                && nodeType != RegistryNodeType.ALERT_SERVER) {
            return servers;
        }
        List<CompletableFuture<Server>> queries = servers.stream()
                .map(server -> CompletableFuture.supplyAsync(() -> queryHeartbeat(nodeType, server),
                        heartbeatQueryExecutor))
                .collect(Collectors.toList());
        return queries.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private Server queryHeartbeat(RegistryNodeType type, Server server) {
        server.setHeartBeatInfo(null);
        server.setLastHeartbeatTime(null);
        server.setMetadataStale(true);
        String address = server.getHost() + ":" + server.getPort();
        try {
            BaseHeartBeat heartbeat = fetchHeartbeat(type, address);
            if (heartbeat == null || heartbeat.getProcessId() != server.getId()
                    || heartbeat.getStartupTime() != server.getCreateTime().getTime()
                    || !address.equals(heartbeat.getHost() + ":" + heartbeat.getPort())) {
                throw new IllegalStateException("Heartbeat does not match the registered instance");
            }
            server.setHeartBeatInfo(JSONUtils.toJsonString(heartbeat));
            server.setLastHeartbeatTime(new Date(heartbeat.getReportTime()));
            server.setMetadataStale(false);
        } catch (Exception ex) {
            log.debug("Failed to query {} heartbeat from {}", type, address, ex);
        }
        return server;
    }

    BaseHeartBeat fetchHeartbeat(RegistryNodeType type, String address) {
        switch (type) {
            case MASTER:
                return Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata();
            case WORKER:
                return Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
            case ALERT_SERVER:
                return Clients.withService(IAlertMetadataService.class).withHost(address).getServerMetadata();
            default:
                throw new IllegalArgumentException("Unsupported heartbeat type: " + type);
        }
    }

    @Override
    @PreDestroy
    public void close() {
        // Let queued queries finish so their waiting HTTP requests can complete.
        heartbeatQueryExecutor.shutdown();
    }

    @Override
    public List<WorkflowExecutorDTO> queryWorkflowExecutors(User loginUser, String masterAddress) {
        if (!loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        WorkflowExecutorQueryResponse response = Clients
                .withService(IWorkflowExecutorQueryClient.class)
                .withHost(masterAddress)
                .queryWorkflowExecutors(new WorkflowExecutorQueryRequest());
        if (!response.isSuccess()) {
            throw new ServiceException(response.getMessage());
        }
        return response.getWorkflowExecutors();
    }

    @Override
    public List<TaskExecutorDTO> queryTaskExecutors(User loginUser, String serverAddress) {
        if (!loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }
        TaskExecutorQueryResponse response = Clients
                .withService(ITaskExecutorQueryClient.class)
                .withHost(serverAddress)
                .queryTaskInstances(new TaskExecutorQueryRequest());
        if (!response.isSuccess()) {
            throw new ServiceException(response.getMessage());
        }
        return response.getTaskExecutors();
    }

}
