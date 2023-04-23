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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_LINEAGE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_WITH_DEPENDENT_ERROR;
import static org.apache.dolphinscheduler.common.constants.Constants.SESSION_USER;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * work flow lineage controller
 */
@Tag(name = "WORK_FLOW_LINEAGE_TAG")
@RestController
@RequestMapping("projects/{projectCode}/lineages")
@Slf4j
public class WorkFlowLineageController extends BaseController {

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @Operation(summary = "queryLineageByWorkFlowName", description = "QUERY_WORKFLOW_LINEAGE_BY_NAME_NOTES")
    @GetMapping(value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<List<WorkFlowLineage>> queryWorkFlowLineageByName(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                                                    @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                    @RequestParam(value = "workFlowName", required = false) String workFlowName) {
        try {
            workFlowName = ParameterUtils.handleEscapes(workFlowName);
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(projectCode, workFlowName);
            return returnDataList(result);
        } catch (Exception e) {
            log.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @Operation(summary = "queryLineageByWorkFlowCode", description = "QUERY_WORKFLOW_LINEAGE_BY_CODE_NOTE")
    @GetMapping(value = "/{workFlowCode}")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Map<String, Object>> queryWorkFlowLineageByCode(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @PathVariable(value = "workFlowCode", required = true) long workFlowCode) {
        try {
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByCode(projectCode, workFlowCode);
            return returnDataList(result);
        } catch (Exception e) {
            log.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @Operation(summary = "queryWorkFlowList", description = "QUERY_WORKFLOW_LINEAGE_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Map<String, Object>> queryWorkFlowLineage(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                                            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        try {
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineage(projectCode);
            return returnDataList(result);
        } catch (Exception e) {
            log.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    /**
     * Whether task can be deleted or not, avoiding task depend on other task of process definition delete by accident.
     *
     * @param loginUser login user
     * @param projectCode project codes which taskCode belong
     * @param processDefinitionCode project code which taskCode belong
     * @param taskCode task definition code
     * @return Result of task can be delete or not
     */
    @Operation(summary = "verifyTaskCanDelete", description = "VERIFY_TASK_CAN_DELETE")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "taskCode", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
    })
    @PostMapping(value = "/tasks/verify-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_WITH_DEPENDENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyTaskCanDelete(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                      @RequestParam(value = "processDefinitionCode", required = true) long processDefinitionCode,
                                      @RequestParam(value = "taskCode", required = true) long taskCode) {
        Result result = new Result();
        Optional<String> taskDepMsg =
                workFlowLineageService.taskDepOnTaskMsg(projectCode, processDefinitionCode, taskCode);
        if (taskDepMsg.isPresent()) {
            throw new ServiceException(taskDepMsg.get());
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
