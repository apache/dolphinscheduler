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
import org.apache.dolphinscheduler.common.model.OkHttpResult;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import lombok.NonNull;

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
     * Executes a synchronous GET request and returns both the {@link Call} object (for cancellation support)
     * and the HTTP response.
     *
     * <p>This method enables the caller to retain a reference to the underlying OkHttp {@link Call},
     * allowing the request to be canceled from another thread (e.g., during task interruption).
     * Although the execution is blocking, calling {@link Call#cancel()} concurrently will cause
     * {@link Call#execute()} to throw an {@link IOException} with the message "canceled".
     *
     * @param url                the target URL for the GET request (must not be null)
     * @param okHttpRequestHeaders optional HTTP headers; may be null
     * @param requestParams      query parameters to be appended to the URL as key-value pairs; may be null
     * @param connectTimeout     connection timeout in milliseconds
     * @param writeTimeout       write (send) timeout in milliseconds
     * @param readTimeout        read (receive) timeout in milliseconds
     * @return                   a wrapper containing the {@link Call} and the resulting {@link OkHttpResponse}
     * @throws IOException       if a network error occurs or the request is canceled during execution
     * @throws RuntimeException  if an unexpected error occurs during request processing
     */
    public static @NonNull OkHttpResult getWithCall(
                                                    @NonNull String url,
                                                    @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                    @Nullable Map<String, Object> requestParams,
                                                    int connectTimeout,
                                                    int writeTimeout,
                                                    int readTimeout) throws IOException {

        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        String finalUrl = addUrlParams(requestParams, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

        if (okHttpRequestHeaders != null && okHttpRequestHeaders.getHeaders() != null) {
            addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        }

        // Build the final request and create a Call object
        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            // Wrap the raw response into the application-specific response type
            OkHttpResponse okHttpResponse = new OkHttpResponse(response.code(), getResponseBody(response));
            return new OkHttpResult(call, okHttpResponse);
        } catch (IOException e) {
            // Distinguish explicit cancellation from other I/O failures
            if (call.isCanceled()) {
                throw new IOException("Request was canceled", e);
            }
            // Wrap unexpected errors with context for debugging
            throw new RuntimeException(String.format("GET request execution failed, URL: %s", url), e);
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
     * Executes a synchronous POST request and returns both the {@link Call} object (for cancellation support)
     * and the HTTP response.
     *
     * <p>This method is designed to be used in scenarios where the caller needs to retain a reference
     * to the underlying OkHttp {@link Call} in order to cancel the request later (e.g., during task interruption).
     * The request is executed synchronously (blocking), but cancellation from another thread will cause
     * {@link Call#execute()} to throw an {@link IOException} with the message "canceled".
     *
     * @param url                the request URL (must not be null)
     * @param okHttpRequestHeaders optional request headers; may be null
     * @param requestParamsMap   query parameters to append to the URL; may be null
     * @param requestBodyMap     request body as a JSON-compatible map; if null, an empty plain-text body is used
     * @param connectTimeout     connection timeout in milliseconds
     * @param writeTimeout       write (send) timeout in milliseconds
     * @param readTimeout        read (receive) timeout in milliseconds
     * @return                   a wrapper containing the {@link Call} and the resulting {@link OkHttpResponse}
     * @throws IOException       if a network error occurs or the request is canceled during execution
     * @throws RuntimeException  if an unexpected error occurs during request processing
     */
    public static @NonNull OkHttpResult postWithCall(
                                                     @NonNull String url,
                                                     @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                     @Nullable Map<String, Object> requestParamsMap,
                                                     @Nullable Map<String, Object> requestBodyMap,
                                                     int connectTimeout,
                                                     int writeTimeout,
                                                     int readTimeout) throws IOException {

        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        String finalUrl = addUrlParams(requestParamsMap, url);
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

        if (okHttpRequestHeaders != null) {
            addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        }

        if (requestBodyMap != null) {
            String jsonBody = JSONUtils.toJsonString(requestBodyMap);
            String contentType = OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue();
            if (okHttpRequestHeaders != null && okHttpRequestHeaders.getOkHttpRequestHeaderContentType() != null) {
                contentType = okHttpRequestHeaders.getOkHttpRequestHeaderContentType().getValue();
            }
            MediaType mediaType = MediaType.parse(contentType);
            RequestBody body = RequestBody.create(jsonBody, mediaType);
            requestBuilder.post(body);
        } else {
            // Use an empty body for POST requests without a payload (rare but allowed)
            requestBuilder.post(RequestBody.create("", MediaType.parse("text/plain")));
        }

        // Build the final request and create a Call object
        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            // Wrap the raw OkHttp response into the application-specific OkHttpResponse
            OkHttpResponse okHttpResponse = new OkHttpResponse(response.code(), getResponseBody(response));
            return new OkHttpResult(call, okHttpResponse);
        } catch (IOException e) {
            // If the call was explicitly canceled, rethrow as a clear IOException
            if (call.isCanceled()) {
                throw new IOException("Request was canceled", e);
            }
            // For all other I/O failures, wrap in a RuntimeException with context
            throw new RuntimeException(String.format("POST request execution failed, URL: %s", url), e);
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
     * Executes a synchronous PUT request and returns both the {@link Call} object (for cancellation support)
     * and the HTTP response.
     *
     * <p>This method allows the caller to retain a reference to the underlying OkHttp {@link Call},
     * enabling request cancellation from another thread (e.g., during task interruption).
     * Although the request is executed synchronously (blocking), calling {@link Call#cancel()}
     * from another thread will cause {@link Call#execute()} to throw an {@link IOException}
     * with the message "canceled".
     *
     * @param url                the target URL for the PUT request (must not be null)
     * @param okHttpRequestHeaders optional HTTP headers; may be null
     * @param requestBodyMap     request body as a JSON-compatible map; if null, the PUT request will have no body
     * @param connectTimeout     connection timeout in milliseconds
     * @param writeTimeout       write (send) timeout in milliseconds
     * @param readTimeout        read (receive) timeout in milliseconds
     * @return                   a wrapper containing the {@link Call} and the resulting {@link OkHttpResponse}
     * @throws IOException       if a network error occurs or the request is canceled during execution
     * @throws RuntimeException  if an unexpected error occurs during request processing
     */
    public static @NonNull OkHttpResult putWithCall(
                                                    @NonNull String url,
                                                    @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                    @Nullable Map<String, Object> requestBodyMap,
                                                    int connectTimeout,
                                                    int writeTimeout,
                                                    int readTimeout) throws IOException {

        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (okHttpRequestHeaders != null && okHttpRequestHeaders.getHeaders() != null) {
            addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        }

        if (requestBodyMap != null) {
            String jsonBody = JSONUtils.toJsonString(requestBodyMap);

            // Determine content type; default to application/json if not specified
            String contentType = OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue();
            if (okHttpRequestHeaders != null && okHttpRequestHeaders.getOkHttpRequestHeaderContentType() != null) {
                contentType = okHttpRequestHeaders.getOkHttpRequestHeaderContentType().getValue();
            }
            MediaType mediaType = MediaType.parse(contentType);
            RequestBody body = RequestBody.create(jsonBody, mediaType);
            requestBuilder.put(body);
        }

        // Build the final request and create a Call object
        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            // Convert the raw OkHttp response into the application-specific response object
            OkHttpResponse okHttpResponse = new OkHttpResponse(response.code(), getResponseBody(response));
            return new OkHttpResult(call, okHttpResponse);
        } catch (IOException e) {
            // Distinguish cancellation from other I/O errors
            if (call.isCanceled()) {
                throw new IOException("Request was canceled", e);
            }
            // Wrap unexpected failures with context
            throw new RuntimeException(String.format("PUT request execution failed, URL: %s", url), e);
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

    /**
     * Executes a synchronous DELETE request and returns both the {@link Call} object (for cancellation support)
     * and the HTTP response.
     *
     * <p>This method enables the caller to keep a reference to the underlying OkHttp {@link Call},
     * allowing the request to be canceled from another thread (e.g., during task interruption).
     * Although the execution is blocking (synchronous), invoking {@link Call#cancel()} concurrently
     * will cause {@link Call#execute()} to throw an {@link IOException} with the message "canceled".
     *
     * @param url                the target URL for the DELETE request (must not be null)
     * @param okHttpRequestHeaders optional HTTP headers; may be null
     * @param connectTimeout     connection timeout in milliseconds
     * @param writeTimeout       write (send) timeout in milliseconds
     * @param readTimeout        read (receive) timeout in milliseconds
     * @return                   a wrapper containing the {@link Call} and the resulting {@link OkHttpResponse}
     * @throws IOException       if a network error occurs or the request is canceled during execution
     * @throws RuntimeException  if an unexpected error occurs during request processing
     */
    public static @NonNull OkHttpResult deleteWithCall(
                                                       @NonNull String url,
                                                       @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                       int connectTimeout,
                                                       int writeTimeout,
                                                       int readTimeout) throws IOException {

        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout);
        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (okHttpRequestHeaders != null && okHttpRequestHeaders.getHeaders() != null) {
            addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        }

        requestBuilder.delete();
        // Build the final request and create a Call object
        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            // Wrap the raw OkHttp response into the application-specific response type
            OkHttpResponse okHttpResponse = new OkHttpResponse(response.code(), getResponseBody(response));
            return new OkHttpResult(call, okHttpResponse);
        } catch (IOException e) {
            // If the call was canceled externally, propagate a clear cancellation exception
            if (call.isCanceled()) {
                throw new IOException("Request was canceled", e);
            }
            // For all other failures, wrap with context for easier debugging
            throw new RuntimeException(String.format("DELETE request execution failed, URL: %s", url), e);
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
