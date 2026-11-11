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
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

class WorkerClustersTest {

    @Test
    void testOnWorkerGroupDelete() {
        WorkerClusters workerClusters = new WorkerClusters();
        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);

        WorkerGroup workerGroup = WorkerGroup.builder()
                .name("flinkCluster")
                .addrList(normalWorkerServerMetadata.getAddress())
                .build();
        workerClusters.onWorkerGroupAdd(Lists.newArrayList(workerGroup));
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("flinkCluster"))
                .containsExactly(normalWorkerServerMetadata.getAddress());

        workerClusters.onWorkerGroupDelete(Lists.newArrayList(workerGroup));
        Truth.assertThat(workerClusters.containsWorkerGroup("flinkCluster")).isFalse();
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("flinkCluster")).isEmpty();
    }

    @Test
    void testOnWorkerGroupAdd() {
        WorkerClusters workerClusters = new WorkerClusters();
        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);

        WorkerGroup workerGroup = WorkerGroup.builder()
                .name("flinkCluster")
                .addrList(normalWorkerServerMetadata.getAddress())
                .build();
        workerClusters.onWorkerGroupAdd(Lists.newArrayList(workerGroup));
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("flinkCluster"))
                .containsExactly(normalWorkerServerMetadata.getAddress());
    }

    @Test
    void testOnWorkerGroupChange() {
        WorkerClusters workerClusters = new WorkerClusters();
        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);

        WorkerGroup workerGroup = WorkerGroup.builder()
                .name("flinkCluster")
                .addrList(normalWorkerServerMetadata.getAddress())
                .build();
        workerClusters.onWorkerGroupAdd(Lists.newArrayList(workerGroup));
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("flinkCluster"))
                .containsExactly(normalWorkerServerMetadata.getAddress());

        WorkerGroup updatedWorkerGroup = WorkerGroup.builder()
                .name("flinkCluster")
                .addrList("")
                .build();
        workerClusters.onWorkerGroupChange(Lists.newArrayList(updatedWorkerGroup));
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("flinkCluster")).isEmpty();
        assertThat(workerClusters.containsWorkerGroup("flinkCluster")).isTrue();
    }

    @Test
    void testOnServerAdded() {
        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        WorkerServerMetadata busyWorkerServerMetadata = getBusyWorkerServerMetadata();

        WorkerClusters workerClusters = new WorkerClusters();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);
        workerClusters.onServerAdded(busyWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(busyWorkerServerMetadata);
        assertThat(workerClusters.getDbWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress(), busyWorkerServerMetadata.getAddress());
        assertThat(workerClusters.getNormalWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress());
    }

    @Test
    void testOnServerRemove() {
        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        WorkerServerMetadata busyWorkerServerMetadata = getBusyWorkerServerMetadata();

        WorkerClusters workerClusters = new WorkerClusters();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);
        workerClusters.onServerAdded(busyWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(busyWorkerServerMetadata);
        workerClusters.onServerRemove(busyWorkerServerMetadata.getServerIdentity());

        assertThat(workerClusters.getDbWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress());
        assertThat(workerClusters.getNormalWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress());
    }

    @Test
    void testOnServerUpdate() {

        WorkerServerMetadata normalWorkerServerMetadata = getNormalWorkerServerMetadata();
        WorkerServerMetadata busyWorkerServerMetadata = getBusyWorkerServerMetadata();

        WorkerClusters workerClusters = new WorkerClusters();
        workerClusters.onServerAdded(normalWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(normalWorkerServerMetadata);
        workerClusters.onServerAdded(busyWorkerServerMetadata.getServerIdentity());
        workerClusters.onServerUpdate(busyWorkerServerMetadata);

        WorkerServerMetadata workerServerMetadata = WorkerServerMetadata.builder()
                .address("127.0.0.2:1235")
                .cpuUsage(0.3)
                .memoryUsage(0.3)
                .serverStatus(ServerStatus.NORMAL)
                .taskThreadPoolUsage(0.3)
                .workerWeight(2)
                .build();

        workerClusters.onServerUpdate(workerServerMetadata);

        assertThat(workerClusters.getDbWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress(), workerServerMetadata.getAddress());
        assertThat(workerClusters.getNormalWorkerServerAddressByGroup("default"))
                .containsExactly(normalWorkerServerMetadata.getAddress(), workerServerMetadata.getAddress());
        assertThat(workerClusters.getServers()).containsExactly(normalWorkerServerMetadata, workerServerMetadata);
    }

    @Test
    void testFirstMetadataPopulatesConfiguredWorkerGroup() {
        WorkerClusters clusters = new WorkerClusters();
        WorkerServerMetadata registration = WorkerServerMetadata.builder().address("localhost:1234")
                .workerGroup(null).serverStartupTime(1).processId(10).build();
        clusters.onServerAdded(registration.getServerIdentity());
        assertThat(clusters.getNormalWorkerServerAddressByGroup("default")).isEmpty();
        WorkerServerMetadata metadata = WorkerServerMetadata.builder().address("localhost:1234")
                .workerGroup("analytics").serverStartupTime(1).processId(10).serverStatus(ServerStatus.NORMAL).build();
        clusters.onServerUpdate(metadata);
        assertThat(clusters.getNormalWorkerServerAddressByGroup("analytics")).containsExactly("localhost:1234");
        clusters.onServerRemove(registration.getServerIdentity());
        assertThat(clusters.getServers()).isEmpty();
        assertThat(clusters.getConfigWorkerServerAddressByGroup("analytics")).isEmpty();
    }

    @Test
    void testWorkerGroupChangeUpdatesMapping() {
        WorkerClusters clusters = new WorkerClusters();
        clusters.onServerUpdate(WorkerServerMetadata.builder().address("localhost:1234").workerGroup("old").build());
        clusters.onServerUpdate(WorkerServerMetadata.builder().address("localhost:1234").workerGroup("new").build());
        assertThat(clusters.getConfigWorkerServerAddressByGroup("old")).isEmpty();
        assertThat(clusters.getConfigWorkerServerAddressByGroup("new")).containsExactly("localhost:1234");
    }

    @Test
    void testRemovalOfPreviousProcessKeepsRestartedWorker() {
        WorkerClusters clusters = new WorkerClusters();
        WorkerServerMetadata restarted = WorkerServerMetadata.builder().address("localhost:1234")
                .serverStartupTime(2).workerGroup("new").build();
        clusters.onServerAdded(restarted.getServerIdentity());
        clusters.onServerUpdate(restarted);
        clusters.onServerRemove(WorkerServerMetadata.builder().address("localhost:1234")
                .serverStartupTime(1).workerGroup("old").build().getServerIdentity());
        assertThat(clusters.getServers()).containsExactly(restarted);
        assertThat(clusters.getConfigWorkerServerAddressByGroup("new")).containsExactly("localhost:1234");
    }

    private WorkerServerMetadata getNormalWorkerServerMetadata() {
        return WorkerServerMetadata.builder()
                .address("127.0.0.1:1235")
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .taskThreadPoolUsage(0.6)
                .workerWeight(2)
                .build();
    }

    private WorkerServerMetadata getBusyWorkerServerMetadata() {
        return WorkerServerMetadata.builder()
                .address("127.0.0.2:1235")
                .cpuUsage(0.8)
                .memoryUsage(0.8)
                .serverStatus(ServerStatus.BUSY)
                .taskThreadPoolUsage(0.9)
                .workerWeight(2)
                .build();
    }
}
