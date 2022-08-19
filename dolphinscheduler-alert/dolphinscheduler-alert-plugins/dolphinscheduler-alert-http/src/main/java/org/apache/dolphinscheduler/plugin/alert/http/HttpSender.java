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

package org.apache.dolphinscheduler.plugin.alert.http;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class HttpSender {
    private static final Logger logger = LoggerFactory.getLogger(HttpSender.class);
    private static final String URL_SPLICE_CHAR = "?";
    /**
     * request type post
     */
    private static final String REQUEST_TYPE_POST = "POST";
    /**
     * request type get
     */
    private static final String REQUEST_TYPE_GET = "GET";
    private static final String DEFAULT_CHARSET = "utf-8";
    private final String headerParams;
    private final String bodyParams;
    private final String contentField;
    private final String requestType;
    private String url;
    private HttpRequestBase httpRequest;

    public HttpSender(Map<String, String> paramsMap) {

        url = paramsMap.get(HttpAlertConstants.NAME_URL);
        headerParams = paramsMap.get(HttpAlertConstants.NAME_HEADER_PARAMS);
        bodyParams = paramsMap.get(HttpAlertConstants.NAME_BODY_PARAMS);
        contentField = paramsMap.get(HttpAlertConstants.NAME_CONTENT_FIELD);
        requestType = paramsMap.get(HttpAlertConstants.NAME_REQUEST_TYPE);
    }

    public AlertResult send(String msg) {

        AlertResult alertResult = new AlertResult();

        try {
            createHttpRequest(msg);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (httpRequest == null) {
            alertResult.setStatus("false");
            alertResult.setMessage("Request types are not supported");
            return alertResult;
        }

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            String resp = EntityUtils.toString(entity, DEFAULT_CHARSET);
            alertResult.setStatus("true");
            alertResult.setMessage(resp);
        } catch (Exception e) {
            logger.error("send http alert msg  exception : {}", e.getMessage());
            alertResult.setStatus("false");
            alertResult.setMessage("send http request  alert fail.");
        }

        return alertResult;
    }

    private void createHttpRequest(String msg) throws MalformedURLException, URISyntaxException {
        if (REQUEST_TYPE_POST.equals(requestType)) {
            httpRequest = new HttpPost(url);
            setHeader();
            //POST request add param in request body
            setMsgInRequestBody(msg);
        } else if (REQUEST_TYPE_GET.equals(requestType)) {
            //GET request add param in url
            setMsgInUrl(msg);
            URL unencodeUrl = new URL(url);
            URI uri = new URI(unencodeUrl.getProtocol(), unencodeUrl.getHost(), unencodeUrl.getPath(), unencodeUrl.getQuery(), null);

            httpRequest = new HttpGet(uri);
            setHeader();
        }
    }

    /**
     * add msg param in url
     */
    private void setMsgInUrl(String msg) {

        if (StringUtils.isNotBlank(contentField)) {
            String type = "&";
            //check splice char is & or ?
            if (!url.contains(URL_SPLICE_CHAR)) {
                type = URL_SPLICE_CHAR;
            }
            url = String.format("%s%s%s=%s", url, type, contentField, msg);
        }
    }

    /**
     * set header params
     */
    private void setHeader() {

        if (httpRequest == null) {
            return;
        }

        HashMap<String, Object> map = JSONUtils.parseObject(headerParams, HashMap.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            httpRequest.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    /**
     * set body params
     */
    private void setMsgInRequestBody(String msg) {
        try {
            ObjectNode objectNode = JSONUtils.parseObject(bodyParams);
            //set msg content field
            objectNode.put(contentField, msg);
            StringEntity entity = new StringEntity(JSONUtils.toJsonString(objectNode), DEFAULT_CHARSET);
            ((HttpPost) httpRequest).setEntity(entity);
        } catch (Exception e) {
            logger.error("send http alert msg  exception : {}", e.getMessage());
        }
    }
}
