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

import static org.apache.dolphinscheduler.api.enums.Status.COMMAND_STATE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_DEFINITION_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_INSTANCE_STATE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUEUE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_INSTANCE_STATE_COUNT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

/**
 * data analysis controller
 */
@Tag(name = "DATA_ANALYSIS_TAG")
@RestController
@RequestMapping("projects/analysis")
public class DataAnalysisController extends BaseController {

    @Autowired
    DataAnalysisService dataAnalysisService;

    /**
     * statistical task instance status data
     *
     * @param loginUser login user
     * @param startDate count start date
     * @param endDate count end date
     * @param projectCode project code
     * @return task instance count data
     */
    @Operation(summary = "countTaskState", description = "COUNT_TASK_STATE_NOTES")
    @Parameters({
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/task-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_INSTANCE_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countTaskState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "startDate", required = false) String startDate,
                                 @RequestParam(value = "endDate", required = false) String endDate,
                                 @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result =
                dataAnalysisService.countTaskStateByProject(loginUser, projectCode, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * statistical process instance status data
     *
     * @param loginUser login user
     * @param startDate start date
     * @param endDate end date
     * @param projectCode project code
     * @return process instance data
     */
    @Operation(summary = "countProcessInstanceState", description = "COUNT_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/process-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_INSTANCE_STATE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countProcessInstanceState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate,
                                            @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result =
                dataAnalysisService.countProcessInstanceStateByProject(loginUser, projectCode, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition count in project code
     */
    @Operation(summary = "countDefinitionByUser", description = "COUNT_PROCESS_DEFINITION_BY_USER_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/define-user-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_DEFINITION_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countDefinitionByUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result = dataAnalysisService.countDefinitionByUser(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state of user projects
     */
    @Operation(summary = "countCommandState", description = "COUNT_COMMAND_STATE_NOTES")
    @GetMapping(value = "/command-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COMMAND_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countCommandState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Object> result = dataAnalysisService.countCommandState(loginUser);
        return returnDataList(result);
    }

    /**
     * queue count
     *
     * @param loginUser login user
     * @return queue state count
     */
    @Operation(summary = "countQueueState", description = "COUNT_QUEUE_STATE_NOTES")
    @GetMapping(value = "/queue-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUEUE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countQueueState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Object> result = dataAnalysisService.countQueueState(loginUser);
        return returnDataList(result);
    }
}
