package org.apache.dolphinscheduler.api.test.pages.security.user;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.TenantPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.entity.TenantResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import com.devskiller.jfairy.Fairy;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UserPageAPI implements IUserPageAPI {
    private final Fairy fairy = Fairy.create();
    private final RequestSpecification reqSpec;
    private final String sessionId;
    private TenantPageAPI tenantPageAPI = null;

    public UserPageAPI(RequestSpecification reqSpec, String sessionId) {
        tenantPageAPI = new TenantPageAPI(reqSpec, sessionId);
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> getUsers(PageRequestEntity pageParamEntity) {
        Response resp = getRequestNewInstance().spec(reqSpec).
            cookies(Constants.SESSION_ID_KEY, sessionId).
            params(pageParamEntity.toMap()).
            when().get(Route.queryUserList());
        return toResponse(resp);
    }

    @Override
    public RestResponse<Result> createUser(UserCreateRequestEntity userCreateRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            userCreateRequestEntity.toMap(), Route.createUser(), RequestMethod.POST));
    }

    @Override
    public UserCreateResponseEntity createUser() {
        return createUserByUserEntity(getUserEntityInstance());
    }

    @Override
    public UserCreateResponseEntity createUserByUserEntity(UserCreateRequestEntity userCreateRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            userCreateRequestEntity.toMap(), Route.createUser(), RequestMethod.POST))
            .getResponse().jsonPath().getObject(Constants.DATA_KEY, UserCreateResponseEntity.class);
    }

    @Override
    public UserCreateRequestEntity getUserEntityInstance() {
        return getUserEntityInstance(fairy.person().getFirstName(), Constants.USER_PASSWD, Constants.USER_EMAIL);
    }

    @Override
    public UserCreateRequestEntity getUserEntityInstance(String userName, String passWord, String email) {
        TenantResponseEntity tenant = createTenant();
        UserCreateRequestEntity userCreateRequestEntity = new UserCreateRequestEntity();
        userCreateRequestEntity.setUserName(userName);
        userCreateRequestEntity.setUserPassword(passWord);
        userCreateRequestEntity.setEmail(email);
        userCreateRequestEntity.setTenantId(tenant.getId());
        return userCreateRequestEntity;
    }

    private TenantResponseEntity createTenant() {
        return tenantPageAPI.createTenant();
    }
}
