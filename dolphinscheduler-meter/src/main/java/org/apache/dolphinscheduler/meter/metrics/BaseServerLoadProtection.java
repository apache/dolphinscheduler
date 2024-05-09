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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BaseServerLoadProtection implements ServerLoadProtection {

    protected boolean enabled = true;

    protected double maxSystemCpuUsagePercentageThresholds = 0.7;

    protected double maxJvmCpuUsagePercentageThresholds = 0.7;

    protected double maxSystemMemoryUsagePercentageThresholds = 0.7;

    protected double maxDiskUsagePercentageThresholds = 0.7;

    @Override
    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!enabled) {
            return false;
        }
        if (systemMetrics.getSystemCpuUsagePercentage() > maxSystemCpuUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the system cpu usage: {} is over then the maxSystemCpuUsagePercentageThresholds {}",
                    systemMetrics.getSystemCpuUsagePercentage(), maxSystemCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getJvmCpuUsagePercentage() > maxJvmCpuUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the jvm cpu usage: {} is over then the maxJvmCpuUsagePercentageThresholds {}",
                    systemMetrics.getJvmCpuUsagePercentage(), maxJvmCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getDiskUsedPercentage() > maxDiskUsagePercentageThresholds) {
            log.info("OverLoad: the DiskUsedPercentage: {} is over then the maxDiskUsagePercentageThresholds {}",
                    systemMetrics.getDiskUsedPercentage(), maxDiskUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getSystemMemoryUsedPercentage() > maxSystemMemoryUsagePercentageThresholds) {
            log.info(
                    "OverLoad: the SystemMemoryUsedPercentage: {} is over then the maxSystemMemoryUsagePercentageThresholds {}",
                    systemMetrics.getSystemMemoryUsedPercentage(), maxSystemMemoryUsagePercentageThresholds);
            return true;
        }
        return false;
    }
}
