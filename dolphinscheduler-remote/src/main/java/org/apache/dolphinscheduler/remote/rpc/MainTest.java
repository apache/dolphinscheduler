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


       // NettyServer nettyServer = new NettyServer(new NettyServerConfig());

        // NettyClient nettyClient=new NettyClient(new NettyClientConfig());

        Host host = new Host("127.0.0.1", 12636);

        IRpcClient rpcClient = new RpcClient();
        IUserService userService = rpcClient.create(IUserService.class, host);
        boolean result = userService.say("calvin");
        System.out.println("异步回掉成功" + result);

        System.out.println(userService.hi(10));
        System.out.println(userService.hi(188888888));

        IUserService user = rpcClient.create(IUserService.class, host);
        System.out.println(user.hi(99999));
        System.out.println(user.hi(998888888));

        System.out.println(IUserService.class.getSimpleName());
        System.out.println(UserService.class.getSimpleName());
    }
}
