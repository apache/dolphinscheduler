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

import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MasterServerLoadProtectionConfig {

    @Bean
    public MasterServerLoadProtection masterServerLoadProtection(
                                                                 IWorkflowRepository workflowRepository,
                                                                 @Value("${master.server-load-protection.max-concurrent-workflow-instances:2147483647}") int maxConcurrentWorkflowInstances,
                                                                 @Value("${master.server-load-protection.max-system-cpu-usage-percentage-thresholds:0.7}") double maxSystemCpuUsagePercentageThresholds,
                                                                 @Value("${master.server-load-protection.max-jvm-cpu-usage-percentage-thresholds:0.7}") double maxJvmCpuUsagePercentageThresholds,
                                                                 @Value("${master.server-load-protection.max-system-memory-usage-percentage-thresholds:0.7}") double maxSystemMemoryUsagePercentageThresholds,
                                                                 @Value("${master.server-load-protection.max-disk-usage-percentage-thresholds:0.7}") double maxDiskUsagePercentageThresholds,
                                                                 @Value("${master.server-load-protection.enabled:true}") boolean enabled) {
        MasterServerLoadProtection protection =
                new MasterServerLoadProtection(workflowRepository,
                        maxConcurrentWorkflowInstances,
                        maxSystemCpuUsagePercentageThresholds,
                        maxJvmCpuUsagePercentageThresholds,
                        maxSystemMemoryUsagePercentageThresholds,
                        maxDiskUsagePercentageThresholds,
                        enabled);
        log.info(
                "Initialized MasterServerLoadProtection with IWorkflowRepository and maxConcurrentWorkflowInstances={}, "
                        +
                        "maxSystemCpuUsagePercentageThresholds={}, maxJvmCpuUsagePercentageThresholds={}, " +
                        "maxSystemMemoryUsagePercentageThresholds={}, maxDiskUsagePercentageThresholds={}, enabled={}",
                maxConcurrentWorkflowInstances, maxSystemCpuUsagePercentageThresholds,
                maxJvmCpuUsagePercentageThresholds,
                maxSystemMemoryUsagePercentageThresholds, maxDiskUsagePercentageThresholds, enabled);
        return protection;
    }
}
