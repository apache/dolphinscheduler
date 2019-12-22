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

import org.apache.commons.lang3.time.DateUtils;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class AccessTokenServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceTest.class);

    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private AccessTokenMapper accessTokenMapper;
    @Before
    public void setUp() {
        remove();
    }


    @After
    public void after(){

        remove();
    }



    @Test
    public  void testQueryAccessTokenList(){

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
        // not exist
        Map<String, Object> result = accessTokenService.delAccessTokenById(userLogin,Integer.MAX_VALUE);
        logger.info(result.toString());
        Assert.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST,result.get(Constants.STATUS));

        // add AccessToken
        add();
        AccessToken accessToken = get();
        //no operation
        result = accessTokenService.delAccessTokenById(userLogin,accessToken.getId());
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM,result.get(Constants.STATUS));

        // correct
        userLogin.setId(Integer.MAX_VALUE);
        result = accessTokenService.delAccessTokenById(userLogin,accessToken.getId());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }

    @Test
    public  void testUpdateToken(){

        add();
        AccessToken accessToken = get();
        Map<String, Object> result = accessTokenService.updateToken(accessToken.getId(),Integer.MAX_VALUE,getDate(),"token");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        accessToken = get();
        //check update
        Assert.assertEquals("token",accessToken.getToken());


    }

    /**
     * remove AccessToken
     */
    private void remove(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("user_id",Integer.MAX_VALUE);
        accessTokenMapper.deleteByMap(map);
    }

    /**
     * get AccessToken
     * @return
     */
    private AccessToken get(){

        Map<String,Object> map = new HashMap<>(1);
        map.put("user_id",Integer.MAX_VALUE);
        List<AccessToken> accessTokens = accessTokenMapper.selectByMap(map);
        if (CollectionUtils.isNotEmpty(accessTokens)){
            return accessTokens.get(0);
        }
        return  new AccessToken();
    }

    /**
     * add AccessToken
     * @return
     */
    private void add(){
        accessTokenService.createToken(Integer.MAX_VALUE,getDate(),"AccessTokenServiceTest");
    }

    private String getDate(){
        Date date = DateUtils.addDays(new Date(),30);
       return org.apache.dolphinscheduler.common.utils.DateUtils.dateToString(date);
    }
}
