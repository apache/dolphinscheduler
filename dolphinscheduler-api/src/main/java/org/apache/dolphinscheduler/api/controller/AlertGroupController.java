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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ALERT_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ALERT_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_ALERT_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALERT_GROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALL_ALERTGROUP_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ALERT_GROUP_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
 * alert group controller
 */
@Tag(name = "ALERT_GROUP_TAG")
@RestController
@RequestMapping("/alert-groups")
@Slf4j
public class AlertGroupController extends BaseController {

    @Autowired
    private AlertGroupService alertGroupService;

    /**
     * create alert group
     *
     * @param loginUser login user
     * @param groupName group name
     * @param description description
     * @return create result code
     */
    @Operation(summary = "createAlertgroup", description = "CREATE_ALERT_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "groupName", description = "GROUP_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "alertInstanceIds", description = "alertInstanceIds", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createAlertgroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "groupName") String groupName,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "alertInstanceIds") String alertInstanceIds) {
        Map<String, Object> result =
                alertGroupService.createAlertgroup(loginUser, groupName, description, alertInstanceIds);
        return returnDataList(result);
    }

    /**
     * alert group list
     *
     * @param loginUser login user
     * @return alert group list
     */
    @Operation(summary = "listAlertgroupById", description = "QUERY_ALERT_GROUP_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERTGROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result list(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Object> result = alertGroupService.queryAlertgroup(loginUser);
        return returnDataList(result);
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return alert group list page
     */
    @Operation(summary = "queryAlertGroupListPaging", description = "QUERY_ALERT_GROUP_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "searchVal", required = false) String searchVal,
                             @RequestParam("pageNo") Integer pageNo,
                             @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return alertGroupService.listPaging(loginUser, searchVal, pageNo, pageSize);
    }
    /**
     * check alarm group detail by Id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return one alert group
     */

    @Operation(summary = "queryAlertGroupById", description = "QUERY_ALERT_GROUP_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "ALERT_GROUP_ID", schema = @Schema(implementation = int.class, example = "1"))
    })
    @PostMapping(value = "/query")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAlertGroupById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam("id") Integer id) {

        Map<String, Object> result = alertGroupService.queryAlertGroupById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser login user
     * @param id alert group id
     * @param groupName group name
     * @param description description
     * @return update result code
     */
    @Operation(summary = "updateAlertgroup", description = "UPDATE_ALERT_GROUP_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "ALERT_GROUP_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "groupName", description = "GROUP_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "alertInstanceIds", description = "ALERT_INSTANCE_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateAlertgroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @PathVariable(value = "id") int id,
                                   @RequestParam(value = "groupName") String groupName,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "alertInstanceIds") String alertInstanceIds) {

        Map<String, Object> result =
                alertGroupService.updateAlertgroup(loginUser, id, groupName, description, alertInstanceIds);
        return returnDataList(result);
    }

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return delete result code
     */
    @Operation(summary = "delAlertgroupById", description = "DELETE_ALERT_GROUP_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "ALERT_GROUP_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result delAlertgroupById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @PathVariable(value = "id") int id) {
        Map<String, Object> result = alertGroupService.delAlertgroupById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * check alert group exist
     *
     * @param loginUser login user
     * @param groupName group name
     * @return check result code
     */
    @Operation(summary = "verifyGroupName", description = "VERIFY_ALERT_GROUP_NAME_NOTES")
    @Parameters({
            @Parameter(name = "groupName", description = "GROUP_NAME", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyGroupName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "groupName") String groupName) {

        boolean exist = alertGroupService.existGroupName(groupName);
        Result result = new Result();
        if (exist) {
            log.error("group {} has exist, can't create again.", groupName);
            result.setCode(Status.ALERT_GROUP_EXIST.getCode());
            result.setMsg(Status.ALERT_GROUP_EXIST.getMsg());
        } else {
            result.setCode(Status.SUCCESS.getCode());
            result.setMsg(Status.SUCCESS.getMsg());
        }
        return result;
    }
}
