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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ClientInvocationHandler implements InvocationHandler {

    private final NettyRemotingClient nettyRemotingClient;

    private final Map<String, ClientMethodInvoker> methodInvokerMap;

    private final Host serverHost;

    ClientInvocationHandler(Host serverHost, NettyRemotingClient nettyRemotingClient) {
        this.serverHost = checkNotNull(serverHost);
        this.nettyRemotingClient = checkNotNull(nettyRemotingClient);
        this.methodInvokerMap = new ConcurrentHashMap<>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(RpcMethod.class) == null) {
            return method.invoke(proxy, args);
        }
        ClientMethodInvoker methodInvoker = methodInvokerMap.computeIfAbsent(
                method.toGenericString(), m -> new SyncClientMethodInvoker(serverHost, method, nettyRemotingClient));
        return methodInvoker.invoke(proxy, method, args);
    }

}
