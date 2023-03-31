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

import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import lombok.extern.slf4j.Slf4j;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * netty server request handler
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * netty remote server
     */
    private final NettyRemotingServer nettyRemotingServer;

    /**
     * server processors queue
     */
    private final ConcurrentHashMap<MessageType, Pair<NettyRequestProcessor, ExecutorService>> processors =
            new ConcurrentHashMap<>();

    public NettyServerHandler(NettyRemotingServer nettyRemotingServer) {
        this.nettyRemotingServer = nettyRemotingServer;
    }

    /**
     * When the current channel is not active,
     * the current channel has reached the end of its life cycle
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    /**
     * The current channel reads data from the remote end
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
            executorRef = nettyRemotingServer.getDefaultExecutor();
        }
        this.processors.putIfAbsent(messageType, new Pair<>(processor, executorRef));
    }

    /**
     * process received logic
     *
     * @param channel channel
     * @param msg message
     */
    private void processReceived(final Channel channel, final Message msg) {
        final MessageType messageType = msg.getType();
        if (MessageType.HEART_BEAT.equals(messageType)) {
            if (log.isDebugEnabled()) {
                log.debug("server receive heart beat from: host: {}", ChannelUtils.getRemoteAddress(channel));
            }
            return;
        }
        final Pair<NettyRequestProcessor, ExecutorService> pair = processors.get(messageType);
        if (pair != null) {
            Runnable r = () -> {
                try {
                    pair.getLeft().process(channel, msg);
                } catch (Exception ex) {
                    log.error("process msg {} error", msg, ex);
                }
            };
            try {
                pair.getRight().submit(r);
            } catch (RejectedExecutionException e) {
                log.warn("thread pool is full, discard msg {} from {}", msg, ChannelUtils.getRemoteAddress(channel));
            }
        } else {
            log.warn("commandType {} not support", messageType);
        }
    }

    /**
     * caught exception
     *
     * @param ctx channel handler context
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }

    /**
     * channel write changed
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        ChannelConfig config = ch.config();

        if (!ch.isWritable()) {
            if (log.isWarnEnabled()) {
                log.warn("{} is not writable, over high water level : {}",
                        ch, config.getWriteBufferHighWaterMark());
            }

            config.setAutoRead(false);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("{} is writable, to low water : {}",
                        ch, config.getWriteBufferLowWaterMark());
            }
            config.setAutoRead(true);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
