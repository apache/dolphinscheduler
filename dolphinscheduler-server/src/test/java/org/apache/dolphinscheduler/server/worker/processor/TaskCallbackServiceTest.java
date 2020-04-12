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
package org.apache.dolphinscheduler.server.worker.processor;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistry;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * test task call back service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TaskCallbackServiceTestConfig.class, SpringZKServer.class, MasterRegistry.class, WorkerRegistry.class,
        ZookeeperRegistryCenter.class, MasterConfig.class, WorkerConfig.class,
        ZookeeperCachedOperator.class, ZookeeperConfig.class, ZookeeperNodeManager.class, TaskCallbackService.class})
public class TaskCallbackServiceTest {

    @Autowired
    private TaskCallbackService taskCallbackService;

    @Autowired
    private MasterRegistry masterRegistry;

    @Test
    public void testSendAck(){
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, Mockito.mock(TaskAckProcessor.class));
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(1);
        ackCommand.setStartTime(new Date());
        taskCallbackService.sendAck(1, ackCommand.convert2Command());

        nettyRemotingServer.close();
        nettyRemotingClient.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAckWithIllegalArgumentException(){
        TaskExecuteAckCommand ackCommand = Mockito.mock(TaskExecuteAckCommand.class);
        taskCallbackService.sendAck(1, ackCommand.convert2Command());
    }

    @Test(expected = IllegalStateException.class)
    public void testSendAckWithIllegalStateException1(){
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, Mockito.mock(TaskAckProcessor.class));
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        channel.close();
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(1);
        ackCommand.setStartTime(new Date());

        nettyRemotingServer.close();
        taskCallbackService.sendAck(1, ackCommand.convert2Command());
    }

    @Test(expected = IllegalStateException.class)
    public void testSendAckWithIllegalStateException2(){
        masterRegistry.registry();
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, Mockito.mock(TaskAckProcessor.class));
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        channel.close();
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(1);
        ackCommand.setStartTime(new Date());

        nettyRemotingServer.close();
        taskCallbackService.sendAck(1, ackCommand.convert2Command());
    }
}
