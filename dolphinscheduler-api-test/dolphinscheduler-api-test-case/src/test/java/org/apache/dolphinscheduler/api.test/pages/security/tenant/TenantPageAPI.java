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

package org.apache.dolphinscheduler.api.test.pages.security.tenant;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.api.test.core.common.FormParam;
import org.apache.dolphinscheduler.api.test.entity.PageParamEntity;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import static io.restassured.RestAssured.given;

public class TenantPageAPI implements ITenantPageAPI {
    private final RequestSpecification reqSpec;
    private final String sessionId;


    public TenantPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createTenant(TenantRequestEntity tenantRequestEntity) {
        return toResponse(RestRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
                tenantRequestEntity.toMap(), Route.tenants(), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> updateTenant(TenantRequestEntity tenantUpdateEntity, int tenantId) {
        return toResponse(RestRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
                tenantUpdateEntity.toMap(), Route.tenants(tenantId), RequestMethod.PUT));
    }

    @Override
    public RestResponse<Result> getTenants(PageParamEntity pageParamEntity) {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                params(pageParamEntity.toMap()).
                when().get(Route.tenants());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> getTenantsListAll() {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                when().get(Route.tenantsList());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> verifyTenantCode(String tenantCode) {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                queryParam("tenantCode", tenantCode).
                when().get(Route.tenantsVerifyCode());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> deleteTenantById(int tenantId) {
        Response resp = getRequestNewInstance().spec(reqSpec).
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                when().delete(Route.tenants(tenantId));
        return toResponse(resp);
    }
}
