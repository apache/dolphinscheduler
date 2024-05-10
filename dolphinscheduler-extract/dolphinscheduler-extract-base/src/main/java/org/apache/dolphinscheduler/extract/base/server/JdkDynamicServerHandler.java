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

package org.apache.dolphinscheduler.extract.base.server;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.extract.base.StandardRpcRequest;
import org.apache.dolphinscheduler.extract.base.StandardRpcResponse;
import org.apache.dolphinscheduler.extract.base.protocal.HeartBeatTransporter;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterHeader;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;
import org.apache.dolphinscheduler.extract.base.utils.ChannelUtils;

import java.util.Map;
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

@Slf4j
@ChannelHandler.Sharable
class JdkDynamicServerHandler extends ChannelInboundHandlerAdapter {

    private final ExecutorService methodInvokeExecutor;

    private final Map<String, ServerMethodInvoker> methodInvokerMap;

    JdkDynamicServerHandler(ExecutorService methodInvokeExecutor) {
        this.methodInvokeExecutor = methodInvokeExecutor;
        this.methodInvokerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        processReceived(ctx.channel(), (Transporter) msg);
    }

    public void registerMethodInvoker(ServerMethodInvoker methodInvoker) {
        checkNotNull(methodInvoker);
        checkNotNull(methodInvoker.getMethodIdentify());

        methodInvokerMap.put(methodInvoker.getMethodIdentify(), methodInvoker);
    }

    private void processReceived(final Channel channel, final Transporter transporter) {
        final String methodIdentifier = transporter.getHeader().getMethodIdentifier();
        if (HeartBeatTransporter.METHOD_IDENTIFY.equals(methodIdentifier)) {
            if (log.isDebugEnabled()) {
                log.debug("server receive heart beat from: host: {}", ChannelUtils.getRemoteAddress(channel));
            }
            return;
        }
        ServerMethodInvoker methodInvoker = methodInvokerMap.get(methodIdentifier);
        try {
            if (methodInvoker == null) {
                log.error("Cannot find the ServerMethodInvoker of : {}", transporter);
                StandardRpcResponse iRpcResponse =
                        StandardRpcResponse.fail("Cannot find the ServerMethodInvoker of " + methodIdentifier);
                TransporterHeader transporterHeader =
                        TransporterHeader.of(transporter.getHeader().getOpaque(), methodIdentifier);
                Transporter response = Transporter.of(transporterHeader, iRpcResponse);
                channel.writeAndFlush(response);
                return;
            }
            methodInvokeExecutor.execute(() -> {
                StandardRpcResponse iRpcResponse;
                try {
                    StandardRpcRequest standardRpcRequest =
                            JsonSerializer.deserialize(transporter.getBody(), StandardRpcRequest.class);
                    Object[] args;
                    if (standardRpcRequest.getArgs() == null || standardRpcRequest.getArgs().length == 0) {
                        args = null;
                    } else {
                        args = new Object[standardRpcRequest.getArgs().length];
                        for (int i = 0; i < standardRpcRequest.getArgs().length; i++) {
                            args[i] = JsonSerializer.deserialize(standardRpcRequest.getArgs()[i],
                                    standardRpcRequest.getArgsTypes()[i]);
                        }
                    }
                    Object result = methodInvoker.invoke(args);
                    if (result == null) {
                        iRpcResponse = StandardRpcResponse.success(null, null);
                    } else {
                        iRpcResponse = StandardRpcResponse.success(JsonSerializer.serialize(result), result.getClass());
                    }
                } catch (Throwable e) {
                    log.error("Invoke method {} failed, {}.", methodIdentifier, e.getMessage(), e);
                    iRpcResponse = StandardRpcResponse.fail(e.getMessage());
                }
                TransporterHeader transporterHeader =
                        TransporterHeader.of(transporter.getHeader().getOpaque(), methodIdentifier);
                Transporter response = Transporter.of(transporterHeader, iRpcResponse);
                channel.writeAndFlush(response);
            });
        } catch (RejectedExecutionException e) {
            log.warn("NettyRemotingServer's thread pool is full, discard msg {} from {}", transporter,
                    ChannelUtils.getRemoteAddress(channel));
            StandardRpcResponse iRpcResponse = StandardRpcResponse.fail("NettyRemotingServer's thread pool is full");
            TransporterHeader transporterHeader =
                    TransporterHeader.of(transporter.getHeader().getOpaque(), methodIdentifier);
            Transporter response = Transporter.of(transporterHeader, iRpcResponse);
            channel.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
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
                log.warn("{} is writable, to low water : {}", ch, config.getWriteBufferLowWaterMark());
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
