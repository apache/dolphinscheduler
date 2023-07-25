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
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;

/**
 * http utils
 */
@Slf4j
public class HttpUtils {

    private HttpUtils() {
        throw new UnsupportedOperationException("Construct HttpUtils");
    }

    public static CloseableHttpClient getInstance() {
        return HttpClientInstance.httpClient;
    }

    private static class HttpClientInstance {

        private static final CloseableHttpClient httpClient = getHttpClientBuilder().build();
    }

    public static HttpClientBuilder getHttpClientBuilder() {
        return HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig);
    }

    private static PoolingHttpClientConnectionManager cm;

    private static SSLContext ctx = null;

    private static SSLConnectionSocketFactory socketFactory;

    private static RequestConfig requestConfig;

    private static Registry<ConnectionSocketFactory> socketFactoryRegistry;

    private static X509TrustManager xtm = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    static {
        try {
            ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{xtm}, null);
        } catch (NoSuchAlgorithmException e) {
            log.error("SSLContext init with NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            log.error("SSLContext init with KeyManagementException", e);
        }
        socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
        /** set timeout、request time、socket timeout */
        requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(Boolean.TRUE)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST, AuthSchemes.SPNEGO))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.SPNEGO))
                .setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT).setSocketTimeout(Constants.SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT).setRedirectsEnabled(true)
                .build();
        socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setDefaultMaxPerRoute(60);
        cm.setMaxTotal(100);

    }

    /**
     * get http request content
     *
     * @param url url
     * @return http get request response content
     */
    public static String get(String url) {
        CloseableHttpClient httpclient = HttpUtils.getInstance();

        HttpGet httpget = new HttpGet(url);
        return getResponseContentString(httpget, httpclient);
    }

    /**
     * get http response content
     *
     * @param httpget    httpget
     * @param httpClient httpClient
     * @return http get request response content
     */
    public static String getResponseContentString(HttpGet httpget, CloseableHttpClient httpClient) {
        if (Objects.isNull(httpget) || Objects.isNull(httpClient)) {
            log.error("HttpGet or HttpClient parameter is null");
            return null;
        }
        String responseContent = null;
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget);
            // check response status is 200
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseContent = EntityUtils.toString(entity, Constants.UTF_8);
                } else {
                    log.warn("http entity is null");
                }
            } else {
                log.error("http get:{} response status code is not 200!", response.getStatusLine().getStatusCode());
            }
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            if (!httpget.isAborted()) {
                httpget.releaseConnection();
                httpget.abort();
            }

        }
        return responseContent;
    }

}
