package org.apache.dolphinscheduler.remote.rpc.remote;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.dolphinscheduler.remote.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.remote.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.remote.rpc.future.RpcFuture;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyClientHandler
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {


    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rsp = (RpcResponse) msg;
        RpcRequestCache rpcRequest = RpcRequestTable.get(rsp.getRequestId());
        if (null != rpcRequest) {
            RpcFuture future = rpcRequest.getRpcFuture();
            RpcRequestTable.remove(rsp.getRequestId());
            future.done(rsp);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            RpcRequest request = new RpcRequest();
            request.setMethodName("heart");
            ctx.channel().writeAndFlush(request);
            logger.info("已超过30秒未与RPC服务器进行读写操作!将发送心跳消息...");

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
