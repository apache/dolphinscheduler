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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterSlotManager implements IMasterSlotReBalancer {

    private final MasterClusters masterClusters;

    private final MasterConfig masterConfig;

    private volatile int currentSlot = -1;

    private volatile int totalSlots = 0;

    public MasterSlotManager(ClusterManager clusterManager, MasterConfig masterConfig) {
        this.masterConfig = masterConfig;
        this.masterClusters = clusterManager.getMasterClusters();
        this.masterClusters.registerListener(new IClusters.IClustersChangeListener<MasterServerMetadata>() {

            @Override
            public void onServerAdded(MasterServerMetadata server) {
                doReBalance(masterClusters.getNormalServers());
            }

            @Override
            public void onServerRemove(MasterServerMetadata server) {
                doReBalance(masterClusters.getNormalServers());
            }

            @Override
            public void onServerUpdate(MasterServerMetadata server) {
                doReBalance(masterClusters.getNormalServers());
            }
        });
    }

    /**
     * Get the current master slot, if the slot is -1, it means the master slot is not available.
     */
    public int getCurrentMasterSlot() {
        return currentSlot;
    }

    /**
     * Get the total master slots.
     */
    public int getTotalMasterSlots() {
        return totalSlots;
    }

    public boolean checkSlotValid() {
        return totalSlots > 0 && currentSlot >= 0;
    }

    @Override
    public void doReBalance(List<MasterServerMetadata> normalMasterServers) {

        int tmpCurrentSlot = -1;
        for (int i = 0; i < normalMasterServers.size(); i++) {
            if (normalMasterServers.get(i).getAddress().equals(masterConfig.getMasterAddress())) {
                tmpCurrentSlot = i;
                break;
            }
        }
        if (tmpCurrentSlot == -1) {
            log.warn(
                    "Do rebalance failed, cannot found the current master: {} in the normal master clusters: {}. Please check the current master server status",
                    masterConfig.getMasterAddress(), normalMasterServers);
            currentSlot = -1;
            return;
        }

        if (totalSlots == normalMasterServers.size() && currentSlot == tmpCurrentSlot) {
            log.debug("No need to rebalance, the currentSlot: {}, totalSlots: {} doesn't changed", currentSlot,
                    totalSlots);
            return;
        }
        totalSlots = normalMasterServers.size();
        currentSlot = tmpCurrentSlot;
        log.info("Do rebalance success, current master slot: {}, total master slots: {}", currentSlot, totalSlots);
    }
}
