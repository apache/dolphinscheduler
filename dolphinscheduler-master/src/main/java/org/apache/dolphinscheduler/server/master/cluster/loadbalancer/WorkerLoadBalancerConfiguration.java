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

import org.apache.dolphinscheduler.server.master.cluster.ClusterManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerLoadBalancerConfiguration {

    @Bean
    public IWorkerLoadBalancer randomWorkerLoadBalancer(MasterConfig masterConfig, ClusterManager clusterManager) {
        WorkerLoadBalancerConfigurationProperties workerLoadBalancerConfigurationProperties =
                masterConfig.getWorkerLoadBalancerConfigurationProperties();
        switch (workerLoadBalancerConfigurationProperties.getType()) {
            case RANDOM:
                return new RandomWorkerLoadBalancer(clusterManager.getWorkerClusters());
            case ROUND_ROBIN:
                return new RoundRobinWorkerLoadBalancer(clusterManager.getWorkerClusters());
            case FIXED_WEIGHTED_ROUND_ROBIN:
                return new FixedWeightedRoundRobinWorkerLoadBalancer(clusterManager.getWorkerClusters());
            case DYNAMIC_WEIGHTED_ROUND_ROBIN:
                return new DynamicWeightedRoundRobinWorkerLoadBalancer(
                        clusterManager.getWorkerClusters(),
                        workerLoadBalancerConfigurationProperties.getDynamicWeightConfigProperties());
            default:
                throw new IllegalArgumentException(
                        "unSupport worker load balancer type " + workerLoadBalancerConfigurationProperties.getType());
        }
    }

}
