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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
    private ProjectDao projectDao;

    @Mock
    private ProjectUserDao projectUserDao;

    @Mock
    private ProjectResourceAuthorizationDao projectResourceAuthorizationDao;

    @Mock
    private ProjectUserPermissionDao projectUserPermissionDao;

    @Mock
    private ProjectAuthorizationService projectAuthorizationService;

    @Mock
    private TaskGroupService taskGroupService;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private String projectName = "ProjectServiceTest";

    private String userName = "ProjectServiceTest";

    @Test
    public void testCreateProject() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, 1,
                PROJECT_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, null, 1,
                baseServiceLogger)).thenReturn(true);

        // REQUEST_PARAMS_NOT_VALID_ERROR
        Result result = projectService.createProject(loginUser, projectName, getDesc());
        logger.info(result.toString());
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), 10001);

        // PROJECT_ALREADY_EXISTS
        Mockito.when(projectDao.queryByName(projectName)).thenReturn(getProject());
        result = projectService.createProject(loginUser, projectName, projectName);
        logger.info(result.toString());
        Assertions.assertEquals(Status.PROJECT_ALREADY_EXISTS.getCode(), result.getCode().intValue());

        // success
        Mockito.when(projectDao.insert(Mockito.any(Project.class))).thenReturn(1);
        result = projectService.createProject(loginUser, "test", "test");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        Mockito.verify(projectUserPermissionDao, Mockito.times(ProjectPermission.values().length))
                .insert(Mockito.any(ProjectUserPermission.class));
        Mockito.verifyNoInteractions(projectUserDao);
    }

    @Test
    public void testCheckProjectPermissionRejectsMissingProjectAndPropagatesDenial() {
        User loginUser = getLoginUser();
        Assertions.assertThrows(ServiceException.class,
                () -> projectService.checkProjectPermission(loginUser, (Project) null, ProjectPermission.VIEW));
        Project project = getProject();
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectAuthorizationService)
                .requirePermission(loginUser, project.getCode(), ProjectPermission.VIEW);
        Assertions.assertThrows(ServiceException.class,
                () -> projectService.checkProjectPermission(loginUser, project, ProjectPermission.VIEW));
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);

    }

    @Test
    public void testCheckProjectPermissionDelegatesDevelop() {
        User loginUser = getLoginUser();
        Project project = getProject();
        projectService.checkProjectPermission(loginUser, project, ProjectPermission.DEVELOP);
        Mockito.verify(projectAuthorizationService).requirePermission(loginUser, project.getCode(),
                ProjectPermission.DEVELOP);
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);

    }

    @Test
    public void testProjectPagingPreservesReadOnlyPermission() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Project project = getProject();
        project.setUserId(2);
        project.setPerm(Constants.READ_PERMISSION);
        Page<Project> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(Collections.singletonList(project));
        Mockito.when(projectDao.queryVisibleProjectListPaging(any(Page.class), eq(user.getId()), eq(false), eq("test")))
                .thenReturn(page);

        ProjectUserPermission grant = new ProjectUserPermission();
        grant.setProjectCode(project.getCode());
        grant.setPermission(ProjectPermission.VIEW);
        Mockito.when(projectUserPermissionDao.queryByProjectsAndUser(Collections.singletonList(project.getCode()),
                user.getId()))
                .thenReturn(Collections.singletonList(grant));
        Result result = projectService.queryProjectListPaging(user, 10, 1, "test");

        PageInfo<Project> data = (PageInfo<Project>) result.getData();
        Assertions.assertEquals(1, data.getTotal());
        Assertions.assertEquals(Constants.READ_PERMISSION, data.getTotalList().get(0).getPerm());
        Assertions.assertEquals(EnumSet.of(ProjectPermission.VIEW), data.getTotalList().get(0).getPermissions());
        Mockito.verify(projectUserPermissionDao).queryByProjectsAndUser(Collections.singletonList(project.getCode()),
                user.getId());
        Mockito.verifyNoInteractions(resourcePermissionCheckService);
    }

    @Test
    public void testProjectPagingPreservesTotalForEmptyPage() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Page<Project> page = new Page<>(3, 10);
        page.setTotal(12);
        Mockito.when(projectDao.queryVisibleProjectListPaging(any(Page.class), eq(user.getId()), eq(false), eq("test")))
                .thenReturn(page);

        Result result = projectService.queryProjectListPaging(user, 10, 3, "test");

        PageInfo<Project> data = (PageInfo<Project>) result.getData();
        Assertions.assertEquals(12, data.getTotal());
        Assertions.assertTrue(data.getTotalList().isEmpty());
        Mockito.verifyNoInteractions(resourcePermissionCheckService, userDao, workflowDefinitionDao);
    }

    @Test
    public void testAdminProjectPagingUsesExplicitAdminScope() {
        User user = getLoginUser();
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectDao.queryVisibleProjectListPaging(any(Page.class), eq(user.getId()), eq(true), eq("test")))
                .thenReturn(new Page<>(1, 10));

        projectService.queryProjectListPaging(user, 10, 1, "test");

        Mockito.verify(projectDao).queryVisibleProjectListPaging(any(Page.class), eq(user.getId()), eq(true),
                eq("test"));
        Mockito.verifyNoInteractions(resourcePermissionCheckService);
    }

    @Test
    public void testProjectDetailsRequireReadPermission() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Project project = getProject();
        project.setUserId(2);
        Mockito.when(projectDao.queryByCode(project.getCode())).thenReturn(project);
        Mockito.when(projectDao.queryByName(project.getName())).thenReturn(project);
        Mockito.when(projectAuthorizationService.requirePermission(user, project.getCode(), ProjectPermission.VIEW))
                .thenThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(),
                        project.getCode()));
        Assertions.assertThrows(ServiceException.class, () -> projectService.queryByCode(user, project.getCode()));
        Assertions.assertThrows(ServiceException.class, () -> projectService.queryByName(user, project.getName()));
        Mockito.doReturn(project).when(projectAuthorizationService)
                .requirePermission(user, project.getCode(), ProjectPermission.VIEW);
        Assertions.assertEquals(project, projectService.queryByCode(user, project.getCode()).getData());
        Assertions.assertEquals(project, projectService.queryByName(user, project.getName()));
        Mockito.verifyNoInteractions(projectUserDao);
    }

    @Test
    public void testMissingProjectIsRejectedBeforeCheckingPermissions() {
        User user = getLoginUser();
        user.setUserType(UserType.ADMIN_USER);
        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> projectService.queryByCode(user, 999L));
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(projectAuthorizationService);
    }

    @Test
    public void testQueryProjectWithAuthorizedLevelListPaging() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        IPage<Project> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(Collections.singletonList(getProject()));
        Mockito.when(projectDao.queryProjectAuthorizationLevelsPaging(any(Page.class), eq(user.getId()), eq(false),
                eq(2), eq("test"))).thenReturn(page);
        Result result = projectService.queryProjectWithAuthorizedLevelListPaging(2, user, 10, 1, "test");
        PageInfo<Project> data = (PageInfo<Project>) result.getData();
        Assertions.assertEquals(1, data.getTotal());
        Assertions.assertEquals(1, data.getTotalList().size());
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);
    }

    @Test
    public void testDeleteProject() {
        User loginUser = getLoginUser();
        Project project = getProject();
        Mockito.when(projectDao.queryByCode(1L)).thenReturn(project);
        ServiceException missing = Assertions.assertThrows(ServiceException.class,
                () -> projectService.deleteProject(loginUser, 11L));
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), missing.getCode());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectAuthorizationService)
                .requirePermission(loginUser, project.getCode(), ProjectPermission.DEVELOP);
        Assertions.assertThrows(ServiceException.class, () -> projectService.deleteProject(loginUser, 1L));
        Mockito.reset(projectAuthorizationService);
        Mockito.when(workflowDefinitionDao.queryAllDefinitionList(1L)).thenReturn(getProcessDefinitions());
        Result result = projectService.deleteProject(loginUser, 1L);
        Assertions.assertEquals(Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL.getCode(), result.getCode());
        Mockito.when(projectDao.deleteById(1)).thenReturn(true);
        Mockito.when(workflowDefinitionDao.queryAllDefinitionList(1L)).thenReturn(Collections.emptyList());
        result = projectService.deleteProject(loginUser, 1L);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
        Mockito.verify(projectUserPermissionDao)
                .deleteByCondition(Mockito.argThat(grant -> grant.getProjectCode() == 1L));
        Mockito.verify(projectResourceAuthorizationDao)
                .deleteByCondition(Mockito.argThat(grant -> grant.getProjectCode() == 1L));
        Mockito.verifyNoInteractions(taskGroupService, projectUserDao);

    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        loginUser.setId(1);
        Project project = getProject();
        project.setCode(2L);
        Mockito.when(projectDao.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectDao.queryByCode(2L)).thenReturn(getProject());
        // PROJECT_NOT_EXIST
        ServiceException notFoundEx = Assertions.assertThrows(ServiceException.class,
                () -> projectService.update(loginUser, 1L, projectName, "desc"));
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), notFoundEx.getCode());

        // PROJECT_ALREADY_EXISTS
        Mockito.when(
                resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, loginUser.getId(),
                        PROJECT_UPDATE, baseServiceLogger))
                .thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, new Object[]{1},
                loginUser.getId(),
                baseServiceLogger)).thenReturn(true);
        Result result = projectService.update(loginUser, 2L, projectName, "desc");
        logger.info(result.toString());
        Assertions.assertEquals(Status.PROJECT_ALREADY_EXISTS.getCode(), result.getCode().intValue());

        // USER_NOT_EXIST
        Mockito.when(userDao.queryById(Mockito.any())).thenReturn(null);
        result = projectService.update(loginUser, 2L, "test", "desc");
        Assertions.assertEquals(Status.USER_NOT_EXIST.getCode(), result.getCode().intValue());

        // success
        Mockito.when(userDao.queryById(Mockito.any())).thenReturn(new User());
        project.setUserId(1);
        Mockito.when(projectDao.updateById(Mockito.any(Project.class))).thenReturn(true);
        result = projectService.update(loginUser, 2L, "test", "desc");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

    @Test
    public void testQueryAuthorizedProject() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Project project = getProject();
        project.setPerm(Constants.READ_PERMISSION);
        Mockito.when(projectDao.queryProjectAuthorizationLevels(user.getId(), false, 2))
                .thenReturn(Collections.singletonList(project));
        Assertions.assertEquals(Collections.singletonList(project),
                projectService.queryAuthorizedProject(user, 2).getData());
        Mockito.when(projectDao.queryVisibleProjects(user.getId(), false))
                .thenReturn(Collections.singletonList(project));
        Assertions.assertEquals(Collections.singletonList(project),
                projectService.queryAuthorizedProject(user, user.getId()).getData());
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);
    }

    @Test
    public void testQueryAuthorizedUser() {
        User user = getLoginUser();
        Assertions.assertThrows(ServiceException.class, () -> projectService.queryAuthorizedUser(user, 100L));
        Project project = getProject();
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(project);
        Mockito.when(projectAuthorizationService.requirePermission(user, project.getCode(), ProjectPermission.VIEW))
                .thenThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM));
        Assertions.assertThrows(ServiceException.class, () -> projectService.queryAuthorizedUser(user, 100L));
        Mockito.verifyNoInteractions(userDao);
        Mockito.doReturn(project).when(projectAuthorizationService)
                .requirePermission(user, project.getCode(), ProjectPermission.VIEW);
        Mockito.when(userDao.queryAuthedUserListByProjectId(project.getId())).thenReturn(getUserList());
        Assertions.assertEquals(getUserList(), projectService.queryAuthorizedUser(user, 100L).getData());
    }

    @Test
    public void testQueryCreatedProject() {

        User loginUser = getLoginUser();

        Mockito.when(projectDao.queryProjectCreatedByUser(1)).thenReturn(getList());

        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        List<Project> projects = projectService.queryProjectCreatedByUser(loginUser);
        logger.info("projects size {}", projects.size());
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryProjectCreatedAndAuthorizedByUser() {
        Result result;
        User loginUser = getLoginUser();
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectDao.queryVisibleProjects(loginUser.getId(), false)).thenReturn(getList());
        result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> notAdminUserResult = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(notAdminUserResult));

        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectDao.queryVisibleProjects(loginUser.getId(), true)).thenReturn(getList());
        result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));
        Mockito.verifyNoInteractions(resourcePermissionCheckService);
    }

    @Test
    public void testQueryAllProjectList() {
        Mockito.when(projectDao.queryVisibleProjects(0, false)).thenReturn(getList());

        User user = new User();
        user.setId(0);
        Result result = projectService.queryAllProjectList(user);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }
    @Test
    public void queryAllProjectListForDependent() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectDao.queryVisibleProjects(user.getId(), false)).thenReturn(getList());

        Result result = projectService.queryAllProjectListForDependent(user);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testAuthorizedProjectCodesAreQueriedDirectly() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectDao.queryVisibleProjectCodes(user.getId(), false))
                .thenReturn(Collections.singletonList(42L));

        Assertions.assertEquals(Collections.singletonList(42L), projectService.getAuthorizedProjectCodes(user));
        Mockito.verifyNoInteractions(resourcePermissionCheckService);
        Assertions.assertThrows(IllegalArgumentException.class, () -> projectService.getAuthorizedProjectCodes(null));
    }

    @Test
    public void testQueryProjectWithAuthorizedLevel() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectDao.queryProjectAuthorizationLevels(user.getId(), false, 2)).thenReturn(getList());
        Assertions.assertEquals(getList(), projectService.queryProjectWithAuthorizedLevel(user, 2).getData());
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);
    }

    @Test
    public void testQueryUnauthorizedProject() {
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        Project project = getProject();
        project.setPerm(0);
        Mockito.when(projectDao.queryProjectAuthorizationLevels(user.getId(), false, 2))
                .thenReturn(Collections.singletonList(project));
        Assertions.assertEquals(Collections.singletonList(project),
                projectService.queryUnauthorizedProject(user, 2).getData());
        project.setPerm(Constants.READ_PERMISSION);
        Assertions.assertTrue(((List<?>) projectService.queryUnauthorizedProject(user, 2).getData()).isEmpty());
        Mockito.verifyNoInteractions(projectUserDao, resourcePermissionCheckService);
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

    private List<WorkflowDefinition> getProcessDefinitions() {
        List<WorkflowDefinition> list = new ArrayList<>();
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setProjectCode(1L);
        list.add(workflowDefinition);
        return list;
    }

    private String getDesc() {
        return "projectUserMapper.deleteProjectRelation(projectId,userId)projectUserMappe"
                + ".deleteProjectRelation(projectId,userId)projectUserMappe"
                + "r.deleteProjectRelation(projectId,userId)projectUserMapper"
                + ".deleteProjectRelation(projectId,userId)projectUserMapper.deleteProjectRelation(projectId,userId)";
    }

}
