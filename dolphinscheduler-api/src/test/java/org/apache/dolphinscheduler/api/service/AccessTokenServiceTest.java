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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AccessTokenServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * access token service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccessTokenServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    @InjectMocks
    private AccessTokenServiceImpl accessTokenService;

    @Mock
    private AccessTokenMapper accessTokenMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryAccessTokenList() {
        IPage<AccessToken> tokenPage = new Page<>();
        tokenPage.setRecords(getList());
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        when(accessTokenMapper.selectAccessTokenPage(any(Page.class), eq("zhangsan"), eq(0))).thenReturn(tokenPage);
        PageInfo<AccessToken> pageInfo = accessTokenService.queryAccessTokenList(user, "zhangsan", 1, 10);
        assertEquals(0, (int) pageInfo.getTotal());

        tokenPage.setTotal(1L);
        when(accessTokenMapper.selectAccessTokenPage(any(Page.class), eq("zhangsan"), eq(0))).thenReturn(tokenPage);
        pageInfo = accessTokenService.queryAccessTokenList(user, "zhangsan", 1, 10);
        assertTrue(pageInfo.getTotal() > 0);
    }

    @Test
    public void testQueryAccessTokenByUser() {
        User user = this.getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> accessTokenService.queryAccessTokenByUser(user, 2));

        user.setUserType(UserType.ADMIN_USER);
        List<AccessToken> accessTokenList = Lists.newArrayList(this.getEntity());
        when(this.accessTokenMapper.queryAccessTokenByUser(Mockito.anyInt())).thenReturn(accessTokenList);
        assertDoesNotThrow(() -> accessTokenService.queryAccessTokenByUser(user, 1));
    }

    @Test
    public void testCreateToken() {
        User user = getLoginUser();

        // Throw ServiceException when user has no permission
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> accessTokenService.createToken(user, 2, getDate(), "AccessTokenServiceTest"));

        user.setId(0);

        // Throw ServiceException when user is invalid
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> accessTokenService.createToken(user, 0, getDate(), "AccessTokenServiceTest"));

        user.setId(1);

        // Given Token
        when(accessTokenMapper.insert(any(AccessToken.class))).thenReturn(2);
        assertDoesNotThrow(() -> {
            accessTokenService.createToken(user, 1, getDate(), "AccessTokenServiceTest");
        });

        // Token is absent
        assertDoesNotThrow(
                () -> accessTokenService.createToken(user, 1, getDate(), null));

        // Throw Service Exception when insert failed
        when(accessTokenMapper.insert(any(AccessToken.class))).thenReturn(0);
        assertThrowsServiceException(Status.CREATE_ACCESS_TOKEN_ERROR,
                () -> accessTokenService.createToken(user, 1, getDate(), "AccessTokenServiceTest"));
    }

    @Test
    public void testGenerateToken() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        String token = accessTokenService.generateToken(getLoginUser(), Integer.MAX_VALUE, getDate());
        assertNotNull(token);
    }

    @Test
    public void testDelAccessTokenById() {
        AccessToken accessToken = getEntity();

        when(accessTokenMapper.selectById(1)).thenReturn(accessToken);
        User userLogin = new User();
        userLogin.setId(1);
        userLogin.setUserType(UserType.ADMIN_USER);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                ACCESS_TOKEN_DELETE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        // not exist
        assertThrowsServiceException(Status.ACCESS_TOKEN_NOT_EXIST,
                () -> accessTokenService.deleteAccessTokenById(userLogin, 0));

        // no operate
        userLogin.setId(2);
        userLogin.setUserType(UserType.GENERAL_USER);

        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 2,
                ACCESS_TOKEN_DELETE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null,
                2, baseServiceLogger)).thenReturn(true);

        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> accessTokenService.deleteAccessTokenById(userLogin, 1));

        // success
        userLogin.setId(1);
        userLogin.setUserType(UserType.ADMIN_USER);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        assertDoesNotThrow(() -> accessTokenService.deleteAccessTokenById(userLogin, 1));
    }

    @Test
    public void testUpdateToken() {
        // operation perm check
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                ACCESS_TOKEN_UPDATE, baseServiceLogger)).thenReturn(false);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> accessTokenService.updateToken(getLoginUser(), 1, 1, getDate(), "token"));

        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                ACCESS_TOKEN_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        // Given Token
        when(accessTokenMapper.selectById(1)).thenReturn(getEntity());
        when(accessTokenMapper.updateById(any())).thenReturn(1);
        AccessToken accessToken =
                accessTokenService.updateToken(getLoginUser(), 1, Integer.MAX_VALUE, getDate(), "token");
        assertEquals("token", accessToken.getToken());

        // Token is absent
        accessToken = accessTokenService.updateToken(getLoginUser(), 1, Integer.MAX_VALUE, getDate(), null);
        assertNotNull(accessToken.getToken());

        // ACCESS_TOKEN_NOT_EXIST
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        assertThrowsServiceException(Status.ACCESS_TOKEN_NOT_EXIST,
                () -> accessTokenService.updateToken(getLoginUser(), 2, Integer.MAX_VALUE, getDate(), "token"));

        // resource perm check
        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        user.setId(2);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 2,
                ACCESS_TOKEN_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 2,
                baseServiceLogger)).thenReturn(true);

        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> accessTokenService.updateToken(user, 1, Integer.MAX_VALUE, getDate(), "token"));

        // Throw Service Exception when update failed
        when(accessTokenMapper.updateById(any(AccessToken.class))).thenReturn(0);
        assertThrowsServiceException(Status.ACCESS_TOKEN_NOT_EXIST,
                () -> accessTokenService.updateToken(getLoginUser(), 1, Integer.MAX_VALUE, getDate(), "token"));
    }

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    /**
     * create entity
     */
    private AccessToken getEntity() {
        AccessToken accessToken = new AccessToken();
        accessToken.setId(1);
        accessToken.setUserId(1);
        accessToken.setToken("AccessTokenServiceTest");
        Date date = DateUtils.add(new Date(), Calendar.DAY_OF_MONTH, 30);
        accessToken.setExpireTime(date);
        return accessToken;
    }

    /**
     * entity list
     */
    private List<AccessToken> getList() {

        List<AccessToken> list = new ArrayList<>();
        list.add(getEntity());
        return list;
    }

    /**
     * get dateStr
     */
    private String getDate() {
        Date date = DateUtils.add(new Date(), Calendar.DAY_OF_MONTH, 30);
        return DateUtils.dateToString(date);
    }
}
