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

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * project service
 **/
public interface ProjectService {

    /**
     * create project
     *
     * @param loginUser login user
     * @param name project name
     * @param desc description
     * @return returns an error if it exists
     */
    Result createProject(User loginUser, String name, String desc);

    /**
     * query project details by code
     *
     * @param projectCode project code
     * @return project detail information
     */
    Result queryByCode(User loginUser, long projectCode);

    /**
     * query project details by name
     *
     * @param loginUser login user
     * @param projectName project name
     * @return project detail information
     */
    Map<String, Object> queryByName(User loginUser, String projectName);

    /**
     * check project and authorization
     *
     * @param loginUser login user
     * @param project project
     * @param projectCode project code
     * @param perm String
     * @return true if the login user have permission to see the project
     */
    Map<String, Object> checkProjectAndAuth(User loginUser, Project project, long projectCode, String perm);

    void checkProjectAndAuthThrowException(User loginUser, Project project, String permission) throws ServiceException;

    void checkProjectAndAuthThrowException(User loginUser, long projectCode, String permission) throws ServiceException;

    boolean hasProjectAndPerm(User loginUser, Project project, Map<String, Object> result, String perm);

    /**
     * has project and permission
     *
     * @param loginUser  login user
     * @param project    project
     * @param result     result
     * @param permission String
     * @return true if the login user have permission to the project
     */
    boolean hasProjectAndPerm(User loginUser, Project project, Result result, String permission);

    boolean hasProjectAndWritePerm(User loginUser, Project project, Result result);

    boolean hasProjectAndWritePerm(User loginUser, Project project, Map<String, Object> result);

    /**
     * admin can view all projects
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which the login user have permission to see
     */
    Result queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal);

    /**
     * admin can view all projects
     *
     * @param userId user id
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which with the login user's authorized level
     */
    Result queryProjectWithAuthorizedLevelListPaging(Integer userId, User loginUser, Integer pageSize, Integer pageNo,
                                                     String searchVal);

    /**
     * delete project by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return delete result code
     */
    Result deleteProject(User loginUser, Long projectCode);

    /**
     * updateProcessInstance project
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param projectName project name
     * @param desc description
     * @return update result code
     */
    Result update(User loginUser, Long projectCode, String projectName, String desc);

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return the projects which user have not permission to see
     */
    Result queryUnauthorizedProject(User loginUser, Integer userId);

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    Result queryAuthorizedProject(User loginUser, Integer userId);

    /**
     * query all project with authorized level
     * @param loginUser login user
     * @return project list
     */
    Result queryProjectWithAuthorizedLevel(User loginUser, Integer userId);

    /**
     * query authorized user
     *
     * @param loginUser     login user
     * @param projectCode   project code
     * @return users        who have permission for the specified project
     */
    Result queryAuthorizedUser(User loginUser, Long projectCode);

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @return projects which the user have permission to see, Except for items created by this user
     */
    Map<String, Object> queryProjectCreatedByUser(User loginUser);

    /**
     * query all project list that have one or more process definitions.
     * @param loginUser
     * @return project list
     */
    Result queryAllProjectList(User loginUser);

    /**
     * query authorized and user create project list by user id
     * @param loginUser login user
     * @return project list
     */
    Result queryProjectCreatedAndAuthorizedByUser(User loginUser);

    /**
     * check project and authorization
     *
     * @param result result
     * @param loginUser login user
     * @param project project
     * @param projectCode project code
     * @param perm String
     * @return true if the login user have permission to see the project
     */
    void checkProjectAndAuth(Result result, User loginUser, Project project, long projectCode, String perm);

    /**
     * the project list in dependent node's permissions should not be restricted
     * @return project list
     */
    Result queryAllProjectListForDependent();
}
