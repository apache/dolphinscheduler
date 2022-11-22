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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_DEFINITION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * workflow controller
 */
@Tag(name = "WORKFLOW_TAG_V2")
@RestController
@RequestMapping("/v2/workflows")
public class WorkflowV2Controller extends BaseController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * Create resource workflow
     *
     * @param loginUser             login user
     * @param workflowCreateRequest the new workflow object will be created
     * @return ResourceResponse object created
     */
    @Operation(summary = "create", description = "CREATE_WORKFLOWS_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> createWorkflow(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @RequestBody WorkflowCreateRequest workflowCreateRequest) {
        ProcessDefinition processDefinition =
                processDefinitionService.createSingleProcessDefinition(loginUser, workflowCreateRequest);
        return Result.success(processDefinition);
    }

    /**
     * Delete workflow by code
     *
     * @param loginUser login user
     * @param code      process definition code
     * @return Result result object delete
     */
    @Operation(summary = "delete", description = "DELETE_WORKFLOWS_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_CODE", schema = @Schema(implementation = long.class, example = "123456", required = true))
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteWorkflow(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @PathVariable("code") Long code) {
        processDefinitionService.deleteProcessDefinitionByCode(loginUser, code);
        return Result.success();
    }

    /**
     * Update resource workflow
     *
     * @param loginUser        login user
     * @param code             workflow resource code you want to update
     * @param workflowUpdateRequest workflowUpdateRequest
     * @return ResourceResponse object updated
     */
    @Operation(summary = "update", description = "UPDATE_WORKFLOWS_NOTES")
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> updateWorkflow(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @PathVariable("code") Long code,
                                                    @RequestBody WorkflowUpdateRequest workflowUpdateRequest) {
        ProcessDefinition processDefinition =
                processDefinitionService.updateSingleProcessDefinition(loginUser, code, workflowUpdateRequest);
        return Result.success(processDefinition);
    }

    /**
     * Get resource workflow
     *
     * @param loginUser        login user
     * @param code             workflow resource code you want to update
     * @return ResourceResponse object get from condition
     */
    @Operation(summary = "get", description = "GET_WORKFLOWS_NOTES")
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> getWorkflow(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @PathVariable("code") Long code) {
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(loginUser, code);
        return Result.success(processDefinition);
    }

    /**
     * Get resource workflows according to query parameter
     *
     * @param loginUser        login user
     * @param workflowFilterRequest workflowFilterRequest
     * @return PageResourceResponse from condition
     */
    @Operation(summary = "get", description = "FILTER_WORKFLOWS_NOTES")
    @PostMapping(value = "/query", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<ProcessDefinition>> filterWorkflows(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                               @RequestBody WorkflowFilterRequest workflowFilterRequest) {
        PageInfo<ProcessDefinition> processDefinitions =
                processDefinitionService.filterProcessDefinition(loginUser, workflowFilterRequest);
        return Result.success(processDefinitions);
    }
}
