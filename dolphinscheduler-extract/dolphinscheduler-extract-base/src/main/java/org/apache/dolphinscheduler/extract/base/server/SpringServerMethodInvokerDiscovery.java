/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.extract.base.server;

import org.apache.dolphinscheduler.extract.base.NettyRemotingServer;
import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

@Slf4j
public class SpringServerMethodInvokerDiscovery implements BeanPostProcessor {

    protected final NettyRemotingServer nettyRemotingServer;

    public SpringServerMethodInvokerDiscovery(NettyRemotingServer nettyRemotingServer) {
        this.nettyRemotingServer = nettyRemotingServer;
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?>[] interfaces = bean.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (anInterface.getAnnotation(RpcService.class) == null) {
                continue;
            }
            registerRpcMethodInvoker(anInterface, bean, beanName);
        }
        return bean;
    }

    private void registerRpcMethodInvoker(Class<?> anInterface, Object bean, String beanName) {
        Method[] declaredMethods = anInterface.getDeclaredMethods();
        for (Method method : declaredMethods) {
            RpcMethod rpcMethod = method.getAnnotation(RpcMethod.class);
            if (rpcMethod == null) {
                continue;
            }
            ServerMethodInvoker methodInvoker = new ServerMethodInvokerImpl(bean, method);
            nettyRemotingServer.registerMethodInvoker(methodInvoker);
            log.info("Register ServerMethodInvoker: {} to bean: {}", methodInvoker.getMethodIdentify(), beanName);
        }
    }
}
