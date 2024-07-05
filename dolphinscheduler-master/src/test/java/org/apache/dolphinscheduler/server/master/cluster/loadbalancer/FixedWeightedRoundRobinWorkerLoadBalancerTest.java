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

package org.apache.dolphinscheduler.server.master.cluster.loadbalancer;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;
import org.apache.dolphinscheduler.server.master.cluster.WorkerServerMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

class FixedWeightedRoundRobinWorkerLoadBalancerTest {

    @Test
    void select_defaultWorkerGroup() {
        FixedWeightedRoundRobinWorkerLoadBalancer loadBalancer = createFixedWeightedRoundRobinWorkerLoadBalancer();

        // select 10000 times to check if the selected worker is in the worker list
        Map<String, Integer> workerSelectedCount = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Optional<String> selectedWorker = loadBalancer.select("default");
            Truth.assertThat(selectedWorker).isPresent();
            workerSelectedCount.put(selectedWorker.get(),
                    workerSelectedCount.getOrDefault(selectedWorker.get(), 0) + 1);
        }
        // Assert the three workers selected times are similar
        Integer times1 = workerSelectedCount.get("127.0.0.1:1234");
        Integer times2 = workerSelectedCount.get("127.0.0.2:1234");
        Integer times3 = workerSelectedCount.get("127.0.0.3:1234");
        Truth.assertThat(Math.abs(times2 - times1 * 2)).isWithin(10);
        Truth.assertThat(Math.abs(times3 - times1 * 3)).isWithin(10);
        Truth.assertThat(times1 + times2 + times3).isEqualTo(10000);
    }

    @Test
    void select_emptyWorkerGroup() {
        FixedWeightedRoundRobinWorkerLoadBalancer loadBalancer = createFixedWeightedRoundRobinWorkerLoadBalancer();
        Truth.assertThat(loadBalancer.select("busyCluster")).isEmpty();
    }

    @Test
    void select_workerGroupNotExist() {
        FixedWeightedRoundRobinWorkerLoadBalancer loadBalancer = createFixedWeightedRoundRobinWorkerLoadBalancer();
        Truth.assertThat(loadBalancer.select("notExistCluster")).isEmpty();
    }

    private FixedWeightedRoundRobinWorkerLoadBalancer createFixedWeightedRoundRobinWorkerLoadBalancer() {
        List<WorkerGroup> workerGroups = Lists.newArrayList(
                WorkerGroup.builder()
                        .name("sparkCluster")
                        .addrList("127.0.0.1:1234")
                        .build(),
                WorkerGroup.builder()
                        .name("flinkCluster")
                        .build(),
                WorkerGroup.builder()
                        .name("busyCluster")
                        .build());
        List<WorkerServerMetadata> workerServers = Lists.newArrayList(
                WorkerServerMetadata.builder()
                        .address("127.0.0.1:1234")
                        .serverStatus(ServerStatus.NORMAL)
                        .cpuUsage(0.1)
                        .memoryUsage(0.1)
                        .workerWeight(1)
                        .build(),
                WorkerServerMetadata.builder()
                        .address("127.0.0.2:1234")
                        .serverStatus(ServerStatus.NORMAL)
                        .cpuUsage(0.2)
                        .memoryUsage(0.2)
                        .workerWeight(2)
                        .build(),
                WorkerServerMetadata.builder()
                        .address("127.0.0.3:1234")
                        .serverStatus(ServerStatus.NORMAL)
                        .cpuUsage(0.3)
                        .memoryUsage(0.3)
                        .workerWeight(3)
                        .build(),
                WorkerServerMetadata.builder()
                        .address("127.0.0.4:1234")
                        .serverStatus(ServerStatus.BUSY)
                        .cpuUsage(0.8)
                        .memoryUsage(0.8)
                        .build());
        WorkerClusters workerClusters = new WorkerClusters();
        FixedWeightedRoundRobinWorkerLoadBalancer loadBalancer =
                new FixedWeightedRoundRobinWorkerLoadBalancer(workerClusters);
        workerClusters.onWorkerGroupChange(workerGroups);
        workerServers.forEach(workerClusters::onServerAdded);

        return loadBalancer;
    }
}
