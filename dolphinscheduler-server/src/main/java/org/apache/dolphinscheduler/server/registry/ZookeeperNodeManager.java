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

package org.apache.dolphinscheduler.server.registry;

import org.apache.curator.framework.CuratorFramework;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.dolphinscheduler.service.zk.AbstractListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class ZookeeperNodeManager implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperNodeManager.class);

    private final Lock masterLock = new ReentrantLock();

    private final Lock workerLock = new ReentrantLock();

    private final Set<String> workerNodes = new HashSet<>();

    private final Set<String> masterNodes = new HashSet<>();

    @Autowired
    private ZookeeperRegistryCenter registryCenter;

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
        registryCenter.getZookeeperCachedOperator().addListener(new MasterNodeListener());
        registryCenter.getZookeeperCachedOperator().addListener(new WorkerNodeListener());
    }

    private void load(){
        Set<String> schedulerNodes = registryCenter.getMasterNodesDirectly();
        syncMasterNodes(schedulerNodes);
        Set<String> workersNodes = registryCenter.getWorkerNodesDirectly();
        syncWorkerNodes(workersNodes);
    }

    class WorkerNodeListener extends AbstractListener {

        @Override
        protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
            if(registryCenter.isWorkerPath(path)){
                try {
                    if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                        logger.info("worker node : {} added.", path);
                        Set<String> previousNodes = new HashSet<>(workerNodes);
                        Set<String> currentNodes = registryCenter.getWorkerNodesDirectly();
                        syncWorkerNodes(currentNodes);
                    } else if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                        logger.info("worker node : {} down.", path);
                        Set<String> previousNodes = new HashSet<>(workerNodes);
                        Set<String> currentNodes = registryCenter.getWorkerNodesDirectly();
                        syncWorkerNodes(currentNodes);
                    }
                } catch (IllegalArgumentException ignore) {
                    logger.warn(ignore.getMessage());
                } catch (Exception ex) {
                    logger.error("WorkerListener capture data change and get data failed", ex);
                }
            }
        }
    }


    class MasterNodeListener extends AbstractListener {

        @Override
        protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
            if (registryCenter.isMasterPath(path)) {
                try {
                    if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                        logger.info("master node : {} added.", path);
                        Set<String> previousNodes = new HashSet<>(masterNodes);
                        Set<String> currentNodes = registryCenter.getMasterNodesDirectly();
                        syncMasterNodes(currentNodes);
                    } else if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                        logger.info("master node : {} down.", path);
                        Set<String> previousNodes = new HashSet<>(masterNodes);
                        Set<String> currentNodes = registryCenter.getMasterNodesDirectly();
                        syncMasterNodes(currentNodes);
                    }
                } catch (Exception ex) {
                    logger.error("MasterNodeListener capture data change and get data failed.", ex);
                }
            }
        }
    }

    public Set<String> getMasterNodes() {
        masterLock.lock();
        try {
            return Collections.unmodifiableSet(masterNodes);
        } finally {
            masterLock.unlock();
        }
    }

    private void syncMasterNodes(Set<String> nodes){
        masterLock.lock();
        try {
            masterNodes.clear();
            masterNodes.addAll(nodes);
        } finally {
            masterLock.unlock();
        }
    }

    private void syncWorkerNodes(Set<String> nodes){
        workerLock.lock();
        try {
            workerNodes.clear();
            workerNodes.addAll(nodes);
        } finally {
            workerLock.unlock();
        }
    }

    public Set<String> getWorkerNodes(){
        workerLock.lock();
        try {
            return Collections.unmodifiableSet(workerNodes);
        } finally {
            workerLock.unlock();
        }
    }

    public void close(){
        registryCenter.close();
    }
}
