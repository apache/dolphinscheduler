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

import org.apache.dolphinscheduler.extract.base.NettyRemotingServer;
import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SingletonJdkDynamicRpcClientProxyFactoryTest {

    private NettyRemotingServer nettyRemotingServer;

    @BeforeEach
    public void setUp() {
        nettyRemotingServer = new NettyRemotingServer(new NettyServerConfig(12345));
        nettyRemotingServer.start();

        new SpringServerMethodInvokerDiscovery(nettyRemotingServer)
                .postProcessAfterInitialization(new IServiceImpl(), "iServiceImpl");
    }

    @Test
    public void getProxyClient() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient("localhost:12345", IService.class);
        Assertions.assertNotNull(proxyClient);
    }

    @Test
    public void testPing() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient("localhost:12345", IService.class);
        String ping = proxyClient.ping("ping");
        Assertions.assertEquals("pong", ping);
    }

    @Test
    public void testVoid() {
        IService proxyClient =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient("localhost:12345", IService.class);
        Assertions.assertDoesNotThrow(proxyClient::voidMethod);
    }

    @AfterEach
    public void tearDown() {
        nettyRemotingServer.close();
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
            return "pong";
        }

        @Override
        public void voidMethod() {
            System.out.println("void method");
        }
    }

}
