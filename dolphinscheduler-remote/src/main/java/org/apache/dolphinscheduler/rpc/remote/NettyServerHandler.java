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

import org.apache.dolphinscheduler.rpc.common.RequestEventType;
import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.common.ThreadPoolManager;
import org.apache.dolphinscheduler.rpc.config.ServiceBean;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * NettyServerHandler
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final ThreadPoolManager threadPoolManager = ThreadPoolManager.INSTANCE;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("channel close");
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client connect success !" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        RpcRequest req = (RpcRequest) msg;

        if (req.getEventType().equals(RequestEventType.HEARTBEAT.getType())) {

            logger.info("accept heartbeat msg");
            return;
        }
        threadPoolManager.addExecuteTask(() -> readHandler(ctx, req));
    }

    private void readHandler(ChannelHandlerContext ctx, RpcRequest req) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(req.getRequestId());

        response.setStatus((byte) 0);

        String classname = req.getClassName();

        String methodName = req.getMethodName();

        Class<?>[] parameterTypes = req.getParameterTypes();

        Object[] arguments = req.getParameters();
        Object result = null;
        try {
            Class serviceClass = ServiceBean.getServiceClass(classname);

            Object object = serviceClass.newInstance();

            Method method = serviceClass.getMethod(methodName, parameterTypes);

            result = method.invoke(object, arguments);
        } catch (Exception e) {
            logger.error("netty server execute error,service name {}", classname + methodName, e);
            response.setStatus((byte) -1);
        }

        response.setResult(result);
        ctx.writeAndFlush(response);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }
}
