package org.apache.dolphinscheduler.remote.rpc.remote;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.FastThreadLocalThread;

import org.apache.dolphinscheduler.remote.rpc.client.ConsumerConfig;
import org.apache.dolphinscheduler.remote.rpc.client.ConsumerConfigCache;
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

        if (null == rpcRequest) {
            logger.warn("未知响应");
            return;
        }

        String serviceName = rpcRequest.getServiceName();
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (!consumerConfig.getAsync()) {
            RpcFuture future = rpcRequest.getRpcFuture();
            RpcRequestTable.remove(rsp.getRequestId());
            future.done(rsp);
            return;

        }

        //async
        new FastThreadLocalThread(() -> {
            try {
                if (rsp.getStatus() == 0) {
                    consumerConfig.getCallBackClass().newInstance().run(rsp.getResult());
                } else {
                    logger.error("xxxx fail");
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("execute async error", e);
            }
        }).start();


    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            RpcRequest request = new RpcRequest();
            request.setMethodName("heart");
            ctx.channel().writeAndFlush(request);
            logger.debug("send heart beat msg...");

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
