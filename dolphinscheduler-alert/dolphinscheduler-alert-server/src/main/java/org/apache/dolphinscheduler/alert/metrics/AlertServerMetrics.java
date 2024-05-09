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

package org.apache.dolphinscheduler.alert.metrics;

import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

@UtilityClass
public class AlertServerMetrics {

    private final Counter alertSuccessCounter =
            Counter.builder("ds.alert.send.count")
                    .tag("status", "success")
                    .description("Alert success count")
                    .register(Metrics.globalRegistry);

    private final Counter alertFailCounter =
            Counter.builder("ds.alert.send.count")
                    .tag("status", "fail")
                    .description("Alert failure count")
                    .register(Metrics.globalRegistry);

    public void registerPendingAlertGauge(final Supplier<Number> supplier) {
        Gauge.builder("ds.alert.pending", supplier)
                .description("Number of pending alert")
                .register(Metrics.globalRegistry);
    }

    public void registerSendingAlertGauge(final Supplier<Number> supplier) {
        Gauge.builder("ds.alert.sending", supplier)
                .description("Number of sending alert")
                .register(Metrics.globalRegistry);
    }

    public static void registerUncachedException(final Supplier<Number> supplier) {
        Gauge.builder("ds.alert.uncached.exception", supplier)
                .description("number of uncached exception")
                .register(Metrics.globalRegistry);
    }

    public void incAlertSuccessCount() {
        alertSuccessCounter.increment();
    }

    public void incAlertFailCount() {
        alertFailCounter.increment();
    }

}
