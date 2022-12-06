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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UDFUserMapperTest extends BaseDaoTest {

    @Autowired
    private UDFUserMapper udfUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    /**
     * insert
     * @return UDFUser
     */
    private UDFUser insertOne() {
        UDFUser udfUser = new UDFUser();
        udfUser.setUdfId(1);
        udfUser.setUserId(1);
        udfUser.setCreateTime(new Date());
        udfUser.setUpdateTime(new Date());
        udfUserMapper.insert(udfUser);
        return udfUser;
    }

    /**
     * insert UDFUser
     * @param user user
     * @param udfFunc  udfFunc
     * @return UDFUser
     */
    private UDFUser insertOne(User user, UdfFunc udfFunc) {
        UDFUser udfUser = new UDFUser();
        udfUser.setUdfId(udfFunc.getId());
        udfUser.setUserId(user.getId());
        udfUser.setCreateTime(new Date());
        udfUser.setUpdateTime(new Date());
        udfUserMapper.insert(udfUser);
        return udfUser;
    }

    /**
     * insert one user
     * @return User
     */
    private User insertOneUser() {
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setQueue("dolphin");
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert one udf
     * @return UdfFunc
     */
    private UdfFunc insertOneUdfFunc() {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setFuncName("dolphin_udf_func");
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mr");
        udfFunc.setType(UdfType.HIVE);
        udfFunc.setResourceId(1);
        udfFunc.setResourceName("dolphin_resource");
        udfFunc.setCreateTime(new Date());
        udfFunc.setUpdateTime(new Date());
        udfFuncMapper.insert(udfFunc);
        return udfFunc;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOneUser
        User user = insertOneUser();
        // insertOneUdfFunc
        UdfFunc udfFunc = insertOneUdfFunc();
        // insertOne
        UDFUser udfUser = insertOne(user, udfFunc);
        udfUser.setUserId(2);
        udfUser.setUdfId(2);
        int update = udfUserMapper.updateById(udfUser);
        Assertions.assertEquals(update, 1);

    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        // insertOneUser
        User user = insertOneUser();
        // insertOneUdfFunc
        UdfFunc udfFunc = insertOneUdfFunc();
        // insertOne
        UDFUser udfUser = insertOne(user, udfFunc);
        int delete = udfUserMapper.deleteById(udfUser.getId());
        Assertions.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        // insertOne
        UDFUser udfUser = insertOne();
        // query
        List<UDFUser> udfUserList = udfUserMapper.selectList(null);
        Assertions.assertNotEquals(udfUserList.size(), 0);
    }

    /**
     * test delete by userId
     */
    @Test
    public void testDeleteByUserId() {
        // insertOneUser
        User user = insertOneUser();
        // insertOneUdfFunc
        UdfFunc udfFunc = insertOneUdfFunc();
        // insertOne
        UDFUser udfUser = insertOne(user, udfFunc);
        int delete = udfUserMapper.deleteByUserId(user.getId());
        Assertions.assertEquals(delete, 1);

    }

    /**
     * test delete by udffuncId
     */
    @Test
    public void testDeleteByUdfFuncId() {
        // insertOneUser
        User user = insertOneUser();
        // insertOneUdfFunc
        UdfFunc udfFunc = insertOneUdfFunc();
        // insertOne
        UDFUser udfUser = insertOne(user, udfFunc);
        int delete = udfUserMapper.deleteByUdfFuncId(udfFunc.getId());
        Assertions.assertEquals(delete, 1);
    }
}
