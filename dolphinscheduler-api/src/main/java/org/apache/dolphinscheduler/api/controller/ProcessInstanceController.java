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
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.queue.ITaskQueue;
import org.apache.dolphinscheduler.common.queue.TaskQueueFactory;
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
public class ProcessInstanceController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);


    @Autowired
    ProcessInstanceService processInstanceService;

    /**
     * query process instance list paging
     * 
     * @param loginUser login user
     * @param projectName project name
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefinitionId process definition id
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @return process instance list
     */
    @ApiOperation(value = "queryProcessInstanceList", notes= "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type ="String"),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type ="ExecutionStatus"),
            @ApiImplicitParam(name = "host", value = "HOST", type ="String"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type ="String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "100")
    })
    @GetMapping(value="list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                   @RequestParam(value = "processDefinitionId", required = false, defaultValue = "0") Integer processDefinitionId,
                                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                   @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                                                   @RequestParam(value = "host", required = false) String host,
                                                                   @RequestParam(value = "startDate", required = false) String startTime,
                                                                   @RequestParam(value = "endDate", required = false) String endTime,
                                                                   @RequestParam("pageNo") Integer pageNo,
                                                                   @RequestParam("pageSize") Integer pageSize){
        try{
            logger.info("query all process instance list, login user:{},project name:{}, define id:{}," +
                    "search value:{},state type:{},host:{},start time:{}, end time:{},page number:{}, page size:{}",
                    loginUser.getUserName(), projectName, processDefinitionId, searchVal, stateType,host,
                    startTime, endTime, pageNo, pageSize);
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = processInstanceService.queryProcessInstanceList(
                    loginUser, projectName, processDefinitionId, startTime, endTime, searchVal, stateType, host, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getCode(), Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return task list for the process instance
     */
    @ApiOperation(value = "queryTaskListByProcessId", notes= "QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/task-list-by-process-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskListByProcessId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam("processInstanceId") Integer processInstanceId
    ) {
        try{
            logger.info("query task instance list by process instance id, login user:{}, project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg(),e);
            return error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getCode(), QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg());
        }
    }

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceJson process instance json
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param flag flag
     * @param locations locations
     * @param connects connects
     * @return update result code
     */
    @ApiOperation(value = "updateProcessInstance", notes= "UPDATE_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceJson", value = "PROCESS_INSTANCE_JSON", type = "String"),
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", type = "String"),
            @ApiImplicitParam(name = "syncDefine", value = "SYNC_DEFINE", type = "Boolean"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_INSTANCE_LOCATIONS", type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_INSTANCE_CONNECTS", type = "String"),
            @ApiImplicitParam(name = "flag", value = "RECOVERY_PROCESS_INSTANCE_FLAG", type = "Flag"),
    })
    @PostMapping(value="/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam( value = "processInstanceJson", required = false) String processInstanceJson,
                                        @RequestParam( value = "processInstanceId") Integer processInstanceId,
                                        @RequestParam( value = "scheduleTime", required = false) String scheduleTime,
                                        @RequestParam( value = "syncDefine", required = true) Boolean syncDefine,
                                        @RequestParam(value = "locations", required = false) String locations,
                                        @RequestParam(value = "connects", required = false) String connects,
                                        @RequestParam( value = "flag", required = false) Flag flag
    ){
        try{
            logger.info("updateProcessInstance process instance, login user:{}, project name:{}, process instance json:{}," +
                    "process instance id:{}, schedule time:{}, sync define:{}, flag:{}, locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, processInstanceJson, processInstanceId, scheduleTime,
                    syncDefine, flag, locations, connects);
            Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectName,
                    processInstanceId, processInstanceJson, scheduleTime, syncDefine, flag, locations, connects);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UPDATE_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.UPDATE_PROCESS_INSTANCE_ERROR.getCode(), Status.UPDATE_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return process instance detail
     */
    @ApiOperation(value = "queryProcessInstanceById", notes= "QUERY_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                     @RequestParam("processInstanceId") Integer processInstanceId
    ){
        try{
            logger.info("query process instance detail by id, login user:{},project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getCode(), Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * delete process instance by id, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessInstanceById", notes= "DELETE_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                     @RequestParam("processInstanceId") Integer processInstanceId
    ){
        try{
            logger.info("delete process instance by id, login user:{}, project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            // task queue
            ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();
            Map<String, Object> result = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId,tasksQueue);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getCode(), Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskId task id
     * @return sub process instance detail
     */
    @ApiOperation(value = "querySubProcessInstanceByTaskId", notes= "QUERY_SUBPROCESS_INSTANCE_BY_TASK_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "TASK_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/select-sub-process")
    @ResponseStatus(HttpStatus.OK)
    public Result querySubProcessInstanceByTaskId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                  @RequestParam("taskId") Integer taskId){
        try{
            Map<String, Object> result = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, taskId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR.getMsg(),e);
            return error(Status.QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR.getCode(), Status.QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR.getMsg());
        }
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param subId sub process id
     * @return parent instance detail
     */
    @ApiOperation(value = "queryParentInstanceBySubId", notes= "QUERY_PARENT_PROCESS_INSTANCE_BY_SUB_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subId", value = "SUB_PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/select-parent-process")
    @ResponseStatus(HttpStatus.OK)
    public Result queryParentInstanceBySubId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                             @RequestParam("subId") Integer subId){
        try{
            Map<String, Object> result = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, subId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR.getMsg(),e);
            return error(Status.QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR.getCode(), Status.QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR.getMsg());
        }
    }

    /**
     * query process instance global variables and local variables
     *
     * @param loginUser login user
     * @param processInstanceId process instance id
     * @return variables data
     */
    @ApiOperation(value = "viewVariables", notes= "QUERY_PROCESS_INSTANCE_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/view-variables")
    @ResponseStatus(HttpStatus.OK)
    public Result viewVariables(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser
            , @RequestParam("processInstanceId") Integer processInstanceId){
        try{
            Map<String, Object> result = processInstanceService.viewVariables(processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR.getCode(), Status.QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR.getMsg());
        }
    }

    /**
     * encapsulation gantt structure
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return gantt tree data
     */
    @ApiOperation(value = "vieGanttTree", notes= "VIEW_GANTT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/view-gantt")
    @ResponseStatus(HttpStatus.OK)
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                   @RequestParam("processInstanceId") Integer processInstanceId){
        try{
            Map<String, Object> result = processInstanceService.viewGantt(processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR.getMsg(),e);
            return error(Status.ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR.getCode(),ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR.getMsg());
        }
    }

    /**
     * batch delete process instance by ids, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceIds process instance id
     * @return delete result code
     */
    @GetMapping(value="/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    public Result batchDeleteProcessInstanceByIds(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable String projectName,
                                            @RequestParam("processInstanceIds") String processInstanceIds
    ){
        try{
            logger.info("delete process instance by ids, login user:{}, project name:{}, process instance ids :{}",
                    loginUser.getUserName(), projectName, processInstanceIds);
            // task queue
            ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();
            Map<String, Object> result = new HashMap<>(5);
            List<Integer> deleteFailedIdList = new ArrayList<Integer>();
            if(StringUtils.isNotEmpty(processInstanceIds)){
                String[] processInstanceIdArray = processInstanceIds.split(",");

                for (String strProcessInstanceId:processInstanceIdArray) {
                    int processInstanceId = Integer.parseInt(strProcessInstanceId);
                    try {
                        Map<String, Object> deleteResult = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId,tasksQueue);
                        if(!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))){
                            deleteFailedIdList.add(processInstanceId);
                            logger.error((String)deleteResult.get(Constants.MSG));
                        }
                    } catch (Exception e) {
                        deleteFailedIdList.add(processInstanceId);
                    }
                }
            }
            if(deleteFailedIdList.size() > 0){
                putMsg(result, Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR,StringUtils.join(deleteFailedIdList,","));
            }else{
                putMsg(result, Status.SUCCESS);
            }

            return returnDataList(result);
        }catch (Exception e){
            logger.error(BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getMsg(),e);
            return error(Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getCode(), Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getMsg());
        }
    }
}
