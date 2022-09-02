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

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceListPagingResponse;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceQueryRequest;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceSuccessResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * task instance controller
 */
@Api(tags = "TASK_INSTANCE_TAG")
@RestController
@RequestMapping("/v2/projects/{projectCode}/task-instances")
public class TaskInstanceV2Controller extends BaseController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    /**
     * query task list paging
     *
     * @param loginUser            login user
     * @param projectCode          project code
     * @param taskInstanceQueryReq taskInstanceQueryReq
     * @return task list page
     */
    @ApiOperation(value = "queryTaskListPaging", notes = "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = false, dataTypeClass = int.class, example = "100"),
        @ApiImplicitParam(name = "processInstanceName", value = "PROCESS_INSTANCE_NAME", required = false, dataTypeClass = String.class),
        @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
        @ApiImplicitParam(name = "taskName", value = "TASK_NAME", dataTypeClass = String.class),
        @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", dataTypeClass = String.class),
        @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", dataTypeClass = TaskExecutionStatus.class),
        @ApiImplicitParam(name = "host", value = "HOST", dataTypeClass = String.class),
        @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
        @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
        @ApiImplicitParam(name = "taskExecuteType", value = "TASK_EXECUTE_TYPE", required = false, dataTypeClass = TaskExecuteType.class, example = "STREAM"),
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20"),
    })
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public TaskInstanceListPagingResponse queryTaskListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                              TaskInstanceQueryRequest taskInstanceQueryReq) {
        Result result = checkPageParams(taskInstanceQueryReq.getPageNo(), taskInstanceQueryReq.getPageSize());
        if (!result.checkResult()) {
            return new TaskInstanceListPagingResponse(result);
        }
        String searchVal = ParameterUtils.handleEscapes(taskInstanceQueryReq.getSearchVal());
        result = taskInstanceService.queryTaskListPaging(loginUser, projectCode,
            taskInstanceQueryReq.getProcessInstanceId(), taskInstanceQueryReq.getProcessInstanceName(),
            taskInstanceQueryReq.getProcessDefinitionName(),
            taskInstanceQueryReq.getTaskName(), taskInstanceQueryReq.getExecutorName(),
            taskInstanceQueryReq.getStartTime(), taskInstanceQueryReq.getEndTime(), searchVal,
            taskInstanceQueryReq.getStateType(), taskInstanceQueryReq.getHost(),
            taskInstanceQueryReq.getTaskExecuteType(), taskInstanceQueryReq.getPageNo(),
            taskInstanceQueryReq.getPageSize());
        return new TaskInstanceListPagingResponse(result);
    }

    /**
     * change one task instance's state from FAILURE to FORCED_SUCCESS
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param id          task instance id
     * @return the result code and msg
     */
    @ApiOperation(value = "force-success", notes = "FORCE_TASK_SUCCESS")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "TASK_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "12")
    })
    @PostMapping(value = "/{id}/force-success", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(FORCE_TASK_SUCCESS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public TaskInstanceSuccessResponse forceTaskSuccess(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                        @PathVariable(value = "id") Integer id) {
        Result result = taskInstanceService.forceTaskSuccess(loginUser, projectCode, id);
        return new TaskInstanceSuccessResponse(result);
    }
}
