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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * process instance controller
 */
@Api(tags = "PROCESS_INSTANCE_TAG", position = 10)
@RestController
@RequestMapping("projects/{projectName}/instance")
public class ProcessInstanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);


    @Autowired
    ProcessInstanceService processInstanceService;

    /**
     * query process instance list paging
     *
     * @param loginUser           login user
     * @param projectName         project name
     * @param pageNo              page number
     * @param pageSize            page size
     * @param processDefinitionId process definition id
     * @param searchVal           search value
     * @param stateType           state type
     * @param host                host
     * @param startTime           start time
     * @param endTime             end time
     * @return process instance list
     */
    @ApiOperation(value = "queryProcessInstanceList", notes = "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
            @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", type = "String"),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type = "ExecutionStatus"),
            @ApiImplicitParam(name = "host", value = "HOST", type = "String"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "100")
    })
    @GetMapping(value = "list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    public Result queryProcessInstanceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam(value = "processDefinitionId", required = false, defaultValue = "0") Integer processDefinitionId,
                                           @RequestParam(value = "searchVal", required = false) String searchVal,
                                           @RequestParam(value = "executorName", required = false) String executorName,
                                           @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                           @RequestParam(value = "host", required = false) String host,
                                           @RequestParam(value = "startDate", required = false) String startTime,
                                           @RequestParam(value = "endDate", required = false) String endTime,
                                           @RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {
        logger.info("query all process instance list, login user:{},project name:{}, define id:{}," +
                        "search value:{},executor name:{},state type:{},host:{},start time:{}, end time:{},page number:{}, page size:{}",
                loginUser.getUserName(), projectName, processDefinitionId, searchVal, executorName, stateType, host,
                startTime, endTime, pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        Map<String, Object> result = processInstanceService.queryProcessInstanceList(
                loginUser, projectName, processDefinitionId, startTime, endTime, searchVal, executorName, stateType, host, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return task list for the process instance
     */
    @ApiOperation(value = "queryTaskListByProcessId", notes = "QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/task-list-by-process-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR)
    public Result queryTaskListByProcessId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam("processInstanceId") Integer processInstanceId
    ) throws IOException {
        logger.info("query task instance list by process instance id, login user:{}, project name:{}, process instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId);
        Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectName, processInstanceId);
        return returnDataList(result);
    }

    /**
     * update process instance
     *
     * @param loginUser           login user
     * @param projectName         project name
     * @param processInstanceJson process instance json
     * @param processInstanceId   process instance id
     * @param scheduleTime        schedule time
     * @param syncDefine          sync define
     * @param flag                flag
     * @param locations           locations
     * @param connects            connects
     * @return update result code
     */
    @ApiOperation(value = "updateProcessInstance", notes = "UPDATE_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceJson", value = "PROCESS_INSTANCE_JSON", type = "String"),
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", type = "String"),
            @ApiImplicitParam(name = "syncDefine", value = "SYNC_DEFINE", type = "Boolean"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_INSTANCE_LOCATIONS", type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_INSTANCE_CONNECTS", type = "String"),
            @ApiImplicitParam(name = "flag", value = "RECOVERY_PROCESS_INSTANCE_FLAG", type = "Flag"),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_INSTANCE_ERROR)
    public Result updateProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "processInstanceJson", required = false) String processInstanceJson,
                                        @RequestParam(value = "processInstanceId") Integer processInstanceId,
                                        @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
                                        @RequestParam(value = "syncDefine", required = true) Boolean syncDefine,
                                        @RequestParam(value = "locations", required = false) String locations,
                                        @RequestParam(value = "connects", required = false) String connects,
                                        @RequestParam(value = "flag", required = false) Flag flag
    ) throws ParseException {
        logger.info("updateProcessInstance process instance, login user:{}, project name:{}, process instance json:{}," +
                        "process instance id:{}, schedule time:{}, sync define:{}, flag:{}, locations:{}, connects:{}",
                loginUser.getUserName(), projectName, processInstanceJson, processInstanceId, scheduleTime,
                syncDefine, flag, locations, connects);
        Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectName,
                processInstanceId, processInstanceJson, scheduleTime, syncDefine, flag, locations, connects);
        return returnDataList(result);
    }

    /**
     * query process instance by id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return process instance detail
     */
    @ApiOperation(value = "queryProcessInstanceById", notes = "QUERY_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    public Result queryProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam("processInstanceId") Integer processInstanceId
    ) {
        logger.info("query process instance detail by id, login user:{},project name:{}, process instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId);
        Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, projectName, processInstanceId);
        return returnDataList(result);
    }

    /**
     * delete process instance by id, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessInstanceById", notes = "DELETE_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_INSTANCE_BY_ID_ERROR)
    public Result deleteProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                            @RequestParam("processInstanceId") Integer processInstanceId
    ) {
        logger.info("delete process instance by id, login user:{}, project name:{}, process instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId);
        // task queue
        Map<String, Object> result = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId);
        return returnDataList(result);
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param taskId      task id
     * @return sub process instance detail
     */
    @ApiOperation(value = "querySubProcessInstanceByTaskId", notes = "QUERY_SUBPROCESS_INSTANCE_BY_TASK_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "TASK_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-sub-process")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR)
    public Result querySubProcessInstanceByTaskId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                  @RequestParam("taskId") Integer taskId) {
        Map<String, Object> result = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, taskId);
        return returnDataList(result);
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param subId       sub process id
     * @return parent instance detail
     */
    @ApiOperation(value = "queryParentInstanceBySubId", notes = "QUERY_PARENT_PROCESS_INSTANCE_BY_SUB_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subId", value = "SUB_PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-parent-process")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR)
    public Result queryParentInstanceBySubId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                             @RequestParam("subId") Integer subId) {
        Map<String, Object> result = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, subId);
        return returnDataList(result);
    }

    /**
     * query process instance global variables and local variables
     *
     * @param loginUser         login user
     * @param processInstanceId process instance id
     * @return variables data
     */
    @ApiOperation(value = "viewVariables", notes = "QUERY_PROCESS_INSTANCE_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR)
    public Result viewVariables(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("processInstanceId") Integer processInstanceId) throws Exception {
        Map<String, Object> result = processInstanceService.viewVariables(processInstanceId);
        return returnDataList(result);
    }

    /**
     * encapsulation gantt structure
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return gantt tree data
     */
    @ApiOperation(value = "vieGanttTree", notes = "VIEW_GANTT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view-gantt")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR)
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                           @RequestParam("processInstanceId") Integer processInstanceId) throws Exception {
        Map<String, Object> result = processInstanceService.viewGantt(processInstanceId);
        return returnDataList(result);
    }

    /**
     * batch delete process instance by ids, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser          login user
     * @param projectName        project name
     * @param processInstanceIds process instance id
     * @return delete result code
     */
    @GetMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR)
    public Result batchDeleteProcessInstanceByIds(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @PathVariable String projectName,
                                                  @RequestParam("processInstanceIds") String processInstanceIds
    ) {
        logger.info("delete process instance by ids, login user:{}, project name:{}, process instance ids :{}",
                loginUser.getUserName(), projectName, processInstanceIds);
        // task queue
        Map<String, Object> result = new HashMap<>(5);
        List<String> deleteFailedIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(",");

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    Map<String, Object> deleteResult = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedIdList.add(strProcessInstanceId);
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedIdList.add(strProcessInstanceId);
                }
            }
        }
        if (!deleteFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return returnDataList(result);
    }
}
