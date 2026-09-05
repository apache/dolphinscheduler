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

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.net.SocketFactory;

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
     * Socket factory that builds sockets with TCP keepalive (SO_KEEPALIVE) enabled. OkHttp creates the
     * raw socket via {@link SocketFactory#createSocket()} and connects it itself; enabling keepalive on the
     * returned socket is preserved once the connection is established.
     */
    private static final SocketFactory KEEP_ALIVE_SOCKET_FACTORY = new SocketFactory() {

        @Override
        public Socket createSocket() throws IOException {
            return enableTcpKeepAlive(SocketFactory.getDefault().createSocket());
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTcpKeepAlive(SocketFactory.getDefault().createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTcpKeepAlive(SocketFactory.getDefault().createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTcpKeepAlive(SocketFactory.getDefault().createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                                   int localPort) throws IOException {
            return enableTcpKeepAlive(
                    SocketFactory.getDefault().createSocket(address, port, localAddress, localPort));
        }

        private Socket enableTcpKeepAlive(Socket socket) throws IOException {
            socket.setKeepAlive(true);
            return socket;
        }
    };

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
        return get(url, okHttpRequestHeaders, requestParams,
                connectTimeout, writeTimeout, readTimeout, false);
    }

    /**
     * http get request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @param keepAlive whether to enable TCP keepalive (SO_KEEPALIVE) on the socket
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse get(@NonNull String url,
                                              @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                              @Nullable Map<String, Object> requestParams,
                                              int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout,
                                              boolean keepAlive) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout, keepAlive);
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
        return post(url, okHttpRequestHeaders, requestParamsMap, requestBodyMap,
                connectTimeout, writeTimeout, readTimeout, false);
    }

    /**
     * http post request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @param keepAlive whether to enable TCP keepalive (SO_KEEPALIVE) on the socket
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse post(@NonNull String url,
                                               @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                               @Nullable Map<String, Object> requestParamsMap,
                                               @Nullable Map<String, Object> requestBodyMap,
                                               int connectTimeout,
                                               int writeTimeout,
                                               int readTimeout,
                                               boolean keepAlive) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout, keepAlive);
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
        return put(url, okHttpRequestHeaders, requestBodyMap,
                connectTimeout, writeTimeout, readTimeout, false);
    }

    /**
     * http put request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @param keepAlive whether to enable TCP keepalive (SO_KEEPALIVE) on the socket
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse put(@NonNull String url,
                                              @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                              @Nullable Map<String, Object> requestBodyMap,
                                              int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout,
                                              boolean keepAlive) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout, keepAlive);
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
        return delete(url, okHttpRequestHeaders, connectTimeout, writeTimeout, readTimeout, false);
    }

    /**
     * http delete request
     * @param connectTimeout connect timeout in milliseconds
     * @param writeTimeout write timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @param keepAlive whether to enable TCP keepalive (SO_KEEPALIVE) on the socket
     * @return OkHttpResponse
     * @throws RuntimeException
     */
    public static @NonNull OkHttpResponse delete(@NonNull String url,
                                                 @Nullable OkHttpRequestHeaders okHttpRequestHeaders,
                                                 int connectTimeout,
                                                 int writeTimeout,
                                                 int readTimeout,
                                                 boolean keepAlive) throws IOException {
        OkHttpClient client = getHttpClient(connectTimeout, writeTimeout, readTimeout, keepAlive);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        addHeader(okHttpRequestHeaders.getHeaders(), requestBuilder);
        requestBuilder = requestBuilder.delete();
        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return new OkHttpResponse(response.code(), getResponseBody(response));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Delete request execute failed, url: %s", url), e);
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
        return getHttpClient(connectTimeout, writeTimeout, readTimeout, false);
    }

    private static OkHttpClient getHttpClient(int connectTimeout,
                                              int writeTimeout,
                                              int readTimeout,
                                              boolean keepAlive) {
        return CLIENT.newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .socketFactory(keepAlive ? KEEP_ALIVE_SOCKET_FACTORY : SocketFactory.getDefault())
                .build();
    }
}
