package org.apache.dolphinscheduler.plugin.registry.etcd;


import io.etcd.jetcd.launcher.EtcdCluster;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class EtcdRegistryTest {
    private static final Logger logger = LoggerFactory.getLogger(EtcdRegistryTest.class);

    @RegisterExtension
    static final EtcdCluster server = new EtcdClusterExtension("test-etcd", 1);

    EtcdRegistry registry;

    @Before
    public void before() {
        EtcdRegistryProperties properties = new EtcdRegistryProperties();
        server.start();

        properties.setEndpoints(String.valueOf(server.getClientEndpoints()));
        registry = new EtcdRegistry(properties);

        registry.put("/sub", "", false);
    }

    @Test
    public void persistTest() {
        registry.put("/nodes/m1", "", false);
        registry.put("/nodes/m2", "", false);
        Assert.assertEquals(Arrays.asList("m1", "m2"), registry.children("/nodes"));
        Assert.assertTrue(registry.exists("/nodes/m1"));
        registry.delete("/nodes/m2");
        Assert.assertFalse(registry.exists("/nodes/m2"));
        registry.delete("/nodes");
        Assert.assertFalse(registry.exists("/nodes/m1"));
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
    public void subscribeTest() throws InterruptedException {
        boolean status = registry.subscribe("/sub", new TestListener());
        // The following add and delete operations are used for debugging
        registry.put("/sub/m1", "tt", false);
        registry.put("/sub/m2", "tt", false);
        registry.delete("/sub/m2");
        registry.delete("/sub");
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
