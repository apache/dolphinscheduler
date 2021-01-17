package org.apache.dolphinscheduler.remote.rpc.client;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

import org.apache.dolphinscheduler.remote.utils.Host;

import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * RpcClient
 */
public class RpcClient implements IRpcClient{

    private ConcurrentHashMap<String,Object> classMap=new ConcurrentHashMap<>();

    @Override
    public <T> T create(Class<T> clazz,Host host) throws Exception {
       // if(!classMap.containsKey(clazz.getName())){
            T proxy = new ByteBuddy()
                .subclass(clazz)
                .method(isDeclaredBy(clazz)).intercept(MethodDelegation.to(new ConsumerInterceptor(new ConsumerInvoker(),host)))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded()
                .getDeclaredConstructor().newInstance();

           // classMap.putIfAbsent(clazz.getName(),proxy);
            return proxy;
      //  }
      //  return (T) classMap.get(clazz.getName());
    }
}
