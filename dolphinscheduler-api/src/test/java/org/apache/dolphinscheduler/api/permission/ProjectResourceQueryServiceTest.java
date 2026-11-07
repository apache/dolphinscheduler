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

package org.apache.dolphinscheduler.api.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.impl.AlertGroupServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DataSourceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.EnvironmentServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.QueueServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TenantServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;
import org.apache.dolphinscheduler.dao.repository.AlertGroupDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;
import org.apache.dolphinscheduler.dao.repository.QueueDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectResourceQueryServiceTest {

    @Mock
    private ProjectAuthorizationService authorization;
    @Mock
    private DataSourceDao dataSourceDao;
    @Mock
    private TenantDao tenantDao;
    @Mock
    private QueueDao queueDao;
    @Mock
    private AlertGroupDao alertGroupDao;
    @Mock
    private EnvironmentMapper environmentMapper;
    @Mock
    private EnvironmentWorkerGroupRelationMapper relationMapper;
    @InjectMocks
    private DataSourceServiceImpl dataSourceService;
    @InjectMocks
    private TenantServiceImpl tenantService;
    @InjectMocks
    private QueueServiceImpl queueService;
    @InjectMocks
    private AlertGroupServiceImpl alertGroupService;
    @InjectMocks
    private EnvironmentServiceImpl environmentService;

    private User user(UserType type) {
        User user = new User();
        user.setId(10);
        user.setUserType(type);
        return user;
    }

    @Test
    void globalListsRequireAdministrator() {
        User user = user(UserType.GENERAL_USER);
        for (Executable query : Arrays.<Executable>asList(
                () -> dataSourceService.queryDataSourceList(user, DbType.MYSQL.getCode(), null),
                () -> tenantService.queryTenantList(user, null),
                () -> queueService.queryList(user, null),
                () -> alertGroupService.queryAllAlertGroup(user, null),
                () -> environmentService.queryAllEnvironmentList(user, null))) {
            ServiceException error = assertThrows(ServiceException.class, query);
            assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), error.getCode());
        }
        verifyNoInteractions(dataSourceDao, tenantDao, queueDao, alertGroupDao, environmentMapper, authorization);
    }

    @Test
    void projectListsRejectUserBeforeReadingResources() {
        User user = user(UserType.GENERAL_USER);
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM))
                .when(authorization).requirePermission(user, 100L, ProjectPermission.VIEW);
        for (Executable query : Arrays.<Executable>asList(
                () -> dataSourceService.queryDataSourceList(user, DbType.MYSQL.getCode(), 100L),
                () -> tenantService.queryTenantList(user, 100L),
                () -> queueService.queryList(user, 100L),
                () -> alertGroupService.queryAllAlertGroup(user, 100L),
                () -> environmentService.queryAllEnvironmentList(user, 100L))) {
            assertThrows(ServiceException.class, query);
        }
        verifyNoInteractions(dataSourceDao, tenantDao, queueDao, alertGroupDao, environmentMapper);
    }

    @Test
    void datasourceTypeFilteringPreservesProjectScopeForAdministrator() {
        User user = user(UserType.ADMIN_USER);
        DataSource mysql = new DataSource();
        mysql.setType(DbType.MYSQL);
        DataSource postgres = new DataSource();
        postgres.setType(DbType.POSTGRESQL);
        when(dataSourceDao.queryByProject(100L)).thenReturn(Arrays.asList(mysql, postgres));
        assertEquals(Collections.singletonList(mysql),
                dataSourceService.queryDataSourceList(user, DbType.MYSQL.getCode(), 100L));
        verify(authorization).requirePermission(user, 100L, ProjectPermission.VIEW);
        verify(dataSourceDao, never()).queryDataSourceByType(anyInt(), anyInt());
    }

    @Test
    void projectListsUseGrantedResourceQueries() {
        User user = user(UserType.GENERAL_USER);
        Tenant tenant = new Tenant();
        Queue queue = new Queue();
        AlertGroup alert = new AlertGroup();
        when(tenantDao.queryByProject(100L)).thenReturn(Collections.singletonList(tenant));
        when(queueDao.queryByProject(100L)).thenReturn(Collections.singletonList(queue));
        when(alertGroupDao.queryByProject(100L)).thenReturn(Collections.singletonList(alert));
        assertEquals(Collections.singletonList(tenant), tenantService.queryTenantList(user, 100L));
        assertEquals(Collections.singletonList(queue), queueService.queryList(user, 100L));
        assertEquals(Collections.singletonList(alert), alertGroupService.queryAllAlertGroup(user, 100L));
        verify(authorization, times(3)).requirePermission(user, 100L, ProjectPermission.VIEW);
    }

    @Test
    void environmentQueryPreservesCodeAndWorkerGroupMapping() {
        User user = user(UserType.GENERAL_USER);
        Environment environment = new Environment();
        environment.setId(1);
        environment.setCode(200L);
        EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
        relation.setEnvironmentCode(200L);
        relation.setWorkerGroup("worker");
        when(environmentMapper.queryByProject(100L)).thenReturn(Collections.singletonList(environment));
        when(relationMapper.selectList(null)).thenReturn(Collections.singletonList(relation));
        assertEquals(Collections.singletonList("worker"),
                environmentService.queryAllEnvironmentList(user, 100L).get(0).getWorkerGroups());
        verify(authorization).requirePermission(user, 100L, ProjectPermission.VIEW);
    }

    @Test
    void databaseMetadataRequiresProjectResourceGrantForAdministrator() {
        User user = user(UserType.ADMIN_USER);
        when(dataSourceDao.queryById(1)).thenReturn(new DataSource());
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION)).when(authorization)
                .requireResourceUse(user, 100L, ProjectPermission.VIEW, ProjectResourceType.DATASOURCE, "1");
        assertThrows(ServiceException.class, () -> dataSourceService.getDatabases(user, 1, 100L));
        assertThrows(ServiceException.class, () -> dataSourceService.getTables(user, 1, "db", 100L));
        assertThrows(ServiceException.class, () -> dataSourceService.getTableColumns(user, 1, "db", "table", 100L));
        verify(authorization, times(3)).requireResourceUse(user, 100L, ProjectPermission.VIEW,
                ProjectResourceType.DATASOURCE, "1");
    }
}
