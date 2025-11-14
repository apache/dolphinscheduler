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

package org.apache.dolphinscheduler.meter.metrics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseServerLoadProtection implements ServerLoadProtection {

    protected final BaseServerLoadProtectionConfig baseServerLoadProtectionConfig;

    public BaseServerLoadProtection(BaseServerLoadProtectionConfig baseServerLoadProtectionConfig) {
        this.baseServerLoadProtectionConfig = baseServerLoadProtectionConfig;
    }

    @Override
    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!baseServerLoadProtectionConfig.isEnabled()) {
            return false;
        }
        if (systemMetrics.getSystemCpuUsagePercentage() > baseServerLoadProtectionConfig
                .getMaxSystemCpuUsagePercentageThresholds()) {
            log.info(
                    "OverLoad: the system cpu usage: {} is over then the maxSystemCpuUsagePercentageThresholds {}",
                    systemMetrics.getSystemCpuUsagePercentage(),
                    baseServerLoadProtectionConfig.getMaxSystemCpuUsagePercentageThresholds());
            return true;
        }
        if (systemMetrics.getJvmCpuUsagePercentage() > baseServerLoadProtectionConfig
                .getMaxJvmCpuUsagePercentageThresholds()) {
            log.info(
                    "OverLoad: the jvm cpu usage: {} is over then the maxJvmCpuUsagePercentageThresholds {}",
                    systemMetrics.getJvmCpuUsagePercentage(),
                    baseServerLoadProtectionConfig.getMaxJvmCpuUsagePercentageThresholds());
            return true;
        }
        if (systemMetrics.getDiskUsedPercentage() > baseServerLoadProtectionConfig
                .getMaxDiskUsagePercentageThresholds()) {
            log.info("OverLoad: the DiskUsedPercentage: {} is over then the maxDiskUsagePercentageThresholds {}",
                    systemMetrics.getDiskUsedPercentage(),
                    baseServerLoadProtectionConfig.getMaxDiskUsagePercentageThresholds());
            return true;
        }
        if (systemMetrics.getSystemMemoryUsedPercentage() > baseServerLoadProtectionConfig
                .getMaxSystemMemoryUsagePercentageThresholds()) {
            log.info(
                    "OverLoad: the SystemMemoryUsedPercentage: {} is over then the maxSystemMemoryUsagePercentageThresholds {}",
                    systemMetrics.getSystemMemoryUsedPercentage(),
                    baseServerLoadProtectionConfig.getMaxSystemMemoryUsagePercentageThresholds());
            return true;
        }
        return false;
    }
}
