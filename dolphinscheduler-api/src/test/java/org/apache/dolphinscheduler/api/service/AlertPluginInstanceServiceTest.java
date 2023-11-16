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

package org.apache.dolphinscheduler.api.service;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALART_INSTANCE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_UPDATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AlertPluginInstanceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.extract.alert.request.AlertTestSendRequest;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.registry.api.RegistryClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * alert plugin instance service test
 */
@ExtendWith(MockitoExtension.class)
public class AlertPluginInstanceServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @InjectMocks
    AlertPluginInstanceServiceImpl alertPluginInstanceService;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Mock
    private PluginDefineMapper pluginDefineMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    @Mock
    private RegistryClient registryClient;

    private List<AlertPluginInstance> alertPluginInstances;

    private User user;

    private final AlertPluginInstanceType normalInstanceType = AlertPluginInstanceType.NORMAL;

    private final AlertPluginInstanceType globalInstanceType = AlertPluginInstanceType.GLOBAL;

    private final WarningType warningType = WarningType.ALL;

    private String uiParams = "[\n"
            + "    {\n"
            + "        \"field\":\"userParams\",\n"
            + "        \"name\":\"user.params\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":\"please enter your custom parameters, which will be passed to you when calling your script\",\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"input\",\n"
            + "        \"title\":\"user.params\",\n"
            + "        \"value\":\"userParams\",\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":false,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ]\n"
            + "    },\n"
            + "    {\n"
            + "        \"field\":\"path\",\n"
            + "        \"name\":\"path\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":\"please upload the file to the disk directory of the alert server, and ensure that the path is absolute and has the corresponding access rights\",\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"input\",\n"
            + "        \"title\":\"path\",\n"
            + "        \"value\":\"/kris/script/path\",\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":true,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ]\n"
            + "    },\n"
            + "    {\n"
            + "        \"field\":\"type\",\n"
            + "        \"name\":\"type\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":null,\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"radio\",\n"
            + "        \"title\":\"type\",\n"
            + "        \"value\":0,\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":true,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ],\n"
            + "        \"options\":[\n"
            + "            {\n"
            + "                \"label\":\"SHELL\",\n"
            + "                \"value\":0,\n"
            + "                \"disabled\":false\n"
            + "            }\n"
            + "        ]\n"
            + "    }\n"
            + "]\n"
            + "\n";

    private String paramsMap = "{\"path\":\"/kris/script/path\",\"userParams\":\"userParams\",\"type\":\"0\"}";

    @BeforeEach
    public void before() {
        user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);
        AlertPluginInstance alertPluginInstance = getAlertPluginInstance(1, normalInstanceType, "test");
        alertPluginInstances = new ArrayList<>();
        alertPluginInstances.add(alertPluginInstance);
    }

    @Test
    public void testCreate() {
        Mockito.when(alertPluginInstanceMapper.existInstanceName("test")).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                1, ALART_INSTANCE_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                null, 0, baseServiceLogger)).thenReturn(true);
        Map<String, Object> result =
                alertPluginInstanceService.create(user, 1, "test", normalInstanceType, warningType, uiParams);
        Assertions.assertEquals(Status.PLUGIN_INSTANCE_ALREADY_EXISTS, result.get(Constants.STATUS));
        Mockito.when(alertPluginInstanceMapper.insert(Mockito.any())).thenReturn(1);
        result = alertPluginInstanceService.create(user, 1, "test1", normalInstanceType, warningType, uiParams);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        Assertions.assertNotNull(result.get(Constants.DATA_LIST));
    }

    @Test
    public void testSendAlert() {
        Result<Object> result;
        Mockito.when(registryClient.getAlertServerAddress()).thenReturn(Optional.empty());
        result = alertPluginInstanceService.testSend(1, uiParams);
        Assertions.assertEquals(Status.ALERT_CHANNEL_NOT_EXIST.getCode(), result.getCode());
        AlertSendResponse.AlertSendResponseResult alertResult = new AlertSendResponse.AlertSendResponseResult();
        alertResult.setSuccess(true);
        AlertTestSendRequest alertTestSendRequest = new AlertTestSendRequest(
                1,
                uiParams);
        Mockito.when(registryClient.getAlertServerAddress()).thenReturn(Optional.of(new Host("127.0.0.1", 50052)));
        result = alertPluginInstanceService.testSend(1, uiParams);
        Assertions.assertEquals(Status.ALERT_TEST_SENDING_FAILED.getCode(), result.getCode());
    }

    @Test
    public void testDelete() {
        List<String> ids = Arrays.asList("11,2,3", "5,96", null, "98,1");
        Mockito.when(alertGroupMapper.queryInstanceIdsList()).thenReturn(ids);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                1, ALERT_PLUGIN_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                null, 0, baseServiceLogger)).thenReturn(true);
        AlertPluginInstance normalInstanceWithId1 = getAlertPluginInstance(1, normalInstanceType, "test1");
        AlertPluginInstance normalInstanceWithId9 = getAlertPluginInstance(9, normalInstanceType, "test9");
        AlertPluginInstance globalInstanceWithId5 = getAlertPluginInstance(5, globalInstanceType, "test5");
        Mockito.when(alertPluginInstanceMapper.selectById(1)).thenReturn(normalInstanceWithId1);
        Mockito.when(alertPluginInstanceMapper.selectById(9)).thenReturn(normalInstanceWithId9);
        Mockito.when(alertPluginInstanceMapper.selectById(5)).thenReturn(globalInstanceWithId5);
        AlertGroup globalAlertGroup = new AlertGroup();
        globalAlertGroup.setId(2);
        globalAlertGroup.setAlertInstanceIds("5,96");
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(globalAlertGroup);
        Mockito.when(alertGroupMapper.updateById(Mockito.any())).thenReturn(1);
        Map<String, Object> result;
        result = alertPluginInstanceService.delete(user, 1);
        Assertions.assertEquals(Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED,
                result.get(Constants.STATUS));
        Mockito.when(alertPluginInstanceMapper.deleteById(9)).thenReturn(1);
        result = alertPluginInstanceService.delete(user, 9);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        Mockito.when(alertPluginInstanceMapper.deleteById(5)).thenReturn(1);
        result = alertPluginInstanceService.delete(user, 5);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdate() {
        Mockito.when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(0);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                1, ALERT_PLUGIN_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                null, 0, baseServiceLogger)).thenReturn(true);
        Map<String, Object> result = alertPluginInstanceService.update(user, 1, "testUpdate", warningType, uiParams);
        Assertions.assertEquals(Status.SAVE_ERROR, result.get(Constants.STATUS));
        Mockito.when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(1);
        result = alertPluginInstanceService.update(user, 1, "testUpdate", warningType, uiParams);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryAll() {
        AlertPluginInstance alertPluginInstance = getAlertPluginInstance(1, normalInstanceType, "test");
        PluginDefine pluginDefine = new PluginDefine("script", "script", uiParams);
        pluginDefine.setId(1);
        List<PluginDefine> pluginDefines = Collections.singletonList(pluginDefine);
        List<AlertPluginInstance> pluginInstanceList = Collections.singletonList(alertPluginInstance);
        Mockito.when(alertPluginInstanceMapper.queryAllAlertPluginInstanceList()).thenReturn(pluginInstanceList);
        Mockito.when(pluginDefineMapper.queryAllPluginDefineList()).thenReturn(pluginDefines);
        Map<String, Object> result = alertPluginInstanceService.queryAll();
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    private AlertPluginInstance getAlertPluginInstance(int id, AlertPluginInstanceType instanceType,
                                                       String instanceName) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setId(id);
        alertPluginInstance.setPluginDefineId(1);
        alertPluginInstance.setInstanceType(instanceType);
        alertPluginInstance.setWarningType(warningType);
        alertPluginInstance.setPluginInstanceParams(paramsMap);
        alertPluginInstance.setInstanceName(instanceName);
        return alertPluginInstance;
    }

}
