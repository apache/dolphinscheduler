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

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.ServerRegistration;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMetrics;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;
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
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.task.executor.dto.TaskExecutorDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import javax.annotation.PostConstruct;
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

    @Autowired
    private ApiConfig apiConfig;

    private final Map<String, Server> servers = new HashMap<>();
    private final Map<Server, Long> heartbeatProgressNanos = new IdentityHashMap<>();
    private final Set<Server> inFlight = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Runnable> pendingEvents = new ArrayList<>();
    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1,
            ThreadUtils.newDaemonThreadFactory("ApiHeartbeatTimer-%d"));
    private ExecutorService executor = Executors.newFixedThreadPool(8,
            ThreadUtils.newDaemonThreadFactory("ApiHeartbeatFetch-%d"));
    private LongSupplier nanoTime = System::nanoTime;
    private long intervalMillis;
    private boolean initializing;
    private boolean reconciling;
    private boolean reconcileRequired;
    private boolean closed;

    @PostConstruct
    public void start() {
        intervalMillis = apiConfig.getHeartbeatFetchInterval().toMillis();
        synchronized (this) {
            initializing = true;
        }
        for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
            registryClient.subscribe(type.getRegistryPath(), new SubscribeListener() {

                @Override
                public void notify(Event event) {
                    onRegistryEvent(type, event);
                }

                @Override
                public SubscribeScope getSubscribeScope() {
                    return SubscribeScope.CHILDREN_ONLY;
                }
            });
        }
        registryClient.addConnectionStateListener(state -> {
            synchronized (this) {
                if (!closed && state == ConnectionState.RECONNECTED) {
                    reconcileRequired = true;
                }
            }
        });
        synchronized (this) {
            reconciling = true;
        }
        reconcile();
        timer.scheduleWithFixedDelay(this::refreshHeartbeats, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void reconcile() {
        try {
            Map<RegistryNodeType, Map<String, ServerRegistration>> snapshot = new EnumMap<>(RegistryNodeType.class);
            for (RegistryNodeType type : new RegistryNodeType[]{RegistryNodeType.MASTER, RegistryNodeType.WORKER}) {
                Map<String, ServerRegistration> registrations = new HashMap<>();
                // Failed or partial reads must not be interpreted as departed members.
                for (String address : registryClient.getServerNodeSet(type)) {
                    ServerRegistration registration = JSONUtils.parseObject(
                            registryClient.get(type.getRegistryPath() + "/" + address), ServerRegistration.class);
                    if (registration == null || !address.equals(registration.getAddress())) {
                        throw new IllegalStateException("Invalid registration: " + address);
                    }
                    registrations.put(address, registration);
                }
                snapshot.put(type, registrations);
            }
            synchronized (this) {
                if (!closed) {
                    snapshot.forEach(this::applySnapshot);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to reconcile server registrations", ex);
            synchronized (this) {
                reconcileRequired = true;
            }
        } finally {
            synchronized (this) {
                reconciling = false;
                initializing = false;
                if (!closed) {
                    pendingEvents.forEach(Runnable::run);
                }
                pendingEvents.clear();
            }
        }
    }

    private synchronized void onRegistryEvent(RegistryNodeType type, Event event) {
        if (closed) {
            return;
        }
        ServerRegistration registration = JSONUtils.parseObject(event.getEventData(), ServerRegistration.class);
        if (registration == null) {
            log.error("Invalid registration event: {}", event);
            return;
        }
        Runnable change = () -> {
            if (event.getType() == Event.Type.REMOVE) {
                remove(type, registration);
            } else {
                add(type, registration);
            }
        };
        if (initializing) {
            pendingEvents.add(change);
        } else {
            change.run();
        }
    }

    private void applySnapshot(RegistryNodeType type, Map<String, ServerRegistration> registrations) {
        for (Server server : new ArrayList<>(servers.values())) {
            if (server.getServerDirectory().startsWith(type.getRegistryPath() + "/")
                    && !registrations.containsKey(address(server))) {
                servers.remove(server.getServerDirectory());
                heartbeatProgressNanos.remove(server);
            }
        }
        registrations.values().forEach(registration -> add(type, registration));
    }

    private String address(Server server) {
        return server.getHost() + ":" + server.getPort();
    }

    private boolean matches(Server server, ServerRegistration registration) {
        return server != null && server.getId() == registration.getProcessId()
                && server.getCreateTime().getTime() == registration.getStartupTime()
                && address(server).equals(registration.getAddress());
    }

    private void add(RegistryNodeType type, ServerRegistration registration) {
        String path = type.getRegistryPath() + "/" + registration.getAddress();
        Server previous = servers.get(path);
        if (matches(previous, registration)) {
            return;
        }
        heartbeatProgressNanos.remove(previous);
        Server server = new Server();
        server.setId(registration.getProcessId());
        server.setHost(registration.getHost());
        server.setPort(registration.getPort());
        server.setCreateTime(new Date(registration.getStartupTime()));
        server.setServerDirectory(path);
        servers.put(path, server);
    }

    private void remove(RegistryNodeType type, ServerRegistration registration) {
        String path = type.getRegistryPath() + "/" + registration.getAddress();
        Server server = servers.get(path);
        if (matches(server, registration)) {
            servers.remove(path);
            heartbeatProgressNanos.remove(server);
        }
    }

    synchronized void refreshHeartbeats() {
        if (closed) {
            return;
        }
        if (reconcileRequired && !reconciling) {
            reconcileRequired = false;
            reconciling = true;
            initializing = true;
            executor.execute(this::reconcile);
        }
        for (Server server : servers.values()) {
            if (inFlight.add(server)) {
                RegistryNodeType type =
                        server.getServerDirectory().startsWith(RegistryNodeType.MASTER.getRegistryPath() + "/")
                                ? RegistryNodeType.MASTER
                                : RegistryNodeType.WORKER;
                executor.execute(() -> refresh(type, server));
            }
        }
    }

    private void refresh(RegistryNodeType type, Server expected) {
        try {
            synchronized (this) {
                if (closed || servers.get(expected.getServerDirectory()) != expected) {
                    return;
                }
            }
            BaseHeartBeat heartbeat = fetchHeartbeat(type, address(expected));
            if (!matches(expected, ServerRegistration.fromHeartBeat(heartbeat))
                    || (type == RegistryNodeType.MASTER && !(heartbeat instanceof MasterHeartBeat))
                    || (type == RegistryNodeType.WORKER && !(heartbeat instanceof WorkerHeartBeat))) {
                throw new IllegalStateException("Heartbeat does not match the registered instance");
            }
            synchronized (this) {
                if (closed || servers.get(expected.getServerDirectory()) != expected) {
                    return;
                }
                if (expected.getLastHeartbeatTime() == null
                        || expected.getLastHeartbeatTime().getTime() != heartbeat.getReportTime()) {
                    heartbeatProgressNanos.put(expected, nanoTime.getAsLong());
                }
                expected.setHeartBeatInfo(JSONUtils.toJsonString(heartbeat));
                expected.setLastHeartbeatTime(new Date(heartbeat.getReportTime()));
            }
        } catch (Exception ex) {
            log.debug("Failed to fetch {} heartbeat from {}", type, address(expected), ex);
        } finally {
            synchronized (this) {
                inFlight.remove(expected);
            }
        }
    }

    BaseHeartBeat fetchHeartbeat(RegistryNodeType type, String address) {
        return type == RegistryNodeType.MASTER
                ? Clients.withService(IMasterMetadataService.class).withHost(address).getServerMetadata()
                : Clients.withService(IWorkerMetadataService.class).withHost(address).getServerMetadata();
    }

    @Override
    @PreDestroy
    public synchronized void close() {
        closed = true;
        timer.shutdownNow();
        executor.shutdownNow();
        inFlight.clear();
        pendingEvents.clear();
        servers.clear();
        heartbeatProgressNanos.clear();
    }

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
        if (nodeType != RegistryNodeType.MASTER && nodeType != RegistryNodeType.WORKER) {
            return registryClient.getServerList(nodeType);
        }
        synchronized (this) {
            List<Server> result = new ArrayList<>();
            for (Server server : servers.values()) {
                if (!server.getServerDirectory().startsWith(nodeType.getRegistryPath() + "/")) {
                    continue;
                }
                Server copy = new Server();
                copy.setId(server.getId());
                copy.setHost(server.getHost());
                copy.setPort(server.getPort());
                copy.setServerDirectory(server.getServerDirectory());
                copy.setCreateTime(new Date(server.getCreateTime().getTime()));
                copy.setHeartBeatInfo(server.getHeartBeatInfo());
                if (server.getLastHeartbeatTime() != null) {
                    copy.setLastHeartbeatTime(new Date(server.getLastHeartbeatTime().getTime()));
                }
                Long progress = heartbeatProgressNanos.get(server);
                copy.setMetadataStale(progress == null
                        || nanoTime.getAsLong() - progress >= TimeUnit.MILLISECONDS.toNanos(3 * intervalMillis));
                result.add(copy);
            }
            return result;
        }
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
