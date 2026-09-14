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
import static org.mockito.Mockito.doNothing;
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
import org.apache.dolphinscheduler.api.executor.workflow.TriggerWorkflowExecutorDelegate;
import org.apache.dolphinscheduler.api.service.impl.ExecutorServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowRequestTransformer;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowRequestTransformer;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ProjectServiceImpl projectService;

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
    public void testTriggerWorkflowDefinition_userHasNoProjectWritePermission() {
        long projectCode = 100L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowTriggerRequest request = WorkflowTriggerRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM,
                loginUser.getUserName(), projectCode))
                        .when(projectService)
                        .checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.triggerWorkflowDefinition(request));
        assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());

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

        doNothing().when(projectService)
                .checkHasProjectWritePermissionThrowException(loginUser, projectCode);

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
    public void testBackfillWorkflowDefinition_userHasNoProjectWritePermission() {
        long projectCode = 100L;
        long workflowDefinitionCode = 200L;
        User loginUser = getLoginUser();

        WorkflowBackFillRequest request = WorkflowBackFillRequest.builder()
                .loginUser(loginUser)
                .projectCode(projectCode)
                .workflowDefinitionCode(workflowDefinitionCode)
                .build();

        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM,
                loginUser.getUserName(), projectCode))
                        .when(projectService)
                        .checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.backfillWorkflowDefinition(request));
        assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());

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

        doNothing().when(projectService)
                .checkHasProjectWritePermissionThrowException(loginUser, projectCode);

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
    public void testControlWorkflowInstance_userHasNoProjectWritePermission() {
        long projectCode = 100L;
        int workflowInstanceId = 1;
        User loginUser = getLoginUser();
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setProjectCode(projectCode);
        when(workflowInstanceDao.queryOptionalById(workflowInstanceId)).thenReturn(Optional.of(workflowInstance));
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.controlWorkflowInstance(
                        loginUser, workflowInstanceId, ExecuteType.REPEAT_RUNNING));

        assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());
        Mockito.verifyNoInteractions(executorClient);
    }

    @Test
    public void testExecuteTask_userHasNoProjectWritePermission() {
        long projectCode = 100L;
        User loginUser = getLoginUser();
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.executeTask(loginUser, projectCode, 1, "1", TaskDependType.TASK_POST));

        assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());
        Mockito.verifyNoInteractions(processService);
    }

    @Test
    public void testForceStartTaskInstance_userHasNoProjectWritePermission() {
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
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(loginUser, projectCode);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> executorService.forceStartTaskInstance(loginUser, queueId));

        assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());
        verify(taskGroupQueueMapper, never()).updateById(Mockito.any());
    }
}
