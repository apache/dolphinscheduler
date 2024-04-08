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

package org.apache.dolphinscheduler.server.worker.config;

import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Data
@Slf4j
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "worker-server-load-protection")
public class WorkerServerLoadProtection implements Validator {

    private boolean enabled = true;

    private double maxCpuUsagePercentageThresholds = 0.7;

    private double maxJvmMemoryUsagePercentageThresholds = 0.7;

    private double maxSystemMemoryUsagePercentageThresholds = 0.7;

    private double maxDiskUsagePercentageThresholds = 0.7;

    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!enabled) {
            return false;
        }
        if (systemMetrics.getTotalCpuUsedPercentage() > maxCpuUsagePercentageThresholds) {
            log.info(
                    "Worker OverLoad: the TotalCpuUsedPercentage: {} is over then the MaxCpuUsagePercentageThresholds {}",
                    systemMetrics.getTotalCpuUsedPercentage(), maxCpuUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getJvmMemoryUsedPercentage() > maxJvmMemoryUsagePercentageThresholds) {
            log.info(
                    "Worker OverLoad: the JvmMemoryUsedPercentage: {} is over then the MaxJvmMemoryUsagePercentageThresholds {}",
                    systemMetrics.getJvmMemoryUsedPercentage(), maxJvmMemoryUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getDiskUsedPercentage() > maxDiskUsagePercentageThresholds) {
            log.info("Worker OverLoad: the DiskUsedPercentage: {} is over then the MaxDiskUsagePercentageThresholds {}",
                    systemMetrics.getDiskUsedPercentage(), maxDiskUsagePercentageThresholds);
            return true;
        }
        if (systemMetrics.getSystemMemoryUsedPercentage() > maxSystemMemoryUsagePercentageThresholds) {
            log.info(
                    "Worker OverLoad: the SystemMemoryUsedPercentage: {} is over then the MaxSystemMemoryUsagePercentageThresholds {}",
                    systemMetrics.getSystemMemoryUsedPercentage(), maxSystemMemoryUsagePercentageThresholds);
            return true;
        }
        return false;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return WorkerServerLoadProtection.class.isAssignableFrom(clazz);

    }

    @Override
    public void validate(Object target, Errors errors) {
        WorkerServerLoadProtection serverLoadProtection = (WorkerServerLoadProtection) target;
        if (serverLoadProtection.isEnabled()) {
            if (serverLoadProtection.getMaxCpuUsagePercentageThresholds() <= 0
                    || serverLoadProtection.getMaxCpuUsagePercentageThresholds() > 1) {
                errors.rejectValue("max-cpu-usage-percentage-thresholds", null, "is invalidated");
            }
            if (serverLoadProtection.getMaxJvmMemoryUsagePercentageThresholds() <= 0
                    || serverLoadProtection.getMaxJvmMemoryUsagePercentageThresholds() > 1) {
                errors.rejectValue("max-jvm-memory-usage-percentage-thresholds", null, "is invalidated");
            }
            if (serverLoadProtection.getMaxDiskUsagePercentageThresholds() <= 0
                    || serverLoadProtection.getMaxCpuUsagePercentageThresholds() > 1) {
                errors.rejectValue("max-disk-usage-percentage-thresholds", null, "is invalidated");
            }
            if (serverLoadProtection.getMaxSystemMemoryUsagePercentageThresholds() <= 0
                    || serverLoadProtection.getMaxCpuUsagePercentageThresholds() > 1) {
                errors.rejectValue("max-system-memory-usage-percentage-thresholds", null, "is invalidated");
            }
        }
        printConfig();
    }

    private void printConfig() {
        String config =
                "\n****************************WorkerServerLoadProtection Configuration**************************************"
                        +
                        "\n  master-server-load-protection-enabled -> " + enabled +
                        "\n  max-cpu-usage-percentage-thresholds -> " + maxCpuUsagePercentageThresholds +
                        "\n  max-jvm-memory-usage-percentage-thresholds -> " + maxJvmMemoryUsagePercentageThresholds +
                        "\n  max-disk-usage-percentage-thresholds -> " + maxDiskUsagePercentageThresholds +
                        "\n  max-system-memory-usage-percentage-thresholds -> "
                        + maxSystemMemoryUsagePercentageThresholds +
                        "\n****************************WorkerServerLoadProtection Configuration**************************************";
        log.info(config);
    }
}
