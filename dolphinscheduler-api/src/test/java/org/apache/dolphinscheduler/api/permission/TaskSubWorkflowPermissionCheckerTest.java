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
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private TaskSubWorkflowPermissionChecker taskSubWorkflowPermissionChecker;

    private User loginUser;

    @BeforeEach
    public void setUp() {
        taskSubWorkflowPermissionChecker =
                new TaskSubWorkflowPermissionChecker(workflowDefinitionDao, projectService);
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
                .checkHasProjectWritePermissionThrowException(loginUser, SUB_WORKFLOW_PROJECT_CODE);

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
                .checkHasProjectWritePermissionThrowException(loginUser, SUB_WORKFLOW_PROJECT_CODE);
    }

    @Test
    public void shouldSkipTaskWithoutSubWorkflow() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setTaskParams("{\"rawScript\":\"echo test\"}");

        taskSubWorkflowPermissionChecker.checkPermission(loginUser, Collections.singletonList(taskDefinition));

        Mockito.verifyNoInteractions(workflowDefinitionDao, projectService);
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
