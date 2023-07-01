/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.utils;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.HttpResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RequestClient {

    private OkHttpClient httpClient = null;

    public RequestClient() {
        this.httpClient = new OkHttpClient();
    }

    @SneakyThrows
    public HttpResponse get(String url, Map<String, String> headers, Map<String, Object> params) {
        String requestUrl = String.format("%s%s%s", Constants.DOLPHINSCHEDULER_API_URL, url, getParams(params));

        Headers headersBuilder = new Headers.Builder().build();
        if (headers != null) {
            headersBuilder = Headers.of(headers);
        }

        log.info("GET request to {}, Headers: {}", requestUrl, headersBuilder);
        Request request = new Request.Builder()
            .url(requestUrl)
            .headers(headersBuilder)
            .get()
            .build();

        Response response = this.httpClient.newCall(request).execute();

        HttpResponseBody responseData = null;
        int responseCode = response.code();
        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData);

        log.info("GET response: {}", httpResponse);

        return httpResponse;
    }

    public static String getParams(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(Constants.QUESTION_MARK);
        if (params.size() > 0) {
            for (Map.Entry<String, Object> item : params.entrySet()) {
                Object value = item.getValue();
                if (Objects.nonNull(value)) {
                    sb.append(Constants.AND_MARK);
                    sb.append(item.getKey());
                    sb.append(Constants.EQUAL_MARK);
                    sb.append(value);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    @SneakyThrows
    public HttpResponse post(String url, Map<String, String> headers, Map<String, Object> params) {
        if (headers == null) {
            headers = new HashMap<>();
        }

        String requestUrl = String.format("%s%s", Constants.DOLPHINSCHEDULER_API_URL, url);
        headers.put("Content-Type", Constants.REQUEST_CONTENT_TYPE);
        Headers headersBuilder = Headers.of(headers);
        RequestBody requestBody = FormBody.create(MediaType.parse(Constants.REQUEST_CONTENT_TYPE), getParams(params));
        log.info("POST request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
            .headers(headersBuilder)
            .url(requestUrl)
            .post(requestBody)
            .build();
        Response response = this.httpClient.newCall(request).execute();
        int responseCode = response.code();
        HttpResponseBody responseData = null;
        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData);

        log.info("POST response: {}", httpResponse);

        return httpResponse;
    }

    @SneakyThrows
    public HttpResponse put(String url, Map<String, String> headers, Map<String, Object> params) {
        if (headers == null) {
            headers = new HashMap<>();
        }

        String requestUrl = String.format("%s%s", Constants.DOLPHINSCHEDULER_API_URL, url);
        headers.put("Content-Type", Constants.REQUEST_CONTENT_TYPE);
        Headers headersBuilder = Headers.of(headers);
        RequestBody requestBody = FormBody.create(MediaType.parse(Constants.REQUEST_CONTENT_TYPE), getParams(params));
        log.info("PUT request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
            .headers(headersBuilder)
            .url(requestUrl)
            .put(requestBody)
            .build();
        Response response = this.httpClient.newCall(request).execute();
        int responseCode = response.code();
        HttpResponseBody responseData = null;
        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData);

        log.info("PUT response: {}", httpResponse);

        return httpResponse;
    }


    @SneakyThrows
    public HttpResponse delete(String url, Map<String, String> headers, Map<String, Object> params) {
        if (headers == null) {
            headers = new HashMap<>();
        }

        String requestUrl = String.format("%s%s", Constants.DOLPHINSCHEDULER_API_URL, url);

        headers.put("Content-Type", Constants.REQUEST_CONTENT_TYPE);

        Headers headersBuilder = Headers.of(headers);

        log.info("DELETE request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
            .headers(headersBuilder)
            .url(requestUrl)
            .delete()
            .build();

        Response response = this.httpClient.newCall(request).execute();

        int responseCode = response.code();
        HttpResponseBody responseData = null;
        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData);

        log.info("DELETE response: {}", httpResponse);

        return httpResponse;
    }
}
