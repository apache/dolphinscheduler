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
import org.apache.dolphinscheduler.alert.service.ListenerEventSender;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ServerDownListenerEvent;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.ListenerEventDao;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListenerEventSenderTest {

    @Mock
    private ListenerEventDao listenerEventDao;

    @Mock
    private AlertPluginInstanceMapper alertPluginInstanceMapper;
    @Mock
    private AlertPluginManager alertPluginManager;

    @Mock
    private AlertConfig alertConfig;

    @InjectMocks
    private ListenerEventSender listenerEventSender;

    @Test
    void testSendServerDownEventSuccess() {
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

        int pluginDefineId = 1;
        String pluginInstanceParams =
                "{\"User\":\"xx\",\"receivers\":\"xx\",\"sender\":\"xx\",\"smtpSslTrust\":\"*\",\"enableSmtpAuth\":\"true\",\"receiverCcs\":null,\"showType\":\"table\",\"starttlsEnable\":\"false\",\"serverPort\":\"25\",\"serverHost\":\"xx\",\"Password\":\"xx\",\"sslEnable\":\"false\"}";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertPluginInstance.setInstanceType(AlertPluginInstanceType.GLOBAL);
        alertPluginInstance.setId(1);
        alertInstanceList.add(alertPluginInstance);
        when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList()).thenReturn(alertInstanceList);

        AlertResult sendResult = new AlertResult();
        sendResult.setSuccess(true);
        sendResult.setMessage(String.format("Alert Plugin %s send success", pluginInstanceName));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(sendResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assertions.assertTrue(sendResult.isSuccess());
        listenerEventSender.sendEvent(successEvent);
    }

    @Test
    void testSendServerDownEventFailed() {
        ServerDownListenerEvent serverDownListenerEvent = new ServerDownListenerEvent();
        serverDownListenerEvent.setEventTime(new Date());
        serverDownListenerEvent.setType("WORKER");
        serverDownListenerEvent.setHost("192.168.*.*");
        ListenerEvent successEvent = new ListenerEvent();
        successEvent.setId(1);
        successEvent.setPostStatus(AlertStatus.WAIT_EXECUTION);
        successEvent.setContent(JSONUtils.toJsonString(serverDownListenerEvent));
        successEvent.setSign(DigestUtils.sha1Hex(successEvent.getContent()));
        successEvent.setEventType(ListenerEventType.SERVER_DOWN);
        successEvent.setCreateTime(new Date());
        successEvent.setUpdateTime(new Date());

        int pluginDefineId = 1;
        String pluginInstanceParams =
                "{\"User\":\"xx\",\"receivers\":\"xx\",\"sender\":\"xx\",\"smtpSslTrust\":\"*\",\"enableSmtpAuth\":\"true\",\"receiverCcs\":null,\"showType\":\"table\",\"starttlsEnable\":\"false\",\"serverPort\":\"25\",\"serverHost\":\"xx\",\"Password\":\"xx\",\"sslEnable\":\"false\"}";
        String pluginInstanceName = "alert-instance-mail";
        List<AlertPluginInstance> alertInstanceList = new ArrayList<>();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(
                pluginDefineId, pluginInstanceParams, pluginInstanceName);
        alertPluginInstance.setInstanceType(AlertPluginInstanceType.GLOBAL);
        alertPluginInstance.setId(1);
        alertInstanceList.add(alertPluginInstance);
        when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList()).thenReturn(alertInstanceList);

        AlertResult sendResult = new AlertResult();
        sendResult.setSuccess(false);
        sendResult.setMessage(String.format("Alert Plugin %s send failed", pluginInstanceName));
        AlertChannel alertChannelMock = mock(AlertChannel.class);
        when(alertChannelMock.process(Mockito.any())).thenReturn(sendResult);
        when(alertPluginManager.getAlertChannel(1)).thenReturn(Optional.of(alertChannelMock));
        Assertions.assertFalse(sendResult.isSuccess());
        listenerEventSender.sendEvent(successEvent);
    }
}
