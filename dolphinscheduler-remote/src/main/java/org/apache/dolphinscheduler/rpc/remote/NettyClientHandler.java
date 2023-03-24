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

package org.apache.dolphinscheduler.rpc.remote;

import org.apache.dolphinscheduler.rpc.client.ConsumerConfig;
import org.apache.dolphinscheduler.rpc.client.ConsumerConfigCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.common.ThreadPoolManager;
import org.apache.dolphinscheduler.rpc.future.RpcFuture;
import org.apache.dolphinscheduler.rpc.protocol.EventType;
import org.apache.dolphinscheduler.rpc.protocol.MessageHeader;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;

import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * NettyClientHandler
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final ThreadPoolManager threadPoolManager = ThreadPoolManager.INSTANCE;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        RpcResponse rsp = (RpcResponse) rpcProtocol.getBody();
        long reqId = rpcProtocol.getMsgHeader().getRequestId();
        RpcRequestCache rpcRequest = RpcRequestTable.get(reqId);

        if (null == rpcRequest) {
            log.warn("rpc read error,this request does not exist");
            return;
        }
        threadPoolManager.addExecuteTask(() -> readHandler(rsp, rpcRequest, reqId));
    }

    private void readHandler(RpcResponse rsp, RpcRequestCache rpcRequest, long reqId) {
        String serviceName = rpcRequest.getServiceName();
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (Boolean.FALSE.equals(consumerConfig.getAsync())) {
            RpcFuture future = rpcRequest.getRpcFuture();
            RpcRequestTable.remove(reqId);
            future.done(rsp);
            return;
        }

        if (Boolean.FALSE.equals(consumerConfig.getCallBack())) {
            return;
        }

        if (rsp.getStatus() == 0) {

            try {
                consumerConfig.getServiceCallBackClass().getDeclaredConstructor().newInstance().run(rsp.getResult());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                log.error("rpc service call back error,serviceName {},rsp {}", serviceName, rsp);
            }
        } else {
            log.error("rpc response error ,serviceName {},rsp {}", serviceName, rsp);
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            RpcProtocol rpcProtocol = new RpcProtocol();
            MessageHeader messageHeader = new MessageHeader();
            messageHeader.setEventType(EventType.HEARTBEAT.getType());
            rpcProtocol.setMsgHeader(messageHeader);
            ctx.channel().writeAndFlush(rpcProtocol);
            log.debug("send heart beat msg...");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }

}
