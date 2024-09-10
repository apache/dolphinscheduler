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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import lombok.NonNull;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static OkHttpClient CLIENT = new OkHttpClient();

    /**
     * http get request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse get(@NonNull String url,
                                              @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                              @Nullable Map<String, Object> requestParams,
                                              int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        String finalUrl = addUrlParams(requestParams, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
        addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            return new OkHttpResponse(response.code(), getResponseBody(response));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Get request execute failed, url: %s", url), e);
        }
    }

    /**
     * http post request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse post(@NonNull String url,
                                               @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                               @Nullable Map<String, Object> requestParamsMap,
                                               @Nullable Map<String, Object> requestBodyMap,
                                               int connectTimeout,
                                               int writeTimeout,
                                               int readTimeout) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        String finalUrl = addUrlParams(requestParamsMap, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
        addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        if (requestBodyMap != null) {
            requestBuilder = requestBuilder.post(RequestBody.create(
                    JSONUtils.toJsonString(requestBodyMap),
                    MediaType.parse(okHttpRequestHeaders.getOkHttpRequestHeaderContentType().getValue())));
        }
        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), getResponseBody(response));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Post request execute failed, url: %s", url), e);
        }
    }

    /**
     * http put request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse put(@NonNull String url,
                                              @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                              @Nullable Map<String, Object> requestBodyMap,
                                              int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        if (requestBodyMap != null) {
            requestBuilder = requestBuilder.put(RequestBody.create(
                    JSONUtils.toJsonString(requestBodyMap),
                    MediaType.parse(okHttpRequestHeaders.getOkHttpRequestHeaderContentType().getValue())));
        }
        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), getResponseBody(response));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Put request execute failed, url: %s", url), e);
        }
    }

    /**
     * http delete request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse delete(@NonNull String url,
                                                 @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                 int connectTimeout,
                                                 int writeTimeout,
                                                 int readTimeout) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        requestBuilder = requestBuilder.delete();
        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), getResponseBody(response));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Delete request execute failed, url: %s", url), e);
        }
    }

    public static @NonNull String demoPost(@NonNull String url,
                                           @Nullable String token,
                                           @Nullable Map<String, Object> requestBodyMap) throws IOException {

        StringBuilder stringBuffer = new StringBuilder();
        if (requestBodyMap != null) {
            for (String key : requestBodyMap.keySet()) {
                stringBuffer.append(key).append("=").append(requestBodyMap.get(key)).append("&");
            }
        }

        RequestBody body =
                RequestBody.create(stringBuffer.toString(),
                        MediaType.parse(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED.getValue()));

        Request request = new Request.Builder()
                .url(url)
                .header("token", token)
                .addHeader("accpect", "application/json")
                .post(body)
                .build();
        OkHttpClient client = getHttpClient(Constants.HTTP_CONNECT_TIMEOUT, Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_CONNECT_TIMEOUT);
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

    private static String addUrlParams(@Nullable Map<String, Object> requestParams, @NonNull String url) {
        if (requestParams == null) {
            return url;
        }

        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException(String.format("url: %s is invalid", url));
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
        }
        return urlBuilder.toString();
    }

    private static void addHeader(@Nullable Map<String, String> headers, @NonNull Request.Builder requestBuilder) {
        if (headers == null) {
            return;
        }
        headers.forEach(requestBuilder::addHeader);
    }

    private static String getResponseBody(@NonNull Response response) throws IOException {
        if (response.code() != HttpStatus.SC_OK || response.body() == null) {
            return String.format("Request execute failed, httpCode: %s, httpBody: %s",
                    response.code(),
                    response.body());
        }
        return response.body().string();
    }

    private static OkHttpClient getHttpClient(int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout) {
        return CLIENT.newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
    }
}
