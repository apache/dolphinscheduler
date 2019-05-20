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
package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.ProcessInstanceService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.queue.TaskQueueFactory;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;

/**
 * process instance controller
 */
@RestController
@RequestMapping("projects/{projectName}/instance")
public class ProcessInstanceController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);


    @Autowired
    ProcessInstanceService processInstanceService;

    /**
     * query process instance list paging
     * 
     * @param loginUser
     * @param projectName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value="list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceList(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param workflowId
     * @return
     */
    @GetMapping(value="/task-list-by-process-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskListByProcessId(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable String projectName,
                                           @RequestParam("processInstanceId") Integer workflowId
    ) {
        try{
            logger.info("query task instance list by process instance id, login user:{}, project name:{}, work instance id:{}",
                    loginUser.getUserName(), projectName, workflowId);
            Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectName, workflowId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg(),e);
            return error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getCode(), QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg());
        }
    }

    /**
     * update process instance
     *
     * @param loginUser
     * @param projectName
     * @param processInstanceJson
     * @param processInstanceId
     * @param scheduleTime
     * @param syncDefine
     * @param flag
     * @return
     */
    @PostMapping(value="/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProcessInstance(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @return
     */
    @GetMapping(value="/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @return
     */
    @GetMapping(value="/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteProcessInstanceById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param taskId
     * @return
     */
    @GetMapping(value="/select-sub-process")
    @ResponseStatus(HttpStatus.OK)
    public Result querySubProcessInstanceByTaskId(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param subId
     * @return
     */
    @GetMapping(value="/select-parent-process")
    @ResponseStatus(HttpStatus.OK)
    public Result queryParentInstanceBySubId(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @PathVariable String projectName,
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
     * @param loginUser
     * @param processInstanceId
     * @return
     */
    @GetMapping(value="/view-variables")
    @ResponseStatus(HttpStatus.OK)
    public Result viewVariables(@RequestAttribute(value = Constants.SESSION_USER) User loginUser
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
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @return
     */
    @GetMapping(value="/view-gantt")
    @ResponseStatus(HttpStatus.OK)
    public Result viewTree(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @PathVariable String projectName,
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
     * @param loginUser
     * @param projectName
     * @param processInstanceIds
     * @return
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
            Map<String, Object> result = processInstanceService.batchDeleteProcessInstanceByIds(loginUser, projectName, processInstanceIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getMsg(),e);
            return error(Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getCode(), Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR.getMsg());
        }
    }
}
