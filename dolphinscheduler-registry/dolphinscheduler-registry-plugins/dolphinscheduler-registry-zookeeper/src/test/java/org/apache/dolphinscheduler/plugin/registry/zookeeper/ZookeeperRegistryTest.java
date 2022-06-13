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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.curator.test.TestingServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryTest.class);

    TestingServer server;

    ZookeeperRegistry registry;

    @Before
    public void before() throws Exception {
        server = new TestingServer(true);

        ZookeeperRegistryProperties p = new ZookeeperRegistryProperties();
        p.getZookeeper().setConnectString(server.getConnectString());
        registry = new ZookeeperRegistry(p);
        registry.start();
        registry.put("/sub", "", false);
    }

    @Test
    public void persistTest() {
        registry.put("/nodes/m1", "", false);
        registry.put("/nodes/m2", "", false);
        Assert.assertEquals(Arrays.asList("m2", "m1"), registry.children("/nodes"));
        Assert.assertTrue(registry.exists("/nodes/m1"));
        registry.delete("/nodes/m2");
        Assert.assertFalse(registry.exists("/nodes/m2"));
    }

    @Test
    public void lockTest() throws InterruptedException {
        CountDownLatch preCountDownLatch = new CountDownLatch(1);
        CountDownLatch allCountDownLatch = new CountDownLatch(2);
        List<String> testData = new ArrayList<>();
        new Thread(() -> {
            registry.acquireLock("/lock");
            preCountDownLatch.countDown();
            logger.info(Thread.currentThread().getName() + " :I got the lock, but I don't want to work. I want to rest for a while");
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
        preCountDownLatch.await();
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
        allCountDownLatch.await();
        Assert.assertEquals(testData, Arrays.asList("thread1", "thread2"));

    }

    @Test
    public void subscribeTest() {
        boolean status = registry.subscribe("/sub", new TestListener());
        Assert.assertTrue(status);

    }

    static class TestListener implements SubscribeListener {
        @Override
        public void notify(Event event) {
            logger.info("I'm test listener");
        }
    }

    @After
    public void after() throws IOException {
        registry.close();
        server.close();
    }

}
