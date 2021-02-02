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

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * alert sender test
 */
public class AlertSenderTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertSenderTest.class);

    private AlertDao alertDao;
    private PluginDao pluginDao;
    private AlertPluginManager alertPluginManager;

    private AlertSender alertSender;

    @Before
    public void before() {
        alertDao = PowerMockito.mock(AlertDao.class);
        pluginDao = PowerMockito.mock(PluginDao.class);
        alertPluginManager = PowerMockito.mock(AlertPluginManager.class);

    }

    @Test
    public void testSyncHandler() {

        int alertGroupId = 1;
        String title = "alert mail test title";
        String content = "alert mail test content";
        alertSender = new AlertSender(alertDao, alertPluginManager);

        //1.alert instance does not exist
        PowerMockito.when(alertDao.listInstanceByAlertGroupId(alertGroupId)).thenReturn(null);

        AlertSendResponseCommand alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content);
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
        PowerMockito.when(alertDao.listInstanceByAlertGroupId(1)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        PowerMockito.when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //3.alert result value is null
        AlertChannel alertChannelMock = PowerMockito.mock(AlertChannel.class);
        PowerMockito.when(alertChannelMock.process(Mockito.any())).thenReturn(null);
        Map<String, AlertChannel> alertChannelMap = new ConcurrentHashMap<>();
        alertChannelMap.put(pluginName, alertChannelMock);
        PowerMockito.when(alertPluginManager.getAlertChannelMap()).thenReturn(alertChannelMap);
        PowerMockito.when(alertPluginManager.getPluginNameById(Mockito.anyInt())).thenReturn("alert-plugin-mail");

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //4.abnormal information inside the alert plug-in code
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(false));
        alertResult.setMessage("Abnormal information inside the alert plug-in code");
        PowerMockito.when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        alertChannelMap = new ConcurrentHashMap<>();
        alertChannelMap.put(pluginName, alertChannelMock);
        PowerMockito.when(alertPluginManager.getAlertChannelMap()).thenReturn(alertChannelMap);

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content);
        Assert.assertFalse(alertSendResponseCommand.getResStatus());
        alertSendResponseCommand.getResResults().forEach(result ->
                logger.info("alert send response result, status:{}, message:{}", result.getStatus(), result.getMessage()));

        //5.alert plugin send success
        alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        PowerMockito.when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        alertChannelMap = new ConcurrentHashMap<>();
        alertChannelMap.put(pluginName, alertChannelMock);
        PowerMockito.when(alertPluginManager.getAlertChannelMap()).thenReturn(alertChannelMap);

        alertSendResponseCommand = alertSender.syncHandler(alertGroupId, title, content);
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
        alertList.add(alert);

        alertSender = new AlertSender(alertList, alertDao, alertPluginManager);

        int pluginDefineId = 1;
        String pluginInstanceParams = "alert-instance-mail-params";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertInstanceList.add(alertPluginInstance);
        PowerMockito.when(alertDao.listInstanceByAlertGroupId(alertGroupId)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        PowerMockito.when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);
        PowerMockito.when(alertPluginManager.getPluginNameById(1)).thenReturn("alert-instance-mail");

        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        AlertChannel alertChannelMock = PowerMockito.mock(AlertChannel.class);
        PowerMockito.when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        ConcurrentHashMap alertChannelMap = new ConcurrentHashMap<>();
        alertChannelMap.put(pluginName, alertChannelMock);
        PowerMockito.when(alertPluginManager.getAlertChannelMap()).thenReturn(alertChannelMap);
        Assert.assertTrue(Boolean.parseBoolean(alertResult.getStatus()));
        alertSender.run();

    }

}
