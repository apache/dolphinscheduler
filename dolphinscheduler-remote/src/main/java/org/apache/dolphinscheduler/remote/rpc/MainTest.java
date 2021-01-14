package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyClient;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyServer;
import org.apache.dolphinscheduler.remote.utils.Host;

/**
 * @author jiangli
 * @date 2021-01-11 21:06
 */
public class MainTest {

    public static void main(String[] args) throws Exception {
        NettyServer nettyServer=new NettyServer(new NettyServerConfig());

        NettyClient nettyClient=new NettyClient(new NettyClientConfig());

        Host host=new Host("127.0.0.1",12366);
        RpcRequest rpcRequest=new RpcRequest();
        rpcRequest.setRequestId("988");
        rpcRequest.setClassName("kris");
        rpcRequest.setMethodName("ll");


       nettyClient.sendMsg(host,rpcRequest);

    }
}
