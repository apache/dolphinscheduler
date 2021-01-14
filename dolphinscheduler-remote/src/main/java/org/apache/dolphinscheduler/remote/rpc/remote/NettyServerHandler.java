package org.apache.dolphinscheduler.remote.rpc.remote;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangli
 * @date 2021-01-13 19:20
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        logger.info("channel close");
       ctx.channel().close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接成功!"+ctx.channel().remoteAddress());
        logger.info("客户端连接成功!"+ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logger.info("server read msg");
        System.out.println("收到消息");
        RpcRequest req= (RpcRequest) msg;
        System.out.println(req.getRequestId());
        RpcResponse response=new RpcResponse();
        response.setMsg("llll");
        response.setRequestId(req.getRequestId());

        Class<?> handlerClass = req.getClass();
        System.out.println(req.getMethodName());
        System.out.println(req.getClassName());
        String methodName = req.getMethodName();
        Class<?>[] parameterTypes = req.getParameterTypes();
        Object[] parameters = req.getParameters();

        // JDK reflect
        Method method = handlerClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(req.getClassName(), parameters);
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        System.out.println("exceptionCaught");
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }
}
