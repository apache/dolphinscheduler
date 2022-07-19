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

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.entity.TenantResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TenantPageAPI implements ITenantPageAPI {
    private static final String tenant = System.getProperty("user.name");
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public TenantPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createTenant(TenantRequestEntity tenantRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            tenantRequestEntity.toMap(), Route.tenants(), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> updateTenant(TenantRequestEntity tenantUpdateEntity, int tenantId) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            tenantUpdateEntity.toMap(), Route.tenants(tenantId), RequestMethod.PUT));
    }

    @Override
    public RestResponse<Result> getTenants(PageRequestEntity pageParamEntity) {
        Response resp = getRequestNewInstance().spec(reqSpec).
            cookies(Constants.SESSION_ID_KEY, sessionId).
            params(pageParamEntity.toMap()).
            when().get(Route.tenants());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> getTenantsListAll() {
        Response resp = getRequestNewInstance().spec(reqSpec).
            cookies(Constants.SESSION_ID_KEY, sessionId).
            when().get(Route.tenantsList());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> verifyTenantCode(String tenantCode) {
        Response resp = getRequestNewInstance().spec(reqSpec).
            cookies(Constants.SESSION_ID_KEY, sessionId).
            queryParam(Constants.TENANT_CODE_KEY, tenantCode).
            when().get(Route.tenantsVerifyCode());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> deleteTenantById(int tenantId) {
        Response resp = getRequestNewInstance().spec(reqSpec).
            cookies(Constants.SESSION_ID_KEY, sessionId).
            when().delete(Route.tenants(tenantId));
        return toResponse(resp);
    }

    @Override
    public TenantResponseEntity createTenant() {
        return createTenantByTenantEntity(getTenantEntityInstance(tenant, 1));
    }

    @Override
    public TenantResponseEntity createTenantByTenantEntity(TenantRequestEntity tenantRequestEntity) {
        RestResponse<Result> resultRestResponse = toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            tenantRequestEntity.toMap(), Route.tenants(), RequestMethod.POST));
        resultRestResponse.isResponseSuccessful();
        return resultRestResponse.getResponse().jsonPath().getObject(Constants.DATA_KEY, TenantResponseEntity.class);
    }

    @Override
    public TenantRequestEntity getTenantEntityInstance() {
        return getTenantEntityInstance(tenant, 1);
    }

    @Override
    public TenantRequestEntity getTenantEntityInstance(String tenantCode, int queueId) {
        TenantRequestEntity tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(tenantCode);
        tenantRequestEntity.setQueueId(queueId);
        return tenantRequestEntity;
    }
}
