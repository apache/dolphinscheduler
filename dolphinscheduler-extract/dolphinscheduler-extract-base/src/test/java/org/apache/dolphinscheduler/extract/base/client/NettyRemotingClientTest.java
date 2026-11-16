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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.extract.base.authentication.RpcAuthenticationConfig;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;

class NettyRemotingClientTest {

    @Test
    void distinctUnavailableEndpointsCanConnectConcurrently() throws Exception {
        ExecutorService callers = Executors.newFixedThreadPool(3);
        Queue<Runnable> deferred = new ConcurrentLinkedQueue<>();
        AtomicBoolean released = new AtomicBoolean();
        CountDownLatch connecting = new CountDownLatch(3);
        try (
                NettyRemotingClient client = newClient();
                ServerSocket first = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                ServerSocket second = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                ServerSocket third = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            List<Host> hosts = Arrays.asList(new Host("127.0.0.1", first.getLocalPort()),
                    new Host("127.0.0.1", second.getLocalPort()), new Host("127.0.0.1", third.getLocalPort()));
            Bootstrap bootstrap = field(client, "bootstrap");
            ChannelHandler initializer = bootstrap.config().handler();
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel channel) {
                    channel.pipeline().addLast(initializer);
                    channel.pipeline().addLast(new ChannelOutboundHandlerAdapter() {

                        @Override
                        public void connect(ChannelHandlerContext ctx, java.net.SocketAddress remote,
                                            java.net.SocketAddress local, ChannelPromise promise) {
                            // Delay real connects without blocking the event loop or mocking the RPC client.
                            deferred.add(() -> ctx.executor().execute(() -> ctx.connect(remote, local, promise)));
                            connecting.countDown();
                            if (released.get()) {
                                resumeConnections(deferred);
                            }
                        }
                    });
                }
            });
            try {
                List<Future<Channel>> requests = new ArrayList<>();
                for (Host host : hosts) {
                    requests.add(callers.submit(() -> client.getOrCreateChannel(host)));
                }
                assertTrue(connecting.await(5, TimeUnit.SECONDS), "Different hosts must reach connect independently");
                first.close();
                second.close();
                third.close();
                released.set(true);
                resumeConnections(deferred);
                for (Future<Channel> request : requests) {
                    ExecutionException failure = assertThrows(ExecutionException.class,
                            () -> request.get(5, TimeUnit.SECONDS));
                    assertTrue(failure.getCause() instanceof ConnectException, failure.toString());
                }
            } finally {
                released.set(true);
                resumeConnections(deferred);
            }
        } finally {
            callers.shutdownNow();
        }
    }

    @Test
    void concurrentCallsToSameHostReuseOneRealConnection() throws Exception {
        ExecutorService callers = Executors.newFixedThreadPool(4);
        CountDownLatch ready = new CountDownLatch(4);
        CountDownLatch start = new CountDownLatch(1);
        NettyRemotingClient client = spy(newClient());
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            Host host = new Host("127.0.0.1", server.getLocalPort());
            List<Future<Channel>> requests = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                requests.add(callers.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return client.getOrCreateChannel(host);
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            Channel channel = requests.get(0).get(5, TimeUnit.SECONDS);
            for (Future<Channel> request : requests) {
                assertSame(channel, request.get(5, TimeUnit.SECONDS));
            }
            try (Socket accepted = server.accept()) {
                assertTrue(channel.isActive());
                verify(client, times(1)).createChannel(host);
            }
        } finally {
            start.countDown();
            client.close();
            callers.shutdownNow();
        }
    }

    @Test
    void inactiveChannelCleanupDoesNotWaitForReconnectOrRemoveReplacement() throws Exception {
        NettyRemotingClient client = spy(newClient());
        ExecutorService callers = Executors.newFixedThreadPool(2);
        CountDownLatch connecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Host host = Host.of("127.0.0.1:10001");
        Channel previous = new EmbeddedChannel();
        previous.close();
        Channel replacement = new EmbeddedChannel();
        Map<Host, Channel> channels = field(client, "channels");
        channels.put(host, previous);
        doAnswer(invocation -> {
            connecting.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return replacement;
        }).when(client).createChannel(host);
        try {
            Future<Channel> reconnect = callers.submit(() -> client.getOrCreateChannel(host));
            assertTrue(connecting.await(5, TimeUnit.SECONDS));
            callers.submit(() -> client.onChannelInactive(host, previous)).get(1, TimeUnit.SECONDS);
            release.countDown();
            assertSame(replacement, reconnect.get(5, TimeUnit.SECONDS));
            client.onChannelInactive(host, previous);
            assertSame(replacement, client.getOrCreateChannel(host));
        } finally {
            release.countDown();
            client.close();
            callers.shutdownNow();
        }
    }

    @Test
    void closeDoesNotWaitForConnectAndClosesTheLateChannel() throws Exception {
        NettyRemotingClient client = spy(newClient());
        ExecutorService caller = Executors.newSingleThreadExecutor();
        CountDownLatch connecting = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Host host = Host.of("127.0.0.1:10001");
        Channel channel = new EmbeddedChannel();
        doAnswer(invocation -> {
            connecting.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return channel;
        }).when(client).createChannel(host);
        try {
            Future<Channel> connect = caller.submit(() -> client.getOrCreateChannel(host));
            assertTrue(connecting.await(5, TimeUnit.SECONDS));
            client.close();
            release.countDown();
            ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> connect.get(5, TimeUnit.SECONDS));
            assertTrue(failure.getCause() instanceof IllegalStateException);
            assertFalse(channel.isActive());
            assertTrue(((Map<?, ?>) field(client, "channels")).isEmpty());
        } finally {
            release.countDown();
            client.close();
            caller.shutdownNow();
        }
    }

    private NettyRemotingClient newClient() {
        return new NettyRemotingClient(NettyClientConfig.builder().workerThreads(1).connectTimeoutMillis(300)
                .authentication(RpcAuthenticationConfig.disabled()).build());
    }

    private static void resumeConnections(Queue<Runnable> deferred) {
        Runnable connect;
        while ((connect = deferred.poll()) != null) {
            connect.run();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T field(NettyRemotingClient client, String name) throws Exception {
        Field field = NettyRemotingClient.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(client);
    }
}
