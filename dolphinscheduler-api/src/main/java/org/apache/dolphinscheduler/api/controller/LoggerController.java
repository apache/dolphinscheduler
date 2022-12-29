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

import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_INSTANCE_LOG_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * logger controller
 */
@Tag(name = "LOGGER_TAG")
@RestController
@RequestMapping("/log")
public class LoggerController extends BaseController {

    @Autowired
    private LoggerService loggerService;

    /**
     * query task log
     *
     * @param loginUser login user
     * @param taskInstanceId task instance id
     * @param skipNum skip number
     * @param limit limit
     * @return task log content
     */
    @Operation(summary = "queryLog", description = "QUERY_TASK_INSTANCE_LOG_NOTES")
    @Parameters({
            @Parameter(name = "taskInstanceId", description = "TASK_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_INSTANCE_LOG_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ResponseTaskLog> queryLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                            @RequestParam(value = "skipLineNum") int skipNum,
                                            @RequestParam(value = "limit") int limit) {
        return loggerService.queryLog(loginUser, taskInstanceId, skipNum, limit);
    }

    /**
     * download log file
     *
     * @param loginUser login user
     * @param taskInstanceId task instance id
     * @return log file content
     */
    @Operation(summary = "downloadTaskLog", description = "DOWNLOAD_TASK_INSTANCE_LOG_NOTES")
    @Parameters({
            @Parameter(name = "taskInstanceId", description = "TASK_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadTaskLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        byte[] logBytes = loggerService.getLogBytes(loginUser, taskInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }

    /**
     * query task log in specified project
     *
     * @param loginUser      login user
     * @param projectCode project code
     * @param taskInstanceId task instance id
     * @param skipNum        skip number
     * @param limit          limit
     * @return task log content
     */
    @Operation(summary = "queryLogInSpecifiedProject", description = "QUERY_TASK_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "taskInstanceId", description = "TASK_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{projectCode}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_INSTANCE_LOG_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<String> queryLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                   @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                   @RequestParam(value = "skipLineNum") int skipNum,
                                   @RequestParam(value = "limit") int limit) {
        return returnDataList(loggerService.queryLog(loginUser, projectCode, taskInstanceId, skipNum, limit));
    }

    /**
     * download log file
     *
     * @param loginUser      login user
     * @param projectCode    project code
     * @param taskInstanceId task instance id
     * @return log file content
     */
    @Operation(summary = "downloadTaskLogInSpecifiedProject", description = "DOWNLOAD_TASK_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "taskInstanceId", description = "TASK_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{projectCode}/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadTaskLog(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        byte[] logBytes = loggerService.getLogBytes(loginUser, projectCode, taskInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }
}
