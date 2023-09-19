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

package org.apache.dolphinscheduler.extract.base.client;

import org.apache.dolphinscheduler.extract.base.NettyRemotingClient;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to create a proxy client which will transform local method invocation to remove invocation.
 */
public class JdkDynamicRpcClientProxyFactory implements IRpcClientProxyFactory {

    private final NettyRemotingClient nettyRemotingClient;

    // todo: use guava cache to avoid memory leak
    private final Map<String, Map<String, Object>> proxyClientCache = new ConcurrentHashMap<>();

    public JdkDynamicRpcClientProxyFactory(NettyRemotingClient nettyRemotingClient) {
        this.nettyRemotingClient = nettyRemotingClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxyClient(String serverHost, Class<T> clientInterface) {
        return (T) proxyClientCache
                .computeIfAbsent(serverHost, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(clientInterface.getName(),
                        key -> Proxy.newProxyInstance(
                                clientInterface.getClassLoader(), new Class[]{clientInterface},
                                new ClientInvocationHandler(Host.of(serverHost), nettyRemotingClient)));
    }
}
