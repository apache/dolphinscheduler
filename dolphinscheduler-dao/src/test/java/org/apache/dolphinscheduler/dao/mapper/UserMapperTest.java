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

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithProcessDefinitionCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class UserMapperTest extends BaseDaoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    /**
     * insert one user
     *
     * @return User
     */
    private User insertOne() {
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        user.setQueueName("test_queue");
        user.setQueue("queue");
        userMapper.insert(user);
        return user;
    }

    private User insertOneUser(Tenant tenant) {
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(tenant.getId());
        user.setTenantCode(tenant.getTenantCode());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert one user
     *
     * @param queue  queue
     * @param tenant tenant
     * @return User
     */
    private User insertOne(Queue queue, Tenant tenant) {
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
     *
     * @return AlertGroup
     */
    private AlertGroup insertOneAlertGroup() {
        // insertOne
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName("alert group 1");
        alertGroup.setDescription("alert test1");

        alertGroup.setCreateTime(new Date());
        alertGroup.setUpdateTime(new Date());
        alertGroupMapper.insert(alertGroup);
        return alertGroup;
    }

    /**
     * insert one AccessToken
     *
     * @param user user
     * @return AccessToken
     */
    private AccessToken insertOneAccessToken(User user) {
        // insertOne
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(user.getId());
        accessToken.setToken("secrettoken");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessToken.setExpireTime(DateUtils.getSomeHourOfDay(new Date(), 1));
        accessTokenMapper.insert(accessToken);
        return accessToken;
    }

    /**
     * insert one Tenant
     *
     * @return Tenant
     */
    private Tenant insertOneTenant() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("dolphin");
        tenant.setDescription("dolphin user use");
        tenant.setQueue("1");
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * insert one Tenant
     *
     * @return Tenant
     */
    private Tenant insertOneTenant(Queue queue) {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("dolphin");
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
     *
     * @return Queue
     */
    private Queue insertOneQueue() {
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
    public void testUpdate() {
        // insertOne
        User user = insertOne();
        // update
        user.setEmail("xx-update@126.com");
        user.setUserName("user1_update");
        user.setUserType(UserType.ADMIN_USER);
        int update = userMapper.updateById(user);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        // insertOne
        User user = insertOne();
        // delete
        int delete = userMapper.deleteById(user.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        // insertOne
        User user = insertOne();
        // query
        List<User> userList = userMapper.selectList(null);
        Assertions.assertNotEquals(0, userList.size());
    }

    /**
     * test query all general user
     */
    @Test
    public void testQueryAllGeneralUser() {
        // insertOne
        User user = insertOne();
        // queryAllGeneralUser
        List<User> userList = userMapper.queryAllGeneralUser();
        Assertions.assertNotEquals(0, userList.size());
    }

    /**
     * test page
     */
    @Test
    public void testQueryUserPaging() {
        // insertOneQueue
        Queue queue = insertOneQueue();
        // insertOneTenant
        Tenant tenant = insertOneTenant();
        // insertOne
        User user = insertOne(queue, tenant);
        // queryUserPaging
        Page<User> page = new Page(1, 3);
        IPage<User> userIPage = userMapper.queryUserPaging(page, user.getUserName());
        Assertions.assertNotEquals(0, userIPage.getTotal());
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailsById() {
        // insertOneQueue and insertOneTenant
        Queue queue = insertOneQueue();
        Tenant tenant = insertOneTenant(queue);
        User user = insertOne(queue, tenant);
        // queryDetailsById
        User queryUser = userMapper.queryDetailsById(user.getId());
        Assertions.assertEquals(user.getUserName(), queryUser.getUserName());
    }

    /**
     * test query tenant code by userId
     */
    @Test
    public void testQueryTenantCodeByUserId() {
        // insertOneTenant
        Tenant tenant = insertOneTenant();
        // insertOne
        User user = insertOneUser(tenant);
        // queryTenantCodeByUserId
        User queryUser = userMapper.queryTenantCodeByUserId(user.getId());
        Assertions.assertEquals(queryUser, user);
    }

    /**
     * test query user by token
     */
    @Test
    public void testQueryUserByToken() {
        // insertOne
        User user = insertOne();
        // insertOneAccessToken
        AccessToken accessToken = insertOneAccessToken(user);
        // queryUserByToken
        User userToken = userMapper.queryUserByToken(accessToken.getToken(), new Date());
        Assertions.assertEquals(userToken.getId(), user.getId());

    }

    @Test
    public void selectByIds() {
        // insertOne
        User user = insertOne();
        List<Integer> userIds = new ArrayList<>();
        userIds.add(user.getId());
        List<User> users = userMapper.selectByIds(userIds);
        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    public void testExistUser() {
        String queueName = "queue";
        Assertions.assertNull(userMapper.existUser(queueName));
        insertOne();
        Assertions.assertTrue(userMapper.existUser(queueName));
    }

    @Test
    public void testQueryUserWithProcessDefinitionCode() {
        User user = insertOne();
        insertProcessDefinition(user.getId());
        ProcessDefinitionLog log = insertProcessDefinitionLog(user.getId());
        long processDefinitionCode = log.getCode();
        List<UserWithProcessDefinitionCode> userWithCodes = userMapper.queryUserWithProcessDefinitionCode(
                null);
        UserWithProcessDefinitionCode userWithCode = userWithCodes.stream()
                .filter(code -> code.getProcessDefinitionCode() == processDefinitionCode)
                .findAny().orElse(null);
        assert userWithCode != null;
        Assertions.assertEquals(userWithCode.getCreatorId(), user.getId());
    }

    private ProcessDefinitionLog insertProcessDefinitionLog(int operator) {
        // insertOne
        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setCode(199L);
        processDefinitionLog.setName("def 1");
        processDefinitionLog.setProjectCode(1L);
        processDefinitionLog.setUserId(operator);
        processDefinitionLog.setVersion(10);
        processDefinitionLog.setUpdateTime(new Date());
        processDefinitionLog.setCreateTime(new Date());
        processDefinitionLog.setOperator(operator);
        processDefinitionLogMapper.insert(processDefinitionLog);
        return processDefinitionLog;
    }

    private ProcessDefinition insertProcessDefinition(int operator) {
        // insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(199L);
        processDefinition.setName("process-name");
        processDefinition.setProjectCode(1010L);
        processDefinition.setVersion(10);
        processDefinition.setUserId(operator);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);
        return processDefinition;
    }

}
