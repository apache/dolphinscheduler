package org.apache.dolphinscheduler.remote.rpc.client;

import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.base.Rpc;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.remote.rpc.filter.FilterChain;
import org.apache.dolphinscheduler.remote.rpc.remote.NettyClient;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.lang.reflect.Method;
import java.util.UUID;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * ConsumerInterceptor
 */
public class ConsumerInterceptor {

    private Invoker invoker;


    private FilterChain filterChain;

    private Host host;

    private NettyClient nettyClient = NettyClient.getInstance();

    public ConsumerInterceptor(Invoker invoker, Host host) {
        this.filterChain = new FilterChain(invoker);
        this.invoker = this.filterChain.buildFilterChain();
        this.host = host;
    }


    @RuntimeType
    public Object intercept(@AllArguments Object[] args, @Origin Method method) throws RemotingException {
        RpcRequest request = buildReq(args, method);


        String serviceName = method.getDeclaringClass().getName() + method.getName();
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (null == consumerConfig) {
            consumerConfig = cacheServiceConfig(method, serviceName);
        }
        boolean async = consumerConfig.getAsync();

        int retries = consumerConfig.getRetries();

        while (retries-- > 0) {
            RpcResponse rsp = (RpcResponse) nettyClient.sendMsg(host, request, async);
            //success
            if (null != rsp && rsp.getStatus() == 0) {
                return rsp.getResult();
            }
        }
        // execute fail
        throw new RemotingException("send msg error");

    }

    private RpcRequest buildReq(Object[] args, Method method) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());

        request.setParameters(args);

        String serviceName = method.getDeclaringClass().getName();

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
        }

        ConsumerConfigCache.putConfig(serviceName, consumerConfig);

        return consumerConfig;
    }

}
