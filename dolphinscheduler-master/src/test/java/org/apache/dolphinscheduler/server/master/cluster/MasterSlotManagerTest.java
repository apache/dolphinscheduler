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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MasterSlotManagerTest {

    private MasterSlotManager masterSlotManager;

    private ClusterManager clusterManager;

    private MasterConfig masterConfig;

    @BeforeEach
    public void setUp() {
        clusterManager = new ClusterManager();
        masterConfig = new MasterConfig();
        masterConfig.setMasterAddress("127.0.0.1:5678");
        masterSlotManager = new MasterSlotManager(clusterManager, masterConfig);
        MasterServerMetadata master1 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(masterConfig.getMasterAddress())
                .build();
        MasterServerMetadata master2 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address("127.0.0.2:5679")
                .build();
        MasterServerMetadata master3 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address("127.0.0.3:5679")
                .build();
        MasterServerMetadata master4 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.BUSY)
                .address("127.0.0.4:5679")
                .build();
        clusterManager.getMasterClusters().onServerAdded(master1);
        clusterManager.getMasterClusters().onServerAdded(master2);
        clusterManager.getMasterClusters().onServerAdded(master3);
        clusterManager.getMasterClusters().onServerAdded(master4);
    }

    @Test
    void getCurrentMasterSlot() {
        assertThat(masterSlotManager.getCurrentMasterSlot()).isAtLeast(0);
    }

    @Test
    void getTotalMasterSlots() {
        assertThat(masterSlotManager.getTotalMasterSlots()).isEqualTo(3);
    }

    @Test
    void checkSlotValid() {
        assertThat(masterSlotManager.checkSlotValid()).isTrue();
    }

    @Test
    void doReBalance() {
        MasterServerMetadata master2 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address("127.0.0.3:5679")
                .build();
        MasterServerMetadata master3 = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.BUSY)
                .address("127.0.0.4:5679")
                .build();
        clusterManager.getMasterClusters().onServerRemove(master2);
        clusterManager.getMasterClusters().onServerRemove(master3);
        // After doReBalance, the total master slots should be 2
        assertThat(masterSlotManager.getTotalMasterSlots()).isEqualTo(2);
    }
}
