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

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

/**
 * The RpcServer based on Netty. The server will register the method invoker and provide the service to the client.
 * Once the server is started, it will listen on the port and wait for the client to connect.
 * <pre>
 *          RpcServer rpcServer = new RpcServer(new NettyServerConfig());
 *          rpcServer.registerServerMethodInvokerProvider(new ServerMethodInvokerProviderImpl());
 *          rpcServer.start();
 * </pre>
 */
@Slf4j
public class RpcServer implements ServerMethodInvokerRegistry, AutoCloseable {

    private final NettyRemotingServer nettyRemotingServer;

    public RpcServer(NettyServerConfig nettyServerConfig) {
        this.nettyRemotingServer = NettyRemotingServerFactory.buildNettyRemotingServer(nettyServerConfig);
    }

    public void start() {
        nettyRemotingServer.start();
    }

    @Override
    public void registerServerMethodInvokerProvider(Object serverMethodInvokerProviderBean) {
        for (Class<?> anInterface : serverMethodInvokerProviderBean.getClass().getInterfaces()) {
            if (anInterface.getAnnotation(RpcService.class) == null) {
                continue;
            }
            for (Method method : anInterface.getDeclaredMethods()) {
                RpcMethod rpcMethod = method.getAnnotation(RpcMethod.class);
                if (rpcMethod == null) {
                    continue;
                }
                ServerMethodInvoker serverMethodInvoker =
                        new ServerMethodInvokerImpl(serverMethodInvokerProviderBean, method);
                nettyRemotingServer.registerMethodInvoker(serverMethodInvoker);
                log.debug("Register ServerMethodInvoker: {} to bean: {}",
                        serverMethodInvoker.getMethodIdentify(), serverMethodInvoker.getMethodProviderIdentify());
            }
        }
    }

    @Override
    public void close() {
        nettyRemotingServer.close();
    }
}
