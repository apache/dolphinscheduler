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

package org.apache.dolphinscheduler.server.master.config;

import org.apache.dolphinscheduler.meter.metrics.BaseServerLoadProtection;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MasterServerLoadProtection extends BaseServerLoadProtection {

    private final int maxConcurrentWorkflowInstances;
    private final IWorkflowRepository workflowRepository;

    public MasterServerLoadProtection(IWorkflowRepository workflowRepository,
                                      int maxConcurrentWorkflowInstances,
                                      double maxSystemCpuUsagePercentageThresholds,
                                      double maxJvmCpuUsagePercentageThresholds,
                                      double maxSystemMemoryUsagePercentageThresholds,
                                      double maxDiskUsagePercentageThresholds,
                                      boolean enabled) {
        this.workflowRepository = workflowRepository;
        this.maxConcurrentWorkflowInstances = maxConcurrentWorkflowInstances;
        this.maxSystemCpuUsagePercentageThresholds = maxSystemCpuUsagePercentageThresholds;
        this.maxJvmCpuUsagePercentageThresholds = maxJvmCpuUsagePercentageThresholds;
        this.maxSystemMemoryUsagePercentageThresholds = maxSystemMemoryUsagePercentageThresholds;
        this.maxDiskUsagePercentageThresholds = maxDiskUsagePercentageThresholds;
        this.enabled = enabled;
    }

    @Override
    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!enabled) {
            return false;
        }

        // Check system metrics first
        if (super.isOverload(systemMetrics)) {
            return true;
        }

        // Check workflow instance count
        int currentWorkflowInstanceCount = workflowRepository.getAll().size();
        if (currentWorkflowInstanceCount >= maxConcurrentWorkflowInstances) {
            log.info(
                    "OverLoad: the workflow instance count: {} exceeds the maxConcurrentWorkflowInstances {}",
                    currentWorkflowInstanceCount, maxConcurrentWorkflowInstances);
            return true;
        }
        return false;
    }
}
