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
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.AccessToken;
import cn.escheduler.dao.model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

/**
 * user test
 */
public class UserMapperTest {


    UserMapper userMapper;
    AccessTokenMapper accessTokenMapper;
    int userId;

    @Before
    public void before(){
        userMapper = ConnectionFactory.getSqlSession().getMapper(UserMapper.class);
        accessTokenMapper = ConnectionFactory.getSqlSession().getMapper(AccessTokenMapper.class);
    }


    @After
    public void testDelete() {
        int delete = userMapper.delete(userId);
        Assert.assertTrue(delete >= 0);
    }


    @Test
    public void testInsert(){
        User user = new User();
        user.setUserName("Dr.strange" + new Date().getTime());
        user.setUserPassword("1234567890");
        user.setEmail("wwww@123.com");
        user.setPhone("12345678901");
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        Assert.assertNotEquals(user.getId(), 0);

        user.setUserName("Dr.chemistry" + new Date().getTime());
        int update = userMapper.update(user);
        Assert.assertEquals(update, 1);


        user = userMapper.queryById(user.getId());
        Assert.assertNotEquals(user.getUserName(), "Dr.chemistry" + new Date().getTime());

        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(user.getId());
        accessToken.setExpireTime(new Date());
        accessToken.setToken("ssssssssssssssssssssssssss");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessTokenMapper.insert(accessToken);

        userId = user.getId();


        User user2 = userMapper.queryUserByToken("ssssssssssssssssssssssssss");
        Assert.assertTrue(user2.getId() >= 0);
    }


    @Test
    public void queryQueueByProcessInstanceId(){
        String queue = userMapper.queryQueueByProcessInstanceId(-1000);
        Assert.assertNotEquals(queue, "ait");
    }



}
