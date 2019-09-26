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

import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.dao.entity.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
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
     * @return
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
     * @param tenant
     * @return
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
     * @param queue
     * @param tenant
     * @return
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
     * @return
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
     * @param user
     * @param alertGroup
     * @return
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
     * @param user
     * @return
     */
    private AccessToken insertOneAccessToken(User user){
        //insertOne
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(user.getId());
        accessToken.setToken("secrettoken");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessToken.setExpireTime(DateUtils.getSomeHourOfDay(new Date(),-1));
        accessTokenMapper.insert(accessToken);
        return accessToken;
    }

    /**
     * insert one Tenant
     * @return
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
     * insert one Queue
     * @return
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
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testDelete(){
        //insertOne
        User user = insertOne();
        //delete
        int delete = userMapper.deleteById(user.getId());
        Assert.assertEquals(delete, 1);
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testQuery() {
        //insertOne
        User user = insertOne();
        //query
        List<User> userList = userMapper.selectList(null);
        Assert.assertNotEquals(userList.size(), 0);
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testQueryAllGeneralUser() {
        //insertOne
        User user = insertOne();
        //queryAllGeneralUser
        List<User> userList = userMapper.queryAllGeneralUser();
        Assert.assertNotEquals(userList.size(), 0);
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testQueryByUserNameAccurately() {
        //insertOne
        User user = insertOne();
        //queryByUserNameAccurately
        User queryUser = userMapper.queryByUserNameAccurately(user.getUserName());
        Assert.assertEquals(queryUser.getUserName(), user.getUserName());
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testQueryUserByNamePassword() {
        //insertOne
        User user = insertOne();
        //queryUserByNamePassword
        User queryUser = userMapper.queryUserByNamePassword(user.getUserName(),user.getUserPassword());
        Assert.assertEquals(queryUser.getUserName(),user.getUserName());
        Assert.assertEquals(queryUser.getUserPassword(),user.getUserPassword());
        userMapper.deleteById(user.getId());
    }

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
        queueMapper.deleteById(queue.getId());
        tenantMapper.deleteById(tenant.getId());
        userMapper.deleteById(user.getId());
    }

    @Test
    public void testQueryDetailsById() {
        //insertOne
        User user = insertOne();
        //queryDetailsById
        User queryUser = userMapper.queryDetailsById(user.getId());
        Assert.assertEquals(queryUser,user);
        userMapper.deleteById(user.getId());
    }

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
        userMapper.deleteById(user.getId());
        alertGroupMapper.deleteById(alertGroup.getId());
        userAlertGroupMapper.deleteById(userAlertGroup.getAlertgroupId());

    }

    @Test
    public void testQueryTenantCodeByUserId() {
        //insertOneTenant
        Tenant tenant = insertOneTenant();
        //insertOne
        User user = insertOne(tenant);
        //queryTenantCodeByUserId
        User queryUser = userMapper.queryTenantCodeByUserId(user.getId());
        Assert.assertEquals(queryUser,user);
        userMapper.deleteById(user.getId());
        tenantMapper.deleteById(tenant.getId());
    }

    @Test
    public void testQueryUserByToken() {
        //insertOne
        User user = insertOne();
        //insertOneAccessToken
        AccessToken accessToken = insertOneAccessToken(user);
        //queryUserByToken
        User userToken = userMapper.queryUserByToken(accessToken.getToken());
        Assert.assertEquals(userToken,user);
        userMapper.deleteById(user.getId());
        accessTokenMapper.deleteById(accessToken.getId());

    }
}