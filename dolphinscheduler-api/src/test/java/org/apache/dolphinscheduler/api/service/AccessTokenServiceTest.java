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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.AccessTokenServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
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
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
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
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceTest.class);

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
        Result result = accessTokenService.queryAccessTokenList(user, "zhangsan", 1, 10);
        PageInfo<AccessToken> pageInfo = (PageInfo<AccessToken>) result.getData();
        Assertions.assertEquals(0, (int) pageInfo.getTotal());

        tokenPage.setTotal(1L);
        when(accessTokenMapper.selectAccessTokenPage(any(Page.class), eq("zhangsan"), eq(0))).thenReturn(tokenPage);
        result = accessTokenService.queryAccessTokenList(user, "zhangsan", 1, 10);
        pageInfo = (PageInfo<AccessToken>) result.getData();
        logger.info(result.toString());
        Assertions.assertTrue(pageInfo.getTotal() > 0);
    }

    @Test
    public void testQueryAccessTokenByUser() {
        User user = this.getLoginUser();
        user.setUserType(UserType.ADMIN_USER);
        List<AccessToken> accessTokenList = Lists.newArrayList(this.getEntity());
        Mockito.when(this.accessTokenMapper.queryAccessTokenByUser(Mockito.anyInt())).thenReturn(accessTokenList);
        Map<String, Object> result = this.accessTokenService.queryAccessTokenByUser(user, 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCreateToken() {
        // Given Token
        when(accessTokenMapper.insert(any(AccessToken.class))).thenReturn(2);
        Result result = accessTokenService.createToken(getLoginUser(), 1, getDate(), "AccessTokenServiceTest");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        // Token is absent
        result = this.accessTokenService.createToken(getLoginUser(), 1, getDate(), null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testGenerateToken() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = accessTokenService.generateToken(getLoginUser(), Integer.MAX_VALUE, getDate());
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        String token = (String) result.get(Constants.DATA_LIST);
        Assertions.assertNotNull(token);
    }

    @Test
    public void testDelAccessTokenById() {

        when(accessTokenMapper.selectById(1)).thenReturn(getEntity());
        User userLogin = new User();
        userLogin.setId(1);
        userLogin.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                ACCESS_TOKEN_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        // not exist
        Map<String, Object> result = accessTokenService.delAccessTokenById(userLogin, 0);
        logger.info(result.toString());
        Assertions.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST, result.get(Constants.STATUS));
        // no operate
        userLogin.setId(2);
        result = accessTokenService.delAccessTokenById(userLogin, 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        // success
        userLogin.setId(1);
        userLogin.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        result = accessTokenService.delAccessTokenById(userLogin, 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdateToken() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                ACCESS_TOKEN_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        // Given Token
        when(accessTokenMapper.selectById(1)).thenReturn(getEntity());
        Map<String, Object> result =
                accessTokenService.updateToken(getLoginUser(), 1, Integer.MAX_VALUE, getDate(), "token");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        Assertions.assertNotNull(result.get(Constants.DATA_LIST));

        // Token is absent
        result = accessTokenService.updateToken(getLoginUser(), 1, Integer.MAX_VALUE, getDate(), null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        Assertions.assertNotNull(result.get(Constants.DATA_LIST));

        // ACCESS_TOKEN_NOT_EXIST
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                baseServiceLogger)).thenReturn(true);
        result = accessTokenService.updateToken(getLoginUser(), 2, Integer.MAX_VALUE, getDate(), "token");
        logger.info(result.toString());
        Assertions.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST, result.get(Constants.STATUS));
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
