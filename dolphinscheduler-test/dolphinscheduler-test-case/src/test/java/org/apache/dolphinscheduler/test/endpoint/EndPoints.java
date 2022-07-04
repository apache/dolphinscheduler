package org.apache.dolphinscheduler.test.endpoint;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.endpoint.api.common.FormParam;
import org.apache.dolphinscheduler.test.endpoint.api.common.RequestMethod;
import org.apache.dolphinscheduler.test.endpoint.api.login.form.LoginFormData;
import org.apache.dolphinscheduler.test.endpoint.utils.RestResponse;
import org.apache.dolphinscheduler.test.endpoint.utils.Result;

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
