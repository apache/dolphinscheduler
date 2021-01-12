package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.client.IRpcClient;
import org.apache.dolphinscheduler.remote.rpc.client.RpcClient;

/**
 * @author jiangli
 * @date 2021-01-11 21:06
 */
public class MainTest {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient();
        IUserService userService = rpcClient.create(IUserService.class);
        for (int i = 0; i < 100; i++) {
            userService.say();
        }

    }
}
