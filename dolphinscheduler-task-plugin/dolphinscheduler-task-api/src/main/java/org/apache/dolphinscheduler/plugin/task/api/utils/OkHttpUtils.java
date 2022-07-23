package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import lombok.NonNull;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static @NonNull String get(@NonNull String url, @Nullable Map<String, Object> requestParams)
        throws IOException {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException(String.format("url: %s is invalid", url));
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if (requestParams != null) {
            for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        Request request = new Request.Builder().url(urlBuilder.build()).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                throw new RuntimeException(String.format("Request execute failed, httpCode: %s, httpBody: %s",
                                                         response.code(),
                                                         response.body()));
            }
            return response.body().string();
        }
    }

    public static @NonNull String post(@NonNull String url,
                                       @Nullable Map<String, Object> requestParamsMap,
                                       @Nullable Map<String, Object> requestBodyMap) throws IOException {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException(String.format("url: %s is invalid", url));
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if (requestParamsMap != null) {
            for (Map.Entry<String, Object> entry : requestParamsMap.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        if (requestBodyMap != null) {
            requestBuilder = requestBuilder.post(RequestBody.create(MediaType.parse("application/json"),
                                                                    JSONUtils.toJsonString(requestBodyMap)));
        }
        try (Response response = CLIENT.newCall(requestBuilder.build()).execute()) {
            if (response.code() != 200 || response.body() == null) {
                throw new RuntimeException(String.format("Request execute failed, httpCode: %s, httpBody: %s",
                                                         response.code(),
                                                         response.body()));
            }
            return response.body().string();
        }
    }
}
