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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsersServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UsersServiceTest.class);

    @InjectMocks
    private UsersService usersService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TenantMapper tenantMapper;
    @Mock
    private ProjectUserMapper projectUserMapper;
    @Mock
    private ResourceUserMapper resourcesUserMapper;
    @Mock
    private UDFUserMapper udfUserMapper;
    @Mock
    private DataSourceUserMapper datasourceUserMapper;
    @Mock
    private AlertGroupMapper alertGroupMapper;
    @Mock
    private ResourceMapper resourceMapper;

    private String queueName ="UsersServiceTestQueue";


    @Before
    public void before(){


    }
    @After
    public  void after(){

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
            //phone error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            phone = "13456432345";
            //tenantId not exists
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.TENANT_NOT_EXIST, result.get(Constants.STATUS));
            //success
            Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
            result = usersService.createUser(user, userName, userPassword, email, 1, phone, queueName);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        } catch (Exception e) {
            logger.error(Status.CREATE_USER_ERROR.getMsg(),e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryUser(){

        String userName = "userTest0001";
        String userPassword = "userTest0001";
        when(userMapper.queryUserByNamePassword(userName,EncryptionUtils.getMd5(userPassword))).thenReturn(getGeneralUser());
        User queryUser = usersService.queryUser(userName,  userPassword);
        logger.info(queryUser.toString());
        Assert.assertTrue(queryUser!=null);
    }

    @Test
    public void testGetUserIdByName() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("test_user");

        //user name null
        int userId = usersService.getUserIdByName("");
        Assert.assertEquals(0, userId);

        //user not exist
        when(usersService.queryUser(user.getUserName())).thenReturn(null);
        int userNotExistId = usersService.getUserIdByName(user.getUserName());
        Assert.assertEquals(-1, userNotExistId);

        //user exist
        when(usersService.queryUser(user.getUserName())).thenReturn(user);
        int userExistId = usersService.getUserIdByName(user.getUserName());
        Assert.assertEquals(user.getId(), userExistId);
    }



    @Test
    public void testQueryUserList(){


        User user = new User();

        //no operate
        Map<String, Object> result = usersService.queryUserList(user);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        //success
        user.setUserType(UserType.ADMIN_USER);
        when(userMapper.selectList(null )).thenReturn(getUserList());
        result = usersService.queryUserList(user);
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(userList.size()>0);
    }

    @Test
    public void testQueryUserListPage(){


        User user = new User();
        IPage<User> page = new Page<>(1,10);
        page.setRecords(getUserList());
        when(userMapper.queryUserPaging(any(Page.class), eq("userTest"))).thenReturn(page);

        //no operate
        Map<String, Object> result = usersService.queryUserList(user,"userTest",1,10);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        //success
        user.setUserType(UserType.ADMIN_USER);
        result = usersService.queryUserList(user,"userTest",1,10);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        PageInfo<User> pageInfo  = (PageInfo<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(pageInfo.getLists().size()>0);
    }

    @Test
    public void testUpdateUser(){

        String userName = "userTest0001";
        String userPassword = "userTest0001";
        try {
            //user not exist
            Map<String, Object> result = usersService.updateUser(getLoginUser(), 0,userName,userPassword,"3443@qq.com",1,"13457864543","queue");
            Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
            logger.info(result.toString());

            //success
            when(userMapper.selectById(1)).thenReturn(getUser());
            result = usersService.updateUser(getLoginUser(), 1,userName,userPassword,"32222s@qq.com",1,"13457864543","queue");
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        } catch (Exception e) {
            logger.error("update user error",e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testDeleteUserById(){

        User loginUser = new User();
        try {
            when(userMapper.queryTenantCodeByUserId(1)).thenReturn(getUser());
            when(userMapper.selectById(1)).thenReturn(getUser());

            //no operate
            Map<String, Object> result = usersService.deleteUserById(loginUser,3);
            logger.info(result.toString());
            Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

            // user not exist
            loginUser.setUserType(UserType.ADMIN_USER);
            result = usersService.deleteUserById(loginUser,3);
            logger.info(result.toString());
            Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

            //success
            result = usersService.deleteUserById(loginUser,1);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        } catch (Exception e) {
           logger.error("delete user error",e);
           Assert.assertTrue(false);
        }


    }

    @Test
    public void testGrantProject(){

        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        String  projectIds= "100000,120000";
        Map<String, Object> result = usersService.grantProject(loginUser, 1, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantProject(loginUser, 2, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        //success
        result = usersService.grantProject(loginUser, 1, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantResources(){

        String resourceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Map<String, Object> result = usersService.grantResources(loginUser, 1, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantResources(loginUser, 2, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        //success
        when(resourceMapper.queryAuthorizedResourceList(1)).thenReturn(new ArrayList<Resource>());

        when(resourceMapper.selectById(Mockito.anyInt())).thenReturn(getResource());
        result = usersService.grantResources(loginUser, 1, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }


    @Test
    public void testGrantUDFFunction(){

        String udfIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Map<String, Object> result = usersService.grantUDFFunction(loginUser, 1, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantUDFFunction(loginUser, 2, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        //success
        result = usersService.grantUDFFunction(loginUser, 1, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantDataSource(){

        String datasourceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Map<String, Object> result = usersService.grantDataSource(loginUser, 1, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantDataSource(loginUser, 2, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        //success
        result = usersService.grantDataSource(loginUser, 1, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    private User getLoginUser(){
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    @Test
    public void getUserInfo(){

        User loginUser = new User();
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.ADMIN_USER);
        // get admin user
        Map<String, Object> result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        User tempUser = (User) result.get(Constants.DATA_LIST);
        //check userName
        Assert.assertEquals("admin",tempUser.getUserName());

        //get general user
        loginUser.setUserType(null);
        loginUser.setId(1);
        when(userMapper.queryDetailsById(1)).thenReturn(getGeneralUser());
        result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        tempUser = (User) result.get(Constants.DATA_LIST);
        //check userName
        Assert.assertEquals("userTest0001",tempUser.getUserName());
    }


    @Test
    public void testQueryAllGeneralUsers(){

        User loginUser = new User();
        //no operate
        Map<String, Object> result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryAllGeneralUser()).thenReturn(getUserList());
        result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    @Test
    public void testVerifyUserName(){

        //not exist user
        Result result = usersService.verifyUserName("admin89899");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        //exist user
        when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
        result = usersService.verifyUserName("userTest0001");
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NAME_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUnauthorizedUser(){

        User loginUser = new User();
        when(userMapper.selectList(null )).thenReturn(getUserList());
        when( userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        //no operate
        Map<String, Object> result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        loginUser.setUserType(UserType.ADMIN_USER);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //success
        result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }


    @Test
    public void testAuthorizedUser(){

        User loginUser = new User();
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        //no operate
        Map<String, Object> result = usersService.authorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.authorizedUser(loginUser, 2);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        logger.info(result.toString());
        Assert.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    /**
     * get user
     * @return
     */
    private User getGeneralUser(){

        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }


    private List<User> getUserList(){
        List<User> userList = new ArrayList<>();
        userList.add(getGeneralUser());
        return userList;
    }

    /**
     * get user
     */
    private User getUser(){

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    /**
     * get tenant
     * @return tenant
     */
    private Tenant getTenant(){
        Tenant tenant = new Tenant();
        tenant.setId(1);
        return tenant;
    }

    /**
     * get resource
     * @return resource
     */
    private Resource getResource(){

        Resource resource = new Resource();
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(ResourceType.FILE);
        return resource;
    }

}