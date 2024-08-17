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

package org.apache.dolphinscheduler.alert.service;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.dao.entity.Alert;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import lombok.SneakyThrows;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertEventPendingQueueTest {

    private AlertEventPendingQueue alertEventPendingQueue;

    private static final int QUEUE_SIZE = 10;

    @BeforeEach
    public void before() {
        AlertConfig alertConfig = new AlertConfig();
        alertConfig.setSenderParallelism(QUEUE_SIZE);
        this.alertEventPendingQueue = new AlertEventPendingQueue(alertConfig);
    }

    @SneakyThrows
    @Test
    void put() {
        for (int i = 0; i < alertEventPendingQueue.capacity(); i++) {
            alertEventPendingQueue.put(new Alert());
        }

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            try {
                alertEventPendingQueue.put(new Alert());
                System.out.println(alertEventPendingQueue.size());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertThrowsExactly(ConditionTimeoutException.class,
                () -> await()
                        .timeout(Duration.ofSeconds(2))
                        .until(completableFuture::isDone));

    }

    @Test
    void take() {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            try {
                alertEventPendingQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertThrowsExactly(ConditionTimeoutException.class,
                () -> await()
                        .timeout(Duration.ofSeconds(2))
                        .until(completableFuture::isDone));
    }

    @SneakyThrows
    @Test
    void size() {
        for (int i = 0; i < alertEventPendingQueue.capacity(); i++) {
            alertEventPendingQueue.put(new Alert());
            assertThat(alertEventPendingQueue.size()).isEqualTo(i + 1);
        }
    }
}
