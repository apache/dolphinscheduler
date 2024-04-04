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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALARM_INSTANCE_MANAGE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_INSTANCE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_PLUGIN_UPDATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AlertPluginInstanceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

    private User noPermUser;

    private final AlertPluginInstanceType normalInstanceType = AlertPluginInstanceType.NORMAL;

    private final AlertPluginInstanceType globalInstanceType = AlertPluginInstanceType.GLOBAL;

    private final WarningType warningType = WarningType.ALL;

    private final Integer GLOBAL_ALERT_GROUP_ID = 2;

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

    private AlertPluginInstance alertPluginInstance;

    @BeforeEach
    public void before() {
        user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);

        noPermUser = new User();
        noPermUser.setUserType(UserType.GENERAL_USER);
        noPermUser.setId(2);

        alertPluginInstance = getAlertPluginInstance(1, normalInstanceType, "test");
        alertPluginInstances = new ArrayList<>();
        alertPluginInstances.add(alertPluginInstance);
    }

    @Test
    public void testCreate() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                noPermUser.getId(), ALERT_INSTANCE_CREATE, baseServiceLogger)).thenReturn(false);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM, () -> alertPluginInstanceService.create(noPermUser,
                1, "test", normalInstanceType, warningType, uiParams));

        when(alertPluginInstanceMapper.existInstanceName("test")).thenReturn(true);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                1, ALERT_INSTANCE_CREATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                null, 0, baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.PLUGIN_INSTANCE_ALREADY_EXISTS,
                () -> alertPluginInstanceService.create(user, 1, "test", normalInstanceType, warningType, uiParams));
        when(alertPluginInstanceMapper.insert(Mockito.any())).thenReturn(1);
        AlertPluginInstance alertPluginInstance =
                alertPluginInstanceService.create(user, 1, "test1", normalInstanceType, warningType, uiParams);
        assertNotNull(alertPluginInstance);

        when(alertGroupMapper.selectById(GLOBAL_ALERT_GROUP_ID)).thenReturn(getGlobalAlertGroup());
        assertDoesNotThrow(() -> alertPluginInstanceService.create(user, 1, "global_plugin_instance",
                AlertPluginInstanceType.GLOBAL, warningType, uiParams));

        when(alertGroupMapper.selectById(GLOBAL_ALERT_GROUP_ID)).thenReturn(getGlobalAlertGroup("1"));
        assertDoesNotThrow(() -> alertPluginInstanceService.create(user, 1, "global_plugin_instance",
                AlertPluginInstanceType.GLOBAL, warningType, uiParams));

        when(alertPluginInstanceMapper.insert(Mockito.any())).thenReturn(-1);
        assertThrowsServiceException(Status.SAVE_ERROR,
                () -> alertPluginInstanceService.create(user, 1, "test_insert_error", normalInstanceType, warningType,
                        uiParams));
    }

    @Test
    public void testSendAlert() {
        Mockito.when(registryClient.getServerList(RegistryNodeType.ALERT_SERVER)).thenReturn(new ArrayList<>());
        assertThrowsServiceException(Status.ALERT_SERVER_NOT_EXIST,
                () -> alertPluginInstanceService.testSend(1, uiParams));
        Server server = new Server();
        server.setPort(50052);
        server.setHost("127.0.0.1");

        Mockito.when(registryClient.getServerList(RegistryNodeType.ALERT_SERVER))
                .thenReturn(Collections.singletonList(server));
        assertThrowsServiceException(Status.ALERT_TEST_SENDING_FAILED,
                () -> alertPluginInstanceService.testSend(1, uiParams));
    }

    @Test
    public void testDelete() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                noPermUser.getId(), ALERT_PLUGIN_DELETE, baseServiceLogger)).thenReturn(false);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertPluginInstanceService.deleteById(noPermUser, 1));

        List<String> ids = Arrays.asList("11,2,3", "5,96", null, "98,1");
        when(alertGroupMapper.queryInstanceIdsList()).thenReturn(ids);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                1, ALERT_PLUGIN_DELETE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                null, 0, baseServiceLogger)).thenReturn(true);
        AlertPluginInstance normalInstanceWithId1 = getAlertPluginInstance(1, normalInstanceType, "test1");
        AlertPluginInstance normalInstanceWithId9 = getAlertPluginInstance(9, normalInstanceType, "test9");
        AlertPluginInstance globalInstanceWithId5 = getAlertPluginInstance(5, globalInstanceType, "test5");
        when(alertPluginInstanceMapper.selectById(1)).thenReturn(normalInstanceWithId1);
        when(alertPluginInstanceMapper.selectById(9)).thenReturn(normalInstanceWithId9);
        when(alertPluginInstanceMapper.selectById(5)).thenReturn(globalInstanceWithId5);
        AlertGroup globalAlertGroup = new AlertGroup();
        globalAlertGroup.setId(2);
        globalAlertGroup.setAlertInstanceIds("5,96");
        when(alertGroupMapper.selectById(2)).thenReturn(globalAlertGroup);
        when(alertGroupMapper.updateById(Mockito.any())).thenReturn(1);

        assertThrowsServiceException(Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED,
                () -> alertPluginInstanceService.deleteById(user, 1));

        when(alertPluginInstanceMapper.deleteById(9)).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> alertPluginInstanceService.deleteById(user, 9));

        when(alertPluginInstanceMapper.deleteById(5)).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> alertPluginInstanceService.deleteById(user, 5));

        when(alertGroupMapper.queryInstanceIdsList()).thenReturn(Collections.emptyList());
        Assertions.assertDoesNotThrow(() -> alertPluginInstanceService.deleteById(user, 9));
    }

    @Test
    public void testUpdate() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                noPermUser.getId(), ALERT_PLUGIN_UPDATE, baseServiceLogger)).thenReturn(false);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertPluginInstanceService.updateById(noPermUser, 1, "test", warningType, uiParams));

        when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(0);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE, 1,
                ALERT_PLUGIN_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE, null, 0,
                baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.SAVE_ERROR,
                () -> alertPluginInstanceService.updateById(user, 1, "testUpdate", warningType, uiParams));

        when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(1);
        AlertPluginInstance alertPluginInstance =
                alertPluginInstanceService.updateById(user, 1, "testUpdate", warningType, uiParams);
        Assertions.assertNotNull(alertPluginInstance);
    }

    @Test
    public void testGetById() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                noPermUser.getId(), ALARM_INSTANCE_MANAGE, baseServiceLogger)).thenReturn(false);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertPluginInstanceService.getById(noPermUser, 1));

        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE,
                user.getId(), ALARM_INSTANCE_MANAGE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_PLUGIN_INSTANCE, null, 0,
                baseServiceLogger)).thenReturn(true);
        when(alertPluginInstanceMapper.selectById(1))
                .thenReturn(getAlertPluginInstance(1, AlertPluginInstanceType.NORMAL, "test_get_instance"));

        Assertions.assertEquals(alertPluginInstanceService.getById(user, 1).getId(), 1);
    }

    @Test
    public void testCheckExistPluginInstanceName() {
        when(alertPluginInstanceMapper.existInstanceName(Mockito.any(String.class))).thenReturn(false);
        Assertions.assertEquals(false, alertPluginInstanceService.checkExistPluginInstanceName("test"));
    }

    @Test
    public void testListPaging() {
        IPage<AlertPluginInstance> page = new Page<>();
        page.setRecords(Collections.singletonList(alertPluginInstance));
        page.setTotal(1);
        page.setPages(1);

        when(alertPluginInstanceMapper.queryByInstanceNamePage(Mockito.any(Page.class), Mockito.any(String.class)))
                .thenReturn(page);
        assertDoesNotThrow(() -> alertPluginInstanceService.listPaging(user, "test", 1, 1));
    }

    @Test
    public void testQueryAll() {
        when(alertPluginInstanceMapper.queryAllAlertPluginInstanceList()).thenReturn(Collections.emptyList());
        Assertions.assertEquals(0, alertPluginInstanceService.queryAll().size());

        when(alertPluginInstanceMapper.queryAllAlertPluginInstanceList())
                .thenReturn(Collections.singletonList(alertPluginInstance));
        when(pluginDefineMapper.queryAllPluginDefineList()).thenReturn(Collections.emptyList());
        Assertions.assertEquals(0, alertPluginInstanceService.queryAll().size());

        AlertPluginInstance alertPluginInstance = getAlertPluginInstance(1, normalInstanceType, "test");
        PluginDefine pluginDefine = new PluginDefine("script", "script", uiParams);
        pluginDefine.setId(1);
        List<PluginDefine> pluginDefines = Collections.singletonList(pluginDefine);
        List<AlertPluginInstance> pluginInstanceList = Collections.singletonList(alertPluginInstance);
        when(alertPluginInstanceMapper.queryAllAlertPluginInstanceList()).thenReturn(pluginInstanceList);
        when(pluginDefineMapper.queryAllPluginDefineList()).thenReturn(pluginDefines);
        Assertions.assertDoesNotThrow(() -> alertPluginInstanceService.queryAll());
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

    private AlertGroup getGlobalAlertGroup(String... alertPluginInstanceIds) {
        AlertGroup globalAlertGroup = new AlertGroup();
        globalAlertGroup.setId(2);
        globalAlertGroup.setAlertInstanceIds(String.join(",", alertPluginInstanceIds));

        return globalAlertGroup;
    }
}
