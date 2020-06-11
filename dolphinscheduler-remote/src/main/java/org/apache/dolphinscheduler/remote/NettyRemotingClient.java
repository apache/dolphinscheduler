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
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.exceptions.RemotingTimeoutException;
import org.apache.dolphinscheduler.remote.exceptions.RemotingTooMuchRequestException;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ReleaseSemaphore;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.handler.NettyClientHandler;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.CallerThreadExecutePolicy;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  remoting netty client
 */
public class NettyRemotingClient {

    private final Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);

    /**
     * client bootstrap
     */
    private final Bootstrap bootstrap = new Bootstrap();

    /**
     *  encoder
     */
    private final NettyEncoder encoder = new NettyEncoder();

    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap(128);

    /**
     *  started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     *  worker group
     */
    private final NioEventLoopGroup workerGroup;

    /**
     *  client config
     */
    private final NettyClientConfig clientConfig;

    /**
     *  saync semaphore
     */
    private final Semaphore asyncSemaphore = new Semaphore(200, true);

    /**
     *  callback thread executor
     */
    private final ExecutorService callbackExecutor;

    /**
     *  client handler
     */
    private final NettyClientHandler clientHandler;

    /**
     *  response future executor
     */
    private final ScheduledExecutorService responseFutureExecutor;

    /**
     *  client init
     * @param clientConfig client config
     */
    public NettyRemotingClient(final NettyClientConfig clientConfig){
        this.clientConfig = clientConfig;
        this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.callbackExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(1000), new NamedThreadFactory("CallbackExecutor", 10),
                new CallerThreadExecutePolicy());
        this.clientHandler = new NettyClientHandler(this, callbackExecutor);

        this.responseFutureExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ResponseFutureExecutor"));

        this.start();
    }

    /**
     *  start
     */
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
        this.responseFutureExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ResponseFuture.scanFutureTable();
            }
        }, 5000, 1000, TimeUnit.MILLISECONDS);
        //
        isStarted.compareAndSet(false, true);
    }

    /**
     *  async send
     * @param host host
     * @param command command
     * @param timeoutMillis timeoutMillis
     * @param invokeCallback callback function
     * @throws InterruptedException
     * @throws RemotingException
     */
    public void sendAsync(final Host host, final Command command,
                          final long timeoutMillis,
                          final InvokeCallback invokeCallback) throws InterruptedException, RemotingException {
        final Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException("network error");
        }
        /**
         * request unique identification
         */
        final long opaque = command.getOpaque();
        /**
         *  control concurrency number
         */
        boolean acquired = this.asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if(acquired){
            final ReleaseSemaphore releaseSemaphore = new ReleaseSemaphore(this.asyncSemaphore);

            /**
             *  response future
             */
            final ResponseFuture responseFuture = new ResponseFuture(opaque,
                    timeoutMillis,
                    invokeCallback,
                    releaseSemaphore);
            try {
                channel.writeAndFlush(command).addListener(new ChannelFutureListener(){

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            responseFuture.setSendOk(true);
                            return;
                        } else {
                            responseFuture.setSendOk(false);
                        }
                        responseFuture.setCause(future.cause());
                        responseFuture.putResponse(null);
                        try {
                            responseFuture.executeInvokeCallback();
                        } catch (Throwable ex){
                            logger.error("execute callback error", ex);
                        } finally{
                            responseFuture.release();
                        }
                    }
                });
            } catch (Throwable ex){
                responseFuture.release();
                throw new RemotingException(String.format("send command to host: %s failed", host), ex);
            }
        } else{
            String message = String.format("try to acquire async semaphore timeout: %d, waiting thread num: %d, total permits: %d",
                    timeoutMillis, asyncSemaphore.getQueueLength(), asyncSemaphore.availablePermits());
            throw new RemotingTooMuchRequestException(message);
        }
    }

    /**
     * sync send
     * @param host host
     * @param command command
     * @param timeoutMillis timeoutMillis
     * @return command
     * @throws InterruptedException
     * @throws RemotingException
     */
    public Command sendSync(final Host host, final Command command, final long timeoutMillis) throws InterruptedException, RemotingException {
        final Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", host));
        }
        final long opaque = command.getOpaque();
        final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
        channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    responseFuture.setSendOk(true);
                    return;
                } else {
                    responseFuture.setSendOk(false);
                }
                responseFuture.setCause(future.cause());
                responseFuture.putResponse(null);
                logger.error("send command {} to host {} failed", command, host);
            }
        });
        /**
         * sync wait for result
         */
        Command result = responseFuture.waitResponse();
        if(result == null){
            if(responseFuture.isSendOK()){
                throw new RemotingTimeoutException(host.toString(), timeoutMillis, responseFuture.getCause());
            } else{
                throw new RemotingException(host.toString(), responseFuture.getCause());
            }
        }
        return result;
    }

    /**
     *  send task
     * @param host host
     * @param command command
     * @throws RemotingException
     */
    public void send(final Host host, final Command command) throws RemotingException {
        Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", host));
        }
        try {
            ChannelFuture future = channel.writeAndFlush(command).await();
            if (future.isSuccess()) {
                logger.debug("send command : {} , to : {} successfully.", command, host.getAddress());
            } else {
                String msg = String.format("send command : %s , to :%s failed", command, host.getAddress());
                logger.error(msg, future.cause());
                throw new RemotingException(msg);
            }
        } catch (Exception e) {
            logger.error("Send command {} to address {} encounter error.", command, host.getAddress());
            throw new RemotingException(String.format("Send command : %s , to :%s encounter error", command, host.getAddress()), e);
        }
    }

    /**
     *  register processor
     * @param commandType command type
     * @param processor processor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor) {
        this.registerProcessor(commandType, processor, null);
    }

    /**
     *  register processor
     *
     * @param commandType command type
     * @param processor processor
     * @param executor thread executor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor, final ExecutorService executor) {
        this.clientHandler.registerProcessor(commandType, processor, executor);
    }

    /**
     *  get channel
     * @param host
     * @return
     */
    public Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if(channel != null && channel.isActive()){
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * create channel
     * @param host host
     * @param isSync sync flag
     * @return channel
     */
    public Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (bootstrap){
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if(isSync){
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
     * close
     */
    public void close() {
        if(isStarted.compareAndSet(true, false)){
            try {
                closeChannels();
                if(workerGroup != null){
                    this.workerGroup.shutdownGracefully();
                }
                if(callbackExecutor != null){
                    this.callbackExecutor.shutdownNow();
                }
                if(this.responseFutureExecutor != null){
                    this.responseFutureExecutor.shutdownNow();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    /**
     *  close channels
     */
    private void closeChannels(){
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }

    /**
     * close channel
     * @param host host
     */
    public void closeChannel(Host host){
        Channel channel = this.channels.remove(host);
        if(channel != null){
            channel.close();
        }
    }
}
