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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.service.AlertBootstrapService;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertBootstrapServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertBootstrapServiceTest.class);

    @Mock
    private AlertDao alertDao;
    @Mock
    private PluginDao pluginDao;
    @Mock
    private AlertPluginManager alertPluginManager;
    @Mock
    private AlertConfig alertConfig;

    @InjectMocks
    private AlertBootstrapService alertBootstrapService;

    private static final String PLUGIN_INSTANCE_PARAMS =
            "{\"User\":\"xx\",\"receivers\":\"xx\",\"sender\":\"xx\",\"smtpSslTrust\":\"*\",\"enableSmtpAuth\":\"true\",\"receiverCcs\":null,\"showType\":\"table\",\"starttlsEnable\":\"false\",\"serverPort\":\"25\",\"serverHost\":\"xx\",\"Password\":\"xx\",\"sslEnable\":\"false\"}";

    private static final String PLUGIN_INSTANCE_NAME = "alert-instance-mail";
    private static final String TITLE = "alert mail test TITLE";
    private static final String CONTENT = "alert mail test CONTENT";
    private static final List<ListenerEvent> EVENTS = new ArrayList<>();

    private static final int PLUGIN_DEFINE_ID = 1;

    private static final int ALERT_GROUP_ID = 1;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSyncHandler() {
        // 1.alert instance does not exist
        when(alertDao.listInstanceByAlertGroupId(ALERT_GROUP_ID)).thenReturn(null);
        when(alertConfig.getWaitTimeout()).thenReturn(0);

        AlertSendResponse alertSendResponse =
                alertBootstrapService.syncHandler(ALERT_GROUP_ID, TITLE, CONTENT, WarningType.ALL.getCode());
        Assertions.assertFalse(alertSendResponse.isSuccess());
        alertSendResponse.getResResults().forEach(result -> logger
                .info("alert send response result, status:{}, message:{}", result.isSuccess(), result.getMessage()));

        // 2.alert plugin does not exist
        int pluginDefineId = 1;
        String pluginInstanceParams = "alert-instance-mail-params";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertPluginInstance.setId(1);
        alertInstanceList.add(alertPluginInstance);
        when(alertDao.listInstanceByAlertGroupId(1)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);

        alertSendResponse =
                alertBootstrapService.syncHandler(ALERT_GROUP_ID, TITLE, CONTENT, WarningType.ALL.getCode());
        Assertions.assertFalse(alertSendResponse.isSuccess());
        alertSendResponse.getResResults().forEach(result -> logger
                .info("alert send response result, status:{}, message:{}", result.isSuccess(), result.getMessage()));

        // 3.alert result value is null
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(null);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        when(alertConfig.getWaitTimeout()).thenReturn(0);

        alertSendResponse =
                alertBootstrapService.syncHandler(ALERT_GROUP_ID, TITLE, CONTENT, WarningType.ALL.getCode());
        Assertions.assertFalse(alertSendResponse.isSuccess());
        alertSendResponse.getResResults().forEach(result -> logger
                .info("alert send response result, status:{}, message:{}", result.isSuccess(), result.getMessage()));

        // 4.abnormal information inside the alert plug-in code
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(false));
        alertResult.setMessage("Abnormal information inside the alert plug-in code");
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));

        alertSendResponse =
                alertBootstrapService.syncHandler(ALERT_GROUP_ID, TITLE, CONTENT, WarningType.ALL.getCode());
        Assertions.assertFalse(alertSendResponse.isSuccess());
        alertSendResponse.getResResults().forEach(result -> logger
                .info("alert send response result, status:{}, message:{}", result.isSuccess(), result.getMessage()));

        // 5.alert plugin send success
        alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        when(alertConfig.getWaitTimeout()).thenReturn(5000);

        alertSendResponse =
                alertBootstrapService.syncHandler(ALERT_GROUP_ID, TITLE, CONTENT, WarningType.ALL.getCode());
        Assertions.assertTrue(alertSendResponse.isSuccess());
        alertSendResponse.getResResults().forEach(result -> logger
                .info("alert send response result, status:{}, message:{}", result.isSuccess(), result.getMessage()));

    }

    @Test
    public void testRun() {
        List<Alert> alertList = new ArrayList<>();
        Alert alert = new Alert();
        alert.setId(1);
        alert.setAlertGroupId(ALERT_GROUP_ID);
        alert.setTitle(TITLE);
        alert.setContent(CONTENT);
        alert.setWarningType(WarningType.FAILURE);
        alertList.add(alert);

        // alertSenderService = new AlertSenderService();

        int pluginDefineId = 1;
        String pluginInstanceParams = "alert-instance-mail-params";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertInstanceList.add(alertPluginInstance);
        when(alertDao.listInstanceByAlertGroupId(ALERT_GROUP_ID)).thenReturn(alertInstanceList);

        String pluginName = "alert-plugin-mail";
        PluginDefine pluginDefine = new PluginDefine(pluginName, "1", null);
        when(pluginDao.getPluginDefineById(pluginDefineId)).thenReturn(pluginDefine);

        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(String.valueOf(true));
        alertResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(alertResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assertions.assertTrue(Boolean.parseBoolean(alertResult.getStatus()));
        when(alertDao.listInstanceByAlertGroupId(1)).thenReturn(new ArrayList<>());
        alertBootstrapService.send(alertList);
    }

    @Test
    public void testSendAlert() {
        AlertResult sendResult = new AlertResult();
        sendResult.setStatus(String.valueOf(true));
        sendResult.setMessage(String.format("Alert Plugin %s send success", PLUGIN_INSTANCE_NAME));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(sendResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Map<String, String> paramsMap = JSONUtils.toMap(PLUGIN_INSTANCE_PARAMS);
        MockedStatic<PluginParamsTransfer> pluginParamsTransferMockedStatic =
                Mockito.mockStatic(PluginParamsTransfer.class);
        pluginParamsTransferMockedStatic.when(() -> PluginParamsTransfer.getPluginParamsMap(PLUGIN_INSTANCE_PARAMS))
                .thenReturn(paramsMap);
        alertBootstrapService.syncTestSend(PLUGIN_DEFINE_ID, PLUGIN_INSTANCE_PARAMS);
    }
}
