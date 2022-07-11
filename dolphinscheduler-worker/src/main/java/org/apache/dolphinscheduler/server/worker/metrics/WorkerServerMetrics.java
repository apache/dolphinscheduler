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

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WorkerServerMetrics {

    private static final Counter WORKER_OVERLOAD_COUNTER =
        Counter.builder("ds.worker.overload.count")
            .description("overloaded workers count")
            .register(Metrics.globalRegistry);

    private static final Counter WORKER_SUBMIT_QUEUE_IS_FULL_COUNTER =
        Counter.builder("ds.worker.full.submit.queue.count")
            .description("full worker submit queues count")
            .register(Metrics.globalRegistry);

    private static final Counter WORKER_RESOURCE_DOWNLOAD_COUNTER =
            Counter.builder("ds.worker.resource.download.count")
                    .description("worker resource download count")
                    .register(Metrics.globalRegistry);

//    private static final Counter WORKER_RESOURCE_DOWNLOAD_SUCCESS_COUNTER =
//            Counter.builder("ds.worker.resource.download.count")
//                    .tag("status", "success")
//                    .description("worker resource download success count")
//                    .register(Metrics.globalRegistry);
//
//    private static final Counter WORKER_RESOURCE_DOWNLOAD_FAILURE_COUNTER =
//            Counter.builder("ds.worker.resource.download.count")
//                    .tag("status", "fail")
//                    .description("worker resource download failure count")
//                    .register(Metrics.globalRegistry);


    private static final Timer WORKER_RESOURCE_DOWNLOAD_DURATION_TIMER =
            Timer.builder("ds.worker.resource.download.duration")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("time cost of resource download on workers")
                    .register(Metrics.globalRegistry);

    private static final DistributionSummary WORKER_RESOURCE_DOWNLOAD_SIZE_DISTRIBUTION =
            DistributionSummary.builder("ds.worker.resource.download.size")
            .baseUnit("KB")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .description("size of downloaded resource files on worker")
            .register(Metrics.globalRegistry);

    public static void incWorkerOverloadCount() {
        WORKER_OVERLOAD_COUNTER.increment();
    }

    public static void incWorkerSubmitQueueIsFullCount() {
        WORKER_SUBMIT_QUEUE_IS_FULL_COUNTER.increment();
    }

    public static void incWorkerResourceDownloadCountByStatus(final String status) {
        Metrics.globalRegistry.counter("ds.worker.resource.download.count", "status", status).increment();
    }

//    public static void incWorkerResourceDownloadSuccessCount() {
//        WORKER_RESOURCE_DOWNLOAD_SUCCESS_COUNTER.increment();
//    }

//    public static void incWorkerResourceDownloadFailureCount() { WORKER_RESOURCE_DOWNLOAD_FAILURE_COUNTER.increment(); }

    public static void recordWorkerResourceDownloadTime(final long milliseconds) {
        WORKER_RESOURCE_DOWNLOAD_DURATION_TIMER.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static void recordWorkerResourceDownloadSize(final double size) {
        WORKER_RESOURCE_DOWNLOAD_SIZE_DISTRIBUTION.record(size);
    }

    public static void registerWorkerRunningTaskGauge(final Supplier<Number> supplier) {
        Gauge.builder("ds.task.running", supplier)
            .description("number of running tasks on workers")
            .register(Metrics.globalRegistry);
    }

}
