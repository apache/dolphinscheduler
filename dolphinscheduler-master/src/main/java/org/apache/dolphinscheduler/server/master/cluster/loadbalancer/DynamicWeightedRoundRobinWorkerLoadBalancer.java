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

import org.apache.dolphinscheduler.server.master.cluster.IClusters;
import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;
import org.apache.dolphinscheduler.server.master.cluster.WorkerServerMetadata;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * This load balancer is used to select a worker from {@link WorkerClusters} by dynamic weights.
 * </p>
 * The dynamic weights are calculated by the worker's load. e.g. cpu/memory/disk usage/thread usage etc.
 * You can config the weight calculation strategy in {@link WorkerLoadBalancerConfigurationProperties.DynamicWeightConfigProperties}.
 */
public class DynamicWeightedRoundRobinWorkerLoadBalancer implements IWorkerLoadBalancer {

    private final WorkerClusters workerClusters;

    private final AtomicInteger robinIndex = new AtomicInteger(0);

    private Map<String, WeightedServer<WorkerServerMetadata>> weightedServerMap = new ConcurrentHashMap<>();

    public DynamicWeightedRoundRobinWorkerLoadBalancer(WorkerClusters workerClusters,
                                                       WorkerLoadBalancerConfigurationProperties.DynamicWeightConfigProperties dynamicWeightConfigProperties) {
        this.workerClusters = workerClusters;
        this.workerClusters.registerListener(new IClusters.IClustersChangeListener<WorkerServerMetadata>() {

            @Override
            public void onServerAdded(WorkerServerMetadata server) {
                weightedServerMap.put(server.getAddress(), new WeightedServer<>(server, calculateWeight(server)));
            }

            @Override
            public void onServerRemove(WorkerServerMetadata server) {
                weightedServerMap.remove(server.getAddress());
            }

            @Override
            public void onServerUpdate(WorkerServerMetadata server) {
                weightedServerMap.put(server.getAddress(), new WeightedServer<>(server, calculateWeight(server)));
            }

            private double calculateWeight(WorkerServerMetadata server) {
                return 100 - (dynamicWeightConfigProperties.getCpuUsageWeight() * server.getCpuUsage()
                        + dynamicWeightConfigProperties.getMemoryUsageWeight() * server.getMemoryUsage()
                        + dynamicWeightConfigProperties.getTaskThreadPoolUsageWeight()
                                * server.getTaskThreadPoolUsage())
                        / 3;
            }
        });
    }

    @Override
    public Optional<String> select(@NotNull String workerGroup) {
        List<WeightedServer<WorkerServerMetadata>> weightedServers =
                workerClusters.getNormalWorkerServerAddressByGroup(workerGroup)
                        .stream()
                        .map(weightedServerMap::get)
                        .filter(Objects::nonNull) // filter non null here to avoid the two map changed between
                        // workerClusters
                        // and weightedServerMap is not atomic
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(weightedServers)) {
            return Optional.empty();
        }

        double totalWeight = weightedServers.stream().mapToDouble(WeightedServer::getWeight).sum();

        WeightedServer<WorkerServerMetadata> selectedWorker = null;
        while (selectedWorker == null) {
            WeightedServer<WorkerServerMetadata> tmpWorker =
                    weightedServers.get((robinIndex.incrementAndGet()) % weightedServers.size());
            tmpWorker.setCurrentWeight(tmpWorker.getCurrentWeight() + tmpWorker.getWeight());

            if (tmpWorker.getCurrentWeight() >= totalWeight) {
                tmpWorker.setCurrentWeight(tmpWorker.getCurrentWeight() - totalWeight);
                selectedWorker = tmpWorker;
            }
        }

        return Optional.of(selectedWorker.getServer().getAddress());
    }

    @Override
    public WorkerLoadBalancerType getType() {
        return WorkerLoadBalancerType.DYNAMIC_WEIGHTED_ROUND_ROBIN;
    }
}
