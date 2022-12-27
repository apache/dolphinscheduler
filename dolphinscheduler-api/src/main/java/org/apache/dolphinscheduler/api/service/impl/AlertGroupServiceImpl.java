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

package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert group service impl
 */
@Service
public class AlertGroupServiceImpl extends BaseServiceImpl implements AlertGroupService {

    private Logger logger = LoggerFactory.getLogger(AlertGroupServiceImpl.class);

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * query alert group list
     *
     * @param loginUser
     * @return alert group list
     */
    @Override
    public Map<String, Object> queryAlertgroup(User loginUser) {
        HashMap<String, Object> result = new HashMap<>();
        List<AlertGroup> alertGroups = this.alertGroupMapper.queryByTenantId(loginUser.getTenantId());
        if (alertGroups == null) {
            alertGroups = new ArrayList<>();
        }
        result.put(Constants.DATA_LIST, alertGroups);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return one alert group
     */
    @Override
    public Map<String, Object> queryAlertGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        // check if exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;
        }
        result.put("data", alertGroup);
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
    @Override
    public Result listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        Result result = new Result();
        IPage<AlertGroup> alertGroupPage;
        PageInfo<AlertGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<AlertGroup> page = new Page<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            alertGroupPage = alertGroupMapper.queryAlertGroupPage(page, searchVal);
        } else {
            List<AlertGroup> groups=alertGroupMapper.queryByTenantId(loginUser.getTenantId());
            if(groups.isEmpty())
            {
                alertGroupPage=new PageDTO<>();
            }else {
                Set<Integer> ids=new HashSet<>();
                for (AlertGroup group:groups) {
                    ids.add(group.getId());
                }
                alertGroupPage = alertGroupMapper.queryAlertGroupPageByIds(page, new ArrayList<>(ids), searchVal);
            }
        }
        pageInfo.setTotal((int) alertGroupPage.getTotal());
        pageInfo.setTotalList(alertGroupPage.getRecords());
        result.setData(pageInfo);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * create alert group
     *
     * @param loginUser login user
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return create result code
     */
    @Override
    @Transactional
    public Map<String, Object> createAlertgroup(User loginUser, String groupName, String desc,
                                                String alertInstanceIds) {
        Map<String, Object> result = new HashMap<>();
        // only admin and project admin can operate
        if (loginUser.getUserType() == UserType.GENERAL_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (checkDescriptionLength(desc)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        AlertGroup alertGroup = new AlertGroup();
        Date now = new Date();

        alertGroup.setGroupName(groupName);
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        alertGroup.setDescription(desc);
        alertGroup.setCreateTime(now);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());
        alertGroup.setTenantId(loginUser.getTenantId());

        // insert
        try {
            int insert = alertGroupMapper.insert(alertGroup);
            if (insert > 0) {
                result.put(Constants.DATA_LIST, alertGroup);
                putMsg(result, Status.SUCCESS);
                permissionPostHandle(AuthorizationType.ALERT_GROUP, loginUser.getId(),
                        Collections.singletonList(alertGroup.getId()), logger);
                logger.info("Create alert group complete, groupName:{}", alertGroup.getGroupName());
            } else {
                logger.error("Create alert group error, groupName:{}", alertGroup.getGroupName());
                putMsg(result, Status.CREATE_ALERT_GROUP_ERROR);
            }
        } catch (DuplicateKeyException ex) {
            logger.error("Create alert group error, groupName:{}", alertGroup.getGroupName(), ex);
            putMsg(result, Status.ALERT_GROUP_EXIST);
        }

        return result;
    }

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser login user
     * @param id alert group id
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return update result code
     */
    @Override
    public Map<String, Object> updateAlertgroup(User loginUser, int id, String groupName, String desc,
                                                String alertInstanceIds) {
        Map<String, Object> result = new HashMap<>();

        if (loginUser.getUserType() == UserType.GENERAL_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (checkDescriptionLength(desc)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        AlertGroup alertGroup = alertGroupMapper.selectById(id);

        if (alertGroup == null) {
            logger.error("Alert group does not exist, id:{}.", id);
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;

        }

        Date now = new Date();

        if (!StringUtils.isEmpty(groupName)) {
            alertGroup.setGroupName(groupName);
        }
        alertGroup.setDescription(desc);
        alertGroup.setUpdateTime(now);
        alertGroup.setTenantId(loginUser.getTenantId());
        alertGroup.setCreateUserId(loginUser.getId());
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        try {
            alertGroupMapper.updateById(alertGroup);
            logger.info("Update alert group complete, groupName:{}", alertGroup.getGroupName());
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Update alert group error, groupName:{}", alertGroup.getGroupName(), ex);
            putMsg(result, Status.ALERT_GROUP_EXIST);
        }
        return result;
    }

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return delete result code
     */
    @Override
    @Transactional
    public Map<String, Object> delAlertgroupById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        // only admin and project admin can operate
        if (loginUser.getUserType() == UserType.GENERAL_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // Not allow to delete the default alarm group ,because the module of service need to use it.
        if (id == 1) {
            logger.warn("Not allow to delete the default alarm group.");
            putMsg(result, Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP);
            return result;
        }

        // check exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            logger.error("Alert group does not exist, id:{}.", id);
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;
        }

        alertGroupMapper.deleteById(id);
        logger.info("Delete alert group complete, groupId:{}", id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify group name exists
     *
     * @param groupName group name
     * @return check result code
     */
    @Override
    public boolean existGroupName(String groupName, User loginUser) {
        return alertGroupMapper.existGroupName(groupName, loginUser.getTenantId()) == Boolean.TRUE;
    }
}
