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
import cn.escheduler.api.service.WorkerGroupService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * worker group controller
 */
@RestController
@RequestMapping("/worker-group")
public class WorkerGroupController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupController.class);


    @Autowired
    WorkerGroupService workerGroupService;


    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.OK)
    public Result saveWorkerGroup(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "id") int id,
                             @RequestParam(value = "name") String name,
                             @RequestParam(value = "ipList") String ipList
                             ) {
        logger.info("save worker group: login user {}, id:{}, name: {}, ipList: {} ",
                loginUser.getUserName(), id, name, ipList);

        try {
            Map<String, Object> result = workerGroupService.saveWorkerGroup(id, name, ipList);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.SAVE_ERROR.getMsg(),e);
            return error(Status.SAVE_ERROR.getCode(), Status.SAVE_ERROR.getMsg());
        }
    }

    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAllWorkerGroupsPaging(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageSize") Integer pageSize
    ) {
        logger.info("query all worker group: login user {}, pageNo:{}, pageSize:{}, searchVal:{}",
                loginUser.getUserName() , pageNo, pageSize, searchVal);

        try {
            Map<String, Object> result = workerGroupService.queryAllGroupPaging(pageNo, pageSize, searchVal);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(Status.SAVE_ERROR.getMsg(),e);
            return error(Status.SAVE_ERROR.getCode(), Status.SAVE_ERROR.getMsg());
        }
    }

    @GetMapping(value = "/delete-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("id") Integer id
    ) {
        logger.info("delete worker group: login user {}, id:{} ",
                loginUser.getUserName() , id);

        try {
            Map<String, Object> result = workerGroupService.deleteWorkerGroupById(id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.SAVE_ERROR.getMsg(),e);
            return error(Status.SAVE_ERROR.getCode(), Status.SAVE_ERROR.getMsg());
        }
    }
}
