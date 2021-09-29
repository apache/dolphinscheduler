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
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_GROUP_QUEUE_LIST_ERROR;

/**
 * task group queue controller
 */
@Api(tags = "TASK_GROUP_QUEUE_TAG")
@RestController
@RequestMapping("/task-group-queue")
public class TaskGroupQueueController extends BaseController {

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;



    
    /**
     * query task group queue list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param pageSize  page size
     * @return queue list
     */
    @ApiOperation(value = "queryTasksByGroupId", notes = "QUERY_ALL_TASKS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "groupId", value = "GROUP_ID", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/query-list-by-group-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_GROUP_QUEUE_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTasksByGroupId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("groupId") Integer groupId,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = taskGroupQueueService.queryTasksByGroupId(loginUser, groupId, pageNo, pageSize);
        return returnDataList(result);
    }

}
