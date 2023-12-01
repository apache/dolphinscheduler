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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dolphinscheduler.api.enums.v2.BaseStatus;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.v2.BaseStatus.*;
import static org.apache.dolphinscheduler.common.constants.Constants.SESSION_USER;

/**
 * scheduler controller
 */
@Tag(name = "SCHEDULE_TAG")
@RestController
@RequestMapping("/projects/{projectCode}/schedules")
public class SchedulerController extends BaseController {

    public static final String DEFAULT_WARNING_TYPE = "NONE";
    public static final String DEFAULT_NOTIFY_GROUP_ID = "1";
    public static final String DEFAULT_FAILURE_POLICY = "CONTINUE";
    public static final String DEFAULT_PROCESS_INSTANCE_PRIORITY = "MEDIUM";

    @Autowired
    private SchedulerService schedulerService;

    /**
     * create schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @return create result code
     */
    @Operation(summary = "createSchedule", description = "CREATE_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "schedule", description = "SCHEDULE", schema = @Schema(implementation = String.class, example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','timezoneId':'America/Phoenix','crontab':'0 0 3/6 * * ? *'}")),
            @Parameter(name = "warningType", description = "WARNING_TYPE", schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = long.class)),
            @Parameter(name = "processInstancePriority", description = "PROCESS_INSTANCE_PRIORITY", schema = @Schema(implementation = Priority.class)),
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_SCHEDULE_ERROR)
    public Result createSchedule(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                 @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
                                 @RequestParam(value = "schedule") String schedule,
                                 @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                 @RequestParam(value = "warningGroupId", required = false, defaultValue = DEFAULT_NOTIFY_GROUP_ID) int warningGroupId,
                                 @RequestParam(value = "failureStrategy", required = false, defaultValue = DEFAULT_FAILURE_POLICY) FailureStrategy failureStrategy,
                                 @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                 @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                 @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                 @RequestParam(value = "processInstancePriority", required = false, defaultValue = DEFAULT_PROCESS_INSTANCE_PRIORITY) Priority processInstancePriority) {
        Map<String, Object> result = schedulerService.insertSchedule(
                loginUser,
                projectCode,
                processDefinitionCode,
                schedule,
                warningType,
                warningGroupId,
                failureStrategy,
                processInstancePriority,
                workerGroup,
                tenantCode,
                environmentCode);

        return returnDataList(result);
    }

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id scheduler id
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @param processInstancePriority process instance priority
     * @return update result code
     */
    @Operation(summary = "updateSchedule", description = "UPDATE_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "SCHEDULE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "schedule", description = "SCHEDULE", schema = @Schema(implementation = String.class, example = "{\"startTime\":\"1996-08-08 00:00:00\",\"endTime\":\"2200-08-08 00:00:00\",\"timezoneId\":\"America/Phoenix\",\"crontab\":\"0 0 3/6 * * ? *\"}")),
            @Parameter(name = "warningType", description = "WARNING_TYPE", schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "processInstancePriority", description = "PROCESS_INSTANCE_PRIORITY", schema = @Schema(implementation = Priority.class)),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = long.class)),
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_SCHEDULE_ERROR)
    public Result updateSchedule(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                 @PathVariable(value = "id") Integer id,
                                 @RequestParam(value = "schedule") String schedule,
                                 @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                 @RequestParam(value = "warningGroupId", required = false, defaultValue = DEFAULT_NOTIFY_GROUP_ID) int warningGroupId,
                                 @RequestParam(value = "failureStrategy", required = false, defaultValue = "END") FailureStrategy failureStrategy,
                                 @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                 @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                 @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                 @RequestParam(value = "processInstancePriority", required = false, defaultValue = DEFAULT_PROCESS_INSTANCE_PRIORITY) Priority processInstancePriority) {

        Map<String, Object> result = schedulerService.updateSchedule(loginUser, projectCode, id, schedule,
                warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, tenantCode,
                environmentCode);
        return returnDataList(result);
    }

    @Operation(summary = "online", description = "ONLINE_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "SCHEDULE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @PostMapping("/{id}/online")
    @ApiException(PUBLISH_SCHEDULE_ONLINE_ERROR)
    public Result<Boolean> publishScheduleOnline(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable("id") Integer id) {
        schedulerService.onlineScheduler(loginUser, projectCode, id);
        return Result.success(true);
    }

    @Operation(summary = "offline", description = "OFFLINE_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "SCHEDULE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @PostMapping("/{id}/offline")
    @ApiException(OFFLINE_SCHEDULE_ERROR)
    public Result<Boolean> offlineSchedule(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable("id") Integer id) {
        schedulerService.offlineScheduler(loginUser, projectCode, id);
        return Result.success(true);
    }

    /**
     * query schedule list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return schedule list page
     */
    @Operation(summary = "queryScheduleListPaging", description = "QUERY_SCHEDULE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionId", description = "PROCESS_DEFINITION_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ApiException(QUERY_SCHEDULE_LIST_PAGING_ERROR)
    public Result queryScheduleListPaging(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "processDefinitionCode", required = false, defaultValue = "0") long processDefinitionCode,
                                          @RequestParam(value = "searchVal", required = false) String searchVal,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return schedulerService.querySchedule(loginUser, projectCode, processDefinitionCode, searchVal, pageNo,
                pageSize);

    }

    /**
     * delete schedule by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id scheule id
     * @return delete result code
     */
    @Operation(summary = "deleteScheduleById", description = "DELETE_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "SCHEDULE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_SCHEDULE_BY_ID_ERROR)
    public Result deleteScheduleById(@RequestAttribute(value = SESSION_USER) User loginUser,
                                     @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                     @PathVariable("id") Integer id) {
        schedulerService.deleteSchedulesById(loginUser, id);
        return new Result(BaseStatus.SUCCESS);
    }

    /**
     * query schedule list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return schedule list
     */
    @Operation(summary = "queryScheduleList", description = "QUERY_SCHEDULE_LIST_NOTES")
    @PostMapping("/list")
    @ApiException(QUERY_SCHEDULE_LIST_ERROR)
    public Result queryScheduleList(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                    @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = schedulerService.queryScheduleList(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param schedule schedule expression
     * @return the next five fire time
     */
    @Operation(summary = "previewSchedule", description = "PREVIEW_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "schedule", description = "SCHEDULE", schema = @Schema(implementation = String.class, example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}")),
    })
    @PostMapping("/preview")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(PREVIEW_SCHEDULE_ERROR)
    public Result previewSchedule(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                  @RequestParam(value = "schedule") String schedule) {
        Map<String, Object> result = schedulerService.previewSchedule(loginUser, schedule);
        return returnDataList(result);
    }

    /**
     * update process definition schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param processInstancePriority process instance priority
     * @return update result code
     */
    @Operation(summary = "updateScheduleByProcessDefinitionCode", description = "UPDATE_SCHEDULE_BY_PROCESS_DEFINITION_CODE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "12345678")),
            @Parameter(name = "schedule", description = "SCHEDULE", schema = @Schema(implementation = String.class, example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}")),
            @Parameter(name = "warningType", description = "WARNING_TYPE", schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "processInstancePriority", description = "PROCESS_INSTANCE_PRIORITY", schema = @Schema(implementation = Priority.class)),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = long.class)),
    })
    @PutMapping("/update/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_SCHEDULE_ERROR)
    public Result updateScheduleByProcessDefinitionCode(@Parameter(hidden = true) @RequestAttribute(value = SESSION_USER) User loginUser,
                                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                        @PathVariable(value = "code") long processDefinitionCode,
                                                        @RequestParam(value = "schedule") String schedule,
                                                        @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                                        @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                                        @RequestParam(value = "failureStrategy", required = false, defaultValue = "END") FailureStrategy failureStrategy,
                                                        @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                                        @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                                        @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") long environmentCode,
                                                        @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority) {
        Map<String, Object> result = schedulerService.updateScheduleByProcessDefinitionCode(loginUser, projectCode,
                processDefinitionCode, schedule,
                warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, tenantCode,
                environmentCode);
        return returnDataList(result);
    }
}
