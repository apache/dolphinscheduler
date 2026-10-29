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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_CREATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkflowDefinitionCount;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class ProjectServiceImpl extends BaseServiceImpl implements ProjectService {

    @Lazy
    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectResourceAuthorizationDao projectResourceAuthorizationDao;

    @Autowired
    private ProjectUserPermissionDao projectUserPermissionDao;

    @Autowired
    private ProjectAuthorizationService projectAuthorizationService;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private UserDao userDao;

    /**
     * create project
     *
     * @param loginUser login user
     * @param name      project name
     * @param desc      description
     * @return returns an error if it exists
     */
    @Override
    @Transactional
    public Result createProject(User loginUser, String name, String desc) {
        Result result = new Result();

        checkDesc(result, desc);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.PROJECTS, PROJECT_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Project project = projectDao.queryByName(name);
        if (project != null) {
            log.warn("Project {} already exists.", project.getName());
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, name);
            return result;
        }

        Date now = new Date();

        project = Project
                .builder()
                .name(name)
                .code(CodeGenerateUtils.genCode())
                .description(desc)
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();

        if (projectDao.insert(project) > 0) {
            for (ProjectPermission permission : ProjectPermission.values()) {
                ProjectUserPermission grant = new ProjectUserPermission();
                grant.setProjectCode(project.getCode());
                grant.setUserId(loginUser.getId());
                grant.setPermission(permission);
                grant.setCreateTime(now);
                projectUserPermissionDao.insert(grant);
            }
            log.info("Project is created and id is :{}", project.getId());
            result.setData(project);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project create error, projectName:{}.", project.getName());
            putMsg(result, Status.CREATE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * check project description
     *
     * @param result
     * @param desc   desc
     */
    public static void checkDesc(Result result, String desc) {
        if (!StringUtils.isEmpty(desc) && desc.codePointCount(0, desc.length()) > 255) {
            result.setCode(Status.DESCRIPTION_TOO_LONG_ERROR.getCode());
            result.setMsg(Status.DESCRIPTION_TOO_LONG_ERROR.getMsg());
        } else {
            result.setCode(Status.SUCCESS.getCode());
        }
    }

    /**
     * query project details by code
     *
     * @param projectCode project code
     * @return project detail information
     */
    @Override
    public Result queryByCode(User loginUser, long projectCode) {
        Result result = new Result();
        Project project = projectDao.queryByCode(projectCode);
        checkProjectReadPermissionThrowException(loginUser, project);
        result.setData(project);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Project queryByName(User loginUser, String projectName) {
        Project project = projectDao.queryByName(projectName);
        checkProjectReadPermissionThrowException(loginUser, project);
        return project;
    }

    private void checkProjectReadPermissionThrowException(User loginUser, Project project) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        projectAuthorizationService.requirePermission(loginUser, project.getCode(), ProjectPermission.VIEW);
    }

    @Override
    public void checkProjectPermission(User loginUser, Project project, ProjectPermission permission) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        projectAuthorizationService.requirePermission(loginUser, project.getCode(), permission);
    }

    @Override
    public void checkProjectPermission(User loginUser, long projectCode, ProjectPermission permission) {
        projectAuthorizationService.requirePermission(loginUser, projectCode, permission);
    }

    /**
     * admin can view all projects
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return project list which the login user have permission to see
     */
    @Override
    public Result queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal) {
        Result result = new Result();
        PageInfo<Project> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<Project> page = new Page<>(pageNo, pageSize);
        IPage<Project> projectIPage = projectDao.queryVisibleProjectListPaging(
                page, loginUser.getId(), loginUser.getUserType() == UserType.ADMIN_USER, searchVal);

        pageInfo.setTotal((int) projectIPage.getTotal());
        List<Project> projectList = projectIPage.getRecords();
        if (CollectionUtils.isEmpty(projectList)) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<User> userList = userDao.queryByIds(projectList.stream()
                .map(Project::getUserId).distinct().collect(Collectors.toList()));
        Map<Integer, String> userMap = userList.stream().collect(Collectors.toMap(User::getId, User::getUserName));
        List<Long> projectCodes = projectList.stream().map(Project::getCode).distinct().collect(Collectors.toList());
        Map<Long, Integer> projectWorkflowDefinitionCountMap = workflowDefinitionDao
                .queryProjectWorkflowDefinitionCountByProjectCodes(projectCodes)
                .stream()
                .collect(Collectors.toMap(ProjectWorkflowDefinitionCount::getProjectCode,
                        ProjectWorkflowDefinitionCount::getCount));
        Map<Long, Set<ProjectPermission>> permissions = loginUser.getUserType() == UserType.ADMIN_USER
                ? Collections.emptyMap()
                : projectUserPermissionDao.queryByProjectsAndUser(projectCodes, loginUser.getId()).stream()
                        .collect(Collectors.groupingBy(ProjectUserPermission::getProjectCode,
                                Collectors.mapping(ProjectUserPermission::getPermission, Collectors.toSet())));
        for (Project project : projectList) {
            project.setPermissions(loginUser.getUserType() == UserType.ADMIN_USER
                    ? EnumSet.allOf(ProjectPermission.class)
                    : permissions.getOrDefault(project.getCode(), Collections.emptySet()));
            project.setUserName(userMap.get(project.getUserId()));
            project.setDefCount(projectWorkflowDefinitionCountMap.getOrDefault(project.getCode(), 0));
        }
        pageInfo.setTotalList(projectList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * admin can view all projects
     *
     * @param userId    user id
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return project list which with the login user's authorized level
     */
    @Override
    public Result queryProjectWithAuthorizedLevelListPaging(Integer userId, User loginUser, Integer pageSize,
                                                            Integer pageNo, String searchVal) {
        Result result = new Result();
        PageInfo<Project> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<Project> page = new Page<>(pageNo, pageSize);
        IPage<Project> projectIPage = projectDao.queryProjectAuthorizationLevelsPaging(page, loginUser.getId(),
                loginUser.getUserType() == UserType.ADMIN_USER, userId, searchVal);
        List<Project> projectList = projectIPage.getRecords();

        pageInfo.setTotal((int) projectIPage.getTotal());
        pageInfo.setTotalList(projectList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete project by code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return delete result code
     */
    @Override
    @Transactional
    public Result deleteProject(User loginUser, Long projectCode) {
        Result result = new Result();
        Project project = projectDao.queryByCode(projectCode);

        checkProjectOwnerOrAdministrator(loginUser, project);

        List<WorkflowDefinition> workflowDefinitionList =
                workflowDefinitionDao.queryAllDefinitionList(project.getCode());

        if (!workflowDefinitionList.isEmpty()) {
            log.warn("Please delete the workflow definitions in project first! project code:{}.", projectCode);
            putMsg(result, Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL);
            return result;
        }
        boolean delete = projectDao.deleteById(project.getId());
        if (delete) {
            ProjectUserPermission memberPermissions = new ProjectUserPermission();
            memberPermissions.setProjectCode(projectCode);
            projectUserPermissionDao.deleteByCondition(memberPermissions);
            ProjectResourceAuthorization resources = new ProjectResourceAuthorization();
            resources.setProjectCode(projectCode);
            projectResourceAuthorizationDao.deleteByCondition(resources);
            log.info("Project is deleted and id is :{}.", project.getId());
            result.setData(Boolean.TRUE);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project delete error, project code:{}, project name:{}.", projectCode, project.getName());
            putMsg(result, Status.DELETE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * updateWorkflowInstance project
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param projectName project name
     * @param desc        description
     * @return update result code
     */
    @Override
    public Result update(User loginUser, Long projectCode, String projectName, String desc) {
        Result result = new Result();

        checkDesc(result, desc);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        Project project = projectDao.queryByCode(projectCode);
        checkProjectOwnerOrAdministrator(loginUser, project);
        Project tempProject = projectDao.queryByName(projectName);
        if (tempProject != null && tempProject.getCode() != project.getCode()) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, projectName);
            return result;
        }
        User user = userDao.queryById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }
        project.setName(projectName);
        project.setDescription(desc);
        project.setUpdateTime(new Date());
        boolean update = projectDao.updateById(project);
        if (update) {
            log.info("Project is updated and id is :{}", project.getId());
            result.setData(project);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project update error, projectCode:{}, projectName:{}.", project.getCode(), project.getName());
            putMsg(result, Status.UPDATE_PROJECT_ERROR);
        }
        return result;
    }

    private void checkProjectOwnerOrAdministrator(User loginUser, Project project) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        if (loginUser.getUserType() != UserType.ADMIN_USER
                && !Objects.equals(loginUser.getId(), project.getUserId())) {
            throw new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(),
                    project.getCode());
        }
    }

    /**
     * query all project with authorized level
     *
     * @param loginUser login user
     * @return project list
     */
    @Override
    public Result queryProjectWithAuthorizedLevel(User loginUser, Integer userId) {
        return Result.success(projectDao.queryProjectAuthorizationLevels(loginUser.getId(),
                loginUser.getUserType() == UserType.ADMIN_USER, userId));
    }

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return the projects which user have not permission to see
     */
    @Override
    public Result queryUnauthorizedProject(User loginUser, Integer userId) {
        List<Project> projects = projectDao.queryProjectAuthorizationLevels(loginUser.getId(),
                loginUser.getUserType() == UserType.ADMIN_USER, userId);
        return Result.success(projects.stream().filter(project -> project.getPerm() == 0).collect(Collectors.toList()));
    }

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @Override
    public Result queryAuthorizedProject(User loginUser, Integer userId) {
        if (Objects.equals(loginUser.getId(), userId)) {
            return Result.success(projectDao.queryVisibleProjects(userId, false));
        }
        List<Project> projects = projectDao.queryProjectAuthorizationLevels(loginUser.getId(),
                loginUser.getUserType() == UserType.ADMIN_USER, userId);
        return Result.success(projects.stream().filter(project -> project.getPerm() != 0).collect(Collectors.toList()));
    }

    /**
     * query authorized user
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return users        who have permission for the specified project
     */
    @Override
    public Result queryAuthorizedUser(User loginUser, Long projectCode) {
        Result result = new Result();

        // 1. check read permission
        Project project = this.projectDao.queryByCode(projectCode);
        this.checkProjectPermission(loginUser, project, ProjectPermission.VIEW);

        // 2. query authorized user list
        List<User> users = this.userDao.queryAuthedUserListByProjectId(project.getId());
        result.setData(users);
        this.putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<Project> queryProjectCreatedByUser(User loginUser) {
        return projectDao.queryProjectCreatedByUser(loginUser.getId());
    }

    /**
     * query authorized and user create project list by user
     *
     * @param loginUser login user
     * @return project list
     */
    @Override
    public Result queryProjectCreatedAndAuthorizedByUser(User loginUser) {
        Result result = new Result();

        List<Project> projects =
                projectDao.queryVisibleProjects(loginUser.getId(), loginUser.getUserType() == UserType.ADMIN_USER);

        result.setData(projects);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query all project list
     *
     * @param user
     * @return project list
     */
    @Override
    public Result queryAllProjectList(User user) {
        Result result = new Result();
        List<Project> projects =
                projectDao.queryVisibleProjects(user.getId(), user.getUserType() == UserType.ADMIN_USER);

        result.setData(projects);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all project for dependent node
     *
     * @return project list
     */
    @Override
    public Result queryAllProjectListForDependent(User loginUser) {
        Result result = new Result<>();
        List<Project> projects =
                projectDao.queryVisibleProjects(loginUser.getId(), loginUser.getUserType() == UserType.ADMIN_USER)
                        .stream()
                        .map(project -> Project.builder().code(project.getCode()).name(project.getName()).build())
                        .collect(Collectors.toList());
        result.setData(projects);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<Long> getAuthorizedProjectCodes(User loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser can not be null");
        }
        return projectDao.queryVisibleProjectCodes(loginUser.getId(), loginUser.getUserType() == UserType.ADMIN_USER);
    }
}
