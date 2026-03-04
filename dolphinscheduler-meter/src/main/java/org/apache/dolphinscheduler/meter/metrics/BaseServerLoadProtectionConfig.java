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

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public abstract class BaseServerLoadProtectionConfig {

    protected boolean enabled = true;

    protected double maxSystemCpuUsagePercentageThresholds = 0.7;

    protected double maxJvmCpuUsagePercentageThresholds = 0.7;

    protected double maxSystemMemoryUsagePercentageThresholds = 0.7;

    /**
     * @deprecated Use {@link #maxDiskUsagePercentageThresholdsRules} instead.
     * This configuration is kept for backward compatibility.
     */
    @Deprecated
    protected double maxDiskUsagePercentageThresholds = 0.7;

    /**
     * List of disk usage threshold rules for monitoring multiple paths.
     * Example configuration:
     * <pre>
     * max-disk-usage-percentage-thresholds-rules:
     *   - disk-path: /data1
     *     usage-percentage-thresholds: 0.9
     *   - disk-path: /data2
     *     usage-percentage-thresholds: 0.8
     * </pre>
     */
    protected List<DiskUsageThresholdRule> maxDiskUsagePercentageThresholdsRules = new ArrayList<>();

}
