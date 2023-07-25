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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.DynamicSubWorkflowDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
 * process instance controller
 */
@Tag(name = "PROCESS_INSTANCE_TAG")
@RestController
@RequestMapping("/projects/{projectCode}/process-instances")
@Slf4j
public class ProcessInstanceController extends BaseController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * query process instance list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefineCode process definition code
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @param otherParamsJson otherParamsJson handle other params
     * @return process instance list
     */
    @Operation(summary = "queryProcessInstanceListPaging", description = "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @Parameters({
            @Parameter(name = "processDefineCode", description = "PROCESS_DEFINITION_CODE", schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "executorName", description = "EXECUTOR_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "stateType", description = "EXECUTION_STATUS", schema = @Schema(implementation = WorkflowExecutionStatus.class)),
            @Parameter(name = "host", description = "HOST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessInstanceList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "processDefineCode", required = false, defaultValue = "0") long processDefineCode,
                                           @RequestParam(value = "searchVal", required = false) String searchVal,
                                           @RequestParam(value = "executorName", required = false) String executorName,
                                           @RequestParam(value = "stateType", required = false) WorkflowExecutionStatus stateType,
                                           @RequestParam(value = "host", required = false) String host,
                                           @RequestParam(value = "startDate", required = false) String startTime,
                                           @RequestParam(value = "endDate", required = false) String endTime,
                                           @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                           @RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = processInstanceService.queryProcessInstanceList(loginUser, projectCode, processDefineCode, startTime,
                endTime,
                searchVal, executorName, stateType, host, otherParamsJson, pageNo, pageSize);
        return result;
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id process instance id
     * @return task list for the process instance
     */
    @Operation(summary = "queryTaskListByProcessId", description = "QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{id}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskListByProcessId(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable("id") Integer id) throws IOException {
        Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectCode, id);
        return returnDataList(result);
    }

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskRelationJson process task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param id process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param locations locations
     * @param tenantCode tenantCode
     * @return update result code
     */
    @Operation(summary = "updateProcessInstance", description = "UPDATE_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "taskRelationJson", description = "TASK_RELATION_JSON", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDefinitionJson", description = "TASK_DEFINITION_JSON", schema = @Schema(implementation = String.class)),
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "scheduleTime", description = "SCHEDULE_TIME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "syncDefine", description = "SYNC_DEFINE", required = true, schema = @Schema(implementation = boolean.class, example = "false")),
            @Parameter(name = "globalParams", description = "PROCESS_GLOBAL_PARAMS", schema = @Schema(implementation = String.class, example = "[]")),
            @Parameter(name = "locations", description = "PROCESS_INSTANCE_LOCATIONS", schema = @Schema(implementation = String.class)),
            @Parameter(name = "timeout", description = "PROCESS_TIMEOUT", schema = @Schema(implementation = int.class, example = "0")),
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.UPDATE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                        @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                        @PathVariable(value = "id") Integer id,
                                        @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
                                        @RequestParam(value = "syncDefine", required = true) Boolean syncDefine,
                                        @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                        @RequestParam(value = "locations", required = false) String locations,
                                        @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout) {
        Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectCode, id,
                taskRelationJson, taskDefinitionJson, scheduleTime, syncDefine, globalParams, locations, timeout);
        return returnDataList(result);
    }

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id process instance id
     * @return process instance detail
     */
    @Operation(summary = "queryProcessInstanceById", description = "QUERY_PROCESS_INSTANCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessInstanceById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable("id") Integer id) {
        Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, projectCode, id);
        return returnDataList(result);
    }

    /**
     * query top n process instance order by running duration
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param size number of process instance
     * @param startTime start time
     * @param endTime end time
     * @return list of process instance
     */
    @Operation(summary = "queryTopNLongestRunningProcessInstance", description = "QUERY_TOPN_LONGEST_RUNNING_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "size", description = "PROCESS_INSTANCE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "startTime", description = "PROCESS_INSTANCE_START_TIME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "endTime", description = "PROCESS_INSTANCE_END_TIME", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/top-n")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessInstance> queryTopNLongestRunningProcessInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                          @RequestParam("size") Integer size,
                                                                          @RequestParam(value = "startTime", required = true) String startTime,
                                                                          @RequestParam(value = "endTime", required = true) String endTime) {
        Map<String, Object> result = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser,
                projectCode, size, startTime, endTime);
        return returnDataList(result);
    }

    /**
     * delete process instance by id, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id process instance id
     * @return delete result code
     */
    @Operation(summary = "deleteProcessInstanceById", description = "DELETE_PROCESS_INSTANCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Void> deleteProcessInstanceById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @PathVariable("id") Integer id) {
        processInstanceService.deleteProcessInstanceById(loginUser, id);
        return Result.success();
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskId task id
     * @return sub process instance detail
     */
    @Operation(summary = "querySubProcessInstanceByTaskCode", description = "QUERY_SUBPROCESS_INSTANCE_BY_TASK_CODE_NOTES")
    @Parameters({
            @Parameter(name = "taskCode", description = "TASK_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/query-sub-by-parent")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result querySubProcessInstanceByTaskId(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("taskId") Integer taskId) {
        Map<String, Object> result =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, taskId);
        return returnDataList(result);
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param subId sub process id
     * @return parent instance detail
     */
    @Operation(summary = "queryParentInstanceBySubId", description = "QUERY_PARENT_PROCESS_INSTANCE_BY_SUB_PROCESS_INSTANCE_ID_NOTES")
    @Parameters({
            @Parameter(name = "subId", description = "SUB_PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/query-parent-by-sub")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR)
    @AccessLogAnnotation
    public Result queryParentInstanceBySubId(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @RequestParam("subId") Integer subId) {
        Map<String, Object> result = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, subId);
        return returnDataList(result);
    }

    /**
     * query dynamic sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param taskId task id
     * @return sub process instance detail
     */
    @Operation(summary = "queryDynamicSubWorkflowInstances", description = "QUERY_DYNAMIC_SUBPROCESS_INSTANCE_BY_TASK_CODE_NOTES")
    @Parameters({
            @Parameter(name = "taskId", description = "taskInstanceId", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/query-dynamic-sub-workflows")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<List<DynamicSubWorkflowDto>> queryDynamicSubWorkflowInstances(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                @RequestParam("taskId") Integer taskId) {
        List<DynamicSubWorkflowDto> dynamicSubWorkflowDtos =
                processInstanceService.queryDynamicSubWorkflowInstances(loginUser, taskId);
        return new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), dynamicSubWorkflowDtos);
    }

    /**
     * query process instance global variables and local variables
     *
     * @param loginUser login user
     * @param id process instance id
     * @return variables data
     */
    @Operation(summary = "viewVariables", description = "QUERY_PROCESS_INSTANCE_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{id}/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR)
    @AccessLogAnnotation
    public Result viewVariables(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                @PathVariable("id") Integer id) {
        Map<String, Object> result = processInstanceService.viewVariables(projectCode, id);
        return returnDataList(result);
    }

    /**
     * encapsulation gantt structure
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id process instance id
     * @return gantt tree data
     */
    @Operation(summary = "vieGanttTree", description = "VIEW_GANTT_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{id}/view-gantt")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR)
    @AccessLogAnnotation
    public Result viewTree(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                           @PathVariable("id") Integer id) throws Exception {
        Map<String, Object> result = processInstanceService.viewGantt(projectCode, id);
        return returnDataList(result);
    }

    /**
     * batch delete process instance by ids, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceIds process instance id
     * @return delete result code
     */
    @Operation(summary = "batchDeleteProcessInstanceByIds", description = "BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "processInstanceIds", description = "PROCESS_INSTANCE_IDS", required = true, schema = @Schema(implementation = String.class)),
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR)
    @AccessLogAnnotation
    public Result batchDeleteProcessInstanceByIds(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @PathVariable long projectCode,
                                                  @RequestParam("processInstanceIds") String processInstanceIds) {
        // task queue
        Map<String, Object> result = new HashMap<>();
        List<String> deleteFailedIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(Constants.COMMA);

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    processInstanceService.deleteProcessInstanceById(loginUser, processInstanceId);
                } catch (Exception e) {
                    log.error("Delete workflow instance: {} error", strProcessInstanceId, e);
                    deleteFailedIdList
                            .add(MessageFormat.format(Status.PROCESS_INSTANCE_ERROR.getMsg(), strProcessInstanceId));
                }
            }
        }
        if (!deleteFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR, String.join("\n", deleteFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return returnDataList(result);
    }

    @Operation(summary = "queryProcessInstanceListByTrigger", description = "QUERY_PROCESS_INSTANCE_BY_TRIGGER_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = Long.class)),
            @Parameter(name = "triggerCode", description = "TRIGGER_CODE", required = true, schema = @Schema(implementation = Long.class))
    })
    @GetMapping("/trigger")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    @AccessLogAnnotation()
    public Result queryProcessInstancesByTriggerCode(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @PathVariable long projectCode,
                                                     @RequestParam(value = "triggerCode") Long triggerCode) {
        Map<String, Object> result = processInstanceService.queryByTriggerCode(loginUser, projectCode, triggerCode);
        return returnDataList(result);
    }
}
