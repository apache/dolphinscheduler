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


import cn.escheduler.api.service.TaskRecordService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static cn.escheduler.api.enums.Status.QUERY_TASK_RECORD_LIST_PAGING_ERROR;

/**
 * data quality controller
 */
@ApiIgnore
@RestController
@RequestMapping("/projects/task-record")
public class TaskRecordController extends BaseController{


    private static final Logger logger = LoggerFactory.getLogger(TaskRecordController.class);


    @Autowired
    TaskRecordService taskRecordService;

    /**
     * query task record list paging
     *
     * @param loginUser
     * @return
     */
    @GetMapping("/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskRecordListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "taskName", required = false) String taskName,
                                            @RequestParam(value = "state", required = false) String state,
                                            @RequestParam(value = "sourceTable", required = false) String sourceTable,
                                            @RequestParam(value = "destTable", required = false) String destTable,
                                            @RequestParam(value = "taskDate", required = false) String taskDate,
                                            @RequestParam(value = "startDate", required = false) String startTime,
                                            @RequestParam(value = "endDate", required = false) String endTime,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize
                                      ){

   try{
        logger.info("query task record list, task name:{}, state :{}, taskDate: {}, start:{}, end:{}",
                taskName, state,  taskDate, startTime, endTime);
        Map<String, Object> result = taskRecordService.queryTaskRecordListPaging(false, taskName, startTime,  taskDate, sourceTable, destTable, endTime,state, pageNo, pageSize);
        return returnDataListPaging(result);
    }catch (Exception e){
        logger.error(QUERY_TASK_RECORD_LIST_PAGING_ERROR.getMsg(),e);
        return error(QUERY_TASK_RECORD_LIST_PAGING_ERROR.getCode(), QUERY_TASK_RECORD_LIST_PAGING_ERROR.getMsg());
    }

    }

    /**
     * query history task record list paging
     *
     * @param loginUser
     * @return
     */
    @GetMapping("/history-list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryHistoryTaskRecordListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "taskName", required = false) String taskName,
                                            @RequestParam(value = "state", required = false) String state,
                                            @RequestParam(value = "sourceTable", required = false) String sourceTable,
                                            @RequestParam(value = "destTable", required = false) String destTable,
                                            @RequestParam(value = "taskDate", required = false) String taskDate,
                                            @RequestParam(value = "startDate", required = false) String startTime,
                                            @RequestParam(value = "endDate", required = false) String endTime,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize
    ){

        try{
            logger.info("query hisotry task record list, task name:{}, state :{}, taskDate: {}, start:{}, end:{}",
                    taskName, state,  taskDate, startTime, endTime);
            Map<String, Object> result = taskRecordService.queryTaskRecordListPaging(true, taskName, startTime,  taskDate, sourceTable, destTable, endTime,state, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_TASK_RECORD_LIST_PAGING_ERROR.getMsg(),e);
            return error(QUERY_TASK_RECORD_LIST_PAGING_ERROR.getCode(), QUERY_TASK_RECORD_LIST_PAGING_ERROR.getMsg());
        }

    }

}
