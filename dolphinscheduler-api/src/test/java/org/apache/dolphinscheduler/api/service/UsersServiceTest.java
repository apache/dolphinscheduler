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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.USER_MANAGER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.UsersServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UsersServiceTest {

    @InjectMocks
    private UsersServiceImpl usersService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AccessTokenMapper accessTokenMapper;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private ProjectUserMapper projectUserMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    @Mock
    private K8sNamespaceUserMapper k8sNamespaceUserMapper;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private SessionService sessionService;

    private String queueName = "UsersServiceTestQueue";

    private static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @Test
    public void testCreateUserForLdap() {
        String userName = "user1";
        String email = "user1@ldap.com";
        User user = usersService.createUser(UserType.ADMIN_USER, userName, email);
        Assertions.assertNotNull(user);
    }

    @Test
    public void testCreateUser() throws Exception {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        String userName = "userTest0001~";
        String userPassword = "userTest";
        String email = "123@qq.com";
        int tenantId = Integer.MAX_VALUE;
        String phone = "13456432345";
        int state = 1;

        // userName error
        final String userNameInvalid = userName;
        final String passwordInitial = userPassword;
        final String emailInitial = email;
        final String phoneInitial = phone;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.createUser(loginUser, userNameInvalid, passwordInitial, emailInitial, tenantId,
                        phoneInitial, queueName, state));

        // password error
        userName = "userTest0001";
        userPassword = "userTest000111111111111111";
        final String userNameOk = userName;
        final String passwordInvalid = userPassword;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.createUser(loginUser, userNameOk, passwordInvalid, emailInitial, tenantId,
                        phoneInitial, queueName, state));

        // email error
        userPassword = "userTest0001";
        email = "1q.com";
        final String passwordOk = userPassword;
        final String emailInvalid = email;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.createUser(loginUser, userNameOk, passwordOk, emailInvalid, tenantId, phoneInitial,
                        queueName, state));

        // phone error
        email = "122222@qq.com";
        phone = "2233";
        final String emailOk = email;
        final String phoneInvalid = phone;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.createUser(loginUser, userNameOk, passwordOk, emailOk, tenantId, phoneInvalid,
                        queueName, state));

        // tenantId not exists
        phone = "13456432345";
        final String phoneOk = phone;
        assertThrowsServiceException(Status.TENANT_NOT_EXIST,
                () -> usersService.createUser(loginUser, userNameOk, passwordOk, emailOk, tenantId, phoneOk, queueName,
                        state));

        // success
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        User created =
                usersService.createUser(loginUser, userNameOk, passwordOk, emailOk, 1, phoneOk, queueName, state);
        Assertions.assertNotNull(created);
    }

    @Test
    public void testQueryUser() {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        when(userMapper.queryUserByNamePassword(userName, EncryptionUtils.getMd5(userPassword)))
                .thenReturn(getGeneralUser());
        User queryUser = usersService.queryUser(userName, userPassword);
        Assertions.assertNotNull(queryUser);
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
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM, () -> usersService.queryUserList(user));

        // success
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.ACCESS_TOKEN, null, 0,
                serviceLogger)).thenReturn(true);
        when(userMapper.queryEnabledUsers()).thenReturn(getUserList());
        List<User> users = usersService.queryUserList(user);
        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    public void testQueryUserListPage() {
        User user = new User();
        IPage<User> page = new Page<>(1, 10);
        page.setRecords(getUserList());
        when(userMapper.queryUserPaging(any(Page.class), eq("userTest"))).thenReturn(page);

        // no operate
        Result result = usersService.queryUserList(user, "userTest", 1, 10);
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

        // success
        user.setUserType(UserType.ADMIN_USER);
        result = usersService.queryUserList(user, "userTest", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        PageInfo<User> pageInfo = (PageInfo<User>) result.getData();
        Assertions.assertFalse(pageInfo.getTotalList().isEmpty());
    }

    @Test
    public void testUpdateUser() {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        // user not exist
        assertThrowsServiceException(
                Status.USER_NOT_EXIST,
                () -> usersService.updateUser(getLoginUser(),
                        0,
                        userName,
                        userPassword,
                        "3443@qq.com",
                        1,
                        "13457864543",
                        "queue",
                        1,
                        "Asia/Shanghai"));

        // success
        when(userMapper.selectById(any())).thenReturn(getUser());
        when(userMapper.updateById(any())).thenReturn(1);
        assertDoesNotThrow(() -> usersService.updateUser(getLoginUser(),
                1,
                userName,
                userPassword,
                "32222s@qq.com",
                1,
                "13457864543",
                "queue",
                1,
                "Asia/Shanghai"));

        // non-admin should not modify tenantId and queue
        when(userMapper.selectById(2)).thenReturn(getNonAdminUser());
        User user = userMapper.selectById(2);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM, () -> usersService.updateUser(user,
                2,
                userName,
                userPassword,
                "abc@qq.com",
                null,
                "13457864543",
                "offline",
                1,
                "Asia/Shanghai"));
    }

    @Test
    public void testUpdateUserSimple() {
        // null user -> USER_NOT_EXIST
        assertThrowsServiceException(Status.USER_NOT_EXIST, () -> usersService.updateUser(null));

        // user with null id -> USER_NOT_EXIST
        User noIdUser = new User();
        assertThrowsServiceException(Status.USER_NOT_EXIST, () -> usersService.updateUser(noIdUser));

        // update failure (0 rows affected) -> UPDATE_USER_ERROR
        User failingUser = new User();
        failingUser.setId(1);
        Mockito.when(userMapper.updateById(Mockito.any())).thenReturn(0);
        assertThrowsServiceException(Status.UPDATE_USER_ERROR, () -> usersService.updateUser(failingUser));

        // success path (1 row affected)
        User successUser = new User();
        successUser.setId(2);
        Mockito.when(userMapper.updateById(Mockito.any())).thenReturn(1);
        User updated = usersService.updateUser(successUser);
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(2, updated.getId());
        Assertions.assertNotNull(updated.getUpdateTime(), "updateTime should be set");
    }

    @Test
    public void testDeleteUserById() {
        User loginUser = new User();
        when(userMapper.queryTenantCodeByUserId(1)).thenReturn(getUser());
        when(userMapper.selectById(1)).thenReturn(getUser());
        when(userMapper.deleteById(1)).thenReturn(1);
        // no operate
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.deleteUserById(loginUser, 3));

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.deleteUserById(loginUser, 3));

        // user is project owner
        Mockito.when(projectDao.queryProjectCreatedByUser(1)).thenReturn(Lists.newArrayList(new Project()));
        assertThrowsServiceException(Status.TRANSFORM_PROJECT_OWNERSHIP,
                () -> usersService.deleteUserById(loginUser, 1));

        // success
        Mockito.when(projectDao.queryProjectCreatedByUser(1)).thenReturn(null);
        assertDoesNotThrow(() -> usersService.deleteUserById(loginUser, 1));
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
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.grantProject(loginUser, userId, projectIds));

        // SUCCESS
        when(userMapper.selectById(userId)).thenReturn(getUser());
        assertDoesNotThrow(() -> usersService.grantProject(loginUser, userId, projectIds));

        // ERROR: NO_CURRENT_OPERATING_PERMISSION
        loginUser.setId(3);
        loginUser.setUserType(UserType.GENERAL_USER);
        when(userMapper.selectById(3)).thenReturn(loginUser);
        assertThrowsServiceException(Status.NO_CURRENT_OPERATING_PERMISSION,
                () -> usersService.grantProject(loginUser, userId, projectIds));
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
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.grantProjectWithReadPerm(loginUser, userId, projectIds));

        // SUCCESS
        when(userMapper.selectById(userId)).thenReturn(getUser());
        assertDoesNotThrow(() -> usersService.grantProjectWithReadPerm(loginUser, userId, projectIds));

        // ERROR: NO_CURRENT_OPERATING_PERMISSION
        loginUser.setId(3);
        loginUser.setUserType(UserType.GENERAL_USER);
        when(userMapper.selectById(3)).thenReturn(loginUser);
        assertThrowsServiceException(Status.NO_CURRENT_OPERATING_PERMISSION,
                () -> usersService.grantProjectWithReadPerm(loginUser, userId, projectIds));
    }

    @Test
    public void testGrantProjectByCode() {
        // Mock Project, User
        final long projectCode = 1L;
        final int projectCreator = 1;
        final int authorizer = 100;
        Mockito.when(this.userMapper.selectById(authorizer)).thenReturn(this.getUser());
        Mockito.when(this.userMapper.selectById(projectCreator)).thenReturn(this.getUser());
        Mockito.when(this.projectDao.queryByCode(projectCode)).thenReturn(this.getProject());

        // ERROR: USER_NOT_EXIST
        User loginUser = new User();
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.grantProjectByCode(loginUser, 999, projectCode));

        // ERROR: PROJECT_NOT_FOUNT
        assertThrowsServiceException(Status.PROJECT_NOT_FOUND,
                () -> usersService.grantProjectByCode(loginUser, authorizer, 999));

        // ERROR: USER_NO_OPERATION_PERM
        loginUser.setId(999);
        loginUser.setUserType(UserType.GENERAL_USER);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.grantProjectByCode(loginUser, authorizer, projectCode));

        // SUCCESS: USER IS PROJECT OWNER
        loginUser.setId(projectCreator);
        loginUser.setUserType(UserType.GENERAL_USER);
        assertDoesNotThrow(() -> usersService.grantProjectByCode(loginUser, authorizer, projectCode));

        // SUCCESS: USER IS ADMINISTRATOR
        loginUser.setId(999);
        loginUser.setUserType(UserType.ADMIN_USER);
        assertDoesNotThrow(() -> usersService.grantProjectByCode(loginUser, authorizer, projectCode));
    }

    @Test
    public void testRevokeProject() {
        Mockito.when(this.userMapper.selectById(1)).thenReturn(this.getUser());

        final long projectCode = 3682329499136L;

        // user no permission
        User loginUser = new User();
        loginUser.setId(0);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.revokeProject(loginUser, 1, projectCode));

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.revokeProject(loginUser, 2, projectCode));

        // success
        Project project = new Project();
        project.setId(0);
        Mockito.when(this.projectDao.queryByCode(Mockito.anyLong())).thenReturn(project);
        assertDoesNotThrow(() -> usersService.revokeProject(loginUser, 1, projectCode));
    }

    @Test
    public void testRevokeProjectById() {
        Mockito.when(this.userMapper.selectById(1)).thenReturn(this.getUser());

        String projectId = "100000";

        // user no permission
        User loginUser = new User();
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.revokeProjectById(loginUser, 1, projectId));

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.revokeProjectById(loginUser, 2, projectId));

        // success
        Mockito.when(this.projectDao.queryByCode(Mockito.anyLong())).thenReturn(new Project());
        assertDoesNotThrow(() -> usersService.revokeProjectById(loginUser, 1, projectId));
    }

    @Test
    public void testGrantNamespaces() {
        String namespaceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();

        // user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.grantNamespaces(loginUser, 2, namespaceIds));
        // success
        when(k8sNamespaceUserMapper.deleteNamespaceRelation(0, 1)).thenReturn(1);
        assertDoesNotThrow(() -> usersService.grantNamespaces(loginUser, 1, namespaceIds));
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
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.grantDataSource(loginUser, userId, datasourceIds));

        // test admin user
        when(userMapper.selectById(userId)).thenReturn(getUser());
        when(datasourceUserMapper.deleteByUserId(Mockito.anyInt())).thenReturn(1);
        assertDoesNotThrow(() -> usersService.grantDataSource(loginUser, userId, datasourceIds));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.grantDataSource(loginUser, userId, datasourceIds));
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
        User adminInfo = usersService.getUserInfo(loginUser);
        Assertions.assertEquals("admin", adminInfo.getUserName());

        // get general user
        loginUser.setUserType(null);
        loginUser.setId(1);
        when(userMapper.queryDetailsById(1)).thenReturn(getGeneralUser());
        when(alertGroupMapper.queryByUserId(1)).thenReturn(getAlertGroups());
        User generalInfo = usersService.getUserInfo(loginUser);
        Assertions.assertEquals("userTest0001", generalInfo.getUserName());
    }

    @Test
    public void testQueryAllGeneralUsers() {
        User loginUser = new User();
        // no operate
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.queryAllGeneralUsers(loginUser));
        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryAllGeneralUser()).thenReturn(getUserList());
        List<User> users = usersService.queryAllGeneralUsers(loginUser);
        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    public void testVerifyUserName() {
        // not exist user
        Result result = usersService.verifyUserName("admin89899");
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // exist user
        when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
        result = usersService.verifyUserName("userTest0001");
        Assertions.assertEquals(Status.USER_NAME_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUnauthorizedUser() {
        User loginUser = new User();
        when(userMapper.selectList(null)).thenReturn(getUserList());
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        // no operate
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.unauthorizedUser(loginUser, 2));
        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        List<User> users = usersService.unauthorizedUser(loginUser, 2);
        Assertions.assertNotNull(users);
    }

    @Test
    public void testAuthorizedUser() {
        User loginUser = new User();
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        // no operate
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.authorizedUser(loginUser, 2));
        // success
        loginUser.setUserType(UserType.ADMIN_USER);
        List<User> users = usersService.authorizedUser(loginUser, 2);
        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    public void testRegisterUser() {
        String userName = "userTest0002~";
        String userPassword = "userTest";
        String repeatPassword = "userTest";
        String email = "123@qq.com";

        // userName error
        final String userNameInvalid = userName;
        final String passwordInitial = userPassword;
        final String repeatInitial = repeatPassword;
        final String emailInitial = email;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.registerUser(userNameInvalid, passwordInitial, repeatInitial, emailInitial));

        userName = "userTest0002";
        userPassword = "userTest000111111111111111";
        // password error
        final String userNameOk = userName;
        final String passwordTooLong = userPassword;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.registerUser(userNameOk, passwordTooLong, repeatInitial, emailInitial));

        userPassword = "userTest0002";
        email = "1q.com";
        // email error
        final String passwordOk = userPassword;
        final String emailInvalid = email;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.registerUser(userNameOk, passwordOk, repeatInitial, emailInvalid));

        // repeatPassword error
        email = "7400@qq.com";
        repeatPassword = "userPassword";
        final String emailOk = email;
        final String repeatMismatch = repeatPassword;
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.registerUser(userNameOk, passwordOk, repeatMismatch, emailOk));

        // success
        repeatPassword = "userTest0002";
        User registered = usersService.registerUser(userNameOk, passwordOk, repeatPassword, emailOk);
        Assertions.assertNotNull(registered);
    }

    @Test
    public void testActivateUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        String userName = "userTest0002~";

        // not admin
        final String invalidName = userName;
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.activateUser(user, invalidName));

        // userName error
        user.setUserType(UserType.ADMIN_USER);
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.activateUser(user, invalidName));

        // user not exist
        userName = "userTest10013";
        final String missingName = userName;
        assertThrowsServiceException(Status.USER_NOT_EXIST,
                () -> usersService.activateUser(user, missingName));

        // user state error
        userName = "userTest0001";
        final String existingName = userName;
        when(userMapper.queryByUserNameAccurately(existingName)).thenReturn(getUser());
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> usersService.activateUser(user, existingName));

        // success
        when(userMapper.queryByUserNameAccurately(existingName)).thenReturn(getDisabledUser());
        User activated = usersService.activateUser(user, existingName);
        Assertions.assertNotNull(activated);
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

        // not admin
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> usersService.batchActivateUser(user, userNames));

        // batch activate user names
        user.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
        when(userMapper.queryByUserNameAccurately("userTest0002")).thenReturn(getDisabledUser());
        Map<String, Object> result = usersService.batchActivateUser(user, userNames);
        Map<String, Object> successData = (Map<String, Object>) result.get("success");
        int totalSuccess = (Integer) successData.get("sum");

        Map<String, Object> failedData = (Map<String, Object>) result.get("failed");
        int totalFailed = (Integer) failedData.get("sum");

        Assertions.assertEquals(1, totalSuccess);
        Assertions.assertEquals(3, totalFailed);
    }

    @Test
    public void testCreateUserIfNotExists() {
        User user;
        String userName = "userTest0001";
        String userPassword = "userTest";
        String email = "abc@x.com";
        String phone = "17366666666";
        String tenantCode = "tenantCode";
        int stat = 1;

        // User exists
        when(userMapper.existUser(userName)).thenReturn(true);
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(userMapper.queryDetailsById(getUser().getId())).thenReturn(getUser());
        when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getUser());
        when(userMapper.updateById(any())).thenReturn(1);
        when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getTenant());
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
     */
    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setCode(1L);
        project.setUserId(1);
        project.setName("PJ-001");
        project.setPerm(7);
        project.setDefCount(0);
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
     * get non-admin user
     */
    private User getNonAdminUser() {
        User user = new User();
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        user.setTenantId(2);
        user.setQueue("queue");
        return user;
    }

    /**
     * get tenant
     */
    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1);
        return tenant;
    }

    private List<AlertGroup> getAlertGroups() {
        List<AlertGroup> alertGroups = new ArrayList<>();
        AlertGroup alertGroup = new AlertGroup();
        alertGroups.add(alertGroup);
        return alertGroups;
    }
}
