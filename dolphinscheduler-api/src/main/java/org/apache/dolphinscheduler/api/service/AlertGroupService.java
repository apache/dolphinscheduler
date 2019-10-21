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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Constants;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * alert group service
 */
@Service
public class AlertGroupService {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupService.class);

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    /**
     * query alert group list
     *
     * @return
     */
    public HashMap<String, Object> queryAlertgroup() {

        HashMap<String, Object> result = new HashMap<>(5);
        List<AlertGroup> alertGroups = alertGroupMapper.queryAllGroupList();
        result.put(Constants.DATA_LIST, alertGroups);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        Page<AlertGroup> page = new Page(pageNo, pageSize);
        IPage<AlertGroup> alertGroupIPage = alertGroupMapper.queryAlertGroupPage(
                page, searchVal);
        PageInfo<AlertGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int)alertGroupIPage.getTotal());
        pageInfo.setLists(alertGroupIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * create alert group
     *
     * @param loginUser
     * @param groupName
     * @param groupType
     * @param desc
     * @return
     */
    public Map<String, Object> createAlertgroup(User loginUser, String groupName, AlertType groupType, String desc) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }

        AlertGroup alertGroup = new AlertGroup();
        Date now = new Date();

        alertGroup.setGroupName(groupName);
        alertGroup.setGroupType(groupType);
        alertGroup.setDescription(desc);
        alertGroup.setCreateTime(now);
        alertGroup.setUpdateTime(now);

        // insert 
        int insert = alertGroupMapper.insert(alertGroup);

        if (insert > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ALERT_GROUP_ERROR);
        }
        return result;
    }

    /**
     * check user is admin or not
     *
     * @param user
     * @return
     */
    public boolean isAdmin(User user) {
        return user.getUserType() == UserType.ADMIN_USER;
    }

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser
     * @param id
     * @param groupName
     * @param groupType
     * @param desc
     * @return
     */
    public Map<String, Object> updateAlertgroup(User loginUser, int id, String groupName, AlertType groupType, String desc) {
        Map<String, Object> result = new HashMap<>(5);

        if (checkAdmin(loginUser, result)){
            return result;
        }


        AlertGroup alertGroup = alertGroupMapper.selectById(id);

        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;

        }

        Date now = new Date();

        if (StringUtils.isNotEmpty(groupName)) {
            alertGroup.setGroupName(groupName);
        }

        if (groupType != null) {
            alertGroup.setGroupType(groupType);
        }
        alertGroup.setDescription(desc);
        alertGroup.setUpdateTime(now);
        // updateProcessInstance
        alertGroupMapper.updateById(alertGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete alert group by id
     *
     * @param loginUser
     * @param id
     * @return
     */
    public Map<String, Object> delAlertgroupById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }


        alertGroupMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * grant user
     *
     * @param loginUser
     * @param alertgroupId
     * @param userIds
     * @return
     */
    public Map<String, Object> grantUser(User loginUser, int alertgroupId, String userIds) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }

        userAlertGroupMapper.deleteByAlertgroupId(alertgroupId);
        if (StringUtils.isEmpty(userIds)) {
            putMsg(result, Status.SUCCESS);
            return result;
        }

        String[] userIdsArr = userIds.split(",");

        for (String userId : userIdsArr) {
            Date now = new Date();
            UserAlertGroup userAlertGroup = new UserAlertGroup();
            userAlertGroup.setAlertgroupId(alertgroupId);
            userAlertGroup.setUserId(Integer.parseInt(userId));
            userAlertGroup.setCreateTime(now);
            userAlertGroup.setUpdateTime(now);
            userAlertGroupMapper.insert(userAlertGroup);
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify group name exists
     *
     * @param loginUser
     * @param groupName
     * @return
     */
    public Result verifyGroupName(User loginUser, String groupName) {
        Result result = new Result();
        List<AlertGroup> alertGroup = alertGroupMapper.queryByGroupName(groupName);
        if (alertGroup != null && alertGroup.size() > 0) {
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
     * is admin?
     * @param loginUser
     * @param result
     * @return
     */
    private boolean checkAdmin(User loginUser, Map<String, Object> result) {
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return true;
        }
        return false;
    }

    /**
     * put message
     *
     * @param result
     * @param status
     */
    private void putMsg(Map<String, Object> result, Status status) {
        result.put(Constants.STATUS, status);
        result.put(Constants.MSG, status.getMsg());
    }
}
