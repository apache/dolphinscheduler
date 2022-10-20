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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_SCHEDULE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_SCHEDULE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_SCHEDULE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_SCHEDULE_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_SCHEDULE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleCreateRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * schedule controller
 */
@Api(tags = "SCHEDULER_TAG")
@RestController
@RequestMapping("/v2/schedules")
public class ScheduleV2Controller extends BaseController {

    @Autowired
    private SchedulerService schedulerService;

    /**
     * Create resource schedule
     *
     * @param loginUser             login user
     * @param scheduleCreateRequest the new schedule object will be created
     * @return ResourceResponse object created
     */
    @ApiOperation(value = "create", notes = "CREATE_SCHEDULE_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_SCHEDULE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Schedule> createSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        Schedule schedule = schedulerService.createSchedulesV2(loginUser, scheduleCreateRequest);
        return Result.success(schedule);
    }

    /**
     * Delete schedule by id
     *
     * @param loginUser login user
     * @param id        schedule object id
     */
    @ApiOperation(value = "delete", notes = "DELETE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "SCHEDULE_ID", dataTypeClass = long.class, example = "123456", required = true)
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_SCHEDULE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @PathVariable("id") Integer id) {
        schedulerService.deleteSchedulesById(loginUser, id);
        return Result.success();
    }

    /**
     * Update resource schedule
     *
     * @param loginUser        login user
     * @param id               schedule object id
     * @param scheduleUpdateRequest the schedule object will be updated
     * @return result Result
     */
    @ApiOperation(value = "update", notes = "UPDATE_SCHEDULE_NOTES")
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_SCHEDULE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Schedule> updateSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable("id") Integer id,
                                           @RequestBody ScheduleUpdateRequest scheduleUpdateRequest) {
        Schedule schedule = schedulerService.updateSchedulesV2(loginUser, id, scheduleUpdateRequest);
        return Result.success(schedule);
    }

    /**
     * Get resource schedule by id
     *
     * @param loginUser        login user
     * @param id               schedule object id
     * @return result Result
     */
    @ApiOperation(value = "get", notes = "GET_SCHEDULE_BY_ID_NOTES")
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_SCHEDULE_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Schedule> getSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable("id") Integer id) {
        Schedule schedule = schedulerService.getSchedule(loginUser, id);
        return Result.success(schedule);
    }

    /**
     * Get resource schedule according to query parameter
     *
     * @param loginUser        login user
     * @
     * @return result Result
     */
    @ApiOperation(value = "get", notes = "QUERY_SCHEDULE_LIST_PAGING_NOTES")
    @PostMapping(value = "/filter", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_SCHEDULE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<Schedule>> filterSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestBody ScheduleFilterRequest scheduleFilterRequest) {
        PageInfo<Schedule> schedules = schedulerService.filterSchedules(loginUser, scheduleFilterRequest);
        return Result.success(schedules);
    }
}
