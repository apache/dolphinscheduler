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
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MasterServerLoadProtectionTest {

    private IWorkflowRepository mockRepository;

    private static final double DEFAULT_THRESHOLD = 0.7;
    private static final double LOW_THRESHOLD = 0.5;
    private static final boolean isEnabled = true;

    @BeforeEach
    public void setUp() {
        mockRepository = Mockito.mock(IWorkflowRepository.class);
        Mockito.when(mockRepository.getAll()).thenReturn(Collections.emptyList());
    }

    @Test
    void isOverload() {
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, DEFAULT_THRESHOLD, DEFAULT_THRESHOLD,
                        DEFAULT_THRESHOLD, DEFAULT_THRESHOLD, isEnabled);

        SystemMetrics systemMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.71)
                .systemMemoryUsedPercentage(0.71)
                .systemCpuUsagePercentage(0.71)
                .jvmCpuUsagePercentage(0.71)
                .diskUsedPercentage(0.71)
                .build();

        masterServerLoadProtection.setEnabled(false);
        Assertions.assertFalse(masterServerLoadProtection.isOverload(systemMetrics));

        masterServerLoadProtection.setEnabled(true);
        Assertions.assertTrue(masterServerLoadProtection.isOverload(systemMetrics));
    }

    @Test
    void isOverloadWithCustomThresholds() {
        /**
         * Set custom thresholds higher than the metrics values.
         * With higher thresholds, the system should not be overloaded
         */
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.8, 0.8, 0.8, 0.8, true);

        SystemMetrics systemMetrics = SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.71)
                .systemMemoryUsedPercentage(0.71)
                .systemCpuUsagePercentage(0.71)
                .jvmCpuUsagePercentage(0.71)
                .diskUsedPercentage(0.71)
                .build();

        Assertions.assertFalse(masterServerLoadProtection.isOverload(systemMetrics));

        /**
         * Now set custom thresholds lower than the metrics values.
         * With a lower system CPU, Memory & Disk threshold, the system should be overloaded.
         */
        MasterServerLoadProtection masterServerLoadProtection2 =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.6, 0.8, 0.8, 0.8, true);

        Assertions.assertTrue(masterServerLoadProtection2.isOverload(systemMetrics));

        MasterServerLoadProtection masterServerLoadProtection3 =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.8, 0.6, 0.8, 0.8, true);

        Assertions.assertTrue(masterServerLoadProtection3.isOverload(systemMetrics));

        MasterServerLoadProtection masterServerLoadProtection4 =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.8, 0.8, 0.6, 0.8, true);

        Assertions.assertTrue(masterServerLoadProtection4.isOverload(systemMetrics));

        MasterServerLoadProtection masterServerLoadProtection5 =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.8, 0.8, 0.8, 0.6, true);

        Assertions.assertTrue(masterServerLoadProtection5.isOverload(systemMetrics));

        MasterServerLoadProtection masterServerLoadProtection6 =
                new MasterServerLoadProtection(mockRepository, Integer.MAX_VALUE, 0.6, 0.6, 0.6, 0.6, true);

        Assertions.assertTrue(masterServerLoadProtection6.isOverload(systemMetrics));

    }

    @Test
    void isOverloadWithMaxConcurrentWorkflowInstances() {
        Collection<IWorkflowExecutionRunnable> mockWorkflows =
                Collections.nCopies(5, Mockito.mock(IWorkflowExecutionRunnable.class));
        Mockito.when(mockRepository.getAll()).thenReturn(mockWorkflows);

        // With a workflow count below the threshold, the system should not be overloaded.
        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, 10, 0.7, 0.7, 0.7, 0.7, true);

        masterServerLoadProtection.setEnabled(true);
        Assertions.assertFalse(masterServerLoadProtection.isOverload(SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .build()));

        // With a workflow count anything >= maxConcurrentWorkflowInstances threshold, the system should be overloaded.
        MasterServerLoadProtection masterServerLoadProtection2 =
                new MasterServerLoadProtection(mockRepository, 5, 0.7, 0.7, 0.7, 0.7, true);
        masterServerLoadProtection2.setEnabled(true);
        Assertions.assertTrue(masterServerLoadProtection2.isOverload(SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .build()));

        MasterServerLoadProtection masterServerLoadProtection3 =
                new MasterServerLoadProtection(mockRepository, 3, 0.7, 0.7, 0.7, 0.7, true);
        masterServerLoadProtection3.setEnabled(true);
        Assertions.assertTrue(masterServerLoadProtection3.isOverload(SystemMetrics.builder()
                .jvmMemoryUsedPercentage(0.5)
                .systemMemoryUsedPercentage(0.5)
                .systemCpuUsagePercentage(0.5)
                .jvmCpuUsagePercentage(0.5)
                .diskUsedPercentage(0.5)
                .build()));
    }

    @Test
    void isNotOverloadWhenAllMetricsAreFine() {
        Collection<IWorkflowExecutionRunnable> mockWorkflows = Collections.nCopies(5,
                Mockito.mock(IWorkflowExecutionRunnable.class));
        Mockito.when(mockRepository.getAll()).thenReturn(mockWorkflows);

        MasterServerLoadProtection masterServerLoadProtection =
                new MasterServerLoadProtection(mockRepository, 10, DEFAULT_THRESHOLD,
                        DEFAULT_THRESHOLD, DEFAULT_THRESHOLD, DEFAULT_THRESHOLD, isEnabled);
        masterServerLoadProtection.setEnabled(true);

        SystemMetrics lowUsageMetrics = SystemMetrics.builder()
                .systemCpuUsagePercentage(LOW_THRESHOLD)
                .jvmCpuUsagePercentage(LOW_THRESHOLD)
                .systemMemoryUsedPercentage(LOW_THRESHOLD)
                .diskUsedPercentage(LOW_THRESHOLD)
                .build();

        Assertions.assertFalse(masterServerLoadProtection.isOverload(lowUsageMetrics));
    }
}
