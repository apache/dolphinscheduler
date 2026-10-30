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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.service.impl.ProjectResourceAuthorizationServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.entity.WorkerGroupPageDetail;
import org.apache.dolphinscheduler.dao.repository.AlertGroupDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.QueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectResourceAuthorizationServiceTest {

    @Mock
    private ProjectAuthorizationService projectAuthorizationService;
    @Mock
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;
    @Mock
    private DataSourceDao dataSourceDao;
    @Mock
    private WorkerGroupDao workerGroupDao;
    @Mock
    private WorkerGroupService workerGroupService;
    @Mock
    private TenantDao tenantDao;
    @Mock
    private QueueDao queueDao;
    @Mock
    private IEnvironmentDao environmentDao;
    @Mock
    private AlertGroupDao alertGroupDao;
    @Mock
    private K8sNamespaceDao namespaceDao;
    @Mock
    private TaskGroupDao taskGroupDao;
    @InjectMocks
    private ProjectResourceAuthorizationServiceImpl service;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1);
        admin.setUserType(UserType.ADMIN_USER);
    }

    @ParameterizedTest
    @EnumSource(ProjectResourceType.class)
    void adminCanGrantEachSupportedResourceType(ProjectResourceType type) {
        String key = "42";
        switch (type) {
            case DATASOURCE:
                Mockito.when(dataSourceDao.queryById(42)).thenReturn(new DataSource());
                break;
            case TENANT:
                Mockito.when(tenantDao.queryById(42)).thenReturn(new Tenant());
                break;
            case QUEUE:
                Mockito.when(queueDao.queryById(42)).thenReturn(new Queue());
                break;
            case ENVIRONMENT:
                Mockito.when(environmentDao.queryById(42)).thenReturn(new Environment());
                break;
            case ALERT_GROUP:
                Mockito.when(alertGroupDao.queryById(42)).thenReturn(new AlertGroup());
                break;
            case K8S_NAMESPACE:
                Mockito.when(namespaceDao.queryById(42)).thenReturn(new K8sNamespace());
                break;
            case TASK_GROUP:
                Mockito.when(taskGroupDao.queryById(42)).thenReturn(new TaskGroup());
                break;
            case WORKER_GROUP:
                key = "workers";
                WorkerGroup group = new WorkerGroup();
                group.setName(key);
                Mockito.when(workerGroupDao.queryWorkerGroupByName(key)).thenReturn(Collections.singletonList(group));
                break;
            default:
                Assertions.fail("Missing resource test: " + type);
        }
        service.grantResource(admin, 100L, type, key);
        Mockito.verify(projectAuthorizationService).requirePermission(admin, 100L, ProjectPermission.VIEW);
        ArgumentCaptor<ProjectResourceAuthorization> grant =
                ArgumentCaptor.forClass(ProjectResourceAuthorization.class);
        Mockito.verify(resourceAuthorizationDao).insert(grant.capture());
        Assertions.assertEquals(100L, grant.getValue().getProjectCode());
        Assertions.assertEquals(type, grant.getValue().getResourceType());
        Assertions.assertEquals(key, grant.getValue().getResourceKey());
    }

    @Test
    void projectMemberAdministratorCannotGrantOrRevokeResources() {
        admin.setUserType(UserType.GENERAL_USER);
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertThrows(ServiceException.class,
                () -> service.revokeResource(admin, 100L, ProjectResourceType.DATASOURCE, "42"));
        Mockito.verifyNoInteractions(projectAuthorizationService, resourceAuthorizationDao, dataSourceDao);
    }

    @Test
    void missingProjectDoesNotCreateGrant() {
        Mockito.when(projectAuthorizationService.requirePermission(admin, 100L, ProjectPermission.VIEW))
                .thenThrow(new ServiceException(Status.PROJECT_NOT_EXIST));
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.DATASOURCE, "42"));
        Mockito.verifyNoInteractions(resourceAuthorizationDao, dataSourceDao);
    }

    @Test
    void defaultWorkerGroupCanBeGrantedWithoutDatabaseRecordAndRepeatedGrantIsIdempotent() {
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "default"))
                .thenReturn(false, true);
        service.grantResource(admin, 100L, ProjectResourceType.WORKER_GROUP, "default");
        service.grantResource(admin, 100L, ProjectResourceType.WORKER_GROUP, "default");
        Mockito.verify(resourceAuthorizationDao).insert(Mockito.any(ProjectResourceAuthorization.class));
        Mockito.verifyNoInteractions(workerGroupDao);
    }

    @Test
    void workerGroupNameMustMatchCaseExactly() {
        WorkerGroup group = new WorkerGroup();
        group.setName("workers");
        Mockito.when(workerGroupDao.queryWorkerGroupByName("WORKERS")).thenReturn(Collections.singletonList(group));
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.WORKER_GROUP, "WORKERS"));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @ParameterizedTest
    @EnumSource(ProjectResourceType.class)
    void missingResourceDoesNotCreateGrant(ProjectResourceType type) {
        Assertions.assertThrows(ServiceException.class, () -> service.grantResource(admin, 100L, type, "42"));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "-1", "042", "+42", "abc", "2147483648"})
    void invalidResourceKeyIsRejected(String key) {
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.DATASOURCE, key));
        Mockito.verifyNoInteractions(resourceAuthorizationDao, dataSourceDao);
    }

    @Test
    void revokeOnlyRemovesSelectedGrantEvenIfResourceWasDeleted() {
        service.revokeResource(admin, 100L, ProjectResourceType.DATASOURCE, "42");
        Mockito.verify(resourceAuthorizationDao).revokeAuthorization(100L, ProjectResourceType.DATASOURCE, "42");
        Mockito.verifyNoInteractions(dataSourceDao);
    }

    @Test
    void builtInDefaultTenantCanBeGranted() {
        Tenant tenant = new Tenant();
        tenant.setId(-1);
        tenant.setTenantCode("default");
        Mockito.when(tenantDao.queryById(-1)).thenReturn(tenant);

        service.grantResource(admin, 100L, ProjectResourceType.TENANT, "-1");

        ArgumentCaptor<ProjectResourceAuthorization> grant =
                ArgumentCaptor.forClass(ProjectResourceAuthorization.class);
        Mockito.verify(resourceAuthorizationDao).insert(grant.capture());
        Assertions.assertEquals(100L, grant.getValue().getProjectCode());
        Assertions.assertEquals(ProjectResourceType.TENANT, grant.getValue().getResourceType());
        Assertions.assertEquals("-1", grant.getValue().getResourceKey());
    }

    @Test
    void reservedTenantIdMustReferToTheBuiltInDefaultTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(-1);
        tenant.setTenantCode("custom");
        Mockito.lenient().when(tenantDao.queryById(-1)).thenReturn(tenant);

        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.TENANT, "-1"));

        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @Test
    void missingDefaultTenantDoesNotCreateGrant() {
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, ProjectResourceType.TENANT, "-1"));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectResourceType.class, names = {"TENANT", "WORKER_GROUP"}, mode = EnumSource.Mode.EXCLUDE)
    void reservedTenantIdIsInvalidForOtherNumericResources(ProjectResourceType type) {
        Assertions.assertThrows(ServiceException.class,
                () -> service.grantResource(admin, 100L, type, "-1"));
        Mockito.verifyNoInteractions(resourceAuthorizationDao, tenantDao);
    }

    @Test
    void queryRequiresViewPermissionInTargetProject() {
        admin.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectAuthorizationService.requirePermission(admin, 100L, ProjectPermission.VIEW))
                .thenThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM));
        Assertions.assertThrows(ServiceException.class,
                () -> service.queryAuthorizations(admin, 100L, ProjectResourceType.DATASOURCE));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }
    @Test
    void configuredWorkerGroupCanBeGrantedWithoutDatabaseRecord() {
        WorkerGroupPageDetail group = new WorkerGroupPageDetail();
        group.setName("configured-workers");
        Mockito.when(workerGroupService.getConfigWorkerGroupPageDetail()).thenReturn(Collections.singletonList(group));
        service.grantResource(admin, 100L, ProjectResourceType.WORKER_GROUP, "configured-workers");
        Mockito.verify(resourceAuthorizationDao).insert(Mockito.any(ProjectResourceAuthorization.class));
    }

}
