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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.UsersServiceImpl;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.UserDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = DaoConfiguration.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:user-permission-cleanup;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;",
        "registry.type=none"
})
@Import(UsersServiceImpl.class)
class UsersServicePermissionCleanupTest {

    private static final int TARGET_USER = 9102;
    private static final int OTHER_USER = 9103;

    @Autowired
    private UsersService usersService;
    @Autowired
    private JdbcTemplate jdbc;
    @SpyBean
    private UserDao userDao;
    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;
    @MockBean
    private SessionService sessionService;
    @MockBean
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private User admin;

    @BeforeEach
    void prepare() {
        jdbc.update("delete from t_ds_project_user_permission where user_id in (?, ?)", TARGET_USER, OTHER_USER);
        jdbc.update("delete from t_ds_user where id in (?, ?)", TARGET_USER, OTHER_USER);
        jdbc.update("insert into t_ds_user (id, user_name, user_type, create_time) "
                + "values (?, 'cleanup-target', 1, current_timestamp)", TARGET_USER);
        jdbc.update("insert into t_ds_user (id, user_name, user_type, create_time) "
                + "values (?, 'cleanup-other', 1, current_timestamp)", OTHER_USER);
        jdbc.update("insert into t_ds_project_user_permission (project_code, user_id, permission) "
                + "values (920001, ?, 'VIEW'), (920001, ?, 'EXECUTE'), (920002, ?, 'DEVELOP'), (920001, ?, 'VIEW')",
                TARGET_USER, TARGET_USER, TARGET_USER, OTHER_USER);
        admin = new User();
        admin.setId(1);
        admin.setUserType(UserType.ADMIN_USER);
    }

    @Test
    void deletingUserRemovesPermissionsAcrossProjectsAndPreservesOtherUsers() throws Exception {
        usersService.deleteUserById(admin, TARGET_USER);

        Assertions.assertEquals(0, permissionCount(TARGET_USER));
        Assertions.assertEquals(1, permissionCount(OTHER_USER));
        Assertions.assertNull(userDao.queryById(TARGET_USER));
        Assertions.assertNotNull(userDao.queryById(OTHER_USER));
    }

    @Test
    void failedUserDeletionRollsBackPermissionCleanup() {
        Mockito.doAnswer(invocation -> {
            Assertions.assertEquals(0, permissionCount(TARGET_USER));
            return false;
        }).when(userDao).deleteById(TARGET_USER);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> usersService.deleteUserById(admin, TARGET_USER));

        Assertions.assertEquals(Status.DELETE_USER_BY_ID_ERROR.getCode(), exception.getCode());
        Assertions.assertEquals(3, permissionCount(TARGET_USER));
        Assertions.assertEquals(1, permissionCount(OTHER_USER));
        Assertions.assertNotNull(userDao.queryById(TARGET_USER));
    }

    private int permissionCount(int userId) {
        return jdbc.queryForObject("select count(*) from t_ds_project_user_permission where user_id = ?",
                Integer.class, userId);
    }
}
