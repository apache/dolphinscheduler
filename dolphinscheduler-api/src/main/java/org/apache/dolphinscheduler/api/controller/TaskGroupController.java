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

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

import static org.apache.dolphinscheduler.api.enums.Status.*;

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
     * @param loginUser login user
     * @param name name
     * @param description description
     * @param groupSize group size
     * @param name project id
     * @return result and msg code
     */
    @ApiOperation(value = "createTaskGroup", notes = "CREATE_TAKS_GROUP_NOTE")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "NAME", dataType = "String"),
        @ApiImplicitParam(name = "description", value = "DESCRIPTION", dataType = "String"),
        @ApiImplicitParam(name = "groupSize", value = "GROUPSIZE", dataType = "Int"),
        @ApiImplicitParam(name = "name", value = "name", dataType = "Int")

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("name") String name,
                                  @RequestParam("description")  String description,
                                  @RequestParam("groupSize") Integer groupSize) {
        User user = new User();

        Map<String, Object> result = taskGroupService.createTaskGroup(loginUser, name, description, groupSize);
        return returnDataList(result);
    }

    /**
     * update task group list
     * @param loginUser login user
     * @param name name
     * @param description description
     * @param groupSize group size
     * @param name project id
     * @return result and msg code
     */
    @ApiOperation(value = "updateTaskGroup", notes = "UPDATE_TAKS_GROUP_NOTE")
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
                                  @RequestParam("description")  String description,
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
    @ApiOperation(value = "queryAllTaskGroup", notes = "QUERY_ALL_TASK_GROUP_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/query-list-all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllTaskGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupService.queryAllTaskGroup(loginUser, pageNo, pageSize);
        return returnDataList(result);
    }

    /**
     * query task group list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param status status
     * @param pageSize  page size
     * @return queue list
     */
    @ApiOperation(value = "queryTaskGroupByStatus", notes = "QUERY_TASK_GROUP_LIST_BY_STSATUS_NOTES")
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
        Map<String, Object> result = taskGroupService.queryTaskGroupByStatus(loginUser, pageNo, pageSize,status);
        return returnDataList(result);
    }

    /**
     * query task group list paging by project id
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param name project id
     * @param pageSize  page size
     * @return queue list
     */
    @ApiOperation(value = "queryTaskGroupByName", notes = "QUERY_TASK_GROUP_LIST_BY_PROJECT_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20"),
        @ApiImplicitParam(name = "name", value = "PROJECT_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/query-list-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskGroupByName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("pageNo") Integer pageNo,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupService.queryTaskGroupByName(loginUser, pageNo, pageSize,name);
        return returnDataList(result);
    }

    /**
     * close a task group
     *
     * @param loginUser login user
     * @param id id
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
     * @param loginUser login user
     * @param id id
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
     * wake a task group compulsively
     * @param loginUser login user
     * @param taskId task id
     * @return result
     */
    @ApiOperation(value = "wakeCompulsively", notes = "WAKE_TASK_COMPULSIVELY_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "taskId", value = "TASKID", required = true, dataType = "Int")
    })
    @PostMapping(value = "/wake-task-compulsively")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(START_TASK_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result wakeCompulsively(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "taskId", required = false) Integer taskId) {
        Map<String, Object> result = taskGroupService.wakeTaskcompulsively(loginUser, taskId);
        return returnDataList(result);
    }
}
