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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_VIEW;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class AlertGroupServiceImpl extends BaseServiceImpl implements AlertGroupService {

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * query alert group list
     *
     * @param loginUser
     * @return alert group list
     */
    @Override
    public List<AlertGroup> queryAllAlertGroup(User loginUser) {
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            return alertGroupMapper.queryAllGroupList();
        }
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP,
                loginUser.getId(), log);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return alertGroupMapper.selectBatchIds(ids);
    }

    @Override
    public List<AlertGroup> queryNormalAlertGroups(User loginUser) {
        return queryAllAlertGroup(loginUser)
                .stream()
                // todo: remove the hardcode
                .filter(alertGroup -> alertGroup.getId() != 2)
                .collect(Collectors.toList());
    }

    /**
     * query alert group by id
     *
     * @param loginUser login user
     * @param id        alert group id
     * @return one alert group
     */
    @Override
    public AlertGroup queryAlertGroupById(User loginUser, Integer id) {

        // only admin can operate
        if (!canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.ALERT_GROUP, ALERT_GROUP_VIEW)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        // check if exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            throw new ServiceException(Status.ALERT_GROUP_NOT_EXIST, id);
        }
        return alertGroup;
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return alert group list page
     */
    @Override
    public PageInfo<AlertGroup> listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Page<AlertGroup> page = new Page<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            IPage<AlertGroup> alertGroupIPage = alertGroupMapper.queryAlertGroupPage(page, searchVal);
            return PageInfo.of(alertGroupIPage);
        }

        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP,
                loginUser.getId(), log);
        if (ids.isEmpty()) {
            return PageInfo.of(pageNo, pageSize);
        }

        IPage<AlertGroup> alertGroupIPage =
                alertGroupMapper.queryAlertGroupPageByIds(page, new ArrayList<>(ids), searchVal);
        return PageInfo.of(alertGroupIPage);
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
    public AlertGroup createAlertGroup(User loginUser, String groupName, String desc, String alertInstanceIds) {
        Map<String, Object> result = new HashMap<>();
        // only admin can operate
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ALERT_GROUP, ALERT_GROUP_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        if (checkDescriptionLength(desc)) {
            log.warn("Parameter description is too long.");
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        AlertGroup alertGroup = new AlertGroup();
        Date now = new Date();

        alertGroup.setGroupName(groupName);
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        alertGroup.setDescription(desc);
        alertGroup.setCreateTime(now);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());

        // insert
        try {
            int insert = alertGroupMapper.insert(alertGroup);
            if (insert > 0) {
                log.info("Create alert group complete, groupName:{}", alertGroup.getGroupName());
                return alertGroup;
            }
            log.error("Create alert group error, groupName:{}", alertGroup.getGroupName());
            throw new ServiceException(Status.CREATE_ALERT_GROUP_ERROR);
        } catch (DuplicateKeyException ex) {
            log.error("Create alert group error, groupName:{}", alertGroup.getGroupName(), ex);
            throw new ServiceException(Status.ALERT_GROUP_EXIST);
        }
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
    public AlertGroup updateAlertGroupById(User loginUser, int id, String groupName, String desc,
                                           String alertInstanceIds) {
        // don't allow to update global alert group
        // todo: remove hardcode
        if (id == 2) {
            throw new ServiceException(Status.NOT_ALLOW_TO_UPDATE_GLOBAL_ALARM_GROUP);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.ALERT_GROUP, ALERT_GROUP_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        if (checkDescriptionLength(desc)) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        AlertGroup alertGroup = alertGroupMapper.selectById(id);

        if (alertGroup == null) {
            throw new ServiceException(Status.ALERT_GROUP_NOT_EXIST);
        }

        Date now = new Date();

        if (!StringUtils.isEmpty(groupName)) {
            alertGroup.setGroupName(groupName);
        }
        alertGroup.setDescription(desc);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        try {
            alertGroupMapper.updateById(alertGroup);
            log.info("Update alert group complete, groupName:{}", alertGroup.getGroupName());
            return alertGroup;
        } catch (DuplicateKeyException ex) {
            log.error("Update alert group error, groupName:{}", alertGroup.getGroupName(), ex);
            throw new ServiceException(Status.ALERT_GROUP_EXIST);
        }
    }

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id        alert group id
     * @return delete result code
     */
    @Override
    public void deleteAlertGroupById(User loginUser, int id) {

        // only admin can operate
        if (!canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.ALERT_GROUP, ALERT_GROUP_DELETE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // Not allow to delete the default alarm group ,because the module of service need to use it.
        if (id == 1 || id == 2) {
            log.warn("Not allow to delete the default alarm group.");
            throw new ServiceException(Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP);
        }

        // check exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            throw new ServiceException(Status.ALERT_GROUP_NOT_EXIST);
        }

        alertGroupMapper.deleteById(id);
        log.info("Delete alert group complete, groupId:{}", id);
    }

    /**
     * verify group name exists
     *
     * @param groupName group name
     * @return check result code
     */
    @Override
    public boolean existGroupName(String groupName) {
        return alertGroupMapper.existGroupName(groupName) == Boolean.TRUE;
    }
}
