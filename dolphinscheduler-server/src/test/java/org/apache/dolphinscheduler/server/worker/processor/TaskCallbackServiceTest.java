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

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * test task call back service
 * todo  refactor it in the form of mock
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class TaskCallbackServiceTest {

   /* @Autowired
    private TaskCallbackService taskCallbackService;

    @Autowired
    private MasterRegistry masterRegistry;

    @Autowired
    private TaskAckProcessor taskAckProcessor;

    @Autowired
    private TaskResponseProcessor taskResponseProcessor;

    @Autowired
    private TaskExecuteProcessor taskExecuteProcessor;

    *//**
     * send ack test
     *
     * @throws Exception
     *//*
    @Test
    public void testSendAck() throws Exception {
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, taskAckProcessor);
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(1);
        ackCommand.setStartTime(new Date());
        taskCallbackService.sendAck(1, ackCommand.convert2Command());

        TaskExecuteResponseCommand responseCommand = new TaskExecuteResponseCommand();
        taskCallbackService.sendResult(1, responseCommand.convert2Command());

        Stopper.stop();

        nettyRemotingServer.close();
        nettyRemotingClient.close();
    }

    *//**
     * send result test
     *
     * @throws Exception
     *//*
    @Test
    public void testSendResult() throws Exception {
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE, taskResponseProcessor);
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        TaskExecuteResponseCommand responseCommand = new TaskExecuteResponseCommand();
        responseCommand.setTaskInstanceId(1);
        responseCommand.setEndTime(new Date());

        taskCallbackService.sendResult(1, responseCommand.convert2Command());

        Thread.sleep(5000);

        Stopper.stop();

        Thread.sleep(5000);

        nettyRemotingServer.close();
        nettyRemotingClient.close();
    }

    @Test
    public void testPause() {
        Assert.assertEquals(5000, taskCallbackService.pause(3));
    }

    @Test
    public void testSendAck1() {
        masterRegistry.registry();
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, taskAckProcessor);
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);
        Channel channel = nettyRemotingClient.getChannel(Host.of("localhost:30000"));
        taskCallbackService.addRemoteChannel(1, new NettyRemoteChannel(channel, 1));
        //        channel.close();

        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(1);
        ackCommand.setStartTime(new Date());

        taskCallbackService.sendAck(1, ackCommand.convert2Command());

        Assert.assertTrue(channel.isOpen());

        Stopper.stop();

        nettyRemotingServer.close();
        nettyRemotingClient.close();
        masterRegistry.unRegistry();
    }

    @Test
    public void testTaskExecuteProcessor() throws Exception {
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_REQUEST, taskExecuteProcessor);
        nettyRemotingServer.start();

        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(clientConfig);

        TaskExecuteRequestCommand taskExecuteRequestCommand = new TaskExecuteRequestCommand();

        nettyRemotingClient.send(new Host("localhost", 30000), taskExecuteRequestCommand.convert2Command());

        taskExecuteRequestCommand.setTaskExecutionContext(JSONUtils.toJsonString(new TaskExecutionContext()));

        nettyRemotingClient.send(new Host("localhost", 30000), taskExecuteRequestCommand.convert2Command());

        Thread.sleep(5000);

        Stopper.stop();

        Thread.sleep(5000);

        nettyRemotingServer.close();
        nettyRemotingClient.close();
    }
*/
}
