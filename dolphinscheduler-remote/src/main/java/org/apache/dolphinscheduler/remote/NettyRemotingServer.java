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

package org.apache.dolphinscheduler.remote;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.dolphinscheduler.remote.codec.NettyDecoder;
import org.apache.dolphinscheduler.remote.codec.NettyEncoder;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.handler.NettyServerHandler;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class NettyRemotingServer {

    private final Logger logger = LoggerFactory.getLogger(NettyRemotingServer.class);

    private final ServerBootstrap serverBootstrap = new ServerBootstrap();

    private final NettyEncoder encoder = new NettyEncoder();

    private final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);

    private final NioEventLoopGroup bossGroup;

    private final NioEventLoopGroup workGroup;

    private final NettyServerConfig serverConfig;

    private final NettyServerHandler serverHandler = new NettyServerHandler(this);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public NettyRemotingServer(final NettyServerConfig serverConfig){
        this.serverConfig = serverConfig;

        this.bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
            }
        });

        this.workGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    public void start(){

        if(this.isStarted.get()){
            return;
        }

        this.serverBootstrap
                .group(this.bossGroup, this.workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, serverConfig.getSoBacklog())
                .option(ChannelOption.SO_KEEPALIVE, serverConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, serverConfig.isTcpNodelay())
                .option(ChannelOption.SO_SNDBUF, serverConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBufferSize())
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        initNettyChannel(ch);
                    }
                });

        ChannelFuture future;
        try {
            future = serverBootstrap.bind(serverConfig.getListenPort()).sync();
        } catch (Exception e) {
            logger.error("NettyRemotingServer bind fail {}, exit", e);
            throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()));
        }
        if (future.isSuccess()) {
            logger.info("NettyRemotingServer bind success at port : {}", serverConfig.getListenPort());
        } else if (future.cause() != null) {
            throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()), future.cause());
        } else {
            throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()));
        }
        //
        isStarted.compareAndSet(false, true);
    }

    private void initNettyChannel(NioSocketChannel ch) throws Exception{
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("encoder", encoder);
        pipeline.addLast("decoder", new NettyDecoder());
        pipeline.addLast("handler", serverHandler);
    }

    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor, final ExecutorService executor) {
        this.serverHandler.registerProcessor(commandType, processor, executor);
    }

    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    public void close() {
        if(isStarted.compareAndSet(true, false)){
            try {
                if(bossGroup != null){
                    this.bossGroup.shutdownGracefully();
                }
                if(workGroup != null){
                    this.workGroup.shutdownGracefully();
                }
                if(defaultExecutor != null){
                    defaultExecutor.shutdown();
                }
            } catch (Exception ex) {
                logger.error("netty server close exception", ex);
            }
            logger.info("netty server closed");
        }
    }
}
