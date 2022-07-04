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
