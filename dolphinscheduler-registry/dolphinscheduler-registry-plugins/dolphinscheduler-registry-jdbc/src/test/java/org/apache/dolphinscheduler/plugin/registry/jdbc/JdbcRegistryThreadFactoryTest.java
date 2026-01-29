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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class JdbcRegistryThreadFactoryTest {

    @Test
    void getDefaultSchedulerThreadExecutor() {
        ScheduledExecutorService schedulerThreadExecutor =
                JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            schedulerThreadExecutor.scheduleWithFixedDelay(atomicInteger::incrementAndGet, 0, 1, TimeUnit.SECONDS);
        }
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(atomicInteger.get()).isEqualTo(100));
    }
}
