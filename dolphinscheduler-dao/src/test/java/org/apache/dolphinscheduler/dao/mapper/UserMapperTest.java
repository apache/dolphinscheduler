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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.*;
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
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    AlertGroupMapper alertGroupMapper;

    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    @Autowired
    AccessTokenMapper accessTokenMapper;

    @Autowired
    TenantMapper tenantMapper;

    @Autowired
    QueueMapper queueMapper;

    /**
     * insert one user
     * @return User
     */
    private User insertOne(){
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
     * @param tenant tenant
     * @return User
     */
    private User insertOne(Tenant tenant){
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(tenant.getId());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert one user
     * @param queue queue
     * @param tenant tenant
     * @return User
     */
    private User insertOne(Queue queue,Tenant tenant){
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(tenant.getId());
        user.setQueue(queue.getQueueName());
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
     * insert one UserAlertGroup
     * @param user user
     * @param alertGroup alertGroup
     * @return UserAlertGroup
     */
    private UserAlertGroup insertOneUserAlertGroup(User user,AlertGroup alertGroup){
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
     * insert one AccessToken
     * @param user user
     * @return AccessToken
     */
    private AccessToken insertOneAccessToken(User user){
        //insertOne
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(user.getId());
        accessToken.setToken("secrettoken");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessToken.setExpireTime(DateUtils.getSomeHourOfDay(new Date(),1));
        accessTokenMapper.insert(accessToken);
        return accessToken;
    }

    /**
     * insert one Tenant
     * @return Tenant
     */
    private Tenant insertOneTenant(){
        Tenant tenant = new Tenant();
        tenant.setTenantCode("dolphin");
        tenant.setTenantName("dolphin test");
        tenant.setDescription("dolphin user use");
        tenant.setQueue("1");
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * insert one Tenant
     * @return Tenant
     */
    private Tenant insertOneTenant(Queue queue){
        Tenant tenant = new Tenant();
        tenant.setTenantCode("dolphin");
        tenant.setTenantName("dolphin test");
        tenant.setDescription("dolphin user use");
        tenant.setQueueId(queue.getId());
        tenant.setQueue(queue.getQueue());
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * insert one Queue
     * @return Queue
     */
    private Queue insertOneQueue(){
        Queue queue = new Queue();
        queue.setQueue("dolphin");
        queue.setQueueName("dolphin queue");
        queue.setCreateTime(new Date());
        queue.setUpdateTime(new Date());
        queueMapper.insert(queue);
        return queue;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        User user = insertOne();
        //update
        user.setEmail("xx-update@126.com");
        user.setUserName("user1_update");
        user.setUserType(UserType.ADMIN_USER);
        int update = userMapper.updateById(user);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        //insertOne
        User user = insertOne();
        //delete
        int delete = userMapper.deleteById(user.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        //insertOne
        User user = insertOne();
        //query
        List<User> userList = userMapper.selectList(null);
        Assert.assertNotEquals(userList.size(), 0);
    }

    /**
     * test query all general user
     */
    @Test
    public void testQueryAllGeneralUser() {
        //insertOne
        User user = insertOne();
        //queryAllGeneralUser
        List<User> userList = userMapper.queryAllGeneralUser();
        Assert.assertNotEquals(userList.size(), 0);
    }

//    /**
//     * test query by username
//     */
//    @Test
//    public void testQueryByUserNameAccurately() {
//        //insertOne
//        User user = insertOne();
//        //queryByUserNameAccurately
//        User queryUser = userMapper.queryByUserNameAccurately(user.getUserName());
//        Assert.assertEquals(queryUser.getUserName(), user.getUserName());
//    }

//    /**
//     * test query by username and password
//     */
//    @Test
//    public void testQueryUserByNamePassword() {
//        //insertOne
//        User user = insertOne();
//        //queryUserByNamePassword
//        User queryUser = userMapper.queryUserByNamePassword(user.getUserName(),user.getUserPassword());
//        Assert.assertEquals(queryUser.getUserName(),user.getUserName());
//        Assert.assertEquals(queryUser.getUserPassword(), user.getUserPassword());
//    }

    /**
     * test page
     */
    @Test
    public void testQueryUserPaging() {
        //insertOneQueue
        Queue queue = insertOneQueue();
        //insertOneTenant
        Tenant tenant = insertOneTenant();
        //insertOne
        User user = insertOne(queue,tenant);
        //queryUserPaging
        Page<User> page = new Page(1,3);
        IPage<User> userIPage = userMapper.queryUserPaging(page, user.getUserName());
        Assert.assertNotEquals(userIPage.getTotal(), 0);
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailsById() {
        //insertOneQueue and insertOneTenant
        Queue queue = insertOneQueue();
        Tenant tenant = insertOneTenant(queue);
        User user = insertOne(queue,tenant);
        //queryDetailsById
        User queryUser = userMapper.queryDetailsById(user.getId());
        Assert.assertEquals(user.getUserName(), queryUser.getUserName());
    }

    /**
     * test query user list by alertgroupId
     */
    @Test
    public void testQueryUserListByAlertGroupId() {
        //insertOne
        User user = insertOne();
        //insertOneAlertGroup
        AlertGroup alertGroup = insertOneAlertGroup();
        //insertOneUserAlertGroup
        UserAlertGroup userAlertGroup = insertOneUserAlertGroup(user, alertGroup);
        //queryUserListByAlertGroupId
        List<User> userList = userMapper.queryUserListByAlertGroupId(userAlertGroup.getAlertgroupId());
        Assert.assertNotEquals(userList.size(), 0);

    }

    /**
     * test query tenant code by userId
     */
    @Test
    public void testQueryTenantCodeByUserId() {
        //insertOneTenant
        Tenant tenant = insertOneTenant();
        //insertOne
        User user = insertOne(tenant);
        //queryTenantCodeByUserId
        User queryUser = userMapper.queryTenantCodeByUserId(user.getId());
        Assert.assertEquals(queryUser,user);
    }

    /**
     * test query user by token
     */
    @Test
    public void testQueryUserByToken() {
        //insertOne
        User user = insertOne();
        //insertOneAccessToken
        AccessToken accessToken = insertOneAccessToken(user);
        //queryUserByToken
        User userToken = userMapper.queryUserByToken(accessToken.getToken());
        Assert.assertEquals(userToken,user);

    }
}
