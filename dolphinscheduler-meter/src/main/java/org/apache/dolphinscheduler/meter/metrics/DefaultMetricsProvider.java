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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

@Slf4j
public class DefaultMetricsProvider implements MetricsProvider {

    private final MeterRegistry meterRegistry;

    // Data basedir path constant
    private static final String DEFAULT_DATA_BASEDIR_PATH = "/tmp/dolphinscheduler";

    public DefaultMetricsProvider(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        registerDataBasedirPathMetrics();
    }

    private SystemMetrics systemMetrics;

    private long lastRefreshTime = 0;

    private double lastSystemCpuUsage = 0.0d;
    private double lastProcessCpuUsage = 0.0d;

    private static final long SYSTEM_METRICS_REFRESH_INTERVAL = 1_000L;

    // Data basedir path metrics
    private double dataBasedirPathTotalBytes = 0.0;
    private double dataBasedirPathFreeBytes = 0.0;
    private volatile boolean dataBasedirPathMetricsRegistered = false;
    private String registeredDataBasedirPath = "";

    /**
     * Register data basedir path metrics to micrometer
     */
    private void registerDataBasedirPathMetrics() {
        try {
            String dataBasedirPath = getDataBasedirPath();
            Path path = Paths.get(dataBasedirPath);

            // Check if path exists, if not, try to create it
            if (!Files.exists(path)) {
                log.info("Data basedir path {} does not exist, trying to create it", dataBasedirPath);
                Files.createDirectories(path);
            }

            // Register gauges for data basedir path disk usage
            Gauge.builder("data.basedir.path.total", (Supplier<Number>) this::getDataBasedirPathTotalBytes)
                    .description("Total space of data basedir path")
                    .tag("path", dataBasedirPath)
                    .register(meterRegistry);

            Gauge.builder("data.basedir.path.free", (Supplier<Number>) this::getDataBasedirPathFreeBytes)
                    .description("Free space of data basedir path")
                    .tag("path", dataBasedirPath)
                    .register(meterRegistry);

            Gauge.builder("data.basedir.path.used", (Supplier<Number>) this::getDataBasedirPathUsedBytes)
                    .description("Used space of data basedir path")
                    .tag("path", dataBasedirPath)
                    .register(meterRegistry);

            Gauge.builder("data.basedir.path.used.percentage",
                    (Supplier<Number>) this::getDataBasedirPathUsedPercentage)
                    .description("Used space percentage of data basedir path")
                    .tag("path", dataBasedirPath)
                    .register(meterRegistry);

            registeredDataBasedirPath = dataBasedirPath;
            dataBasedirPathMetricsRegistered = true;
            log.info("Successfully registered data basedir path metrics for path: {}", dataBasedirPath);
        } catch (Exception e) {
            log.warn("Failed to register data basedir path metrics", e);
        }
    }

    /**
     * Get data basedir path from configuration
     * @return data basedir path
     */
    private String getDataBasedirPath() {
        try {
            return PropertyUtils.getString(Constants.DATA_BASEDIR_PATH, DEFAULT_DATA_BASEDIR_PATH);
        } catch (Exception e) {
            log.warn("Failed to get data.basedir.path from configuration, using default: {}", DEFAULT_DATA_BASEDIR_PATH,
                    e);
            return DEFAULT_DATA_BASEDIR_PATH;
        }
    }

    /**
     * Refresh data basedir path disk usage metrics
     */
    private void refreshDataBasedirPathMetrics() {
        try {
            if (!dataBasedirPathMetricsRegistered) {
                return;
            }

            String dataBasedirPath = getDataBasedirPath();
            // If the path has changed, we should re-register the metrics
            if (!registeredDataBasedirPath.equals(dataBasedirPath)) {
                log.info("Data basedir path changed from {} to {}, re-registering metrics", registeredDataBasedirPath,
                        dataBasedirPath);
                registerDataBasedirPathMetrics();
                return;
            }

            Path path = Paths.get(dataBasedirPath);
            FileStore fileStore = Files.getFileStore(path);

            dataBasedirPathTotalBytes = fileStore.getTotalSpace();
            dataBasedirPathFreeBytes = fileStore.getUsableSpace();
        } catch (Exception e) {
            log.warn("Failed to refresh data basedir path metrics", e);
        }
    }

    // Getters for data basedir path metrics
    public double getDataBasedirPathTotalBytes() {
        return dataBasedirPathTotalBytes;
    }

    public double getDataBasedirPathFreeBytes() {
        return dataBasedirPathFreeBytes;
    }

    public double getDataBasedirPathUsedBytes() {
        return getDataBasedirPathTotalBytes() - getDataBasedirPathFreeBytes();
    }

    public double getDataBasedirPathUsedPercentage() {
        double total = getDataBasedirPathTotalBytes();
        if (total <= 0) {
            return 0.0;
        }
        return getDataBasedirPathUsedBytes() / total;
    }

    @Override
    public SystemMetrics getSystemMetrics() {
        if (System.currentTimeMillis() - lastRefreshTime < SYSTEM_METRICS_REFRESH_INTERVAL) {
            return systemMetrics;
        }

        // Refresh data basedir path metrics
        refreshDataBasedirPathMetrics();

        double systemCpuUsage = meterRegistry.get("system.cpu.usage").gauge().value();
        if (Double.compare(systemCpuUsage, Double.NaN) == 0) {
            systemCpuUsage = lastSystemCpuUsage;
        } else {
            lastSystemCpuUsage = systemCpuUsage;
        }
        double processCpuUsage = meterRegistry.get("process.cpu.usage").gauge().value();
        if (Double.compare(processCpuUsage, Double.NaN) == 0) {
            processCpuUsage = lastProcessCpuUsage;
        } else {
            lastProcessCpuUsage = processCpuUsage;
        }

        // Calculate JVM memory usage and maximum values
        double jvmHeapUsed = calculateTotalMemory(meterRegistry, "heap", "jvm.memory.used");
        double jvmNonHeapUsed = calculateTotalMemory(meterRegistry, "nonheap", "jvm.memory.used");

        double jvmHeapMax = calculateTotalMemory(meterRegistry, "heap", "jvm.memory.max");
        double jvmNonHeapMax = calculateTotalMemory(meterRegistry, "nonheap", "jvm.memory.max");

        // Calculate totals
        double jvmMemoryUsed = jvmHeapUsed + jvmNonHeapUsed;
        double jvmMemoryMax = jvmHeapMax + jvmNonHeapMax;

        // Ensure jvmMemoryMax is not zero
        double jvmMemoryUsedPercentage = (jvmMemoryMax > 0) ? (jvmMemoryUsed / jvmMemoryMax) : 0.0;

        long totalSystemMemory = OSUtils.getTotalSystemMemory();
        long systemMemoryAvailable = OSUtils.getSystemAvailableMemoryUsed();

        double diskToTalBytes = meterRegistry.get("disk.total").gauge().value();
        double diskFreeBytes = meterRegistry.get("disk.free").gauge().value();

        systemMetrics = SystemMetrics.builder()
                .systemCpuUsagePercentage(systemCpuUsage)
                .jvmCpuUsagePercentage(processCpuUsage)
                .jvmMemoryUsed(jvmMemoryUsed)
                .jvmMemoryMax(jvmMemoryMax)
                .jvmHeapUsed(jvmHeapUsed)
                .jvmHeapMax(jvmHeapMax)
                .jvmNonHeapUsed(jvmNonHeapUsed)
                .jvmNonHeapMax(jvmNonHeapMax)
                .jvmMemoryUsedPercentage(jvmMemoryUsedPercentage)
                .systemMemoryUsed(totalSystemMemory - systemMemoryAvailable)
                .systemMemoryMax(totalSystemMemory)
                .systemMemoryUsedPercentage((double) (totalSystemMemory - systemMemoryAvailable) / totalSystemMemory)
                .diskUsed(diskToTalBytes - diskFreeBytes)
                .diskTotal(diskToTalBytes)
                .diskUsedPercentage((diskToTalBytes - diskFreeBytes) / diskToTalBytes)
                .dataBasedirPathUsed(getDataBasedirPathUsedBytes())
                .dataBasedirPathTotal(getDataBasedirPathTotalBytes())
                .dataBasedirPathUsedPercentage(getDataBasedirPathUsedPercentage())
                .build();
        lastRefreshTime = System.currentTimeMillis();
        return systemMetrics;
    }

    /**
     * Calculate the total memory usage for a specified area
     * This method calculates the total memory usage by iterating over all meters in the MeterRegistry that match the given name
     * It only sums up meters that have the same area tag and a value greater than 0
     *
     * @param meterRegistry A MeterRegistry instance used to retrieve memory data
     * @param area          The memory area type ("heap" or "nonheap")
     * @param name          The meter name to match, used to find related meters in the MeterRegistry
     * @return The total memory usage for the specified area
     */
    private double calculateTotalMemory(MeterRegistry meterRegistry, String area, String name) {
        double memory = 0.0;
        Iterable<Meter> meters = meterRegistry.find(name).meters();
        for (Meter meter : meters) {
            if (area.equals(meter.getId().getTag("area"))) {
                double value = meter.measure().iterator().next().getValue();
                // Ignore undefined maximum values (-1)
                if (value > 0) {
                    memory += value;
                }
            }
        }
        return memory;
    }

}
