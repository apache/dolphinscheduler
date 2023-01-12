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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ENVIRONMENT_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_ENVIRONMENT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
 * environment controller
 */
@Tag(name = "ENVIRONMENT_TAG")
@RestController
@RequestMapping("environment")
public class EnvironmentController extends BaseController {

    @Autowired
    private EnvironmentService environmentService;

    /**
     * create environment
     *
     * @param loginUser   login user
     * @param name environment name
     * @param config config
     * @param description description
     * @return returns an error if it exists
     */
    @Operation(summary = "createEnvironment", description = "CREATE_ENVIRONMENT_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "ENVIRONMENT_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "config", description = "ENVIRONMENT_CONFIG", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "ENVIRONMENT_DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "workerGroups", description = "WORKER_GROUP_LIST", schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createEnvironment(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("name") String name,
                                    @RequestParam("config") String config,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam(value = "workerGroups", required = false) String workerGroups) {

        Map<String, Object> result =
                environmentService.createEnvironment(loginUser, name, config, description, workerGroups);
        return returnDataList(result);
    }

    /**
     * update environment
     *
     * @param loginUser   login user
     * @param code   environment code
     * @param name environment name
     * @param config environment config
     * @param description description
     * @return update result code
     */
    @Operation(summary = "updateEnvironment", description = "UPDATE_ENVIRONMENT_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "ENVIRONMENT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "name", description = "ENVIRONMENT_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "config", description = "ENVIRONMENT_CONFIG", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "ENVIRONMENT_DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "workerGroups", description = "WORKER_GROUP_LIST", schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateEnvironment(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("code") Long code,
                                    @RequestParam("name") String name,
                                    @RequestParam("config") String config,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam(value = "workerGroups", required = false) String workerGroups) {
        Map<String, Object> result =
                environmentService.updateEnvironmentByCode(loginUser, code, name, config, description, workerGroups);
        return returnDataList(result);
    }

    /**
     * query environment details by code
     *
     * @param environmentCode environment code
     * @return environment detail information
     */
    @Operation(summary = "queryEnvironmentByCode", description = "QUERY_ENVIRONMENT_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/query-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryEnvironmentByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("environmentCode") Long environmentCode) {

        Map<String, Object> result = environmentService.queryEnvironmentByCode(environmentCode);
        return returnDataList(result);
    }

    /**
     * query environment list paging
     *
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return environment list which the login user have permission to see
     */
    @Operation(summary = "queryEnvironmentListPaging", description = "QUERY_ENVIRONMENT_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryEnvironmentListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = environmentService.queryEnvironmentListPaging(loginUser, pageNo, pageSize, searchVal);
        return result;
    }

    /**
     * delete environment by code
     *
     * @param loginUser login user
     * @param environmentCode environment code
     * @return delete result code
     */
    @Operation(summary = "deleteEnvironmentByCode", description = "DELETE_ENVIRONMENT_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteEnvironment(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("environmentCode") Long environmentCode) {

        Map<String, Object> result = environmentService.deleteEnvironmentByCode(loginUser, environmentCode);
        return returnDataList(result);
    }

    /**
     * query all environment list
     *
     * @param loginUser login user
     * @return all environment list
     */
    @Operation(summary = "queryAllEnvironmentList", description = "QUERY_ALL_ENVIRONMENT_LIST_NOTES")
    @GetMapping(value = "/query-environment-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllEnvironmentList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = environmentService.queryAllEnvironmentList(loginUser);
        return returnDataList(result);
    }

    /**
     * verify environment and environment name
     *
     * @param loginUser login user
     * @param environmentName environment name
     * @return true if the environment name not exists, otherwise return false
     */
    @Operation(summary = "verifyEnvironment", description = "VERIFY_ENVIRONMENT_NOTES")
    @Parameters({
            @Parameter(name = "environmentName", description = "ENVIRONMENT_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/verify-environment")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyEnvironment(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "environmentName") String environmentName) {
        Map<String, Object> result = environmentService.verifyEnvironment(environmentName);
        return returnDataList(result);
    }
}
