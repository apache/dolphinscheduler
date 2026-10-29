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
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskSubWorkflowPermissionCheckerTest {

    private static final int USER_ID = 1;
    private static final long SUB_WORKFLOW_CODE = 42L;
    private static final long ANOTHER_SUB_WORKFLOW_CODE = 43L;
    private static final long SUB_WORKFLOW_PROJECT_CODE = 99L;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private TaskResourcePermissionChecker taskResourcePermissionChecker;

    private TaskSubWorkflowPermissionChecker taskSubWorkflowPermissionChecker;

    private User loginUser;

    @BeforeEach
    public void setUp() {
        taskSubWorkflowPermissionChecker =
                new TaskSubWorkflowPermissionChecker(workflowDefinitionDao, projectService,
                        taskDefinitionLogDao, taskResourcePermissionChecker);
        loginUser = new User();
        loginUser.setId(USER_ID);
        loginUser.setUserType(UserType.GENERAL_USER);
    }

    @Test
    public void shouldRejectMissingSubWorkflow() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkPermission(
                        loginUser, Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE))));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(projectService);
    }

    @Test
    public void shouldRejectWhenSubWorkflowQueryReturnsNull() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(null);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkPermission(
                        loginUser, Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE))));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(projectService);
    }

    @Test
    public void shouldRejectUnavailableSubWorkflowProject() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        Mockito.doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService)
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.EXECUTE);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkPermission(
                        loginUser, Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE))));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
    }

    @Test
    public void shouldAllowAvailableSubWorkflowsAndCheckEachProjectOnce() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(Arrays.asList(
                        getWorkflowDefinition(SUB_WORKFLOW_CODE),
                        getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE)));

        Assertions.assertDoesNotThrow(() -> taskSubWorkflowPermissionChecker.checkPermission(
                loginUser,
                Arrays.asList(
                        getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE),
                        getSubWorkflowTaskDefinition(ANOTHER_SUB_WORKFLOW_CODE))));

        Mockito.verify(projectService, Mockito.times(1))
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.EXECUTE);
    }

    @Test
    public void executionChecksPublishedChildVersionsAndTerminatesCycles() {
        WorkflowDefinition first = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        first.setVersion(7);
        WorkflowDefinition second = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        second.setVersion(8);
        second.setProjectCode(100L);
        TaskDefinitionLog childReference = new TaskDefinitionLog();
        childReference.setTaskType("SUB_WORKFLOW");
        childReference.setTaskParams("{\"workflowDefinitionCode\":43}");
        TaskDefinitionLog cycleReference = new TaskDefinitionLog();
        cycleReference.setTaskType("SUB_WORKFLOW");
        cycleReference.setTaskParams("{\"workflowDefinitionCode\":42}");
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(first));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(second));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 7))
                .thenReturn(Collections.singletonList(childReference));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 8))
                .thenReturn(Collections.singletonList(cycleReference));

        taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser, Arrays.asList(
                getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE), getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                "parent-workers", 10L, "parent-tenant", 20);

        Mockito.verify(taskDefinitionLogDao).queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 7);
        Mockito.verify(taskDefinitionLogDao).queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 8);
        Mockito.verifyNoMoreInteractions(taskDefinitionLogDao);
        Mockito.verify(projectService).checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE,
                ProjectPermission.EXECUTE);
        Mockito.verify(projectService).checkProjectPermission(loginUser, 100L, ProjectPermission.EXECUTE);
        Mockito.verify(taskResourcePermissionChecker).checkPermission(SUB_WORKFLOW_PROJECT_CODE,
                Collections.singletonList(childReference));
        Mockito.verify(taskResourcePermissionChecker).checkPermission(100L, Collections.singletonList(cycleReference));
        Mockito.verify(taskResourcePermissionChecker).checkExecutionSettings(SUB_WORKFLOW_PROJECT_CODE,
                "parent-workers", 10L, "parent-tenant", 20);
        Mockito.verify(taskResourcePermissionChecker).checkExecutionSettings(100L,
                "parent-workers", 10L, "parent-tenant", 20);
    }

    @Test
    public void executionRejectsChildResourcesAfterGrantRevocation() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setTaskType("SHELL");
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(task));
        Mockito.doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskResourcePermissionChecker).checkPermission(SUB_WORKFLOW_PROJECT_CODE,
                        Collections.singletonList(task));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                        "parent-workers", 10L, "parent-tenant", 20));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
    }

    @Test
    public void executionRejectsRevokedChildProjectPermissionBeforeReadingTasks() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.EXECUTE);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                        "parent-workers", 10L, "parent-tenant", 20));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(taskDefinitionLogDao, taskResourcePermissionChecker);
    }

    @Test
    public void shouldSkipTaskWithoutSubWorkflow() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setTaskParams("{\"rawScript\":\"echo test\"}");

        taskSubWorkflowPermissionChecker.checkPermission(loginUser, Collections.singletonList(taskDefinition));

        Mockito.verifyNoInteractions(workflowDefinitionDao, projectService);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectResourceType.class, names = {
            "WORKER_GROUP", "ENVIRONMENT", "TENANT", "QUEUE", "ALERT_GROUP"})
    public void executionRejectsInheritedResourceMissingFromChildProject(
                                                                         ProjectResourceType missingType) {
        ProjectResourceAuthorizationDao grants = Mockito.mock(
                ProjectResourceAuthorizationDao.class);
        TenantDao tenants = Mockito.mock(
                TenantDao.class);
        IEnvironmentDao environments = Mockito.mock(
                IEnvironmentDao.class);
        Tenant tenant = new Tenant();
        tenant.setId(5);
        tenant.setQueueId(6);
        Environment environment =
                new Environment();
        environment.setId(8);
        Mockito.lenient().when(tenants.queryByCode("parent-tenant"))
                .thenReturn(Optional.of(tenant));
        Mockito.lenient().when(environments.queryByEnvironmentCode(10L))
                .thenReturn(Optional.of(environment));
        Mockito.when(grants.isAuthorized(Mockito.eq(SUB_WORKFLOW_PROJECT_CODE), Mockito.any(), Mockito.anyString()))
                .thenReturn(true);
        String missingKey;
        switch (missingType) {
            case WORKER_GROUP:
                missingKey = "parent-workers";
                break;
            case ENVIRONMENT:
                missingKey = "8";
                break;
            case TENANT:
                missingKey = "5";
                break;
            case QUEUE:
                missingKey = "6";
                break;
            default:
                missingKey = "20";
        }
        Mockito.when(grants.isAuthorized(SUB_WORKFLOW_PROJECT_CODE, missingType, missingKey)).thenReturn(false);
        TaskResourcePermissionChecker resources = new TaskResourcePermissionChecker(
                Mockito.mock(TaskDatasourcePermissionChecker.class), grants, environments, tenants,
                Mockito.mock(K8sNamespaceDao.class));
        TaskSubWorkflowPermissionChecker checker = new TaskSubWorkflowPermissionChecker(
                workflowDefinitionDao, projectService, taskDefinitionLogDao, resources);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        TaskDefinitionLog childTask = new TaskDefinitionLog();
        childTask.setTaskType("SHELL");
        childTask.setWorkerGroup("child-workers");
        Mockito.lenient().when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(childTask));
        resources.checkPermission(SUB_WORKFLOW_PROJECT_CODE, Collections.singletonList(childTask));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> checker.checkExecutionPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                        "parent-workers", 10L, "parent-tenant", 20));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(grants).isAuthorized(SUB_WORKFLOW_PROJECT_CODE, missingType, missingKey);
    }

    private TaskDefinition getSubWorkflowTaskDefinition(long workflowDefinitionCode) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("SUB_WORKFLOW");
        taskDefinition.setTaskParams("{\"workflowDefinitionCode\":" + workflowDefinitionCode + "}");
        return taskDefinition;
    }

    private WorkflowDefinition getWorkflowDefinition(long workflowDefinitionCode) {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(workflowDefinitionCode);
        workflowDefinition.setProjectCode(SUB_WORKFLOW_PROJECT_CODE);
        return workflowDefinition;
    }
}
