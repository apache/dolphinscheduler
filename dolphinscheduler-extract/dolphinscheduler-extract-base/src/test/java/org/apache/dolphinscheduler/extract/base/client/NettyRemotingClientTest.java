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

package org.apache.dolphinscheduler.extract.base.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;

class NettyRemotingClientTest {

    @Test
    void slowConnectionDoesNotBlockAnotherHost() throws Exception {
        NettyRemotingClient client = spy(new NettyRemotingClient(NettyClientConfig.builder().workerThreads(1).build()));
        ExecutorService calls = Executors.newFixedThreadPool(2);
        CountDownLatch connecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Host slowHost = Host.of("127.0.0.1:10001");
        Host healthyHost = Host.of("127.0.0.1:10002");
        Channel slowChannel = new EmbeddedChannel();
        Channel healthyChannel = new EmbeddedChannel();
        doAnswer(invocation -> {
            connecting.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return slowChannel;
        }).when(client).createChannel(slowHost);
        doReturn(healthyChannel).when(client).createChannel(healthyHost);
        try {
            Future<Channel> slow = calls.submit(() -> client.getOrCreateChannel(slowHost));
            assertTrue(connecting.await(5, TimeUnit.SECONDS));
            Future<Channel> healthy = calls.submit(() -> client.getOrCreateChannel(healthyHost));
            assertSame(healthyChannel, healthy.get(1, TimeUnit.SECONDS));
            release.countDown();
            assertSame(slowChannel, slow.get(1, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            calls.shutdownNow();
            client.close();
        }
    }

    @Test
    void concurrentCallsToSameHostShareOneConnection() throws Exception {
        NettyRemotingClient client = spy(new NettyRemotingClient(NettyClientConfig.builder().workerThreads(1).build()));
        ExecutorService calls = Executors.newFixedThreadPool(2);
        CountDownLatch connecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Channel channel = new EmbeddedChannel();
        Host host = Host.of("127.0.0.1:10001");
        doAnswer(invocation -> {
            connecting.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return channel;
        }).when(client).createChannel(any());
        try {
            Future<Channel> first = calls.submit(() -> client.getOrCreateChannel(host));
            assertTrue(connecting.await(5, TimeUnit.SECONDS));
            Future<Channel> second = calls.submit(() -> client.getOrCreateChannel(host));
            release.countDown();
            assertSame(channel, first.get(1, TimeUnit.SECONDS));
            assertSame(channel, second.get(1, TimeUnit.SECONDS));
            verify(client, times(1)).createChannel(host);
        } finally {
            release.countDown();
            calls.shutdownNow();
            client.close();
        }
    }
    @Test
    @SuppressWarnings("unchecked")
    void inactiveChannelCleanupDoesNotWaitForReconnect() throws Exception {
        NettyRemotingClient client = spy(new NettyRemotingClient(NettyClientConfig.builder().workerThreads(1).build()));
        ExecutorService calls = Executors.newFixedThreadPool(2);
        CountDownLatch connecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Host host = Host.of("127.0.0.1:10001");
        Channel previous = new EmbeddedChannel();
        previous.close();
        Channel replacement = new EmbeddedChannel();
        Field field = NettyRemotingClient.class.getDeclaredField("channels");
        field.setAccessible(true);
        ((Map<Host, Channel>) field.get(client)).put(host, previous);
        doAnswer(invocation -> {
            connecting.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return replacement;
        }).when(client).createChannel(host);
        try {
            Future<Channel> reconnect = calls.submit(() -> client.getOrCreateChannel(host));
            assertTrue(connecting.await(5, TimeUnit.SECONDS));
            // A Netty event loop must be able to finish the old channel's inactive callback while connecting.
            Future<?> cleanup = calls.submit(() -> client.onChannelInactive(host, previous));
            cleanup.get(1, TimeUnit.SECONDS);
            release.countDown();
            assertSame(replacement, reconnect.get(1, TimeUnit.SECONDS));
            assertSame(replacement, client.getOrCreateChannel(host));
            verify(client, times(1)).createChannel(host);
        } finally {
            release.countDown();
            calls.shutdownNow();
            client.close();
        }
    }

}
