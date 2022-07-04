package org.apache.dolphinscheduler.test.endpoint;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.endpoint.api.common.FormParam;
import org.apache.dolphinscheduler.test.endpoint.api.common.RequestMethod;
import org.apache.dolphinscheduler.test.endpoint.utils.RestResponse;
import org.apache.dolphinscheduler.test.endpoint.utils.Result;

import java.util.Map;

public interface EndPoint {
    default RestResponse<Result> toResponse(Response response) {
        return new RestResponse<>(Result.class, response);
    }

    default Response RestRequestByRequestMap(RequestSpecification request, String sessionId,
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
