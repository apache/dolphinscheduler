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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.executor.workflow.BackfillWorkflowExecutorDelegate;
import org.apache.dolphinscheduler.api.executor.workflow.ExecutorClient;
import org.apache.dolphinscheduler.api.executor.workflow.StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation;
import org.apache.dolphinscheduler.api.executor.workflow.TriggerWorkflowExecutorDelegate;
import org.apache.dolphinscheduler.api.permission.TaskResourcePermissionChecker;
import org.apache.dolphinscheduler.api.permission.TaskSubWorkflowPermissionChecker;
import org.apache.dolphinscheduler.api.service.impl.ExecutorServiceImpl;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowRequestTransformer;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowRequestTransformer;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExecutorServiceTest {

    @InjectMocks
    private ExecutorServiceImpl executorService;

    @Mock
    private ProjectAuthorizationService projectAuthorizationService;

    @Mock
    private TaskResourcePermissionChecker taskResourcePermissionChecker;

    @Mock
    private TaskSubWorkflowPermissionChecker taskSubWorkflowPermissionChecker;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private TriggerWorkflowRequestTransformer triggerWorkflowRequestTransformer;

    @Mock
    private TriggerWorkflowDTOValidator triggerWorkflowDTOValidator;

    @Mock
    private BackfillWorkflowRequestTransformer backfillWorkflowRequestTransformer;

    @Mock
    private BackfillWorkflowDTOValidator backfillWorkflowDTOValidator;

    @Mock
    private ExecutorClient executorClient;

    @Mock
    private TriggerWorkflowExecutorDelegate triggerWorkflowExecutorDelegate;

    @Mock
    private BackfillWorkflowExecutorDelegate backfillWorkflowExecutorDelegate;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private ProcessService processService;

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("test");
        user.setUserType(UserType.GENERAL_USER);
        return user;
    }

    @Test
    public void testTriggerWorkflowDefinition_userHasNoProjectExecutePermission() {
        long projectCode = 100L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowTriggerRequest request = WorkflowTriggerRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM,
                loginUser.getUserName(), projectCode))
                        .when(projectAuthorizationService)
                        .requirePermission(loginUser, projectCode, ProjectPermission.EXECUTE);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.triggerWorkflowDefinition(request));
        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), ex.getCode());

        verify(triggerWorkflowRequestTransformer, never()).transform(Mockito.any());
        verify(triggerWorkflowDTOValidator, never()).validate(Mockito.any());
    }

    @Test
    public void testTriggerWorkflowDefinition_workflowNotInRequestProject() {
        long projectCode = 100L;
        long otherProjectCode = 999L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowTriggerRequest request = WorkflowTriggerRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(workflowDefinitionCode);
        workflowDefinition.setProjectCode(otherProjectCode);
        TriggerWorkflowDTO dto = TriggerWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(workflowDefinition)
                .build();
        when(triggerWorkflowRequestTransformer.transform(request)).thenReturn(dto);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.triggerWorkflowDefinition(request));
        assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), ex.getCode());

        verify(triggerWorkflowDTOValidator, never()).validate(Mockito.any());
    }

    @Test
    public void testBackfillWorkflowDefinition_userHasNoProjectExecutePermission() {
        long projectCode = 100L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowBackFillRequest request = WorkflowBackFillRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM,
                loginUser.getUserName(), projectCode))
                        .when(projectAuthorizationService)
                        .requirePermission(loginUser, projectCode, ProjectPermission.EXECUTE);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.backfillWorkflowDefinition(request));
        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), ex.getCode());

        verify(backfillWorkflowRequestTransformer, never()).transform(Mockito.any());
        verify(backfillWorkflowDTOValidator, never()).validate(Mockito.any());
    }

    @Test
    public void testBackfillWorkflowDefinition_workflowNotInRequestProject() {
        long projectCode = 100L;
        long otherProjectCode = 999L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowBackFillRequest request = WorkflowBackFillRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(workflowDefinitionCode);
        workflowDefinition.setProjectCode(otherProjectCode);
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(workflowDefinition)
                .build();
        when(backfillWorkflowRequestTransformer.transform(request)).thenReturn(dto);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.backfillWorkflowDefinition(request));
        assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), ex.getCode());

        verify(backfillWorkflowDTOValidator, never()).validate(Mockito.any());
    }

    @Test
    public void testControlWorkflowInstance_userHasNoProjectExecutePermission() {
        long projectCode = 100L;
        int workflowInstanceId = 1;
        User loginUser = getLoginUser();
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setProjectCode(projectCode);
        when(workflowInstanceDao.queryOptionalById(workflowInstanceId)).thenReturn(Optional.of(workflowInstance));
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM))
                .when(projectAuthorizationService).requirePermission(loginUser, projectCode, ProjectPermission.EXECUTE);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.controlWorkflowInstance(
                        loginUser, workflowInstanceId, ExecuteType.REPEAT_RUNNING));

        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), ex.getCode());
        Mockito.verifyNoInteractions(executorClient);
    }

    @Test
    public void testExecuteTask_userHasNoProjectExecutePermission() {
        long projectCode = 100L;
        User loginUser = getLoginUser();
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM))
                .when(projectAuthorizationService).requirePermission(loginUser, projectCode, ProjectPermission.EXECUTE);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.executeTask(loginUser, projectCode, 1, "1", TaskDependType.TASK_POST));

        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), ex.getCode());
        Mockito.verifyNoInteractions(processService);
    }

    @Test
    public void testForceStartTaskInstance_userHasNoProjectExecutePermission() {
        long projectCode = 100L;
        int workflowInstanceId = 1;
        int queueId = 2;
        User loginUser = getLoginUser();
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setWorkflowInstanceId(workflowInstanceId);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setProjectCode(projectCode);
        when(taskGroupQueueMapper.selectById(queueId)).thenReturn(taskGroupQueue);
        when(workflowInstanceDao.queryOptionalById(workflowInstanceId)).thenReturn(Optional.of(workflowInstance));
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM))
                .when(projectAuthorizationService).requirePermission(loginUser, projectCode, ProjectPermission.EXECUTE);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.forceStartTaskInstance(loginUser, queueId));

        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), ex.getCode());
        verify(taskGroupQueueMapper, never()).updateById(Mockito.any());
    }
    @Test
    public void testExecuteTaskRejectsInstanceFromAnotherProject() {
        User loginUser = getLoginUser();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(200L);
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.of(instance));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> executorService.executeTask(loginUser, 100L, 1, "1", TaskDependType.TASK_POST));

        assertEquals(Status.WORKFLOW_INSTANCE_NOT_EXIST.getCode(), exception.getCode());
        verify(projectAuthorizationService).requirePermission(loginUser, 100L, ProjectPermission.EXECUTE);
        Mockito.verifyNoInteractions(executorClient);
    }

    @ParameterizedTest
    @EnumSource(value = ExecuteType.class, names = {"REPEAT_RUNNING", "START_FAILURE_TASK_PROCESS",
            "RECOVER_SUSPENDED_PROCESS"})
    public void testRerunChecksPinnedTaskVersionBeforeSubmittingCommand(ExecuteType executeType) {
        User user = getLoginUser();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(100L);
        instance.setWorkflowDefinitionCode(200L);
        instance.setWorkflowDefinitionVersion(3);
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(300L);
        task.setVersion(2);
        when(workflowInstanceDao.queryOptionalById(1)).thenReturn(Optional.of(instance));
        when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(200L, 3))
                .thenReturn(Collections.singletonList(task));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskResourcePermissionChecker).checkPermission(100L, Collections.singletonList(task));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> executorService.controlWorkflowInstance(user, 1, executeType));

        assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(executorClient);
        verify(taskDefinitionLogDao).queryByWorkflowDefinitionCodeAndVersion(200L, 3);
    }

    @Test
    public void testRerunRejectsRevokedSubWorkflowPermissionBeforeSubmittingCommand() {
        User user = getLoginUser();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(100L);
        instance.setWorkflowDefinitionCode(200L);
        instance.setWorkflowDefinitionVersion(3);
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setTaskType("SUB_WORKFLOW");
        task.setTaskParams("{\"workflowDefinitionCode\":400}");
        when(workflowInstanceDao.queryOptionalById(1)).thenReturn(Optional.of(instance));
        when(taskDefinitionLogDao.queryByWorkflowDefinitionCodeAndVersion(200L, 3))
                .thenReturn(Collections.singletonList(task));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskSubWorkflowPermissionChecker).checkExecutionPermission(user, Collections.singletonList(task),
                        instance.getWorkerGroup(), instance.getEnvironmentCode(), instance.getTenantCode(),
                        instance.getWarningGroupId());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> executorService.controlWorkflowInstance(user, 1, ExecuteType.REPEAT_RUNNING));

        assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(executorClient);
    }

    @Test
    public void testStopDoesNotRequireResourceGrants() {
        User user = getLoginUser();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(100L);
        when(workflowInstanceDao.queryOptionalById(1)).thenReturn(Optional.of(instance));
        StopWorkflowInstanceOperation operation =
                Mockito.mock(StopWorkflowInstanceOperation.class, Mockito.RETURNS_SELF);
        when(executorClient.stopWorkflowInstance()).thenReturn(operation);

        executorService.controlWorkflowInstance(user, 1, ExecuteType.STOP);

        verify(projectAuthorizationService).requirePermission(user, 100L, ProjectPermission.EXECUTE);
        verify(operation).execute();
        Mockito.verifyNoInteractions(taskDefinitionLogDao, taskResourcePermissionChecker,
                taskSubWorkflowPermissionChecker);
    }

}
