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

import static org.apache.dolphinscheduler.api.enums.Status.FORCE_TASK_SUCCESS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.REMOVE_TASK_INSTANCE_CACHE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_SAVEPOINT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_STOP_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceRemoveCacheResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
 * task instance controller
 */
@Tag(name = "TASK_INSTANCE_TAG")
@RestController
@RequestMapping("/projects/{projectCode}/task-instances")
public class TaskInstanceController extends BaseController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    /**
     * query task list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @param pageNo page number
     * @param pageSize page size
     * @param taskExecuteType task execute type
     * @return task list page
     */
    @Operation(summary = "queryTaskListPaging", description = "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "PROCESS_INSTANCE_ID", required = false, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "processInstanceName", description = "PROCESS_INSTANCE_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskName", description = "TASK_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "executorName", description = "EXECUTOR_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "stateType", description = "EXECUTION_STATUS", schema = @Schema(implementation = TaskExecutionStatus.class)),
            @Parameter(name = "host", description = "HOST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskExecuteType", description = "TASK_EXECUTE_TYPE", required = false, schema = @Schema(implementation = TaskExecuteType.class, example = "STREAM")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                      @RequestParam(value = "processInstanceId", required = false, defaultValue = "0") Integer processInstanceId,
                                      @RequestParam(value = "processInstanceName", required = false) String processInstanceName,
                                      @RequestParam(value = "processDefinitionName", required = false) String processDefinitionName,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "taskName", required = false) String taskName,
                                      @RequestParam(value = "executorName", required = false) String executorName,
                                      @RequestParam(value = "stateType", required = false) TaskExecutionStatus stateType,
                                      @RequestParam(value = "host", required = false) String host,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam(value = "taskExecuteType", required = false, defaultValue = "BATCH") TaskExecuteType taskExecuteType,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = taskInstanceService.queryTaskListPaging(
                loginUser,
                projectCode,
                processInstanceId,
                processInstanceName,
                processDefinitionName,
                taskName,
                executorName,
                startTime,
                endTime,
                searchVal,
                stateType,
                host,
                taskExecuteType,
                pageNo,
                pageSize);
        return result;
    }

    /**
     * change one task instance's state from FAILURE to FORCED_SUCCESS
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id task instance id
     * @return the result code and msg
     */
    @Operation(summary = "force-success", description = "FORCE_TASK_SUCCESS")
    @Parameters({
            @Parameter(name = "id", description = "TASK_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "12"))
    })
    @PostMapping(value = "/{id}/force-success")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(FORCE_TASK_SUCCESS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result forceTaskSuccess(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @Schema(name = "projectCode", required = true) @PathVariable long projectCode,
                                   @PathVariable(value = "id") Integer id) {
        return taskInstanceService.forceTaskSuccess(loginUser, projectCode, id);
    }

    /**
     * task savepoint, for stream task
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id task instance id
     * @return the result code and msg
     */
    @Operation(summary = "savepoint", description = "TASK_SAVEPOINT")
    @Parameters({
            @Parameter(name = "id", description = "TASK_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "12"))
    })
    @PostMapping(value = "/{id}/savepoint")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_SAVEPOINT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> taskSavePoint(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @PathVariable(value = "id") Integer id) {
        return taskInstanceService.taskSavePoint(loginUser, projectCode, id);
    }

    /**
     * task stop, for stream task
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id task instance id
     * @return the result code and msg
     */
    @Operation(summary = "stop", description = "TASK_INSTANCE_STOP")
    @Parameters({
            @Parameter(name = "id", description = "TASK_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "12"))
    })
    @PostMapping(value = "/{id}/stop")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_STOP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> stopTask(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                   @PathVariable(value = "id") Integer id) {
        return taskInstanceService.stopTask(loginUser, projectCode, id);
    }

    /**
     * remove task instance cache
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id task instance id
     * @return the result code and msg
     */
    @Operation(summary = "remove-task-instance-cache", description = "REMOVE_TASK_INSTANCE_CACHE")
    @Parameters({
            @Parameter(name = "id", description = "TASK_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "12"))
    })
    @DeleteMapping(value = "/{id}/remove-cache")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(REMOVE_TASK_INSTANCE_CACHE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public TaskInstanceRemoveCacheResponse removeTaskInstanceCache(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                   @PathVariable(value = "id") Integer id) {
        return taskInstanceService.removeTaskInstanceCache(loginUser, projectCode, id);
    }
}
