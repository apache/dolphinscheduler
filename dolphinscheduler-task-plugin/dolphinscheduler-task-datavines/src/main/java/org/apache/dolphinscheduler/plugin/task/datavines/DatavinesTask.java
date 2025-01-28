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

package org.apache.dolphinscheduler.plugin.task.datavines;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;

@Slf4j
public class DatavinesTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;

    private DatavinesParameters datavinesParameters;
    private String jobExecutionId;
    private boolean executionStatus;

    protected DatavinesTask(TaskExecutionContext taskExecutionContext) {
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
        this.datavinesParameters = JSONUtils.parseObject(taskParams, DatavinesParameters.class);
        log.info("initialize datavines task params : {}", JSONUtils.toPrettyJsonString(datavinesParameters));
        if (this.datavinesParameters == null || !this.datavinesParameters.checkParameters()) {
            throw new DatavinesTaskException("datavines task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        super.handle(taskCallBack);
    }

    @Override
    public void submitApplication() throws TaskException {
        executeJob();
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        trackApplicationStatusInner();
    }

    private void executeJob() {
        try {
            String address = this.datavinesParameters.getAddress();
            String jobId = this.datavinesParameters.getJobId();
            String token = this.datavinesParameters.getToken();
            JsonNode result;
            String apiResultDataKey = DatavinesTaskConstants.API_RESULT_DATA;
            result = executeJob(address, jobId, token);
            if (checkResult(result)) {
                jobExecutionId = result.get(apiResultDataKey).asText();
                executionStatus = true;
            }
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            log.error(DatavinesTaskConstants.SUBMIT_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DatavinesTaskConstants.SUBMIT_FAILED_MSG, ex);
        }
    }

    public void trackApplicationStatusInner() throws TaskException {
        try {
            String address = this.datavinesParameters.getAddress();
            if (executionStatus && jobExecutionId == null) {
                // Use address-taskId as app id
                setAppIds(String.format(DatavinesTaskConstants.APPIDS_FORMAT, address, this.jobExecutionId));
                setExitStatusCode(mapStatusToExitCode(false));
                log.info("datavines task failed.");
                return;
            }
            String apiResultDataKey = DatavinesTaskConstants.API_RESULT_DATA;
            boolean finishFlag = false;
            while (!finishFlag) {
                JsonNode jobExecutionStatus =
                        getJobExecutionStatus(address, jobExecutionId, this.datavinesParameters.getToken());
                if (!checkResult(jobExecutionStatus)) {
                    break;
                }
                String jobExecutionStatusStr = jobExecutionStatus.get(apiResultDataKey).asText();
                switch (jobExecutionStatusStr) {
                    case DatavinesTaskConstants.STATUS_SUCCESS:
                        setAppIds(String.format(DatavinesTaskConstants.APPIDS_FORMAT, address, this.jobExecutionId));
                        JsonNode jobExecutionResult =
                                getJobExecutionResult(address, jobExecutionId, this.datavinesParameters.getToken());
                        if (!checkResult(jobExecutionResult)) {
                            break;
                        }

                        String jobExecutionResultStr = jobExecutionResult.get(apiResultDataKey).asText();
                        boolean checkResult = true;
                        if (this.datavinesParameters.isFailureBlock()) {
                            checkResult = DatavinesTaskConstants.STATUS_SUCCESS.equals(jobExecutionResultStr);
                        }

                        setExitStatusCode(mapStatusToExitCode(checkResult));
                        log.info("datavines task finished, execution status is {} and check result is {}",
                                jobExecutionStatusStr, jobExecutionResultStr);
                        finishFlag = true;
                        break;
                    case DatavinesTaskConstants.STATUS_FAILURE:
                    case DatavinesTaskConstants.STATUS_KILL:
                        errorHandle("task execution status: " + jobExecutionStatusStr);
                        finishFlag = true;
                        break;
                    default:
                        Thread.sleep(DatavinesTaskConstants.SLEEP_MILLIS);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error(DatavinesTaskConstants.TRACK_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DatavinesTaskConstants.TRACK_FAILED_MSG, ex);
        }
    }

    /**
     * map datavines task status to exitStatusCode
     *
     * @param status datavines job status
     * @return exitStatusCode
     */
    private int mapStatusToExitCode(boolean status) {
        if (status) {
            return EXIT_CODE_SUCCESS;
        } else {
            return EXIT_CODE_FAILURE;
        }
    }

    private boolean checkResult(JsonNode result) {
        boolean isCorrect = true;
        if (result instanceof MissingNode || result instanceof NullNode) {
            errorHandle(DatavinesTaskConstants.API_ERROR_MSG);
            isCorrect = false;
        } else if (result.get(DatavinesTaskConstants.API_RESULT_CODE)
                .asInt() != DatavinesTaskConstants.API_RESULT_CODE_SUCCESS) {
            errorHandle(result.get(DatavinesTaskConstants.API_RESULT_MSG));
            isCorrect = false;
        }
        return isCorrect;
    }

    private void errorHandle(Object msg) {
        setExitStatusCode(EXIT_CODE_FAILURE);
        log.error("datavines task execute failed with error: {}", msg);
    }

    @Override
    public AbstractParameters getParameters() {
        return datavinesParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        String address = this.datavinesParameters.getAddress();
        log.info("trying terminate datavines task, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                jobExecutionId);
        killJobExecution(address, jobExecutionId, this.datavinesParameters.getToken());
        log.warn("datavines task terminated, taskId: {}, address: {}, jobExecutionId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                jobExecutionId);
    }

    private JsonNode executeJob(String address, String jobId, String token) {
        return parse(doPost(address + DatavinesTaskConstants.EXECUTE_JOB + jobId, token));
    }

    private JsonNode getJobExecutionStatus(String address, String jobExecutionId, String token) {
        return parse(doGet(address + DatavinesTaskConstants.GET_JOB_EXECUTION_STATUS + jobExecutionId, token));
    }

    private JsonNode getJobExecutionResult(String address, String jobExecutionId, String token) {
        return parse(doGet(address + DatavinesTaskConstants.GET_JOB_EXECUTION_RESULT + jobExecutionId, token));
    }

    private JsonNode killJobExecution(String address, String jobExecutionId, String token) {
        return parse(doPost(address + DatavinesTaskConstants.JOB_EXECUTION_KILL + jobExecutionId, token));
    }

    private JsonNode parse(String res) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = null;
        try {
            result = mapper.readTree(res);
        } catch (JsonProcessingException e) {
            log.error("datavines task submit failed with error", e);
        }
        return result;
    }

    private String doGet(String url, String token) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            URI uri = uriBuilder.build();
            httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + token);
            log.info("access url: {}", uri);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("datavines task succeed with results: {}", result);
            } else {
                log.error("datavines task terminated,response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("datavines task terminated: {}", ie.getMessage());
        } catch (Exception e) {
            log.error("datavines task terminated: ", e);
        } finally {
            if (null != httpGet) {
                httpGet.releaseConnection();
            }
        }
        return result;
    }

    private String doPost(String url, String token) {
        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader("Authorization", "Bearer " + token);
            HttpResponse response = httpClient.execute(httpPost);
            log.info("access url: {}", url);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                log.info("datavines task succeed with results: {}", result);
            } else {
                log.error("datavines task terminated, response: {}", response);
            }
        } catch (IllegalArgumentException ie) {
            log.error("datavines task terminated: {}", ie.getMessage());
        } catch (Exception he) {
            log.error("datavines task terminated: ", he);
        }
        return result;
    }

}
