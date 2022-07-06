package org.apache.dolphinscheduler.api.test.pages.login;

import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.api.test.pages.login.entity.LoginRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface ILoginPageAPI {
    RestResponse<Result> loginUser(RequestSpecification request,
                                   LoginRequestEntity loginRequestEntity);

    RestResponse<Result> loginUser(RequestSpecification request,
                                   String User, String Passwd);

    RestResponse<Result> loginOut(RequestSpecification request);
}
