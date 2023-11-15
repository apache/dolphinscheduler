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
import org.apache.dolphinscheduler.alert.service.ListenerEventPostService;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ServerDownListenerEvent;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerEventPostServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ListenerEventPostServiceTest.class);

    @Mock
    private ListenerEventMapper listenerEventMapper;
    @Mock
    private AlertPluginInstanceMapper alertPluginInstanceMapper;
    @Mock
    private AlertPluginManager alertPluginManager;
    @Mock
    private AlertConfig alertConfig;

    @InjectMocks
    private ListenerEventPostService listenerEventPostService;

    private static final List<AlertPluginInstance> ALERT_INSTANCE_LIST = new ArrayList<>();

    private static final String PLUGIN_INSTANCE_PARAMS =
            "{\"User\":\"xx\",\"receivers\":\"xx\",\"sender\":\"xx\",\"smtpSslTrust\":\"*\",\"enableSmtpAuth\":\"true\",\"receiverCcs\":null,\"showType\":\"table\",\"starttlsEnable\":\"false\",\"serverPort\":\"25\",\"serverHost\":\"xx\",\"Password\":\"xx\",\"sslEnable\":\"false\"}";

    private static final String PLUGIN_INSTANCE_NAME = "alert-instance-mail";
    private static final List<ListenerEvent> EVENTS = new ArrayList<>();

    private static final int PLUGIN_DEFINE_ID = 1;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeAll
    public static void initConfig() {
        ServerDownListenerEvent serverDownListenerEvent = new ServerDownListenerEvent();
        serverDownListenerEvent.setEventTime(new Date());
        serverDownListenerEvent.setType("WORKER");
        serverDownListenerEvent.setHost("192.168.*.*");
        ListenerEvent successEvent = new ListenerEvent();
        successEvent.setId(1);
        successEvent.setPostStatus(AlertStatus.WAIT_EXECUTION);
        successEvent.setContent(JSONUtils.toJsonString(serverDownListenerEvent));
        successEvent.setSign(DigestUtils.sha256Hex(successEvent.getContent()));
        successEvent.setEventType(ListenerEventType.SERVER_DOWN);
        successEvent.setCreateTime(new Date());
        successEvent.setUpdateTime(new Date());
        EVENTS.add(successEvent);

        String PLUGIN_INSTANCE_NAME = "alert-instance-mail";
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                PLUGIN_DEFINE_ID, PLUGIN_INSTANCE_PARAMS, PLUGIN_INSTANCE_NAME);
        alertPluginInstance.setInstanceType(AlertPluginInstanceType.GLOBAL);
        alertPluginInstance.setId(1);
        ALERT_INSTANCE_LIST.add(alertPluginInstance);
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
        listenerEventPostService.testSend(PLUGIN_DEFINE_ID, PLUGIN_INSTANCE_PARAMS);
    }

    @Test
    public void testSendServerDownEVENTSuccess() {
        when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList()).thenReturn(ALERT_INSTANCE_LIST);
        AlertResult sendResult = new AlertResult();
        sendResult.setStatus(String.valueOf(true));
        sendResult.setMessage(String.format("Alert Plugin %s send success", PLUGIN_INSTANCE_NAME));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(sendResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assertions.assertTrue(Boolean.parseBoolean(sendResult.getStatus()));
        when(listenerEventMapper.deleteById(1)).thenReturn(1);
        listenerEventPostService.send(EVENTS);
    }

    @Test
    public void testSendServerDownEventFailed() {
        when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList()).thenReturn(ALERT_INSTANCE_LIST);
        AlertResult sendResult = new AlertResult();
        sendResult.setStatus(String.valueOf(false));
        sendResult.setMessage(String.format("Alert Plugin %s send failed", PLUGIN_INSTANCE_NAME));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(sendResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assertions.assertFalse(Boolean.parseBoolean(sendResult.getStatus()));
        listenerEventPostService.send(EVENTS);
    }
}
