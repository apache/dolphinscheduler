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
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 *  netty client request handler
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     *  netty client
     */
    private final NettyRemotingClient nettyRemotingClient;

    /**
     *  callback thread executor
     */
    private final ExecutorService callbackExecutor;

    public NettyClientHandler(NettyRemotingClient nettyRemotingClient, ExecutorService callbackExecutor){
        this.nettyRemotingClient = nettyRemotingClient;
        this.callbackExecutor = callbackExecutor;
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
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
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
        processReceived((Command)msg);
    }

    /**
     *  process received logic
     *
     * @param responseCommand responseCommand
     */
    private void processReceived(final Command responseCommand) {
        ResponseFuture future = ResponseFuture.getFuture(responseCommand.getOpaque());
        if(future != null){
            future.setResponseCommand(responseCommand);
            future.release();
            if(future.getInvokeCallback() != null){
                this.callbackExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        future.executeInvokeCallback();
                    }
                });
            } else{
                future.putResponse(responseCommand);
            }
        } else{
            logger.warn("receive response {}, but not matched any request ", responseCommand);
        }
    }

    /**
     *  caught exception
     * @param ctx channel handler context
     * @param cause  cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught : {}",cause.getMessage(), cause);
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     *  channel write changed
     *
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
                        ch, config.getWriteBufferHighWaterMark());
            }

            config.setAutoRead(false);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is writable, to low water : {}",
                        ch, config.getWriteBufferLowWaterMark());
            }
            config.setAutoRead(true);
        }
    }
}