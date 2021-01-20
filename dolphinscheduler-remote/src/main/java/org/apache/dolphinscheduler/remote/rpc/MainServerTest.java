package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyServer;

/**
 * @author jiangli
 * @date 2021-01-20 14:54
 */
public class MainServerTest {

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer(new NettyServerConfig());
    }
}
