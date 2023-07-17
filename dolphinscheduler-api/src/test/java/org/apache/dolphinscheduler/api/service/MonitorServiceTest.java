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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.MonitorServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.MonitorDBDao;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * monitor service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MonitorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceTest.class);

    @InjectMocks
    private MonitorServiceImpl monitorService;

    @Mock
    private MonitorDBDao monitorDBDao;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private RegistryClient registryClient;

    private User user;

    public static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @BeforeEach
    public void init() {
        user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);
    }

    @Test
    public void testQueryDatabaseState() {
        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_DATABASES_VIEW, true);
        Mockito.when(monitorDBDao.queryDatabaseState()).thenReturn(getList());
        Map<String, Object> result = monitorService.queryDatabaseState(user);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<MonitorRecord> monitorRecordList = (List<MonitorRecord>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(monitorRecordList));

        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_DATABASES_VIEW, false);
        Map<String, Object> noPermission = monitorService.queryDatabaseState(user);
        Assertions.assertEquals(Status.SUCCESS, noPermission.get(Constants.STATUS));
    }

    @Test
    public void testQueryMaster() {
        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_MASTER_VIEW, true);
        Mockito.when(registryClient.getServerList(RegistryNodeType.MASTER)).thenReturn(getServerList());
        Map<String, Object> result = monitorService.queryMaster(user);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_MASTER_VIEW, false);
        Map<String, Object> noPermission = monitorService.queryMaster(user);
        Assertions.assertEquals(Status.SUCCESS, noPermission.get(Constants.STATUS));
    }

    @Test
    public void testQueryWorker() {
        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_WORKER_VIEW, true);
        Mockito.when(registryClient.getServerList(RegistryNodeType.WORKER)).thenReturn(getServerList());
        Map<String, Object> result = monitorService.queryWorker(user);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        mockPermissionCheck(ApiFuncIdentificationConstant.MONITOR_WORKER_VIEW, false);
        Map<String, Object> noPermission = monitorService.queryWorker(user);
        Assertions.assertEquals(Status.SUCCESS, noPermission.get(Constants.STATUS));
    }

    @Test
    public void testGetServerListFromZK() {
        // TODO need zk
        /* List<Server> serverList = monitorService.getServerListFromZK(true); */
        /* logger.info(serverList.toString()); */
    }

    private void mockPermissionCheck(String permissionKey, boolean result) {
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.MONITOR, 1,
                permissionKey, serviceLogger)).thenReturn(result);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.MONITOR, null, 0,
                serviceLogger)).thenReturn(true);
    }

    private List<MonitorRecord> getList() {
        List<MonitorRecord> monitorRecordList = new ArrayList<>();
        monitorRecordList.add(getEntity());
        return monitorRecordList;
    }

    private MonitorRecord getEntity() {
        MonitorRecord monitorRecord = new MonitorRecord();
        monitorRecord.setDbType(DbType.MYSQL);
        return monitorRecord;
    }

    private List<Server> getServerList() {
        Server server = new Server();
        server.setId(1);
        server.setHost("127.0.0.1");
        server.setZkDirectory("ws/server");
        server.setPort(123);
        server.setCreateTime(new Date());
        server.setLastHeartbeatTime(new Date());

        List<Server> servers = new ArrayList<>();
        servers.add(server);
        return servers;
    }

}
