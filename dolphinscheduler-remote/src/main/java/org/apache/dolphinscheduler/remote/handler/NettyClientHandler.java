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

package org.apache.dolphinscheduler.remote.handler;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import lombok.extern.slf4j.Slf4j;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * netty client request handler
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * netty client
     */
    private final NettyRemotingClient nettyRemotingClient;

    private static byte[] heartBeatData = "heart_beat".getBytes();

    /**
     * callback thread executor
     */
    private final ExecutorService callbackExecutor;

    /**
     * processors
     */
    private final ConcurrentHashMap<MessageType, Pair<NettyRequestProcessor, ExecutorService>> processors;

    /**
     * default executor
     */
    private final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);

    public NettyClientHandler(NettyRemotingClient nettyRemotingClient, ExecutorService callbackExecutor) {
        this.nettyRemotingClient = nettyRemotingClient;
        this.callbackExecutor = callbackExecutor;
        this.processors = new ConcurrentHashMap<>();
    }

    /**
     * When the current channel is not active,
     * the current channel has reached the end of its life cycle
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     * The current channel reads data from the remote
     *
     * @param ctx channel handler context
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        processReceived(ctx.channel(), (Message) msg);
    }

    /**
     * register processor
     *
     * @param messageType command type
     * @param processor processor
     */
    public void registerProcessor(final MessageType messageType, final NettyRequestProcessor processor) {
        this.registerProcessor(messageType, processor, null);
    }

    /**
     * register processor
     *
     * @param messageType command type
     * @param processor processor
     * @param executor thread executor
     */
    public void registerProcessor(final MessageType messageType, final NettyRequestProcessor processor,
                                  final ExecutorService executor) {
        ExecutorService executorRef = executor;
        if (executorRef == null) {
            executorRef = defaultExecutor;
        }
        this.processors.putIfAbsent(messageType, new Pair<>(processor, executorRef));
    }

    /**
     * process received logic
     *
     * @param message command
     */
    private void processReceived(final Channel channel, final Message message) {
        ResponseFuture future = ResponseFuture.getFuture(message.getOpaque());
        if (future != null) {
            future.setResponseCommand(message);
            future.release();
            if (future.getInvokeCallback() != null) {
                future.removeFuture();
                this.callbackExecutor.submit(future::executeInvokeCallback);
            } else {
                future.putResponse(message);
            }
        } else {
            processByCommandType(channel, message);
        }
    }

    public void processByCommandType(final Channel channel, final Message message) {
        final Pair<NettyRequestProcessor, ExecutorService> pair = processors.get(message.getType());
        if (pair != null) {
            Runnable run = () -> {
                try {
                    pair.getLeft().process(channel, message);
                } catch (Exception e) {
                    log.error(String.format("process command %s exception", message), e);
                }
            };
            try {
                pair.getRight().submit(run);
            } catch (RejectedExecutionException e) {
                log.warn("thread pool is full, discard command {} from {}", message,
                        ChannelUtils.getRemoteAddress(channel));
            }
        } else {
            log.warn("receive response {}, but not matched any request ", message);
        }
    }

    /**
     * caught exception
     *
     * @param ctx channel handler context
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught : {}", cause.getMessage(), cause);
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Message heartBeat = new Message();
            heartBeat.setType(MessageType.HEART_BEAT);
            heartBeat.setBody(heartBeatData);
            ctx.channel().writeAndFlush(heartBeat)
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (log.isDebugEnabled()) {
                log.debug("Client send heart beat to: {}", ChannelUtils.getRemoteAddress(ctx.channel()));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
