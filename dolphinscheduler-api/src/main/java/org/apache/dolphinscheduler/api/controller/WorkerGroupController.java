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
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * worker group controller
 */
@Tag(name = "WORKER_GROUP_TAG")
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
    @Operation(summary = "saveWorkerGroup", description = "CREATE_WORKER_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "WORKER_GROUP_ID", schema = @Schema(implementation = int.class, example = "10", defaultValue = "0")),
            @Parameter(name = "name", description = "WORKER_GROUP_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "addrList", description = "WORKER_ADDR_LIST", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "WORKER_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "otherParamsJson", description = "WORKER_PARMS_JSON", required = false, schema = @Schema(implementation = String.class)),
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SAVE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result saveWorkerGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "id", required = false, defaultValue = "0") int id,
                                  @RequestParam(value = "name") String name,
                                  @RequestParam(value = "addrList") String addrList,
                                  @RequestParam(value = "description", required = false, defaultValue = "") String description,
                                  @RequestParam(value = "otherParamsJson", required = false, defaultValue = "") String otherParamsJson) {
        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, id, name, addrList, description, otherParamsJson);
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
    @Operation(summary = "queryAllWorkerGroupsPaging", description = "QUERY_WORKER_GROUP_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllWorkerGroupsPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @Operation(summary = "queryAllWorkerGroups", description = "QUERY_WORKER_GROUP_LIST_NOTES")
    @GetMapping(value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllWorkerGroups(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
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
    @Operation(summary = "deleteWorkerGroupById", description = "DELETE_WORKER_GROUP_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "WORKER_GROUP_ID", required = true, schema = @Schema(implementation = int.class, example = "10")),
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_WORKER_GROUP_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteWorkerGroupById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @Operation(summary = "queryWorkerAddressList", description = "QUERY_WORKER_ADDRESS_LIST_NOTES")
    @GetMapping(value = "/worker-address-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_ADDRESS_LIST_FAIL)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryWorkerAddressList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = workerGroupService.getWorkerAddressList();
        return returnDataList(result);
    }

}
