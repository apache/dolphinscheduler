package org.apache.dolphinscheduler.remote.rpc.remote;

import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

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

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }


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

        RpcResponse response = new RpcResponse();
        if (req.getEventType() == 0) {

            logger.info("接受心跳消息!...");
            return;
        }
        response.setRequestId(req.getRequestId());

        response.setStatus((byte) 0);


        String classname = req.getClassName();

        String methodName = req.getMethodName();

        Class<?>[] parameterTypes = req.getParameterTypes();

        Object[] arguments = req.getParameters();
        Object result = null;
        try {

            Class serviceClass = Class.forName(classname);

            Object object = serviceClass.newInstance();

            Method method = serviceClass.getMethod(methodName, parameterTypes);

            result = method.invoke(object, arguments);
        } catch (Exception e) {
            logger.error("netty server execute error", e);
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
        System.out.println("exceptionCaught");
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }
}
