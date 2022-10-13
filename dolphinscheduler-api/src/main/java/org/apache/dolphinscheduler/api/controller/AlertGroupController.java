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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * alert group controller
 */
@Api(tags = "ALERT_GROUP_TAG")
@RestController
@RequestMapping("/alert-groups")
public class AlertGroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

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
    @ApiOperation(value = "createAlertgroup", notes = "CREATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "alertInstanceIds", value = "alertInstanceIds", required = true, dataTypeClass = String.class)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "listAlertgroupById", notes = "QUERY_ALERT_GROUP_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERTGROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result list(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

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
    @ApiOperation(value = "queryAlertGroupListPaging", notes = "QUERY_ALERT_GROUP_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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

    @ApiOperation(value = "queryAlertGroupById", notes = "QUERY_ALERT_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", dataTypeClass = int.class, example = "1")
    })
    @PostMapping(value = "/query")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAlertGroupById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "updateAlertgroup", notes = "UPDATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "alertInstanceIds", value = "alertInstanceIds", required = true, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "delAlertgroupById", notes = "DELETE_ALERT_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result delAlertgroupById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "verifyGroupName", notes = "VERIFY_ALERT_GROUP_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataTypeClass = String.class),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "groupName") String groupName) {

        boolean exist = alertGroupService.existGroupName(groupName);
        Result result = new Result();
        if (exist) {
            logger.error("group {} has exist, can't create again.", groupName);
            result.setCode(Status.ALERT_GROUP_EXIST.getCode());
            result.setMsg(Status.ALERT_GROUP_EXIST.getMsg());
        } else {
            result.setCode(Status.SUCCESS.getCode());
            result.setMsg(Status.SUCCESS.getMsg());
        }
        return result;
    }
}
