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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AlertGroupServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert group service test
 */
@ExtendWith(MockitoExtension.class)
public class AlertGroupServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger logger = LoggerFactory.getLogger(AlertGroupServiceTest.class);
    private static final Logger alertGroupServiceLogger = LoggerFactory.getLogger(AlertGroupServiceImpl.class);

    @InjectMocks
    private AlertGroupServiceImpl alertGroupService;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    private String groupName = "AlertGroupServiceTest";

    @Spy
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    public void testQueryAlertGroup() {

        Mockito.when(alertGroupMapper.queryAllGroupList()).thenReturn(getList());
        Map<String, Object> result = alertGroupService.queryAlertgroup(getLoginUser());
        logger.info(result.toString());
        List<AlertGroup> alertGroups = (List<AlertGroup>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(alertGroups));
    }

    @Test
    public void testListPaging() {
        IPage<AlertGroup> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getList());
        Mockito.when(alertGroupMapper.queryAlertGroupPage(any(Page.class), eq(groupName))).thenReturn(page);
        User user = new User();
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        user.setId(88);

        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        Result result = alertGroupService.listPaging(user, groupName, 1, 10);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        // success
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        result = alertGroupService.listPaging(user, groupName, 1, 10);
        logger.info(result.toString());
        PageInfo<AlertGroup> pageInfo = (PageInfo<AlertGroup>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testCreateAlertgroup() {

        Mockito.when(alertGroupMapper.insert(any(AlertGroup.class))).thenReturn(2);
        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = alertGroupService.createAlertgroup(user, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        // success
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), baseServiceLogger)).thenReturn(true);
        result = alertGroupService.createAlertgroup(user, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        Assertions.assertNotNull(result.get(Constants.DATA_LIST));
    }

    @Test
    public void testCreateAlertgroupDuplicate() {

        Mockito.when(alertGroupMapper.insert(any(AlertGroup.class)))
                .thenThrow(new DuplicateKeyException("group name exist"));
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), baseServiceLogger)).thenReturn(true);
        Map<String, Object> result = alertGroupService.createAlertgroup(user, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.ALERT_GROUP_EXIST, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdateAlertgroup() {

        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = alertGroupService.updateAlertgroup(user, 1, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        user.setUserType(UserType.ADMIN_USER);
        // not exist
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{1}, 0, baseServiceLogger)).thenReturn(true);
        result = alertGroupService.updateAlertgroup(user, 1, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.ALERT_GROUP_NOT_EXIST, result.get(Constants.STATUS));
        // success
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{2}, user.getId(), baseServiceLogger)).thenReturn(true);
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.updateAlertgroup(user, 2, groupName, groupName, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testUpdateAlertgroupDuplicate() {
        User user = new User();
        user.setId(0);
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{2}, user.getId(), baseServiceLogger)).thenReturn(true);
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        Mockito.when(alertGroupMapper.updateById(Mockito.any()))
                .thenThrow(new DuplicateKeyException("group name exist"));
        Map<String, Object> result = alertGroupService.updateAlertgroup(user, 2, groupName, groupName, null);
        Assertions.assertEquals(Status.ALERT_GROUP_EXIST, result.get(Constants.STATUS));
    }

    @Test
    public void testDelAlertgroupById() {

        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        Map<String, Object> result = alertGroupService.delAlertgroupById(user, 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        // not exist
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, null,
                user.getId(), ALERT_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{2}, 0, baseServiceLogger)).thenReturn(true);
        result = alertGroupService.delAlertgroupById(user, 2);
        logger.info(result.toString());
        Assertions.assertEquals(Status.ALERT_GROUP_NOT_EXIST, result.get(Constants.STATUS));
        // success
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{2}, 0, baseServiceLogger)).thenReturn(true);
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.delAlertgroupById(user, 2);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testVerifyGroupName() {
        // group name not exist
        boolean result = alertGroupService.existGroupName(groupName);
        Assertions.assertFalse(result);
        Mockito.when(alertGroupMapper.existGroupName(groupName)).thenReturn(true);

        // group name exist
        result = alertGroupService.existGroupName(groupName);
        Assertions.assertTrue(result);
    }

    /**
     * create admin user
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * get list
     */
    private List<AlertGroup> getList() {
        List<AlertGroup> alertGroups = new ArrayList<>();
        alertGroups.add(getEntity());
        return alertGroups;
    }

    /**
     * get entity
     */
    private AlertGroup getEntity() {
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setId(0);
        alertGroup.setGroupName(groupName);
        return alertGroup;
    }

}
