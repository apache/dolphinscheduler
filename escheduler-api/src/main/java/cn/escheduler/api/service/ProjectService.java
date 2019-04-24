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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.mapper.ProjectMapper;
import cn.escheduler.dao.mapper.ProjectUserMapper;
import cn.escheduler.dao.mapper.UserMapper;
import cn.escheduler.dao.model.Project;
import cn.escheduler.dao.model.ProjectUser;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.escheduler.api.utils.CheckUtils.checkDesc;

/**
 * project service
 */
@Service
public class ProjectService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsersService userService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    /**
     * create project
     *
     * @param loginUser
     * @param name
     * @param desc
     * @return
     */
    public Map<String, Object> createProject(User loginUser, String name, String desc) {

        Map<String, Object> result = new HashMap<>(5);
        Map<String, Object> descCheck = checkDesc(desc);
        if (descCheck.get(Constants.STATUS) != Status.SUCCESS) {
            return descCheck;
        }

        /**
         * only general users can create projects. administrators have no corresponding tenants and can only view
         * 管理员没有对应的租户,只能查看,只有普通用户才可以创建项目
         */
        if (!userService.isGeneral(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Project project = projectMapper.queryByName(name);
        if (project != null) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, name);
            return result;
        }
        project = new Project();
        Date now = new Date();

        project.setName(name);
        project.setDesc(desc);
        project.setUserId(loginUser.getId());
        project.setUserName(loginUser.getUserName());
        project.setCreateTime(now);
        project.setUpdateTime(now);

        if (projectMapper.insert(project) > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * query project details by id
     *
     * @param projectId
     * @return
     */
    public Map<String, Object> queryById(Integer projectId) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryById(projectId);

        if (project != null) {
            result.put(Constants.DATA_LIST, project);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectId);
        }
        return result;
    }

    /**
     * check project and authorization
     * 检查项目权限
     *
     * @param loginUser
     * @param project
     * @param projectName
     * @return
     */
    public Map<String, Object> checkProjectAndAuth(User loginUser, Project project, String projectName) {

        Map<String, Object> result = new HashMap<>(5);

        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        } else if (!checkReadPermission(loginUser, project)) {
            // check read permission
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), projectName);
        }else {
            putMsg(result, Status.SUCCESS);
        }


        return result;
    }

    /**
     * admin can view all projects
     * 如果是管理员,则所有项目都可见
     *
     * @param loginUser
     * @param pageSize
     * @param pageNo
     * @param searchVal
     * @return
     */
    public Map<String, Object> queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal) {
        Map<String, Object> result = new HashMap<>();
        int count = 0;
        PageInfo pageInfo = new PageInfo<Project>(pageNo, pageSize);
        List<Project> projectList = null;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            count = projectMapper.countAllProjects(searchVal);
            projectList = projectMapper.queryAllProjectListPaging(pageInfo.getStart(), pageSize, searchVal);
            for (Project project : projectList) {
                project.setPerm(cn.escheduler.common.Constants.DEFAULT_ADMIN_PERMISSION);
            }

        } else {
            count = projectMapper.countProjects(loginUser.getId(), searchVal);
            projectList = projectMapper.queryProjectListPaging(loginUser.getId(),
                    pageInfo.getStart(), pageSize, searchVal);
        }
        pageInfo.setTotalCount(count);
        pageInfo.setLists(projectList);
        result.put(Constants.COUNT, count);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * delete project by id
     *
     * @param loginUser
     * @param projectId
     * @return
     */
    public Map<String, Object> deleteProject(User loginUser, Integer projectId) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryById(projectId);
        Map<String, Object> checkResult = getCheckResult(loginUser, project);
        if (checkResult != null) {
            return checkResult;
        }

        int delete = projectMapper.delete(projectId);
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * get check result
     *
     * @param loginUser
     * @param project
     * @return
     */
    private Map<String, Object> getCheckResult(User loginUser, Project project) {
        Map<String, Object> checkResult = checkProjectAndAuth(loginUser, project, project.getName());
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }
        return null;
    }

    /**
     * updateProcessInstance project
     *
     * @param loginUser
     * @param projectId
     * @param projectName
     * @param desc
     * @return
     */
    public Map<String, Object> update(User loginUser, Integer projectId, String projectName, String desc) {
        Map<String, Object> result = new HashMap<>(5);

        Project project = projectMapper.queryById(projectId);
        Map<String, Object> checkResult = getCheckResult(loginUser, project);
        if (checkResult != null) {
            return checkResult;
        }
        Project tempProject = projectMapper.queryByName(projectName);
        if (tempProject != null && tempProject.getId() != projectId) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, projectName);
            return result;
        }
        project.setName(projectName);
        project.setDesc(desc);
        project.setUpdateTime(new Date());

        int update = projectMapper.update(project);
        if (update > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_PROJECT_ERROR);
        }
        return result;
    }


    /**
     * query unauthorized project
     *
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> queryUnauthorizedProject(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        /**
         * query all project list except specified userId
         */
        List<Project> projectList = projectMapper.queryProjectExceptUserId(userId);
        List<Project> resultList = new ArrayList<>();
        Set<Project> projectSet = null;
        if (projectList != null && projectList.size() > 0) {
            projectSet = new HashSet<>(projectList);

            List<Project> authedProjectList = projectMapper.authedProject(userId);

            resultList = getUnauthorizedProjects(projectSet, authedProjectList);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * get unauthorized project
     *
     * @param projectSet
     * @param authedProjectList
     * @return
     */
    private List<Project> getUnauthorizedProjects(Set<Project> projectSet, List<Project> authedProjectList) {
        List<Project> resultList;
        Set<Project> authedProjectSet = null;
        if (authedProjectList != null && authedProjectList.size() > 0) {
            authedProjectSet = new HashSet<>(authedProjectList);
            projectSet.removeAll(authedProjectSet);

        }
        resultList = new ArrayList<>(projectSet);
        return resultList;
    }


    /**
     * query authorized project
     *
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> queryAuthorizedProject(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        if (checkAdmin(loginUser, result)) {
            return result;
        }

        List<Project> projects = projectMapper.authedProject(userId);
        result.put(Constants.DATA_LIST, projects);
        putMsg(result,Status.SUCCESS);

        return result;
    }


    /**
     * check whether have read permission
     *
     * @param user
     * @param project
     * @return
     */
    private boolean checkReadPermission(User user, Project project) {
        int permissionId = queryPermission(user, project);
        return (permissionId & cn.escheduler.common.Constants.READ_PERMISSION) != 0;
    }

    /**
     * query permission id
     *
     * @param user
     * @param project
     * @return
     */
    private int queryPermission(User user, Project project) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return cn.escheduler.common.Constants.READ_PERMISSION;
        }

        if (project.getUserId() == user.getId()) {
            return cn.escheduler.common.Constants.ALL_PERMISSIONS;
        }

        ProjectUser projectUser = projectUserMapper.query(project.getId(), user.getId());

        if (projectUser == null) {
            return 0;
        }

        return projectUser.getPerm();

    }

}
