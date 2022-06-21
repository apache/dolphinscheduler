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

package org.apache.dolphinscheduler.server.worker.metrics;

import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

public final class WorkerServerMetrics {

    public WorkerServerMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Counter WORKER_OVERLOAD_COUNTER =
            Counter.builder("ds.worker.overload.count")
                    .description("overloaded workers count")
                    .register(Metrics.globalRegistry);

    private static final Counter WORKER_SUBMIT_QUEUE_IS_FULL_COUNTER =
            Counter.builder("ds.worker.full.submit.queue.count")
                    .description("full worker submit queues count")
                    .register(Metrics.globalRegistry);

    public static void incWorkerOverloadCount() {
        WORKER_OVERLOAD_COUNTER.increment();
    }

    public static void incWorkerSubmitQueueIsFullCount() {
        WORKER_SUBMIT_QUEUE_IS_FULL_COUNTER.increment();
    }

    public static void registerWorkerRunningTaskGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.task.running", supplier)
                .description("number of running tasks on workers")
                .register(Metrics.globalRegistry);

    }
}
