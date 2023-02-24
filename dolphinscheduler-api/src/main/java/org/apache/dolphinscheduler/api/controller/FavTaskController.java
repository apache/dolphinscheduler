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

import static org.apache.dolphinscheduler.api.enums.Status.ADD_TASK_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_TASK_TYPE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.FavTaskService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * fav controller
 */
@Tag(name = "FAVOURITE_TAG")
@RestController
@RequestMapping("/favourite")
public class FavTaskController extends BaseController {

    @Resource
    private FavTaskService favTaskService;

    /**
     * get task type list
     *
     * @param loginUser login user
     * @return task type list
     */
    @Operation(summary = "listTaskType", description = "QUERY_TASK_TYPE_LIST")
    @GetMapping(value = "/taskTypes")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listTaskType(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<FavTaskDto> favTaskList = favTaskService.getFavTaskList(loginUser);
        return success(Status.SUCCESS.getMsg(), favTaskList);
    }

    /**
     * delete task fav
     *
     * @param loginUser login user
     * @return
     */
    @Operation(summary = "deleteTaskType", description = "DELETE_TASK_TYPE")
    @DeleteMapping(value = "/{taskType}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteFavTask(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("taskType") String taskType) {
        boolean b = favTaskService.deleteFavTask(loginUser, taskType);
        return success(b);
    }

    /**
     * add task fav
     *
     * @param loginUser login user
     * @return
     */
    @Operation(summary = "addTaskType", description = "ADD_TASK_TYPE")
    @PostMapping(value = "/{taskType}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ADD_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result addFavTask(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @PathVariable("taskType") String taskType) {
        int i = favTaskService.addFavTask(loginUser, taskType);
        return success(i > 0);
    }
}
