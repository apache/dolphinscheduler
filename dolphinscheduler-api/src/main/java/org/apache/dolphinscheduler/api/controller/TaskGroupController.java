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

import static org.apache.dolphinscheduler.api.enums.Status.CLOSE_TASK_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TASK_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_GROUP_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_GROUP_QUEUE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_TASK_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TASK_GROUP_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;

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

@Tag(name = "TASK_GROUP_TAG")
@RestController
@RequestMapping("/task-group")
public class TaskGroupController extends BaseController {

    @Autowired
    private TaskGroupService taskGroupService;

    @Operation(summary = "create", description = "CREATE_TASK_GROUP_NOTE")
    @Parameters({
            @Parameter(name = "name", description = "NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class)),
            @Parameter(name = "description", description = "TASK_GROUP_DESCRIPTION", schema = @Schema(implementation = String.class)),
            @Parameter(name = "groupSize", description = "GROUP_SIZE", schema = @Schema(implementation = int.class)),

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_CREATE)
    public Result<TaskGroup> createTaskGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("name") String name,
                                             @RequestParam(value = "projectCode", required = false, defaultValue = "0") Long projectCode,
                                             @RequestParam("description") String description,
                                             @RequestParam("groupSize") Integer groupSize) {
        TaskGroup taskGroup = taskGroupService.createTaskGroup(loginUser, projectCode, name, description, groupSize);
        return Result.success(taskGroup);
    }

    @Operation(summary = "update", description = "UPDATE_TASK_GROUP_NOTE")
    @Parameters({
            @Parameter(name = "id", description = "TASK_GROUP_ID", schema = @Schema(implementation = int.class)),
            @Parameter(name = "name", description = "TASK_GROUP_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "TASK_GROUP_DESCRIPTION", schema = @Schema(implementation = String.class)),
            @Parameter(name = "groupSize", description = "GROUP_SIZE", schema = @Schema(implementation = int.class)),

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_UPDATE)
    public Result<TaskGroup> updateTaskGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("id") Integer id,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("groupSize") Integer groupSize) {
        TaskGroup taskGroup = taskGroupService.updateTaskGroup(loginUser, id, name, description, groupSize);
        return Result.success(taskGroup);
    }

    @Operation(summary = "list-paging", description = "QUERY_ALL_TASK_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "name", description = "TASK_GROUP_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    public Result<PageInfo<TaskGroup>> queryAllTaskGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @RequestParam(value = "name", required = false) String name,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam("pageNo") Integer pageNo,
                                                         @RequestParam("pageSize") Integer pageSize) {
        PageInfo<TaskGroup> pageInfo = taskGroupService.queryAllTaskGroup(loginUser, name, status, pageNo, pageSize);
        return Result.success(pageInfo);
    }

    @Operation(summary = "queryTaskGroupByStatus", description = "QUERY_TASK_GROUP_LIST_BY_STATUS_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "status", description = "TASK_GROUP_STATUS", required = true, schema = @Schema(implementation = int.class))
    })
    @GetMapping(value = "/query-list-by-status")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    public Result<PageInfo<TaskGroup>> queryTaskGroupByStatus(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @RequestParam("pageNo") Integer pageNo,
                                                              @RequestParam(value = "status", required = false) Integer status,
                                                              @RequestParam("pageSize") Integer pageSize) {
        PageInfo<TaskGroup> pageInfo = taskGroupService.queryTaskGroupByStatus(loginUser, pageNo, pageSize, status);
        return Result.success(pageInfo);
    }

    @Operation(summary = "queryTaskGroupByName", description = "QUERY_TASK_GROUP_LIST_BY_PROJECT_ID_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/query-list-by-projectCode")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    public Result<PageInfo<TaskGroup>> queryTaskGroupByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                            @RequestParam("pageNo") Integer pageNo,
                                                            @RequestParam(value = "projectCode", required = false) Long projectCode,
                                                            @RequestParam("pageSize") Integer pageSize) {
        PageInfo<TaskGroup> pageInfo =
                taskGroupService.queryTaskGroupByProjectCode(loginUser, pageNo, pageSize, projectCode);
        return Result.success(pageInfo);
    }

    @Operation(summary = "closeTaskGroup", description = "CLOSE_TASK_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "ID", required = true, schema = @Schema(implementation = int.class))
    })
    @PostMapping(value = "/close-task-group")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CLOSE_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_CLOSE)
    public Result<Void> closeTaskGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "id", required = false) Integer id) {
        taskGroupService.closeTaskGroup(loginUser, id);
        return Result.success();
    }

    @Operation(summary = "startTaskGroup", description = "START_TASK_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "TASK_GROUP_ID", required = true, schema = @Schema(implementation = int.class))
    })
    @PostMapping(value = "/start-task-group")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_START)
    public Result<Void> startTaskGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "id", required = false) Integer id) {
        taskGroupService.startTaskGroup(loginUser, id);
        return Result.success();
    }

    @Operation(summary = "forceStart", description = "FORCE_START_TASK_GROUP")
    @Parameters({
            @Parameter(name = "queueId", description = "TASK_GROUP_QUEUE_ID", required = true, schema = @Schema(implementation = int.class))
    })
    @PostMapping(value = "/forceStart")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_START)
    public Result<Void> forceStart(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "queueId") Integer id) {
        taskGroupService.forceStartTask(loginUser, id);
        return Result.success();
    }

    @Operation(summary = "modifyPriority", description = "MODIFY_TASK_GROUP_PRIORITY")
    @Parameters({
            @Parameter(name = "queueId", description = "TASK_GROUP_QUEUE_ID", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "priority", description = "TASK_GROUP_QUEUE_PRIORITY", required = true, schema = @Schema(implementation = int.class))
    })
    @PostMapping(value = "/modifyPriority")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @OperatorLog(auditType = AuditType.TASK_GROUP_MODIFY)
    public Result<Void> modifyPriority(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "queueId") Integer queueId,
                                       @RequestParam(value = "priority") Integer priority) {
        taskGroupService.modifyPriority(loginUser, queueId, priority);
        return Result.success();
    }

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;

    @Operation(summary = "queryTaskGroupQueuesByGroupId", description = "QUERY_TASKS_GROUP_GROUP_QUEUES")
    @Parameters({
            @Parameter(name = "groupId", description = "GROUP_ID", required = false, schema = @Schema(implementation = int.class, example = "1", defaultValue = "-1")),
            @Parameter(name = "taskInstanceName", description = "TASK_INSTANCE_NAME", required = false, schema = @Schema(implementation = String.class, example = "taskName")),
            @Parameter(name = "workflowInstanceName", description = "WORKFLOW_INSTANCE_NAME", required = false, schema = @Schema(implementation = String.class, example = "workflowInstanceName")),
            @Parameter(name = "status", description = "TASK_GROUP_STATUS", required = false, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping(value = "/query-list-by-group-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_QUEUE_LIST_ERROR)
    public Result<PageInfo<TaskGroupQueue>> queryTaskGroupQueues(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestParam(value = "groupId", required = false, defaultValue = "-1") Integer groupId,
                                                                 @RequestParam(value = "taskInstanceName", required = false) String taskName,
                                                                 @RequestParam(value = "workflowInstanceName", required = false) String workflowInstanceName,
                                                                 @RequestParam(value = "status", required = false) Integer status,
                                                                 @RequestParam("pageNo") Integer pageNo,
                                                                 @RequestParam("pageSize") Integer pageSize) {
        PageInfo<TaskGroupQueue> pageInfo = taskGroupQueueService.queryTasksByGroupId(
                loginUser,
                taskName,
                workflowInstanceName,
                status,
                groupId,
                pageNo,
                pageSize);
        return Result.success(pageInfo);
    }

}
