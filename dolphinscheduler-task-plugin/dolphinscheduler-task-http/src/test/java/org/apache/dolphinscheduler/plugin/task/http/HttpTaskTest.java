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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test HttpTask
 */
@ExtendWith(MockitoExtension.class)
public class HttpTaskTest {

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String APPLICATION_JSON_VALUE = "application/json";

    private static final String MOCK_DISPATCH_PATH_REQ_BODY_TO_RES_BODY = "/requestBody/to/responseBody";

    private static final String MOCK_DISPATCH_PATH_REQ_PARAMS_TO_RES_BODY = "/requestParams/to/responseBody";

    private final List<MockWebServer> mockWebServers = new ArrayList<>();

    @AfterEach
    public void after() {
        mockWebServers.forEach(IOUtils::closeQuietly);
        mockWebServers.clear();
    }

    @Test
    public void testHandleCheckCodeDefaultSuccess() throws Exception {
        HttpTask getHttpTask = generateHttpTask(HttpMethod.GET, HttpStatus.SC_OK);
        HttpTask postHttpTask = generateHttpTask(HttpMethod.POST, HttpStatus.SC_OK);
        HttpTask headHttpTask = generateHttpTask(HttpMethod.HEAD, HttpStatus.SC_OK);
        HttpTask putHttpTask = generateHttpTask(HttpMethod.PUT, HttpStatus.SC_OK);
        HttpTask deleteHttpTask = generateHttpTask(HttpMethod.DELETE, HttpStatus.SC_OK);
        getHttpTask.handle(null);
        postHttpTask.handle(null);
        headHttpTask.handle(null);
        putHttpTask.handle(null);
        deleteHttpTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, getHttpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_SUCCESS, postHttpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_SUCCESS, headHttpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_SUCCESS, putHttpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_SUCCESS, deleteHttpTask.getExitStatusCode());
    }

    @Test
    public void testHandleCheckCodeDefaultError() throws Exception {
        HttpTask getHttpTask = generateHttpTask(HttpMethod.GET, HttpStatus.SC_BAD_REQUEST);
        getHttpTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_FAILURE, getHttpTask.getExitStatusCode());
    }

    @Test
    public void testHandleCheckCodeCustom() throws Exception {
        String condition = HttpStatus.SC_CREATED + "";
        HttpTask httpTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.STATUS_CODE_CUSTOM,
                condition, HttpStatus.SC_CREATED, "");
        HttpTask httpErrorTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.STATUS_CODE_CUSTOM,
                condition, HttpStatus.SC_OK, "");
        httpTask.handle(null);
        httpErrorTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, httpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_FAILURE, httpErrorTask.getExitStatusCode());
    }

    @Test
    public void testHandleCheckBodyContains() throws Exception {
        HttpTask httpTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.BODY_CONTAINS,
                "success", HttpStatus.SC_OK, "{\"status\": \"success\"}");
        HttpTask httpErrorTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.BODY_CONTAINS,
                "success", HttpStatus.SC_OK, "{\"status\": \"failed\"}");
        httpTask.handle(null);
        httpErrorTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, httpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_FAILURE, httpErrorTask.getExitStatusCode());
    }

    @Test
    public void testHandleCheckBodyNotContains() throws Exception {
        HttpTask httpTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.BODY_NOT_CONTAINS,
                "failed", HttpStatus.SC_OK, "{\"status\": \"success\"}");
        HttpTask httpErrorTask = generateHttpTask(HttpMethod.GET, HttpCheckCondition.BODY_NOT_CONTAINS,
                "failed", HttpStatus.SC_OK, "{\"status\": \"failed\"}");
        httpTask.handle(null);
        httpErrorTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, httpTask.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_FAILURE, httpErrorTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithHttpBodyParams() throws Exception {
        List<HttpProperty> httpParams = new ArrayList<>();
        HttpProperty property = new HttpProperty();
        property.setProp("day");
        property.setValue("${day}");
        property.setHttpParametersType(HttpParametersType.BODY);
        httpParams.add(property);

        Map<String, String> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("day", "20220812");
        // The MockWebServer will return the request body as response body directly
        // So we just need to check if the response body contains string "20220812"
        HttpTask httpTask = generateHttpTask(MOCK_DISPATCH_PATH_REQ_BODY_TO_RES_BODY, HttpMethod.POST,
                httpParams, prepareParamsMap, HttpCheckCondition.BODY_CONTAINS, "20220812",
                HttpStatus.SC_OK, "");
        httpTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, httpTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithHttpParameterParams() throws Exception {
        List<HttpProperty> httpParams = new ArrayList<>();
        HttpProperty property = new HttpProperty();
        property.setProp("day");
        property.setValue("${day}");
        property.setHttpParametersType(HttpParametersType.PARAMETER);
        httpParams.add(property);

        Map<String, String> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("day", "20220812");
        // The MockWebServer will return the request parameter as response body directly
        // So we just need to check if the response body contains string "20220812"
        HttpTask httpTask = generateHttpTask(MOCK_DISPATCH_PATH_REQ_PARAMS_TO_RES_BODY, HttpMethod.POST,
                httpParams, prepareParamsMap, HttpCheckCondition.BODY_CONTAINS, "20220812",
                HttpStatus.SC_OK, "");
        httpTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, httpTask.getExitStatusCode());
    }

    @Test
    public void testAddDefaultOutput() throws Exception {
        HttpTask httpTask = generateHttpTask(HttpMethod.GET, HttpStatus.SC_OK);
        AbstractParameters httpParameters = httpTask.getParameters();
        String response = "{\"status\": \"success\"}";
        httpTask.addDefaultOutput(response);

        List<Property> varPool = httpParameters.getVarPool();
        Assertions.assertEquals(1, varPool.size());
        Property property = varPool.get(0);
        Assertions.assertEquals("null.response", property.getProp());
        Assertions.assertEquals(Direct.OUT, property.getDirect());
        Assertions.assertEquals(DataType.VARCHAR, property.getType());
        Assertions.assertEquals(response, property.getValue());
    }

    private String withMockWebServer(String path, int actualResponseCode,
                                     String actualResponseBody) throws IOException {
        MockWebServer server = new MockWebServer();
        mockWebServers.add(server);
        server.start();
        server.setDispatcher(generateMockDispatcher(path, actualResponseCode, actualResponseBody));
        return server.url(path).toString();
    }

    private HttpTask generateHttpTask(HttpMethod httpMethod, int actualResponseCode) throws IOException {
        return generateHttpTask("/test", httpMethod, null, null,
                HttpCheckCondition.STATUS_CODE_DEFAULT, "", actualResponseCode, "");
    }

    private HttpTask generateHttpTask(HttpMethod httpMethod, HttpCheckCondition httpCheckConditionType,
                                      String condition, int actualResponseCode,
                                      String actualResponseBody) throws IOException {
        return generateHttpTask("/test", httpMethod, null, null,
                httpCheckConditionType, condition, actualResponseCode, actualResponseBody);
    }
    private HttpTask generateHttpTask(String mockPath, HttpMethod httpMethod, List<HttpProperty> httpParams,
                                      Map<String, String> prepareParamsMap, HttpCheckCondition httpCheckConditionType,
                                      String condition, int actualResponseCode,
                                      String actualResponseBody) throws IOException {
        String url = withMockWebServer(mockPath, actualResponseCode, actualResponseBody);
        String paramData = generateHttpParameters(url, httpMethod, httpParams, httpCheckConditionType, condition);
        return generateHttpTaskFromParamData(paramData, prepareParamsMap);
    }

    private HttpTask generateHttpTaskFromParamData(String paramData, Map<String, String> prepareParamsMap) {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(paramData);
        if (prepareParamsMap != null) {
            Map<String, Property> propertyParamsMap = new HashMap<>();
            prepareParamsMap.forEach((k, v) -> {
                Property property = new Property();
                property.setProp(k);
                property.setValue(v);
                propertyParamsMap.put(k, property);
            });
            Mockito.when(taskExecutionContext.getPrepareParamsMap()).thenReturn(propertyParamsMap);
        }
        HttpTask httpTask = new HttpTask(taskExecutionContext);
        httpTask.init();
        return httpTask;
    }

    private String generateHttpParameters(String url, HttpMethod httpMethod, List<HttpProperty> httpParams,
                                          HttpCheckCondition httpCheckConditionType,
                                          String condition) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HttpParameters httpParameters = new HttpParameters();
        httpParameters.setUrl(url);
        httpParameters.setHttpMethod(httpMethod);
        httpParameters.setHttpCheckCondition(httpCheckConditionType);
        httpParameters.setCondition(condition);
        httpParameters.setConnectTimeout(10000);
        httpParameters.setSocketTimeout(10000);
        httpParameters.setHttpParams(httpParams);
        return mapper.writeValueAsString(httpParameters);
    }

    private Dispatcher generateMockDispatcher(String path, int actualResponseCode, String actualResponseBody) {
        return new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) {
                MockResponse mockResponse = new MockResponse()
                        .setResponseCode(actualResponseCode)
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);

                if (request.getPath().startsWith(MOCK_DISPATCH_PATH_REQ_BODY_TO_RES_BODY)) {
                    // return request body as mock response body
                    mockResponse.setBody(request.getBody().readUtf8());
                } else if (request.getPath().startsWith(MOCK_DISPATCH_PATH_REQ_PARAMS_TO_RES_BODY)) {
                    // return request params as mock response body
                    ObjectMapper mapper = new ObjectMapper();
                    HttpUrl httpUrl = request.getRequestUrl();
                    Set<String> parameterNames = httpUrl.queryParameterNames();
                    Map<String, String> resBodyMap = new HashMap<>();
                    parameterNames.forEach(name -> resBodyMap.put(name, httpUrl.queryParameter(name)));
                    try {
                        mockResponse.setBody(mapper.writeValueAsString(resBodyMap));
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                } else if (request.getPath().startsWith(path)) {
                    mockResponse.setBody(actualResponseBody);
                } else {
                    mockResponse.setResponseCode(HttpStatus.SC_NOT_FOUND);
                }
                return mockResponse;
            }
        };
    }
}
