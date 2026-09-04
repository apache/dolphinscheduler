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
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.future.ResponseFuture;
import org.apache.dolphinscheduler.extract.base.protocal.HeartBeatTransporter;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

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
        log.info("Channel inactive: {}", ctx.channel());
        // The channel is gone: no response can ever arrive, fail every in-flight request sent on
        // it now instead of letting each caller wait for its individual RPC timeout.
        ResponseFuture.failAllForChannel(ctx.channel(),
                new RemoteException("Channel closed before response arrived: " + ctx.channel()));
        nettyRemotingClient.onChannelInactive(ctx.channel());
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
        // Only putResponse writes the field — it must go through the done-guard so a future that
        // was already failed by a channel error cannot be silently resurrected by a late response.
        // If deserialization throws, the request is still in FUTURE_TABLE, so the exception
        // flowing into exceptionCaught below fails it with the real error instead of a timeout.
        future.putResponse(deserialize);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("NettyClientHandler catch an exception on channel: {}", ctx.channel(), cause);
        // Fail ALL in-flight requests on this channel with the REAL cause (e.g.
        // TooLongFrameException from the maxFrameSize guard) — callers must not wait until RPC
        // timeout, and the caller-side error inspection relies on the original exception.
        ResponseFuture.failAllForChannel(ctx.channel(), cause);
        nettyRemotingClient.onChannelInactive(ctx.channel());
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel()
                    .writeAndFlush(HeartBeatTransporter.getHeartBeatTransporter())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (log.isDebugEnabled()) {
                log.info("Client send heartbeat to: {}", ctx.channel().remoteAddress());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
