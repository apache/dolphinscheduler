package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;

/**
 * Invoker
 */
public interface Invoker {

    RpcResponse invoke(RpcRequest req) throws Throwable;
}
