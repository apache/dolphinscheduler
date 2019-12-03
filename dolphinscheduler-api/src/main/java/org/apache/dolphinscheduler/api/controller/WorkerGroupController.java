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
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
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

/**
 * worker group controller
 */
@Api(tags = "WORKER_GROUP_TAG", position = 1)
@RestController
@RequestMapping("/worker-group")
public class WorkerGroupController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupController.class);


    @Autowired
    WorkerGroupService workerGroupService;


    /**
     * create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param ipList ip list
     * @return create or update result code
     */
    @ApiOperation(value = "saveWorkerGroup", notes= "CREATE_WORKER_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", dataType = "Int", example = "10", defaultValue = "0"),
            @ApiImplicitParam(name = "name", value = "WORKER_GROUP_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "ipList", value = "WORKER_IP_LIST", required = true, dataType ="String")
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.OK)
    public Result saveWorkerGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "id", required = false, defaultValue = "0") int id,
                             @RequestParam(value = "name") String name,
                             @RequestParam(value = "ipList") String ipList
                             ) {
        logger.info("save worker group: login user {}, id:{}, name: {}, ipList: {} ",
                loginUser.getUserName(), id, name, ipList);

        try {
            Map<String, Object> result = workerGroupService.saveWorkerGroup(loginUser,id, name, ipList);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.SAVE_ERROR.getMsg(),e);
            return error(Status.SAVE_ERROR.getCode(), Status.SAVE_ERROR.getMsg());
        }
    }

    /**
     * query worker groups paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    @ApiOperation(value = "queryAllWorkerGroupsPaging", notes= "QUERY_WORKER_GROUP_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", dataType = "Int", example = "10", defaultValue = "0"),
            @ApiImplicitParam(name = "name", value = "WORKER_GROUP_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "ipList", value = "WORKER_IP_LIST", required = true, dataType ="String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAllWorkerGroupsPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageSize") Integer pageSize
    ) {
        logger.info("query all worker group paging: login user {}, pageNo:{}, pageSize:{}, searchVal:{}",
                loginUser.getUserName() , pageNo, pageSize, searchVal);

        try {
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = workerGroupService.queryAllGroupPaging(loginUser,pageNo, pageSize, searchVal);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(Status.QUERY_WORKER_GROUP_FAIL.getMsg(),e);
            return error(Status.QUERY_WORKER_GROUP_FAIL.getCode(), Status.QUERY_WORKER_GROUP_FAIL.getMsg());
        }
    }

    /**
     * query all worker groups
     *
     * @param loginUser login user
     * @return all worker group list
     */
    @ApiOperation(value = "queryAllWorkerGroups", notes= "QUERY_WORKER_GROUP_LIST_NOTES")
    @GetMapping(value = "/all-groups")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAllWorkerGroups(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser
    ) {
        logger.info("query all worker group: login user {}",
                loginUser.getUserName() );

        try {
            Map<String, Object> result = workerGroupService.queryAllGroup();
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.QUERY_WORKER_GROUP_FAIL.getMsg(),e);
            return error(Status.QUERY_WORKER_GROUP_FAIL.getCode(), Status.QUERY_WORKER_GROUP_FAIL.getMsg());
        }
    }

    /**
     * delete worker group by id
     * @param loginUser login user
     * @param id group id
     * @return delete result code
     */
    @ApiOperation(value = "deleteById", notes= "DELETE_WORKER_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", required = true, dataType = "Int", example = "10"),

    })
    @GetMapping(value = "/delete-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("id") Integer id
    ) {
        logger.info("delete worker group: login user {}, id:{} ",
                loginUser.getUserName() , id);

        try {
            Map<String, Object> result = workerGroupService.deleteWorkerGroupById(id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.DELETE_WORKER_GROUP_FAIL.getMsg(),e);
            return error(Status.DELETE_WORKER_GROUP_FAIL.getCode(), Status.DELETE_WORKER_GROUP_FAIL.getMsg());
        }
    }
}
