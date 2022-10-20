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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_SCHEDULE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TASK_DEFINITION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.task.TaskCreateRequest;
import org.apache.dolphinscheduler.api.dto.task.TaskFilterRequest;
import org.apache.dolphinscheduler.api.dto.task.TaskUpdateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * task definition controller
 */
@Api(tags = "TASK_DEFINITION_TAG")
@RestController
@RequestMapping("v2/tasks")
public class TaskDefinitionV2Controller extends BaseController {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    /**
     * Create resource task definition
     *
     * @param loginUser         login user
     * @param taskCreateRequest task definition json
     * @return Result object created
     */
    @ApiOperation(value = "create", notes = "CREATE_TASK_DEFINITION_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<TaskDefinition> createTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestBody TaskCreateRequest taskCreateRequest) {
        TaskDefinition taskDefinition = taskDefinitionService.createTaskDefinitionV2(loginUser, taskCreateRequest);
        return Result.success(taskDefinition);
    }

    /**
     * Delete resource task definition by code
     *
     * @param loginUser login user
     * @param code      task definition code
     */
    @ApiOperation(value = "delete", notes = "DELETE_TASK_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", dataTypeClass = long.class, example = "123456", required = true)
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_SCHEDULE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable("code") Long code) {
        taskDefinitionService.deleteTaskDefinitionByCode(loginUser, code);
        return Result.success();
    }

    /**
     * Update resource task definition by code
     *
     * @param loginUser         login user
     * @param code              task code of resource you want to update
     * @param taskUpdateRequest workflowUpdateRequest
     * @return ResourceResponse object updated
     */
    @ApiOperation(value = "update", notes = "UPDATE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", dataTypeClass = long.class, example = "123456", required = true)
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<TaskDefinition> updateTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @PathVariable("code") Long code,
                                                       @RequestBody TaskUpdateRequest taskUpdateRequest) {
        TaskDefinition taskDefinition =
                taskDefinitionService.updateTaskDefinitionV2(loginUser, code, taskUpdateRequest);
        return Result.success(taskDefinition);
    }

    /**
     * Get resource task definition by code
     *
     * @param loginUser login user
     * @param code      task code of resource you want to update
     * @return ResourceResponse object get from condition
     */
    @ApiOperation(value = "get", notes = "GET_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", dataTypeClass = long.class, example = "123456", required = true)
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<TaskDefinition> getTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @PathVariable("code") Long code) {
        TaskDefinition taskDefinition = taskDefinitionService.getTaskDefinition(loginUser, code);
        return Result.success(taskDefinition);
    }

    /**
     * Get resource task definition according to query parameter
     *
     * @param loginUser         login user
     * @param taskFilterRequest workflowFilterRequest
     * @return PageResourceResponse from condition
     */
    @ApiOperation(value = "get", notes = "FILTER_TASK_DEFINITION_NOTES")
    @PostMapping(value = "/query", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<TaskDefinition>> filterTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestBody TaskFilterRequest taskFilterRequest) {
        PageInfo<TaskDefinition> taskDefinitions =
                taskDefinitionService.filterTaskDefinition(loginUser, taskFilterRequest);
        return Result.success(taskDefinitions);
    }
}
