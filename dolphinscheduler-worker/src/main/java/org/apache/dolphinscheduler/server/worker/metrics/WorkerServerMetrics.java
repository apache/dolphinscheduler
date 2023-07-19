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

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

@UtilityClass
public class WorkerServerMetrics {

    private final Counter workerOverloadCounter =
            Counter.builder("ds.worker.overload.count")
                    .description("overloaded workers count")
                    .register(Metrics.globalRegistry);

    private final Counter workerFullSubmitQueueCounter =
            Counter.builder("ds.worker.full.submit.queue.count")
                    .description("full worker submit queues count")
                    .register(Metrics.globalRegistry);

    private final Counter workerResourceDownloadSuccessCounter =
            Counter.builder("ds.worker.resource.download.count")
                    .tag("status", "success")
                    .description("worker resource download success count")
                    .register(Metrics.globalRegistry);

    private final Counter workerResourceDownloadFailCounter =
            Counter.builder("ds.worker.resource.download.count")
                    .tag("status", "fail")
                    .description("worker resource download failure count")
                    .register(Metrics.globalRegistry);

    private final Timer workerResourceDownloadDurationTimer =
            Timer.builder("ds.worker.resource.download.duration")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("time cost of resource download on workers")
                    .register(Metrics.globalRegistry);

    private final DistributionSummary workerResourceDownloadSizeDistribution =
            DistributionSummary.builder("ds.worker.resource.download.size")
                    .baseUnit("bytes")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("size of downloaded resource files on worker")
                    .register(Metrics.globalRegistry);

    public void incWorkerOverloadCount() {
        workerOverloadCounter.increment();
    }

    public void incWorkerSubmitQueueIsFullCount() {
        workerFullSubmitQueueCounter.increment();
    }

    public void incWorkerResourceDownloadSuccessCount() {
        workerResourceDownloadSuccessCounter.increment();
    }

    public void incWorkerResourceDownloadFailureCount() {
        workerResourceDownloadFailCounter.increment();
    }

    public void recordWorkerResourceDownloadTime(final long milliseconds) {
        workerResourceDownloadDurationTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordWorkerResourceDownloadSize(final long size) {
        workerResourceDownloadSizeDistribution.record(size);
    }

    public void registerWorkerTaskTotalGauge(final Supplier<Number> supplier) {
        Gauge.builder("ds.worker.task", supplier)
                .description("total number of tasks on worker")
                .register(Metrics.globalRegistry);
    }

    public void registerWorkerExecuteQueueSizeGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.worker.execute.queue.size", supplier)
                .description("worker execute queue size")
                .register(Metrics.globalRegistry);
    }

    public void registerWorkerActiveExecuteThreadGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.worker.active.execute.thread", supplier)
                .description("number of active task execute threads on worker")
                .register(Metrics.globalRegistry);
    }

    public void registerWorkerMemoryAvailableGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.worker.memory.available", supplier)
                .description("worker memory available")
                .register(Metrics.globalRegistry);
    }

    public void registerWorkerCpuUsageGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.worker.cpu.usage", supplier)
                .description("worker cpu usage")
                .register(Metrics.globalRegistry);
    }

    public void registerWorkerMemoryUsageGauge(Supplier<Number> supplier) {
        Gauge.builder("ds.worker.memory.usage", supplier)
                .description("worker memory usage")
                .register(Metrics.globalRegistry);
    }

}
