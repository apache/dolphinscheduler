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

package org.apache.dolphinscheduler.api.test.pages.token;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenGenerateEntity;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TokenPageAPI implements ITokenPageAPI {
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public TokenPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> generateToken(TokenGenerateEntity tokenGenerateEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            tokenGenerateEntity.toMap(), Route.generateAccessToken(), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> createToken(TokenRequestEntity tokenRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
                tokenRequestEntity.toMap(), Route.accessTokens(), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> updateToken(TokenRequestEntity tokenRequestEntity, int id) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
                tokenRequestEntity.toMap(), Route.accessTokens(id), RequestMethod.PUT));
    }

    @Override
    public RestResponse<Result> queryTokenByUser(int userId) {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(Constants.SESSION_ID_KEY, sessionId).
                when().get(Route.accessTokenByUser(userId));
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> queryTokenList(PageRequestEntity pageParamEntity) {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(Constants.SESSION_ID_KEY, sessionId).
                params(pageParamEntity.toMap()).
                when().get(Route.accessTokens());
        return toResponse(resp);
    }
}
