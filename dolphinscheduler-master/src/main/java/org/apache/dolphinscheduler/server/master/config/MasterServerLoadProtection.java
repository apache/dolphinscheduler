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

import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterServerLoadProtection {

    private boolean enabled = true;

    private double maxCpuUsagePercentageThresholds = 0.7;

    private double maxJVMMemoryUsagePercentageThresholds = 0.7;

    private double maxSystemMemoryUsagePercentageThresholds = 0.7;

    private double maxDiskUsagePercentageThresholds = 0.7;

    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!enabled) {
            return false;
        }
        if (systemMetrics.getTotalCpuUsedPercentage() > maxCpuUsagePercentageThresholds) {
            log.info(
                    "Master OverLoad: the TotalCpuUsedPercentage: {} is over then the MaxCpuUsagePercentageThresholds {}",
                    systemMetrics.getTotalCpuUsedPercentage(), maxCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getJvmMemoryUsedPercentage() > maxJVMMemoryUsagePercentageThresholds) {
            log.info(
                    "Master OverLoad: the JvmMemoryUsedPercentage: {} is over then the MaxJVMMemoryUsagePercentageThresholds {}",
                    systemMetrics.getJvmMemoryUsedPercentage(), maxCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getDiskUsedPercentage() > maxDiskUsagePercentageThresholds) {
            log.info("Master OverLoad: the DiskUsedPercentage: {} is over then the MaxDiskUsagePercentageThresholds {}",
                    systemMetrics.getDiskUsedPercentage(), maxCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getSystemMemoryUsedPercentage() > maxSystemMemoryUsagePercentageThresholds) {
            log.info(
                    "Master OverLoad: the SystemMemoryUsedPercentage: {} is over then the MaxSystemMemoryUsagePercentageThresholds {}",
                    systemMetrics.getSystemMemoryUsedPercentage(), maxSystemMemoryUsagePercentageThresholds);
            return true;
        }
        return false;
    }

}
