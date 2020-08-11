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
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * alert group controller
 */
@Api(tags = "ALERT_GROUP_TAG", position = 1)
@RestController
@RequestMapping("alert-group")
public class AlertGroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

    @Autowired
    private AlertGroupService alertGroupService;


    /**
     * create alert group
     *
     * @param loginUser   login user
     * @param groupName   group name
     * @param groupType   group type
     * @param description description
     * @return create result code
     */
    @ApiOperation(value = "createAlertgroup", notes = "CREATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupType", value = "GROUP_TYPE", required = true, dataType = "AlertType"),
            @ApiImplicitParam(name = "description", value = "DESC", dataType = "String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_GROUP_ERROR)
    public Result createAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "groupName") String groupName,
                                   @RequestParam(value = "groupType") AlertType groupType,
                                   @RequestParam(value = "description", required = false) String description) {
        logger.info("loginUser user {}, create alertgroup, groupName: {}, groupType: {}, desc: {}",
                loginUser.getUserName(), groupName, groupType, description);
        Map<String, Object> result = alertGroupService.createAlertgroup(loginUser, groupName, groupType, description);
        return returnDataList(result);
    }

    /**
     * alert group list
     *
     * @param loginUser login user
     * @return alert group list
     */
    @ApiOperation(value = "list", notes = "QUERY_ALERT_GROUP_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERTGROUP_ERROR)
    public Result list(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login  user {}, query all alertGroup",
                loginUser.getUserName());
        HashMap<String, Object> result = alertGroupService.queryAlertgroup();
        return returnDataList(result);
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return alert group list page
     */
    @ApiOperation(value = "queryAlertGroupListPaging", notes = "QUERY_ALERT_GROUP_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_GROUP_ERROR)
    public Result listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam("pageNo") Integer pageNo,
                             @RequestParam(value = "searchVal", required = false) String searchVal,
                             @RequestParam("pageSize") Integer pageSize) {
        logger.info("login  user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(), pageNo, searchVal, pageSize);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = alertGroupService.listPaging(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser   login user
     * @param id          alert group id
     * @param groupName   group name
     * @param groupType   group type
     * @param description description
     * @return update result code
     */
    @ApiOperation(value = "updateAlertgroup", notes = "UPDATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupType", value = "GROUP_TYPE", required = true, dataType = "AlertType"),
            @ApiImplicitParam(name = "description", value = "DESC", dataType = "String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_GROUP_ERROR)
    public Result updateAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id,
                                   @RequestParam(value = "groupName") String groupName,
                                   @RequestParam(value = "groupType") AlertType groupType,
                                   @RequestParam(value = "description", required = false) String description) {
        logger.info("login  user {}, updateProcessInstance alertgroup, groupName: {}, groupType: {}, desc: {}",
                loginUser.getUserName(), groupName, groupType, description);
        Map<String, Object> result = alertGroupService.updateAlertgroup(loginUser, id, groupName, groupType, description);
        return returnDataList(result);
    }

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id        alert group id
     * @return delete result code
     */
    @ApiOperation(value = "delAlertgroupById", notes = "DELETE_ALERT_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_GROUP_ERROR)
    public Result delAlertgroupById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete AlertGroup, id: {},", loginUser.getUserName(), id);
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
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
    })
    @GetMapping(value = "/verify-group-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "groupName") String groupName) {
        logger.info("login user {}, verify group name: {}", loginUser.getUserName(), groupName);

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

    /**
     * grant user
     *
     * @param loginUser    login user
     * @param userIds      user ids in the group
     * @param alertgroupId alert group id
     * @return grant result code
     */
    @ApiOperation(value = "grantUser", notes = "GRANT_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "userIds", value = "USER_IDS", required = true, dataType = "String")
    })
    @PostMapping(value = "/grant-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ALERT_GROUP_GRANT_USER_ERROR)
    public Result grantUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                            @RequestParam(value = "alertgroupId") int alertgroupId,
                            @RequestParam(value = "userIds") String userIds) {
        logger.info("login user {}, grant user, alertGroupId: {},userIds : {}", loginUser.getUserName(), alertgroupId, userIds);
        Map<String, Object> result = alertGroupService.grantUser(loginUser, alertgroupId, userIds);
        return returnDataList(result);
    }
}
