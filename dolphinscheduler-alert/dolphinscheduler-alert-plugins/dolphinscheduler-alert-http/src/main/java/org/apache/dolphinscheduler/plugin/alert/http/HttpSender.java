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

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HttpSender {

    private Map<String, String> headerParams;
    private OkHttpRequestHeaderContentType contentType;
    private Map<String, String> bodyParams;
    private HttpRequestMethod requestType;
    private int timeout;
    private String url;

    public HttpSender(Map<String, String> paramsMap) {
        paramsValidator(paramsMap);
    }

    private void paramsValidator(Map<String, String> paramsMap) {
        url = paramsMap.get(HttpAlertConstants.NAME_URL);
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url can not be null");
        }

        String headerParamsString = paramsMap.get(HttpAlertConstants.NAME_HEADER_PARAMS);
        if (StringUtils.isNotBlank(headerParamsString)) {
            headerParams = JSONUtils.toMap(headerParamsString);
            if (headerParams == null) {
                throw new IllegalArgumentException("headerParams is not a valid json");
            }
        } else {
            headerParams = new HashMap<>();
        }

        String bodyParamsString = paramsMap.get(HttpAlertConstants.NAME_BODY_PARAMS);
        if (StringUtils.isNotBlank(bodyParamsString)) {
            bodyParams = JSONUtils.toMap(bodyParamsString);
            if (bodyParams == null) {
                throw new IllegalArgumentException("bodyParams is not a valid json");
            }
        } else {
            bodyParams = new HashMap<>();
        }

        try {
            requestType = HttpRequestMethod.valueOf(paramsMap.get(HttpAlertConstants.NAME_REQUEST_TYPE));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("requestType is not a valid value");
        }

        contentType = OkHttpRequestHeaderContentType.fromValue(paramsMap.get(HttpAlertConstants.NAME_CONTENT_TYPE));
        if (contentType == null) {
            throw new IllegalArgumentException("contentType is not a valid value");
        }

        timeout = StringUtils.isNotBlank(paramsMap.get(HttpAlertConstants.NAME_TIMEOUT))
                ? Integer.parseInt(paramsMap.get(HttpAlertConstants.NAME_TIMEOUT))
                : HttpAlertConstants.DEFAULT_TIMEOUT * 1000;
    }

    public AlertResult send(String msg) {

        AlertResult alertResult = new AlertResult();
        OkHttpResponse okHttpResponse;

        try {
            okHttpResponse = sendHttpRequest(msg);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        validateResponse(okHttpResponse, alertResult);

        return alertResult;
    }

    private void validateResponse(OkHttpResponse okHttpResponse, AlertResult alertResult) {
        if (okHttpResponse.getStatusCode() != HttpStatus.SC_OK) {
            alertResult.setSuccess(false);
            alertResult
                    .setMessage(String.format("send http alert failed, response body: %s", okHttpResponse.getBody()));
        } else {
            alertResult.setSuccess(true);
            alertResult
                    .setMessage(String.format("send http alert success, response body: %s", okHttpResponse.getBody()));
        }
    }

    private OkHttpResponse sendHttpRequest(String msg) throws RuntimeException {
        switch (requestType) {
            case POST:
                setMsgInHeader(msg);
                setMsgInRequestBody(msg);
                return sendPostRequest();
            case GET:
                setMsgInUrl(msg);
                setMsgInHeader(msg);
                return sendGetRequest();
            case PUT:
                setMsgInHeader(msg);
                setMsgInRequestBody(msg);
                return sendPutRequest();
            default:
                throw new RuntimeException(String.format("http request method %s not supported",
                        requestType));
        }
    }

    @SneakyThrows
    private OkHttpResponse sendGetRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(headerParams);
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(contentType);
        Map<String, Object> requestParams = new HashMap<>();
        log.info("sending http alert get request, url: {}, header: {}, requestParams: {}, contentType: {}",
                url, headerParams, requestParams, contentType.getValue());
        return OkHttpUtils.get(url, okHttpRequestHeaders,
                requestParams, timeout, timeout, timeout);
    }

    @SneakyThrows
    private OkHttpResponse sendPostRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(headerParams);
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(contentType);
        Map<String, Object> requestBody = Collections.unmodifiableMap(bodyParams);
        log.info("sending http alert post request, url: {}, header: {}, requestBody: {}, contentType: {}",
                url, headerParams, requestBody, contentType.getValue());
        return OkHttpUtils.post(url, okHttpRequestHeaders, null,
                requestBody, timeout, timeout, timeout);
    }

    @SneakyThrows
    private OkHttpResponse sendPutRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(headerParams);
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(contentType);
        Map<String, Object> requestBody = Collections.unmodifiableMap(bodyParams);
        log.info("sending http alert put request, url: {}, header: {}, requestBody: {}, contentType: {}",
                url, headerParams, requestBody, contentType.getValue());
        return OkHttpUtils.put(url, okHttpRequestHeaders,
                requestBody, timeout, timeout, timeout);
    }

    /**
     * add msg param in url
     */
    private void setMsgInUrl(String msg) {
        if (url.contains(HttpAlertConstants.MSG_PARAMS)) {
            try {
                url = url.replace(HttpAlertConstants.MSG_PARAMS,
                        URLEncoder.encode(msg, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * set header params
     */
    private void setMsgInHeader(String msg) {
        if (msg == null) {
            return;
        }

        headerParams.forEach((key, value) -> {
            if (value.contains(HttpAlertConstants.MSG_PARAMS)) {
                headerParams.put(key, value.replace(HttpAlertConstants.MSG_PARAMS, msg));
            }
        });
    }

    /**
     * set body params
     */
    private void setMsgInRequestBody(String msg) {
        if (bodyParams == null) {
            return;
        }

        bodyParams.forEach((key, value) -> {
            if (value.contains(HttpAlertConstants.MSG_PARAMS)) {
                bodyParams.put(key, value.replace(HttpAlertConstants.MSG_PARAMS, msg));
            }
        });
    }

}
