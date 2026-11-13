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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
@Slf4j
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService, AutoCloseable {

    private static final int HEARTBEAT_QUERY_CONCURRENCY = 8;
    private static final long HEARTBEAT_QUERY_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);

    @Autowired
    private DatabaseMonitor databaseMonitor;

    @Autowired
    private RegistryClient registryClient;

    private final ThreadPoolExecutor heartbeatQueryExecutor = new ThreadPoolExecutor(
            HEARTBEAT_QUERY_CONCURRENCY, HEARTBEAT_QUERY_CONCURRENCY, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(64), ThreadUtils.newDaemonThreadFactory("ApiHeartbeatQuery-%d"),
            new ThreadPoolExecutor.AbortPolicy());

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
        long deadline = System.nanoTime() + HEARTBEAT_QUERY_TIMEOUT_NANOS;
        servers.forEach(server -> {
            server.setHeartBeatInfo(null);
            server.setLastHeartbeatTime(null);
            server.setMetadataStale(true);
        });
        Iterator<Server> pending = servers.iterator();
        Map<FutureTask<BaseHeartBeat>, Server> queries = new HashMap<>();
        BlockingQueue<FutureTask<BaseHeartBeat>> completed = new ArrayBlockingQueue<>(HEARTBEAT_QUERY_CONCURRENCY);
        try {
            while (pending.hasNext() || !queries.isEmpty()) {
                while (pending.hasNext() && queries.size() < HEARTBEAT_QUERY_CONCURRENCY
                        && deadline - System.nanoTime() > 0) {
                    Server server = pending.next();
                    FutureTask<BaseHeartBeat> query = new FutureTask<BaseHeartBeat>(
                            () -> deadline - System.nanoTime() <= 0 ? null : queryHeartbeat(nodeType, server)) {

                        @Override
                        protected void done() {
                            completed.add(this);
                        }
                    };
                    queries.put(query, server);
                    try {
                        heartbeatQueryExecutor.execute(query);
                    } catch (RejectedExecutionException ex) {
                        query.cancel(false);
                        log.debug("Heartbeat query capacity exceeded for {}:{}", server.getHost(), server.getPort());
                    }
                }
                if (queries.isEmpty()) {
                    break;
                }
                FutureTask<BaseHeartBeat> query = completed.poll(Math.max(0, deadline - System.nanoTime()),
                        TimeUnit.NANOSECONDS);
                if (query == null) {
                    break;
                }
                Server server = queries.remove(query);
                try {
                    BaseHeartBeat heartbeat = query.get();
                    if (heartbeat != null) {
                        server.setHeartBeatInfo(JSONUtils.toJsonString(heartbeat));
                        server.setLastHeartbeatTime(new Date(heartbeat.getReportTime()));
                        server.setMetadataStale(false);
                    }
                } catch (CancellationException | ExecutionException ex) {
                    log.debug("Failed to query {} heartbeat from {}:{}", nodeType, server.getHost(), server.getPort(),
                            ex);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            for (FutureTask<BaseHeartBeat> query : queries.keySet()) {
                // Running RPCs retain their transport timeout; only the request thread updates response data.
                query.cancel(false);
                heartbeatQueryExecutor.remove(query);
            }
        }
        return servers;
    }

    private BaseHeartBeat queryHeartbeat(RegistryNodeType type, Server server) {
        String address = server.getHost() + ":" + server.getPort();
        BaseHeartBeat heartbeat = fetchHeartbeat(type, address);
        if (heartbeat == null || heartbeat.getProcessId() != server.getId()
                || heartbeat.getStartupTime() != server.getCreateTime().getTime()
                || !address.equals(heartbeat.getHost() + ":" + heartbeat.getPort())) {
            throw new IllegalStateException("Heartbeat does not match the registered instance");
        }
        return heartbeat;
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
