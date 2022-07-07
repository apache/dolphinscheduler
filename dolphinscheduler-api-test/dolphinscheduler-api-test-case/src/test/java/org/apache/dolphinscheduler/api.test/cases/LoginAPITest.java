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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static io.restassured.RestAssured.given;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.login.LoginPageAPI;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.Status;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("User Login Page API test")
public class LoginAPITest extends AbstractAPITest {
    private LoginPageAPI loginPageAPI = null;
    private LoginRequestEntity loginRequestEntity = null;

    @BeforeAll
    public void initTenantPageAPIFactory() {
        loginPageAPI = pageAPIFactory.createLoginPageAPI();
        loginRequestEntity = new LoginRequestEntity();
        loginRequestEntity.setUserName(Constants.USER_NAME);
        loginRequestEntity.setUserPassword(Constants.USER_PASSWD);
    }

    @Test
    @DisplayName("Test the correct user information to log in to the system")
    void testUserLogin() {
        loginPageAPI.loginUser(given().spec(reqSpec), loginRequestEntity).
            isResponseSuccessful();
    }

    @Test
    @DisplayName("Test getting user session")
    void testGetUserSession() {
        loginPageAPI.loginUser(given().spec(reqSpec), loginRequestEntity).
            getResponse()
            .then()
            .body("data.sessionId", notNullValue());

    }

    @ParameterizedTest
    @MethodSource("testErrorUserLoginInfoProvider")
    @DisplayName("Test error user information login system")
    void testErrorUserLogin(String userName, String userPasswd) {
        loginPageAPI.loginUser(given().spec(reqSpec), userName, userPasswd).getResponse().
            then().
            body(Constants.CODE_KEY, is(Status.USER_LOGIN_FAILURE.getCode())).
            body(Constants.MSG_KEY, equalTo(Status.USER_LOGIN_FAILURE.getMsg())).
            body(Constants.DATA_KEY, nullValue()).
            body(Constants.SUCCESS_KEY, equalTo(false));
    }

    static Stream<Arguments> testErrorUserLoginInfoProvider() {
        return Stream.of(
            arguments("admin", "ds"),
            arguments("admin", " "),
            arguments("admin ", Constants.USER_PASSWD)
        );
    }

    @Test
    void testGetUserSessionToObject() {
        LoginResponseEntity login = loginPageAPI.loginUser(given().spec(reqSpec), loginRequestEntity).
            getResponse().jsonPath().getObject(Constants.DATA_KEY, LoginResponseEntity.class);

        assertNotNull(login.getSessionId());
    }
}
