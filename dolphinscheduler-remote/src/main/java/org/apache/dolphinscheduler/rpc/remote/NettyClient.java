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

package org.apache.dolphinscheduler.rpc.remote;

import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.NettyUtils;
import org.apache.dolphinscheduler.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.rpc.codec.NettyDecoder;
import org.apache.dolphinscheduler.rpc.codec.NettyEncoder;
import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.future.RpcFuture;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * NettyClient
 */
public class NettyClient {

    public static NettyClient getInstance() {
        return NettyClient.NettyClientInner.INSTANCE;
    }

    private static class NettyClientInner {

        private static final NettyClient INSTANCE = new NettyClient(new NettyClientConfig());
    }

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * worker group
     */
    private final EventLoopGroup workerGroup;

    /**
     * client config
     */
    private final NettyClientConfig clientConfig;


    /**
     * client bootstrap
     */
    private final Bootstrap bootstrap = new Bootstrap();

    /**
     * started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap<>(128);

    /**
     * get channel
     */
    private Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * create channel
     *
     * @param host host
     * @param isSync sync flag
     * @return channel
     */
    public Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channels.put(host, channel);
                return channel;
            }
        } catch (Exception ex) {
            logger.warn(String.format("connect to %s error", host), ex);
        }
        return null;
    }

    /**
     * client init
     *
     * @param clientConfig client config
     */
    private NettyClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (Epoll.isAvailable()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        this.start();

    }

    /**
     * start
     */
    private void start() {

        this.bootstrap
                .group(this.workerGroup)
                .channel(NettyUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new NettyEncoder())
                                .addLast(new NettyDecoder(RpcResponse.class))
                                .addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS))
                                .addLast(new NettyClientHandler());
                    }
                });

        isStarted.compareAndSet(false, true);
    }

    public RpcResponse sendMsg(Host host, RpcProtocol<RpcRequest> protocol, Boolean async) {

        Channel channel = getChannel(host);
        assert channel != null;
        RpcRequest request = protocol.getBody();
        RpcRequestCache rpcRequestCache = new RpcRequestCache();
        String serviceName = request.getClassName() + request.getMethodName();
        rpcRequestCache.setServiceName(serviceName);
        long reqId = protocol.getMsgHeader().getRequestId();
        RpcFuture future = null;
        if (Boolean.FALSE.equals(async)) {
            future = new RpcFuture(request, reqId);
            rpcRequestCache.setRpcFuture(future);
        }
        RpcRequestTable.put(protocol.getMsgHeader().getRequestId(), rpcRequestCache);
        channel.writeAndFlush(protocol);
        RpcResponse result = null;
        if (Boolean.TRUE.equals(async)) {
            result = new RpcResponse();
            result.setStatus((byte) 0);
            result.setResult(true);
            return result;
        }
        try {
            assert future != null;
            result = future.get();
        } catch (InterruptedException e) {
            logger.error("send msg error，service name is {}", serviceName, e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * close
     */
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    /**
     * close channels
     */
    private void closeChannels() {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }
}
