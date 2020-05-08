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

import java.util.*;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * alert group service
 */
@Service
public class AlertGroupService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupService.class);

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private UserAlertGroupService userAlertGroupService;
    /**
     * query alert group list
     *
     * @return alert group list
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
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return alert group list page
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
     * @param loginUser login user
     * @param groupName group name
     * @param groupType group type
     * @param desc description
     * @return create result code
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
     * updateProcessInstance alert group
     *
     * @param loginUser login user
     * @param id alert group id
     * @param groupName group name
     * @param groupType group type
     * @param desc description
     * @return update result code
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
     * @param loginUser login user
     * @param id alert group id
     * @return delete result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> delAlertgroupById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }
        //check exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;
        }

        userAlertGroupService.deleteByAlertGroupId(id);
        alertGroupMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * grant user
     *
     * @param loginUser login user
     * @param alertgroupId alert group id
     * @param userIds user id list
     * @return grant result code
     */
    public Map<String, Object> grantUser(User loginUser, int alertgroupId, String userIds) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }

        userAlertGroupService.deleteByAlertGroupId(alertgroupId);
        if (StringUtils.isEmpty(userIds)) {
            putMsg(result, Status.SUCCESS);
            return result;
        }

        String[] userIdsArr = userIds.split(",");
        Date now = new Date();
        List<UserAlertGroup> alertGroups = new ArrayList<>(userIds.length());
        for (String userId : userIdsArr) {
            UserAlertGroup userAlertGroup = new UserAlertGroup();
            userAlertGroup.setAlertgroupId(alertgroupId);
            userAlertGroup.setUserId(Integer.parseInt(userId));
            userAlertGroup.setCreateTime(now);
            userAlertGroup.setUpdateTime(now);
            alertGroups.add(userAlertGroup);
        }

        if (CollectionUtils.isNotEmpty(alertGroups)) {
            userAlertGroupService.saveBatch(alertGroups);
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify group name exists
     *
     * @param groupName group name
     * @return check result code
     */
    public boolean existGroupName(String groupName) {
        List<AlertGroup> alertGroup = alertGroupMapper.queryByGroupName(groupName);
        return CollectionUtils.isNotEmpty(alertGroup);
    }
}
