package org.apache.dolphinscheduler.test.apis;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.apis.common.FormParam;
import org.apache.dolphinscheduler.test.apis.common.PageParamEntity;
import org.apache.dolphinscheduler.test.apis.common.RequestMethod;
import org.apache.dolphinscheduler.test.apis.configCenter.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.apis.login.entity.LoginRequestEntity;
import org.apache.dolphinscheduler.test.apis.login.form.LoginFormData;
import org.apache.dolphinscheduler.test.utils.RestResponse;
import org.apache.dolphinscheduler.test.utils.Result;

import java.util.Map;

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


    public static RestResponse<Result> loginUser(RequestSpecification request, String sessionId,
                                                 LoginRequestEntity loginRequest) {
        return new RestResponse<>(Result.class, RestRequestByRequestMap(request, sessionId, loginRequest.toMap(), Route.login(), RequestMethod.POST));
    }

    public static RestResponse<Result> createTenant(RequestSpecification request, String sessionId,
                                                    TenantRequestEntity tenantRequestEntity) {
        return new RestResponse<>(Result.class, RestRequestByRequestMap(request, sessionId, tenantRequestEntity.toMap(), Route.tenants(), RequestMethod.POST));
    }

    public static RestResponse<Result> updateTenant(RequestSpecification request, String sessionId,
                                                    TenantRequestEntity tenantUpdateEntity, int tenantId) {
        return new RestResponse<>(Result.class, RestRequestByRequestMap(request, sessionId, tenantUpdateEntity.toMap(), Route.tenants(tenantId), RequestMethod.PUT));
    }

    public static RestResponse<Result> getTenants(RequestSpecification request, String sessionId, PageParamEntity pageParamEntity) {
        Response resp = request.
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                params(pageParamEntity.toMap()).
                when().get(Route.tenants());

        return new RestResponse<>(Result.class, resp);
    }


    public static RestResponse<Result> getTenantsListAll(RequestSpecification request, String sessionId) {
        Response resp = request.
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                when().get(Route.tenantsList());
        return new RestResponse<>(Result.class, resp);
    }


    public static RestResponse<Result> verifyTenantCode(RequestSpecification request, String sessionId,
                                                        TenantRequestEntity tenantRequestEntity) {
        Response resp = request.
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                params(tenantRequestEntity.toMap()).
                when().get(Route.tenantsVerifyCode());
        return new RestResponse<>(Result.class, resp);
    }


    public static RestResponse<Result> deleteTenantById(RequestSpecification request, String sessionId,
                                                        int tenantId) {
        Response resp = request.
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                when().delete(Route.tenants(tenantId));
        return new RestResponse<>(Result.class, resp);
    }


    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder().
                setBaseUri(BASE_URL).
                setPort(PORT).
                setBasePath(BASE_PATH).build();
    }

    private static Response RestRequestByRequestMap(RequestSpecification request, String sessionId,
                                                    Map<String, ?> map, String url, RequestMethod requestMethod) {
        RequestSpecification rs = request.
                cookies(FormParam.SESSION_ID.getParam(), sessionId).
                formParams(map).
                when();

        if (requestMethod == RequestMethod.PUT) {
            return rs.put(url);
        }

        return rs.post(url);
    }
}
