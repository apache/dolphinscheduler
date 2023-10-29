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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.extract.base.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Event.Type;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServerNodeManager implements InitializingBean {

    private final ReentrantReadWriteLock workerGroupLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock workerGroupReadLock = workerGroupLock.readLock();
    private final ReentrantReadWriteLock.WriteLock workerGroupWriteLock = workerGroupLock.writeLock();

    private final ReentrantReadWriteLock workerNodeInfoLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock workerNodeInfoReadLock = workerNodeInfoLock.readLock();
    private final ReentrantReadWriteLock.WriteLock workerNodeInfoWriteLock = workerNodeInfoLock.writeLock();

    private final ReentrantLock masterNodeInfoLock = new ReentrantLock();

    /**
     * worker group nodes, workerGroup -> ips, combining registryWorkerGroupNodes and dbWorkerGroupNodes
     */
    private final ConcurrentHashMap<String, Set<String>> workerGroupNodes = new ConcurrentHashMap<>();

    private final Map<String, MasterHeartBeat> masterNodeInfo = new HashMap<>();

    private final Map<String, WorkerHeartBeat> workerNodeInfo = new HashMap<>();

    /**
     * executor service
     */
    private ScheduledExecutorService executorService;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ListenerEventAlertManager listenerEventAlertManager;

    private final List<WorkerInfoChangeListener> workerInfoChangeListeners = new ArrayList<>();

    private final List<MasterInfoChangeListener> masterInfoChangeListeners = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {

        // load nodes from zookeeper
        refreshNodesAndGroupMappings();

        // init executor service
        executorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ServerNodeManagerExecutor"));
        executorService.scheduleWithFixedDelay(
                new WorkerNodeInfoAndGroupDbSyncTask(),
                0,
                masterConfig.getWorkerGroupRefreshInterval().getSeconds(),
                TimeUnit.SECONDS);

        registryClient.subscribe(RegistryNodeType.MASTER.getRegistryPath(), new MasterDataListener());
        registryClient.subscribe(RegistryNodeType.WORKER.getRegistryPath(), new WorkerDataListener());
    }

    class WorkerNodeInfoAndGroupDbSyncTask implements Runnable {

        @Override
        public void run() {
            try {
                // sync worker node info
                refreshNodesAndGroupMappings();
            } catch (Exception e) {
                log.error("WorkerNodeInfoAndGroupDbSyncTask error:", e);
            }
        }
    }

    /**
     * Refresh master/worker nodes and worker group mapping information
     */
    private void refreshNodesAndGroupMappings() {
        updateWorkerNodes();
        updateWorkerGroupMappings();
        notifyWorkerInfoChangeListeners();

        updateMasterNodes();
        notifyMasterInfoChangeListeners();
    }

    /**
     * worker group node listener
     */
    class WorkerDataListener implements SubscribeListener {

        @Override
        public void notify(Event event) {
            final String path = event.path();
            final Type type = event.type();
            final String data = event.data();
            if (registryClient.isWorkerPath(path)) {
                try {
                    String[] parts = path.split("/");
                    final String workerAddress = parts[parts.length - 1];

                    // todo: update workerNodeInfo
                    log.debug("received subscribe event : {}", event);
                    if (type == Type.ADD) {
                        log.info("Worker: {} added, currentNode : {}", path, workerAddress);
                    } else if (type == Type.REMOVE) {
                        log.info("Worker node : {} down.", path);
                        alertDao.sendServerStoppedAlert(1, path, "WORKER");
                        listenerEventAlertManager.publishServerDownListenerEvent(path, "WORKER");
                    } else if (type == Type.UPDATE) {
                        syncSingleWorkerNodeInfo(workerAddress, JSONUtils.parseObject(data, WorkerHeartBeat.class));
                    }
                } catch (Exception ex) {
                    log.error("WorkerGroupListener capture data change and get data failed", ex);
                }
            }
        }

        private void syncSingleWorkerNodeInfo(String workerAddress, WorkerHeartBeat info) {
            workerNodeInfoWriteLock.lock();
            try {
                workerNodeInfo.put(workerAddress, info);
            } finally {
                workerNodeInfoWriteLock.unlock();
            }
        }
    }

    class MasterDataListener implements SubscribeListener {

        @Override
        public void notify(Event event) {
            final String path = event.path();
            final Type type = event.type();
            if (registryClient.isMasterPath(path)) {
                try {
                    if (type.equals(Type.ADD)) {
                        log.info("master node : {} added.", path);
                    } else if (type.equals(Type.REMOVE)) {
                        log.info("master node : {} down.", path);
                        alertDao.sendServerStoppedAlert(1, path, "MASTER");
                        listenerEventAlertManager.publishServerDownListenerEvent(path, "MASTER");
                    }
                } catch (Exception ex) {
                    log.error("MasterNodeListener capture data change and get data failed.", ex);
                }
            }
        }
    }

    private void updateMasterNodes() {
        masterNodeInfoLock.lock();
        try {
            masterNodeInfo.clear();
            Map<String, String> masterNodeMaps = registryClient.getServerMaps(RegistryNodeType.MASTER);
            for (Map.Entry<String, String> entry : masterNodeMaps.entrySet()) {
                masterNodeInfo.put(entry.getKey(), JSONUtils.parseObject(entry.getValue(), MasterHeartBeat.class));
            }
        } catch (Exception e) {
            log.error("update master nodes error", e);
        } finally {
            masterNodeInfoLock.unlock();
        }

    }

    private void updateWorkerNodes() {
        workerGroupWriteLock.lock();
        try {
            Map<String, String> workerNodeMaps = registryClient.getServerMaps(RegistryNodeType.WORKER);
            for (Map.Entry<String, String> entry : workerNodeMaps.entrySet()) {
                workerNodeInfo.put(entry.getKey(), JSONUtils.parseObject(entry.getValue(), WorkerHeartBeat.class));
            }
        } finally {
            workerGroupWriteLock.unlock();
        }
    }

    private void updateWorkerGroupMappings() {
        List<WorkerGroup> workerGroups = workerGroupMapper.queryAllWorkerGroup();
        Map<String, Set<String>> tmpWorkerGroupMappings = new HashMap<>();
        try {
            workerNodeInfoReadLock.lock();
            for (WorkerGroup workerGroup : workerGroups) {
                String workerGroupName = workerGroup.getName();
                String[] workerAddresses = workerGroup.getAddrList().split(Constants.COMMA);
                if (ArrayUtils.isEmpty(workerAddresses)) {
                    continue;
                }
                Set<String> activeWorkerNodes = Arrays.stream(workerAddresses)
                        .filter(workerNodeInfo::containsKey).collect(Collectors.toSet());
                tmpWorkerGroupMappings.put(workerGroupName, activeWorkerNodes);
            }
            if (!tmpWorkerGroupMappings.containsKey(Constants.DEFAULT_WORKER_GROUP)) {
                tmpWorkerGroupMappings.put(Constants.DEFAULT_WORKER_GROUP, workerNodeInfo.keySet());
            }
        } finally {
            workerNodeInfoReadLock.unlock();
        }

        workerGroupWriteLock.lock();
        try {
            workerGroupNodes.clear();
            workerGroupNodes.putAll(tmpWorkerGroupMappings);
        } finally {
            workerGroupWriteLock.unlock();
        }
    }

    public Map<String, Set<String>> getWorkerGroupNodes() {
        workerGroupReadLock.lock();
        try {
            return Collections.unmodifiableMap(workerGroupNodes);
        } finally {
            workerGroupReadLock.unlock();
        }
    }

    /**
     * get worker group nodes
     *
     * @param workerGroup workerGroup
     * @return worker nodes
     */
    public Set<String> getWorkerGroupNodes(String workerGroup) throws WorkerGroupNotFoundException {
        workerGroupReadLock.lock();
        try {
            if (StringUtils.isEmpty(workerGroup)) {
                workerGroup = Constants.DEFAULT_WORKER_GROUP;
            }
            Set<String> nodes = workerGroupNodes.get(workerGroup);
            if (nodes == null) {
                throw new WorkerGroupNotFoundException(String.format("WorkerGroup: %s is invalidated", workerGroup));
            }
            if (CollectionUtils.isEmpty(nodes)) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(nodes);
        } finally {
            workerGroupReadLock.unlock();
        }
    }

    public Map<String, WorkerHeartBeat> getWorkerNodeInfo() {
        return Collections.unmodifiableMap(workerNodeInfo);
    }

    public Optional<WorkerHeartBeat> getWorkerNodeInfo(String workerServerAddress) {
        workerNodeInfoReadLock.lock();
        try {
            return Optional.ofNullable(workerNodeInfo.getOrDefault(workerServerAddress, null));
        } finally {
            workerNodeInfoReadLock.unlock();
        }
    }

    public Map<String, MasterHeartBeat> getMasterNodeInfo() {
        return Collections.unmodifiableMap(masterNodeInfo);
    }

    /**
     * Add the resource change listener, when the resource changed, the listener will be notified.
     *
     * @param listener will be trigger, when the worker node info changed.
     */
    public synchronized void addWorkerInfoChangeListener(WorkerInfoChangeListener listener) {
        workerInfoChangeListeners.add(listener);
    }

    private void notifyWorkerInfoChangeListeners() {
        Map<String, Set<String>> workerGroupNodeMap = getWorkerGroupNodes();
        Map<String, WorkerHeartBeat> workerNodeInfoMap = getWorkerNodeInfo();
        for (WorkerInfoChangeListener listener : workerInfoChangeListeners) {
            listener.notify(workerGroupNodeMap, workerNodeInfoMap);
        }
    }

    public synchronized void addMasterInfoChangeListener(MasterInfoChangeListener listener) {
        masterInfoChangeListeners.add(listener);
    }

    private void notifyMasterInfoChangeListeners() {
        Map<String, MasterHeartBeat> masterNodeInfoMap = getMasterNodeInfo();
        for (MasterInfoChangeListener listener : masterInfoChangeListeners) {
            listener.notify(masterNodeInfoMap);
        }
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

}
