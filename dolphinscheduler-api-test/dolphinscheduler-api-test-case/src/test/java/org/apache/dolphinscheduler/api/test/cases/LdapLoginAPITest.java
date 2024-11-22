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

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.GetUserInfoResponseData;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.security.UserPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/ldap-login/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class LdapLoginAPITest {

    private static String sessionId;

    @Test
    @Order(10)
    public void testAdminUserLoginSuccess() {
        final String username = "admin_user01";

        final String password = "123";

        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        UserPage userPage = new UserPage();
        HttpResponse getUserInfoHttpResponse = userPage.getUserInfo(sessionId);
        GetUserInfoResponseData getUserInfoResponseData =
                JSONUtils.convertValue(getUserInfoHttpResponse.getBody().getData(), GetUserInfoResponseData.class);
        Assertions.assertEquals(username, getUserInfoResponseData.getUserName());
        Assertions.assertEquals(UserType.ADMIN_USER, getUserInfoResponseData.getUserType());
    }

    @Test
    @Order(20)
    public void testAdminUserFilterLoginSuccess() {
        final String username = "admin_user03";

        final String password = "123";

        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        UserPage userPage = new UserPage();
        HttpResponse getUserInfoHttpResponse = userPage.getUserInfo(sessionId);
        GetUserInfoResponseData getUserInfoResponseData =
                JSONUtils.convertValue(getUserInfoHttpResponse.getBody().getData(), GetUserInfoResponseData.class);
        Assertions.assertEquals(username, getUserInfoResponseData.getUserName());
        Assertions.assertEquals(UserType.ADMIN_USER, getUserInfoResponseData.getUserType());
    }

    @Test
    @Order(30)
    public void testGeneralUserLoginSuccess() {
        final String username = "general_user02";

        final String password = "123";

        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        UserPage userPage = new UserPage();
        HttpResponse getUserInfoHttpResponse = userPage.getUserInfo(sessionId);
        GetUserInfoResponseData getUserInfoResponseData =
                JSONUtils.convertValue(getUserInfoHttpResponse.getBody().getData(), GetUserInfoResponseData.class);
        Assertions.assertEquals(username, getUserInfoResponseData.getUserName());
        Assertions.assertEquals(UserType.GENERAL_USER, getUserInfoResponseData.getUserType());
    }

    @Test
    @Order(40)
    public void testGeneralUserLoginFailed() {
        final String username = "general_user02";

        final String password = "1";

        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        Boolean loginResult = loginHttpResponse.getBody().getSuccess();
        Assertions.assertFalse(loginResult);
    }
}
