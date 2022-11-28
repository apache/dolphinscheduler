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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALL_WORKFLOW_COUNT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.project.StatisticsStateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * workflow instance controller
 */
@Tag(name = "STATISTICS_V2")
@RestController
@RequestMapping("/v2/statistics")
public class StatisticsV2Controller extends BaseController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DataAnalysisService dataAnalysisService;
    /**
     * query all workflow count
     * @param loginUser login user
     * @return workflow count
     */
    @Operation(summary = "queryAllWorkflowCount", description = "QUERY_ALL_WORKFLOW_COUNT")
    @GetMapping(value = "/workflows/count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_WORKFLOW_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkflowInstanceCounts(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return projectService.queryAllWorkflowCounts(loginUser);
    }

    /**
     * query all workflow States count
     * @param loginUser login user
     * @param statisticsStateRequest statisticsStateRequest
     * @return workflow States count
     */
    @Operation(summary = "queryAllWorkflowStatesCount", description = "QUERY_ALL_WORKFLOW_STATES_COUNT")
    @GetMapping(value = "/workflows/states/count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_WORKFLOW_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkflowStatesCounts(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody(required = false) StatisticsStateRequest statisticsStateRequest) {
        Map<String, Object> result =
                projectService.countWorkflowStates(loginUser, statisticsStateRequest);
        return returnDataList(result);
    }

    /**
     * query one workflow States count
     * @param loginUser login user
     * @param workflowName workflowName
     * @return workflow States count
     */
    @Operation(summary = "queryAllWorkflowStatesCount", description = "QUERY_ALL_WORKFLOW_STATES_COUNT")
    @GetMapping(value = "/{workflowName}/states/count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_WORKFLOW_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryOneWorkflowStates(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable("workflowName") String workflowName) {
        Map<String, Object> result =
                projectService.countOneWorkflowStates(loginUser, workflowName);
        return returnDataList(result);
    }
}
