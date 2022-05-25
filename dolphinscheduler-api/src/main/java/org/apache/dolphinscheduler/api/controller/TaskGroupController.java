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

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * task group controller
 */
@Api(tags = "task group")
@RestController
@RequestMapping("/task-group")
public class TaskGroupController extends BaseController {

    @Autowired
    private TaskGroupService taskGroupService;

    /**
     * query task group list
     *
     * @param loginUser   login user
     * @param name        name
     * @param description description
     * @param groupSize   group size
     * @param name        project id
     * @return result and msg code
     */
    @ApiOperation(value = "create", notes = "CREATE_TASK_GROUP_NOTE")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "NAME", dataType = "String"),
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", type = "Long"),
        @ApiImplicitParam(name = "description", value = "DESCRIPTION", dataType = "String"),
        @ApiImplicitParam(name = "groupSize", value = "GROUPSIZE", dataType = "Int"),

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("name") String name,
                                  @RequestParam(value = "projectCode", required = false, defaultValue = "0") Long projectcode,
                                  @RequestParam("description") String description,
                                  @RequestParam("groupSize") Integer groupSize) {
        Map<String, Object> result = taskGroupService.createTaskGroup(loginUser, projectcode, name, description, groupSize);
        return returnDataList(result);
    }

    /**
     * update task group list
     *
     * @param loginUser   login user
     * @param name        name
     * @param description description
     * @param groupSize   group size
     * @param name        project id
     * @return result and msg code
     */
    @ApiOperation(value = "update", notes = "UPDATE_TASK_GROUP_NOTE")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", dataType = "Int"),
        @ApiImplicitParam(name = "name", value = "NAME", dataType = "String"),
        @ApiImplicitParam(name = "description", value = "DESCRIPTION", dataType = "String"),
        @ApiImplicitParam(name = "groupSize", value = "GROUPSIZE", dataType = "Int"),

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("id") Integer id,
                                  @RequestParam("name") String name,
                                  @RequestParam("description") String description,
                                  @RequestParam("groupSize") Integer groupSize) {
        Map<String, Object> result = taskGroupService.updateTaskGroup(loginUser, id, name, description, groupSize);
        return returnDataList(result);
    }

    /**
     * query task group list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param pageSize  page size
     * @return queue list
     */
    @ApiOperation(value = "list-paging", notes = "QUERY_ALL_TASK_GROUP_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "name", value = "NAME", required = false, dataType = "String"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "status", required = false) Integer status,
                                    @RequestParam("pageNo") Integer pageNo,
                                    @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupService.queryAllTaskGroup(loginUser, name, status, pageNo, pageSize);
        return returnDataList(result);
    }

    /**
     * query task group list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param status    status
     * @param pageSize  page size
     * @return queue list
     */
    @ApiOperation(value = "queryTaskGroupByStatus", notes = "QUERY_TASK_GROUP_LIST_BY_STATUS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20"),
        @ApiImplicitParam(name = "status", value = "status", required = true, dataType = "Int")
    })
    @GetMapping(value = "/query-list-by-status")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskGroupByStatus(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("pageNo") Integer pageNo,
                                         @RequestParam(value = "status", required = false) Integer status,
                                         @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupService.queryTaskGroupByStatus(loginUser, pageNo, pageSize, status);
        return returnDataList(result);
    }

    /**
     * query task group list paging by project code
     *
     * @param loginUser   login user
     * @param pageNo      page number
     * @param projectCode project code
     * @param pageSize    page size
     * @return queue list
     */
    @ApiOperation(value = "queryTaskGroupByName", notes = "QUERY_TASK_GROUP_LIST_BY_PROJECT_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20"),
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataType = "String")
    })
    @GetMapping(value = "/query-list-by-projectCode")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskGroupByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam(value = "projectCode", required = false) Long projectCode,
                                       @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupService.queryTaskGroupByProjectCode(loginUser, pageNo, pageSize, projectCode);
        return returnDataList(result);
    }

    /**
     * close a task group
     *
     * @param loginUser login user
     * @param id        id
     * @return result
     */
    @ApiOperation(value = "closeTaskGroup", notes = "CLOSE_TASK_GROUP_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "ID", required = true, dataType = "Int")
    })
    @PostMapping(value = "/close-task-group")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CLOSE_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result closeTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "id", required = false) Integer id) {

        Map<String, Object> result = taskGroupService.closeTaskGroup(loginUser, id);
        return returnDataList(result);
    }

    /**
     * start a task group
     *
     * @param loginUser login user
     * @param id        id
     * @return result
     */
    @ApiOperation(value = "startTaskGroup", notes = "START_TASK_GROUP_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "ID", required = true, dataType = "Int")
    })
    @PostMapping(value = "/start-task-group")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "id", required = false) Integer id) {
        Map<String, Object> result = taskGroupService.startTaskGroup(loginUser, id);
        return returnDataList(result);
    }

    /**
     * force start task without task group
     *
     * @param loginUser login user
     * @param queueId   task group queue id
     * @return result
     */
    @ApiOperation(value = "forceStart", notes = "WAKE_TASK_COMPULSIVELY_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "queueId", value = "TASK_GROUP_QUEUEID", required = true, dataType = "Int")
    })
    @PostMapping(value = "/forceStart")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result forceStart(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "queueId") Integer queueId) {
        Map<String, Object> result = taskGroupService.forceStartTask(loginUser, queueId);
        return returnDataList(result);
    }

    /**
     * force start task without task group
     *
     * @param loginUser login user
     * @param queueId   task group queue id
     * @return result
     */
    @ApiOperation(value = "modifyPriority", notes = "WAKE_TASK_COMPULSIVELY_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "queueId", value = "TASK_GROUP_QUEUEID", required = true, dataType = "Int"),
        @ApiImplicitParam(name = "priority", value = "TASK_GROUP_QUEUE_PRIORITY", required = true, dataType = "Int")
    })
    @PostMapping(value = "/modifyPriority")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result modifyPriority(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "queueId") Integer queueId,
                                 @RequestParam(value = "priority") Integer priority) {
        Map<String, Object> result = taskGroupService.modifyPriority(loginUser, queueId,priority);
        return returnDataList(result);
    }

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;

    /**
     * query task group queue list paging
     *
     * @param groupId     ID for task group
     * @param taskName    Task Name
     * @param processName Process instance name
     * @param status      Task queue status
     * @param loginUser   login user
     * @param pageNo      page number
     * @param pageSize    page size
     * @return queue list
     */
    @ApiOperation(value = "queryTasksByGroupId", notes = "QUERY_ALL_TASKS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "groupId", value = "GROUP_ID", required = false, dataType = "Int", example = "1", defaultValue = "-1"),
        @ApiImplicitParam(name = "taskInstanceName", value = "TASK_INSTANCE_NAME", required = false, dataType = "String", example = "taskName"),
        @ApiImplicitParam(name = "processInstanceName", value = "PROCESS_INSTANCE_NAME", required = false, dataType = "String", example = "processName"),
        @ApiImplicitParam(name = "status", value = "STATUS", required = false, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/query-list-by-group-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_QUEUE_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTasksByGroupId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam(value = "groupId", required = false, defaultValue = "-1") Integer groupId,
                                      @RequestParam(value = "taskInstanceName",required = false) String taskName,
                                      @RequestParam(value = "processInstanceName",required = false) String processName,
                                      @RequestParam(value = "status",required = false) Integer status,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupQueueService.queryTasksByGroupId(loginUser, taskName,processName,status,
            groupId, pageNo, pageSize);
        return returnDataList(result);
    }

}
