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

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskResourcePermissionCheckerTest {

    @Mock
    private TaskDatasourcePermissionChecker datasourcePermissionChecker;
    @Mock
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;
    @Mock
    private IEnvironmentDao environmentDao;
    @Mock
    private TenantDao tenantDao;
    @Mock
    private K8sNamespaceDao namespaceDao;
    @InjectMocks
    private TaskResourcePermissionChecker checker;

    private TaskDefinition task;

    @BeforeEach
    void setUp() {
        task = new TaskDefinition();
        task.setWorkerGroup("workers");
    }

    @Test
    void sqlEmailRequiresProjectAlertGroupGrant() {
        task.setTaskType("SQL");
        task.setTaskParams("{\"sendEmail\":true,\"groupId\":9}");
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.ALERT_GROUP, "9"))
                .thenReturn(true);
        checker.checkPermission(100L, Arrays.asList(task, task));
        Mockito.verify(resourceAuthorizationDao, Mockito.times(2))
                .isAuthorized(100L, ProjectResourceType.ALERT_GROUP, "9");
    }

    @Test
    void sqlWithoutEmailDoesNotUseStoredAlertGroup() {
        task.setTaskType("SQL");
        task.setTaskParams("{\"sendEmail\":false,\"groupId\":9}");
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        checker.checkPermission(100L, Collections.singletonList(task));
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers");
        Mockito.verifyNoMoreInteractions(resourceAuthorizationDao);
    }

    @ParameterizedTest
    @ValueSource(strings = {"K8S", "KUBEFLOW", "SPARK"})
    void namespaceGrantMustMatchBothNameAndCluster(String taskType) {
        task.setTaskType(taskType);
        task.setTaskParams("{\"namespace\":\"{\\\"name\\\":\\\"jobs\\\",\\\"cluster\\\":\\\"production\\\"}\"}");
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        K8sNamespace namespace = new K8sNamespace();
        namespace.setNamespace("jobs");
        namespace.setClusterName("development");
        Mockito.when(namespaceDao.queryByProject(100L)).thenReturn(Collections.singletonList(namespace));
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));

        namespace.setClusterName("production");
        checker.checkPermission(100L, Arrays.asList(task, task));
        Mockito.verify(namespaceDao, Mockito.times(2)).queryByProject(100L);
    }

    @Test
    void ungrantedNamespaceIsRejected() {
        task.setTaskType("K8S");
        task.setTaskParams("{\"namespace\":\"{\\\"name\\\":\\\"jobs\\\",\\\"cluster\\\":\\\"production\\\"}\"}");
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Mockito.when(namespaceDao.queryByProject(100L)).thenReturn(Collections.emptyList());
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
    }

    @Test
    void namespaceWithoutClusterCannotUseSameNamedGrant() {
        task.setTaskType("K8S");
        task.setTaskParams("{\"namespace\":\"{\\\"name\\\":\\\"jobs\\\"}\"}");
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
        Mockito.verifyNoInteractions(namespaceDao);
    }

    @Test
    void workerGroupMustBeGrantedToDestinationProject() {
        task.setProjectCode(200L);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        checker.checkPermission(100L, Collections.singletonList(task));
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(200L, Collections.singletonList(task)));
    }

    @Test
    void defaultWorkerGroupAlsoRequiresExplicitGrant() {
        task.setWorkerGroup(null);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "default");
    }

    @Test
    void environmentCodeIsResolvedToResourceIdAndSharedReferencesAreDeduplicated() {
        task.setEnvironmentCode(900L);
        task.setTaskGroupId(7);
        Environment environment = new Environment();
        environment.setId(42);
        environment.setCode(900L);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Mockito.when(environmentDao.queryByEnvironmentCode(900L)).thenReturn(Optional.of(environment));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.ENVIRONMENT, "42"))
                .thenReturn(true);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.TASK_GROUP, "7"))
                .thenReturn(true);

        checker.checkPermission(100L, Arrays.asList(task, task));

        Mockito.verify(environmentDao).queryByEnvironmentCode(900L);
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers");
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.ENVIRONMENT, "42");
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.TASK_GROUP, "7");
        Mockito.verify(datasourcePermissionChecker).checkPermission(100L, Arrays.asList(task, task));
    }

    @Test
    void missingEnvironmentIsRejected() {
        task.setEnvironmentCode(900L);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Mockito.when(environmentDao.queryByEnvironmentCode(900L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
    }

    @Test
    void sharedTaskGroupStillRequiresProjectGrant() {
        task.setTaskGroupId(7);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(task)));
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.TASK_GROUP, "7");
    }

    @Test
    void unsetEnvironmentAndTaskGroupDoNotRequireGrants() {
        task.setEnvironmentCode(-1L);
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        checker.checkPermission(100L, Collections.singletonList(task));
        Mockito.verifyNoInteractions(environmentDao);
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers");
        Mockito.verifyNoMoreInteractions(resourceAuthorizationDao);
    }
    @Test
    void executionSettingsRequireTenantQueueAndAlertGrants() {
        Tenant tenant = new Tenant();
        tenant.setId(3);
        tenant.setQueueId(4);
        Mockito.when(tenantDao.queryByCode("tenant")).thenReturn(Optional.of(tenant));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "workers"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "workers", null, "tenant", 5));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.TENANT, "3"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "workers", null, "tenant", 5));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.QUEUE, "4"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "workers", null, "tenant", 5));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.ALERT_GROUP, "5"))
                .thenReturn(true);
        Assertions.assertDoesNotThrow(
                () -> checker.checkExecutionSettings(100L, "workers", null, "tenant", 5));
    }

    @Test
    void executionWorkerGroupCannotBypassTaskResourceGrants() {
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "other-workers", null, "tenant", null));
        Mockito.verify(resourceAuthorizationDao).isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "other-workers");
        Mockito.verifyNoInteractions(tenantDao);
    }

    @Test
    void defaultTenantExecutionStillRequiresTenantAndQueueGrants() {
        Tenant tenant = new Tenant();
        tenant.setId(-1);
        tenant.setTenantCode("default");
        tenant.setQueueId(1);
        Mockito.when(tenantDao.queryByCode("default")).thenReturn(Optional.of(tenant));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "default"))
                .thenReturn(true);

        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "default", null, "default", null));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.TENANT, "-1"))
                .thenReturn(true);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "default", null, "default", null));
        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.QUEUE, "1"))
                .thenReturn(true);

        Assertions.assertDoesNotThrow(
                () -> checker.checkExecutionSettings(100L, "default", null, "default", null));

        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.TENANT, "-1"))
                .thenReturn(false);
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionSettings(100L, "default", null, "default", null));
    }

}
