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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.apache.dolphinscheduler.api.utils.CheckUtils.checkDesc;

/**
 * project service
 *HttpTask./
 **/
@Service
public class ProjectService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * create project
     *
     * @param loginUser login user
     * @param name project name
     * @param desc description
     * @return returns an error if it exists
     */
    public Map<String, Object> createProject(User loginUser, String name, String desc) {

        Map<String, Object> result = new HashMap<>(5);
        Map<String, Object> descCheck = checkDesc(desc);
        if (descCheck.get(Constants.STATUS) != Status.SUCCESS) {
            return descCheck;
        }

        Project project = projectMapper.queryByName(name);
        if (project != null) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, name);
            return result;
        }
        project = new Project();
        Date now = new Date();

        project.setName(name);
        project.setDescription(desc);
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
     * @param projectId project id
     * @return project detail information
     */
    public Map<String, Object> queryById(Integer projectId) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.selectById(projectId);

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
     *
     * @param loginUser login user
     * @param project project
     * @param projectName project name
     * @return true if the login user have permission to see the project
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

    public boolean hasProjectAndPerm(User loginUser, Project project, Map<String, Object> result) {
        boolean checkResult = false;
        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, "");
        } else if (!checkReadPermission(loginUser, project)) {
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), project.getName());
        } else {
            checkResult = true;
        }
        return checkResult;
    }

    /**
     * admin can view all projects
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which the login user have permission to see
     */
    public Map<String, Object> queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal) {
        Map<String, Object> result = new HashMap<>();
        PageInfo pageInfo = new PageInfo<Project>(pageNo, pageSize);

        Page<Project> page = new Page(pageNo, pageSize);

        int userId = loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId();
        IPage<Project> projectIPage = projectMapper.queryProjectListPaging(page, userId, searchVal);

        List<Project> projectList = projectIPage.getRecords();
        if(userId != 0){
            for (Project project : projectList) {
                project.setPerm(org.apache.dolphinscheduler.common.Constants.DEFAULT_ADMIN_PERMISSION);
            }
        }
        pageInfo.setTotalCount((int)projectIPage.getTotal());
        pageInfo.setLists(projectList);
        result.put(Constants.COUNT, (int)projectIPage.getTotal());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * delete project by id
     *
     * @param loginUser login user
     * @param projectId project id
     * @return delete result code
     */
    public Map<String, Object> deleteProject(User loginUser, Integer projectId) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.selectById(projectId);
        Map<String, Object> checkResult = getCheckResult(loginUser, project);
        if (checkResult != null) {
            return checkResult;
        }

        if (!hasPerm(loginUser, project.getUserId())) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryAllDefinitionList(projectId);

        if(processDefinitionList.size() > 0){
            putMsg(result, Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL);
            return result;
        }
        int delete = projectMapper.deleteById(projectId);
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
     * @param loginUser login user
     * @param project project
     * @return check result
     */
    private Map<String, Object> getCheckResult(User loginUser, Project project) {
        String projectName = project == null ? null:project.getName();
        Map<String, Object> checkResult = checkProjectAndAuth(loginUser, project, projectName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }
        return null;
    }

    /**
     * updateProcessInstance project
     *
     * @param loginUser login user
     * @param projectId project id
     * @param projectName project name
     * @param desc description
     * @return update result code
     */
    public Map<String, Object> update(User loginUser, Integer projectId, String projectName, String desc) {
        Map<String, Object> result = new HashMap<>(5);

        Map<String, Object> descCheck = checkDesc(desc);
        if (descCheck.get(Constants.STATUS) != Status.SUCCESS) {
            return descCheck;
        }

        Project project = projectMapper.selectById(projectId);
        boolean hasProjectAndPerm = hasProjectAndPerm(loginUser, project, result);
        if (!hasProjectAndPerm) {
            return result;
        }
        Project tempProject = projectMapper.queryByName(projectName);
        if (tempProject != null && tempProject.getId() != projectId) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, projectName);
            return result;
        }
        project.setName(projectName);
        project.setDescription(desc);
        project.setUpdateTime(new Date());

        int update = projectMapper.updateById(project);
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
     * @param loginUser login user
     * @param userId user id
     * @return the projects which user have not permission to see
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

            List<Project> authedProjectList = projectMapper.queryAuthedProjectListByUserId(userId);

            resultList = getUnauthorizedProjects(projectSet, authedProjectList);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * get unauthorized project
     *
     * @param projectSet project set
     * @param authedProjectList authed project list
     * @return project list that authorization
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
     * @param loginUser login user
     * @param userId user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    public Map<String, Object> queryAuthorizedProject(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        if (checkAdmin(loginUser, result)) {
            return result;
        }

        List<Project> projects = projectMapper.queryAuthedProjectListByUserId(userId);
        result.put(Constants.DATA_LIST, projects);
        putMsg(result,Status.SUCCESS);

        return result;
    }


    /**
     * check whether have read permission
     *
     * @param user user
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
     * @param user user
     * @param project project
     * @return permission
     */
    private int queryPermission(User user, Project project) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return Constants.READ_PERMISSION;
        }

        if (project.getUserId() == user.getId()) {
            return Constants.ALL_PERMISSIONS;
        }

        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());

        if (projectUser == null) {
            return 0;
        }

        return projectUser.getPerm();

    }

    /**
     * query all project list that have one or more process definitions.
     * @return project list
     */
    public Map<String, Object> queryAllProjectList() {
        Map<String, Object> result = new HashMap<>();
        List<Project> projects = projectMapper.selectList(null);
        List<ProcessDefinition>  processDefinitions = processDefinitionMapper.selectList(null);
        if(projects != null){
            Set set = new HashSet<>();
            for (ProcessDefinition processDefinition : processDefinitions){
                set.add(processDefinition.getProjectId());
            }
            List<Project> tempDeletelist = new ArrayList<Project>();
            for (Project project : projects) {
                if(!set.contains(project.getId())){
                    tempDeletelist.add(project);
                }
            }
            projects.removeAll(tempDeletelist);
        }
        result.put(Constants.DATA_LIST, projects);
        putMsg(result,Status.SUCCESS);
        return result;
    }

}
