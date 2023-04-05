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

package org.apache.dolphinscheduler.service.cache;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.cache.CacheExpireRequest;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.service.cache.impl.CacheNotifyServiceImpl;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

/**
 * tenant cache proxy test
 */
@ExtendWith(MockitoExtension.class)
public class CacheNotifyServiceTest {

    @InjectMocks
    private CacheNotifyServiceImpl cacheNotifyService;

    @Mock
    private RegistryClient registryClient;

    @Test
    public void testNotifyMaster() {
        User user1 = new User();
        user1.setId(100);
        Message cacheExpireMessage = new CacheExpireRequest(CacheType.USER, "100").convert2Command();

        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(new NettyRequestProcessor() {

            @Override
            public void process(Channel channel, Message message) {
                Assertions.assertEquals(cacheExpireMessage, message);
            }

            @Override
            public MessageType getCommandType() {
                return MessageType.CACHE_EXPIRE;
            }
        });
        nettyRemotingServer.start();

        List<Server> serverList = new ArrayList<>();
        Server server = new Server();
        server.setHost("127.0.0.1");
        server.setPort(serverConfig.getListenPort());
        serverList.add(server);

        Mockito.when(registryClient.getServerList(RegistryNodeType.MASTER)).thenReturn(serverList);

        cacheNotifyService.notifyMaster(cacheExpireMessage);

        nettyRemotingServer.close();
    }
}
