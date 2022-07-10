package org.apache.dolphinscheduler.api.test.pages.token;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.Route;
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
