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

import org.apache.dolphinscheduler.extract.base.StandardRpcResponse;
import org.apache.dolphinscheduler.extract.base.future.ResponseFuture;
import org.apache.dolphinscheduler.extract.base.protocal.HeartBeatTransporter;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;
import org.apache.dolphinscheduler.extract.base.utils.ChannelUtils;

import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final NettyRemotingClient nettyRemotingClient;

    public NettyClientHandler(NettyRemotingClient nettyRemotingClient) {
        this.nettyRemotingClient = nettyRemotingClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        processReceived((Transporter) msg);
    }

    private void processReceived(final Transporter transporter) {
        ResponseFuture future = ResponseFuture.getFuture(transporter.getHeader().getOpaque());
        if (future == null) {
            log.warn("Cannot find the ResponseFuture if transporter: {}", transporter);
            return;
        }
        StandardRpcResponse deserialize = JsonSerializer.deserialize(transporter.getBody(), StandardRpcResponse.class);
        future.setIRpcResponse(deserialize);
        future.putResponse(deserialize);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("NettyClientHandler catch an exception : {}", cause.getMessage(), cause);
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel()
                    .writeAndFlush(HeartBeatTransporter.getHeartBeatTransporter())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (log.isDebugEnabled()) {
                log.debug("Client send heart beat to: {}", ChannelUtils.getRemoteAddress(ctx.channel()));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
