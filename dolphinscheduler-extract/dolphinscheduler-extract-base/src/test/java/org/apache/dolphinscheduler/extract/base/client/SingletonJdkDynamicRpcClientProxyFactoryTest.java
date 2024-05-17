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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.exception.MethodInvocationException;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SingletonJdkDynamicRpcClientProxyFactoryTest {

    private SpringServerMethodInvokerDiscovery springServerMethodInvokerDiscovery;

    private String serverAddress;

    @BeforeEach
    public void setUp() {
        int listenPort = RandomUtils.nextInt(10000, 20000);
        NettyServerConfig nettyServerConfig = NettyServerConfig.builder()
                .serverName("ApiServer")
                .listenPort(listenPort)
                .build();
        serverAddress = "localhost:" + listenPort;
        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(nettyServerConfig);
        springServerMethodInvokerDiscovery.registerServerMethodInvokerProvider(new IServiceImpl());
        springServerMethodInvokerDiscovery.start();
    }

    @Test
    public void getProxyClient() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(serverAddress, IService.class);
        Assertions.assertNotNull(proxyClient);
    }

    @Test
    public void testPing() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(serverAddress, IService.class);
        assertEquals("pong", proxyClient.ping("ping"));

        MethodInvocationException methodInvocationException =
                Assertions.assertThrows(MethodInvocationException.class, () -> proxyClient.ping(null));
        assertEquals("ping: null is illegal", methodInvocationException.getMessage());
    }

    @Test
    public void testVoid() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(serverAddress, IService.class);
        assertDoesNotThrow(proxyClient::voidMethod);
    }

    @AfterEach
    public void tearDown() {
        springServerMethodInvokerDiscovery.close();
    }

    @RpcService
    public interface IService {

        @RpcMethod
        String ping(String ping);

        @RpcMethod
        void voidMethod();
    }

    public static class IServiceImpl implements IService {

        @Override
        public String ping(String ping) {
            if (StringUtils.isEmpty(ping)) {
                throw new IllegalArgumentException("ping: " + ping + " is illegal");
            }
            return "pong";
        }

        @Override
        public void voidMethod() {
            System.out.println("void method");
        }
    }

}
