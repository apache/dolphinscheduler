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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.service.zk.AbstractListener;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

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

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *  server node manager
 */
@Service
public class ServerNodeManager implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ServerNodeManager.class);

    /**
     * master lock
     */
    private final Lock masterLock = new ReentrantLock();

    /**
     * worker group lock
     */
    private final Lock workerGroupLock = new ReentrantLock();

    /**
     * worker node info lock
     */
    private final Lock workerNodeInfoLock = new ReentrantLock();

    /**
     * worker group nodes
     */
    private final ConcurrentHashMap<String, Set<String>> workerGroupNodes = new ConcurrentHashMap<>();

    /**
     * master nodes
     */
    private final Set<String> masterNodes = new HashSet<>();

    /**
     * worker node info
     */
    private final Map<String, String> workerNodeInfo = new HashMap<>();

    /**
     * executor service
     */
    private ScheduledExecutorService executorService;

    /**
     * zk client
     */
    @Autowired
    private ZKClient zkClient;

    /**
     * zookeeper registry center
     */
    @Autowired
    private ZookeeperRegistryCenter registryCenter;

    /**
     * worker group mapper
     */
    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    /**
     * alert dao
     */
    @Autowired
    private AlertDao alertDao;

    /**
     * init listener
     * @throws Exception if error throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * load nodes from zookeeper
         */
        load();
        /**
         * init executor service
         */
        executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ServerNodeManagerExecutor"));
        executorService.scheduleWithFixedDelay(new WorkerNodeInfoAndGroupDbSyncTask(), 0, 10, TimeUnit.SECONDS);
        /**
         * init MasterNodeListener listener
         */
        registryCenter.getRegisterOperator().addListener(new MasterNodeListener());
        /**
         * init WorkerNodeListener listener
         */
        registryCenter.getRegisterOperator().addListener(new WorkerGroupNodeListener());
    }

    /**
     *  load nodes from zookeeper
     */
    private void load() {
        /**
         * master nodes from zookeeper
         */
        Set<String> initMasterNodes = registryCenter.getMasterNodesDirectly();
        syncMasterNodes(initMasterNodes);

        /**
         * worker group nodes from zookeeper
         */
        Set<String> workerGroups = registryCenter.getWorkerGroupDirectly();
        for (String workerGroup : workerGroups) {
            syncWorkerGroupNodes(workerGroup, registryCenter.getWorkerGroupNodesDirectly(workerGroup));
        }
    }

    /**
     * zookeeper client
     */
    @Component
    static class ZKClient extends AbstractZKClient {}

    /**
     *  worker node info and worker group db sync task
     */
    class WorkerNodeInfoAndGroupDbSyncTask implements Runnable {

        @Override
        public void run() {
            // sync worker node info
            Map<String, String> newWorkerNodeInfo = zkClient.getServerMaps(ZKNodeType.WORKER, true);
            syncWorkerNodeInfo(newWorkerNodeInfo);

            // sync worker group nodes from database
            List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup();
            if (CollectionUtils.isNotEmpty(workerGroupList)) {
                for (WorkerGroup wg : workerGroupList) {
                    String workerGroup = wg.getName();
                    Set<String> nodes = new HashSet<>();
                    String[] addrs = wg.getAddrList().split(Constants.COMMA);
                    for (String addr : addrs) {
                        if (newWorkerNodeInfo.containsKey(addr)) {
                            nodes.add(addr);
                        }
                    }
                    if (!nodes.isEmpty()) {
                        syncWorkerGroupNodes(workerGroup, nodes);
                    }
                }
            }
        }
    }

    /**
     *  worker group node listener
     */
    class WorkerGroupNodeListener extends AbstractListener {

        @Override
        protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
            if (registryCenter.isWorkerPath(path)) {
                try {
                    if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                        logger.info("worker group node : {} added.", path);
                        String group = parseGroup(path);
                        Set<String> currentNodes = registryCenter.getWorkerGroupNodesDirectly(group);
                        logger.info("currentNodes : {}", currentNodes);
                        syncWorkerGroupNodes(group, currentNodes);
                    } else if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                        logger.info("worker group node : {} down.", path);
                        String group = parseGroup(path);
                        Set<String> currentNodes = registryCenter.getWorkerGroupNodesDirectly(group);
                        syncWorkerGroupNodes(group, currentNodes);
                        alertDao.sendServerStopedAlert(1, path, "WORKER");
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warn(ex.getMessage());
                } catch (Exception ex) {
                    logger.error("WorkerGroupListener capture data change and get data failed", ex);
                }
            }
        }

        private String parseGroup(String path) {
            String[] parts = path.split("/");
            if (parts.length < 6) {
                throw new IllegalArgumentException(String.format("worker group path : %s is not valid, ignore", path));
            }
            return parts[parts.length - 2];
        }
    }

    /**
     *  master node listener
     */
    class MasterNodeListener extends AbstractListener {

        @Override
        protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
            if (registryCenter.isMasterPath(path)) {
                try {
                    if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                        logger.info("master node : {} added.", path);
                        Set<String> currentNodes = registryCenter.getMasterNodesDirectly();
                        syncMasterNodes(currentNodes);
                    } else if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                        logger.info("master node : {} down.", path);
                        Set<String> currentNodes = registryCenter.getMasterNodesDirectly();
                        syncMasterNodes(currentNodes);
                        alertDao.sendServerStopedAlert(1, path, "MASTER");
                    }
                } catch (Exception ex) {
                    logger.error("MasterNodeListener capture data change and get data failed.", ex);
                }
            }
        }
    }

    /**
     *  get master nodes
     * @return master nodes
     */
    public Set<String> getMasterNodes() {
        masterLock.lock();
        try {
            return Collections.unmodifiableSet(masterNodes);
        } finally {
            masterLock.unlock();
        }
    }

    /**
     *  sync master nodes
     * @param nodes master nodes
     */
    private void syncMasterNodes(Set<String> nodes) {
        masterLock.lock();
        try {
            masterNodes.clear();
            masterNodes.addAll(nodes);
        } finally {
            masterLock.unlock();
        }
    }

    /**
     * sync worker group nodes
     * @param workerGroup worker group
     * @param nodes worker nodes
     */
    private void syncWorkerGroupNodes(String workerGroup, Set<String> nodes) {
        workerGroupLock.lock();
        try {
            workerGroup = workerGroup.toLowerCase();
            Set<String> workerNodes = workerGroupNodes.getOrDefault(workerGroup, new HashSet<>());
            workerNodes.clear();
            workerNodes.addAll(nodes);
            workerGroupNodes.put(workerGroup, workerNodes);
        } finally {
            workerGroupLock.unlock();
        }
    }

    public Map<String, Set<String>> getWorkerGroupNodes() {
        return Collections.unmodifiableMap(workerGroupNodes);
    }

    /**
     * get worker group nodes
     * @param workerGroup workerGroup
     * @return worker nodes
     */
    public Set<String> getWorkerGroupNodes(String workerGroup) {
        workerGroupLock.lock();
        try {
            if (StringUtils.isEmpty(workerGroup)) {
                workerGroup = Constants.DEFAULT_WORKER_GROUP;
            }
            workerGroup = workerGroup.toLowerCase();
            Set<String> nodes = workerGroupNodes.get(workerGroup);
            if (CollectionUtils.isNotEmpty(nodes)) {
                return Collections.unmodifiableSet(nodes);
            }
            return nodes;
        } finally {
            workerGroupLock.unlock();
        }
    }

    /**
     * get worker node info
     * @return worker node info
     */
    public Map<String, String> getWorkerNodeInfo() {
        return Collections.unmodifiableMap(workerNodeInfo);
    }

    /**
     * get worker node info
     * @param workerNode worker node
     * @return worker node info
     */
    public String getWorkerNodeInfo(String workerNode) {
        workerNodeInfoLock.lock();
        try {
            return workerNodeInfo.getOrDefault(workerNode, null);
        } finally {
            workerNodeInfoLock.unlock();
        }
    }

    /**
     * sync worker node info
     * @param newWorkerNodeInfo new worker node info
     */
    private void syncWorkerNodeInfo(Map<String, String> newWorkerNodeInfo) {
        workerNodeInfoLock.lock();
        try {
            workerNodeInfo.clear();
            workerNodeInfo.putAll(newWorkerNodeInfo);
        } finally {
            workerNodeInfoLock.unlock();
        }
    }

    /**
     *  destroy
     */
    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
        registryCenter.close();
    }

}
