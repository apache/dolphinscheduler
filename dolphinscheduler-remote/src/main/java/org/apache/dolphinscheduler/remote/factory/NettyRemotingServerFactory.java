package org.apache.dolphinscheduler.remote.factory;

import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NettyRemotingServerFactory {

    public NettyRemotingServer buildNettyRemotingServer(int listenPort) {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(listenPort);
        return new NettyRemotingServer(serverConfig);
    }
}
