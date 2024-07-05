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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class MasterClustersTest {

    @Test
    void getServers() {
        MasterClusters masterClusters = new MasterClusters();
        MasterServerMetadata normalMasterServerMetadata = getNormalMasterServerMetadata();
        MasterServerMetadata busyMasterServerMetadata = getBusyMasterServerMetadata();
        masterClusters.onServerAdded(normalMasterServerMetadata);
        masterClusters.onServerAdded(busyMasterServerMetadata);
        assertThat(masterClusters.getServers()).containsExactly(normalMasterServerMetadata, busyMasterServerMetadata);
    }

    @Test
    void getNormalServers() {
        MasterClusters masterClusters = new MasterClusters();
        MasterServerMetadata normalMasterServerMetadata = getNormalMasterServerMetadata();
        MasterServerMetadata busyMasterServerMetadata = getBusyMasterServerMetadata();
        masterClusters.onServerAdded(normalMasterServerMetadata);
        masterClusters.onServerAdded(busyMasterServerMetadata);
        assertThat(masterClusters.getNormalServers()).containsExactly(normalMasterServerMetadata);
    }

    @Test
    void registerListener() {
        MasterClusters masterClusters = new MasterClusters();
        AtomicBoolean addServerFlag = new AtomicBoolean(false);
        masterClusters.registerListener(
                (IClusters.ServerAddedListener<MasterServerMetadata>) server -> addServerFlag.set(true));
        masterClusters.onServerAdded(getNormalMasterServerMetadata());
        assertThat(addServerFlag.get()).isTrue();
    }

    @Test
    void onServerAdded() {
        MasterClusters masterClusters = new MasterClusters();
        masterClusters.onServerAdded(getNormalMasterServerMetadata());
        assertThat(masterClusters.getServers()).containsExactly(getNormalMasterServerMetadata());
    }

    @Test
    void onServerRemove() {
        MasterClusters masterClusters = new MasterClusters();
        masterClusters.onServerAdded(getNormalMasterServerMetadata());
        masterClusters.onServerRemove(getNormalMasterServerMetadata());
        assertThat(masterClusters.getServers()).isEmpty();
    }

    @Test
    void onServerUpdate() {
        MasterClusters masterClusters = new MasterClusters();
        masterClusters.onServerAdded(getNormalMasterServerMetadata());
        MasterServerMetadata updatedMasterServerMetadata = MasterServerMetadata.builder()
                .address("127.0.0.1:5679")
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.BUSY)
                .build();
        masterClusters.onServerUpdate(updatedMasterServerMetadata);
        assertThat(masterClusters.getServers()).containsExactly(updatedMasterServerMetadata);
    }

    private MasterServerMetadata getNormalMasterServerMetadata() {
        return MasterServerMetadata.builder()
                .address("127.0.0.1:5679")
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .build();
    }

    private MasterServerMetadata getBusyMasterServerMetadata() {
        return MasterServerMetadata.builder()
                .address("127.0.0.2:1235")
                .cpuUsage(0.8)
                .memoryUsage(0.8)
                .serverStatus(ServerStatus.BUSY)
                .build();
    }
}
