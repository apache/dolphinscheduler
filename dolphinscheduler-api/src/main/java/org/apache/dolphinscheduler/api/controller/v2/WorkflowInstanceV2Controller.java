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

package org.apache.dolphinscheduler.api.controller.v2;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * workflow instance controller
 */
@Tag(name = "WORKFLOW_INSTANCE_TAG_V2")
@RestController
@RequestMapping("/v2/workflow-instances")
public class WorkflowInstanceV2Controller extends BaseController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ExecutorService execService;

    /**
     * query workflow instance list paging
     * @param loginUser login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return workflow instance list
     */
    @Operation(summary = "queryWorkflowInstanceListPaging", description = "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkflowInstanceListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestBody WorkflowInstanceQueryRequest workflowInstanceQueryRequest) {
        Result result =
                checkPageParams(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        if (!result.checkResult()) {
            return result;
        }

        result = processInstanceService.queryProcessInstanceList(loginUser, workflowInstanceQueryRequest);
        return result;
    }

    /**
     * Query workflowInstance by id
     *
     * @param loginUser login user
     * @param workflowInstanceId      workflow instance id
     * @return Result result object query
     */
    @Operation(summary = "queryWorkflowInstanceById", description = "QUERY_WORKFLOW_INSTANCE_BY_ID")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", schema = @Schema(implementation = Integer.class, example = "123456", required = true))
    })
    @GetMapping(value = "/{workflowInstanceId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkflowInstanceById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable("workflowInstanceId") Integer workflowInstanceId) {
        Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, workflowInstanceId);
        return returnDataList(result);
    }

    /**
     * Delete workflowInstance by id
     *
     * @param loginUser login user
     * @param workflowInstanceId      workflow instance code
     * @return Result result object delete
     */
    @Operation(summary = "delete", description = "DELETE_WORKFLOWS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", schema = @Schema(implementation = Integer.class, example = "123456", required = true))
    })
    @DeleteMapping(value = "/{workflowInstanceId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Void> deleteWorkflowInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @PathVariable("workflowInstanceId") Integer workflowInstanceId) {
        processInstanceService.deleteProcessInstanceById(loginUser, workflowInstanceId);
        return Result.success();
    }

    /**
     * do action to workflow instance: pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param workflowInstanceId workflow instance id
     * @param executeType execute type
     * @return execute result code
     */
    @Operation(summary = "execute", description = "EXECUTE_ACTION_TO_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/{workflowInstanceId}/execute/{executeType}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result execute(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          @PathVariable("workflowInstanceId") Integer workflowInstanceId,
                          @PathVariable("executeType") ExecuteType executeType) {
        Map<String, Object> result = execService.execute(loginUser, workflowInstanceId, executeType);
        return returnDataList(result);
    }
}
