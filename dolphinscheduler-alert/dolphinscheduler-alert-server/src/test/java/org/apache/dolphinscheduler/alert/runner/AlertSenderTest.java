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

package org.apache.dolphinscheduler.alert.runner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.alert.AlertPluginManager;
import org.apache.dolphinscheduler.alert.AlertSender;
import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertSenderTest {
    private static final Logger logger = LoggerFactory.getLogger(AlertSenderTest.class);

    private AlertDao alertDao;
    private PluginDao pluginDao;
    private AlertPluginManager alertPluginManager;

    private AlertSender alertSender;

    @Before
    public void before() {
        alertDao = mock(AlertDao.class);
        pluginDao = mock(PluginDao.class);
        alertPluginManager = mock(AlertPluginManager.class);
    }

    @Test
    public void testSyncHandler() {

        int alertGroupId = 1;
        String title = "alert mail test title";
        String content = "alert mail test content";
        alertSender = new AlertSender(alertDao, alertPluginManager);

        //1.alert instance does not exist
        when(alertDao.listInstanceByAlertGroupId(alertGroupId)).thenReturn(null);

        AlertSendResponseCommand alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content, WarningType.ALL.getCode());
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
            logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //2.alert plugin does not exist
        int pluginDefineId = 1;
        String pluginInstanceParams = "alert-instance-mail-params";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
            pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertInstanceList.add(alertPluginInstance);
        when(alertDao.listInstanceByAlertGroupId(1)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content, WarningType.ALL.getCode());
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
            logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //3.alert result value is null
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(null);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content, WarningType.ALL.getCode());
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
            logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //4.abnormal information inside the alert plug-in code
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(false));
        alertResult.setMessage("Abnormal information inside the alert plug-in code");
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content, WarningType.ALL.getCode());
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
            logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //5.alert plugin send success
        alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content, WarningType.ALL.getCode());
        Assert.assertTrue(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
            logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

    }

    @Test
    public void testRun() {
        int alertGroupId = 1;
        String title = "alert mail test title";
        String content = "alert mail test content";
        List<Alert> alertList = new ArrayList<>();
        Alert alert = new Alert();
        alert.setAlertGroupId(alertGroupId);
        alert.setTitle(title);
        alert.setContent(content);
        alert.setWarningType(WarningType.FAILURE);
        alertList.add(alert);

        alertSender = new AlertSender(alertDao, alertPluginManager);

        int pluginDefineId = 1;
        String pluginInstanceParams = "alert-instance-mail-params";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
            pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertInstanceList.add(alertPluginInstance);
        when(alertDao.listInstanceByAlertGroupId(alertGroupId)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);

        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assert.assertTrue(Boolean.parseBoolean(alertResult.getStatus()));
        alertSender.send(alertList);
    }
}
