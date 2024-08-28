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

import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The factory class for creating a dynamic proxy client.
 * <pre>
 *     final IService proxyClient = Clients
 *            .withService(IService.class)
 *            .withHost(serverAddress);
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Clients {

    private static final JdkDynamicRpcClientProxyFactory jdkDynamicRpcClientProxyFactory =
            new JdkDynamicRpcClientProxyFactory(
                    NettyRemotingClientFactory.buildNettyRemotingClient(
                            new NettyClientConfig()));

    public static <T> JdkDynamicRpcClientProxyBuilder<T> withService(Class<T> serviceClazz) {
        return new JdkDynamicRpcClientProxyBuilder<>(serviceClazz);
    }

    public static class JdkDynamicRpcClientProxyBuilder<T> {

        private final Class<T> serviceClazz;

        public JdkDynamicRpcClientProxyBuilder(Class<T> serviceClazz) {
            this.serviceClazz = serviceClazz;
        }

        public T withHost(String serviceHost) {
            return jdkDynamicRpcClientProxyFactory.getProxyClient(serviceHost, serviceClazz);
        }
    }

}
