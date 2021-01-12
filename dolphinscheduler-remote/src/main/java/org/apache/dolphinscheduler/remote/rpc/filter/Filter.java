package org.apache.dolphinscheduler.remote.rpc.filter;

import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;


import com.amazonaws.Response;


public interface Filter {


    RpcResponse filter(Invoker invoker, RpcRequest req) throws Throwable;
}
