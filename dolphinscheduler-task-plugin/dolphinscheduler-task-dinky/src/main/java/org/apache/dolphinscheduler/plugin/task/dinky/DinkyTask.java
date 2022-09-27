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

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

public class DinkyTask extends AbstractRemoteTask {

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;

    /**
     * dinky parameters
     */
    private DinkyParameters dinkyParameters;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
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
        logger.info("dinky task params:{}", taskParams);
        this.dinkyParameters = JSONUtils.parseObject(taskParams, DinkyParameters.class);
        if (this.dinkyParameters == null || !this.dinkyParameters.checkParameters()) {
            throw new DinkyTaskException("dinky task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {

            String address = this.dinkyParameters.getAddress();
            String taskId = this.dinkyParameters.getTaskId();
            boolean isOnline = this.dinkyParameters.isOnline();
            JsonNode result;
            if (isOnline) {
                // Online dinky task, and only one job is allowed to execute
                result = onlineTask(address, taskId);
            } else {
                // Submit dinky task
                result = submitTask(address, taskId);
            }
            if (checkResult(result)) {
                boolean status = result.get(DinkyTaskConstants.API_RESULT_DATAS).get("success").asBoolean();
                String jobInstanceId = result.get(DinkyTaskConstants.API_RESULT_DATAS).get("jobInstanceId").asText();
                boolean finishFlag = false;
                while (!finishFlag) {
                    JsonNode jobInstanceInfoResult = getJobInstanceInfo(address, jobInstanceId);
                    if (!checkResult(jobInstanceInfoResult)) {
                        break;
                    }
                    String jobInstanceStatus =
                            jobInstanceInfoResult.get(DinkyTaskConstants.API_RESULT_DATAS).get("status").asText();
                    switch (jobInstanceStatus) {
                        case DinkyTaskConstants.STATUS_FINISHED:
                            final int exitStatusCode = mapStatusToExitCode(status);
                            // Use address-taskId as app id
                            setAppIds(String.format("%s-%s", address, taskId));
                            setExitStatusCode(exitStatusCode);
                            logger.info("dinky task finished with results: {}",
                                    result.get(DinkyTaskConstants.API_RESULT_DATAS));
                            finishFlag = true;
                            break;
                        case DinkyTaskConstants.STATUS_FAILED:
                        case DinkyTaskConstants.STATUS_CANCELED:
                        case DinkyTaskConstants.STATUS_UNKNOWN:
                            errorHandle(jobInstanceInfoResult.get(DinkyTaskConstants.API_RESULT_DATAS).get("error")
                                    .asText());
                            finishFlag = true;
                            break;
                        default:
                            Thread.sleep(DinkyTaskConstants.SLEEP_MILLIS);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("Execute dinkyTask failed", ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute dinkyTask failed", ex);
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

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

    private boolean checkResult(JsonNode result) {
        if (result instanceof MissingNode || result == null) {
            errorHandle(DinkyTaskConstants.API_VERSION_ERROR_TIPS);
            return false;
        } else if (result.get("code").asInt() == DinkyTaskConstants.API_ERROR) {
            errorHandle(result.get("msg"));
            return false;
        }
        return true;
    }

    private void errorHandle(Object msg) {
        setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        logger.error("dinky task submit failed with error: {}", msg);
    }

    @Override
    public AbstractParameters getParameters() {
        return dinkyParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        String address = this.dinkyParameters.getAddress();
        String taskId = this.dinkyParameters.getTaskId();
        logger.info("trying terminate dinky task, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                taskId);
        cancelTask(address, taskId);
        logger.warn("dinky task terminated, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                taskId);
    }

    private JsonNode submitTask(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_TASK_ID, taskId);
        return parse(doGet(address + DinkyTaskConstants.SUBMIT_TASK, params));
    }

    private JsonNode onlineTask(String address, String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put(DinkyTaskConstants.PARAM_TASK_ID, taskId);
        return parse(doGet(address + DinkyTaskConstants.ONLINE_TASK, params));
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
            logger.error("dinky task submit failed with error", e);
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
            logger.info("access url: {}", uri);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                logger.info("dinky task succeed with results: {}", result);
            } else {
                logger.error("dinky task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            logger.error("dinky task terminated: {}", ie.getMessage());
        } catch (Exception e) {
            logger.error("dinky task terminated: ", e);
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
                logger.info("dinky task succeed with results: {}", result);
            } else {
                logger.error("dinky task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            logger.error("dinky task terminated: {}", ie.getMessage());
        } catch (Exception he) {
            logger.error("dinky task terminated: ", he);
        }
        return result;
    }
}
