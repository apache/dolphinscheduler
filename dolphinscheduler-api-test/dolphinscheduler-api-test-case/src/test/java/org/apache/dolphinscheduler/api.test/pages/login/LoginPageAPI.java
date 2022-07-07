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

package org.apache.dolphinscheduler.api.test.pages.login;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.login.form.LoginFormData;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public class LoginPageAPI implements ILoginPageAPI {

    public LoginPageAPI() {
    }


    @Override
    public RestResponse<Result> loginUser(RequestSpecification request, LoginRequestEntity loginRequestEntity) {
        Response resp = request.
                params(loginRequestEntity.toMap()).
                when().
                post(Route.login());
        return new RestResponse<>(Result.class, resp);
    }

    @Override
    public RestResponse<Result> loginUser(RequestSpecification request,
                                                 String User, String Passwd) {
        Response resp = request.
                formParam(LoginFormData.USR_NAME.getParam(), User).
                formParam(LoginFormData.USER_PASSWD.getParam(), Passwd).
                when().
                post(Route.login());
        return new RestResponse<>(Result.class, resp);
    }

    @Override
    public RestResponse<Result> loginOut(RequestSpecification request) {
        Response resp = request.
                when().
                post(Route.loginOut());
        return new RestResponse<>(Result.class, resp);
    }

}
