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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROJECT_PARAMETER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROJECT_PARAMETER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROJECT_PARAMETER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROJECT_PARAMETER_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectParameterService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@Tag(name = "PROJECT_PARAMETER_TAG")
@RestController
@RequestMapping("projects/{projectCode}/project-parameter")
@Slf4j
public class ProjectParameterController extends BaseController {

    @Autowired
    private ProjectParameterService projectParameterService;

    @Operation(summary = "createProjectParameter", description = "CREATE_PROJECT_PARAMETER_NOTES")
    @Parameters({
            @Parameter(name = "projectParameterName", description = "PROJECT_PARAMETER_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectParameterValue", description = "PROJECT_PARAMETER_VALUE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectParameterDataType", description = "PROJECT_PARAMETER_DATA_TYPE", schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROJECT_PARAMETER_ERROR)
    public Result createProjectParameter(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @RequestParam("projectParameterName") String projectParameterName,
                                         @RequestParam(value = "projectParameterValue") String projectParameterValue,
                                         @RequestParam(value = "projectParameterDataType", defaultValue = "VARCHAR") String projectParameterDataType) {
        return projectParameterService.createProjectParameter(loginUser, projectCode, projectParameterName,
                projectParameterValue, projectParameterDataType);
    }

    @Operation(summary = "updateProjectParameter", description = "UPDATE_PROJECT_PARAMETER_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_PARAMETER_CODE", schema = @Schema(implementation = long.class, example = "123456")),
            @Parameter(name = "projectParameterName", description = "PROJECT_PARAMETER_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectParameterValue", description = "PROJECT_PARAMETER_VALUE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectParameterDataType", description = "PROJECT_PARAMETER_DATA_TYPE", schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_PARAMETER_ERROR)
    public Result updateProjectParameter(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @PathVariable("code") Long code,
                                         @RequestParam("projectParameterName") String projectParameterName,
                                         @RequestParam(value = "projectParameterValue") String projectParameterValue,
                                         @RequestParam(value = "projectParameterDataType") String projectParameterDataType) {
        return projectParameterService.updateProjectParameter(loginUser, projectCode, code, projectParameterName,
                projectParameterValue, projectParameterDataType);
    }

    @Operation(summary = "deleteProjectParametersByCode", description = "DELETE_PROJECT_PARAMETER_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_PARAMETER_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROJECT_PARAMETER_ERROR)
    public Result deleteProjectParametersByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @RequestParam("code") long code) {

        return projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, code);
    }

    @Operation(summary = "batchDeleteProjectParametersByCodes", description = "DELETE_PROJECT_PARAMETER_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROJECT_PARAMETER_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROJECT_PARAMETER_ERROR)
    public Result batchDeleteProjectParametersByCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                      @RequestParam("codes") String codes) {

        return projectParameterService.batchDeleteProjectParametersByCodes(loginUser, projectCode, codes);
    }

    @Operation(summary = "queryProjectParameterListPaging", description = "QUERY_PROJECT_PARAMETER_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_PARAMETER_ERROR)
    public Result queryProjectParameterListPaging(
                                                  @Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam(value = "searchVal", required = false) String searchVal,
                                                  @RequestParam(value = "projectParameterDataType", required = false) String projectParameterDataType,
                                                  @RequestParam("pageNo") Integer pageNo,
                                                  @RequestParam("pageSize") Integer pageSize) {

        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return projectParameterService.queryProjectParameterListPaging(loginUser, projectCode, pageSize, pageNo,
                searchVal, projectParameterDataType);
    }

    @Operation(summary = "queryProjectParameterByCode", description = "QUERY_PROJECT_PARAMETER_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROJECT_PARAMETER_CODE", schema = @Schema(implementation = long.class, example = "123456"))
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_PARAMETER_ERROR)
    public Result queryProjectParameterByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable("code") long code) {
        return projectParameterService.queryProjectParameterByCode(loginUser, projectCode, code);
    }

}
