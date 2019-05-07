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


import cn.escheduler.api.service.TaskInstanceService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.QUERY_TASK_LIST_PAGING_ERROR;

/**
 * task instance controller
 */
@RestController
@RequestMapping("/projects/{projectName}/task-instance")
public class TaskInstanceController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceController.class);

    @Autowired
    TaskInstanceService taskInstanceService;


    /**
     * query task list paging
     *
     * @param loginUser
     * @return
     */
    @GetMapping("/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskListPaging(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @PathVariable String projectName,
                                      @RequestParam(value = "processInstanceId", required = false, defaultValue = "0") Integer processInstanceId,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "taskName", required = false) String taskName,
                                      @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                      @RequestParam(value = "host", required = false) String host,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize){

        try{
            logger.info("query task instance list, project name:{},process instance:{}, search value:{},task name:{}, state type:{}, host:{}, start:{}, end:{}",
                    projectName, processInstanceId, searchVal, taskName, stateType, host, startTime, endTime);
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = taskInstanceService.queryTaskListPaging(
                    loginUser, projectName, processInstanceId, taskName, startTime, endTime, searchVal, stateType, host, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_TASK_LIST_PAGING_ERROR.getMsg(),e);
            return error(QUERY_TASK_LIST_PAGING_ERROR.getCode(), QUERY_TASK_LIST_PAGING_ERROR.getMsg());
        }

    }

}
