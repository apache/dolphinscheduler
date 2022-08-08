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

package org.apache.dolphinscheduler.alert;

import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.experimental.UtilityClass;

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

    public void incAlertSuccessCount() {
        alertSuccessCounter.increment();
    }

    public void incAlertFailCount() {
        alertFailCounter.increment();
    }

}
