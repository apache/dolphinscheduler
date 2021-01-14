package org.apache.dolphinscheduler.remote.rpc.remote;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;

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
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        logger.info("server read msg");
        System.out.println("收到消息");
        RpcRequest req= (RpcRequest) msg;
        System.out.println(req.getRequestId());
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
