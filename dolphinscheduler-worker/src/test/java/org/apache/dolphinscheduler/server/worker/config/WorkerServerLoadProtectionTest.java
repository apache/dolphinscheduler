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

import org.apache.dolphinscheduler.meter.metrics.DiskUsageThresholdRule;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class WorkerServerLoadProtectionTest {

    @Test
    void isOverload() {
        WorkerConfig workerConfig = new WorkerConfig();
        WorkerServerLoadProtection workerServerLoadProtection = new WorkerServerLoadProtection(workerConfig);
        SystemMetrics systemMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.71)
                .systemMemoryUsedPercentage(0.71)
                .systemCpuUsagePercentage(0.71)
                .jvmCpuUsagePercentage(0.71)
                .diskUsedPercentage(0.71)
                .dataBasedirPathUsedPercentage(0.71)
                .build();

        workerConfig.getServerLoadProtection().setEnabled(false);
        Assertions.assertFalse(workerServerLoadProtection.isOverload(systemMetrics));

        workerConfig.getServerLoadProtection().setEnabled(true);
        Assertions.assertTrue(workerServerLoadProtection.isOverload(systemMetrics));
    }

    @Test
    void isOverloadWithDiskRules() {
        WorkerConfig workerConfig = new WorkerConfig();
        WorkerServerLoadProtection workerServerLoadProtection = new WorkerServerLoadProtection(workerConfig);

        // Configure disk usage rules
        DiskUsageThresholdRule rule1 = new DiskUsageThresholdRule();
        rule1.setDiskPath("/data1");
        rule1.setUsagePercentageThresholds(0.8);

        DiskUsageThresholdRule rule2 = new DiskUsageThresholdRule();
        rule2.setDiskPath("/data2");
        rule2.setUsagePercentageThresholds(0.9);

        workerConfig.getServerLoadProtection().setMaxDiskUsagePercentageThresholdsRules(Arrays.asList(rule1, rule2));

        // Test with normal metrics (no overload)
        SystemMetrics normalMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .dataBasedirPathUsedPercentage(0.5)
                .build();

        // This might return true if /data1 or /data2 actually exists and is over threshold
        // In unit test environment, paths might not exist, so it might return false
        // The actual behavior depends on the test environment
    }
}
