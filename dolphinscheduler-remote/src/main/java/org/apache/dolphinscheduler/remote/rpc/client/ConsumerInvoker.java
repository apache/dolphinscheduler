package org.apache.dolphinscheduler.remote.rpc.client;

import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

/**
 * @author jiangli
 * @date 2021-01-09 15:27
 */
public class ConsumerInvoker implements Invoker {
    @Override
    public RpcResponse invoke(RpcRequest req) throws Throwable {

        System.out.println(req.getRequestId()+"kris");
        return null;
    }
}
