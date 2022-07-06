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
