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

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Event.Type;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.queue.MasterPriorityQueue;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * server node manager
 */
@Service
public class ServerNodeManager implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ServerNodeManager.class);

    private final Lock masterLock = new ReentrantLock();

    private final ReentrantReadWriteLock workerGroupLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock workerGroupReadLock = workerGroupLock.readLock();
    private final ReentrantReadWriteLock.WriteLock workerGroupWriteLock = workerGroupLock.writeLock();

    private final ReentrantReadWriteLock workerNodeInfoLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock workerNodeInfoReadLock = workerNodeInfoLock.readLock();
    private final ReentrantReadWriteLock.WriteLock workerNodeInfoWriteLock = workerNodeInfoLock.writeLock();

    /**
     * worker group nodes, workerGroup -> ips, combining registryWorkerGroupNodes and dbWorkerGroupNodes
     */
    private final ConcurrentHashMap<String, Set<String>> workerGroupNodes = new ConcurrentHashMap<>();

    /**
     * master nodes
     */
    private final Set<String> masterNodes = new HashSet<>();

    private final Map<String, WorkerHeartBeat> workerNodeInfo = new HashMap<>();

    /**
     * executor service
     */
    private ScheduledExecutorService executorService;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    private final MasterPriorityQueue masterPriorityQueue = new MasterPriorityQueue();

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private MasterConfig masterConfig;

    private List<WorkerInfoChangeListener> workerInfoChangeListeners = new ArrayList<>();

    private static volatile int MASTER_SLOT = 0;

    private static volatile int MASTER_SIZE = 0;

    public static int getSlot() {
        return MASTER_SLOT;
    }

    public static int getMasterSize() {
        return MASTER_SIZE;
    }

    /**
     * init listener
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        // load nodes from zookeeper
        load();

        // init executor service
        executorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ServerNodeManagerExecutor"));
        executorService.scheduleWithFixedDelay(new WorkerNodeInfoAndGroupDbSyncTask(), 0, 10, TimeUnit.SECONDS);

        // init MasterNodeListener listener
        registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_MASTERS, new MasterDataListener());

        // init WorkerNodeListener listener
        registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_WORKERS, new WorkerDataListener());
    }

    /**
     * load nodes from zookeeper
     */
    public void load() {
        // master nodes from zookeeper
        updateMasterNodes();
        updateWorkerNodes();
        updateWorkerGroupMappings();
    }

    /**
     * worker node info and worker group db sync task
     */
    class WorkerNodeInfoAndGroupDbSyncTask implements Runnable {

        @Override
        public void run() {
            try {

                // sync worker node info
                updateWorkerNodes();
                updateWorkerGroupMappings();
                notifyWorkerInfoChangeListeners();
            } catch (Exception e) {
                logger.error("WorkerNodeInfoAndGroupDbSyncTask error:", e);
            }
        }
    }

    protected Set<String> getWorkerAddressByWorkerGroup(Map<String, String> newWorkerNodeInfo,
                                                        WorkerGroup wg) {
        Set<String> nodes = new HashSet<>();
        String[] addrs = wg.getAddrList().split(Constants.COMMA);
        for (String addr : addrs) {
            if (newWorkerNodeInfo.containsKey(addr)) {
                nodes.add(addr);
            }
        }
        return nodes;
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
                    logger.debug("received subscribe event : {}", event);
                    if (type == Type.ADD) {
                        logger.info("Worker: {} added, currentNode : {}", path, workerAddress);
                    } else if (type == Type.REMOVE) {
                        logger.info("Worker node : {} down.", path);
                        alertDao.sendServerStoppedAlert(1, path, "WORKER");
                    } else if (type == Type.UPDATE) {
                        syncSingleWorkerNodeInfo(workerAddress, JSONUtils.parseObject(data, WorkerHeartBeat.class));
                    }
                } catch (Exception ex) {
                    logger.error("WorkerGroupListener capture data change and get data failed", ex);
                }

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
                        logger.info("master node : {} added.", path);
                        updateMasterNodes();
                    }
                    if (type.equals(Type.REMOVE)) {
                        logger.info("master node : {} down.", path);
                        updateMasterNodes();
                        alertDao.sendServerStoppedAlert(1, path, "MASTER");
                    }
                } catch (Exception ex) {
                    logger.error("MasterNodeListener capture data change and get data failed.", ex);
                }
            }
        }
    }

    private void updateMasterNodes() {
        MASTER_SLOT = 0;
        MASTER_SIZE = 0;
        this.masterNodes.clear();
        String nodeLock = Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_MASTERS;
        try {
            registryClient.getLock(nodeLock);
            Collection<String> currentNodes = registryClient.getMasterNodesDirectly();
            List<Server> masterNodes = registryClient.getServerList(NodeType.MASTER);
            syncMasterNodes(currentNodes, masterNodes);
        } catch (Exception e) {
            logger.error("update master nodes error", e);
        } finally {
            registryClient.releaseLock(nodeLock);
        }

    }

    private void updateWorkerNodes() {
        workerGroupWriteLock.lock();
        try {
            Map<String, String> workerNodeMaps = registryClient.getServerMaps(NodeType.WORKER);
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
            notifyWorkerInfoChangeListeners();
        } finally {
            workerGroupWriteLock.unlock();
        }
    }

    /**
     * sync master nodes
     *
     * @param nodes master nodes
     */
    private void syncMasterNodes(Collection<String> nodes, List<Server> masterNodes) {
        masterLock.lock();
        try {
            this.masterNodes.addAll(nodes);
            this.masterPriorityQueue.clear();
            this.masterPriorityQueue.putList(masterNodes);
            int index = masterPriorityQueue.getIndex(masterConfig.getMasterAddress());
            if (index >= 0) {
                MASTER_SIZE = nodes.size();
                MASTER_SLOT = index;
            } else {
                logger.warn("current addr:{} is not in active master list",
                        masterConfig.getMasterAddress());
            }
            logger.info("update master nodes, master size: {}, slot: {}, addr: {}", MASTER_SIZE,
                    MASTER_SLOT, masterConfig.getMasterAddress());
        } finally {
            masterLock.unlock();
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
    public Set<String> getWorkerGroupNodes(String workerGroup) {
        workerGroupReadLock.lock();
        try {
            if (StringUtils.isEmpty(workerGroup)) {
                workerGroup = Constants.DEFAULT_WORKER_GROUP;
            }
            Set<String> nodes = workerGroupNodes.get(workerGroup);
            if (CollectionUtils.isNotEmpty(nodes)) {
                return Collections.unmodifiableSet(nodes);
            }
            return nodes;
        } finally {
            workerGroupReadLock.unlock();
        }
    }

    public Map<String, WorkerHeartBeat> getWorkerNodeInfo() {
        return Collections.unmodifiableMap(workerNodeInfo);
    }

    /**
     * get worker node info
     *
     * @param workerNode worker node
     * @return worker node info
     */
    public WorkerHeartBeat getWorkerNodeInfo(String workerNode) {
        workerNodeInfoReadLock.lock();
        try {
            return workerNodeInfo.getOrDefault(workerNode, null);
        } finally {
            workerNodeInfoReadLock.unlock();
        }
    }

    /**
     * sync worker node info
     *
     * @param newWorkerNodeInfo new worker node info
     */
    private void syncAllWorkerNodeInfo(Map<String, String> newWorkerNodeInfo) {
        workerNodeInfoWriteLock.lock();
        try {
            workerNodeInfo.clear();
            for (Map.Entry<String, String> entry : newWorkerNodeInfo.entrySet()) {
                workerNodeInfo.put(entry.getKey(), JSONUtils.parseObject(entry.getValue(), WorkerHeartBeat.class));
            }
        } finally {
            workerNodeInfoWriteLock.unlock();
        }
    }

    /**
     * sync single worker node info
     */
    private void syncSingleWorkerNodeInfo(String node, WorkerHeartBeat info) {
        workerNodeInfoWriteLock.lock();
        try {
            workerNodeInfo.put(node, info);
        } finally {
            workerNodeInfoWriteLock.unlock();
        }
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
        Map<String, Set<String>> workerGroupNodes = getWorkerGroupNodes();
        Map<String, WorkerHeartBeat> workerNodeInfo = getWorkerNodeInfo();
        for (WorkerInfoChangeListener listener : workerInfoChangeListeners) {
            listener.notify(workerGroupNodes, workerNodeInfo);
        }
    }

    /**
     * destroy
     */
    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

}
