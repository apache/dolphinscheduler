
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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_UPDATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * project service test
 **/
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceTest.class);
    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger projectLogger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectUserMapper projectUserMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private String projectName = "ProjectServiceTest";

    private String userName = "ProjectServiceTest";

    @Test
    public void testCreateProject() {

        User loginUser = getLoginUser();
        loginUser.setId(1);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, null, 1,
                PROJECT_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, null, 1,
                baseServiceLogger)).thenReturn(true);
        Result result = projectService.createProject(loginUser, projectName, getDesc());
        logger.info(result.toString());
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), 10001);

        // project name exist
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject());
        result = projectService.createProject(loginUser, projectName, projectName);
        logger.info(result.toString());
        Assertions.assertEquals(Status.PROJECT_ALREADY_EXISTS.getCode(), result.getCode().intValue());

        // success
        Mockito.when(projectMapper.insert(Mockito.any(Project.class))).thenReturn(1);
        result = projectService.createProject(loginUser, "test", "test");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

    @Test
    public void testCheckProjectAndAuth() {

        long projectCode = 1L;
        User loginUser = getLoginUser();

        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, null, projectCode, PROJECT);
        logger.info(result.toString());
        Status status = (Status) result.get(Constants.STATUS);
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST, result.get(Constants.STATUS));

        Project project = getProject();
        // USER_NO_OPERATION_PROJECT_PERM
        project.setUserId(2);
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, PROJECT);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM, result.get(Constants.STATUS));

        // success
        project.setUserId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                project.getUserId(), PROJECT, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                0, baseServiceLogger)).thenReturn(true);
        result = projectService.checkProjectAndAuth(loginUser, project, projectCode, PROJECT);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        Map<String, Object> result2 = new HashMap<>();

        result2 = projectService.checkProjectAndAuth(loginUser, null, projectCode, PROJECT);
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST, result2.get(Constants.STATUS));

        Project project1 = getProject();
        // USER_NO_OPERATION_PROJECT_PERM
        project1.setUserId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                loginUser.getId(), PROJECT, baseServiceLogger)).thenReturn(true);
        result2 = projectService.checkProjectAndAuth(loginUser, project1, projectCode, PROJECT);
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM, result2.get(Constants.STATUS));

        // success
        project1.setUserId(1);
        projectService.checkProjectAndAuth(loginUser, project1, projectCode, PROJECT);

    }

    @Test
    public void testHasProjectAndPerm() {

        // Mockito.when(projectUserMapper.queryProjectRelation(1, 1)).thenReturn(getProjectUser());
        User loginUser = getLoginUser();
        Project project = getProject();
        Map<String, Object> result = new HashMap<>();
        // not exist user
        User tempUser = new User();
        tempUser.setId(Integer.MAX_VALUE);
        tempUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                tempUser.getId(), null, baseServiceLogger)).thenReturn(true);
        boolean checkResult = projectService.hasProjectAndPerm(tempUser, project, result, null);
        logger.info(result.toString());
        Assertions.assertFalse(checkResult);

        // success
        result = new HashMap<>();
        project.setUserId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                loginUser.getId(), null, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{project.getId()},
                0, baseServiceLogger)).thenReturn(true);
        checkResult = projectService.hasProjectAndPerm(loginUser, project, result, null);
        logger.info(result.toString());
        Assertions.assertTrue(checkResult);
    }

    @Test
    public void testDeleteProject() {
        User loginUser = getLoginUser();
        Mockito.when(projectMapper.queryByCode(1L)).thenReturn(getProject());
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{1}, loginUser.getId(),
                PROJECT_DELETE, baseServiceLogger)).thenReturn(true);
        // PROJECT_NOT_FOUNT
        Result result = projectService.deleteProject(loginUser, 11L);
        logger.info(result.toString());
        Assertions.assertTrue(Status.PROJECT_NOT_EXIST.getCode() == result.getCode());
        loginUser.setId(2);
        // USER_NO_OPERATION_PROJECT_PERM
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                loginUser.getId(),
                baseServiceLogger)).thenReturn(true);
        result = projectService.deleteProject(loginUser, 1L);
        logger.info(result.toString());
        Assertions.assertTrue(Status.USER_NO_OPERATION_PROJECT_PERM.getCode() == result.getCode());

        // DELETE_PROJECT_ERROR_DEFINES_NOT_NULL
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(1L)).thenReturn(getProcessDefinitions());
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(1);
        Mockito.when(
                resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1}, 0,
                        baseServiceLogger))
                .thenReturn(true);
        result = projectService.deleteProject(loginUser, 1L);
        logger.info(result.toString());
        Assertions.assertTrue(Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL.getCode() == result.getCode());

        // success
        Mockito.when(projectMapper.deleteById(1)).thenReturn(1);
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(1L)).thenReturn(new ArrayList<>());
        result = projectService.deleteProject(loginUser, 1L);
        logger.info(result.toString());
        Assertions.assertTrue(Status.SUCCESS.getCode() == result.getCode());
    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        loginUser.setId(1);
        Project project = getProject();
        project.setCode(2L);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS,
                new Object[]{1}, loginUser.getId(),
                PROJECT_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                loginUser.getId(),
                baseServiceLogger)).thenReturn(true);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectMapper.queryByCode(2L)).thenReturn(getProject());
        // PROJECT_NOT_FOUNT
        Result result = projectService.update(loginUser, 1L, projectName, "desc", "testUser");
        logger.info(result.toString());
        Assertions.assertTrue(Status.PROJECT_NOT_FOUND.getCode() == result.getCode());

        // PROJECT_ALREADY_EXISTS
        result = projectService.update(loginUser, 2L, projectName, "desc", userName);
        logger.info(result.toString());
        Assertions.assertTrue(Status.PROJECT_ALREADY_EXISTS.getCode() == result.getCode());

        Mockito.when(userMapper.queryByUserNameAccurately(Mockito.any())).thenReturn(null);
        result = projectService.update(loginUser, 2L, "test", "desc", "testuser");
        Assertions.assertTrue(Status.USER_NOT_EXIST.getCode() == result.getCode());

        // success
        Mockito.when(userMapper.queryByUserNameAccurately(Mockito.any())).thenReturn(new User());
        project.setUserId(1);
        Mockito.when(projectMapper.updateById(Mockito.any(Project.class))).thenReturn(1);
        result = projectService.update(loginUser, 2L, "test", "desc", "testUser");
        logger.info(result.toString());
        Assertions.assertTrue(Status.SUCCESS.getCode() == result.getCode());

    }

    @Test
    public void testQueryAuthorizedProject() {
        Mockito.when(projectMapper.queryAuthedProjectListByUserId(2)).thenReturn(getList());

        User loginUser = getLoginUser();

        // test admin user
        loginUser.setUserType(UserType.ADMIN_USER);
        Result result = projectService.queryAuthorizedProject(loginUser, 2);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

        // test non-admin user
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setId(3);
        result = projectService.queryAuthorizedProject(loginUser, 2);
        projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));
    }

    @Test
    public void testQueryAuthorizedUser() {
        final User loginUser = this.getLoginUser();

        // Failure 1: PROJECT_NOT_FOUND
        Result result = this.projectService.queryAuthorizedUser(loginUser, 3682329499136L);
        logger.info("FAILURE 1: {}", result.toString());
        Assertions.assertTrue(Status.PROJECT_NOT_FOUND.getCode() == result.getCode());

        // Failure 2: USER_NO_OPERATION_PROJECT_PERM
        loginUser.setId(100);
        Mockito.when(this.projectMapper.queryByCode(Mockito.anyLong())).thenReturn(this.getProject());
        result = this.projectService.queryAuthorizedUser(loginUser, 3682329499136L);
        logger.info("FAILURE 2: {}", result.toString());
        Assertions.assertTrue(Status.USER_NO_OPERATION_PROJECT_PERM.getCode() == result.getCode());

        // SUCCESS
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(
                resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                        loginUser.getId(), PROJECT, baseServiceLogger))
                .thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                0, baseServiceLogger)).thenReturn(true);
        Mockito.when(this.userMapper.queryAuthedUserListByProjectId(1)).thenReturn(this.getUserList());
        result = this.projectService.queryAuthorizedUser(loginUser, 3682329499136L);
        logger.info("SUCCESS 1: {}", result.toString());
        List<User> users = (List<User>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(users));

        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(
                resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                        loginUser.getId(), PROJECT, baseServiceLogger))
                .thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                1, baseServiceLogger)).thenReturn(true);
        result = this.projectService.queryAuthorizedUser(loginUser, 3682329499136L);
        logger.info("SUCCESS 2: {}", result.toString());
        users = (List<User>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(users));
    }

    @Test
    public void testQueryCreatedProject() {

        User loginUser = getLoginUser();

        Mockito.when(projectMapper.queryProjectCreatedByUser(1)).thenReturn(getList());

        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = projectService.queryProjectCreatedByUser(loginUser);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryProjectCreatedAndAuthorizedByUser() {

        Result result;
        User loginUser = getLoginUser();
        Set<Integer> set = new HashSet();
        set.add(1);
        List<Integer> list = new ArrayList<>(1);
        list.add(1);
        // not admin user
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS,
                loginUser.getId(), projectLogger)).thenReturn(set);
        Mockito.when(projectMapper.selectBatchIds(set)).thenReturn(getList());
        result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> notAdminUserResult = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(notAdminUserResult));

        // admin user
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS,
                loginUser.getId(), projectLogger)).thenReturn(set);
        Mockito.when(projectMapper.selectBatchIds(set)).thenReturn(getList());
        result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> projects = (List<Project>) result.getData();

        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryAllProjectList() {
        Mockito.when(projectMapper.queryAllProject(0)).thenReturn(getList());

        User user = new User();
        user.setId(0);
        Result result = projectService.queryAllProjectList(user);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }
    @Test
    public void queryAllProjectListForDependent() {
        Mockito.when(projectMapper.queryAllProjectForDependent()).thenReturn(getList());

        Result result = projectService.queryAllProjectListForDependent();
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryUnauthorizedProject() {
        Set<Integer> set = new HashSet();
        set.add(1);
        // test admin user
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(1);
        List<Integer> list = new ArrayList<>(1);
        list.add(1);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS,
                loginUser.getId(), projectLogger)).thenReturn(set);
        Mockito.when(projectMapper.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(), list))
                .thenReturn(getList());
        Result result = projectService.queryUnauthorizedProject(loginUser, 2);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS,
                loginUser.getId(), projectLogger)).thenReturn(set);
        Mockito.when(projectMapper.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(), list))
                .thenReturn(getList());
        result = projectService.queryUnauthorizedProject(loginUser, 3);
        logger.info(result.toString());
        projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));
    }

    private Project getProject() {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private Project getProject(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setCode(1L);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private List<Project> getList() {
        List<Project> list = new ArrayList<>();
        list.add(getProject(1));
        list.add(getProject(2));
        list.add(getProject(3));
        return list;
    }

    private List<Project> getSingleList() {
        return Collections.singletonList(getProject(2));
    }

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * Get general user
     *
     * @return
     */
    private User getGeneralUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    /**
     * Get user list
     *
     * @return
     */
    private List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(this.getGeneralUser());
        return userList;
    }

    /**
     * get project user
     */
    private ProjectUser getProjectUser() {
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(1);
        projectUser.setUserId(1);
        return projectUser;
    }

    private List<ProcessDefinition> getProcessDefinitions() {
        List<ProcessDefinition> list = new ArrayList<>();
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(1L);
        list.add(processDefinition);
        return list;
    }

    private List<Integer> getProjectIds() {
        return Collections.singletonList(1);
    }

    private String getDesc() {
        return "projectUserMapper.deleteProjectRelation(projectId,userId)projectUserMappe"
                + ".deleteProjectRelation(projectId,userId)projectUserMappe"
                + "r.deleteProjectRelation(projectId,userId)projectUserMapper"
                + ".deleteProjectRelation(projectId,userId)projectUserMapper.deleteProjectRelation(projectId,userId)";
    }

}
