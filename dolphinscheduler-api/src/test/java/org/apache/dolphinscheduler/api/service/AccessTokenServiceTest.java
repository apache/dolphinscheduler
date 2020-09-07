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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.AccessTokenServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceTest.class);

    @InjectMocks
    private AccessTokenServiceImpl accessTokenService;

    @Mock
    private AccessTokenMapper accessTokenMapper;


    @Test
    @SuppressWarnings("unchecked")
    public void testQueryAccessTokenList() {

        IPage<AccessToken> tokenPage = new Page<>();
        tokenPage.setRecords(getList());
        tokenPage.setTotal(1L);
        when(accessTokenMapper.selectAccessTokenPage(any(Page.class), eq("zhangsan"), eq(0))).thenReturn(tokenPage);

        User user = new User();
        Map<String, Object> result = accessTokenService.queryAccessTokenList(user, "zhangsan", 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        PageInfo<AccessToken> pageInfo = (PageInfo<AccessToken>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(pageInfo.getTotalCount() > 0);
    }

    @Test
    public void testCreateToken() {

        when(accessTokenMapper.insert(any(AccessToken.class))).thenReturn(2);
        Map<String, Object> result = accessTokenService.createToken(1, getDate(), "AccessTokenServiceTest");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGenerateToken() {

        Map<String, Object> result = accessTokenService.generateToken(Integer.MAX_VALUE, getDate());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        String token = (String) result.get(Constants.DATA_LIST);
        Assert.assertNotNull(token);
    }

    @Test
    public void testDelAccessTokenById() {

        when(accessTokenMapper.selectById(1)).thenReturn(getEntity());
        User userLogin = new User();
        // not exist
        Map<String, Object> result = accessTokenService.delAccessTokenById(userLogin, 0);
        logger.info(result.toString());
        Assert.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST, result.get(Constants.STATUS));
        // no operate
        result = accessTokenService.delAccessTokenById(userLogin, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //success
        userLogin.setId(1);
        userLogin.setUserType(UserType.ADMIN_USER);
        result = accessTokenService.delAccessTokenById(userLogin, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdateToken() {

        when(accessTokenMapper.selectById(1)).thenReturn(getEntity());
        Map<String, Object> result = accessTokenService.updateToken(1, Integer.MAX_VALUE, getDate(), "token");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        // not exist
        result = accessTokenService.updateToken(2, Integer.MAX_VALUE, getDate(), "token");
        logger.info(result.toString());
        Assert.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST, result.get(Constants.STATUS));

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
