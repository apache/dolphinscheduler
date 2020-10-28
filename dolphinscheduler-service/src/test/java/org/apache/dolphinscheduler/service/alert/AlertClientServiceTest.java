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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * alert client service test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AlertClientService.class})
public class AlertClientServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertClientServiceTest.class);

    private NettyRemotingClient client;

    private AlertClientService alertClient;

    @Before
    public void before() throws Exception {
        client = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(client);
        alertClient = new AlertClientService();
    }

    @Test
    public void testSendAlert() throws Exception {
        String host = "127.0.0.1";
        int port = 50501;
        int groupId = 1;
        String title = "test-title";
        String content = "test-content";

        //1.alter server does not exist
        AlertSendResponseCommand alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertNull(alertSendResponseCommand);

        AlertSendRequestCommand alertSendRequestCommand = new AlertSendRequestCommand(groupId,title,content);
        Command reqCommand = alertSendRequestCommand.convert2Command();
        boolean sendResponseStatus;
        List<AlertSendResponseResult> sendResponseResults = new ArrayList<>();

        //2.alter instance does not exist
        sendResponseStatus = false;
        AlertSendResponseResult alertResult = new AlertSendResponseResult();
        String message = String.format("Alert GroupId %s send error : not found alert instance",groupId);
        alertResult.setStatus(false);
        alertResult.setMessage(message);
        sendResponseResults.add(alertResult);
        AlertSendResponseCommand alertSendResponseCommandData = new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        Command resCommand = alertSendResponseCommandData.convert2Command(reqCommand.getOpaque());

        PowerMockito.when(client.sendSync(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(resCommand);
        alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}",result.getStatus(),result.getMessage()));

        //3.alter plugin does not exist
        sendResponseStatus = false;
        String pluginInstanceName = "alert-mail";
        message = String.format("Alert Plugin %s send error : return value is null",pluginInstanceName);
        alertResult.setStatus(false);
        alertResult.setMessage(message);
        alertSendResponseCommandData = new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        resCommand = alertSendResponseCommandData.convert2Command(reqCommand.getOpaque());
        PowerMockito.when(client.sendSync(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(resCommand);
        alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}",result.getStatus(),result.getMessage()));

        //4.alter result is null
        sendResponseStatus = false;
        message = String.format("Alert Plugin %s send error : return result value is null",pluginInstanceName);
        alertResult.setStatus(false);
        alertResult.setMessage(message);
        alertSendResponseCommandData = new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        resCommand = alertSendResponseCommandData.convert2Command(reqCommand.getOpaque());
        PowerMockito.when(client.sendSync(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(resCommand);
        alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}",result.getStatus(),result.getMessage()));

        //5.abnormal information inside the alert plug-in code
        sendResponseStatus = false;
        alertResult.setStatus(false);
        alertResult.setMessage("Abnormal information inside the alert plug-in code");
        alertSendResponseCommandData = new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        resCommand = alertSendResponseCommandData.convert2Command(reqCommand.getOpaque());
        PowerMockito.when(client.sendSync(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(resCommand);
        alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}",result.getStatus(),result.getMessage()));

        //6.alert plugin send success
        sendResponseStatus = true;
        message = String.format("Alert Plugin %s send success",pluginInstanceName);
        alertResult.setStatus(true);
        alertResult.setMessage(message);
        alertSendResponseCommandData = new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        resCommand = alertSendResponseCommandData.convert2Command(reqCommand.getOpaque());
        PowerMockito.when(client.sendSync(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(resCommand);
        alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertTrue(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}",result.getStatus(),result.getMessage()));

        if (Objects.nonNull(alertClient) && alertClient.isRunning()) {
            alertClient.close();
        }

    }

}
