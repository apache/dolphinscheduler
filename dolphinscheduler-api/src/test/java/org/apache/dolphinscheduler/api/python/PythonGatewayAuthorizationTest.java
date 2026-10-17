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

package org.apache.dolphinscheduler.api.python;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PythonGatewayAuthorizationTest {

    @InjectMocks
    private PythonGateway gateway;
    @Mock
    private UsersService usersService;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectDao projectDao;
    @Mock
    private ProjectUserDao legacyProjectUserDao;

    @Test
    void existingProjectRequiresDevelopmentPermissionWithoutAutomaticGrant() {
        User user = new User();
        user.setId(1);
        Project project = new Project();
        project.setCode(100L);
        project.setUserId(2);
        Mockito.when(usersService.queryUser("user")).thenReturn(user);
        Mockito.when(projectDao.queryByName("project")).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectPermission(user, project, ProjectPermission.DEVELOP);
        Assertions.assertThrows(ServiceException.class, () -> gateway.createOrGrantProject("user", "project", ""));
        Mockito.verify(projectService).checkProjectPermission(user, project, ProjectPermission.DEVELOP);
        Mockito.verifyNoInteractions(legacyProjectUserDao);
    }

    @Test
    void missingProjectUsesProjectCreationPermissionInitialization() {
        User user = new User();
        Mockito.when(usersService.queryUser("user")).thenReturn(user);
        gateway.createOrGrantProject("user", "project", "description");
        Mockito.verify(projectService).createProject(user, "project", "description");
        Mockito.verifyNoInteractions(legacyProjectUserDao);
    }
}
