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
     * worker group nodes from registry center, workerGroup -> ips
     */
    private final ConcurrentHashMap<String, Set<String>> registryWorkerGroupNodes = new ConcurrentHashMap<>();

    /**
     * worker group nodes from db, workerGroup -> ips
     */
    private final ConcurrentHashMap<String, Set<String>> dbWorkerGroupNodes = new ConcurrentHashMap<>();

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

    /**
     * eg : /dolphinscheduler/node/worker/group/127.0.0.1:xxx
     */
    private static final int WORKER_LISTENER_CHECK_LENGTH = 5;

    /**
     * worker group mapper
     */
    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    private final MasterPriorityQueue masterPriorityQueue = new MasterPriorityQueue();

    /**
     * alert dao
     */
    @Autowired
    private AlertDao alertDao;

    /**
     * master config
     */
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

        // worker group nodes from zookeeper
        Collection<String> workerGroups = registryClient.getWorkerGroupDirectly();
        for (String workerGroup : workerGroups) {
            syncWorkerGroupNodesFromRegistry(workerGroup,
                    registryClient.getWorkerGroupNodesDirectly(workerGroup), Type.ADD);
        }
    }

    /**
     * worker node info and worker group db sync task
     */
    class WorkerNodeInfoAndGroupDbSyncTask implements Runnable {

        @Override
        public void run() {
            try {
                dbWorkerGroupNodes.clear();

                // sync worker node info
                Map<String, String> registryWorkerNodeMap = registryClient
                        .getServerMaps(NodeType.WORKER, true);
                syncAllWorkerNodeInfo(registryWorkerNodeMap);
                // sync worker group nodes from database
                List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup();
                if (CollectionUtils.isNotEmpty(workerGroupList)) {
                    for (WorkerGroup wg : workerGroupList) {
                        String workerGroupName = wg.getName();
                        Set<String> workerAddress = getWorkerAddressByWorkerGroup(
                                registryWorkerNodeMap, wg);
                        if (!workerAddress.isEmpty()) {
                            Set<String> workerNodes = dbWorkerGroupNodes
                                    .getOrDefault(workerGroupName, new HashSet<>());
                            workerNodes.clear();
                            workerNodes.addAll(workerAddress);
                            dbWorkerGroupNodes.put(workerGroupName, workerNodes);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("WorkerNodeInfoAndGroupDbSyncTask error:", e);
            } finally {
                refreshWorkerGroupNodes();
            }
        }
    }

<<<<<<< HEAD
    protected Set<String> getWorkerAddressByWorkerGroup(Map<String, String> newWorkerNodeInfo,
                                                        WorkerGroup wg) {
=======
    protected Set<String> getWorkerAddressByWorkerGroup(Map<String, String> newWorkerNodeInfo, WorkerGroup wg) {
>>>>>>> 2e61c76c2 ([Improvement] Add remote task model (#11767))
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
                    if (parts.length < WORKER_LISTENER_CHECK_LENGTH) {
                        throw new IllegalArgumentException(
                                String.format("worker group path : %s is not valid, ignore", path));
                    }
                    final String workerGroupName = parts[parts.length - 2];
                    final String workerAddress = parts[parts.length - 1];

                    logger.debug("received subscribe event : {}", event);
                    Collection<String> currentNodes = registryClient
                            .getWorkerGroupNodesDirectly(workerGroupName);
                    syncWorkerGroupNodesFromRegistry(workerGroupName, currentNodes, type);

                    if (type == Type.ADD) {
                        logger.info("worker group node : {} added, currentNodes : {}", path,
                                currentNodes);
                    } else if (type == Type.REMOVE) {
                        logger.info("worker group node : {} down.", path);
                        alertDao.sendServerStoppedAlert(1, path, "WORKER");
                    } else if (type == Type.UPDATE) {
                        syncSingleWorkerNodeInfo(workerAddress,
                                JSONUtils.parseObject(data, WorkerHeartBeat.class));
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warn(ex.getMessage());
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
<<<<<<< HEAD
            logger.info("update master nodes, master size: {}, slot: {}, addr: {}", MASTER_SIZE,
                    MASTER_SLOT, masterConfig.getMasterAddress());
=======
            logger.info("update master nodes, master size: {}, slot: {}, addr: {}", MASTER_SIZE, MASTER_SLOT,
                    masterConfig.getMasterAddress());
>>>>>>> 2e61c76c2 ([Improvement] Add remote task model (#11767))
        } finally {
            masterLock.unlock();
        }
    }

    /**
     * sync worker group nodes from registry center
     *
     * @param workerGroup worker group
     * @param nodes worker nodes
     * @param type event type
     */
    private void syncWorkerGroupNodesFromRegistry(String workerGroup, Collection<String> nodes,
                                                  Type type) {
        try {
            if (type == Type.REMOVE) {
                if (!registryWorkerGroupNodes.containsKey(workerGroup)) {
                    logger.warn("cannot remove worker group {}, not in active list", workerGroup);
                    return;
                }
                registryWorkerGroupNodes.remove(workerGroup);
            } else {
                Set<String> workerNodes = registryWorkerGroupNodes
                        .getOrDefault(workerGroup, new HashSet<>());
                workerNodes.clear();
                workerNodes.addAll(nodes);
                registryWorkerGroupNodes.put(workerGroup, workerNodes);
            }
        } finally {
            refreshWorkerGroupNodes();
        }
    }

    /**
     * refresh worker group nodes
     */
    private void refreshWorkerGroupNodes() {
        workerGroupWriteLock.lock();
        try {
            workerGroupNodes.clear();
            workerGroupNodes.putAll(registryWorkerGroupNodes);
            workerGroupNodes.putAll(dbWorkerGroupNodes);
            logger.debug("refresh worker group nodes, current list: {}",
                    Arrays.toString(workerGroupNodes.keySet().toArray()));
        } finally {
            notifyWorkerInfoChangeListeners();
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
                workerNodeInfo.put(entry.getKey(),
                        JSONUtils.parseObject(entry.getValue(), WorkerHeartBeat.class));
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
