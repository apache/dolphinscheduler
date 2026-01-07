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

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.AuthenticationUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.InterfaceInfo;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingFailureConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingInterfaceInfo;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.PollingSuccessConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.RequestParameter;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ResponseParameter;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ThirdPartySystemConnectorConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;

import com.jayway.jsonpath.JsonPath;

@Slf4j
public class ExternalSystemTask extends AbstractTask {

    private Boolean traceEnabled = true;

    private ExternalSystemParameters externalSystemParameters;
    private ThirdPartySystemConnectorConnectionParam baseExternalSystemParams;
    private TaskExecutionContext taskExecutionContext;
    private String accessToken;
    private Map<String, String> parameterMap = new HashMap<>();
    private Set<String> successStatusCache = new HashSet<>();
    private Set<String> failureStatusCache = new HashSet<>();

    private boolean isTimeout = false;
    private long taskStartTime;

    public ExternalSystemTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
        this.externalSystemParameters =
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ExternalSystemParameters.class);
        baseExternalSystemParams =
                externalSystemParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
        try {
            accessToken = baseExternalSystemParams.getAuthConfig().getHeaderPrefix() + " "
                    + AuthenticationUtils.authenticateAndGetToken(baseExternalSystemParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        externalSystemParameters = JSONUtils.parseObject(
                taskExecutionContext.getTaskParams(),
                ExternalSystemParameters.class);

        if (externalSystemParameters == null || !externalSystemParameters.checkParameters()) {
            throw new RuntimeException("external system task params is not valid");
        }

        log.info("Initialize external system task with externalSystemId: {}, externalTaskId: {}, externalTaskName: {}",
                externalSystemParameters.getDatasource(),
                externalSystemParameters.getExternalTaskId(),
                externalSystemParameters.getExternalTaskName());

        // Initialize parameter mapping
        initParameterMap();
        initStatusCache();
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            taskStartTime = System.currentTimeMillis();
            submitExternalTask();
            TimeUnit.SECONDS.sleep(10);
            trackExternalTaskStatus();
        } catch (Exception e) {
            log.error("external system task error", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Execute external system task failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        try {
            log.info("cancel external system task");
            cancelTaskInstance();
        } catch (Exception e) {
            throw new TaskException("cancel external system task error", e);
        } finally {
            // Only set to failure status when timeout failure strategy is enabled and it actually timed out
            log.info("External task timeout check isTimeoutFailureEnabled:{}", isTimeoutFailureEnabled());
            if (isTimeoutFailureEnabled()) {
                long currentTime = System.currentTimeMillis();
                long usedTimeMillis = currentTime - taskStartTime;
                long usedTime = (usedTimeMillis + 59999) / 60000; // Round up to minutes (ceiling division)
                log.info(
                        "External task timeout check, used time: {}m, timeout: {}m, currentTime: {}, taskStartTime: {}",
                        usedTime, taskExecutionContext.getTaskTimeout() / 60, currentTime, taskStartTime);
                if (usedTime >= taskExecutionContext.getTaskTimeout() / 60) {
                    isTimeout = true;
                    log.warn("External task timeout, used time: {}m, timeout: {}m",
                            usedTime, taskExecutionContext.getTaskTimeout() / 60);
                }
            }
            if (isTimeout) {
                setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
                log.info("External task cancelled due to timeout, set status to FAILED");
            } else {
                setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
                log.info("External task cancelled manually or timeout not enabled, set status to KILLED");
            }
        }
    }

    private void submitExternalTask() throws TaskException {
        try {
            InterfaceInfo submitConfig = baseExternalSystemParams.getSubmitInterface();
            String url = replaceParameterPlaceholders(baseExternalSystemParams.getCompleteUrl(submitConfig.getUrl()));
            Map<String, String> headers = new HashMap<>();
            buildAuthHeader(accessToken, headers);
            buildHeaders(submitConfig, headers);
            Map<String, Object> requestBody = buildRequestBody(submitConfig);
            Map<String, Object> requestParams = buildRequestParams(submitConfig);

            int interfaceTimeout = baseExternalSystemParams.getInterfaceTimeout();
            log.info("Using interface timeout value: {} milliseconds", interfaceTimeout);
            OkHttpResponse response =
                    executeRequestWithoutRetry(submitConfig.getMethod(), url, headers, requestParams, requestBody,
                            interfaceTimeout, interfaceTimeout, interfaceTimeout);
            log.info("Submit task response:{}", response);
            if (response.getStatusCode() != 200) {
                throw new TaskException("Submit task failed: " + response.getBody());
            }

            parseSubmitResponse(submitConfig.getResponseParameters(), response.getBody());
        } catch (Exception e) {
            log.error("Submit task failed:{}", e);
            throw new TaskException("Submit task failed", e);
        }
    }

    private void trackExternalTaskStatus() throws TaskException {
        try {
            String status;
            do {
                // Only check timeout when timeout failure strategy is enabled
                if (isTimeoutFailureEnabled()) {
                    long currentTime = System.currentTimeMillis();
                    long usedTimeMillis = currentTime - taskStartTime;
                    long usedTime = (usedTimeMillis + 59999) / 60000; // Round up to minutes (ceiling division)
                    if (usedTime >= taskExecutionContext.getTaskTimeout()) {
                        isTimeout = true;
                        log.error("External task timeout, used time: {}m, timeout: {}m",
                                usedTime, taskExecutionContext.getTaskTimeout() / 60);
                        setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
                        cancelTaskInstance();
                        return;
                    }
                }

                status = pollTaskStatus();

                if (successStatusCache.contains(status)) {
                    setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
                    log.info("External task completed successfully with status: {}", status);
                    return;
                } else if (failureStatusCache.contains(status)) {
                    setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
                    log.error("External task failed with status: {}", status);
                    return;
                }

                TimeUnit.SECONDS.sleep(10);
            } while (traceEnabled);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("Task status tracking interrupted", e);
        } catch (Exception e) {
            log.error("Track task status failed", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Track task status failed", e);
        }
    }

    private String pollTaskStatus() throws TaskException {
        try {
            PollingInterfaceInfo pollConfig =
                    baseExternalSystemParams.getPollStatusInterface();
            String url = replaceParameterPlaceholders(baseExternalSystemParams.getCompleteUrl(pollConfig.getUrl()));
            Map<String, String> headers = new HashMap<>();
            buildAuthHeader(accessToken, headers);
            buildHeaders(pollConfig, headers);
            Map<String, Object> requestBody = buildRequestBody(pollConfig);
            Map<String, Object> requestParams = buildRequestParams(pollConfig);

            int interfaceTimeout = baseExternalSystemParams.getInterfaceTimeout();
            OkHttpResponse response =
                    executeRequestWithRetry(pollConfig.getMethod(), url, headers, requestParams, requestBody,
                            interfaceTimeout, interfaceTimeout, interfaceTimeout, 3);
            log.info("poll task status response:{}", response);

            if (response.getStatusCode() != 200) {
                throw new TaskException("polling task failed: " + response.getBody());
            }

            String successField = pollConfig.getPollingSuccessConfig().getSuccessField();
            String failureField = pollConfig.getPollingFailureConfig().getFailureField();

            Object successStatusObj = JsonPath.read(response.getBody(), successField);
            Object failureStatusObj = JsonPath.read(response.getBody(), failureField);

            log.info("PollTaskStatus successfully, external task instance success status: {}, failure status: {}",
                    successStatusObj.toString(), failureStatusObj.toString());

            if (successStatusObj != null) {
                return successStatusObj.toString();
            } else if (failureStatusObj != null) {
                return failureStatusObj.toString();
            } else {
                log.warn("successStatusObj and failureStatusObj are null");
                return "UNKNOWN";
            }
        } catch (Exception e) {
            log.error("Poll task status failed", e);
            throw new TaskException("Poll task status failed", e);
        }
    }

    private void cancelTaskInstance() throws TaskException {
        try {
            traceEnabled = false;
            InterfaceInfo stopConfig = baseExternalSystemParams.getStopInterface();
            log.info("start cancel External System TaskInstance");
            String url = replaceParameterPlaceholders(baseExternalSystemParams.getCompleteUrl(stopConfig.getUrl()));
            Map<String, String> headers = new HashMap<>();
            buildAuthHeader(accessToken, headers);
            buildHeaders(stopConfig, headers);
            Map<String, Object> requestBody = buildRequestBody(stopConfig);
            Map<String, Object> requestParams = buildRequestParams(stopConfig);

            int interfaceTimeout = baseExternalSystemParams.getInterfaceTimeout();
            OkHttpResponse response =
                    executeRequestWithRetry(stopConfig.getMethod(), url, headers, requestParams, requestBody,
                            interfaceTimeout, interfaceTimeout, interfaceTimeout, 3);
            log.info("cancel task response:{}", response);

            if (response.getStatusCode() != 200) {
                throw new TaskException("Cancel task failed: " + response.getBody());
            }
            log.info("Cancel task result: {}", response.getBody());
        } catch (Exception e) {
            log.error("Cancel task failed", e);
            throw new TaskException("Cancel task failed", e);
        }
    }

    private OkHttpResponse executeRequestWithoutRetry(InterfaceInfo.HttpMethod method, String url,
                                                      Map<String, String> headers, Map<String, Object> requestParams,
                                                      Map<String, Object> requestBody, int connectTimeout,
                                                      int readTimeout,
                                                      int writeTimeout) throws TaskException {
        return executeRequestWithRetry(method, url, headers, requestParams, requestBody, connectTimeout, readTimeout,
                writeTimeout, 0);
    }

    private OkHttpResponse executeRequestWithRetry(InterfaceInfo.HttpMethod method, String url,
                                                   Map<String, String> headers, Map<String, Object> requestParams,
                                                   Map<String, Object> requestBody, int connectTimeout, int readTimeout,
                                                   int writeTimeout, int maxRetries) throws TaskException {
        int retryCount = 0;
        while (retryCount <= maxRetries) {
            OkHttpRequestHeaders okHttpRequestHeaders = new OkHttpRequestHeaders();
            okHttpRequestHeaders.setHeaders(headers);
            OkHttpRequestHeaderContentType contentType = getContentType(headers);
            okHttpRequestHeaders.setOkHttpRequestHeaderContentType(getContentType(headers));
            try {
                switch (method) {
                    case POST:
                        if (contentType.equals(OkHttpRequestHeaderContentType.APPLICATION_JSON)) {
                            return OkHttpUtils.post(url, okHttpRequestHeaders, requestParams, requestBody,
                                    connectTimeout,
                                    readTimeout, writeTimeout);
                        }
                        if (contentType.equals(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED)) {
                            FormBody.Builder formBodyBuilder = new FormBody.Builder();
                            if (requestBody != null) {
                                for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                                    formBodyBuilder.add(entry.getKey(), entry.getValue().toString());
                                }
                            }
                            return OkHttpUtils.postFormBody(url, okHttpRequestHeaders, requestParams,
                                    formBodyBuilder.build(), connectTimeout,
                                    readTimeout, writeTimeout);
                        }
                    case PUT:
                        return OkHttpUtils.put(url, okHttpRequestHeaders, requestBody, connectTimeout, readTimeout,
                                writeTimeout);
                    case GET:
                        return OkHttpUtils.get(url, okHttpRequestHeaders, requestParams, connectTimeout, readTimeout,
                                writeTimeout);
                    default:
                        throw new TaskException("Unsupported HTTP method: " + method);
                }
            } catch (Exception e) {
                retryCount++;
                if (maxRetries > 0) {
                    log.warn("Request failed, retrying... (attempt {}/{})", retryCount, maxRetries, e);
                    if (retryCount > maxRetries) {
                        throw new TaskException("Request failed after " + maxRetries + " retries", e);
                    }
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new TaskException("Request retry interrupted", ie);
                    }
                } else {
                    throw new TaskException("Request failed without retry", e);
                }
            }
        }
        return null; // This line should never be reached
    }

    private void buildAuthHeader(String accessToken, Map<String, String> headers) {
        headers.put("Authorization", accessToken);
    }

    private Map<String, String> buildHeaders(InterfaceInfo config,
                                             Map<String, String> requestParams) {
        for (RequestParameter param : config.getParameters()) {
            if (param.getLocation().equals(RequestParameter.ParamLocation.HEADER)) {
                requestParams.put(param.getParamName(), replaceParameterPlaceholders(param.getParamValue()));
            }
        }
        return requestParams;
    }

    private Map<String, Object> buildRequestBody(InterfaceInfo config) {
        Map<String, Object> requestBody = new HashMap<>();
        if (config.getBody() != null) {
            requestBody = JSONUtils.parseObject(replaceParameterPlaceholders(config.getBody()), Map.class);
        }
        return requestBody;
    }

    private Map<String, Object> buildRequestParams(InterfaceInfo config) {
        Map<String, Object> requestParams = new HashMap<>();
        for (RequestParameter param : config.getParameters()) {
            if (param.getLocation().equals(RequestParameter.ParamLocation.PARAM)) {
                requestParams.put(param.getParamName(), replaceParameterPlaceholders(param.getParamValue()));
            }
        }
        return requestParams;
    }

    private String replaceParameterPlaceholders(String template) {
        if (StringUtils.isEmpty(template)) {
            return template;
        }

        StringBuilder result = new StringBuilder(template);
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            int index;
            while ((index = result.indexOf(placeholder)) != -1) {
                result.replace(index, index + placeholder.length(), entry.getValue());
            }
        }
        String resultString = ParameterUtils.convertParameterPlaceholders(result.toString(), parameterMap);
        log.info("after replaceParameterPlaceholders:{}", resultString);
        return resultString;
    }

    private void parseSubmitResponse(List<ResponseParameter> responseParameters,
                                     String responseBody) throws TaskException {
        try {
            for (ResponseParameter param : responseParameters) {
                String jsonPath = param.getJsonPath();
                String key = param.getKey();
                Object value = JsonPath.read(responseBody, jsonPath);

                if (value == null) {
                    log.warn("Response parameter {} not found in response body", key);
                    continue;
                }

                parameterMap.put(key, value.toString().replace("\"", ""));
                log.info("Parsed parameter {}: {}", key, value.toString());

            }
        } catch (Exception e) {
            log.error("Parse submit response failed", e);
            throw new TaskException("Parse submit response failed", e);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return this.externalSystemParameters;
    }

    /**
     * Initialize parameter mapping
     */
    private void initParameterMap() {
        Map<String, Property> prepareParamsMap = taskExecutionContext.getPrepareParamsMap();
        if (prepareParamsMap != null) {
            for (Map.Entry<String, Property> entry : prepareParamsMap.entrySet()) {
                parameterMap.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        if (externalSystemParameters.getExternalTaskId() != null) {
            parameterMap.put(ExternalTaskConstants.EXTERNAL_TASK_ID, externalSystemParameters.getExternalTaskId());
            parameterMap.put(ExternalTaskConstants.EXTERNAL_TASK_NAME, externalSystemParameters.getExternalTaskName());
        }
    }

    private void initStatusCache() {
        PollingSuccessConfig successConfig =
                baseExternalSystemParams.getPollStatusInterface().getPollingSuccessConfig();

        if (successConfig != null && successConfig.getSuccessValue() != null) {
            try {
                String successValueString = successConfig.getSuccessValue();
                String[] successValues = successValueString.split(",");
                for (String successValue : successValues) {
                    successStatusCache.add(successValue);
                }
                log.info("trackExternalTaskStatus successValues is :{}", successStatusCache);

            } catch (NullPointerException e) {
                log.error("Error: successValue is null");
            }
        }
        PollingFailureConfig failureConfig =
                baseExternalSystemParams.getPollStatusInterface().getPollingFailureConfig();
        if (failureConfig != null && failureConfig.getFailureField() != null) {
            try {
                String failureValueString = failureConfig.getFailureValue();
                String[] failureValues = failureValueString.split(",");
                for (String failureValue : failureValues) {
                    failureStatusCache.add(failureValue);
                }
                log.info("trackExternalTaskStatus failureValues is :{}", failureStatusCache);
            } catch (NullPointerException e) {
                log.error("Error: failureStatus is null");
            }
        }
    }

    /**
     * Check if timeout failure strategy is enabled
     */
    private boolean isTimeoutFailureEnabled() {
        return taskExecutionContext.getTaskTimeoutStrategy() != null
                && taskExecutionContext.getTaskTimeout() > 0
                && taskExecutionContext.getTaskTimeout() < Integer.MAX_VALUE
                && (taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                        || taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED);
    }

    private OkHttpRequestHeaderContentType getContentType(Map<String, String> headers) {
        if (headers == null || (!headers.containsKey(ExternalTaskConstants.CONTENT_TYPE)
                && !headers.containsKey(ExternalTaskConstants.CONTENT_TYPE_LOWERCASE))) {
            return OkHttpRequestHeaderContentType.APPLICATION_JSON;
        }
        String contentType = headers.get(ExternalTaskConstants.CONTENT_TYPE);
        return OkHttpRequestHeaderContentType.fromValue(contentType) != null
                ? OkHttpRequestHeaderContentType.fromValue(contentType)
                : OkHttpRequestHeaderContentType.APPLICATION_JSON;
    }

}
