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

package org.apache.dolphinscheduler.api.test.utils;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.HttpResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
        Map<String, String> responseHeaders = new HashMap<>();

        Headers responseHeadersObj = response.headers();
        for (String name : responseHeadersObj.names()) {
            responseHeaders.put(name, responseHeadersObj.get(name));
        }

        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData, responseHeaders);

        log.info("GET response: {}", httpResponse);

        return httpResponse;
    }

    public static String getParams(Map<String, Object> params) {
        return getParams(params, true);
    }

    public static String getParams(Map<String, Object> params, boolean includeQuestionMark) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        if (!(params instanceof SortedMap)) {
            params = new TreeMap<>(params);
        }

        StringBuilder sb = new StringBuilder(params.size() * 16);
        boolean isFirst = true;

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            String key = entry.getKey();

            if (value.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object item = java.lang.reflect.Array.get(value, i);
                    if (item != null) {
                        appendParam(sb, isFirst, key, item.toString(), includeQuestionMark);
                        isFirst = false;
                    }
                }
            } else {
                appendParam(sb, isFirst, key, value.toString(), includeQuestionMark);
                isFirst = false;
            }
        }

        return sb.toString();
    }

    private static void appendParam(StringBuilder sb, boolean isFirst, String key, String value,
                                    boolean includeQuestionMark) {
        if (isFirst) {
            if (includeQuestionMark) {
                sb.append(Constants.QUESTION_MARK);
            }
        } else {
            sb.append(Constants.AND_MARK);
        }
        try {
            sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()))
                    .append(Constants.EQUAL_MARK)
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            sb.append(key).append(Constants.EQUAL_MARK).append(value);
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
        RequestBody requestBody =
                FormBody.create(getParams(params, false), MediaType.parse(Constants.REQUEST_CONTENT_TYPE));
        log.info("POST request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
                .headers(headersBuilder)
                .url(requestUrl)
                .post(requestBody)
                .build();
        Response response = this.httpClient.newCall(request).execute();
        int responseCode = response.code();
        HttpResponseBody responseData = null;
        Map<String, String> responseHeaders = new HashMap<>();

        Headers responseHeadersObj = response.headers();
        for (String name : responseHeadersObj.names()) {
            responseHeaders.put(name, responseHeadersObj.get(name));
        }

        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData, responseHeaders);

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
        RequestBody requestBody =
                FormBody.create(getParams(params, false), MediaType.parse(Constants.REQUEST_CONTENT_TYPE));
        log.info("PUT request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
                .headers(headersBuilder)
                .url(requestUrl)
                .put(requestBody)
                .build();
        Response response = this.httpClient.newCall(request).execute();
        int responseCode = response.code();
        HttpResponseBody responseData = null;
        Map<String, String> responseHeaders = new HashMap<>();

        Headers responseHeadersObj = response.headers();
        for (String name : responseHeadersObj.names()) {
            responseHeaders.put(name, responseHeadersObj.get(name));
        }

        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData, responseHeaders);

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
        Map<String, String> responseHeaders = new HashMap<>();

        Headers responseHeadersObj = response.headers();
        for (String name : responseHeadersObj.names()) {
            responseHeaders.put(name, responseHeadersObj.get(name));
        }

        if (response.body() != null) {
            responseData = JSONUtils.parseObject(response.body().string(), HttpResponseBody.class);
        }
        response.close();

        HttpResponse httpResponse = new HttpResponse(responseCode, responseData, responseHeaders);

        log.info("DELETE response: {}", httpResponse);

        return httpResponse;
    }
}
