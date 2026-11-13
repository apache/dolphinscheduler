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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class DefaultMetricsProviderTest {

    @Test
    void concurrentCacheMissesCollectAndPublishOnlyOneSnapshot() throws Exception {
        CountDownLatch collecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger collections = new AtomicInteger();
        SimpleMeterRegistry registry = registry();
        try {
            Gauge.builder("system.cpu.usage", () -> {
                collections.incrementAndGet();
                collecting.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return 0.25;
            }).register(registry);
            DefaultMetricsProvider provider = new DefaultMetricsProvider(registry);
            AtomicReference<SystemMetrics> first = new AtomicReference<>();
            AtomicReference<SystemMetrics> second = new AtomicReference<>();
            Thread firstRequest = new Thread(() -> first.set(provider.getSystemMetrics()));
            Thread secondRequest = new Thread(() -> second.set(provider.getSystemMetrics()));
            try {
                firstRequest.start();
                assertTrue(collecting.await(5, TimeUnit.SECONDS));
                secondRequest.start();
                Awaitility.await().atMost(Duration.ofSeconds(5))
                        .until(() -> secondRequest.getState() == Thread.State.BLOCKED || collections.get() > 1);
                assertEquals(1, collections.get());
            } finally {
                release.countDown();
                firstRequest.join(5000);
                secondRequest.join(5000);
            }
            assertNotNull(first.get());
            assertSame(first.get(), second.get());
            assertEquals(0.25, second.get().getSystemCpuUsagePercentage());
        } finally {
            registry.close();
        }
    }

    @Test
    void reusesCachedMetricsBetweenRequests() {
        AtomicInteger collections = new AtomicInteger();
        SimpleMeterRegistry registry = registry();
        try {
            Gauge.builder("system.cpu.usage", () -> {
                collections.incrementAndGet();
                return 0.25;
            }).register(registry);
            DefaultMetricsProvider provider = new DefaultMetricsProvider(registry);
            SystemMetrics first = provider.getSystemMetrics();
            assertSame(first, provider.getSystemMetrics());
            assertEquals(1, collections.get());
        } finally {
            registry.close();
        }
    }

    private SimpleMeterRegistry registry() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Gauge.builder("process.cpu.usage", () -> 0.1).register(registry);
        Gauge.builder("disk.total", () -> 100.0).register(registry);
        Gauge.builder("disk.free", () -> 50.0).register(registry);
        return registry;
    }
}
