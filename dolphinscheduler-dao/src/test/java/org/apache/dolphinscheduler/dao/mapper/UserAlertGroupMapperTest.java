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


import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class UserAlertGroupMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    AlertGroupMapper alertGroupMapper;

    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    /**
     * insert one UserAlertGroup
     * @param user user
     * @param alertGroup alertGroup
     * @return UserAlertGroup
     */
    private UserAlertGroup insertOne(User user,AlertGroup alertGroup){
        UserAlertGroup userAlertGroup = new UserAlertGroup();
        userAlertGroup.setAlertgroupName(alertGroup.getGroupName());
        userAlertGroup.setAlertgroupId(alertGroup.getId());
        userAlertGroup.setUserId(user.getId());
        userAlertGroup.setCreateTime(new Date());
        userAlertGroup.setUpdateTime(new Date());
        userAlertGroupMapper.insert(userAlertGroup);
        return userAlertGroup;
    }

    /**
     * insert one UserAlertGroup
     * @return UserAlertGroup
     */
    private UserAlertGroup insertOne(){
        UserAlertGroup userAlertGroup = new UserAlertGroup();
        userAlertGroup.setAlertgroupName("dolphin_alert_group");
        userAlertGroup.setAlertgroupId(10);
        userAlertGroup.setUserId(4);
        userAlertGroup.setCreateTime(new Date());
        userAlertGroup.setUpdateTime(new Date());
        userAlertGroupMapper.insert(userAlertGroup);
        return userAlertGroup;
    }

    /**
     * insert one user
     * @return User
     */
    private User insertOneUser(){
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
     * insert one AlertGroup
     * @return AlertGroup
     */
    private AlertGroup insertOneAlertGroup(){
        //insertOne
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName("alert group 1");
        alertGroup.setDescription("alert test1");
        alertGroup.setGroupType(AlertType.EMAIL);

        alertGroup.setCreateTime(new Date());
        alertGroup.setUpdateTime(new Date());
        alertGroupMapper.insert(alertGroup);
        return alertGroup;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOneUser
        User user = insertOneUser();
        //insertOneAlertGroup
        AlertGroup alertGroup = insertOneAlertGroup();

        //insertOne
        UserAlertGroup userAlertGroup = insertOne();
        //update
        userAlertGroup.setUserId(user.getId());
        userAlertGroup.setAlertgroupId(alertGroup.getId());
        userAlertGroup.setUpdateTime(new Date());

        int update = userAlertGroupMapper.updateById(userAlertGroup);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        //insertOne
        UserAlertGroup userAlertGroup = insertOne();
        //delete
        int delete = userAlertGroupMapper.deleteById(userAlertGroup.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        //insertOne
        UserAlertGroup userAlertGroup = insertOne();
        //query
        List<UserAlertGroup> userAlertGroupList = userAlertGroupMapper.selectList(null);
        Assert.assertNotEquals(userAlertGroupList.size(), 0);
    }

    /**
     * test delete by alertgroupId
     */
    @Test
    public void testDeleteByAlertgroupId() {
        //insertOneUser
        User user = insertOneUser();
        //insertOneAlertGroup
        AlertGroup alertGroup = insertOneAlertGroup();

        //insertOne
        UserAlertGroup userAlertGroup = insertOne(user,alertGroup);
        int delete = userAlertGroupMapper.deleteByAlertgroupId(alertGroup.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test list user by alertgroupId
     */
    @Test
    public void testListUserByAlertgroupId() {
        //insertOneUser
        User user = insertOneUser();
        //insertOneAlertGroup
        AlertGroup alertGroup = insertOneAlertGroup();

        //insertOne
        UserAlertGroup userAlertGroup = insertOne(user,alertGroup);
        List<User> userList = userAlertGroupMapper.listUserByAlertgroupId(alertGroup.getId());
        Assert.assertNotEquals(userList.size(), 0);

    }
}