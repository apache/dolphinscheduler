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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static cn.escheduler.api.enums.Status.*;

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
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createAlertgroup(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public Result list(@RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
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
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result listPaging(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateAlertgroup(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result delAlertgroupById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @GetMapping(value = "/verify-group-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @PostMapping(value = "/grant-user")
    @ResponseStatus(HttpStatus.OK)
    public Result grantUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
