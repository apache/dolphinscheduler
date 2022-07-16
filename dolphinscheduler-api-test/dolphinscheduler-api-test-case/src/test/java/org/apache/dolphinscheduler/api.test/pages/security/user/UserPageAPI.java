package org.apache.dolphinscheduler.api.test.pages.security.user;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
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

    public UserPageAPI(RequestSpecification reqSpec, String sessionId) {
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
        return null;
    }

    @Override
    public UserCreateResponseEntity createUserByUserEntity(ProjectRequestEntity projectRequestEntity) {
        return null;
    }

    @Override
    public UserCreateRequestEntity getUserEntityInstance() {
        return null;
    }

    @Override
    public UserCreateRequestEntity getUserEntityInstance(String userName, String passWord, String email) {
        UserCreateRequestEntity userCreateRequestEntity = new UserCreateRequestEntity();
        userCreateRequestEntity.setUserName(userName);
        userCreateRequestEntity.setUserPassword(passWord);
        userCreateRequestEntity.setEmail(email);
        return userCreateRequestEntity;
    }
}
