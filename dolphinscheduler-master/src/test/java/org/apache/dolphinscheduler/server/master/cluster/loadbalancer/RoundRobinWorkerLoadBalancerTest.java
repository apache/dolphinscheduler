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

import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class RoundRobinWorkerLoadBalancerTest extends BaseWorkerLoadBalancerTest {

    @Test
    void select_defaultWorkerGroup() {
        WorkerClusters defaultWorkerClusters = getDefaultWorkerClusters();
        RoundRobinWorkerLoadBalancer roundRobinWorkerLoadBalancer =
                new RoundRobinWorkerLoadBalancer(defaultWorkerClusters);
        // select 10000 times to check if the selected worker is in the worker list
        Map<String, Integer> workerSelectedCount = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Optional<String> selectedWorker = roundRobinWorkerLoadBalancer.select("default");
            Truth.assertThat(selectedWorker).isPresent();
            workerSelectedCount.put(selectedWorker.get(),
                    workerSelectedCount.getOrDefault(selectedWorker.get(), 0) + 1);
        }
        // Assert the three workers selected times are similar
        Integer times1 = workerSelectedCount.get("127.0.0.1:1234");
        Integer times2 = workerSelectedCount.get("127.0.0.2:1234");
        Integer times3 = workerSelectedCount.get("127.0.0.3:1234");
        Truth.assertThat(Math.abs(times1 - times2)).isWithin(10);
        Truth.assertThat(Math.abs(times1 - times3)).isWithin(10);
        Truth.assertThat(Math.abs(times2 - times3)).isWithin(10);
    }

    @Test
    void select_emptyWorkerGroup() {
        WorkerClusters defaultWorkerClusters = getDefaultWorkerClusters();
        RoundRobinWorkerLoadBalancer roundRobinWorkerLoadBalancer =
                new RoundRobinWorkerLoadBalancer(defaultWorkerClusters);
        Truth.assertThat(roundRobinWorkerLoadBalancer.select("busyCluster")).isEmpty();
    }

    @Test
    void select_workerGroupNotExist() {
        WorkerClusters defaultWorkerClusters = getDefaultWorkerClusters();
        RoundRobinWorkerLoadBalancer roundRobinWorkerLoadBalancer =
                new RoundRobinWorkerLoadBalancer(defaultWorkerClusters);
        Truth.assertThat(roundRobinWorkerLoadBalancer.select("notExistCluster")).isEmpty();
    }
}
