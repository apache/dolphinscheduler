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
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * project controller
 */
@Api(tags = "PROJECT_TAG")
@RestController
@RequestMapping("projects")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    /**
     * create project
     *
     * @param loginUser login user
     * @param projectName project name
     * @param description description
     * @return returns an error if it exists
     */
    @ApiOperation(value = "create", notes = "CREATE_PROJECT_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectName", value = "PROJECT_NAME", dataType = "String"),
        @ApiImplicitParam(name = "description", value = "PROJECT_DESC", dataType = "String")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> result = projectService.createProject(loginUser, projectName, description);
        return returnDataList(result);
    }

    /**
     * update project
     *
     * @param loginUser login user
     * @param code project code
     * @param projectName project name
     * @param description description
     * @return update result code
     */
    @ApiOperation(value = "update", notes = "UPDATE_PROJECT_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataType = "Long", example = "123456"),
        @ApiImplicitParam(name = "projectName", value = "PROJECT_NAME", dataType = "String"),
        @ApiImplicitParam(name = "description", value = "PROJECT_DESC", dataType = "String"),
        @ApiImplicitParam(name = "userName", value = "USER_NAME", dataType = "String"),
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("code") Long code,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "userName") String userName) {
        Map<String, Object> result = projectService.update(loginUser, code, projectName, description, userName);
        return returnDataList(result);
    }

    /**
     * query project details by code
     *
     * @param loginUser login user
     * @param code project code
     * @return project detail information
     */
    @ApiOperation(value = "queryProjectByCode", notes = "QUERY_PROJECT_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataType = "Long", example = "123456")
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_DETAILS_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable("code") long code) {
        Map<String, Object> result = projectService.queryByCode(loginUser, code);
        return returnDataList(result);
    }

    /**
     * query project list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which the login user have permission to see
     */
    @ApiOperation(value = "queryProjectListPaging", notes = "QUERY_PROJECT_LIST_PAGING_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "10"),
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "searchVal", required = false) String searchVal,
                                         @RequestParam("pageSize") Integer pageSize,
                                         @RequestParam("pageNo") Integer pageNo
    ) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = projectService.queryProjectListPaging(loginUser, pageSize, pageNo, searchVal);
        return result;
    }

    /**
     * delete project by code
     *
     * @param loginUser login user
     * @param code project code
     * @return delete result code
     */
    @ApiOperation(value = "delete", notes = "DELETE_PROJECT_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataType = "Long", example = "123456")
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("code") Long code) {
        Map<String, Object> result = projectService.deleteProject(loginUser, code);
        return returnDataList(result);
    }

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return the projects which user have not permission to see
     */
    @ApiOperation(value = "queryUnauthorizedProject", notes = "QUERY_UNAUTHORIZED_PROJECT_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/unauth-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UNAUTHORIZED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUnauthorizedProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {
        Map<String, Object> result = projectService.queryUnauthorizedProject(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @ApiOperation(value = "queryAuthorizedProject", notes = "QUERY_AUTHORIZED_PROJECT_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_PROJECT)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuthorizedProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("userId") Integer userId) {
        Map<String, Object> result = projectService.queryAuthorizedProject(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * query authorized user
     *
     * @param loginUser     login user
     * @param projectCode   project code
     * @return users        who have permission for the specified project
     */
    @ApiOperation(value = "queryAuthorizedUser", notes = "QUERY_AUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataType = "Long", example = "100")
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_USER)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuthorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam("projectCode") Long projectCode) {
        Map<String, Object> result = this.projectService.queryAuthorizedUser(loginUser, projectCode);
        return this.returnDataList(result);
    }

    /**
     * query authorized and user created project
     *
     * @param loginUser login user
     * @return projects which the user create and authorized
     */
    @ApiOperation(value = "queryProjectCreatedAndAuthorizedByUser", notes = "QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_NOTES")
    @GetMapping(value = "/created-and-authed")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectCreatedAndAuthorizedByUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        return returnDataList(result);
    }

    /**
     * query all project list
     *
     * @param loginUser login user
     * @return all project list
     */
    @ApiOperation(value = "queryAllProjectList", notes = "QUERY_ALL_PROJECT_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProjectList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = projectService.queryAllProjectList(loginUser);
        return returnDataList(result);
    }
}
