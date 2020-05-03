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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.Response;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * http utils
 */
public class HttpUtils {

    public static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
    }

    /**
     * get http request content
     *
     * @param url url
     * @return http get request response content
     */
    public static String get(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpget = new HttpGet(url);
        /** set timeout、request time、socket timeout */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(Constants.SOCKET_TIMEOUT)
                .setRedirectsEnabled(true)
                .build();
        httpget.setConfig(requestConfig);
        String responseContent = null;
        CloseableHttpResponse response = null;

        Response result = new Response();
        try {
            response = httpclient.execute(httpget);
            processResponse(response, result);
            responseContent = result.getContent();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (!httpget.isAborted()) {
                httpget.releaseConnection();
                httpget.abort();
            }

            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return responseContent;
    }

    /**
     * execute request
     *
     * @param request http request which could built from RequestBuilder
     * @return response, or null
     */
    public static Response execute(HttpUriRequest request) {
        return execute(request, null);
    }

    /**
     * execute request
     *
     * @param request http request which could built from RequestBuilder
     * @param config http request config
     * @return response, or null
     */
    public static Response execute(HttpUriRequest request, RequestConfig config) {
        Response ret = new Response();
        CloseableHttpClient client = getClient(config);

        try {
            CloseableHttpResponse response = client.execute(request);
            //check response status is 200
            processResponse(response, ret);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (!request.isAborted()) {
                request.abort();
            }

            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return ret.getCode() == -1 ? null : ret;
    }

    /**
     * set timeout、request time、socket timeout and return client
     *
     * @return Closeable http client
     */
    private static CloseableHttpClient getClient(RequestConfig requestConfig) {
        /** set timeout、request time、socket timeout */
        RequestConfig config;
        if (requestConfig != null) {
            config = requestConfig;
        } else {
            config = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
                    .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
                    .setSocketTimeout(Constants.SOCKET_TIMEOUT)
                    .setRedirectsEnabled(true)
                    .build();
        }
        return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    /**
     * @param response http response
     * @param ret response
     */
    private static void processResponse(CloseableHttpResponse response, Response ret) {
        if (response == null) {
            return;
        }

        try {
            ret.setCode(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ret.setContent(EntityUtils.toString(entity, Constants.UTF_8));
            } else {
                logger.warn("http entity is null");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
