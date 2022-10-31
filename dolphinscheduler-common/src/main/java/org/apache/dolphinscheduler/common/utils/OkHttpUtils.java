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

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
            .writeTimeout(5, TimeUnit.MINUTES) // write timeout
            .readTimeout(5, TimeUnit.MINUTES) // read timeout
            .build();

    public static @NonNull String get(@NonNull String url,
                                      @Nullable Map<String, String> httpHeaders,
                                      @Nullable Map<String, Object> requestParams) throws IOException {
        String finalUrl = addUrlParams(requestParams, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
        addHeader(httpHeaders, requestBuilder);
        Request request = requestBuilder.build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return getResponseBody(response);
        }
    }

    public static @NonNull String post(@NonNull String url,
                                       @Nullable Map<String, String> httpHeaders,
                                       @Nullable Map<String, Object> requestParamsMap,
                                       @Nullable Map<String, Object> requestBodyMap) throws IOException {
        String finalUrl = addUrlParams(requestParamsMap, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
        if (requestBodyMap != null) {
            requestBuilder = requestBuilder.post(RequestBody.create(MediaType.parse("application/json"),
                    JSONUtils.toJsonString(requestBodyMap)));
        }
        try (Response response = CLIENT.newCall(requestBuilder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    public static @NonNull String demoPost(@NonNull String url,
                                           @Nullable String token,
                                           @Nullable Map<String, Object> requestBodyMap) throws IOException {

        StringBuffer stringBuffer = new StringBuffer();
        if (requestBodyMap != null) {
            for (String key : requestBodyMap.keySet()) {
                stringBuffer.append(key + "=" + requestBodyMap.get(key) + "&");
            }
        }

        RequestBody body =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), stringBuffer.toString());

        Request request = new Request.Builder()
                .url(url)
                .header("token", token)
                .addHeader("accpect", "application/json")
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
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
            throw new RuntimeException(String.format("Request execute failed, httpCode: %s, httpBody: %s",
                    response.code(),
                    response.body()));
        }
        return response.body().string();
    }
}
