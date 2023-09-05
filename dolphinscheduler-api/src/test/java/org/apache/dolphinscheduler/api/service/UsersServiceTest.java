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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.USER_MANAGER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.UsersServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

/**
 * users service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UsersServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceTest.class);

    @InjectMocks
    private UsersServiceImpl usersService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AccessTokenMapper accessTokenMapper;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    @Mock
    private ProjectUserMapper projectUserMapper;

    @Mock
    private ResourceUserMapper resourceUserMapper;

    @Mock
    private MetricsCleanUpService metricsCleanUpService;

    @Mock
    private UDFUserMapper udfUserMapper;

    @Mock
    private K8sNamespaceUserMapper k8sNamespaceUserMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private StorageOperate storageOperate;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private String queueName = "UsersServiceTestQueue";

    private static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @BeforeEach
    public void before() {
        Mockito.when(resourcePermissionCheckService.functionDisabled()).thenReturn(false);
    }

    @AfterEach
    public void after() {

    }

    @Test
    public void testCreateUserForLdap() {
        String userName = "user1";
        String email = "user1@ldap.com";
        User user = usersService.createUser(UserType.ADMIN_USER, userName, email);
        Assertions.assertNotNull(user);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        String userName = "userTest0001~";
        String userPassword = "userTest";
        String email = "123@qq.com";
        int tenantId = Integer.MAX_VALUE;
        String phone = "13456432345";
        int state = 1;
        try {
            // userName error
            Map<String, Object> result =
                    usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userName = "userTest0001";
            userPassword = "userTest000111111111111111";
            // password error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userPassword = "userTest0001";
            email = "1q.com";
            // email error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            email = "122222@qq.com";
            phone = "2233";
            // phone error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            phone = "13456432345";
            // tenantId not exists
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.TENANT_NOT_EXIST, result.get(Constants.STATUS));
            // success
            Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
            result = usersService.createUser(user, userName, userPassword, email, 1, phone, queueName, state);
            logger.info(result.toString());
            Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        } catch (Exception e) {
            logger.error(Status.CREATE_USER_ERROR.getMsg(), e);
            Assertions.assertTrue(false);
        }
    }

    @Test
    public void testQueryUser() {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        when(userMapper.queryUserByNamePassword(userName, EncryptionUtils.getMd5(userPassword)))
                .thenReturn(getGeneralUser());
        User queryUser = usersService.queryUser(userName, userPassword);
        logger.info(queryUser.toString());
        Assertions.assertTrue(queryUser != null);
    }

    @Test
    public void testSelectByIds() {
        List<Integer> ids = new ArrayList<>();
        List<User> users = usersService.queryUser(ids);
        Assertions.assertTrue(users.isEmpty());
        ids.add(1);
        List<User> userList = new ArrayList<>();
        userList.add(new User());
        when(userMapper.selectByIds(ids)).thenReturn(userList);
        List<User> userList1 = usersService.queryUser(ids);
        Assertions.assertFalse(userList1.isEmpty());
    }

    @Test
    public void testGetUserIdByName() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("test_user");

        // user name null
        int userId = usersService.getUserIdByName("");
        Assertions.assertEquals(0, userId);

        // user not exist
        when(usersService.queryUser(user.getUserName())).thenReturn(null);
        int userNotExistId = usersService.getUserIdByName(user.getUserName());
        Assertions.assertEquals(-1, userNotExistId);

        // user exist
        when(usersService.queryUser(user.getUserName())).thenReturn(user);
        Integer userExistId = usersService.getUserIdByName(user.getUserName());
        Assertions.assertEquals(user.getId(), userExistId);
    }

    @Test
    public void testQueryUserList() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                USER_MANAGER, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                serviceLogger)).thenReturn(false);
        Map<String, Object> result = usersService.queryUserList(user);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        // success
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.ACCESS_TOKEN, 1,
                USER_MANAGER, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                serviceLogger)).thenReturn(true);
        user.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryEnabledUsers()).thenReturn(getUserList());
        result = usersService.queryUserList(user);
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(userList.size() > 0);
    }

    @Test
    public void testQueryUserListPage() {
        User user = new User();
        IPage<User> page = new Page<>(1, 10);
        page.setRecords(getUserList());
        when(userMapper.queryUserPaging(any(Page.class), eq("userTest"))).thenReturn(page);

        // no operate
        Result result = usersService.queryUserList(user, "userTest", 1, 10);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

        // success
        user.setUserType(UserType.ADMIN_USER);
        result = usersService.queryUserList(user, "userTest", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        PageInfo<User> pageInfo = (PageInfo<User>) result.getData();
        Assertions.assertTrue(pageInfo.getTotalList().size() > 0);
    }

    @Test
    public void testUpdateUser() throws IOException {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        // user not exist
        Map<String, Object> result = usersService.updateUser(getLoginUser(), 0, userName, userPassword,
                "3443@qq.com", 1, "13457864543", "queue", 1, "Asia/Shanghai");
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // success
        when(userMapper.selectById(1)).thenReturn(getUser());
        when(userMapper.updateById(any())).thenReturn(1);
        result = usersService.updateUser(getLoginUser(), 1, userName, userPassword, "32222s@qq.com", 1,
                "13457864543", "queue", 1, "Asia/Shanghai");
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteUserById() {
        User loginUser = new User();
        try {
            when(userMapper.queryTenantCodeByUserId(1)).thenReturn(getUser());
            when(userMapper.selectById(1)).thenReturn(getUser());
            when(userMapper.deleteById(1)).thenReturn(1);
            // no operate
            Map<String, Object> result = usersService.deleteUserById(loginUser, 3);
            logger.info(result.toString());
            Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

            // user not exist
            loginUser.setUserType(UserType.ADMIN_USER);
            result = usersService.deleteUserById(loginUser, 3);
            logger.info(result.toString());
            Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

            // user is project owner
            Mockito.when(projectMapper.queryProjectCreatedByUser(1)).thenReturn(Lists.newArrayList(new Project()));
            result = usersService.deleteUserById(loginUser, 1);
            Assertions.assertEquals(Status.TRANSFORM_PROJECT_OWNERSHIP, result.get(Constants.STATUS));

            // success
            Mockito.when(projectMapper.queryProjectCreatedByUser(1)).thenReturn(null);
            Mockito.doNothing().when(metricsCleanUpService).cleanUpApiResponseTimeMetricsByUserId(Mockito.anyInt());
            result = usersService.deleteUserById(loginUser, 1);
            logger.info(result.toString());
            Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            Mockito.verify(metricsCleanUpService, times(1)).cleanUpApiResponseTimeMetricsByUserId(Mockito.anyInt());
        } catch (Exception e) {
            logger.error("delete user error", e);
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testGrantProject() {
        String projectIds = "100000,120000";
        User loginUser = new User();
        int userId = 3;

        // user not exist
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.selectById(userId)).thenReturn(null);
        Map<String, Object> result = usersService.grantProject(loginUser, userId, projectIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // SUCCESS
        when(userMapper.selectById(userId)).thenReturn(getUser());
        result = usersService.grantProject(loginUser, userId, projectIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantProjectWithReadPerm() {
        String projectIds = "100000,120000";
        User loginUser = new User();
        int userId = 3;

        // user not exist
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.selectById(userId)).thenReturn(null);
        Map<String, Object> result = usersService.grantProjectWithReadPerm(loginUser, userId, projectIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // SUCCESS
        when(userMapper.selectById(userId)).thenReturn(getUser());
        result = usersService.grantProjectWithReadPerm(loginUser, userId, projectIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantProjectByCode() {
        // Mock Project, User
        final long projectCode = 1L;
        final int projectCreator = 1;
        final int authorizer = 100;
        Mockito.when(this.userMapper.selectById(authorizer)).thenReturn(this.getUser());
        Mockito.when(this.userMapper.selectById(projectCreator)).thenReturn(this.getUser());
        Mockito.when(this.projectMapper.queryByCode(projectCode)).thenReturn(this.getProject());

        // ERROR: USER_NOT_EXIST
        User loginUser = new User();
        Map<String, Object> result = this.usersService.grantProjectByCode(loginUser, 999, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // ERROR: PROJECT_NOT_FOUNT
        result = this.usersService.grantProjectByCode(loginUser, authorizer, 999);
        logger.info(result.toString());
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, result.get(Constants.STATUS));

        // ERROR: USER_NO_OPERATION_PERM
        loginUser.setId(999);
        loginUser.setUserType(UserType.GENERAL_USER);
        result = this.usersService.grantProjectByCode(loginUser, authorizer, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        // SUCCESS: USER IS PROJECT OWNER
        loginUser.setId(projectCreator);
        loginUser.setUserType(UserType.GENERAL_USER);
        result = this.usersService.grantProjectByCode(loginUser, authorizer, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // SUCCESS: USER IS ADMINISTRATOR
        loginUser.setId(999);
        loginUser.setUserType(UserType.ADMIN_USER);
        result = this.usersService.grantProjectByCode(loginUser, authorizer, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testRevokeProject() {
        Mockito.when(this.userMapper.selectById(1)).thenReturn(this.getUser());

        final long projectCode = 3682329499136L;

        // user no permission
        User loginUser = new User();
        loginUser.setId(0);
        Map<String, Object> result = this.usersService.revokeProject(loginUser, 1, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = this.usersService.revokeProject(loginUser, 2, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // success
        Project project = new Project();
        project.setId(0);
        Mockito.when(this.projectMapper.queryByCode(Mockito.anyLong())).thenReturn(project);
        result = this.usersService.revokeProject(loginUser, 1, projectCode);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testRevokeProjectById() {
        Mockito.when(this.userMapper.selectById(1)).thenReturn(this.getUser());

        String projectId = "100000";

        // user no permission
        User loginUser = new User();
        Map<String, Object> result = this.usersService.revokeProjectById(loginUser, 1, projectId);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = this.usersService.revokeProjectById(loginUser, 2, projectId);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // success
        Mockito.when(this.projectMapper.queryByCode(Mockito.anyLong())).thenReturn(new Project());
        result = this.usersService.revokeProjectById(loginUser, 1, projectId);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantResources() {
        String resourceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.grantResources(loginUser, 2, resourceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        // success
        when(resourceMapper.selectById(Mockito.anyInt())).thenReturn(getResource());
        when(resourceUserMapper.deleteResourceUser(1, 0)).thenReturn(1);
        result = usersService.grantResources(loginUser, 1, resourceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testGrantUDFFunction() {
        String udfIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.grantUDFFunction(loginUser, 2, udfIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        // success
        when(udfUserMapper.deleteByUserId(1)).thenReturn(1);
        result = usersService.grantUDFFunction(loginUser, 1, udfIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantNamespaces() {
        String namespaceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = usersService.grantNamespaces(loginUser, 2, namespaceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));
        // success
        when(k8sNamespaceUserMapper.deleteNamespaceRelation(0, 1)).thenReturn(1);
        result = usersService.grantNamespaces(loginUser, 1, namespaceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testGrantDataSource() {
        String datasourceIds = "100000,120000";
        User loginUser = new User();
        int userId = 3;

        // user not exist
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.selectById(userId)).thenReturn(null);
        Map<String, Object> result = usersService.grantDataSource(loginUser, userId, datasourceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

        // test admin user
        when(userMapper.selectById(userId)).thenReturn(getUser());
        when(datasourceUserMapper.deleteByUserId(Mockito.anyInt())).thenReturn(1);
        result = usersService.grantDataSource(loginUser, userId, datasourceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        result = usersService.grantDataSource(loginUser, userId, datasourceIds);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    @Test
    public void getUserInfo() {
        User loginUser = new User();
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.ADMIN_USER);
        // get admin user
        Map<String, Object> result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        User tempUser = (User) result.get(Constants.DATA_LIST);
        // check userName
        Assertions.assertEquals("admin", tempUser.getUserName());

        // get general user
        loginUser.setUserType(null);
        loginUser.setId(1);
        when(userMapper.queryDetailsById(1)).thenReturn(getGeneralUser());
        when(alertGroupMapper.queryByUserId(1)).thenReturn(getAlertGroups());
        result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        tempUser = (User) result.get(Constants.DATA_LIST);
        // check userName
        Assertions.assertEquals("userTest0001", tempUser.getUserName());
    }

    @Test
    public void testQueryAllGeneralUsers() {
        User loginUser = new User();
        // no operate
        Map<String, Object> result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryAllGeneralUser()).thenReturn(getUserList());
        result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    @Test
    public void testVerifyUserName() {
        // not exist user
        Result result = usersService.verifyUserName("admin89899");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // exist user
        when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
        result = usersService.verifyUserName("userTest0001");
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NAME_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUnauthorizedUser() {
        User loginUser = new User();
        when(userMapper.selectList(null)).thenReturn(getUserList());
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        // no operate
        Map<String, Object> result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        loginUser.setUserType(UserType.ADMIN_USER);
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        // success
        result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testAuthorizedUser() {
        User loginUser = new User();
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        // no operate
        Map<String, Object> result = usersService.authorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.authorizedUser(loginUser, 2);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<User> userList = (List<User>) result.get(Constants.DATA_LIST);
        logger.info(result.toString());
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    @Test
    public void testRegisterUser() {
        String userName = "userTest0002~";
        String userPassword = "userTest";
        String repeatPassword = "userTest";
        String email = "123@qq.com";
        try {
            // userName error
            Map<String, Object> result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userName = "userTest0002";
            userPassword = "userTest000111111111111111";
            // password error
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            userPassword = "userTest0002";
            email = "1q.com";
            // email error
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            // repeatPassword error
            email = "7400@qq.com";
            repeatPassword = "userPassword";
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            // success
            repeatPassword = "userTest0002";
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    public void testActivateUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        String userName = "userTest0002~";
        try {
            // not admin
            Map<String, Object> result = usersService.activateUser(user, userName);
            Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

            // userName error
            user.setUserType(UserType.ADMIN_USER);
            result = usersService.activateUser(user, userName);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            // user not exist
            userName = "userTest10013";
            result = usersService.activateUser(user, userName);
            Assertions.assertEquals(Status.USER_NOT_EXIST, result.get(Constants.STATUS));

            // user state error
            userName = "userTest0001";
            when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getUser());
            result = usersService.activateUser(user, userName);
            Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

            // success
            when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getDisabledUser());
            result = usersService.activateUser(user, userName);
            Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    public void testBatchActivateUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        List<String> userNames = new ArrayList<>();
        userNames.add("userTest0001");
        userNames.add("userTest0002");
        userNames.add("userTest0003~");
        userNames.add("userTest0004");

        try {
            // not admin
            Map<String, Object> result = usersService.batchActivateUser(user, userNames);
            Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

            // batch activate user names
            user.setUserType(UserType.ADMIN_USER);
            when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
            when(userMapper.queryByUserNameAccurately("userTest0002")).thenReturn(getDisabledUser());
            result = usersService.batchActivateUser(user, userNames);
            Map<String, Object> responseData = (Map<String, Object>) result.get(Constants.DATA_LIST);
            Map<String, Object> successData = (Map<String, Object>) responseData.get("success");
            int totalSuccess = (Integer) successData.get("sum");

            Map<String, Object> failedData = (Map<String, Object>) responseData.get("failed");
            int totalFailed = (Integer) failedData.get("sum");

            Assertions.assertEquals(1, totalSuccess);
            Assertions.assertEquals(3, totalFailed);
            Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    public void testCreateUserIfNotExists() throws IOException {
        User user;
        String userName = "userTest0001";
        String userPassword = "userTest";
        String email = "abc@x.com";
        String phone = "17366666666";
        String tenantCode = "tenantCode";
        int stat = 1;

        // User exists
        Mockito.when(userMapper.existUser(userName)).thenReturn(true);
        Mockito.when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        Mockito.when(userMapper.queryDetailsById(getUser().getId())).thenReturn(getUser());
        Mockito.when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getUser());
        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getTenant());
        user = usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queueName, stat);
        Assertions.assertEquals(getUser(), user);

        // User not exists
        Mockito.when(userMapper.existUser(userName)).thenReturn(false);
        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getTenant());
        user = usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queueName, stat);
        Assertions.assertNotNull(user);
    }

    /**
     * get disabled user
     */
    private User getDisabledUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        user.setState(0);
        return user;
    }

    /**
     * Get project
     *
     * @return
     */
    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setCode(1L);
        project.setUserId(1);
        project.setName("PJ-001");
        project.setPerm(7);
        project.setDefCount(0);
        project.setInstRunningCount(0);
        return project;
    }

    /**
     * get user
     */
    private User getGeneralUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    private List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(getGeneralUser());
        return userList;
    }

    /**
     * get user
     */
    private User getUser() {
        User user = new User();
        user.setId(0);
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        user.setState(1);
        return user;
    }

    /**
     * get tenant
     *
     * @return tenant
     */
    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1);
        return tenant;
    }

    /**
     * get resource
     *
     * @return resource
     */
    private Resource getResource() {
        Resource resource = new Resource();
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(ResourceType.FILE);
        return resource;
    }

    private List<AlertGroup> getAlertGroups() {
        List<AlertGroup> alertGroups = new ArrayList<>();
        AlertGroup alertGroup = new AlertGroup();
        alertGroups.add(alertGroup);
        return alertGroups;
    }

}
