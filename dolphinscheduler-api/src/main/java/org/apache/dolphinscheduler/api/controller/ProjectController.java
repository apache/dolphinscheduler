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
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import lombok.extern.slf4j.Slf4j;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * project controller
 */
@Tag(name = "PROJECT_TAG")
@RestController
@RequestMapping("projects")
@Slf4j
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    /**
     * create project
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param description description
     * @return returns an error if it exists
     */
    @Operation(summary = "create", description = "CREATE_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "projectName", description = "PROJECT_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "PROJECT_DESC", schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "description", required = false) String description) {
        return projectService.createProject(loginUser, projectName, description);
    }

    /**
     * update project
     *
     * @param loginUser   login user
     * @param code        project code
     * @param projectName project name
     * @param description description
     * @return update result code
     */
    @Operation(summary = "update", description = "UPDATE_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "123456")),
            @Parameter(name = "projectName", description = "PROJECT_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "PROJECT_DESC", schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("code") Long code,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "description", required = false) String description) {
        return projectService.update(loginUser, code, projectName, description);
    }

    /**
     * query project details by code
     *
     * @param loginUser login user
     * @param code      project code
     * @return project detail information
     */
    @Operation(summary = "queryProjectByCode", description = "QUERY_PROJECT_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "123456"))
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_DETAILS_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable("code") long code) {
        return projectService.queryByCode(loginUser, code);
    }

    /**
     * query project list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return project list which the login user have permission to see
     */
    @Operation(summary = "queryProjectListPaging", description = "QUERY_PROJECT_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "searchVal", required = false) String searchVal,
                                         @RequestParam("pageSize") Integer pageSize,
                                         @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            log.warn("Pagination parameters check failed, pageNo:{}, pageSize:{}", pageNo, pageSize);
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = projectService.queryProjectListPaging(loginUser, pageSize, pageNo, searchVal);
        return result;
    }

    /**
     * query project with authorized level list paging
     *
     * @param userId user id
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which with the login user's authorized level
     */
    @Operation(summary = "queryProjectWithAuthorizedLevelListPaging", description = "QUERY_PROJECT_WITH_AUTH_LEVEL_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/project-with-authorized-level-list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectWithAuthorizedLevelListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                            @RequestParam("userId") Integer userId,
                                                            @RequestParam(value = "searchVal", required = false) String searchVal,
                                                            @RequestParam("pageSize") Integer pageSize,
                                                            @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = projectService.queryProjectWithAuthorizedLevelListPaging(userId, loginUser, pageSize, pageNo,
                searchVal);
        return result;
    }

    /**
     * delete project by code
     *
     * @param loginUser login user
     * @param code      project code
     * @return delete result code
     */
    @Operation(summary = "delete", description = "DELETE_PROJECT_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "123456"))
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("code") Long code) {
        return projectService.deleteProject(loginUser, code);
    }

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return the projects which user have not permission to see
     */
    @Operation(summary = "queryUnauthorizedProject", description = "QUERY_UNAUTHORIZED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/unauth-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UNAUTHORIZED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUnauthorizedProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {
        return projectService.queryUnauthorizedProject(loginUser, userId);
    }

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId    user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @Operation(summary = "queryAuthorizedProject", description = "QUERY_AUTHORIZED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/authed-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_PROJECT)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuthorizedProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("userId") Integer userId) {
        return projectService.queryAuthorizedProject(loginUser, userId);
    }

    /**
     * query all project with authorized level
     *
     * @param loginUser login user
     * @param userId user id
     * @return All projects with users' authorized level for them
     */
    @Operation(summary = "queryProjectWithAuthorizedLevel", description = "QUERY_PROJECT_AUTHORIZED_LEVEL")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/project-with-authorized-level")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_PROJECT)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectWithAuthorizedLevel(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam("userId") Integer userId) {
        return projectService.queryProjectWithAuthorizedLevel(loginUser, userId);
    }

    /**
     * query authorized user
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return users        who have permission for the specified project
     */
    @Operation(summary = "queryAuthorizedUser", description = "QUERY_AUTHORIZED_USER_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_USER)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuthorizedUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam("projectCode") Long projectCode) {
        return projectService.queryAuthorizedUser(loginUser, projectCode);
    }

    /**
     * query authorized and user created project
     *
     * @param loginUser login user
     * @return projects which the user create and authorized
     */
    @Operation(summary = "queryProjectCreatedAndAuthorizedByUser", description = "QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_NOTES")
    @GetMapping(value = "/created-and-authed")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectCreatedAndAuthorizedByUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
    }

    /**
     * query all project list
     *
     * @param loginUser login user
     * @return all project list
     */
    @Operation(summary = "queryAllProjectList", description = "QUERY_ALL_PROJECT_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProjectList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return projectService.queryAllProjectList(loginUser);
    }

    /**
     * query all project list for dependent
     *
     * @param loginUser login user
     * @return all project list
     */
    @Operation(summary = "queryAllProjectListForDependent", description = "QUERY_ALL_PROJECT_LIST_FOR_DEPENDENT_NOTES")
    @GetMapping(value = "/list-dependent")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProjectListForDependent(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return projectService.queryAllProjectListForDependent();
    }
}
