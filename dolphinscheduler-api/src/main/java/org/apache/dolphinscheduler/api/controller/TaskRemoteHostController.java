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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TASK_REMOTE_HOST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_REMOTE_HOST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_REMOTE_HOST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_REMOTE_HOST_PAGE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SUCCESS;
import static org.apache.dolphinscheduler.api.enums.Status.TEST_CONNECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TASK_REMOTE_HOST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VARIFY_TASK_REMOTE_HOST_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.TaskRemoteHostDTO;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskRemoteHostService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.TaskRemoteHostVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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
 * task remote host controller
 */
@Tag(name = "TASK_REMOTE_HOST_TAG")
@RestController
@RequestMapping("/remote_host")
public class TaskRemoteHostController {

    @Autowired
    private TaskRemoteHostService taskRemoteHostService;

    @Operation(summary = "createTaskRemoteHost", description = "CREATE_TASK_REMOTE_HOST_NOTES")
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_REMOTE_HOST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createTaskRemoteHost(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestBody TaskRemoteHostDTO taskRemoteHostDTO) {
        int result = taskRemoteHostService.createTaskRemoteHost(loginUser, taskRemoteHostDTO);
        return result > 0 ? Result.success() : Result.error(CREATE_TASK_REMOTE_HOST_ERROR);
    }

    @Operation(summary = "updateTaskRemoteHost", description = "UPDATE_TASK_REMOTE_HOST_NOTES")
    @PutMapping(value = "/update/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_REMOTE_HOST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateTaskRemoteHost(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable("code") Long code,
                                       @RequestBody TaskRemoteHostDTO taskRemoteHostDTO) {
        int result = taskRemoteHostService.updateTaskRemoteHost(code, loginUser, taskRemoteHostDTO);
        return result > 0 ? Result.success() : Result.error(UPDATE_TASK_REMOTE_HOST_ERROR);
    }

    @Operation(summary = "delete", description = "DELETE_TASK_REMOTE_HOST_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_REMOTE_HOST_CODE", schema = @Schema(implementation = long.class, example = "123456", required = true))
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_REMOTE_HOST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskRemoteHost(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("taskRemoteHostCode") Long code) {
        int result = taskRemoteHostService.deleteByCode(code, loginUser);
        return result > 0 ? Result.success() : Result.error(DELETE_TASK_REMOTE_HOST_ERROR);
    }

    @Operation(summary = "queryTaskRemoteHostListPaging", description = "QUERY_TASK_REMOTE_HOST_PAGE_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_REMOTE_HOST_PAGE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<TaskRemoteHostVO>> queryTaskRemoteHostListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                            @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                            @RequestParam("pageSize") Integer pageSize,
                                                                            @RequestParam("pageNo") Integer pageNo) {
        PageInfo<TaskRemoteHostVO> taskRemoteHostVOPageInfo =
                taskRemoteHostService.queryTaskRemoteHostListPaging(loginUser, searchVal, pageNo, pageSize);
        return Result.success(taskRemoteHostVOPageInfo);
    }

    @Operation(summary = "queryTaskRemoteHostList", description = "QUERY_ALL_TASK_REMOTE_HOST_LIST_NOTES")
    @GetMapping(value = "/query-remote-host-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_REMOTE_HOST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<List<TaskRemoteHostVO>> queryTaskRemoteHostList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<TaskRemoteHostVO> taskRemoteHostVOList = taskRemoteHostService.queryAllTaskRemoteHosts(loginUser);
        return Result.success(taskRemoteHostVOList);
    }

    @Operation(summary = "textConnect", description = "TEXT_CONNECT_HOST_NOTES")
    @PostMapping(value = "/test-connect")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TEST_CONNECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result testConnect(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestBody TaskRemoteHostDTO taskRemoteHostDTO) {
        boolean result = taskRemoteHostService.testConnect(taskRemoteHostDTO);
        return result ? Result.success(SUCCESS) : Result.error(TEST_CONNECT_ERROR);
    }

    @Operation(summary = "verifyTaskRemoteHost", description = "VERIFY_TASK_REMOTE_HOST_NOTES")
    @Parameters({
            @Parameter(name = "taskRemoteHostName", description = "TASK_REMOTE_HOST_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/verify-host")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VARIFY_TASK_REMOTE_HOST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyTaskRemoteHost(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "taskRemoteHostName") String taskRemoteHostName) {
        boolean result = taskRemoteHostService.verifyTaskRemoteHost(taskRemoteHostName);
        return result ? Result.success() : Result.error(VARIFY_TASK_REMOTE_HOST_ERROR);
    }

}
