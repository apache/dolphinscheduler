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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MasterServerLoadProtectionTest {

    @Test
    void isOverload() {
        MasterServerLoadProtection masterServerLoadProtection = new MasterServerLoadProtection();
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
}
