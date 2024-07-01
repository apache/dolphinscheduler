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
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_ALERT_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUEUE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_INSTANCE_STATE_COUNT_ERROR;

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.TaskInstanceCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowInstanceCountVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
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
    private DataAnalysisService dataAnalysisService;

    @Operation(summary = "countTaskState", description = "COUNT_TASK_STATE_NOTES")
    @Parameters({
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/task-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_INSTANCE_STATE_COUNT_ERROR)
    public Result<TaskInstanceCountVO> getTaskInstanceStateCount(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestParam(value = "startDate", required = false) String startDate,
                                                                 @RequestParam(value = "endDate", required = false) String endDate,
                                                                 @RequestParam(value = "projectCode", required = false) Long projectCode) {
        if (projectCode == null) {
            return Result.success(dataAnalysisService.getAllTaskInstanceStateCount(loginUser, startDate, endDate));
        }
        return Result.success(
                dataAnalysisService.getTaskInstanceStateCountByProject(loginUser, projectCode, startDate, endDate));
    }

    @Operation(summary = "countProcessInstanceState", description = "COUNT_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/process-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_INSTANCE_STATE_ERROR)
    public Result<WorkflowInstanceCountVO> getWorkflowInstanceStateCount(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                         @RequestParam(value = "startDate", required = false) String startDate,
                                                                         @RequestParam(value = "endDate", required = false) String endDate,
                                                                         @RequestParam(value = "projectCode", required = false) Long projectCode) {
        if (projectCode == null) {
            return Result.success(dataAnalysisService.getAllWorkflowInstanceStateCount(loginUser, startDate, endDate));
        }
        return Result.success(
                dataAnalysisService.getWorkflowInstanceStateCountByProject(loginUser, projectCode, startDate, endDate));
    }

    @Operation(summary = "countDefinitionByUser", description = "COUNT_PROCESS_DEFINITION_BY_USER_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/define-user-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_DEFINITION_USER_ERROR)
    public Result<WorkflowDefinitionCountVO> countDefinitionByUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @RequestParam(value = "projectCode", required = false) Long projectCode) {
        if (projectCode == null) {
            return Result.success(dataAnalysisService.getAllWorkflowDefinitionCount(loginUser));
        }
        return Result.success(dataAnalysisService.getWorkflowDefinitionCountByProject(loginUser, projectCode));
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
    public Result<List<CommandStateCount>> countCommandState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        List<CommandStateCount> commandStateCounts = dataAnalysisService.countCommandState(loginUser);
        return Result.success(commandStateCounts);
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
    public Result<Map<String, Integer>> countQueueState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Integer> stringIntegerMap = dataAnalysisService.countQueueState(loginUser);
        return Result.success(stringIntegerMap);
    }

    /**
     * command queue
     *
     * @param loginUser login user
     * @return queue state count
     */
    @Operation(summary = "listPendingCommands", description = "LIST_PENDING_COMMANDS")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping("/listCommand")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_GROUP_ERROR)
    public Result<PageInfo<Command>> listPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @RequestParam(value = "projectCode", required = false) Long projectCode,
                                                @RequestParam("pageNo") Integer pageNo,
                                                @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        PageInfo<Command> commandPageInfo =
                dataAnalysisService.listPendingCommands(loginUser, projectCode, pageNo, pageSize);
        return Result.success(commandPageInfo);
    }

    /**
     * error command
     *
     * @param loginUser login user
     * @return queue state count
     */
    @Operation(summary = "listErrorCommand", description = "LIST_ERROR_COMMAND_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping("/listErrorCommand")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_GROUP_ERROR)
    public Result<PageInfo<ErrorCommand>> listErrorCommand(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                           @RequestParam(value = "projectCode", required = false) Long projectCode,
                                                           @RequestParam("pageNo") Integer pageNo,
                                                           @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        PageInfo<ErrorCommand> errorCommandPageInfo =
                dataAnalysisService.listErrorCommand(loginUser, projectCode, pageNo, pageSize);
        return Result.success(errorCommandPageInfo);
    }
}
