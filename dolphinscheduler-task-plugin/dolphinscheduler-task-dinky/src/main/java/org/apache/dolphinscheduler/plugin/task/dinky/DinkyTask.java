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

package org.apache.dolphinscheduler.plugin.task.dinky;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;

@Slf4j
public class DinkyTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;

    private DinkyParameters dinkyParameters;
    private String jobInstanceId;
    private boolean status;
    private String dinkyVersion;

    protected DinkyTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        this.dinkyParameters = JSONUtils.parseObject(taskParams, DinkyParameters.class);
        log.info("Initialize dinky task params: {}", JSONUtils.toPrettyJsonString(dinkyParameters));
        if (this.dinkyParameters == null || !this.dinkyParameters.checkParameters()) {
            throw new DinkyTaskException("dinky task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        // Get dinky version
        dinkyVersion = getDinkyVersion(this.dinkyParameters.getAddress());
        super.handle(taskCallBack);
    }

    @Override
    public void submitApplication() throws TaskException {
        if (dinkyVersion.startsWith("0")) {
            submitApplicationV0();
        } else {
            submitApplicationV1();
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        if (dinkyVersion.startsWith("0")) {
            trackApplicationStatusV0();
        } else {
            trackApplicationStatusV1();
        }
    }

    private void submitApplicationV0() {
        try {
            String address = this.dinkyParameters.getAddress();
            String taskId = this.dinkyParameters.getTaskId();
            boolean isOnline = this.dinkyParameters.isOnline();
            JsonNode result;
            String apiResultDatasKey = DinkyTaskConstants.API_RESULT_DATAS;
            if (isOnline) {
                // Online dinky-0.6.5 task, and only one job is allowed to execute
                result = onlineTaskV0(address, taskId);
            } else {
                // Submit dinky-0.6.5 task
                result = submitTaskV0(address, taskId);
            }
            if (checkResultV0(result)) {
                status = result.get(apiResultDatasKey).get(DinkyTaskConstants.API_RESULT_SUCCESS).asBoolean();
                if (result.get(apiResultDatasKey).has(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID)
                        && !(result.get(apiResultDatasKey)
                                .get(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID) instanceof NullNode)) {
                    jobInstanceId =
                            result.get(apiResultDatasKey).get(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID).asText();
                }
            }
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            log.error(DinkyTaskConstants.SUBMIT_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DinkyTaskConstants.SUBMIT_FAILED_MSG, ex);
        }
    }

    private void submitApplicationV1() {
        try {
            String address = this.dinkyParameters.getAddress();
            String taskId = this.dinkyParameters.getTaskId();
            boolean isOnline = this.dinkyParameters.isOnline();
            JsonNode result;
            String apiResultDataKey = DinkyTaskConstants.API_RESULT_DATA;
            // Submit dinky-1.0.0 task
            result = submitTaskV1(address, taskId, isOnline, generateVariables());
            if (checkResultV1(result)) {
                status = result.get(DinkyTaskConstants.API_RESULT_SUCCESS).asBoolean();
                if (result.get(apiResultDataKey).has(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID)
                        && !(result.get(apiResultDataKey)
                                .get(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID) instanceof NullNode)) {
                    jobInstanceId =
                            result.get(apiResultDataKey).get(DinkyTaskConstants.API_RESULT_JOB_INSTANCE_ID).asText();
                }
            } else {
                log.error(DinkyTaskConstants.SUBMIT_FAILED_MSG + "{}", result.get(DinkyTaskConstants.API_RESULT_MSG));
                setExitStatusCode(EXIT_CODE_FAILURE);
                throw new TaskException(
                        DinkyTaskConstants.SUBMIT_FAILED_MSG + result.get(DinkyTaskConstants.API_RESULT_MSG));
            }
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            log.error(DinkyTaskConstants.SUBMIT_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DinkyTaskConstants.SUBMIT_FAILED_MSG, ex);
        }
    }

    public void trackApplicationStatusV0() throws TaskException {
        try {
            String address = this.dinkyParameters.getAddress();
            String taskId = this.dinkyParameters.getTaskId();
            if (status && jobInstanceId == null) {
                // Use address-taskId as app id
                setAppIds(String.format(DinkyTaskConstants.APPIDS_FORMAT, address, taskId));
                setExitStatusCode(mapStatusToExitCode(true));
                log.info("Dinky common sql task finished.");
                return;
            }
            String apiResultDatasKey = DinkyTaskConstants.API_RESULT_DATAS;
            boolean finishFlag = false;
            while (!finishFlag) {
                JsonNode jobInstanceInfoResult = getJobInstanceInfo(address, jobInstanceId);
                if (!checkResultV0(jobInstanceInfoResult)) {
                    break;
                }
                String jobInstanceStatus =
                        jobInstanceInfoResult.get(apiResultDatasKey).get("status").asText();
                switch (jobInstanceStatus) {
                    case DinkyTaskConstants.STATUS_FINISHED:
                        final int exitStatusCode = mapStatusToExitCode(status);
                        // Use address-taskId as app id
                        setAppIds(String.format(DinkyTaskConstants.APPIDS_FORMAT, address, taskId));
                        setExitStatusCode(exitStatusCode);
                        log.info("dinky task finished with results: {}",
                                jobInstanceInfoResult.get(apiResultDatasKey));
                        finishFlag = true;
                        break;
                    case DinkyTaskConstants.STATUS_FAILED:
                    case DinkyTaskConstants.STATUS_CANCELED:
                    case DinkyTaskConstants.STATUS_UNKNOWN:
                        errorHandle(
                                jobInstanceInfoResult.get(apiResultDatasKey).get(DinkyTaskConstants.API_RESULT_ERROR)
                                        .asText());
                        finishFlag = true;
                        break;
                    default:
                        Thread.sleep(DinkyTaskConstants.SLEEP_MILLIS);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error(DinkyTaskConstants.TRACK_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DinkyTaskConstants.TRACK_FAILED_MSG, ex);
        }
    }

    public void trackApplicationStatusV1() throws TaskException {
        try {

            String address = this.dinkyParameters.getAddress();
            String taskId = this.dinkyParameters.getTaskId();
            if (status && jobInstanceId == null) {
                // Use address-taskId as app id
                setAppIds(String.format(DinkyTaskConstants.APPIDS_FORMAT, address, taskId));
                setExitStatusCode(mapStatusToExitCode(true));
                log.info("Dinky common sql task finished.");
                return;
            }
            String apiResultDataKey = DinkyTaskConstants.API_RESULT_DATA;
            boolean finishFlag = false;
            while (!finishFlag) {
                JsonNode jobInstanceInfoResult = getJobInstanceInfo(address, jobInstanceId);
                if (!checkResultV1(jobInstanceInfoResult)) {
                    break;
                }
                String jobInstanceStatus =
                        jobInstanceInfoResult.get(apiResultDataKey).get("status").asText();
                switch (jobInstanceStatus) {
                    case DinkyTaskConstants.STATUS_FINISHED:
                        final int exitStatusCode = mapStatusToExitCode(status);
                        // Use address-taskId as app id
                        setAppIds(String.format(DinkyTaskConstants.APPIDS_FORMAT, address, taskId));
                        setExitStatusCode(exitStatusCode);
                        log.info("dinky task finished with results: {}",
                                jobInstanceInfoResult.get(apiResultDataKey));
                        finishFlag = true;
                        break;
                    case DinkyTaskConstants.STATUS_FAILED:
                    case DinkyTaskConstants.STATUS_CANCELED:
                    case DinkyTaskConstants.STATUS_UNKNOWN:
                        errorHandle(jobInstanceInfoResult.get(apiResultDataKey).get(DinkyTaskConstants.API_RESULT_ERROR)
                                .asText());
                        finishFlag = true;
                        break;
                    default:
                        Thread.sleep(DinkyTaskConstants.SLEEP_MILLIS);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error(DinkyTaskConstants.TRACK_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DinkyTaskConstants.TRACK_FAILED_MSG, ex);
        }
    }
    /**
     * map dinky task status to exitStatusCode
     *
     * @param status dinky job status
     * @return exitStatusCode
     */
    private int mapStatusToExitCode(boolean status) {
        if (status) {
            return TaskConstants.EXIT_CODE_SUCCESS;
        } else {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    private boolean checkResultV0(JsonNode result) {
        boolean isCorrect = true;
        if (result instanceof MissingNode || result instanceof NullNode) {
            errorHandle(DinkyTaskConstants.API_VERSION_ERROR_TIPS);
            isCorrect = false;
        } else if (result.get(DinkyTaskConstants.API_RESULT_CODE).asInt() == DinkyTaskConstants.API_ERROR) {
            errorHandle(result.get(DinkyTaskConstants.API_RESULT_MSG));
            isCorrect = false;
        }
        return isCorrect;
    }

    private boolean checkResultV1(JsonNode result) {
        boolean isCorrect = true;
        if (result instanceof MissingNode || result instanceof NullNode) {
            errorHandle(DinkyTaskConstants.API_VERSION_ERROR_TIPS);
            isCorrect = false;
        } else if (!result.get(DinkyTaskConstants.API_RESULT_SUCCESS).asBoolean()) {
            errorHandle(result.get(DinkyTaskConstants.API_RESULT_MSG));
            isCorrect = false;
        }
        return isCorrect;
    }

    private void errorHandle(Object msg) {
        setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        log.error("dinky task submit failed with error: {}", msg);
    }

    @Override
    public AbstractParameters getParameters() {
        return dinkyParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        String address = this.dinkyParameters.getAddress();
        String taskId = this.dinkyParameters.getTaskId();
        log.info("trying terminate dinky task, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                taskId);
        cancelTask(address, taskId);
        log.warn("dinky task terminated, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                taskId);
    }

    private Map<String, String> generateVariables() {
        Map<String, String> variables = new ConcurrentHashMap<>();
        List<Property> propertyList = JSONUtils.toList(taskExecutionContext.getGlobalParams(), Property.class);
        if (propertyList != null && !propertyList.isEmpty()) {
            for (Property property : propertyList) {
                variables.put(property.getProp(), property.getValue());
            }
        }
        List<Property> localParams = this.dinkyParameters.getLocalParams();
        if (localParams == null || localParams.isEmpty()) {
            return variables;
        }
        for (Property property : localParams) {
            variables.put(property.getProp(), property.getValue());
        }
        return variables;
    }

    private String getDinkyVersion(String address) {
        JsonNode versionJsonNode = parse(doGet(address + DinkyTaskConstants.GET_VERSION, new HashMap<>()));
        if (versionJsonNode instanceof MissingNode || versionJsonNode == null
                || versionJsonNode.get(DinkyTaskConstants.API_RESULT_CODE).asInt() == DinkyTaskConstants.API_ERROR) {
            return "0";
        }
        return versionJsonNode.get(DinkyTaskConstants.API_RESULT_DATA).asText();
    }

    private JsonNode submitTaskV0(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_TASK_ID, taskId);
        return parse(doGet(address + DinkyTaskConstants.SUBMIT_TASK, params));
    }

    private JsonNode onlineTaskV0(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_TASK_ID, taskId);
        return parse(doGet(address + DinkyTaskConstants.ONLINE_TASK, params));
    }

    private JsonNode submitTaskV1(String address, String taskId, boolean isOnline, Map<String, String> variables) {
        Map<String, Object> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_TASK_ID, taskId);
        params.put(DinkyTaskConstants.PARAM_TASK_IS_ONLINE, isOnline);
        params.put(DinkyTaskConstants.PARAM_TASK_VARIABLES, variables);
        return parse(sendJsonStr(address + DinkyTaskConstants.SUBMIT_TASK, JSONUtils.toJsonString(params)));
    }

    private JsonNode cancelTask(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_JSON_TASK_ID, taskId);
        params.put(DinkyTaskConstants.PARAM_SAVEPOINT_TYPE, DinkyTaskConstants.SAVEPOINT_CANCEL);
        return parse(sendJsonStr(address + DinkyTaskConstants.SAVEPOINT_TASK, JSONUtils.toJsonString(params)));
    }

    private JsonNode getJobInstanceInfo(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_JOB_INSTANCE_ID, taskId);
        return parse(doGet(address + DinkyTaskConstants.GET_JOB_INFO, params));
    }

    private JsonNode parse(String res) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = null;
        try {
            result = mapper.readTree(res);
        } catch (JsonProcessingException e) {
            log.error("dinky task submit failed with error", e);
        }
        return result;
    }

    private String doGet(String url, Map<String, String> params) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (null != params && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = uriBuilder.build();
            httpGet = new HttpGet(uri);
            log.info("access url: {}", uri);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("dinky task succeed with results: {}", result);
            } else {
                log.error("dinky task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("dinky task terminated: {}", ie.getMessage());
        } catch (Exception e) {
            log.error("dinky task terminated: ", e);
        } finally {
            if (null != httpGet) {
                httpGet.releaseConnection();
            }
        }
        return result;
    }

    private String sendJsonStr(String url, String params) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");
            httpPost.setHeader("Accept", "application/json");
            if (StringUtils.isNotBlank(params)) {
                httpPost.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("dinky task succeed with results: {}", result);
            } else {
                log.error("dinky task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("dinky task terminated: {}", ie.getMessage());
        } catch (Exception he) {
            log.error("dinky task terminated: ", he);
        }
        return result;
    }

}
