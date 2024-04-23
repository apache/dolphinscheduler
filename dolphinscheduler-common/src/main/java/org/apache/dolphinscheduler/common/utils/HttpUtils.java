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

import org.apache.http.HttpEntity;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP utilities class with secure SSL context.
 */
@Slf4j
public class HttpUtils {

    private static final PoolingHttpClientConnectionManager cm;
    private static final SSLContext ctx;
    private static final SSLConnectionSocketFactory socketFactory;
    private static final RequestConfig requestConfig;

    static {
        try {
            // Use default SSL context which includes standard certificate validation
            ctx = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to get default SSLContext", e);
            throw new RuntimeException("Failed to get default SSLContext", e);
        }

        socketFactory = new SSLConnectionSocketFactory(ctx, new DefaultHostnameVerifier());

        // Set timeout, request time, socket timeout
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(Boolean.TRUE)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST, AuthSchemes.SPNEGO))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.SPNEGO))
                .setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
                .setSocketTimeout(Constants.SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setRedirectsEnabled(true)
                .build();

        cm = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", socketFactory)
                        .build());

        cm.setDefaultMaxPerRoute(60);
        cm.setMaxTotal(100);
    }

    // Private constructor to prevent instantiation
    private HttpUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Returns a singleton instance of the HTTP client
    public static CloseableHttpClient getInstance() {
        return HttpClientInstance.httpClient;
    }

    private static class HttpClientInstance {

        private static final CloseableHttpClient httpClient = getHttpClientBuilder().build();
    }

    // Builds and returns an HttpClient with the custom configuration
    public static HttpClientBuilder getHttpClientBuilder() {
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig);
    }

    /**
     * Executes a GET request and returns the response content as a string.
     *
     * @param url The URL to send the GET request to
     * @return The response content as a string
     */
    public static String get(String url) {
        CloseableHttpClient httpClient = getInstance();
        HttpGet httpGet = new HttpGet(url);
        return getResponseContentString(httpGet, httpClient);
    }

    /**
     * Gets the response content from an executed HttpGet request.
     *
     * @param httpGet     The HttpGet request to execute
     * @param httpClient  The HttpClient to use for the request
     * @return The response content as a string
     */
    public static String getResponseContentString(HttpGet httpGet, CloseableHttpClient httpClient) {
        if (httpGet == null || httpClient == null) {
            log.error("HttpGet or HttpClient parameter is null");
            return null;
        }

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            // Check if the response status is 200 (OK)
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error("HTTP GET request to {} returned status code: {}", httpGet.getURI(),
                        response.getStatusLine().getStatusCode());
                return null;
            }

            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            log.error("Error executing HTTP GET request", e);
            return null;
        } finally {
            httpGet.releaseConnection();
        }
    }
}
