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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * permission service test
 */
@ExtendWith(MockitoExtension.class)
public class ResourcePermissionCheckServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePermissionCheckServiceTest.class);

    @Mock
    private ProcessService processService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    ResourcePermissionCheckServiceImpl resourcePermissionCheckService;

    @BeforeEach
    public void setup() {
        ResourcePermissionCheckServiceImpl.RESOURCE_LIST_MAP.put(AuthorizationType.PROJECTS,
                new ResourcePermissionCheckServiceImpl.ProjectsResourcePermissionCheck(projectMapper));
    }

    @Test
    public void testResourcePermissionCheck() {
        User user = getGeneralUser();

        Assertions.assertTrue(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, null,
                user.getId(), logger));

        List<Project> projects = Arrays.asList(getProject(1), getProject(2), getProject(3));
        Mockito.when(projectMapper.listAuthorizedProjects(user.getId(), null)).thenReturn(projects);

        Assertions.assertTrue(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{}, user.getId(), logger));
        Assertions.assertTrue(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{1, 2}, user.getId(), logger));
        Assertions.assertFalse(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{1, 2, 3, 4}, user.getId(), logger));
        Assertions.assertFalse(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{2, 3, 4}, user.getId(), logger));
        Assertions.assertFalse(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{4, 5}, user.getId(), logger));
    }

    @Test
    public void testOperationPermissionCheck() {
        User user = getGeneralUser();

        Mockito.when(processService.getUserById(user.getId())).thenReturn(null);
        Assertions.assertFalse(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                user.getId(), ApiFuncIdentificationConstant.PROJECT, logger));

        Mockito.when(processService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertTrue(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                user.getId(), ApiFuncIdentificationConstant.PROJECT, logger));
    }

    @Test
    public void testUserOwnedResourceIdsAcquisition() {
        User generalUser = getGeneralUser();
        Mockito.when(processService.getUserById(generalUser.getId())).thenReturn(null);
        Assertions.assertEquals(0, resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, generalUser.getId(), logger).size());

        // GENERAL_USER
        List<Project> projects = Arrays.asList(getProject(1), getProject(2), getProject(3));
        Mockito.when(processService.getUserById(generalUser.getId())).thenReturn(generalUser);
        Mockito.when(projectMapper.listAuthorizedProjects(generalUser.getId(), null)).thenReturn(projects);
        Assertions.assertEquals(3, resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, generalUser.getId(), logger).size());

        // ADMIN_USER
        User adminUser = getAdminUser();
        Mockito.when(processService.getUserById(adminUser.getId())).thenReturn(adminUser);
        Mockito.when(projectMapper.listAuthorizedProjects(0, null)).thenReturn(projects);
        Assertions.assertEquals(3, resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, adminUser.getId(), logger).size());
    }

    private User getGeneralUser() {
        User user = new User();
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    private User getAdminUser() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    private Project getProject(int projectId) {
        Project project = new Project();
        project.setCode(1L);
        project.setId(projectId);
        project.setName("projectName");
        return project;
    }
}
