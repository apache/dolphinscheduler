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

import io.netty.channel.*;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 *  netty client request handler
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     *  netty remote client
     */
    private final NettyRemotingClient nettyRemotingClient;

    /**
     *  client processors queue
     */
    private final ConcurrentHashMap<CommandType, Pair<NettyRequestProcessor, ExecutorService>> processors = new ConcurrentHashMap();

    public NettyClientHandler(NettyRemotingClient nettyRemotingClient){
        this.nettyRemotingClient = nettyRemotingClient;
    }

    /**
     *  When the current channel is not active,
     *  the current channel has reached the end of its life cycle
     *
     * @param ctx channel handler context
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyRemotingClient.removeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     *  The current channel reads data from the remote
     *
     * @param ctx channel handler context
     * @param msg message
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processReceived(ctx.channel(), (Command)msg);
    }

    /**
     *  register processor
     *
     * @param commandType command type
     * @param processor processor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor) {
         this.registerProcessor(commandType, processor, nettyRemotingClient.getDefaultExecutor());
    }

    /**
     * register processor
     *
     * @param commandType command type
     * @param processor processor
     * @param executor thread executor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor, final ExecutorService executor) {
        ExecutorService executorRef = executor;
        if(executorRef == null){
            executorRef = nettyRemotingClient.getDefaultExecutor();
        }
        this.processors.putIfAbsent(commandType, new Pair<NettyRequestProcessor, ExecutorService>(processor, executorRef));
    }

    /**
     *  process received logic
     *
     * @param channel channel
     * @param msg message
     */
    private void processReceived(final Channel channel, final Command msg) {
        final CommandType commandType = msg.getType();
        final Pair<NettyRequestProcessor, ExecutorService> pair = processors.get(commandType);
        if (pair != null) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        pair.getLeft().process(channel, msg);
                    } catch (Throwable ex) {
                        logger.error("process msg {} error : {}", msg, ex);
                    }
                }
            };
            try {
                pair.getRight().submit(r);
            } catch (RejectedExecutionException e) {
                logger.warn("thread pool is full, discard msg {} from {}", msg, ChannelUtils.getRemoteAddress(channel));
            }
        } else {
            logger.warn("commandType {} not support", commandType);
        }
    }

    /**
     *  caught exception
     *
     * @param ctx channel handler context
     * @param cause cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught : {}", cause);
        nettyRemotingClient.removeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     *  channel write changed
     * @param ctx channel handler context
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        ChannelConfig config = ch.config();

        if (!ch.isWritable()) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is not writable, over high water level : {}",
                        new Object[]{ch, config.getWriteBufferHighWaterMark()});
            }

            config.setAutoRead(false);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is writable, to low water : {}",
                        new Object[]{ch, config.getWriteBufferLowWaterMark()});
            }
            config.setAutoRead(true);
        }
    }
}