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

package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpTask extends AbstractTask {

    private HttpParameters httpParameters;

    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public HttpTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        this.httpParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HttpParameters.class);
        log.info("Initialize http task params: {}", JSONUtils.toPrettyJsonString(httpParameters));

        if (httpParameters == null || !httpParameters.checkParameters()) {
            throw new RuntimeException("http task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {

        OkHttpResponse httpResponse = sendRequest();

        validateResponse(httpResponse.getBody(), httpResponse.getStatusCode());
    }

    @Override
    public void cancel() throws TaskException {
    }

    private void validateResponse(String body, int statusCode) {
        switch (httpParameters.getHttpCheckCondition()) {
            case BODY_CONTAINS:
                if (StringUtils.isEmpty(body) || !body.contains(httpParameters.getCondition())) {
                    log.error("http request failed, url: {}, statusCode: {}, checkCondition: {}, body: {}",
                            httpParameters.getUrl(), statusCode, HttpCheckCondition.BODY_CONTAINS.name(), body);
                    exitStatusCode = Constants.EXIT_CODE_FAILURE;
                    return;
                }
                break;
            case BODY_NOT_CONTAINS:
                if (StringUtils.isEmpty(body) || body.contains(httpParameters.getCondition())) {
                    log.error("http request failed, url: {}, statusCode: {}, checkCondition: {}, body: {}",
                            httpParameters.getUrl(), statusCode, HttpCheckCondition.BODY_NOT_CONTAINS.name(), body);
                    exitStatusCode = Constants.EXIT_CODE_FAILURE;
                    return;
                }
                break;
            case STATUS_CODE_CUSTOM:
                if (statusCode != Integer.parseInt(httpParameters.getCondition())) {
                    log.error("http request failed, url: {}, statusCode: {}, checkCondition: {}, body: {}",
                            httpParameters.getUrl(), statusCode, HttpCheckCondition.STATUS_CODE_CUSTOM.name(), body);
                    exitStatusCode = Constants.EXIT_CODE_FAILURE;
                    return;
                }
                break;
            case STATUS_CODE_DEFAULT:
                if (HttpConstants.RESPONSE_CODE_SUCCESS != statusCode) {
                    log.error("http request failed, url: {}, statusCode: {}, checkCondition: {}, body: {}",
                            httpParameters.getUrl(), statusCode, HttpCheckCondition.STATUS_CODE_DEFAULT.name(), body);
                    exitStatusCode = Constants.EXIT_CODE_FAILURE;
                    return;
                }
                break;
            default:
                throw new TaskException(String.format("http check condition %s not supported",
                        httpParameters.getHttpCheckCondition()));
        }

        // default success log
        log.info("http request success, url: {}, statusCode: {}, body: {}", httpParameters.getUrl(), statusCode, body);
        exitStatusCode = Constants.EXIT_CODE_SUCCESS;
    }

    private OkHttpResponse sendRequest() {
        switch (httpParameters.getHttpRequestMethod()) {
            case GET:
                return sendGetRequest();
            case POST:
                return sendPostRequest();
            case PUT:
                return sendPutRequest();
            case DELETE:
                return sendDeleteRequest();
            default:
                throw new TaskException(String.format("http request method %s not supported",
                        httpParameters.getHttpRequestMethod()));
        }
    }

    @SneakyThrows
    private OkHttpResponse sendGetRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(getHeaders());
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(getContentType());
        Map<String, Object> requestParams = getRequestParams();

        OkHttpResponse okHttpResponse = OkHttpUtils.get(httpParameters.getUrl(), okHttpRequestHeaders,
                requestParams, httpParameters.getConnectTimeout(),
                httpParameters.getConnectTimeout(), httpParameters.getConnectTimeout());
        addDefaultOutput(JSONUtils.toJsonString(okHttpResponse));
        return okHttpResponse;
    }

    @SneakyThrows
    private OkHttpResponse sendPostRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(getHeaders());
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(getContentType());
        Map<String, Object> requestBody = getRequestBody();

        OkHttpResponse okHttpResponse = OkHttpUtils.post(httpParameters.getUrl(), okHttpRequestHeaders, null,
                requestBody, httpParameters.getConnectTimeout(),
                httpParameters.getConnectTimeout(), httpParameters.getConnectTimeout());
        addDefaultOutput(JSONUtils.toJsonString(okHttpResponse));
        return okHttpResponse;
    }

    @SneakyThrows
    private OkHttpResponse sendPutRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(getHeaders());
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(getContentType());
        Map<String, Object> requestBody = getRequestBody();

        OkHttpResponse okHttpResponse = OkHttpUtils.put(httpParameters.getUrl(), okHttpRequestHeaders,
                requestBody, httpParameters.getConnectTimeout(),
                httpParameters.getConnectTimeout(), httpParameters.getConnectTimeout());
        addDefaultOutput(JSONUtils.toJsonString(okHttpResponse));
        return okHttpResponse;
    }

    @SneakyThrows
    private OkHttpResponse sendDeleteRequest() {
        OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
        okHttpRequestHeaders.setHeaders(getHeaders());
        okHttpRequestHeaders.setOkHttpRequestHeaderContentType(getContentType());

        OkHttpResponse okHttpResponse = OkHttpUtils.delete(httpParameters.getUrl(), okHttpRequestHeaders,
                httpParameters.getConnectTimeout(), httpParameters.getConnectTimeout(),
                httpParameters.getConnectTimeout());
        addDefaultOutput(JSONUtils.toJsonString(okHttpResponse));
        return okHttpResponse;
    }

    private Map<String, String> getHeaders() {
        if (httpParameters.getHttpRequestParams() == null) {
            return null;
        }

        return httpParameters.getHttpRequestParams().stream()
                .filter(httpProperty -> httpProperty.getHttpParametersType() != null)
                .filter(httpProperty -> httpProperty.getHttpParametersType().equals(HttpParametersType.HEADERS)
                        && !httpProperty.getProp().equalsIgnoreCase(HttpConstants.CONTENT_TYPE))
                .peek((httpProperty) -> {
                    httpProperty.setProp(ParameterUtils.convertParameterPlaceholders(httpProperty.getProp(),
                            ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap())));
                    httpProperty.setValue(ParameterUtils.convertParameterPlaceholders(httpProperty.getValue(),
                            ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap())));
                })
                .collect(Collectors.toMap(HttpProperty::getProp, HttpProperty::getValue));
    }

    private OkHttpRequestHeaderContentType getContentType() {
        if (httpParameters.getHttpRequestParams() == null) {
            return OkHttpRequestHeaderContentType.APPLICATION_JSON;
        }

        return OkHttpRequestHeaderContentType.fromValue(
                httpParameters.getHttpRequestParams().stream()
                        .filter(httpProperty -> httpProperty.getHttpParametersType().equals(HttpParametersType.HEADERS)
                                && httpProperty.getProp().equalsIgnoreCase(HttpConstants.CONTENT_TYPE))
                        .filter(httpProperty -> OkHttpRequestHeaderContentType
                                .fromValue(httpProperty.getValue()) != null)
                        .findFirst()
                        .orElse(HttpProperty.builder().value(OkHttpRequestHeaderContentType.APPLICATION_JSON.getValue())
                                .build())
                        .getValue());
    }

    private Map<String, Object> getRequestParams() {
        if (httpParameters.getHttpRequestParams() == null) {
            return null;
        }

        return httpParameters.getHttpRequestParams().stream()
                .filter(httpProperty -> httpProperty.getHttpParametersType().equals(HttpParametersType.PARAMETER))
                .peek((httpProperty) -> {
                    httpProperty.setProp(ParameterUtils.convertParameterPlaceholders(httpProperty.getProp(),
                            ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap())));
                    httpProperty.setValue(ParameterUtils.convertParameterPlaceholders(httpProperty.getValue(),
                            ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap())));
                })
                .collect(Collectors.toMap(HttpProperty::getProp, HttpProperty::getValue));
    }

    private Map<String, Object> getRequestBody() {
        String convertedParams = ParameterUtils.convertParameterPlaceholders(httpParameters.getHttpRequestBody(),
                ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
        Map<String, String> requestBody = JSONUtils.toMap(convertedParams);
        if (requestBody == null) {
            return null;
        }

        return requestBody.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public AbstractParameters getParameters() {
        return this.httpParameters;
    }

    public void addDefaultOutput(String response) {
        // put response in output
        Property outputProperty = new Property();
        outputProperty.setProp(String.format("%s.%s", taskExecutionContext.getTaskName(), "response"));
        outputProperty.setDirect(Direct.OUT);
        outputProperty.setType(DataType.VARCHAR);
        outputProperty.setValue(response);
        httpParameters.addPropertyToValPool(outputProperty);
    }
}
