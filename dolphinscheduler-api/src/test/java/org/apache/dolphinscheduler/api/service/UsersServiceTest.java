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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class UsersServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UsersServiceTest.class);

    @Autowired
    private UsersService usersService;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantMapper tenantMapper;
    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueMapper queueMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ResourceUserMapper resourcesUserMapper;
    @Autowired
    private UDFUserMapper udfUserMapper;
    @Autowired
    private DataSourceUserMapper datasourceUserMapper;
    @Autowired
    private AlertGroupService alertGroupService;

    private String queueName ="UsersServiceTestQueue";

    private String tenantName = "UsersServiceTestTenant";



    @Before
    public void before(){
        removeUser();
        removeTenant();
        removeQueue();

    }
    @After
    public  void after(){
        removeUser();
        removeTenant();
        removeQueue();
    }


    @Test
    public void testCreateUser(){

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        String userName = "userTest0001~";
        String userPassword = "userTest";
        String email = "123@qq.com";
        int tenantId = Integer.MAX_VALUE;
        String phone= "13456432345";
        try {
            //userName error
            Map<String, Object> result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userName = "userTest0001";
            userPassword = "userTest000111111111111111";
            //password error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userPassword = "userTest0001";
            email = "1q.com";
            //email error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            email = "122222@qq.com";
            phone ="2233";
            //email error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            phone = "13456432345";
            //tenantId not exists
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.TENANT_NOT_EXIST, result.get(Constants.STATUS));
            //correct
            tenantId =getTenantId();
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        } catch (Exception e) {
            logger.error(Status.CREATE_USER_ERROR.getMsg(),e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryUser(){
        //add user
        add();
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        User queryUser = usersService.queryUser(userName, userPassword);
        Assert.assertTrue(queryUser!=null);
    }



    @Test
    public void testQueryUserList(){

        //add user
        add();
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.queryUserList(user);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(userList.size()>0);
    }

    @Test
    public void testQueryUserListPage(){

        //add user
        add();
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.queryUserList(user,"userTest",1,10);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        PageInfo<User> pageInfo  = (PageInfo<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(pageInfo.getLists().size()>0);
    }

    @Test
    public void testUpdateUser(){

        String userName = "userTest0001";
        String userPassword = "userTest0001";
        //add user
        add();
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        User queryUser = usersService.queryUser(userName, userPassword);
        if (queryUser == null){
            logger.error("user not exist");
            Assert.assertTrue(false);
        }

        //modidy Password
        userPassword = "userTest0002";
        try {
            usersService.updateUser(queryUser.getId(),queryUser.getUserName(),userPassword,queryUser.getEmail(),queryUser.getTenantId(),queryUser.getPhone(),queryUser.getQueue());
        } catch (Exception e) {
          logger.error("update user error",e);
          Assert.assertTrue(false);
        }
        //check user Password
        queryUser = usersService.queryUser(userName, userPassword);
        Assert.assertNotNull(queryUser);
    }

    @Test
    public void testDeleteUserById(){

        String userName = "userTest0001";
        String userPassword = "userTest0001";

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        // get new user
        User user = getUser();
        Assert.assertNotNull(user);

        try {
            usersService.deleteUserById(loginUser, user.getId());
        } catch (Exception e) {
           logger.error("delete user error",e);
           Assert.assertTrue(false);
        }

        //user not exist
        user = usersService.queryUser(userName, userPassword);
        Assert.assertNull(user);

    }

    @Test
    public void testGrantProject(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);

        User user = getUser();
        if (user == null){
            logger.error("user  not exist");
            Assert.assertNotNull(user);
        }
        String proejectIds = "100000,120000";
        Map<String, Object> result = usersService.grantProject(loginUser, user.getId(), proejectIds);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        //remove user project
        removeProject(user.getId());
    }

    @Test
    public void testGrantResources(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);

        User user = getUser();
        if (user == null){
            logger.error("user  not exist");
            Assert.assertNotNull(user);
        }
        String resourceIds = "100000,120000";
        Map<String, Object> result = usersService.grantResources(loginUser, user.getId(), resourceIds);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        //remove user resource
        removeResource(user.getId());
    }


    @Test
    public void testGrantUDFFunction(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);

        User user = getUser();
        if (user == null){
            logger.error("user  not exist");
            Assert.assertNotNull(user);
        }
        String udfIds = "100000,120000";
        Map<String, Object> result = usersService.grantUDFFunction(loginUser, user.getId(), udfIds);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        //remove user udf
        removeUdf(user.getId());
    }

    @Test
    public void testGrantDataSource(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);

        User user = getUser();
        if (user == null){
            logger.error("user  not exist");
            Assert.assertNotNull(user);
        }
        String datasourceIds = "100000,120000";
        Map<String, Object> result = usersService.grantDataSource(loginUser, user.getId(), datasourceIds);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        //remove  user datasource
        removeDatasource(user.getId());

    }


    @Test
    public void getUserInfo(){

        User loginUser = new User();
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        User tempUser = (User) result.get(Constants.DATA_LIST);
        // check admin user
        Assert.assertEquals("admin",tempUser.getUserName());

        //get general user
        loginUser = getUser();
        loginUser.setUserType(UserType.GENERAL_USER);
        result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        tempUser = (User) result.get(Constants.DATA_LIST);
        //check general user
        Assert.assertEquals("userTest0001",tempUser.getUserName());
    }


    @Test
    public void testQueryAllGeneralUsers(){

        //add user
        add();
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.queryAllGeneralUsers(loginUser);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(userList));

    }

    @Test
    public void testVerifyUserName(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        //not exist user
        Result result = usersService.verifyUserName("admin89899");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        add();
        //exist user
        result = usersService.verifyUserName("userTest0001");
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NAME_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUnauthorizedUser(){

        add();
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.unauthorizedUser(loginUser, 90999);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }


    @Test
    public void testAuthorizedUser(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.authorizedUser(loginUser, 90999);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    /**
     * add and get user
     * @return
     */
    private User getUser(){

        add();
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
       return usersService.queryUser(userName, userPassword);
    }

    /**
     * add and get  Tenant
     * @return
     */
    private int  getTenantId(){

        //add Tenant
        Tenant entity = new Tenant();
        entity.setTenantCode(tenantName);
        entity.setTenantName(tenantName);
        entity.setQueueId(getQueueId());
        tenantMapper.insert(entity);
        // get
        List<Tenant>  tenantList = tenantMapper.queryByTenantCode(tenantName);
        if (CollectionUtils.isNotEmpty(tenantList)){
           return  tenantList.get(0).getId();
        }
        return 0;
    }

    /**
     * add and get Queue
     * @return
     */
    private int getQueueId(){

        //add queue
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        queueService.createQueue(user,queueName,queueName);
        // get
        List<Queue>  queueList = queueMapper.queryAllQueueList(queueName,queueName);
        if (CollectionUtils.isNotEmpty(queueList)){
            return queueList.get(0).getId();
        }
        return 0;
    }

    /**
     * delete user
     */
    private void removeUser(){

        Map<String,Object> map = new HashMap<>(1);
        map.put("user_name", "userTest0001");
        userMapper.deleteByMap(map);

    }

    /**
     * add user
     */
    private void add(){

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        String email = "123@qq.com";
        int tenantId =  getTenantId();
        String phone= "13456432345";
        //add user
        try {
            usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
        } catch (Exception e) {
            logger.error("create user error",e);
        }
    }

    /**
     * remove tenant
     */
    private void removeTenant(){

        Map<String,Object> map = new HashMap<>(1);
        map.put("tenant_name",tenantName);
        tenantMapper.deleteByMap(map);

    }

    /**
     * remove queue
     */
    private void removeQueue(){

        Map<String,Object> map = new HashMap<>(1);
        map.put("queue_name",queueName);
        queueMapper.deleteByMap(map);
    }


    /**
     * remove user project
     * @param userId
     */
    private void removeProject(int userId){

        projectUserMapper.deleteProjectRelation(100000,userId);
        projectUserMapper.deleteProjectRelation(120000,userId);

    }

    /**
     * remove user Resource
     * @param userId
     */
    private void removeResource(int userId){
        resourcesUserMapper.deleteResourceUser(userId,100000);
        resourcesUserMapper.deleteResourceUser(userId,120000);
    }

    /**
     * remove user Udf
     * @param userId
     */
    private void removeUdf(int userId){
        udfUserMapper.deleteByUserId(userId);
    }

    /**
     * remove user Datasource
     * @param userId
     */
    private void removeDatasource(int userId){
        datasourceUserMapper.deleteByUserId(userId);
    }
}