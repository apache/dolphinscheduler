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

package org.apache.dolphinscheduler.api.test.utils;

import static org.hamcrest.Matchers.equalTo;

import org.apache.dolphinscheduler.api.test.base.IRestResponse;
import org.apache.dolphinscheduler.api.test.core.common.Constants;

import java.util.Objects;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class RestResponse<T> implements IRestResponse<T> {
    private T data;
    private Response response;
    private Exception e;

    public RestResponse(Class<T> t, Response response) {
        this.response = response;
        try {
            this.data = t.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("There should be a default constructor in the Response POJO");
        }
    }

    public String getContent() {
        return response.getBody().asString();
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public boolean isSuccessful() {
        int code = response.getStatusCode();
        return code >= 200 && code <= 300;
    }

    public ValidatableResponse isResponseSuccessful() {
        return response.then().
                body(Constants.CODE_KEY, equalTo(0));
    }

    public String getStatusDescription() {
        return response.getStatusLine();
    }

    public Response getResponse() {
        return response;
    }

    public Object getResponseData() {
        return Objects.requireNonNull(JSONUtils.parseObject(getContent(), Result.class)).getData();
    }

    public <T> T getResponseJsonData(Class<T> targetType) {
        return JSONUtils.convertValue(getResponseData(), targetType);
    }

    public T getBody() {
        try {
            data = (T) response.getBody().as(data.getClass());
        } catch (Exception e) {
            this.e = e;
        }
        return data;
    }

    public Exception getException() {
        return e;
    }

    @Override
    public String toString() {
        return response.asString();
    }
}