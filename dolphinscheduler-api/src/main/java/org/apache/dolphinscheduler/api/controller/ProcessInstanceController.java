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

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * process instance controller
 */
@Api(tags = "PROCESS_INSTANCE_TAG")
@RestController
@RequestMapping("/projects/{projectCode}/process-instances")
public class ProcessInstanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);

    @Autowired
    ProcessInstanceService processInstanceService;

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
     * @return process instance list
     */
    @ApiOperation(value = "queryProcessInstanceListPaging", notes = "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefiniteCode", value = "PROCESS_DEFINITION_CODE", dataType = "Long", example = "100"),
        @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
        @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", type = "String"),
        @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type = "ExecutionStatus"),
        @ApiImplicitParam(name = "host", value = "HOST", type = "String"),
        @ApiImplicitParam(name = "startDate", value = "START_DATE", type = "String"),
        @ApiImplicitParam(name = "endDate", value = "END_DATE", type = "String"),
        @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "10")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessInstanceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "processDefineCode", required = false, defaultValue = "0") long processDefineCode,
                                           @RequestParam(value = "searchVal", required = false) String searchVal,
                                           @RequestParam(value = "executorName", required = false) String executorName,
                                           @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                           @RequestParam(value = "host", required = false) String host,
                                           @RequestParam(value = "startDate", required = false) String startTime,
                                           @RequestParam(value = "endDate", required = false) String endTime,
                                           @RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = processInstanceService.queryProcessInstanceList(loginUser, projectCode, processDefineCode, startTime, endTime,
                searchVal, executorName, stateType, host, pageNo, pageSize);
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
    @ApiOperation(value = "queryTaskListByProcessId", notes = "QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskListByProcessId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
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
    @ApiOperation(value = "updateProcessInstance", notes = "UPDATE_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "taskRelationJson", value = "TASK_RELATION_JSON", type = "String"),
        @ApiImplicitParam(name = "taskDefinitionJson", value = "TASK_DEFINITION_JSON", type = "String"),
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "1"),
        @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", type = "String"),
        @ApiImplicitParam(name = "syncDefine", value = "SYNC_DEFINE", required = true, type = "Boolean", example = "false"),
        @ApiImplicitParam(name = "globalParams", value = "PROCESS_GLOBAL_PARAMS", type = "String", example = "[]"),
        @ApiImplicitParam(name = "locations", value = "PROCESS_INSTANCE_LOCATIONS", type = "String"),
        @ApiImplicitParam(name = "timeout", value = "PROCESS_TIMEOUT", type = "Int", example = "0"),
        @ApiImplicitParam(name = "tenantCode", value = "TENANT_CODE", type = "String", example = "default")
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                        @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                        @PathVariable(value = "id") Integer id,
                                        @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
                                        @RequestParam(value = "syncDefine", required = true) Boolean syncDefine,
                                        @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                        @RequestParam(value = "locations", required = false) String locations,
                                        @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                        @RequestParam(value = "tenantCode", required = true) String tenantCode) {
        Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectCode, id,
            taskRelationJson, taskDefinitionJson, scheduleTime, syncDefine, globalParams, locations, timeout, tenantCode);
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
    @ApiOperation(value = "queryProcessInstanceById", notes = "QUERY_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
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
    @ApiOperation(value = "queryTopNLongestRunningProcessInstance", notes = "QUERY_TOPN_LONGEST_RUNNING_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "size", value = "PROCESS_INSTANCE_SIZE", required = true, dataType = "Int", example = "10"),
        @ApiImplicitParam(name = "startTime", value = "PROCESS_INSTANCE_START_TIME", required = true, dataType = "String"),
        @ApiImplicitParam(name = "endTime", value = "PROCESS_INSTANCE_END_TIME", required = true, dataType = "String"),
    })
    @GetMapping(value = "/top-n")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessInstance> queryTopNLongestRunningProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                          @RequestParam("size") Integer size,
                                                                          @RequestParam(value = "startTime", required = true) String startTime,
                                                                          @RequestParam(value = "endTime", required = true) String endTime) {
        Map<String, Object> result = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, startTime, endTime);
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
    @ApiOperation(value = "deleteProcessInstanceById", notes = "DELETE_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessInstance> deleteProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                             @PathVariable("id") Integer id) {
        Map<String, Object> result = processInstanceService.deleteProcessInstanceById(loginUser, projectCode, id);
        return returnDataList(result);
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskId task id
     * @return sub process instance detail
     */
    @ApiOperation(value = "querySubProcessInstanceByTaskCode", notes = "QUERY_SUBPROCESS_INSTANCE_BY_TASK_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/query-sub-by-parent")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result querySubProcessInstanceByTaskId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("taskId") Integer taskId) {
        Map<String, Object> result = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, taskId);
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
    @ApiOperation(value = "queryParentInstanceBySubId", notes = "QUERY_PARENT_PROCESS_INSTANCE_BY_SUB_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "subId", value = "SUB_PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/query-parent-by-sub")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR)
    @AccessLogAnnotation
    public Result queryParentInstanceBySubId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @RequestParam("subId") Integer subId) {
        Map<String, Object> result = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, subId);
        return returnDataList(result);
    }

    /**
     * query process instance global variables and local variables
     *
     * @param loginUser login user
     * @param id process instance id
     * @return variables data
     */
    @ApiOperation(value = "viewVariables", notes = "QUERY_PROCESS_INSTANCE_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR)
    @AccessLogAnnotation
    public Result viewVariables(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
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
    @ApiOperation(value = "vieGanttTree", notes = "VIEW_GANTT_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}/view-gantt")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR)
    @AccessLogAnnotation
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
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
    @ApiOperation(value = "batchDeleteProcessInstanceByIds", notes = "BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectName", value = "PROJECT_NAME", required = true, dataType = "String"),
        @ApiImplicitParam(name = "processInstanceIds", value = "PROCESS_INSTANCE_IDS", required = true, dataType = "String"),
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR)
    @AccessLogAnnotation
    public Result batchDeleteProcessInstanceByIds(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @PathVariable long projectCode,
                                                  @RequestParam("processInstanceIds") String processInstanceIds) {
        // task queue
        Map<String, Object> result = new HashMap<>();
        List<String> deleteFailedIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(",");

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    Map<String, Object> deleteResult = processInstanceService.deleteProcessInstanceById(loginUser, projectCode, processInstanceId);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedIdList.add((String) deleteResult.get(Constants.MSG));
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedIdList.add(MessageFormat.format(Status.PROCESS_INSTANCE_ERROR.getMsg(), strProcessInstanceId));
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
}
