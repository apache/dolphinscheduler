package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import org.apache.dolphinscheduler.remote.rpc.client.IRpcClient;
import org.apache.dolphinscheduler.remote.rpc.client.RpcClient;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyServer;
import org.apache.dolphinscheduler.remote.utils.Host;

/**
 * @author jiangli
 * @date 2021-01-11 21:06
 */
public class MainTest {

    public static void main(String[] args) throws Exception {
        NettyServer nettyServer = new NettyServer(new NettyServerConfig());

        // NettyClient nettyClient=new NettyClient(new NettyClientConfig());

        Host host = new Host("127.0.0.1", 12366);

        IRpcClient rpcClient = new RpcClient();
        UserService userService = rpcClient.create(UserService.class);
        String result = userService.say("calvin");
        System.out.println( "异步回掉成功"+result);

        // nettyClient.sendMsg(host,rpcRequest);

    }
}
