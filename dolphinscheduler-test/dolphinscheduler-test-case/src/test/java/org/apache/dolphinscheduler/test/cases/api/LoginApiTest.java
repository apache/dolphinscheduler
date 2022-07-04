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


package org.apache.dolphinscheduler.test.cases.api;

import org.apache.dolphinscheduler.test.cases.common.AbstractApiTest;
import org.apache.dolphinscheduler.test.endpoint.EndPoints;
import org.apache.dolphinscheduler.test.endpoint.api.common.FormParam;
import org.apache.dolphinscheduler.test.endpoint.api.login.entity.LoginResponseEntity;
import org.apache.dolphinscheduler.test.endpoint.api.login.form.LoginFormData;
import org.apache.dolphinscheduler.test.endpoint.utils.Status;
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

@DisplayName("User Login API interface test")
public class LoginApiTest extends AbstractApiTest {

    @Test
    @DisplayName("Test the correct user information to log in to the system")
    void testUserLogin() {
        EndPoints.loginUser(given().spec(reqSpec),
                        LoginFormData.USR_NAME.getData(),
                        LoginFormData.USER_PASSWD.getData()).
                isResponseSuccessful();
    }

    @Test
    void testGetUserSession() {
        EndPoints.loginUser(given().spec(reqSpec),
                LoginFormData.USR_NAME.getData(),
                LoginFormData.USER_PASSWD.getData()).
                getResponse()
                .then()
                .body("data.sessionId", notNullValue());

    }

    @Test
    void testGetUserSessionToObject() {
        LoginResponseEntity login = EndPoints.loginUser(given().spec(reqSpec),
                        LoginFormData.USR_NAME.getData(),
                        LoginFormData.USER_PASSWD.getData()).
                getResponse().jsonPath().getObject(FormParam.DATA.getParam(), LoginResponseEntity.class);

        assertNotNull(login.getSessionId());
    }

    @ParameterizedTest
    @MethodSource("testErrorUserLoginInfoProvider")
    @DisplayName("Test error user information login system")
    void testErrorUserLogin(String userName, String userPasswd) {
        EndPoints.loginUser(given().spec(reqSpec),userName, userPasswd).getResponse().
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
}
