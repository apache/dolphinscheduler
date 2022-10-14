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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.etcd.jetcd.test.EtcdClusterExtension;

public class EtcdRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(EtcdRegistryTest.class);

    public static EtcdRegistry registry;

    @BeforeAll
    public static void before() throws Exception {
        EtcdClusterExtension server = EtcdClusterExtension.builder()
                .withNodes(1)
                .withImage("ibmcom/etcd:3.2.24")
                .build();
        EtcdRegistryProperties properties = new EtcdRegistryProperties();
        server.restart();
        properties.setEndpoints(String.valueOf(server.clientEndpoints().get(0)));
        registry = new EtcdRegistry(properties);
        registry.put("/sub", "sub", false);
    }

    @Test
    public void persistTest() {
        registry.put("/nodes/m1", "", false);
        registry.put("/nodes/m2", "", false);
        Assertions.assertEquals(Arrays.asList("m1", "m2"), registry.children("/nodes"));
        Assertions.assertTrue(registry.exists("/nodes/m1"));
        registry.delete("/nodes/m2");
        Assertions.assertFalse(registry.exists("/nodes/m2"));
        registry.delete("/nodes");
        Assertions.assertFalse(registry.exists("/nodes/m1"));
    }

    @Test
    public void lockTest() {
        CountDownLatch preCountDownLatch = new CountDownLatch(1);
        CountDownLatch allCountDownLatch = new CountDownLatch(2);
        List<String> testData = new ArrayList<>();
        new Thread(() -> {
            registry.acquireLock("/lock");
            preCountDownLatch.countDown();
            logger.info(Thread.currentThread().getName()
                    + " :I got the lock, but I don't want to work. I want to rest for a while");
            try {
                Thread.sleep(1000);
                logger.info(Thread.currentThread().getName() + " :I'm going to start working");
                testData.add("thread1");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                logger.info(Thread.currentThread().getName() + " :I have finished my work, now I release the lock");
                registry.releaseLock("/lock");
                allCountDownLatch.countDown();
            }
        }).start();
        try {
            preCountDownLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                logger.info(Thread.currentThread().getName() + " :I am trying to acquire the lock");
                registry.acquireLock("/lock");
                logger.info(Thread.currentThread().getName() + " :I got the lock and I started working");

                testData.add("thread2");
            } finally {
                registry.releaseLock("/lock");
                allCountDownLatch.countDown();
            }

        }).start();
        try {
            allCountDownLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(testData, Arrays.asList("thread1", "thread2"));
    }

    @Test
    public void subscribeTest() {
        boolean status = registry.subscribe("/sub", new TestListener());
        // The following add and delete operations are used for debugging
        registry.put("/sub/m1", "tt", false);
        registry.put("/sub/m2", "tt", false);
        registry.delete("/sub/m2");
        registry.delete("/sub");
        Assertions.assertTrue(status);

    }

    static class TestListener implements SubscribeListener {

        @Override
        public void notify(Event event) {
            logger.info("I'm test listener");
        }
    }

    @AfterAll
    public static void after() throws IOException {
        registry.close();
    }
}
