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

package org.apache.dolphinscheduler.api.metrics;

import java.util.concurrent.TimeUnit;

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

@UtilityClass
public class ApiServerMetrics {

    private final Counter apiRequestCounter =
            Counter.builder("ds.api.request.count")
                    .description("Api request count")
                    .register(Metrics.globalRegistry);

    private final Counter apiResponse2xxCounter =
            Counter.builder("ds.api.response.count")
                    .tag("code", "2xx")
                    .description("Api 2xx response count")
                    .register(Metrics.globalRegistry);

    private final Counter apiResponse3xxCounter =
            Counter.builder("ds.api.response.count")
                    .tag("code", "3xx")
                    .description("Api 3xx response count")
                    .register(Metrics.globalRegistry);

    private final Counter apiResponse4xxCounter =
            Counter.builder("ds.api.response.count")
                    .tag("code", "4xx")
                    .description("Api 4xx response count")
                    .register(Metrics.globalRegistry);

    private final Counter apiResponse5xxCounter =
            Counter.builder("ds.api.response.count")
                    .tag("code", "5xx")
                    .description("Api 5xx response count")
                    .register(Metrics.globalRegistry);

    private final DistributionSummary apiResourceUploadSizeDistribution =
            DistributionSummary.builder("ds.api.resource.upload.size")
                    .baseUnit("bytes")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("size of upload resource files on api")
                    .register(Metrics.globalRegistry);

    private final DistributionSummary apiResourceDownloadSizeDistribution =
            DistributionSummary.builder("ds.api.resource.download.size")
                    .baseUnit("bytes")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("size of download resource files on api")
                    .register(Metrics.globalRegistry);

    static {
        Timer.builder("ds.api.response.time")
                .tag("user.id", "dummy")
                .description("response time on api")
                .register(Metrics.globalRegistry);
    }

    public void incApiRequestCount() {
        apiRequestCounter.increment();
    }

    public void incApiResponse2xxCount() {
        apiResponse2xxCounter.increment();
    }

    public void incApiResponse3xxCount() {
        apiResponse3xxCounter.increment();
    }

    public void incApiResponse4xxCount() {
        apiResponse4xxCounter.increment();
    }

    public void incApiResponse5xxCount() {
        apiResponse5xxCounter.increment();
    }

    public void recordApiResourceUploadSize(final long size) {
        apiResourceUploadSizeDistribution.record(size);
    }

    public void recordApiResourceDownloadSize(final long size) {
        apiResourceDownloadSizeDistribution.record(size);
    }

    public void recordApiResponseTime(final long milliseconds, final int userId) {
        Metrics.globalRegistry.timer(
                "ds.api.response.time",
                "user.id", String.valueOf(userId)).record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void cleanUpApiResponseTimeMetricsByUserId(final int userId) {
        Metrics.globalRegistry.remove(
                Metrics.globalRegistry.timer(
                        "ds.api.response.time",
                        "user.id", String.valueOf(userId)));
    }
}
