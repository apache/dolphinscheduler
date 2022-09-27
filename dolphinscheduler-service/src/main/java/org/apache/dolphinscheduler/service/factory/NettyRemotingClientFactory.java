package org.apache.dolphinscheduler.service.factory;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;

import lombok.experimental.UtilityClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class NettyRemotingClientFactory {

    private final Logger logger = LoggerFactory.getLogger(NettyRemotingClientFactory.class);

    public NettyRemotingClient buildNettyRemotingClient() {
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        logger.info("NettyRemotingClient initialized with config: {}", nettyClientConfig);
        return new NettyRemotingClient(nettyClientConfig);
    }
}
