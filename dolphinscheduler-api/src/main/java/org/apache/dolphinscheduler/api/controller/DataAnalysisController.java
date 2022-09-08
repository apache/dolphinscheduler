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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * data analysis controller
 */
@Api(tags = "DATA_ANALYSIS_TAG")
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
    @ApiOperation(value = "countTaskState", notes = "COUNT_TASK_STATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/task-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_INSTANCE_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countTaskState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "startDate", required = false) String startDate,
                                 @RequestParam(value = "endDate", required = false) String endDate,
                                 @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        return dataAnalysisService.countTaskStateByProject(loginUser, projectCode, startDate, endDate);
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
    @ApiOperation(value = "countProcessInstanceState", notes = "COUNT_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/process-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_INSTANCE_STATE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countProcessInstanceState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate,
                                            @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        return dataAnalysisService.countProcessInstanceStateByProject(loginUser, projectCode, startDate, endDate);
    }

    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition count in project code
     */
    @ApiOperation(value = "countDefinitionByUser", notes = "COUNT_PROCESS_DEFINITION_BY_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/define-user-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_DEFINITION_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countDefinitionByUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        return dataAnalysisService.countDefinitionByUser(loginUser, projectCode);
    }

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state of user projects
     */
    @ApiOperation(value = "countCommandState", notes = "COUNT_COMMAND_STATE_NOTES")
    @GetMapping(value = "/command-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COMMAND_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countCommandState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        return dataAnalysisService.countCommandState(loginUser);
    }

    /**
     * queue count
     *
     * @param loginUser login user
     * @return queue state count
     */
    @ApiOperation(value = "countQueueState", notes = "COUNT_QUEUE_STATE_NOTES")
    @GetMapping(value = "/queue-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUEUE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countQueueState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        return dataAnalysisService.countQueueState(loginUser);
    }
}
