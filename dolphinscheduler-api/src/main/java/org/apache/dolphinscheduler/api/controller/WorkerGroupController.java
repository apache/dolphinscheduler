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


import org.apache.dolphinscheduler.api.exceptions.ApiException;
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

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * worker group controller
 */
@Api(tags = "WORKER_GROUP_TAG", position = 1)
@RestController
@RequestMapping("/worker-group")
public class WorkerGroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupController.class);

    @Autowired
    WorkerGroupService workerGroupService;




    /**
     * query worker groups paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return worker group list page
     */
    @ApiOperation(value = "queryAllWorkerGroupsPaging", notes = "QUERY_WORKER_GROUP_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", dataType = "Int", example = "10", defaultValue = "0"),
            @ApiImplicitParam(name = "name", value = "WORKER_GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "ipList", value = "WORKER_IP_LIST", required = true, dataType = "String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    public Result queryAllWorkerGroupsPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageSize") Integer pageSize
    ) {
        logger.info("query all worker group paging: login user {}, pageNo:{}, pageSize:{}, searchVal:{}",
                loginUser.getUserName(), pageNo, pageSize, searchVal);

        searchVal = ParameterUtils.handleEscapes(searchVal);
        Map<String, Object> result = workerGroupService.queryAllGroupPaging(loginUser, pageNo, pageSize, searchVal);
        return returnDataListPaging(result);
    }

    /**
     * query all worker groups
     *
     * @param loginUser login user
     * @return all worker group list
     */
    @ApiOperation(value = "queryAllWorkerGroups", notes = "QUERY_WORKER_GROUP_LIST_NOTES")
    @GetMapping(value = "/all-groups")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    public Result queryAllWorkerGroups(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser
    ) {
        logger.info("query all worker group: login user {}",
                loginUser.getUserName());

        Map<String, Object> result = workerGroupService.queryAllGroup();
        return returnDataList(result);
    }


}
