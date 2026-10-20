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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class TaskDatasourcePermissionCheckerTest {

    private static final int USER_ID = 1;
    private static final int DATASOURCE_ID = 42;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private TaskDatasourcePermissionChecker taskDatasourcePermissionChecker;

    private User loginUser;

    @BeforeEach
    public void setUp() {
        taskDatasourcePermissionChecker = new TaskDatasourcePermissionChecker(resourcePermissionCheckService);
        loginUser = new User();
        loginUser.setId(USER_ID);
        loginUser.setUserType(UserType.GENERAL_USER);
    }

    @Test
    public void shouldRejectUnauthorizedDatasourceReferencedByTask() {
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(
                eq(AuthorizationType.DATASOURCE), any(Object[].class), eq(USER_ID), any(Logger.class)))
                .thenReturn(false);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDatasourcePermissionChecker.checkPermission(
                        loginUser, Collections.singletonList(getRemoteShellTaskDefinition())));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        ArgumentCaptor<Object[]> datasourceIdsCaptor = ArgumentCaptor.forClass(Object[].class);
        Mockito.verify(resourcePermissionCheckService).resourcePermissionCheck(
                eq(AuthorizationType.DATASOURCE), datasourceIdsCaptor.capture(), eq(USER_ID), any(Logger.class));
        Assertions.assertArrayEquals(new Integer[]{DATASOURCE_ID}, datasourceIdsCaptor.getValue());
    }

    @Test
    public void shouldAllowAuthorizedDatasourceReferencedByTask() {
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(
                eq(AuthorizationType.DATASOURCE), any(Object[].class), eq(USER_ID), any(Logger.class)))
                .thenReturn(true);

        Assertions.assertDoesNotThrow(() -> taskDatasourcePermissionChecker.checkPermission(
                loginUser, Collections.singletonList(getRemoteShellTaskDefinition())));
    }

    @Test
    public void shouldSkipPermissionCheckForTaskWithoutDatasource() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setTaskParams("{\"rawScript\":\"echo test\"}");

        taskDatasourcePermissionChecker.checkPermission(loginUser, Collections.singletonList(taskDefinition));

        Mockito.verifyNoInteractions(resourcePermissionCheckService);
    }

    private TaskDefinition getRemoteShellTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("REMOTESHELL");
        taskDefinition.setTaskParams(
                "{\"rawScript\":\"echo test\",\"type\":\"SSH\",\"datasource\":" + DATASOURCE_ID + "}");
        return taskDefinition;
    }
}
