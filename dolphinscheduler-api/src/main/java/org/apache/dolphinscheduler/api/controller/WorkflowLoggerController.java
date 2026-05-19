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

import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_WORKFLOW_INSTANCE_LOG_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_INSTANCE_LOG_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.WorkflowLoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ResponseWorkflowLog;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Workflow logger controller
 */
@Tag(name = "WORKFLOW_LOGGER_TAG")
@RestController
@RequestMapping("/workflow-log")
public class WorkflowLoggerController extends BaseController {

    @Autowired
    private WorkflowLoggerService workflowLoggerService;

    /**
     * query workflow log
     *
     * @param loginUser login user
     * @param workflowInstanceId workflow instance id
     * @param skipNum skip number
     * @param limit limit
     * @return workflow log content
     */
    @Operation(summary = "queryWorkflowLog", description = "QUERY_WORKFLOW_INSTANCE_LOG_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_INSTANCE_LOG_ERROR)
    public Result<ResponseWorkflowLog> queryWorkflowLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam(value = "workflowInstanceId") int workflowInstanceId,
                                                        @RequestParam(value = "skipLineNum") int skipNum,
                                                        @RequestParam(value = "limit") int limit) {
        return workflowLoggerService.queryWorkflowLog(loginUser, workflowInstanceId, skipNum, limit);
    }

    /**
     * download workflow log file
     *
     * @param loginUser login user
     * @param workflowInstanceId workflow instance id
     * @return log file content
     */
    @Operation(summary = "downloadWorkflowLog", description = "DOWNLOAD_WORKFLOW_INSTANCE_LOG_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_WORKFLOW_INSTANCE_LOG_FILE_ERROR)
    public ResponseEntity downloadWorkflowLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @RequestParam(value = "workflowInstanceId") int workflowInstanceId) {
        byte[] logBytes = workflowLoggerService.getWorkflowLogBytes(loginUser, workflowInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + "workflow-" + workflowInstanceId + "-" + System.currentTimeMillis()
                                + ".log" + "\"")
                .body(logBytes);
    }

    /**
     * query workflow log in specified project
     *
     * @param loginUser      login user
     * @param projectCode project code
     * @param workflowInstanceId workflow instance id
     * @param skipNum        skip number
     * @param limit          limit
     * @return workflow log content
     */
    @Operation(summary = "queryWorkflowLogInSpecifiedProject", description = "QUERY_WORKFLOW_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{projectCode}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_INSTANCE_LOG_ERROR)
    public Result<String> queryWorkflowLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "workflowInstanceId") int workflowInstanceId,
                                           @RequestParam(value = "skipLineNum") int skipNum,
                                           @RequestParam(value = "limit") int limit) {
        return Result.success(
                workflowLoggerService.queryWorkflowLog(loginUser, projectCode, workflowInstanceId, skipNum, limit));
    }

    /**
     * download workflow log file in specified project
     *
     * @param loginUser      login user
     * @param projectCode    project code
     * @param workflowInstanceId workflow instance id
     * @return log file content
     */
    @Operation(summary = "downloadWorkflowLogInSpecifiedProject", description = "DOWNLOAD_WORKFLOW_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{projectCode}/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_WORKFLOW_INSTANCE_LOG_FILE_ERROR)
    public ResponseEntity downloadWorkflowLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @RequestParam(value = "workflowInstanceId") int workflowInstanceId) {
        byte[] logBytes = workflowLoggerService.getWorkflowLogBytes(loginUser, projectCode, workflowInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + "workflow-" + workflowInstanceId + "-" + System.currentTimeMillis()
                                + ".log" + "\"")
                .body(logBytes);
    }
}
