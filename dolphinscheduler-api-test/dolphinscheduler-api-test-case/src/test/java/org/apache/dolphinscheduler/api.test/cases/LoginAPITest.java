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

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.common.FormParam;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.login.LoginPageAPI;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.login.form.LoginFormData;
import org.apache.dolphinscheduler.api.test.utils.Status;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("User Login API test")
public class LoginAPITest extends AbstractAPITest {
    private LoginPageAPI loginPageAPI = null;
    private LoginRequestEntity loginRequestEntity = null;

    @BeforeAll
    public void initTenantPageAPIFactory() {
        loginPageAPI = pageAPIFactory.createLoginPageAPI();
        loginRequestEntity = new LoginRequestEntity();
        loginRequestEntity.setUserName(LoginFormData.USR_NAME.getData());
        loginRequestEntity.setUserPassword(LoginFormData.USER_PASSWD.getData());
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
                body(FormParam.CODE.getParam(), is(Status.USER_NAME_PASSWD_ERROR.getCode())).
                body(FormParam.MSG.getParam(), equalTo(Status.USER_NAME_PASSWD_ERROR.getMsg())).
                body(FormParam.DATA.getParam(), nullValue()).
                body(FormParam.SUCCESS.getParam(), equalTo(false));
    }

    static Stream<Arguments> testErrorUserLoginInfoProvider() {
        return Stream.of(
                arguments("admin", "ds"),
                arguments("admin", " "),
                arguments("admin ", LoginFormData.USER_PASSWD.getData())
        );
    }


    @Test
    void testGetUserSessionToObject() {
        LoginResponseEntity login = loginPageAPI.loginUser(given().spec(reqSpec), loginRequestEntity).
                getResponse().jsonPath().getObject(FormParam.DATA.getParam(), LoginResponseEntity.class);

        assertNotNull(login.getSessionId());
    }
}
