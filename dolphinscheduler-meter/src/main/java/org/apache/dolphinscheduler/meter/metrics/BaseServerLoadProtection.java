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

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseServerLoadProtection implements ServerLoadProtection {

    protected final BaseServerLoadProtectionConfig baseServerLoadProtectionConfig;

    public BaseServerLoadProtection(BaseServerLoadProtectionConfig baseServerLoadProtectionConfig) {
        this.baseServerLoadProtectionConfig = baseServerLoadProtectionConfig;
    }

    @PostConstruct
    public void init() {
        checkDeprecatedConfig();
    }

    /**
     * Check if deprecated configuration is used and log warning
     */
    protected void checkDeprecatedConfig() {
        // Check if old config is explicitly set (not default value)
        // We assume if rules list is empty, user might be using old config
        List<DiskUsageThresholdRule> rules = baseServerLoadProtectionConfig.getMaxDiskUsagePercentageThresholdsRules();
        if (rules.isEmpty()) {
            log.warn("Configuration 'max-disk-usage-percentage-thresholds' is deprecated. " +
                    "Please use 'max-disk-usage-percentage-thresholds-rules' instead. " +
                    "Example: max-disk-usage-percentage-thresholds-rules:\n" +
                    "  - disk-path: /data\n" +
                    "    usage-percentage-thresholds: 0.8");
        }
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
        if (isDiskOverloaded()) {
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

    /**
     * Check if any monitored disk is overloaded
     */
    protected boolean isDiskOverloaded() {
        List<DiskUsageThresholdRule> rules = baseServerLoadProtectionConfig.getMaxDiskUsagePercentageThresholdsRules();

        // If no rules configured, fall back to deprecated config
        if (rules.isEmpty()) {
            return isDiskOverloadedWithDeprecatedConfig();
        }

        // Check each configured path
        for (DiskUsageThresholdRule rule : rules) {
            if (isDiskPathOverloaded(rule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check disk overload using deprecated configuration
     */
    @SuppressWarnings("deprecation")
    protected boolean isDiskOverloadedWithDeprecatedConfig() {
        double threshold = baseServerLoadProtectionConfig.getMaxDiskUsagePercentageThresholds();
        // Get system root disk usage
        double diskUsage = getDiskUsageForPath("/");
        if (diskUsage > threshold) {
            log.info("OverLoad: the DiskUsedPercentage: {} is over then the maxDiskUsagePercentageThresholds {}",
                    diskUsage, threshold);
            return true;
        }
        return false;
    }

    /**
     * Check if specific disk path is overloaded
     */
    protected boolean isDiskPathOverloaded(DiskUsageThresholdRule rule) {
        String path = rule.getDiskPath();
        double threshold = rule.getUsagePercentageThresholds();
        double usage = getDiskUsageForPath(path);

        if (usage > threshold) {
            log.info("OverLoad: the Disk {} usage: {} is over then the threshold {}",
                    path, usage, threshold);
            return true;
        }
        return false;
    }

    /**
     * Get disk usage percentage for a specific path
     */
    protected double getDiskUsageForPath(String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                log.warn("Disk path {} does not exist, skipping", pathStr);
                return 0.0;
            }
            FileStore fileStore = Files.getFileStore(path);
            long total = fileStore.getTotalSpace();
            long usable = fileStore.getUsableSpace();
            long used = total - usable;
            if (total <= 0) {
                return 0.0;
            }
            return (double) used / total;
        } catch (Exception e) {
            log.warn("Failed to get disk usage for path: {}", pathStr, e);
            return 0.0;
        }
    }
}
