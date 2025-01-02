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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.extract.base.IRpcResponse;
import org.apache.dolphinscheduler.extract.base.RpcMethodRetryStrategy;
import org.apache.dolphinscheduler.extract.base.SyncRequestDto;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.exception.RemoteTimeoutException;
import org.apache.dolphinscheduler.extract.base.future.ResponseFuture;
import org.apache.dolphinscheduler.extract.base.metrics.ClientSyncDurationMetrics;
import org.apache.dolphinscheduler.extract.base.metrics.ClientSyncExceptionMetrics;
import org.apache.dolphinscheduler.extract.base.metrics.RpcMetrics;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterDecoder;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterEncoder;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.extract.base.utils.NettyUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

@Slf4j
public class NettyRemotingClient implements AutoCloseable {

    private final Bootstrap bootstrap = new Bootstrap();

    private final ReentrantLock channelsLock = new ReentrantLock();
    private final Map<Host, Channel> channels = new ConcurrentHashMap<>();

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private final EventLoopGroup workerGroup;

    private final NettyClientConfig clientConfig;

    private final NettyClientHandler clientHandler;

    public NettyRemotingClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        ThreadFactory nettyClientThreadFactory = ThreadUtils.newDaemonThreadFactory("NettyClientThread-");
        if (Epoll.isAvailable()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), nettyClientThreadFactory);
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), nettyClientThreadFactory);
        }
        this.clientHandler = new NettyClientHandler(this);

        this.start();
    }

    private void start() {

        this.bootstrap
                .group(this.workerGroup)
                .channel(NettyUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("client-idle-handler",
                                        new IdleStateHandler(
                                                0,
                                                clientConfig.getHeartBeatIntervalMillis(),
                                                0,
                                                TimeUnit.MILLISECONDS))
                                .addLast(new TransporterDecoder(), clientHandler, new TransporterEncoder());
                    }
                });
        isStarted.compareAndSet(false, true);
    }

    public IRpcResponse sendSync(final SyncRequestDto syncRequestDto) throws RemoteException {
        final Host host = syncRequestDto.getServerHost();
        final Transporter transporter = syncRequestDto.getTransporter();
        final long timeoutMillis = syncRequestDto.getTimeoutMillis() < 0 ? clientConfig.getDefaultRpcTimeoutMillis()
                : syncRequestDto.getTimeoutMillis();

        final RpcMethodRetryStrategy retryStrategy = syncRequestDto.getRetryStrategy();

        int maxRetryTimes = retryStrategy.maxRetryTimes();
        int currentExecuteTimes = 1;

        while (true) {
            final long start = System.currentTimeMillis();
            try {
                return doSendSync(transporter, host, timeoutMillis);
            } catch (Exception ex) {
                ClientSyncExceptionMetrics clientSyncExceptionMetrics =
                        ClientSyncExceptionMetrics.of(syncRequestDto, ex);
                RpcMetrics.recordClientSyncRequestException(clientSyncExceptionMetrics);

                if (currentExecuteTimes < maxRetryTimes
                        && Arrays.stream(retryStrategy.retryFor()).anyMatch(e -> e.isInstance(ex))) {
                    currentExecuteTimes++;
                    if (retryStrategy.retryInterval() > 0) {
                        ThreadUtils.sleep(retryStrategy.retryInterval());
                    }
                    continue;
                }

                if (ex instanceof RemoteException) {
                    throw (RemoteException) ex;
                } else {
                    throw new RemoteException("Call method to " + host + " failed", ex);
                }
            } finally {
                ClientSyncDurationMetrics clientSyncDurationMetrics = ClientSyncDurationMetrics
                        .of(syncRequestDto)
                        .withMilliseconds(System.currentTimeMillis() - start);
                RpcMetrics.recordClientSyncRequestDuration(clientSyncDurationMetrics);
            }
        }
    }

    private IRpcResponse doSendSync(final Transporter transporter,
                                    final Host serverHost,
                                    long timeoutMills) throws RemoteException, InterruptedException {
        final Channel channel = getOrCreateChannel(serverHost);
        if (channel == null) {
            throw new RemoteException(String.format("connect to : %s fail", serverHost));
        }
        final ResponseFuture responseFuture = new ResponseFuture(transporter.getHeader().getOpaque(), timeoutMills);
        channel.writeAndFlush(transporter).addListener(future -> {
            if (future.isSuccess()) {
                responseFuture.setSendOk(true);
                return;
            } else {
                responseFuture.setSendOk(false);
            }
            responseFuture.setCause(future.cause());
            responseFuture.putResponse(null);
            log.error("Send Sync request {} to host {} failed", transporter, serverHost, responseFuture.getCause());
        });
        /*
         * sync wait for result
         */
        final IRpcResponse iRpcResponse = responseFuture.waitResponse();
        if (iRpcResponse != null) {
            return iRpcResponse;
        }
        if (responseFuture.isSendOK()) {
            throw new RemoteTimeoutException(serverHost.toString(), timeoutMills, responseFuture.getCause());
        } else {
            throw new RemoteException(serverHost.toString(), responseFuture.getCause());
        }
    }

    Channel getOrCreateChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        try {
            channelsLock.lock();
            channel = channels.get(host);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            channel = createChannel(host);
            channels.put(host, channel);
        } finally {
            channelsLock.unlock();
        }
        return channel;
    }

    /**
     * create channel
     *
     * @param host host
     * @return channel
     */
    Channel createChannel(Host host) {
        try {
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            future = future.sync();
            if (future.isSuccess()) {
                return future.channel();
            } else {
                throw new IllegalArgumentException("connect to host: " + host + " failed", future.cause());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Connect to host: " + host + " failed", e);
        }
    }

    @Override
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
                log.info("netty client closed");
            } catch (Exception ex) {
                log.error("netty client close exception", ex);
            }
        }
    }

    private void closeChannels() {
        try {
            channelsLock.lock();
            channels.values().forEach(Channel::close);
        } finally {
            channelsLock.unlock();
        }
    }

    public void closeChannel(Host host) {
        try {
            channelsLock.lock();
            Channel channel = this.channels.remove(host);
            if (channel != null) {
                channel.close();
            }
        } finally {
            channelsLock.unlock();
        }
    }
}
