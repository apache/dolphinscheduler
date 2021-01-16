package org.apache.dolphinscheduler.remote.rpc.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import org.apache.dolphinscheduler.remote.decoder.NettyDecoder;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;

import org.apache.dolphinscheduler.remote.decoder.NettyEncoder;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.remote.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.remote.rpc.future.RpcFuture;
import org.apache.dolphinscheduler.remote.serialize.ProtoStuffUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.NettyUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyClient
 */
public class NettyClient {

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * worker group
     */
    private final EventLoopGroup workerGroup;

    /**
     * client config
     */
    private final NettyClientConfig clientConfig;


    /**
     * client bootstrap
     */
    private final Bootstrap bootstrap = new Bootstrap();

    /**
     * started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap(128);

    /**
     * get channel
     */
    public Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * create channel
     *
     * @param host host
     * @param isSync sync flag
     * @return channel
     */
    public Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channels.put(host, channel);
                return channel;
            }
        } catch (Exception ex) {
            logger.warn(String.format("connect to %s error", host), ex);
        }
        return null;
    }

    /**
     * client init
     *
     * @param clientConfig client config
     */
    public NettyClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (NettyUtils.useEpoll()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        this.start();

    }

    /**
     * start
     */
    private void start() {

        this.bootstrap
            .group(this.workerGroup)
            .channel(NettyUtils.getSocketChannelClass())
            .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
            .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
            .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
            .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
            .handler(new LoggingHandler(LogLevel.DEBUG))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline()
                        .addLast(new NettyEncoder(RpcRequest.class))  //OUT - 1
                        .addLast(new NettyDecoder(RpcResponse.class))
                        .addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS))

                        .addLast(new NettyClientHandler());
                }
            });

        isStarted.compareAndSet(false, true);
        System.out.println("netty client start");
    }

    public Object sendMsg(Host host, RpcRequest request, Boolean async) {

        System.out.println("这个不是异步"+async);
        Channel channel = getChannel(host);
        assert channel != null;
        RpcRequestCache rpcRequestCache = new RpcRequestCache();
        rpcRequestCache.setServiceName(request.getClassName() + request.getMethodName());
        RpcFuture future = new RpcFuture();
        rpcRequestCache.setRpcFuture(future);
        RpcRequestTable.put(request.getRequestId(), rpcRequestCache);
        channel.writeAndFlush(request);

        Object result = null;
        if (async) {
            return true;
        }
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * close
     */
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    /**
     * close channels
     */
    private void closeChannels() {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }
}
