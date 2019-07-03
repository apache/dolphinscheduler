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

import cn.escheduler.api.service.AlertGroupService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.utils.ParameterUtils;
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

import java.util.HashMap;
import java.util.Map;

import static cn.escheduler.api.enums.Status.*;

/**
 * alert group controller
 */
@Api(tags = "ALERT_GROUP_TAG", position = 1)
@RestController
@RequestMapping("alert-group")
public class AlertGroupController extends  BaseController{

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

    @Autowired
    private AlertGroupService alertGroupService;


    /**
     * create alert group
     * @param loginUser
     * @param groupName
     * @param groupType
     * @param desc
     * @return
     */
    @ApiOperation(value = "createAlertgroup", notes= "CREATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupType", value = "GROUP_TYPE", required = true, dataType ="AlertType"),
            @ApiImplicitParam(name = "desc", value = "DESC",  dataType ="String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "groupName") String groupName,
                               @RequestParam(value = "groupType") AlertType groupType,
                               @RequestParam(value = "desc",required = false) String desc) {
        logger.info("loginUser user {}, create alertgroup, groupName: {}, groupType: {}, desc: {}",
                loginUser.getUserName(), groupName, groupType,desc);
        try {
            Map<String, Object> result = alertGroupService.createAlertgroup(loginUser, groupName, groupType,desc);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(CREATE_ALERT_GROUP_ERROR.getMsg(),e);
            return error(CREATE_ALERT_GROUP_ERROR.getCode(),CREATE_ALERT_GROUP_ERROR.getMsg());
        }
    }

    /**
     * alert group list
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "list", notes= "QUERY_ALERT_GROUP_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public Result list(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login  user {}, query all alertGroup",
                loginUser.getUserName());
        try{
            HashMap<String, Object> result = alertGroupService.queryAlertgroup();
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_ALL_ALERTGROUP_ERROR.getMsg(),e);
            return error(QUERY_ALL_ALERTGROUP_ERROR.getCode(),QUERY_ALL_ALERTGROUP_ERROR.getMsg());
        }
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser
     * @param pageNo
     * @param searchVal
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "queryTaskListPaging", notes= "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam("pageNo") Integer pageNo,
                             @RequestParam(value = "searchVal", required = false) String searchVal,
                             @RequestParam("pageSize") Integer pageSize){
        logger.info("login  user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        try{
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != SUCCESS){
                return returnDataListPaging(result);
            }

            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = alertGroupService.listPaging(loginUser, searchVal, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(LIST_PAGING_ALERT_GROUP_ERROR.getMsg(),e);
            return error(LIST_PAGING_ALERT_GROUP_ERROR.getCode(),LIST_PAGING_ALERT_GROUP_ERROR.getMsg());
        }
    }

    /**
     * updateProcessInstance alert group
     * @param loginUser
     * @param id
     * @param groupName
     * @param groupType
     * @param desc
     * @return
     */
    @ApiOperation(value = "updateAlertgroup", notes= "UPDATE_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int",example = "100"),
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupType", value = "GROUP_TYPE", required = true, dataType ="AlertType"),
            @ApiImplicitParam(name = "desc", value = "DESC",  dataType ="String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateAlertgroup(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id,
                                   @RequestParam(value = "groupName") String groupName,
                                   @RequestParam(value = "groupType") AlertType groupType,
                                   @RequestParam(value = "desc",required = false) String desc) {
        logger.info("login  user {}, updateProcessInstance alertgroup, groupName: {}, groupType: {}, desc: {}",
                loginUser.getUserName(), groupName, groupType,desc);
        try {
            Map<String, Object> result = alertGroupService.updateAlertgroup(loginUser, id, groupName, groupType, desc);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(UPDATE_ALERT_GROUP_ERROR.getMsg(),e);
            return error(UPDATE_ALERT_GROUP_ERROR.getCode(),UPDATE_ALERT_GROUP_ERROR.getMsg());
        }
    }

    /**
     * delete alert group by id
     * @param loginUser
     * @param id
     * @return
     */
    @ApiOperation(value = "delAlertgroupById", notes= "DELETE_ALERT_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int",example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result delAlertgroupById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete AlertGroup, id: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = alertGroupService.delAlertgroupById(loginUser, id);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(DELETE_ALERT_GROUP_ERROR.getMsg(),e);
            return error(DELETE_ALERT_GROUP_ERROR.getCode(),DELETE_ALERT_GROUP_ERROR.getMsg());
        }
    }


    /**
     * check alert group exist
     * @param loginUser
     * @param groupName
     * @return
     */
    @ApiOperation(value = "verifyGroupName", notes= "VERIFY_ALERT_GROUP_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
    })
    @GetMapping(value = "/verify-group-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value ="groupName") String groupName
    ) {
        logger.info("login user {}, verfiy group name: {}",
                loginUser.getUserName(),groupName);

        return alertGroupService.verifyGroupName(loginUser, groupName);
    }

    /**
     * grant user
     *
     * @param loginUser
     * @param userIds
     * @return
     */
    @ApiOperation(value = "grantUser", notes= "GRANT_ALERT_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_GROUP_ID", required = true, dataType = "Int",example = "100"),
            @ApiImplicitParam(name = "userIds", value = "USER_IDS", required = true, dataType = "String")
    })
    @PostMapping(value = "/grant-user")
    @ResponseStatus(HttpStatus.OK)
    public Result grantUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "alertgroupId") int  alertgroupId,
                                  @RequestParam(value = "userIds") String userIds) {
        logger.info("login user {}, grant user, alertGroupId: {},userIds : {}", loginUser.getUserName(), alertgroupId,userIds);
        try {
            Map<String, Object> result = alertGroupService.grantUser(loginUser, alertgroupId, userIds);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(ALERT_GROUP_GRANT_USER_ERROR.getMsg(),e);
            return error(ALERT_GROUP_GRANT_USER_ERROR.getCode(),ALERT_GROUP_GRANT_USER_ERROR.getMsg());
        }
    }
}
