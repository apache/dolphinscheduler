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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ALERT_GROUP_VIEW;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AlertGroupServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private String tooLongDescription =
            "this is a toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                    +
                    "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                    +
                    "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long description";

    @InjectMocks
    private AlertGroupServiceImpl alertGroupService;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    private String groupName = "AlertGroupServiceTest";

    @Spy
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    public void testQueryAlertGroup() {
        User user = getLoginUser();

        when(alertGroupMapper.queryAllGroupList()).thenReturn(getList());
        List<AlertGroup> alertGroups = alertGroupService.queryAllAlertGroup(user);
        Assertions.assertEquals(2, alertGroups.size());

        user.setUserType(UserType.GENERAL_USER);
        user.setId(2);

        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP, 2,
                alertGroupServiceLogger))
                        .thenReturn(Collections.emptySet());
        Assertions.assertEquals(alertGroupService.queryAllAlertGroup(user).size(), 0);

        user.setId(3);
        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP, 3,
                alertGroupServiceLogger))
                        .thenReturn(Collections.singleton(1));
        assertDoesNotThrow(() -> alertGroupService.queryAllAlertGroup(user));
    }

    @Test
    public void testQueryNormalAlertGroup() {

        when(alertGroupMapper.queryAllGroupList()).thenReturn(getList());
        List<AlertGroup> alertGroups = alertGroupService.queryNormalAlertGroups(getLoginUser());
        Assertions.assertEquals(1, alertGroups.size());
    }

    @Test
    public void testQueryAlertGroupById() {
        User user = getLoginUser();
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);

        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, 2, ALERT_GROUP_VIEW,
                baseServiceLogger))
                        .thenReturn(false);

        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertGroupService.queryAlertGroupById(user, 1));

        user.setId(1);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, 1, ALERT_GROUP_VIEW,
                baseServiceLogger))
                        .thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, new Object[]{999}, 1,
                baseServiceLogger))
                        .thenReturn(true);
        when(alertGroupMapper.selectById(999)).thenReturn(null);

        assertThrowsServiceException(Status.ALERT_GROUP_NOT_EXIST,
                () -> alertGroupService.queryAlertGroupById(user, 999));

        when(alertGroupMapper.selectById(999)).thenReturn(getEntity());
        assertDoesNotThrow(() -> alertGroupService.queryAlertGroupById(user, 999));
    }

    @Test
    public void testListPaging() {
        IPage<AlertGroup> page = new Page<>(1, 10);
        page.setTotal(2L);
        page.setRecords(getList());
        when(alertGroupMapper.queryAlertGroupPage(any(Page.class), eq(groupName))).thenReturn(page);
        User user = new User();
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        user.setId(88);

        PageInfo<AlertGroup> alertGroupPageInfo = alertGroupService.listPaging(user, groupName, 1, 10);
        assertNotNull(alertGroupPageInfo);
        // success
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        alertGroupPageInfo = alertGroupService.listPaging(user, groupName, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(alertGroupPageInfo.getTotalList()));

        user.setUserType(UserType.GENERAL_USER);
        user.setId(99);
        page.setTotal(1);
        page.setRecords(Collections.singletonList(getEntity()));

        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP, user.getId(),
                alertGroupServiceLogger))
                        .thenReturn(Collections.singleton(1));
        when(alertGroupMapper.queryAlertGroupPageByIds(any(Page.class), any(List.class), eq(groupName)))
                .thenReturn(page);

        alertGroupService.listPaging(user, groupName, 1, 10).getTotal();
    }

    @Test
    public void testCreateAlertgroup() {

        when(alertGroupMapper.insert(any(AlertGroup.class))).thenReturn(3);
        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertGroupService.createAlertGroup(user, groupName, groupName, null));

        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        // success
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, user.getId(),
                ALERT_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, null, user.getId(),
                baseServiceLogger)).thenReturn(true);

        assertThrowsServiceException(Status.DESCRIPTION_TOO_LONG_ERROR,
                () -> alertGroupService.createAlertGroup(user, groupName, tooLongDescription, null));
        AlertGroup alertGroup = alertGroupService.createAlertGroup(user, groupName, groupName, null);
        assertNotNull(alertGroup);

        when(alertGroupMapper.insert(any(AlertGroup.class))).thenReturn(-1);
        assertThrowsServiceException(Status.CREATE_ALERT_GROUP_ERROR,
                () -> alertGroupService.createAlertGroup(user, groupName, groupName, null));

        when(alertGroupMapper.insert(any(AlertGroup.class))).thenThrow(DuplicateKeyException.class);
        assertThrowsServiceException(Status.ALERT_GROUP_EXIST,
                () -> alertGroupService.createAlertGroup(user, groupName, groupName, null));
    }

    @Test
    public void testCreateAlertgroupDuplicate() {

        when(alertGroupMapper.insert(any(AlertGroup.class))).thenThrow(new DuplicateKeyException("group name exist"));
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, user.getId(),
                ALERT_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, null, user.getId(),
                baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.ALERT_GROUP_EXIST,
                () -> alertGroupService.createAlertGroup(user, groupName, groupName, null));
    }

    @Test
    public void testUpdateAlertgroup() {

        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertGroupService.updateAlertGroupById(user, 1, groupName, groupName, null));

        // not exist
        user.setUserType(UserType.ADMIN_USER);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, user.getId(),
                ALERT_GROUP_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, new Object[]{1}, 0,
                baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.ALERT_GROUP_NOT_EXIST,
                () -> alertGroupService.updateAlertGroupById(user, 1, groupName, groupName, null));

        assertThrowsServiceException(Status.DESCRIPTION_TOO_LONG_ERROR,
                () -> alertGroupService.updateAlertGroupById(user, 1, groupName, tooLongDescription, null));

        // success
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, new Object[]{3},
                user.getId(), baseServiceLogger)).thenReturn(true);
        when(alertGroupMapper.selectById(3)).thenReturn(getEntity());
        assertDoesNotThrow(() -> alertGroupService.updateAlertGroupById(user, 3, groupName, groupName, null));
    }

    @Test
    public void testUpdateAlertgroupDuplicate() {
        User user = new User();
        user.setId(0);
        user.setUserType(UserType.ADMIN_USER);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP,
                user.getId(), ALERT_GROUP_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{3}, user.getId(), baseServiceLogger)).thenReturn(true);
        when(alertGroupMapper.selectById(3)).thenReturn(getEntity());
        when(alertGroupMapper.updateById(Mockito.any()))
                .thenThrow(new DuplicateKeyException("group name exist"));
        assertThrowsServiceException(Status.ALERT_GROUP_EXIST,
                () -> alertGroupService.updateAlertGroupById(user, 3, groupName, groupName, null));
    }

    @Test
    public void testUpdateGlobalAlertgroup() {
        User user = new User();
        user.setId(0);
        user.setUserType(UserType.ADMIN_USER);
        AlertGroup globalAlertGroup = new AlertGroup();
        globalAlertGroup.setId(2);
        globalAlertGroup.setGroupName("global alert group");
        assertThrowsServiceException(Status.NOT_ALLOW_TO_UPDATE_GLOBAL_ALARM_GROUP,
                () -> alertGroupService.updateAlertGroupById(user, 2, groupName, groupName, null));
    }

    @Test
    public void testDelAlertgroupById() {
        User user = new User();
        user.setId(0);
        // no operate
        user.setUserType(UserType.GENERAL_USER);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP, user.getId(),
                ALERT_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> alertGroupService.deleteAlertGroupById(user, 1));

        // not exist
        user.setUserType(UserType.ADMIN_USER);
        user.setId(0);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ALERT_GROUP,
                user.getId(), ALERT_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{3}, 0, baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.ALERT_GROUP_NOT_EXIST,
                () -> alertGroupService.deleteAlertGroupById(user, 3));

        // not allowed1
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, new Object[]{1}, 0,
                baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP,
                () -> alertGroupService.deleteAlertGroupById(user, 1));
        // not allowed2
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP,
                new Object[]{2}, 0, baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP,
                () -> alertGroupService.deleteAlertGroupById(user, 2));
        // success
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ALERT_GROUP, new Object[]{4}, 0,
                baseServiceLogger)).thenReturn(true);
        when(alertGroupMapper.selectById(4)).thenReturn(getEntity());
        assertDoesNotThrow(() -> alertGroupService.deleteAlertGroupById(user, 4));
    }

    @Test
    public void testVerifyGroupName() {
        // group name not exist
        boolean result = alertGroupService.existGroupName(groupName);
        Assertions.assertFalse(result);
        when(alertGroupMapper.existGroupName(groupName)).thenReturn(true);

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
        AlertGroup defaultAdminWarningGroup = new AlertGroup();
        defaultAdminWarningGroup.setId(1);
        defaultAdminWarningGroup.setGroupName("default admin warning group");
        alertGroups.add(defaultAdminWarningGroup);
        AlertGroup globalAlertGroup = new AlertGroup();
        globalAlertGroup.setId(2);
        globalAlertGroup.setGroupName("global alert group");
        alertGroups.add(globalAlertGroup);
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
