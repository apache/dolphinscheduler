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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_DELETE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkflowDefinitionCount;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.NonNull;
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
    private TaskGroupService taskGroupService;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private UserMapper userMapper;

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
        checkProjectAndAuthThrowException(loginUser, project, PROJECT);
        result.setData(project);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Project queryByName(User loginUser, String projectName) {
        Project project = projectDao.queryByName(projectName);
        checkProjectAndAuthThrowException(loginUser, project, PROJECT);
        return project;
    }

    @Override
    public void checkProjectAndAuthThrowException(@NonNull User loginUser, @Nullable Project project,
                                                  String permission) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS, permission)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(),
                    project.getCode());
        }
    }

    @Override
    public void checkProjectAndAuthThrowException(User loginUser, Long projectCode, String permission) {
        if (projectCode == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        Project project = projectDao.queryByCode(projectCode);
        checkProjectAndAuthThrowException(loginUser, project, permission);
    }

    @Override
    public void checkHasProjectWritePermissionThrowException(User loginUser, long projectCode) {
        Project project = projectDao.queryByCode(projectCode);
        checkHasProjectWritePermissionThrowException(loginUser, project);
    }

    @Override
    public void checkHasProjectWritePermissionThrowException(User loginUser, Project project) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, null);
        }
        // case 1: user is admin
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            return;
        }
        // case 2: user is project owner
        if (project.getUserId().equals(loginUser.getId())) {
            return;
        }
        // case 3: check user permission level
        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), loginUser.getId());
        if (projectUser == null || projectUser.getPerm() != Constants.DEFAULT_ADMIN_PERMISSION) {
            throw new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM, loginUser.getUserName(), project.getCode());
        }
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
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<Project> projectIPage =
                projectDao.queryProjectListPaging(page, new ArrayList<>(projectIds), searchVal);

        List<Project> projectList = projectIPage.getRecords();
        if (loginUser.getUserType() != UserType.ADMIN_USER) {
            for (Project project : projectList) {
                project.setPerm(Constants.DEFAULT_ADMIN_PERMISSION);
            }
        }
        if (CollectionUtils.isEmpty(projectList)) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<User> userList = userMapper.selectByIds(projectList.stream()
                .map(Project::getUserId).distinct().collect(Collectors.toList()));
        Map<Integer, String> userMap = userList.stream().collect(Collectors.toMap(User::getId, User::getUserName));
        List<Long> projectCodes = projectList.stream().map(Project::getCode).distinct().collect(Collectors.toList());
        Map<Long, Integer> projectWorkflowDefinitionCountMap = workflowDefinitionDao
                .queryProjectWorkflowDefinitionCountByProjectCodes(projectCodes)
                .stream()
                .collect(Collectors.toMap(ProjectWorkflowDefinitionCount::getProjectCode,
                        ProjectWorkflowDefinitionCount::getCount));
        for (Project project : projectList) {
            project.setUserName(userMap.get(project.getUserId()));
            project.setDefCount(projectWorkflowDefinitionCountMap.getOrDefault(project.getCode(), 0));
        }
        pageInfo.setTotal((int) projectIPage.getTotal());
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
        Set<Integer> allProjectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        Set<Integer> userProjectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, userId, log);
        if (allProjectIds.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<Project> projectIPage =
                projectDao.queryProjectListPaging(page, new ArrayList<>(allProjectIds), searchVal);

        List<Project> projectList = projectIPage.getRecords();

        for (Project project : projectList) {
            if (userProjectIds.contains(project.getId())) {
                ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), userId);
                if (projectUser == null) {
                    // in this case, the user is the project owner, maybe it's better to set it to ALL_PERMISSION.
                    project.setPerm(Constants.DEFAULT_ADMIN_PERMISSION);
                } else {
                    project.setPerm(projectUser.getPerm());
                }
            } else {
                project.setPerm(0);
            }
        }

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
    public Result deleteProject(User loginUser, Long projectCode) {
        Result result = new Result();
        Project project = projectDao.queryByCode(projectCode);

        checkHasProjectWritePermissionThrowException(loginUser, project);
        checkProjectAndAuthThrowException(loginUser, project, PROJECT_DELETE);

        List<WorkflowDefinition> workflowDefinitionList =
                workflowDefinitionDao.queryAllDefinitionList(project.getCode());

        if (!workflowDefinitionList.isEmpty()) {
            log.warn("Please delete the workflow definitions in project first! project code:{}.", projectCode);
            putMsg(result, Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL);
            return result;
        }
        // delete the task group
        taskGroupService.deleteTaskGroupByProjectCode(project.getCode());

        boolean delete = projectDao.deleteById(project.getId());
        if (delete) {
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
        checkHasProjectWritePermissionThrowException(loginUser, project);
        Project tempProject = projectDao.queryByName(projectName);
        if (tempProject != null && tempProject.getCode() != project.getCode()) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, projectName);
            return result;
        }
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }
        project.setName(projectName);
        project.setDescription(desc);
        project.setUpdateTime(new Date());
        project.setUserId(user.getId());
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

    /**
     * query all project with authorized level
     *
     * @param loginUser login user
     * @return project list
     */
    @Override
    public Result queryProjectWithAuthorizedLevel(User loginUser, Integer userId) {
        Result result = new Result();

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        List<Project> projectList = projectDao.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(),
                new ArrayList<>(projectIds));

        List<Project> unauthorizedProjectsList = new ArrayList<>();
        List<Project> authedProjectList = new ArrayList<>();
        Set<Project> projectSet;
        if (projectList != null && !projectList.isEmpty()) {
            projectSet = new HashSet<>(projectList);
            authedProjectList = projectDao.queryAuthedProjectListByUserId(userId);
            unauthorizedProjectsList = getUnauthorizedProjects(projectSet, authedProjectList);
        }

        for (int i = 0; i < authedProjectList.size(); i++) {
            authedProjectList.get(i).setPerm(7);
        }

        for (int i = 0; i < unauthorizedProjectsList.size(); i++) {
            unauthorizedProjectsList.get(i).setPerm(0);
        }

        List<Project> joined = new ArrayList<>();
        joined.addAll(authedProjectList);
        joined.addAll(unauthorizedProjectsList);

        result.setData(joined);
        putMsg(result, Status.SUCCESS);
        return result;
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
        Result result = new Result();

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Project> projectList = projectDao.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(),
                new ArrayList<>(projectIds));

        List<Project> resultList = new ArrayList<>();
        Set<Project> projectSet;
        if (projectList != null && !projectList.isEmpty()) {
            projectSet = new HashSet<>(projectList);

            List<Project> authedProjectList = projectDao.queryAuthedProjectListByUserId(userId);

            resultList = getUnauthorizedProjects(projectSet, authedProjectList);
        }
        result.setData(resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get unauthorized project
     *
     * @param projectSet        project set
     * @param authedProjectList authed project list
     * @return project list that unauthorized
     */
    private List<Project> getUnauthorizedProjects(Set<Project> projectSet, List<Project> authedProjectList) {
        List<Project> resultList;
        Set<Project> authedProjectSet;
        if (authedProjectList != null && !authedProjectList.isEmpty()) {
            authedProjectSet = new HashSet<>(authedProjectList);
            projectSet.removeAll(authedProjectSet);
        }
        resultList = new ArrayList<>(projectSet);
        return resultList;
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
        Result result = new Result();

        List<Project> projects = projectDao.queryAuthedProjectListByUserId(userId);
        result.setData(projects);
        putMsg(result, Status.SUCCESS);

        return result;
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
        this.checkProjectAndAuthThrowException(loginUser, project, PROJECT);

        // 2. query authorized user list
        List<User> users = this.userMapper.queryAuthedUserListByProjectId(project.getId());
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

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Project> projects = projectDao.queryByIds(projectIds);

        result.setData(projects);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * check whether have read permission
     *
     * @param user    user
     * @param project project
     * @return true if the user have permission to see the project, otherwise return false
     */
    private boolean checkReadPermission(User user, Project project) {
        int permissionId = queryPermission(user, project);
        return (permissionId & Constants.READ_PERMISSION) != 0;
    }

    /**
     * query permission id
     *
     * @param user    user
     * @param project project
     * @return permission
     */
    private int queryPermission(User user, Project project) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return Constants.READ_PERMISSION;
        }

        if (Objects.equals(project.getUserId(), user.getId())) {
            return Constants.ALL_PERMISSIONS;
        }

        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());

        if (projectUser == null) {
            return 0;
        }

        return projectUser.getPerm();

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
                projectDao.queryAllProject(user.getUserType() == UserType.ADMIN_USER ? 0 : user.getId());

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
    public Result queryAllProjectListForDependent() {
        Result result = new Result<>();
        List<Project> projects =
                projectDao.queryAllProjectForDependent();
        result.setData(projects);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<Long> getAuthorizedProjectCodes(User loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser can not be null");
        }
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        return projectDao.queryByIds(projectIds)
                .stream()
                .map(Project::getCode)
                .collect(Collectors.toList());
    }
}
