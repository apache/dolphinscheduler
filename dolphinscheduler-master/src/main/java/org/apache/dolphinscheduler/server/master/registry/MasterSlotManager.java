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

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.queue.MasterPriorityQueue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MasterSlotManager {

    @Autowired
    protected ServerNodeManager serverNodeManager;

    @Autowired
    protected MasterConfig masterConfig;

    private volatile int currentSlot = 0;
    private volatile int totalSlot = 0;

    @PostConstruct
    public void init() {
        serverNodeManager.addMasterInfoChangeListener(new MasterSlotManager.SlotChangeListener());
    }

    public int getSlot() {
        return currentSlot;
    }

    public int getMasterSize() {
        return totalSlot;
    }

    public class SlotChangeListener implements MasterInfoChangeListener {

        private final Lock slotLock = new ReentrantLock();

        private final MasterPriorityQueue masterPriorityQueue = new MasterPriorityQueue();

        @Override
        public void notify(Map<String, MasterHeartBeat> masterNodeInfo) {
            List<Server> serverList = masterNodeInfo.values().stream()
                    .filter(heartBeat -> !heartBeat.getServerStatus().equals(ServerStatus.ABNORMAL))
                    .map(this::convertHeartBeatToServer).collect(Collectors.toList());
            syncMasterNodes(serverList);
        }

        /**
         * sync master nodes
         */
        private void syncMasterNodes(List<Server> masterNodes) {
            slotLock.lock();
            try {
                this.masterPriorityQueue.clear();
                this.masterPriorityQueue.putAll(masterNodes);
                int tempCurrentSlot = masterPriorityQueue.getIndex(masterConfig.getMasterAddress());
                int tempTotalSlot = masterNodes.size();
                if (tempCurrentSlot < 0) {
                    totalSlot = 0;
                    currentSlot = 0;
                    log.warn("Current master is not in active master list");
                } else if (tempCurrentSlot != currentSlot || tempTotalSlot != totalSlot) {
                    totalSlot = tempTotalSlot;
                    currentSlot = tempCurrentSlot;
                    log.info("Update master nodes, total master size: {}, current slot: {}", totalSlot, currentSlot);
                }
            } finally {
                slotLock.unlock();
            }
        }

        private Server convertHeartBeatToServer(MasterHeartBeat masterHeartBeat) {
            Server server = new Server();
            server.setCreateTime(new Date(masterHeartBeat.getStartupTime()));
            server.setLastHeartbeatTime(new Date(masterHeartBeat.getReportTime()));
            server.setId(masterHeartBeat.getProcessId());
            server.setHost(masterHeartBeat.getHost());
            server.setPort(masterHeartBeat.getPort());

            return server;
        }

    }
}
