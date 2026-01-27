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

package org.apache.dolphinscheduler.plugin.task.externalSystem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.AuthenticationUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.AuthConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.InterfaceInfo;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingFailureConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingInterfaceInfo;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingSuccessConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ResponseParameter;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ThirdPartySystemConnectorConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExternalSystemTaskTest {

    private TaskExecutionContext taskExecutionContext;
    private ExternalSystemParameters externalSystemParameters;
    private ThirdPartySystemConnectorConnectionParam thirdPartyParams;
    private ExternalSystemTask externalSystemTask;

    @BeforeEach
    public void setUp() throws Exception {
        taskExecutionContext = mock(TaskExecutionContext.class);
        externalSystemParameters = new ExternalSystemParameters();
        externalSystemParameters.setDatasource(1);
        externalSystemParameters.setExternalTaskId("task-123");
        externalSystemParameters.setExternalTaskName("Test Task");

        thirdPartyParams = new ThirdPartySystemConnectorConnectionParam();
        AuthConfig authConfig = new AuthConfig();
        authConfig.setHeaderPrefix("Bearer");
        thirdPartyParams.setAuthConfig(authConfig);

        InterfaceInfo submitInterface = new InterfaceInfo();
        submitInterface.setUrl("http://test.com/api/tasks");
        submitInterface.setMethod(InterfaceInfo.HttpMethod.POST);
        submitInterface.setBody("{\"taskName\":\"${externalTaskName}\"}");
        submitInterface.setParameters(Collections.emptyList());
        submitInterface.setResponseParameters(Arrays.asList(
                createResponseParameter("taskId", "$.taskId"),
                createResponseParameter("status", "$.status")));
        thirdPartyParams.setSubmitInterface(submitInterface);

        PollingInterfaceInfo pollInterface = new PollingInterfaceInfo();
        pollInterface.setUrl("http://test.com/api/tasks/${taskId}/status");
        pollInterface.setMethod(InterfaceInfo.HttpMethod.GET);
        pollInterface.setParameters(Collections.emptyList());
        pollInterface.setResponseParameters(Collections.emptyList());

        PollingSuccessConfig successConfig = new PollingSuccessConfig();
        successConfig.setSuccessField("$.status");
        successConfig.setSuccessValue("COMPLETED,SUCCESS");
        pollInterface.setPollingSuccessConfig(successConfig);

        PollingFailureConfig failureConfig = new PollingFailureConfig();
        failureConfig.setFailureField("$.status");
        failureConfig.setFailureValue("FAILED,ERROR");
        pollInterface.setPollingFailureConfig(failureConfig);

        thirdPartyParams.setPollStatusInterface(pollInterface);

        InterfaceInfo stopInterface = new InterfaceInfo();
        stopInterface.setUrl("http://test.com/api/tasks/${taskId}/stop");
        stopInterface.setMethod(InterfaceInfo.HttpMethod.POST);
        stopInterface.setParameters(Collections.emptyList());
        thirdPartyParams.setStopInterface(stopInterface);

        String paramsJson = JSONUtils.toJsonString(externalSystemParameters);
        Mockito.lenient().when(taskExecutionContext.getTaskParams()).thenReturn(paramsJson);
        Mockito.lenient().when(taskExecutionContext.getTaskTimeout()).thenReturn(300000);
        Mockito.lenient().when(taskExecutionContext.getTaskTimeoutStrategy()).thenReturn(TaskTimeoutStrategy.FAILED);

        org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper resourceHelper =
                mock(org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper.class);

        org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters dataSourceParams =
                mock(org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters.class);
        Mockito.lenient().when(dataSourceParams.getConnectionParams())
                .thenReturn(JSONUtils.toJsonString(thirdPartyParams));

        Mockito.lenient().when(resourceHelper.getResourceParameters(
                org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType.DATASOURCE,
                externalSystemParameters.getDatasource())).thenReturn(dataSourceParams);

        Mockito.lenient().when(taskExecutionContext.getResourceParametersHelper()).thenReturn(resourceHelper);

        try (MockedStatic<AuthenticationUtils> mockedAuth = Mockito.mockStatic(AuthenticationUtils.class)) {
            mockedAuth
                    .when(() -> AuthenticationUtils
                            .authenticateAndGetToken(any(ThirdPartySystemConnectorConnectionParam.class)))
                    .thenReturn("mocked-token");

            externalSystemTask = new ExternalSystemTask(taskExecutionContext);

            java.lang.reflect.Field parametersField =
                    ExternalSystemTask.class.getDeclaredField("externalSystemParameters");
            parametersField.setAccessible(true);
            parametersField.set(externalSystemTask, externalSystemParameters);

            java.lang.reflect.Field baseParamsField =
                    ExternalSystemTask.class.getDeclaredField("baseExternalSystemParams");
            baseParamsField.setAccessible(true);
            baseParamsField.set(externalSystemTask, thirdPartyParams);

            externalSystemTask.init();
        }
    }

    private ResponseParameter createResponseParameter(String key, String jsonPath) {
        ResponseParameter param = new ResponseParameter();
        param.setKey(key);
        param.setJsonPath(jsonPath);
        return param;
    }

    @Test
    public void testInit() {
        try (MockedStatic<JSONUtils> mockedJsonUtils = Mockito.mockStatic(JSONUtils.class)) {
            mockedJsonUtils.when(() -> JSONUtils.parseObject(any(String.class), eq(ExternalSystemParameters.class)))
                    .thenReturn(externalSystemParameters);

            externalSystemTask.init();

            Assertions.assertNotNull(externalSystemTask.getParameters());
        }
    }

    @Test
    public void testSubmitExternalTask() throws Exception {
        try (MockedStatic<OkHttpUtils> mockedOkHttp = Mockito.mockStatic(OkHttpUtils.class)) {
            OkHttpResponse submitResponse = mock(OkHttpResponse.class);
            when(submitResponse.getStatusCode()).thenReturn(200);
            when(submitResponse.getBody())
                    .thenReturn("{\"taskId\": \"12345\", \"status\": \"SUBMITTED\", \"name\": \"Test Task\"}");

            OkHttpResponse runningResponse = mock(OkHttpResponse.class);
            when(runningResponse.getStatusCode()).thenReturn(200);
            when(runningResponse.getBody()).thenReturn("{\"status\": \"RUNNING\"}");

            OkHttpResponse completedResponse = mock(OkHttpResponse.class);
            when(completedResponse.getStatusCode()).thenReturn(200);
            when(completedResponse.getBody()).thenReturn("{\"status\": \"COMPLETED\"}");

            mockedOkHttp.when(() -> OkHttpUtils.post(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), any(Map.class), anyInt(), anyInt(), anyInt()))
                    .thenReturn(submitResponse);

            mockedOkHttp.when(() -> OkHttpUtils.get(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), anyInt(), anyInt(), anyInt()))
                    .thenReturn(runningResponse)
                    .thenReturn(runningResponse)
                    .thenReturn(completedResponse);

            TaskCallBack taskCallBack = mock(TaskCallBack.class);

            Assertions.assertDoesNotThrow(() -> {
                externalSystemTask.handle(taskCallBack);
            });

            mockedOkHttp.verify(() -> OkHttpUtils.get(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), anyInt(), anyInt(), anyInt()), times(3));
        }
    }

    @Test
    public void testSubmitExternalTaskWithSuccessStatus() throws Exception {
        try (MockedStatic<OkHttpUtils> mockedOkHttp = Mockito.mockStatic(OkHttpUtils.class)) {
            OkHttpResponse submitResponse = mock(OkHttpResponse.class);
            when(submitResponse.getStatusCode()).thenReturn(200);
            when(submitResponse.getBody()).thenReturn(
                    "{\"taskId\": \"12345\", \"taskInstanceId\": \"instance-123\", \"status\": \"SUBMITTED\", \"name\": \"Test Task\"}");

            OkHttpResponse runningResponse1 = mock(OkHttpResponse.class);
            when(runningResponse1.getStatusCode()).thenReturn(200);
            when(runningResponse1.getBody()).thenReturn("{\"status\": \"RUNNING\"}");

            OkHttpResponse runningResponse2 = mock(OkHttpResponse.class);
            when(runningResponse2.getStatusCode()).thenReturn(200);
            when(runningResponse2.getBody()).thenReturn("{\"status\": \"RUNNING\"}");

            OkHttpResponse successResponse = mock(OkHttpResponse.class);
            when(successResponse.getStatusCode()).thenReturn(200);
            when(successResponse.getBody()).thenReturn("{\"status\": \"SUCCESS\"}");

            mockedOkHttp.when(() -> OkHttpUtils.post(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), any(Map.class), anyInt(), anyInt(), anyInt()))
                    .thenReturn(submitResponse);

            mockedOkHttp.when(() -> OkHttpUtils.get(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), anyInt(), anyInt(), anyInt()))
                    .thenReturn(runningResponse1)
                    .thenReturn(runningResponse2)
                    .thenReturn(successResponse);

            TaskCallBack taskCallBack = mock(TaskCallBack.class);

            Assertions.assertDoesNotThrow(() -> {
                externalSystemTask.handle(taskCallBack);
            });

            mockedOkHttp.verify(() -> OkHttpUtils.get(any(String.class), any(OkHttpRequestHeaders.class),
                    any(Map.class), anyInt(), anyInt(), anyInt()), times(3));

            Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, externalSystemTask.getExitStatusCode());
        }
    }

    @Test
    public void testTimeoutCheckEnabled() {
        try {
            java.lang.reflect.Method method = ExternalSystemTask.class.getDeclaredMethod("isTimeoutFailureEnabled");
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(externalSystemTask);
            Assertions.assertTrue(result);
        } catch (Exception e) {
            Assertions.fail("Failed to invoke isTimeoutFailureEnabled method: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceParameterPlaceholders() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("externalTaskId", "task-123");
        parameterMap.put("externalTaskName", "Test Task");

        try {
            java.lang.reflect.Field field = ExternalSystemTask.class.getDeclaredField("parameterMap");
            field.setAccessible(true);
            field.set(externalSystemTask, parameterMap);
        } catch (Exception e) {
            Assertions.fail("Failed to set parameterMap field");
        }

        String template = "http://test.com/api/tasks/${externalTaskId}/${externalTaskName}";
        try {
            java.lang.reflect.Method method =
                    ExternalSystemTask.class.getDeclaredMethod("replaceParameterPlaceholders", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(externalSystemTask, template);

            Assertions.assertEquals("http://test.com/api/tasks/task-123/Test Task", result);
        } catch (Exception e) {
            Assertions.fail("Failed to invoke replaceParameterPlaceholders method: " + e.getMessage());
        }
    }
}
