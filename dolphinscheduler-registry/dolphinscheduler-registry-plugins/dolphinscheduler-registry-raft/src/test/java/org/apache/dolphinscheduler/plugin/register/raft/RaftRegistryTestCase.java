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
package org.apache.dolphinscheduler.plugin.register.raft;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistry;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.raft.server.RaftRegisterServer;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import lombok.SneakyThrows;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.truth.Truth;

@SpringBootTest(classes = RaftRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = RaftRegistryProperties.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RaftRegistryTestCase {

    @Autowired
    private RaftRegistryProperties raftRegistryProperties;
    private static RaftRegisterServer raftRegisterServer;
    private static RaftRegistry registry;

    @BeforeAll
    public void start() {
        if (raftRegisterServer == null && registry == null) {
            raftRegisterServer = new RaftRegisterServer(raftRegistryProperties);
            registry = new RaftRegistry(raftRegistryProperties);
            raftRegisterServer.start();
            registry.start();
        }
    }

    @Test
    public void testPut() {
        String key = "/nodes/master" + System.nanoTime();
        String value = "127.0.0.1:8080";
        registry.put(key, value, true);
        Truth.assertThat(registry.get(key)).isEqualTo(value);

        // Update the value
        registry.put(key, "123", true);
        Truth.assertThat(registry.get(key)).isEqualTo("123");
    }

    @Test
    public void testGet() {
        String key = "/nodes/master" + System.nanoTime();
        String value = "127.0.0.1:8080";
        assertThrows(RegistryException.class, () -> registry.get(key));
        registry.put(key, value, true);
        Truth.assertThat(registry.get(key)).isEqualTo(value);
    }

    @Test
    public void testIsConnected() throws InterruptedException {
        Truth.assertThat(registry.isConnected()).isTrue();
    }

    @Test
    public void testConnectUntilTimeout() {
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> registry.connectUntilTimeout(Duration.ofSeconds(3)));
    }

    @SneakyThrows
    @Test
    public void testSubscribe() {
        final AtomicBoolean subscribeAdded = new AtomicBoolean(false);
        final AtomicBoolean subscribeRemoved = new AtomicBoolean(false);
        final AtomicBoolean subscribeUpdated = new AtomicBoolean(false);

        SubscribeListener subscribeListener = event -> {
            System.out.println("Receive event: " + event);
            if (event.type() == Event.Type.ADD) {
                subscribeAdded.compareAndSet(false, true);
            }
            if (event.type() == Event.Type.REMOVE) {
                subscribeRemoved.compareAndSet(false, true);
            }
            if (event.type() == Event.Type.UPDATE) {
                subscribeUpdated.compareAndSet(false, true);
            }
        };

        String key = "/nodes/master/";
        registry.subscribe(key, subscribeListener);
        registry.put(key, String.valueOf(System.nanoTime()), true);
        // If multiple event occurs in a refresh time, only the last event will be triggered
        Thread.sleep(3000);
        registry.put(key, String.valueOf(System.nanoTime()), true);
        Thread.sleep(3000);
        registry.delete(key);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Assertions.assertTrue(subscribeAdded.get());
                    Assertions.assertTrue(subscribeUpdated.get());
                    Assertions.assertTrue(subscribeRemoved.get());
                });
    }

    @SneakyThrows
    @Test
    public void testAddConnectionStateListener() {
        AtomicReference<ConnectionState> connectionState = new AtomicReference<>();
        registry.addConnectionStateListener(connectionState::set);
        Truth.assertThat(connectionState.get()).isNull();
        // after initialization, the connection status will change to connected
        // await().atMost(Duration.ofSeconds(2))
        // .until(() -> ConnectionState.CONNECTED == connectionState.get());
    }

    @Test
    public void testDelete() {
        String key = "/nodes/master" + System.nanoTime();
        String value = "127.0.0.1:8080";
        // Delete a non-existent key
        registry.delete(key);

        registry.put(key, value, true);
        Truth.assertThat(registry.get(key)).isEqualTo(value);
        registry.delete(key);
        Truth.assertThat(registry.exists(key)).isFalse();
    }

    @Test
    public void testChildren() {
        String master1 = "/nodes/master/127.0.0.1:8080";
        String master2 = "/nodes/master/127.0.0.2:8080";
        String value = "123";
        registry.put(master1, value, true);
        registry.put(master2, value, true);
        System.out.println(registry.children("/nodes/master"));
        Truth.assertThat(registry.children("/nodes/master"))
                .containsAtLeastElementsIn(Lists.newArrayList("127.0.0.1:8080", "127.0.0.2:8080"));
    }

    @Test
    public void testExists() {
        String key = "/nodes/master" + System.nanoTime();
        String value = "123";
        Truth.assertThat(registry.exists(key)).isFalse();
        registry.put(key, value, true);
        Truth.assertThat(registry.exists(key)).isTrue();
    }

    @SneakyThrows
    @Test
    public void testAcquireLock() {
        String lockKey = "/lock" + System.nanoTime();

        // 1. Acquire the lock at the main thread
        Truth.assertThat(registry.acquireLock(lockKey)).isTrue();
        // Acquire the lock at the main thread again
        // It should acquire success
        Truth.assertThat(registry.acquireLock(lockKey)).isTrue();

        // Acquire the lock at another thread
        // It should acquire failed
        CompletableFuture<Boolean> acquireResult = CompletableFuture.supplyAsync(() -> registry.acquireLock(lockKey));
        assertThrows(TimeoutException.class, () -> acquireResult.get(3000, TimeUnit.MILLISECONDS));
    }

    @SneakyThrows
    @Test
    public void testAcquireLock_withTimeout() {
        String lockKey = "/lock" + System.nanoTime();
        // 1. Acquire the lock in the main thread
        Truth.assertThat(registry.acquireLock(lockKey, 3000)).isTrue();

        // Acquire the lock in the main thread
        // It should acquire success
        Truth.assertThat(registry.acquireLock(lockKey, 3000)).isTrue();

        // Acquire the lock at another thread
        // It should acquire failed
        CompletableFuture<Boolean> acquireResult =
                CompletableFuture.supplyAsync(() -> registry.acquireLock(lockKey, 3000));
        Truth.assertThat(acquireResult.get()).isFalse();
    }

    @SneakyThrows
    @Test
    public void testReleaseLock() {
        String lockKey = "/lock" + System.nanoTime();
        // 1. Acquire the lock in the main thread
        Truth.assertThat(registry.acquireLock(lockKey, 3000)).isTrue();

        // Acquire the lock at another thread
        // It should acquire failed
        CompletableFuture<Boolean> acquireResult =
                CompletableFuture.supplyAsync(() -> registry.acquireLock(lockKey, 3000));
        Truth.assertThat(acquireResult.get()).isFalse();

        // 2. Release the lock in the main thread
        Truth.assertThat(registry.releaseLock(lockKey)).isTrue();

        // Acquire the lock at another thread
        // It should acquire success
        acquireResult = CompletableFuture.supplyAsync(() -> registry.acquireLock(lockKey, 3000));
        Truth.assertThat(acquireResult.get()).isTrue();
    }
}
