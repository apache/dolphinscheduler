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

import static java.util.stream.Collectors.toList;

import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class UdfFuncMapperTest extends BaseDaoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    /**
     * insert one udf
     *
     * @return UdfFunc
     */
    private UdfFunc insertOne(String funcName) {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setUserId(1);
        udfFunc.setFuncName(funcName);
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
     * insert one udf
     *
     * @return
     */
    private UdfFunc insertOne(User user) {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setUserId(user.getId());
        udfFunc.setFuncName("dolphin_udf_func" + user.getUserName());
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
     * insert one user
     *
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
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert one user
     *
     * @return User
     */
    private User insertOneUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert UDFUser
     *
     * @param user    user
     * @param udfFunc udf func
     * @return UDFUser
     */
    private UDFUser insertOneUDFUser(User user, UdfFunc udfFunc) {
        UDFUser udfUser = new UDFUser();
        udfUser.setUdfId(udfFunc.getId());
        udfUser.setUserId(user.getId());
        udfUser.setCreateTime(new Date());
        udfUser.setUpdateTime(new Date());
        udfUserMapper.insert(udfUser);
        return udfUser;
    }

    /**
     * create general user
     *
     * @return User
     */
    private User createGeneralUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        UdfFunc udfFunc = insertOne("func1");
        udfFunc.setResourceName("dolphin_resource_update");
        udfFunc.setResourceId(2);
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mrUpdate");
        udfFunc.setUpdateTime(new Date());
        // update
        int update = udfFuncMapper.updateById(udfFunc);
        Assertions.assertEquals(update, 1);

    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        // insertOne
        UdfFunc udfFunc = insertOne("func2");
        // delete
        int delete = udfFuncMapper.deleteById(udfFunc.getId());
        Assertions.assertEquals(delete, 1);
    }

    /**
     * test query udf by ids
     */
    @Test
    public void testQueryUdfByIdStr() {
        // insertOne
        UdfFunc udfFunc = insertOne("func3");
        // insertOne
        UdfFunc udfFunc1 = insertOne("func4");
        Integer[] idArray = new Integer[]{udfFunc.getId(), udfFunc1.getId()};
        // queryUdfByIdStr
        List<UdfFunc> udfFuncList = udfFuncMapper.queryUdfByIdStr(idArray, "");
        Assertions.assertNotEquals(udfFuncList.size(), 0);
    }

    /**
     * test page
     */
    @Test
    public void testQueryUdfFuncPaging() {
        // insertOneUser
        User user = insertOneUser();
        // insertOne
        UdfFunc udfFunc = insertOne(user);
        // queryUdfFuncPaging
        Page<UdfFunc> page = new Page(1, 3);

        IPage<UdfFunc> udfFuncIPage =
                udfFuncMapper.queryUdfFuncPaging(page, Collections.singletonList(udfFunc.getId()), "");
        Assertions.assertNotEquals(udfFuncIPage.getTotal(), 0);

    }

    /**
     * test get udffunc by type
     */
    @Test
    public void testGetUdfFuncByType() {
        // insertOneUser
        User user = insertOneUser();
        // insertOne
        UdfFunc udfFunc = insertOne(user);
        // getUdfFuncByType
        List<UdfFunc> udfFuncList =
                udfFuncMapper.getUdfFuncByType(Collections.singletonList(udfFunc.getId()), udfFunc.getType().ordinal());
        Assertions.assertNotEquals(udfFuncList.size(), 0);

    }

    /**
     * test query udffunc expect userId
     */
    @Test
    public void testQueryUdfFuncExceptUserId() {
        // insertOneUser
        User user1 = insertOneUser();
        User user2 = insertOneUser("user2");
        // insertOne
        UdfFunc udfFunc1 = insertOne(user1);
        UdfFunc udfFunc2 = insertOne(user2);
        List<UdfFunc> udfFuncList = udfFuncMapper.queryUdfFuncExceptUserId(user1.getId());
        Assertions.assertNotEquals(udfFuncList.size(), 0);

    }

    /**
     * test query authed udffunc
     */
    @Test
    public void testQueryAuthedUdfFunc() {
        // insertOneUser
        User user = insertOneUser();

        // insertOne
        UdfFunc udfFunc = insertOne(user);

        // insertOneUDFUser
        UDFUser udfUser = insertOneUDFUser(user, udfFunc);
        // queryAuthedUdfFunc
        List<UdfFunc> udfFuncList = udfFuncMapper.queryAuthedUdfFunc(user.getId());
        Assertions.assertNotEquals(udfFuncList.size(), 0);
    }

    @Test
    public void testListAuthorizedUdfFunc() {
        // create general user
        User generalUser1 = createGeneralUser("user1");
        User generalUser2 = createGeneralUser("user2");

        // create udf function
        UdfFunc udfFunc = insertOne(generalUser1);
        UdfFunc unauthorizdUdfFunc = insertOne(generalUser2);

        // udf function ids
        Integer[] udfFuncIds = new Integer[]{udfFunc.getId(), unauthorizdUdfFunc.getId()};

        List<UdfFunc> authorizedUdfFunc = udfFuncMapper.listAuthorizedUdfFunc(generalUser1.getId(), udfFuncIds);

        Assertions.assertEquals(generalUser1.getId().intValue(), udfFunc.getUserId());
        Assertions.assertNotEquals(generalUser1.getId().intValue(), unauthorizdUdfFunc.getUserId());
        Assertions.assertFalse(authorizedUdfFunc.stream().map(t -> t.getId()).collect(toList())
                .containsAll(Arrays.asList(udfFuncIds)));

        // authorize object unauthorizdUdfFunc to generalUser1
        insertOneUDFUser(generalUser1, unauthorizdUdfFunc);
        authorizedUdfFunc = udfFuncMapper.listAuthorizedUdfFunc(generalUser1.getId(), udfFuncIds);
        Assertions.assertTrue(authorizedUdfFunc.stream().map(t -> t.getId()).collect(toList())
                .containsAll(Arrays.asList(udfFuncIds)));
    }

    @Test
    public void batchUpdateUdfFuncTest() {
        // create general user
        User generalUser1 = createGeneralUser("user1");
        UdfFunc udfFunc = insertOne(generalUser1);
        udfFunc.setResourceName("/updateTest");
        List<UdfFunc> udfFuncList = new ArrayList<>();
        udfFuncList.add(udfFunc);
        Assertions.assertTrue(udfFuncMapper.batchUpdateUdfFunc(udfFuncList) > 0);

    }
}
