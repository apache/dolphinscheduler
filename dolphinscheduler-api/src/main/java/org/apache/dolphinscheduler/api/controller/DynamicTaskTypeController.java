/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.LIST_TASK_TYPE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.configuration.DynamicTaskTypeConfiguration;
import org.apache.dolphinscheduler.api.dto.taskType.DynamicTaskInfo;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * dynamic task type controller
 */
@Tag(name = "DYNAMIC_TASK_TYPE")
@RestController
@RequestMapping("/dynamic")
public class DynamicTaskTypeController extends BaseController {

    @Resource
    private DynamicTaskTypeConfiguration dynamicTaskTypeConfiguration;

    /**
     * get dynamic task category list
     *
     * @param loginUser login user
     * @return dynamic task category list
     */
    @Operation(summary = "listTaskCates", description = "LIST_TASK_TYPE_CATES")
    @GetMapping(value = "/taskCategories")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listDynamicTaskCategories(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<String> taskCategories = dynamicTaskTypeConfiguration.getTaskCategories();
        return success(Status.SUCCESS.getMsg(), taskCategories);
    }

    /**
     * get dynamic task category list
     *
     * @param loginUser login user
     * @return dynamic task category list
     */
    @Operation(summary = "listDynamicTaskTypes", description = "LIST_DYNAMIC_TASK_TYPES")
    @GetMapping(value = "/{taskCategory}/taskTypes")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listDynamicTaskTypes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable("taskCategory") String taskCategory) {
        List<DynamicTaskInfo> taskTypes = dynamicTaskTypeConfiguration.getTaskTypesByCategory(taskCategory);
        return success(Status.SUCCESS.getMsg(), taskTypes);
    }

}
