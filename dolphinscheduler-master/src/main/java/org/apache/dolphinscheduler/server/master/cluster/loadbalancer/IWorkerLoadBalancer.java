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

import java.util.Optional;

import lombok.NonNull;

/**
 * The worker load balancer used to select a worker from the {@link WorkerClusters} by load balancer algorithm.
 */
public interface IWorkerLoadBalancer {

    /**
     * Select a worker address under the given worker group.
     *
     * @param workerGroup worker group cannot be null.
     * @return the selected worker address, or empty if no worker is available.
     */
    Optional<String> select(@NonNull String workerGroup);

    WorkerLoadBalancerType getType();

}
