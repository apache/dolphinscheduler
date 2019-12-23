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
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.mockito.Mockito.*;

import java.util.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = ApiApplicationServer.class)
public class AccessTokenServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceTest.class);


    private AccessTokenService accessTokenService = new AccessTokenService();

    @Before
    public void setUp() {

        AccessTokenMapper accessTokenMapper1 = mock(AccessTokenMapper.class);
        AccessToken accessToken = new AccessToken();
        accessToken.setId(1);
        accessToken.setToken("AccessTokenServiceTest");
        Date date = DateUtils.addDays(new Date(),30);
        accessToken.setExpireTime(date);
        when(accessTokenMapper1.insert(accessToken)).thenReturn(1);
        when(accessTokenMapper1.selectById(1)).thenReturn(accessToken);
        when(accessTokenMapper1.deleteById(1)).thenReturn(0);

        Page<AccessToken> page = new Page(1, 10);
        Page<AccessToken> accessTokenList = new Page();
        accessTokenList.setRecords(new ArrayList<>());
        when(accessTokenMapper1.selectAccessTokenPage(page,"test",0)).thenReturn(accessTokenList);

        accessTokenService.setAccessTokenMapper(accessTokenMapper1);

    }


    @After
    public void after(){

    }



    @Test
    public  void testQueryAccessTokenList(){

        User user =new User();
        Map<String, Object> result = accessTokenService.queryAccessTokenList(user,"test",1,10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testCreateToken(){

        Map<String, Object> result = accessTokenService.createToken(Integer.MAX_VALUE,getDate(),"AccessTokenServiceTest");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
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


    private String getDate(){
        Date date = DateUtils.addDays(new Date(),30);
       return org.apache.dolphinscheduler.common.utils.DateUtils.dateToString(date);
    }
}
