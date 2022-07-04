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

package org.apache.dolphinscheduler.test.endpoint;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.endpoint.api.login.form.LoginFormData;
import org.apache.dolphinscheduler.test.endpoint.utils.RestResponse;
import org.apache.dolphinscheduler.test.endpoint.utils.Result;


public class EndPoints {
    public static final String BASE_URL = "http://localhost";
    public static final String BASE_PATH = "/dolphinscheduler";
    public static final int PORT = 3000;


    public static RestResponse<Result> getSession(RequestSpecification request, RequestSpecification reqSpec) {
        Response resp = request.
                spec(reqSpec).
                formParam(LoginFormData.USR_NAME.getParam(), LoginFormData.USR_NAME.getData()).
                formParam(LoginFormData.USER_PASSWD.getParam(), LoginFormData.USER_PASSWD.getData()).
                when().
                post(Route.login());
        return new RestResponse<>(Result.class, resp);
    }

    public static RestResponse<Result> loginUser(RequestSpecification request,
                                                 String User, String Passwd) {
        Response resp = request.
                formParam(LoginFormData.USR_NAME.getParam(), User).
                formParam(LoginFormData.USER_PASSWD.getParam(), Passwd).
                when().
                post(Route.login());
        return new RestResponse<>(Result.class, resp);
    }

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder().
                setBaseUri(BASE_URL).
                setPort(PORT).
                setBasePath(BASE_PATH).build();
    }

}
