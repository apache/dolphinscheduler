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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class AlertPluginInstanceResourcePermissionCheckTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceResourcePermissionCheckTest.class);
    @InjectMocks
    private ResourcePermissionCheckServiceImpl.AlertPluginInstanceResourcePermissionCheck alertPluginInstanceResourcePermissionCheck;

    @Test
    public void testPermissionCheck() {
        User user = getLoginUser();
        Assertions.assertFalse(alertPluginInstanceResourcePermissionCheck.permissionCheck(user.getId(), null, logger));
    }

    @Test
    public void testAuthorizationTypes() {
        List<AuthorizationType> authorizationTypes = alertPluginInstanceResourcePermissionCheck.authorizationTypes();
        Assertions.assertEquals(Collections.singletonList(AuthorizationType.ALERT_PLUGIN_INSTANCE), authorizationTypes);
    }

    @Test
    public void testListAuthorizedResourceIds() {
        User user = getLoginUser();

        Assertions.assertEquals(0,
                alertPluginInstanceResourcePermissionCheck.listAuthorizedResourceIds(user.getId(), logger).size());
    }

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("test");
        loginUser.setId(1);
        return loginUser;
    }
}
