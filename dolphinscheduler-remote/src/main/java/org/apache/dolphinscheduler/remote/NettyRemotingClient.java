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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.dolphinscheduler.remote.codec.NettyDecoder;
import org.apache.dolphinscheduler.remote.codec.NettyEncoder;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.exceptions.RemotingTimeoutException;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.handler.NettyClientHandler;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  remoting netty client
 */
public class NettyRemotingClient {

    private final Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);

    private final Bootstrap bootstrap = new Bootstrap();

    private final NettyEncoder encoder = new NettyEncoder();

    private final ConcurrentHashMap<Address, Channel> channels = new ConcurrentHashMap();

    private final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private final NioEventLoopGroup workerGroup;

    private final NettyClientHandler clientHandler = new NettyClientHandler(this);

    private final NettyClientConfig clientConfig;

    public NettyRemotingClient(final NettyClientConfig clientConfig){
        this.clientConfig = clientConfig;
        this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.start();
    }

    private void start(){

        this.bootstrap
                .group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new NettyDecoder(),
                                clientHandler,
                                encoder);
                    }
                });
        //
        isStarted.compareAndSet(false, true);
    }

    //TODO
    public void send(final Address address, final Command command, final InvokeCallback invokeCallback) throws RemotingException {
        final Channel channel = getChannel(address);
        if (channel == null) {
            throw new RemotingException("network error");
        }
        try {
            channel.writeAndFlush(command).addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        logger.info("sent command {} to {}", command, address);
                    } else{
                        logger.error("send command {} to {} failed, error {}", command, address, future.cause());
                    }
                }
            });
        } catch (Exception ex) {
            String msg = String.format("send command %s to address %s encounter error", command, address);
            throw new RemotingException(msg, ex);
        }
    }

    public Command sendSync(final Address address, final Command command, final long timeoutMillis) throws RemotingException {
        final Channel channel = getChannel(address);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", address));
        }
        final long opaque = command.getOpaque();
        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null);
            channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        responseFuture.setSendOk(true);
                        return;
                    } else{
                        responseFuture.setSendOk(false);
                        responseFuture.setCause(channelFuture.cause());
                        responseFuture.putResponse(null);
                        logger.error("send command {} to address {} failed", command, address);
                    }
                }
            });
            Command result = responseFuture.waitResponse();
            if(result == null){
                if(responseFuture.isSendOK()){
                    throw new RemotingTimeoutException(address.toString(), timeoutMillis, responseFuture.getCause());
                } else{
                    throw new RemoteException(address.toString(), responseFuture.getCause());
                }
            }
            return result;
        } catch (Exception ex) {
            String msg = String.format("send command %s to address %s error", command, address);
            throw new RemotingException(msg, ex);
        }
    }

    public Channel getChannel(Address address) {
        Channel channel = channels.get(address);
        if(channel != null && channel.isActive()){
            return channel;
        }
        return createChannel(address, true);
    }

    public Channel createChannel(Address address, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (bootstrap){
                future = bootstrap.connect(new InetSocketAddress(address.getHost(), address.getPort()));
            }
            if(isSync){
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channels.put(address, channel);
                return channel;
            }
        } catch (Exception ex) {
            logger.info("connect to {} error  {}", address, ex);
        }
        return null;
    }

    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    public void close() {
        if(isStarted.compareAndSet(true, false)){
            try {
                closeChannels();
                if(workerGroup != null){
                    this.workerGroup.shutdownGracefully();
                }
                if(defaultExecutor != null){
                    defaultExecutor.shutdown();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    private void closeChannels(){
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }

    public void closeChannel(Address address){
        Channel channel = this.channels.remove(address);
        if(channel != null){
            channel.close();
        }
    }
}
