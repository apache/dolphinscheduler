package org.apache.dolphinscheduler.remote.rpc.client;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.filter.FilterChain;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyClient;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * ConsumerInterceptor
 */
public class ConsumerInterceptor {

    private Invoker invoker;


    private FilterChain filterChain;

    public ConsumerInterceptor(Invoker invoker) {
        this.filterChain = new FilterChain(invoker);
        this.invoker = this.filterChain.buildFilterChain();
    }


    @RuntimeType
    public Object intercept(@AllArguments Object[] args, @Origin Method method) throws Throwable {
        RpcRequest request = buildReq(args, method);
        //todo
        System.out.println(invoker.invoke(request));
        NettyClient nettyClient = new NettyClient(new NettyClientConfig());
        Host host = new Host("127.0.0.1", 12366);
        nettyClient.sendMsg(host, request);
        return null;

    }

    private RpcRequest buildReq(Object[] args, Method method) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        return request;
    }

}
