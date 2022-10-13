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

import static org.apache.dolphinscheduler.api.enums.Status.DELETE_WORKER_GROUP_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKER_ADDRESS_LIST_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKER_GROUP_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.SAVE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * worker group controller
 */
@Api(tags = "WORKER_GROUP_TAG")
@RestController
@RequestMapping("/worker-groups")
public class WorkerGroupController extends BaseController {

    @Autowired
    WorkerGroupService workerGroupService;

    /**
     * create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param addrList addr list
     * @return create or update result code
     */
    @ApiOperation(value = "saveWorkerGroup", notes = "CREATE_WORKER_GROUP_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", dataType = "Int", example = "10", defaultValue = "0"),
        @ApiImplicitParam(name = "name", value = "WORKER_GROUP_NAME", required = true, dataType = "String"),
        @ApiImplicitParam(name = "addrList", value = "WORKER_ADDR_LIST", required = true, dataType = "String"),
        @ApiImplicitParam(name = "description", value = "WORKER_DESC", required = false, dataType = "String"),
        @ApiImplicitParam(name = "otherParamsJson", value = "WORKER_PARMS_JSON", required = false, dataType = "String"),
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SAVE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result saveWorkerGroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "id", required = false, defaultValue = "0") int id,
                                  @RequestParam(value = "name") String name,
                                  @RequestParam(value = "addrList") String addrList,
                                  @RequestParam(value = "description",required = false, defaultValue = "") String description,
                                  @RequestParam(value = "otherParamsJson",required = false, defaultValue = "") String otherParamsJson
    ) {
        Map<String, Object> result = workerGroupService.saveWorkerGroup(loginUser, id, name, addrList, description, otherParamsJson);
        return returnDataList(result);
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
    @ApiOperation(value = "queryAllWorkerGroupsPaging", notes = "QUERY_WORKER_GROUP_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class)
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllWorkerGroupsPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam(value = "searchVal", required = false) String searchVal) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;

        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = workerGroupService.queryAllGroupPaging(loginUser, pageNo, pageSize, searchVal);
        return result;
    }

    /**
     * query all worker groups
     *
     * @param loginUser login user
     * @return all worker group list
     */
    @ApiOperation(value = "queryAllWorkerGroups", notes = "QUERY_WORKER_GROUP_LIST_NOTES")
    @GetMapping(value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllWorkerGroups(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = workerGroupService.queryAllGroup(loginUser);
        return returnDataList(result);
    }

    /**
     * delete worker group by id
     *
     * @param loginUser login user
     * @param id group id
     * @return delete result code
     */
    @ApiOperation(value = "deleteWorkerGroupById", notes = "DELETE_WORKER_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", required = true, dataTypeClass = int.class, example = "10"),
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteWorkerGroupById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable("id") Integer id) {
        Map<String, Object> result = workerGroupService.deleteWorkerGroupById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * query worker address list
     *
     * @param loginUser login user
     * @return all worker address list
     */
    @ApiOperation(value = "queryWorkerAddressList", notes = "QUERY_WORKER_ADDRESS_LIST_NOTES")
    @GetMapping(value = "/worker-address-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_ADDRESS_LIST_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkerAddressList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = workerGroupService.getWorkerAddressList();
        return returnDataList(result);
    }

}
