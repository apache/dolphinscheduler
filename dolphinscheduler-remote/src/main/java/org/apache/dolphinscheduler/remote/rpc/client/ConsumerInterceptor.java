package org.apache.dolphinscheduler.remote.rpc.client;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.base.Rpc;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.filter.FilterChain;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyClient;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * ConsumerInterceptor
 */
public class ConsumerInterceptor {

    private Invoker invoker;


    private FilterChain filterChain;

    private NettyClient nettyClient=new NettyClient(new NettyClientConfig());

    public ConsumerInterceptor(Invoker invoker) {
        this.filterChain = new FilterChain(invoker);
        this.invoker = this.filterChain.buildFilterChain();
    }


    @RuntimeType
    public Object intercept(@AllArguments Object[] args, @Origin Method method) throws Throwable {
        RpcRequest request = buildReq(args, method);


        String serviceName = method.getDeclaringClass().getName() + method;
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (null == consumerConfig) {
            consumerConfig = cacheServiceConfig(method, serviceName);
        }
        boolean async = consumerConfig.getAsync();

        //load balance
        Host host = new Host("127.0.0.1", 12336);

        return nettyClient.sendMsg(host, request, async);
    }

    private RpcRequest buildReq(Object[] args, Method method) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());

        request.setParameters(args);

        String serviceName = method.getDeclaringClass().getName() + method;

        return request;
    }

    private ConsumerConfig cacheServiceConfig(Method method, String serviceName) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setServiceName(serviceName);
        boolean annotationPresent = method.isAnnotationPresent(Rpc.class);
        if (annotationPresent) {
            Rpc rpc = method.getAnnotation(Rpc.class);
            consumerConfig.setAsync(rpc.async());
            consumerConfig.setCallBackClass(rpc.callback());
            consumerConfig.setRetries(rpc.retries());
            consumerConfig.setOneway(rpc.isOneway());
        }
        ConsumerConfigCache.putConfig(serviceName, consumerConfig);

        return consumerConfig;
    }

}
