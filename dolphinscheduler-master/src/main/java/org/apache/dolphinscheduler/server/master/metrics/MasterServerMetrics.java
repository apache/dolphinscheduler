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

package org.apache.dolphinscheduler.server.master.metrics;

import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

@UtilityClass
public class MasterServerMetrics {

    /**
     * Used to measure the master server is overload.
     */
    private final Counter masterOverloadCounter =
            Counter.builder("ds.master.overload.count")
                    .description("Master server overload count")
                    .register(Metrics.globalRegistry);

    /**
     * Used to measure the number of process command consumed by master.
     */
    private final Counter masterConsumeCommandCounter =
            Counter.builder("ds.master.consume.command.count")
                    .description("Master server consume command count")
                    .register(Metrics.globalRegistry);

    public void registerMasterMemoryAvailableGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.master.memory.available", supplier)
                .description("Master memory available")
                .register(Metrics.globalRegistry);
    }

    public void registerMasterCpuUsageGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.master.cpu.usage", supplier)
                .description("master cpu usage")
                .register(Metrics.globalRegistry);
    }

    public void registerMasterMemoryUsageGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.master.memory.usage", supplier)
                .description("Master memory usage")
                .register(Metrics.globalRegistry);
    }

    public static void registerUncachedException(final Supplier<Number> supplier) {
        Gauge.builder("ds.master.uncached.exception", supplier)
                .description("number of uncached exception")
                .register(Metrics.globalRegistry);
    }

    public void incMasterOverload() {
        masterOverloadCounter.increment();
    }

    public void incMasterConsumeCommand(int commandCount) {
        masterConsumeCommandCounter.increment(commandCount);
    }

}
