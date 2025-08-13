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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MasterServerLoadProtectionTest {

    private IWorkflowRepository mockRepository;

    @BeforeEach
    public void setUp() {
        mockRepository = Mockito.mock(IWorkflowRepository.class);
        Mockito.when(mockRepository.getAll()).thenReturn(Collections.emptyList());
    }

    @Test
    void isOverload() {
        final MasterConfig masterConfig = new MasterConfig();
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, masterConfig);

        SystemMetrics systemMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.71)
                .systemMemoryUsedPercentage(0.71)
                .systemCpuUsagePercentage(0.71)
                .jvmCpuUsagePercentage(0.71)
                .diskUsedPercentage(0.71)
                .build();

        masterConfig.getServerLoadProtection().setEnabled(false);
        assertFalse(masterServerLoadProtection.isOverload(systemMetrics));

        masterConfig.getServerLoadProtection().setEnabled(true);
        assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
    }

    @Test
    void isOverloadWithCustomThresholds() {
        /**
         * Set custom thresholds higher than the metrics values.
         * With higher thresholds, the system should not be overloaded
         */
        final MasterConfig masterConfig = new MasterConfig();
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, masterConfig);

        SystemMetrics systemMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.71)
                .systemMemoryUsedPercentage(0.71)
                .systemCpuUsagePercentage(0.71)
                .jvmCpuUsagePercentage(0.71)
                .diskUsedPercentage(0.71)
                .build();

        masterConfig.getServerLoadProtection().setMaxJvmCpuUsagePercentageThresholds(0.8);
        masterConfig.getServerLoadProtection().setMaxSystemCpuUsagePercentageThresholds(0.8);
        masterConfig.getServerLoadProtection().setMaxSystemMemoryUsagePercentageThresholds(0.8);
        masterConfig.getServerLoadProtection().setMaxDiskUsagePercentageThresholds(0.8);
        assertFalse(masterServerLoadProtection.isOverload(systemMetrics));

        /**
         * Now set custom thresholds lower than the metrics values.
         * With a lower system CPU, Memory & Disk threshold, the system should be overloaded.
         */
        masterConfig.getServerLoadProtection().setMaxJvmCpuUsagePercentageThresholds(0.7);
        assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
        masterConfig.getServerLoadProtection().setMaxJvmCpuUsagePercentageThresholds(0.8);

        masterConfig.getServerLoadProtection().setMaxSystemCpuUsagePercentageThresholds(0.7);
        assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
        masterConfig.getServerLoadProtection().setMaxSystemCpuUsagePercentageThresholds(0.8);

        masterConfig.getServerLoadProtection().setMaxSystemMemoryUsagePercentageThresholds(0.7);
        assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
        masterConfig.getServerLoadProtection().setMaxSystemMemoryUsagePercentageThresholds(0.8);

        masterConfig.getServerLoadProtection().setMaxDiskUsagePercentageThresholds(0.7);
        assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
        masterConfig.getServerLoadProtection().setMaxDiskUsagePercentageThresholds(0.8);

    }

    @Test
    void isOverloadWithMaxConcurrentWorkflowInstances() {
        Mockito.when(mockRepository.getAll())
                .thenReturn(Collections.nCopies(5, Mockito.mock(IWorkflowExecutionRunnable.class)));

        // With a workflow count below the threshold, the system should not be overloaded.
        MasterConfig masterConfig = new MasterConfig();
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, masterConfig);

        assertFalse(masterServerLoadProtection.isOverload(SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .build()));

        // With a workflow count anything >= maxConcurrentWorkflowInstances threshold, the system should be overloaded.
        masterConfig.getServerLoadProtection().setMaxConcurrentWorkflowInstances(5);
        assertTrue(masterServerLoadProtection.isOverload(SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .build()));
    }

}
