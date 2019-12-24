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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceTest.class);


    @InjectMocks
    private AccessTokenService accessTokenService ;

    @Mock
    private AccessTokenMapper accessTokenMapper;

    @Before
    public void setUp() {

        AccessToken accessToken = new AccessToken();
        accessToken.setId(1);
        accessToken.setToken("AccessTokenServiceTest");
        Date date = DateUtils.addDays(new Date(),30);
        accessToken.setExpireTime(date);
        // select
        when(accessTokenMapper.selectById(1)).thenReturn(accessToken);
       //selectAccessTokenPage
        IPage<AccessToken> accessTokenList = new Page<>();
        when(accessTokenMapper.selectAccessTokenPage(any(Page.class),eq("zhangsan"),eq(0))).thenReturn(accessTokenList);
    }


    @After
    public void after(){

    }



    @Test
    public  void testQueryAccessTokenList(){

        User user =new User();
        Map<String, Object> result = accessTokenService.queryAccessTokenList(user,"zhangsan",1,10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testCreateToken(){

        Map<String, Object> result = accessTokenService.createToken(Integer.MAX_VALUE,getDate(),"AccessTokenServiceTest");
        logger.info(result.toString());
       // Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testGenerateToken(){

        Map<String, Object> result = accessTokenService.generateToken(Integer.MAX_VALUE,getDate());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }

    @Test
    public  void testDelAccessTokenById(){

        User userLogin = new User();
        Map<String, Object> result = accessTokenService.delAccessTokenById(userLogin,1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }

    @Test
    public  void testUpdateToken(){

        Map<String, Object> result = accessTokenService.updateToken(1,Integer.MAX_VALUE,getDate(),"token");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }


    /**
     * get dateStr
     * @return
     */
    private String getDate(){
        Date date = DateUtils.addDays(new Date(),30);
       return org.apache.dolphinscheduler.common.utils.DateUtils.dateToString(date);
    }
}
