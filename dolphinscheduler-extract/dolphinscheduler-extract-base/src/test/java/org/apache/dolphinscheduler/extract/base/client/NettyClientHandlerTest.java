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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.extract.base.IRpcResponse;
import org.apache.dolphinscheduler.extract.base.RpcMethodRetryStrategy;
import org.apache.dolphinscheduler.extract.base.SyncRequestDto;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.exception.RemoteTimeoutException;
import org.apache.dolphinscheduler.extract.base.future.ResponseFuture;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterDecoder;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterHeader;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.TooLongFrameException;

class NettyClientHandlerTest {

    private EventLoopGroup serverBossGroup;

    private EventLoopGroup serverWorkerGroup;

    private NettyRemotingClient nettyRemotingClient;

    @AfterEach
    void tearDown() {
        if (nettyRemotingClient != null) {
            nettyRemotingClient.close();
        }
        if (serverBossGroup != null) {
            serverBossGroup.shutdownGracefully();
        }
        if (serverWorkerGroup != null) {
            serverWorkerGroup.shutdownGracefully();
        }
    }

    /**
     * When a decoder exception (e.g. TooLongFrameException from the maxFrameSize guard) fires on a
     * channel that carries MULTIPLE in-flight requests, the handler must complete ALL pending
     * ResponseFutures with the cause — a channel is shared by concurrent requests, so tracking a
     * single opaque would leave the others waiting until RPC timeout.
     */
    @Test
    void exceptionCaught_completesAllPendingFuturesWithCause() throws Exception {
        long opaqueA = 888001L;
        long opaqueB = 888002L;

        TooLongFrameException cause =
                new TooLongFrameException("Body length 123456789 exceeds max frame size 67108864");

        Channel channel = mock(Channel.class);
        ResponseFuture futureA = new ResponseFuture(opaqueA, 30_000, channel);
        ResponseFuture futureB = new ResponseFuture(opaqueB, 30_000, channel);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        NettyRemotingClient mockClient = mock(NettyRemotingClient.class);
        NettyClientHandler handler = new NettyClientHandler(mockClient);

        handler.exceptionCaught(ctx, cause);

        // BOTH futures must be completed immediately — no 30 s timeout wait.
        IRpcResponse responseA = futureA.waitResponse();
        IRpcResponse responseB = futureB.waitResponse();
        assertNull(responseA, "Response A should be null (error, not a normal RPC response)");
        assertNull(responseB, "Response B should be null (error, not a normal RPC response)");
        assertNotNull(futureA.getCause(), "Cause must be set on future A");
        assertNotNull(futureB.getCause(), "Cause must be set on future B");
        assertSame(cause, futureA.getCause(), "Cause must be the original exception (A)");
        assertSame(cause, futureB.getCause(), "Cause must be the original exception (B)");
    }

    /**
     * Channel closure (peer restart, network blip) must fail every in-flight request sent on that
     * channel — no response can ever arrive on a closed channel, so waiting for the RPC timeout
     * would only add latency to an already-decided failure.
     */
    @Test
    void channelInactive_completesAllPendingFuturesWithCause() throws Exception {
        final Channel channel = mock(Channel.class);
        final ResponseFuture futureA = new ResponseFuture(888003L, 30_000, channel);
        final ResponseFuture futureB = new ResponseFuture(888004L, 30_000, channel);

        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        final NettyRemotingClient mockClient = mock(NettyRemotingClient.class);
        final NettyClientHandler handler = new NettyClientHandler(mockClient);

        handler.channelInactive(ctx);

        final long start = System.nanoTime();
        assertNull(futureA.waitResponse(), "Drained future must not carry a response");
        assertNull(futureB.waitResponse(), "Drained future must not carry a response");
        assertTrue(System.nanoTime() - start < 5_000_000_000L,
                "Drained futures must wake immediately, not wait out their timeout");
        assertNotNull(futureA.getCause(), "Channel closure must set a cause on future A");
        assertNotNull(futureB.getCause(), "Channel closure must set a cause on future B");
        assertNull(ResponseFuture.getFuture(888003L), "Drained future must leave FUTURE_TABLE");
        assertNull(ResponseFuture.getFuture(888004L), "Drained future must leave FUTURE_TABLE");
    }

    /**
     * When there is no in-flight request on the channel, the handler must not throw — just log,
     * close the channel, and move on.
     */
    @Test
    void exceptionCaught_noPendingFuture_doesNotThrow() {
        final Channel channel = mock(Channel.class);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        final NettyRemotingClient mockClient = mock(NettyRemotingClient.class);
        final NettyClientHandler handler = new NettyClientHandler(mockClient);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> handler.exceptionCaught(ctx, new RuntimeException("unrelated error")));
    }

    /**
     * A response whose body cannot be deserialized must fail the pending request with the real
     * error — NOT leave it hanging until RPC timeout. The request stays in FUTURE_TABLE until
     * its response has been SUCCESSFULLY handed to the future, so the deserialization error
     * flowing into exceptionCaught still finds it there and drains it. Otherwise the failing
     * request alone waits out its timeout with a misleading RemoteTimeoutException while every
     * OTHER pending request receives the actual error.
     */
    @Test
    void channelRead_deserializeFailure_failsPendingFutureNotTimeout() throws Exception {
        final long opaque = 888101L;

        final NettyRemotingClient mockClient = mock(NettyRemotingClient.class);
        final NettyClientHandler handler = new NettyClientHandler(mockClient);
        final EmbeddedChannel channel = new EmbeddedChannel(handler);
        final ResponseFuture future = new ResponseFuture(opaque, 2_000, channel);

        // Body is not valid JSON → JsonSerializer.deserialize throws inside processReceived,
        // the error flows through the real pipeline into exceptionCaught.
        final Transporter transporter = Transporter.of(
                new TransporterHeader(opaque, "test-method"),
                "not-json".getBytes(StandardCharsets.UTF_8));

        try {
            channel.writeInbound(transporter);
            channel.checkException();
        } catch (Throwable expected) {
            // The deserialization error surfacing here is fine — what matters is the future.
        }

        assertNull(future.waitResponse(), "Future must be failed, not completed with a response");
        assertNotNull(future.getCause(),
                "The failing request must receive the real error promptly — not hang until timeout");
    }

    /**
     * Regression test for the shared-channel race: two CONCURRENT sync requests multiplexed over
     * one channel, then the server replies with a frame whose declared body length exceeds
     * maxFrameSize. The decoder throws TooLongFrameException and BOTH callers must receive that
     * real error promptly — not one caller succeeding while the other hangs until timeout.
     */
    @Test
    void concurrentRequestsSharingChannel_decoderExceptionFailsAll() throws Exception {
        // Server that decodes incoming Transporters; once it has seen both requests it writes a
        // malformed frame (declared body length > 64 MB) so the CLIENT decoder throws.
        serverBossGroup = new NioEventLoopGroup(1);
        serverWorkerGroup = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(serverBossGroup, serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new TransporterDecoder())
                                .addLast(new SimpleChannelInboundHandler<Transporter>() {

                                    private volatile int requestCount = 0;

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, Transporter msg) {
                                        requestCount++;
                                        if (requestCount >= 2) {
                                            // Both requests are on the wire — poison the channel.
                                            ctx.writeAndFlush(Unpooled.wrappedBuffer(malformedFrame()));
                                        }
                                    }
                                });
                    }
                });
        int serverPort = ((InetSocketAddress) serverBootstrap.bind(0).sync().channel().localAddress()).getPort();

        nettyRemotingClient = new NettyRemotingClient(NettyClientConfig.builder().build());
        Host host = new Host("localhost", serverPort);
        // Pre-warm the channel so both concurrent requests below multiplex over the SAME channel.
        nettyRemotingClient.getOrCreateChannel(host);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Throwable>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                results.add(pool.submit(() -> {
                    try {
                        nettyRemotingClient.sendSync(newSyncRequest(host, 5_000));
                        return null;
                    } catch (Throwable e) {
                        return e;
                    }
                }));
            }

            for (Future<Throwable> result : results) {
                Throwable thrown = result.get(30, TimeUnit.SECONDS);
                assertNotNull(thrown, "Both requests must fail — the server never sent a valid response");
                assertTrue(thrown instanceof RemoteException,
                        "Expected RemoteException but got: " + thrown);
                assertTrue(findCause(thrown, TooLongFrameException.class),
                        "The error must carry the real decoder exception (TooLongFrameException), "
                                + "not a misleading timeout. Got: " + thrown);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * A request that times out while the channel stays HEALTHY (server accepted it but never
     * responds; heartbeats keep the connection alive) leaks nothing: the global FUTURE_TABLE
     * keeps no trace of the abandoned request. There is no response, no failure notification
     * and no channel death that would otherwise clean up — the timeout path itself must drop
     * its own state.
     */
    @Test
    void sendSync_timeout_leaksNothingInFutureTable() throws Exception {
        // Server that accepts connections and never responds — the channel stays healthy.
        final int serverPort = startSilentServer();

        nettyRemotingClient = new NettyRemotingClient(NettyClientConfig.builder().build());
        final Host host = new Host("localhost", serverPort);
        nettyRemotingClient.getOrCreateChannel(host);

        final SyncRequestDto dto = newSyncRequest(host, 500);
        final long opaque = dto.getTransporter().getHeader().getOpaque();

        assertThrows(RemoteTimeoutException.class, () -> nettyRemotingClient.sendSync(dto));

        assertNull(ResponseFuture.getFuture(opaque),
                "Timed-out request's future must be removed from FUTURE_TABLE");
    }

    /**
     * A caller thread INTERRUPTED while waiting for its response must not leak its FUTURE_TABLE
     * entry. Interruption (shutdownNow / task cancellation) bypasses the timeout branch, so the
     * interrupt path needs its own cleanup — otherwise, with a healthy-but-silent channel, the
     * entry leaks per interruption.
     */
    @Test
    void sendSync_interruptedWhileWaiting_leaksNothingInFutureTable() throws Exception {
        // Server that accepts connections and never responds — the channel stays healthy.
        final int serverPort = startSilentServer();

        nettyRemotingClient = new NettyRemotingClient(NettyClientConfig.builder().build());
        final Host host = new Host("localhost", serverPort);
        nettyRemotingClient.getOrCreateChannel(host);

        final SyncRequestDto dto = newSyncRequest(host, 60_000);
        final long opaque = dto.getTransporter().getHeader().getOpaque();

        final ExecutorService pool = Executors.newSingleThreadExecutor();
        final java.util.concurrent.Future<?> call = pool.submit(() -> {
            try {
                nettyRemotingClient.sendSync(dto);
            } catch (Exception expected) {
                // interrupted — expected
            }
        });
        try {
            // Wait until the request is actually in flight (in FUTURE_TABLE) before interrupting.
            Awaitility.await().atMost(Duration.ofSeconds(5))
                    .until(() -> ResponseFuture.getFuture(opaque) != null);
            call.cancel(true); // interrupt the waiting caller
        } finally {
            pool.shutdownNow();
        }
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "Interrupted caller must terminate");

        assertNull(ResponseFuture.getFuture(opaque),
                "Interrupted request's future must be removed from FUTURE_TABLE");
    }

    /**
     * If the channel dies between {@code getOrCreateChannel}'s liveness check and the request
     * registration (e.g. the drain of a concurrent channel failure just ran), the send must fail
     * FAST: no write attempt on the dead channel, no FUTURE_TABLE leak, no waiting out the RPC
     * timeout for a response that can never arrive.
     */
    @Test
    void sendSync_inactiveChannelAtSend_failsFastWithoutWriting() throws Exception {
        nettyRemotingClient = Mockito.spy(new NettyRemotingClient(NettyClientConfig.builder().build()));
        final Channel deadChannel = mock(Channel.class);
        when(deadChannel.isActive()).thenReturn(false);

        final Host host = new Host("localhost", 12345);
        Mockito.doReturn(deadChannel).when(nettyRemotingClient).getOrCreateChannel(host);

        final SyncRequestDto dto = newSyncRequest(host, 60_000);
        final long opaque = dto.getTransporter().getHeader().getOpaque();

        final long start = System.nanoTime();
        assertThrows(RemoteException.class, () -> nettyRemotingClient.sendSync(dto));
        assertTrue(System.nanoTime() - start < 5_000_000_000L,
                "A dead channel must fail the send immediately, not wait out the timeout");
        Mockito.verify(deadChannel, Mockito.never()).writeAndFlush(Mockito.any());
        assertNull(ResponseFuture.getFuture(opaque),
                "The abandoned request must not stay in FUTURE_TABLE");
    }

    /**
     * A server that accepts connections and never responds — the channel stays healthy.
     */
    private int startSilentServer() throws Exception {
        serverBossGroup = new NioEventLoopGroup(1);
        serverWorkerGroup = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(serverBossGroup, serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // accept and hold the connection open — never reply
                    }
                });
        return ((InetSocketAddress) serverBootstrap.bind(0).sync().channel().localAddress()).getPort();
    }

    /**
     * A no-retry sync request against {@code host} with the given timeout.
     */
    private SyncRequestDto newSyncRequest(final Host host, final long timeoutMillis) {
        final RpcMethodRetryStrategy noRetry = Mockito.mock(RpcMethodRetryStrategy.class);
        when(noRetry.maxRetryTimes()).thenReturn(1);
        when(noRetry.retryFor()).thenReturn(new Class[0]);
        return SyncRequestDto.builder()
                .timeoutMillis(timeoutMillis)
                .retryStrategy(noRetry)
                .transporter(Transporter.of(TransporterHeader.of("test-method"), new byte[]{0x01}))
                .serverHost(host)
                .build();
    }

    private static boolean findCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            final Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return false;
    }

    /**
     * A late inactive notification for a DEAD channel must not evict the healthy replacement
     * another thread already created for the same host. Sequence: C1 dies, caller thread builds
     * C2 for host H (channels: H→C2), and only then C1's inactive event fires. Removing by host
     * would delete C2 — orphaning it (never reused, never closed by closeChannels) and forcing
     * every later request to build yet another connection. Removal must match the channel
     * identity, not the host.
     */
    @Test
    void onChannelInactive_lateEventForDeadChannel_keepsReplacementChannel() throws Exception {
        // Minimal echo-less server: connections are accepted and held open.
        final int serverPort = startSilentServer();

        nettyRemotingClient = new NettyRemotingClient(NettyClientConfig.builder().build());
        final Host host = new Host("localhost", serverPort);

        final Channel c1 = nettyRemotingClient.getOrCreateChannel(host);
        c1.close().sync();
        // Let C1's real inactive callback finish before building the replacement.
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> nettyRemotingClient.getOrCreateChannel(host) != c1);
        final Channel c2 = nettyRemotingClient.getOrCreateChannel(host);

        // C1's inactive notification arrives LATE (event-loop delay) — after C2 already replaced it.
        nettyRemotingClient.onChannelInactive(c1);

        final Channel c3 = nettyRemotingClient.getOrCreateChannel(host);
        assertSame(c2, c3, "A late inactive event for the dead channel must not evict its healthy replacement");
    }

    /**
     * A syntactically valid frame prefix whose declared body length (64 MB + 1) exceeds the
     * decoder's max frame size. Only the prefix is needed — the decoder rejects on the length
     * field before any body byte arrives.
     */
    private static byte[] malformedFrame() {
        final int maxFrameSize = 64 * 1024 * 1024;
        ByteBuffer buffer = ByteBuffer.allocate(2 + 4 + 8 + 4);
        buffer.put(Transporter.MAGIC);
        buffer.put(Transporter.VERSION);
        buffer.putInt(8); // header length
        buffer.put(new byte[8]); // header bytes
        buffer.putInt(maxFrameSize + 1); // body length > maxFrameSize
        return buffer.array();
    }
}
