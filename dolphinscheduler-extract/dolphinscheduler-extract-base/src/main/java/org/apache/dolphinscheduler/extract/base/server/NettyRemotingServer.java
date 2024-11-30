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

package org.apache.dolphinscheduler.extract.base.server;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterDecoder;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterEncoder;
import org.apache.dolphinscheduler.extract.base.utils.NettyUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * remoting netty server
 */
@Slf4j
class NettyRemotingServer {

    private final ServerBootstrap serverBootstrap = new ServerBootstrap();

    @Getter
    private final String serverName;

    @Getter
    private final ExecutorService methodInvokerExecutor;

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workGroup;

    private final NettyServerConfig serverConfig;

    private final JdkDynamicServerHandler channelHandler;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    NettyRemotingServer(final NettyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.serverName = serverConfig.getServerName();
        this.methodInvokerExecutor = ThreadUtils.newDaemonFixedThreadExecutor(
                serverName + "-methodInvoker-%d", Runtime.getRuntime().availableProcessors() * 2 + 1);
        this.channelHandler = new JdkDynamicServerHandler(methodInvokerExecutor);
        ThreadFactory bossThreadFactory =
                ThreadUtils.newDaemonThreadFactory(serverName + "-boss-%d");
        ThreadFactory workerThreadFactory =
                ThreadUtils.newDaemonThreadFactory(serverName + "-worker-%d");
        if (Epoll.isAvailable()) {
            this.bossGroup = new EpollEventLoopGroup(1, bossThreadFactory);
            this.workGroup = new EpollEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        } else {
            this.bossGroup = new NioEventLoopGroup(1, bossThreadFactory);
            this.workGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        }
    }

    void start() {
        if (isStarted.compareAndSet(false, true)) {
            this.serverBootstrap
                    .group(this.bossGroup, this.workGroup)
                    .channel(NettyUtils.getServerSocketChannelClass())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, serverConfig.getSoBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isSoKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, serverConfig.isTcpNoDelay())
                    .childOption(ChannelOption.SO_SNDBUF, serverConfig.getSendBufferSize())
                    .childOption(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBufferSize())
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            initNettyChannel(ch);
                        }
                    });

            ChannelFuture future;
            try {
                future = serverBootstrap.bind(serverConfig.getListenPort()).sync();
            } catch (Exception e) {
                throw new RemoteException(
                        String.format("%s bind %s fail", serverConfig.getServerName(), serverConfig.getListenPort()),
                        e);
            }

            if (future.isSuccess()) {
                log.info("{} bind success at port: {}", serverConfig.getServerName(), serverConfig.getListenPort());
                return;
            }

            throw new RemoteException(
                    String.format("%s bind %s fail", serverConfig.getServerName(), serverConfig.getListenPort()),
                    future.cause());
        }
    }

    /**
     * init netty channel
     *
     * @param ch socket channel
     */
    private void initNettyChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast("encoder", new TransporterEncoder())
                .addLast("decoder", new TransporterDecoder())
                .addLast("server-idle-handle",
                        new IdleStateHandler(serverConfig.getConnectionIdleTime(), 0, 0, TimeUnit.MILLISECONDS))
                .addLast("handler", channelHandler);
    }

    void registerMethodInvoker(ServerMethodInvoker methodInvoker) {
        channelHandler.registerMethodInvoker(methodInvoker);
    }

    void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                if (bossGroup != null) {
                    this.bossGroup.shutdownGracefully();
                }
                if (workGroup != null) {
                    this.workGroup.shutdownGracefully();
                }
                methodInvokerExecutor.shutdown();
            } catch (Exception ex) {
                log.error("netty server close exception", ex);
            }
            log.info("netty server closed");
        }
    }
}
