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

import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterActiveConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.queue.MasterPriorityQueue;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterActiveManager implements InitializingBean {

    private volatile int currentSlot = 0;

    private volatile int totalSlot = 0;

    private final MasterPriorityQueue masterPriorityQueue = new MasterPriorityQueue();

    private final Lock masterLock = new ReentrantLock();

    private final MasterConfig masterConfig;

    private final MasterActiveConfig masterActiveConfig;

    private final RegistryClient registryClient;

    private final ActiveHandler activeHandler;

    public MasterActiveManager(MasterConfig masterConfig, RegistryClient registryClient) {
        this.masterConfig = masterConfig;
        this.masterActiveConfig = masterConfig.getMasterActiveConfig();
        this.registryClient = registryClient;
        if (MasterActiveConfig.MasterActiveStrategy.RESOURCE.equals(masterActiveConfig.getStrategy())) {
            this.activeHandler = new ResourceActiveHandler();
        } else {
            this.activeHandler = new ActiveHandler() {
            };
        }
    }

    @Override
    public void afterPropertiesSet() {

        // load nodes from zookeeper
        updateMasterNodes();

        registryClient.subscribe(
                masterActiveConfig.getStrategy().getActiveRegistryNodeType().getRegistryPath(),
                new SlotListener());
    }

    class SlotListener implements SubscribeListener {

        @Override
        public void notify(Event event) {
            final String path = event.path();
            final Event.Type type = event.type();
            if (path != null && path.startsWith(
                    masterActiveConfig.getStrategy().getActiveRegistryNodeType().getRegistryPath())) {
                try {
                    // The update event was not consumed, otherwise, slot allocation will be very frequent.
                    // so resource usage not up-to-date and cannot be sorted
                    // currently, the masters are sorted based on the creation time
                    if (type.equals(Event.Type.ADD)) {
                        log.info("master node : {} active.", path);
                        updateMasterNodes();
                    }
                    if (type.equals(Event.Type.REMOVE)) {
                        log.info("master node : {} inactive.", path);
                        updateMasterNodes();
                    }
                } catch (Exception ex) {
                    log.error("SlotListener capture data change and get data failed.", ex);
                }
            }
        }
    }

    private void updateMasterNodes() {
        currentSlot = 0;
        totalSlot = 0;
        String nodeLock = RegistryNodeType.MASTER_NODE_LOCK.getRegistryPath();
        try {
            registryClient.getLock(nodeLock);
            List<Server> masterNodeList = registryClient.getServerList(
                    masterConfig.getMasterActiveConfig().getStrategy().getActiveRegistryNodeType());
            syncMasterNodes(masterNodeList);
        } catch (Exception e) {
            log.error("update master nodes error", e);
        } finally {
            registryClient.releaseLock(nodeLock);
        }
    }

    /**
     * sync master nodes
     *
     * @param masterNodes master nodes
     */
    private void syncMasterNodes(List<Server> masterNodes) {
        masterLock.lock();
        try {
            this.masterPriorityQueue.clear();
            this.masterPriorityQueue.putList(masterNodes);
            int index = masterPriorityQueue.getIndex(masterConfig.getMasterAddress());
            if (index >= 0) {
                totalSlot = masterNodes.size();
                currentSlot = index;
            } else {
                log.warn("Current master is not in active master list");
            }
            log.info("Update master nodes, total master size: {}, current slot: {}", totalSlot, currentSlot);
        } finally {
            masterLock.unlock();
        }
    }

    public int getSlot() {
        return currentSlot;
    }

    public int getMasterSize() {
        return totalSlot;
    }

    public void checkSelfState(MasterHeartBeat masterHeartBeat) {
        activeHandler.checkSelfState(masterHeartBeat);
    }

    public void inactive() {
        activeHandler.inactive();
    }

    interface ActiveHandler {

        /**
         * master close or lost connection to registry
         */
        default void inactive() {
        }

        /**
         * check self state
         * Timed call by heartbeat thread
         */
        default void checkSelfState(MasterHeartBeat masterHeartBeat) {
        }
    }

    class ResourceActiveHandler implements ActiveHandler {

        private final String activeRegistryPath;

        private volatile int state = MASTER_INACTIVE;

        public static final int MASTER_ACTIVE = 1;
        public static final int MASTER_INACTIVE = 0;

        public ResourceActiveHandler() {
            this.activeRegistryPath =
                    MasterActiveConfig.MasterActiveStrategy.RESOURCE.getActiveRegistryNodeType().getRegistryPath()
                            + "/" + masterConfig.getMasterAddress();
        }

        @Override
        public void inactive() {
            state = MASTER_INACTIVE;
            try {
                registryClient.remove(activeRegistryPath);
            } catch (Exception e) {
                log.error("MasterServer remove active path exception ", e);
            }
        }

        @Override
        public void checkSelfState(MasterHeartBeat masterHeartBeat) {
            if (state == MASTER_ACTIVE) {
                if (masterHeartBeat.getCpuUsage() > masterActiveConfig.cpuHighWatchMark(masterConfig) ||
                        masterHeartBeat.getMemoryUsage() > masterActiveConfig.memoryHighWatchMark(masterConfig)) {
                    log.info("self state from active to inactive");
                    // deregister when state from active to inactive
                    registryClient.remove(activeRegistryPath);
                    state = MASTER_INACTIVE;
                }
            } else {
                if (masterHeartBeat.getCpuUsage() < masterActiveConfig.cpuLowWatchMark() &&
                        masterHeartBeat.getMemoryUsage() < masterActiveConfig.memoryLowWatchMark()) {
                    log.info("self state from inactive to active");
                    // only register once when state from inactive to active
                    registryClient.persistEphemeral(activeRegistryPath, JSONUtils.toJsonString(masterHeartBeat));
                    state = MASTER_ACTIVE;
                }
            }
        }
    }
}
