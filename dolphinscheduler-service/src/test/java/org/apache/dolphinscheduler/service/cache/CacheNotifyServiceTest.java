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
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.service.cache.config.CacheConfig;
import org.apache.dolphinscheduler.service.cache.processor.CacheNotifyService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * tenant cache proxy test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CacheNotifyServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private CacheNotifyService cacheNotifyService;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private CacheConfig cacheConfig;

    @Test
    public void testNotifyMaster() {
        User user1 = new User();
        user1.setId(100);
        Command cacheExpireCommand = new CacheExpireCommand(CacheType.USER, user1).convert2Command();

        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.CACHE_EXPIRE, (channel, command) -> {
            Assert.assertEquals(cacheExpireCommand, command);
        });
        nettyRemotingServer.start();

        List<Server> serverList = new ArrayList<>();
        Server server = new Server();
        server.setHost("127.0.0.1");
        server.setPort(serverConfig.getListenPort());
        serverList.add(server);

        Mockito.when(registryClient.getServerList(NodeType.MASTER)).thenReturn(serverList);
        Mockito.when(cacheConfig.isCacheEnable()).thenReturn(true);

        cacheNotifyService.notifyMaster(cacheExpireCommand);

        nettyRemotingServer.close();
    }
}
