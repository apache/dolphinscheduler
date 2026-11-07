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
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;

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
public class TaskDatasourcePermissionCheckerTest {

    @Mock
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;

    private TaskDatasourcePermissionChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new TaskDatasourcePermissionChecker(resourceAuthorizationDao);
    }

    @Test
    public void shouldRejectDatasourceNotGrantedToTargetProject() {
        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Collections.singletonList(remoteShellTask())));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(resourceAuthorizationDao).queryByProjectAndType(100L, ProjectResourceType.DATASOURCE);
    }

    @Test
    public void shouldUseDestinationProjectInsteadOfSubmittedTaskProject() {
        TaskDefinition task = remoteShellTask();
        task.setProjectCode(200L);
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.DATASOURCE))
                .thenReturn(Collections.singletonList(datasourceGrant(42)));
        Assertions.assertDoesNotThrow(() -> checker.checkPermission(100L, Collections.singletonList(task)));
        Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(200L, Collections.singletonList(task)));
    }

    @Test
    public void shouldCheckSharedDatasourceOnlyOncePerRequest() {
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.DATASOURCE))
                .thenReturn(Collections.singletonList(datasourceGrant(42)));
        checker.checkPermission(100L, Arrays.asList(remoteShellTask(), remoteShellTask()));
        Mockito.verify(resourceAuthorizationDao).queryByProjectAndType(100L, ProjectResourceType.DATASOURCE);
    }

    @Test
    public void shouldSkipDatasourceCheckForTaskWithoutDatasource() {
        TaskDefinition task = new TaskDefinition();
        task.setTaskType("SHELL");
        task.setTaskParams("{\"rawScript\":\"echo test\"}");
        checker.checkPermission(100L, Collections.singletonList(task));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @Test
    public void shouldSkipDatasourceCheckWhenNoTasksAreSubmitted() {
        checker.checkPermission(100L, null);
        checker.checkPermission(100L, Collections.emptyList());
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @Test
    public void shouldRejectUnauthorizedDatasourceAlongsideTaskWithoutDatasource() {
        TaskDefinition shellTask = new TaskDefinition();
        shellTask.setTaskType("SHELL");
        shellTask.setTaskParams("{\"rawScript\":\"echo test\"}");

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Arrays.asList(shellTask, remoteShellTask())));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(resourceAuthorizationDao).queryByProjectAndType(100L, ProjectResourceType.DATASOURCE);
    }

    @Test
    public void shouldRejectRequestWhenOnlySomeDatasourcesAreGranted() {
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.DATASOURCE))
                .thenReturn(Collections.singletonList(datasourceGrant(42)));
        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> checker.checkPermission(100L, Arrays.asList(remoteShellTask(42), remoteShellTask(43))));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(resourceAuthorizationDao).queryByProjectAndType(100L, ProjectResourceType.DATASOURCE);
    }

    @Test
    public void shouldAcceptAllGrantedDatasourcesWithOneProjectGrantQuery() {
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.DATASOURCE))
                .thenReturn(Arrays.asList(datasourceGrant(42), datasourceGrant(43)));
        Assertions.assertDoesNotThrow(
                () -> checker.checkPermission(100L, Arrays.asList(remoteShellTask(42), remoteShellTask(43))));
        Mockito.verify(resourceAuthorizationDao).queryByProjectAndType(100L, ProjectResourceType.DATASOURCE);
    }

    private ProjectResourceAuthorization datasourceGrant(int datasourceId) {
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setProjectCode(100L);
        grant.setResourceType(ProjectResourceType.DATASOURCE);
        grant.setResourceKey(Integer.toString(datasourceId));
        return grant;
    }

    private TaskDefinition remoteShellTask() {
        return remoteShellTask(42);
    }

    private TaskDefinition remoteShellTask(int datasourceId) {
        TaskDefinition task = new TaskDefinition();
        task.setTaskType("REMOTESHELL");
        task.setTaskParams("{\"rawScript\":\"echo test\",\"type\":\"SSH\",\"datasource\":" + datasourceId + "}");
        return task;
    }
}
