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
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    public void executionRejectsOfflineChildBeforeReadingItsTasks() {
        WorkflowDefinition child = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        child.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(child));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                        "parent-workers", 10L, "parent-tenant", 20));

        Assertions.assertEquals(Status.SUB_WORKFLOW_DEFINITION_NOT_RELEASE.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(taskDefinitionLogDao, taskResourcePermissionChecker);
    }

    @Test
    public void executionRejectsOfflineGrandchildOfOnlineChild() {
        WorkflowDefinition child = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        WorkflowDefinition grandchild = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        grandchild.setReleaseState(ReleaseState.OFFLINE);
        TaskDefinitionLog reference = new TaskDefinitionLog();
        reference.setTaskType("SUB_WORKFLOW");
        reference.setTaskParams("{\"workflowDefinitionCode\":43}");
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(child));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(grandchild));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(reference));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)),
                        "parent-workers", 10L, "parent-tenant", 20));

        Assertions.assertEquals(Status.SUB_WORKFLOW_DEFINITION_NOT_RELEASE.getCode(), exception.getCode());
        Mockito.verify(taskDefinitionLogDao, Mockito.never())
                .queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0);
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
            "WORKER_GROUP", "ENVIRONMENT", "TENANT", "ALERT_GROUP"})
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

    @ParameterizedTest
    @ValueSource(strings = {"save", "execute", "schedule"})
    void dependentReferencesRequireTargetProjectView(String operation) {
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.VIEW);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences(operation, getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE,
                        SUB_WORKFLOW_CODE, 0)));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        Mockito.verifyNoInteractions(taskResourcePermissionChecker, taskDefinitionLogDao);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, 7})
    void pendingDefinitionAcceptsSelfReferencesBeforeSaving(long taskCode) {
        WorkflowDefinition pending = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        TaskDefinition task = getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE, SUB_WORKFLOW_CODE, taskCode);
        task.setCode(7L);
        task.setProjectCode(SUB_WORKFLOW_PROJECT_CODE);

        Assertions.assertDoesNotThrow(() -> taskSubWorkflowPermissionChecker.checkPermission(
                loginUser, Collections.singletonList(task), pending));

        Mockito.verify(projectService).checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE,
                ProjectPermission.VIEW);
        Mockito.verifyNoInteractions(workflowDefinitionDao, taskDefinitionLogDao);
    }

    @ParameterizedTest
    @CsvSource({"98,7,99", "99,8,99", "99,7,98"})
    void pendingDefinitionRejectsInvalidSelfReference(long referenceProject, long taskCode, long taskProject) {
        WorkflowDefinition pending = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        TaskDefinition task = getDependentTaskDefinition(referenceProject, SUB_WORKFLOW_CODE, taskCode);
        task.setCode(7L);
        task.setProjectCode(taskProject);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkPermission(loginUser, Collections.singletonList(task),
                        pending));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        Mockito.verifyNoInteractions(workflowDefinitionDao, taskDefinitionLogDao);
    }

    @ParameterizedTest
    @CsvSource({"false,true", "true,false", "true,true"})
    void pendingDefinitionStillChecksExternalReferences(boolean exists, boolean viewAllowed) {
        WorkflowDefinition pending = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        WorkflowDefinition external = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        external.setProjectCode(100L);
        TaskDefinition selfTask = getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE, SUB_WORKFLOW_CODE, 0);
        TaskDefinition externalTask = getDependentTaskDefinition(100L, ANOTHER_SUB_WORKFLOW_CODE, 7L);
        TaskDefinitionLog externalMember = new TaskDefinitionLog();
        externalMember.setCode(7L);
        externalMember.setProjectCode(100L);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(exists ? Collections.singletonList(external) : Collections.emptyList());
        if (exists && !viewAllowed) {
            Mockito.doNothing().when(projectService)
                    .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.VIEW);
            Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                    .checkProjectPermission(loginUser, 100L, ProjectPermission.VIEW);
        } else if (exists) {
            Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0))
                    .thenReturn(Collections.singletonList(externalMember));
        }

        Runnable check = () -> taskSubWorkflowPermissionChecker.checkPermission(loginUser,
                Arrays.asList(selfTask, externalTask), pending);
        if (exists && viewAllowed) {
            Assertions.assertDoesNotThrow(check::run);
            Mockito.verify(taskDefinitionLogDao)
                    .queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0);
        } else {
            ServiceException error = Assertions.assertThrows(ServiceException.class, check::run);
            Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        }
        Mockito.verify(workflowDefinitionDao).queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE));
        Mockito.verifyNoMoreInteractions(workflowDefinitionDao);
    }

    @Test
    void pendingDefinitionDoesNotReplacePersistedReferencesInChildWorkflows() {
        WorkflowDefinition pending = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        WorkflowDefinition child = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(child));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(new TaskDefinitionLog(
                        getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE, SUB_WORKFLOW_CODE, 0))));

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> taskSubWorkflowPermissionChecker.checkPermission(loginUser,
                        Collections.singletonList(getSubWorkflowTaskDefinition(ANOTHER_SUB_WORKFLOW_CODE)), pending));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        Mockito.verify(workflowDefinitionDao).queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, 7})
    void dependentReferencesNeedOnlyViewAndCanReadOfflineWorkflow(long taskCode) {
        WorkflowDefinition target = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        target.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(target));
        if (taskCode > 0) {
            TaskDefinitionLog member = new TaskDefinitionLog();
            member.setCode(taskCode);
            member.setProjectCode(SUB_WORKFLOW_PROJECT_CODE);
            Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                    .thenReturn(Collections.singletonList(member));
        }

        for (String operation : Arrays.asList("save", "execute", "schedule")) {
            checkReferences(operation, getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE,
                    SUB_WORKFLOW_CODE, taskCode));
        }

        Mockito.verify(projectService, Mockito.times(3))
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.VIEW);
        Mockito.verifyNoMoreInteractions(projectService);
        Mockito.verifyNoInteractions(taskResourcePermissionChecker);
    }

    @ParameterizedTest
    @CsvSource({"98,42,0", "99,43,0", "99,42,88", "99,42,-2"})
    void dependentReferencesRejectMismatchedProjectWorkflowOrTask(long projectCode, long workflowCode,
                                                                  long taskCode) {
        if (taskCode >= -1) {
            Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(workflowCode)))
                    .thenReturn(workflowCode == SUB_WORKFLOW_CODE
                            ? Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE))
                            : Collections.emptyList());
        }
        if (taskCode > 0) {
            TaskDefinitionLog differentTask = new TaskDefinitionLog();
            differentTask.setCode(7L);
            differentTask.setProjectCode(SUB_WORKFLOW_PROJECT_CODE);
            Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                    .thenReturn(Collections.singletonList(differentTask));
        }

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences("save", getDependentTaskDefinition(projectCode, workflowCode, taskCode)));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
    }

    @Test
    void dependentTaskMustBelongToTheTargetsActualProject() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        TaskDefinitionLog misplaced = new TaskDefinitionLog();
        misplaced.setCode(7L);
        misplaced.setProjectCode(100L);
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(misplaced));

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences("save", getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE,
                        SUB_WORKFLOW_CODE, 7L)));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "not-json", "{\"dependence\":{\"dependTaskList\":[]}}",
            "{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[]}]}}",
            "{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[null]}]}}"})
    void dependentReferencesRejectInvalidDependencyStructure(String parameters) {
        TaskDefinition task = new TaskDefinition();
        task.setTaskType("DEPENDENT");
        task.setTaskParams(parameters);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences("save", task));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        Mockito.verifyNoInteractions(workflowDefinitionDao, projectService, taskDefinitionLogDao);
    }

    @ParameterizedTest
    @ValueSource(strings = {"save", "execute", "schedule"})
    void nestedDependentReferencesAreCheckedWithoutBroadeningSaveSubworkflowPermissions(String operation) {
        WorkflowDefinition child = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        WorkflowDefinition grandchild = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        grandchild.setProjectCode(100L);
        WorkflowDefinition dependency = getWorkflowDefinition(44L);
        dependency.setProjectCode(101L);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(child));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(grandchild));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(44L)))
                .thenReturn(Collections.singletonList(dependency));
        TaskDefinitionLog childReference = new TaskDefinitionLog();
        childReference.setTaskType("SUB_WORKFLOW");
        childReference.setTaskParams("{\"workflowDefinitionCode\":43}");
        TaskDefinitionLog dependentReference = new TaskDefinitionLog();
        dependentReference.setTaskType("DEPENDENT");
        dependentReference.setTaskParams(getDependentTaskDefinition(101L, 44L, 0).getTaskParams());
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(childReference));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(dependentReference));
        Mockito.doNothing().when(projectService)
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.EXECUTE);
        if (!"save".equals(operation)) {
            Mockito.doNothing().when(projectService)
                    .checkProjectPermission(loginUser, 100L, ProjectPermission.EXECUTE);
        }
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, 101L, ProjectPermission.VIEW);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences(operation, getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE)));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        if ("save".equals(operation)) {
            Mockito.verify(projectService, Mockito.never())
                    .checkProjectPermission(loginUser, 100L, ProjectPermission.EXECUTE);
        }
        Mockito.verify(projectService, Mockito.never())
                .checkProjectPermission(loginUser, 101L, ProjectPermission.EXECUTE);
    }

    @Test
    void dependentPermissionRevocationBlocksManualExecutionAfterSave() {
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));
        TaskDefinition dependency = getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE, SUB_WORKFLOW_CODE, 0);
        checkReferences("save", dependency);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PERM)).when(projectService)
                .checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE, ProjectPermission.VIEW);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> checkReferences("execute", dependency));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void dependentReferencesValidateEveryModelAndBatchTargetLookups(boolean secondTargetExists) {
        TaskDefinition task = getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE, SUB_WORKFLOW_CODE, 0);
        DependentParameters parameters = JSONUtils.parseObject(task.getTaskParams(), DependentParameters.class);
        DependentParameters second = JSONUtils.parseObject(getDependentTaskDefinition(SUB_WORKFLOW_PROJECT_CODE,
                ANOTHER_SUB_WORKFLOW_CODE, 0).getTaskParams(), DependentParameters.class);
        parameters.getDependence().getDependTaskList().addAll(second.getDependence().getDependTaskList());
        task.setTaskParams(JSONUtils.toJsonString(parameters));
        java.util.Set<Long> targets =
                new java.util.HashSet<>(Arrays.asList(SUB_WORKFLOW_CODE, ANOTHER_SUB_WORKFLOW_CODE));
        Mockito.when(workflowDefinitionDao.queryByCodes(targets)).thenReturn(secondTargetExists
                ? Arrays.asList(getWorkflowDefinition(SUB_WORKFLOW_CODE),
                        getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE))
                : Collections.singletonList(getWorkflowDefinition(SUB_WORKFLOW_CODE)));

        if (secondTargetExists) {
            checkReferences("save", task);
            Mockito.verify(projectService).checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE,
                    ProjectPermission.VIEW);
            Mockito.verifyNoMoreInteractions(projectService);
        } else {
            ServiceException error =
                    Assertions.assertThrows(ServiceException.class, () -> checkReferences("save", task));
            Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        }
        Mockito.verify(workflowDefinitionDao).queryByCodes(targets);
        Mockito.verifyNoMoreInteractions(workflowDefinitionDao);
    }

    @Test
    void savingCyclicSubworkflowsChecksNestedDependenciesAndTerminates() {
        WorkflowDefinition child = getWorkflowDefinition(SUB_WORKFLOW_CODE);
        child.setReleaseState(ReleaseState.OFFLINE);
        WorkflowDefinition grandchild = getWorkflowDefinition(ANOTHER_SUB_WORKFLOW_CODE);
        grandchild.setProjectCode(100L);
        WorkflowDefinition dependency = getWorkflowDefinition(44L);
        dependency.setProjectCode(101L);
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(child));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(ANOTHER_SUB_WORKFLOW_CODE)))
                .thenReturn(Collections.singletonList(grandchild));
        Mockito.when(workflowDefinitionDao.queryByCodes(Collections.singleton(44L)))
                .thenReturn(Collections.singletonList(dependency));
        TaskDefinitionLog next = new TaskDefinitionLog();
        next.setTaskType("SUB_WORKFLOW");
        next.setTaskParams("{\"workflowDefinitionCode\":43}");
        TaskDefinitionLog dependent = new TaskDefinitionLog();
        dependent.setTaskType("DEPENDENT");
        dependent.setTaskParams(getDependentTaskDefinition(101L, 44L, 0).getTaskParams());
        TaskDefinitionLog cycle = new TaskDefinitionLog();
        cycle.setTaskType("SUB_WORKFLOW");
        cycle.setTaskParams("{\"workflowDefinitionCode\":42}");
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0))
                .thenReturn(Arrays.asList(next, dependent));
        Mockito.when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0))
                .thenReturn(Collections.singletonList(cycle));

        checkReferences("save", getSubWorkflowTaskDefinition(SUB_WORKFLOW_CODE));

        Mockito.verify(taskDefinitionLogDao).queryByWorkflowDefinitionCodeAndVersion(SUB_WORKFLOW_CODE, 0);
        Mockito.verify(taskDefinitionLogDao).queryByWorkflowDefinitionCodeAndVersion(ANOTHER_SUB_WORKFLOW_CODE, 0);
        Mockito.verifyNoMoreInteractions(taskDefinitionLogDao);
        Mockito.verify(projectService).checkProjectPermission(loginUser, 101L, ProjectPermission.VIEW);
        Mockito.verify(projectService).checkProjectPermission(loginUser, SUB_WORKFLOW_PROJECT_CODE,
                ProjectPermission.EXECUTE);
        Mockito.verifyNoMoreInteractions(projectService);
        Mockito.verifyNoInteractions(taskResourcePermissionChecker);
    }

    private void checkReferences(String operation, TaskDefinition task) {
        if ("save".equals(operation)) {
            taskSubWorkflowPermissionChecker.checkPermission(loginUser, Collections.singletonList(task));
        } else if ("execute".equals(operation)) {
            taskSubWorkflowPermissionChecker.checkExecutionPermission(loginUser, Collections.singletonList(task),
                    "parent-workers", 10L, "parent-tenant", 20);
        } else {
            taskSubWorkflowPermissionChecker.checkResourcePermission(loginUser, Collections.singletonList(task),
                    "parent-workers", 10L, "parent-tenant", 20);
        }
    }

    private TaskDefinition getDependentTaskDefinition(long projectCode, long workflowCode, long taskCode) {
        TaskDefinition task = new TaskDefinition();
        task.setTaskType("DEPENDENT");
        task.setTaskParams("{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[{\"projectCode\":"
                + projectCode + ",\"definitionCode\":" + workflowCode + ",\"depTaskCode\":" + taskCode
                + ",\"parameterPassing\":true}]}]}}");
        return task;
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
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        return workflowDefinition;
    }
}
