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

package org.apache.dolphinscheduler.server.master.utils;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class MasterThreadFactoryTest {

    @Test
    void getDefaultSchedulerThreadExecutor() {
        ScheduledExecutorService defaultSchedulerThreadExecutor =
                MasterThreadFactory.getDefaultSchedulerThreadExecutor();
        Truth.assertThat(defaultSchedulerThreadExecutor).isNotNull();

        AtomicBoolean taskOneFlag = new AtomicBoolean(false);
        defaultSchedulerThreadExecutor.scheduleWithFixedDelay(() -> {
            taskOneFlag.set(true);
        }, 0,
                1,
                java.util.concurrent.TimeUnit.SECONDS);

        AtomicBoolean taskTwoFlag = new AtomicBoolean(false);
        defaultSchedulerThreadExecutor.scheduleWithFixedDelay(() -> {
            taskTwoFlag.set(true);
        }, 0,
                1,
                java.util.concurrent.TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Truth.assertThat(taskOneFlag.get()).isTrue();
                    Truth.assertThat(taskTwoFlag.get()).isTrue();
                });

    }
}
