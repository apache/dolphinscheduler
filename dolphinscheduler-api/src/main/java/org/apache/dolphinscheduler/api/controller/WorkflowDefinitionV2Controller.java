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

import io.swagger.annotations.*;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.CreateEmptyWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.CreateEmptyWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.CreateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.CreateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION_ERROR;

/**
 * workflow definition controller
 */
@Api(tags = "WORKFLOW_DEFINITION_TAG")
@RestController
@RequestMapping("v2/projects/{projectCode}/workflow-definition")
public class WorkflowDefinitionV2Controller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDefinitionV2Controller.class);

    @Autowired
    private ProcessDefinitionService workflowDefinitionService;

    /**
     * create workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     workflow definition request
     * @return create workflow response
     */
    @ApiOperation(value = "createWorkflowDefinition", notes = "CREATE_WORKFLOW_DEFINITION_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateWorkflowDefinitionResponse createWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                     @RequestBody CreateWorkflowDefinitionRequest request) {
        Map<String, Object> result = workflowDefinitionService.createProcessDefinition(loginUser, projectCode, request.getName(),
            request.getDescription(), request.getGlobalParams(), request.getLocations(), request.getTimeout(), request.getTenantCode(),
            request.getTaskRelationJson(), request.getTaskDefinitionJson(), request.getOtherParamsJson(), request.getExecutionType());
        return new CreateWorkflowDefinitionResponse(result);
    }

    /**
     * create empty workflow definition
     *
     * @param loginUser
     * @param projectCode
     * @param request     workflow definition request
     * @return create empty workflow response
     */
    @ApiOperation(value = "createEmptyWorkflowDefinition", notes = "CREATE_EMPTY_WORKFLOW_NOTES")
    @PostMapping(value = "/empty")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateEmptyWorkflowDefinitionResponse createEmptyWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                               @RequestBody CreateEmptyWorkflowDefinitionRequest request) {
        Map<String, Object> result = workflowDefinitionService.createEmptyProcessDefinition(loginUser, projectCode, request.getName(),
            request.getDescription(), request.getGlobalParams(), request.getTimeout(), request.getTenantCode(),
            request.getScheduleJson(), request.getExecutionType());
        return new CreateEmptyWorkflowDefinitionResponse(result);
    }

}
