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

package org.apache.dolphinscheduler.api.test.pages;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.HttpResponseBody;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class OidcLoginPage {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false)
            .build();

    @SneakyThrows
    private HttpResponse get(String url, Map<String, Object> params) {
        String requestUrl =
                String.format("%s%s%s", Constants.DOLPHINSCHEDULER_API_URL, url, RequestClient.getParams(params));
        Request request = new Request.Builder().url(requestUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            int responseCode = response.code();
            Map<String, String> responseHeaders = new HashMap<>();
            Headers responseHeadersObj = response.headers();
            for (String name : responseHeadersObj.names()) {
                responseHeaders.put(name, responseHeadersObj.get(name));
            }

            HttpResponseBody responseData = null;
            if (response.body() != null) {
                String contentType = response.header("Content-Type");
                if (contentType != null && contentType.contains("application/json")) {
                    responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
                }
            }
            return new HttpResponse(responseCode, responseData, responseHeaders);
        }
    }

    public HttpResponse getOidcProviders() {
        return new RequestClient().get("/oidc-providers", null, new HashMap<>());
    }

    public HttpResponse initiateOidcLogin(String providerId) {
        return get("/oauth2/authorization/" + providerId, new HashMap<>());
    }

    public HttpResponse initiateOidcLoginWithInvalidProvider(String invalidProviderId) {
        return get("/oauth2/authorization/" + invalidProviderId, new HashMap<>());
    }

    public HttpResponse handleOidcCallback(String providerId, String code, String state) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("state", state);
        return get("/login/oauth2/code/" + providerId, params);
    }

    public HttpResponse handleOidcCallbackError(String providerId, String error, String state) {
        Map<String, Object> params = new HashMap<>();
        params.put("error", error);
        params.put("state", state);
        return get("/login/oauth2/code/" + providerId, params);
    }

    public HttpResponse handleOidcCallbackMissingCode(String providerId, String state) {
        Map<String, Object> params = new HashMap<>();
        params.put("state", state);
        return get("/login/oauth2/code/" + providerId, params);
    }

    /**
     * Optional: Test /login endpoint in OIDC mode should be disabled
     */
    public HttpResponse loginWithPassword(String username, String password) {
        return new LoginPage().login(username, password);
    }
}
