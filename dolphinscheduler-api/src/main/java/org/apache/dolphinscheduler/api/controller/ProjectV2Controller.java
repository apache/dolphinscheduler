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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUTHORIZED_PROJECT;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUTHORIZED_USER;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROJECT_DETAILS_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_UNAUTHORIZED_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROJECT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.project.ProjectCreateRequest;
import org.apache.dolphinscheduler.api.dto.project.ProjectCreateResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectDeleteResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectListPagingResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectListResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectQueryRequest;
import org.apache.dolphinscheduler.api.dto.project.ProjectQueryResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectUpdateRequest;
import org.apache.dolphinscheduler.api.dto.project.ProjectUpdateResponse;
import org.apache.dolphinscheduler.api.dto.user.UserListResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * project controller
 */
@Api(tags = "PROJECT_TAG")
@RestController
@RequestMapping("/v2/projects")
public class ProjectV2Controller extends BaseController {

    @Autowired
    private ProjectService projectService;

    /**
     * create project
     *
     * @param loginUser            login user
     * @param projectCreateRequest projectCreateRequest
     * @return ProjectResponse ProjectResponse
     */
    @ApiOperation(value = "create", notes = "CREATE_PROJECT_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectCreateResponse createProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestBody ProjectCreateRequest projectCreateRequest) {
        Result result = projectService.createProject(loginUser, projectCreateRequest.getProjectName(),
                projectCreateRequest.getDescription());
        return new ProjectCreateResponse(result);
    }

    /**
     * update project
     *
     * @param loginUser        login user
     * @param code             project code
     * @param projectUpdateReq projectUpdateRequest
     * @return result Result
     */
    @ApiOperation(value = "update", notes = "UPDATE_PROJECT_NOTES")
    @PutMapping(value = "/{code}", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectUpdateResponse updateProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @PathVariable("code") Long code,
                                               @RequestBody ProjectUpdateRequest projectUpdateReq) {
        Result result = projectService.update(loginUser, code, projectUpdateReq.getProjectName(),
                projectUpdateReq.getDescription(), projectUpdateReq.getUserName());
        return new ProjectUpdateResponse(result);
    }

    /**
     * query project details by project code
     *
     * @param loginUser login user
     * @param code      project code
     * @return project detail information
     */
    @ApiOperation(value = "queryProjectByCode", notes = "QUERY_PROJECT_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataTypeClass = long.class, example = "123456", required = true)
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_DETAILS_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectQueryResponse queryProjectByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @PathVariable("code") long code) {
        Result result = projectService.queryByCode(loginUser, code);
        return new ProjectQueryResponse(result);
    }

    /**
     * query project list paging
     *
     * @param loginUser       login user
     * @param projectQueryReq projectQueryReq
     * @return project list which the login user have permission to see
     */
    @ApiOperation(value = "queryProjectListPaging", notes = "QUERY_PROJECT_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class, example = "test"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1")
    })
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListPagingResponse queryProjectListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                            ProjectQueryRequest projectQueryReq) {
        Result result = checkPageParams(projectQueryReq.getPageNo(), projectQueryReq.getPageSize());
        if (!result.checkResult()) {
            return new ProjectListPagingResponse(result);
        }
        String searchVal = ParameterUtils.handleEscapes(projectQueryReq.getSearchVal());
        result = projectService.queryProjectListPaging(loginUser, projectQueryReq.getPageSize(),
                projectQueryReq.getPageNo(), searchVal);
        return new ProjectListPagingResponse(result);
    }

    /**
     * delete project by code
     *
     * @param loginUser login user
     * @param code      project code
     * @return delete result code
     */
    @ApiOperation(value = "delete", notes = "DELETE_PROJECT_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataTypeClass = long.class, example = "123456", required = true)
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectDeleteResponse deleteProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @PathVariable("code") Long code) {
        Result result = projectService.deleteProject(loginUser, code);
        return new ProjectDeleteResponse(result);
    }

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return the projects which user have not permission to see
     */
    @ApiOperation(value = "queryUnauthorizedProject", notes = "QUERY_UNAUTHORIZED_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataTypeClass = int.class, example = "100", required = true)
    })
    @GetMapping(value = "/unauth-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UNAUTHORIZED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListResponse queryUnauthorizedProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam("userId") Integer userId) {
        Result result = projectService.queryUnauthorizedProject(loginUser, userId);
        return new ProjectListResponse(result);
    }

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @ApiOperation(value = "queryAuthorizedProject", notes = "QUERY_AUTHORIZED_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataTypeClass = int.class, example = "100", required = true)
    })
    @GetMapping(value = "/authed-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_PROJECT)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListResponse queryAuthorizedProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @RequestParam("userId") Integer userId) {
        Result result = projectService.queryAuthorizedProject(loginUser, userId);
        return new ProjectListResponse(result);
    }

    /**
     * query authorized user
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return users        who have permission for the specified project
     */
    @ApiOperation(value = "queryAuthorizedUser", notes = "QUERY_AUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100", required = true)
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_USER)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UserListResponse queryAuthorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @RequestParam("projectCode") Long projectCode) {
        Result result = projectService.queryAuthorizedUser(loginUser, projectCode);
        return new UserListResponse(result);
    }

    /**
     * query authorized and user created project
     *
     * @param loginUser login user
     * @return projects which the user create and authorized
     */
    @ApiOperation(value = "queryProjectCreatedAndAuthorizedByUser", notes = "QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginUser", value = "LOGIN_USER", dataTypeClass = Object.class, example = "\"{id:100}\"", required = true)
    })
    @GetMapping(value = "/created-and-authed")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListResponse queryProjectCreatedAndAuthorizedByUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Result result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        return new ProjectListResponse(result);
    }

    /**
     * query all project list
     *
     * @param loginUser login user
     * @return all project list
     */
    @ApiOperation(value = "queryAllProjectList", notes = "QUERY_ALL_PROJECT_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginUser", value = "LOGIN_USER", dataTypeClass = Object.class, example = "\"{id:100}\"", required = true)
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListResponse queryAllProjectList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Result result = projectService.queryAllProjectList(loginUser);
        return new ProjectListResponse(result);
    }

    /**
     * query all project list for dependent
     *
     * @param loginUser login user
     * @return all project list
     */
    @ApiOperation(value = "queryAllProjectListForDependent", notes = "QUERY_ALL_PROJECT_LIST_FOR_DEPENDENT_NOTES")
    @GetMapping(value = "/list-dependent")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ProjectListResponse queryAllProjectListForDependent(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Result result = projectService.queryAllProjectListForDependent();
        return new ProjectListResponse(result);
    }
}
