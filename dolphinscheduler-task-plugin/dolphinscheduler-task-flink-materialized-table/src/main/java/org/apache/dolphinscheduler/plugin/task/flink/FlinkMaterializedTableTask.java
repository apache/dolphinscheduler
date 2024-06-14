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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.NameValuePair;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class FlinkMaterializedTableTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;

    private FlinkMaterializedTableParameters flinkMaterializedTableParameters;

    private CloseableHttpClient client;

    private String sessionHandle;

    private String jobId;

    private String gatewayEndpoint;

    private String identifier;

    private Map<String, String> tmpExecutionConfig;

//    v3/sessions/:session_handle/materialized-tables/:identifier/refresh

    protected FlinkMaterializedTableTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        this.flinkMaterializedTableParameters = JSONUtils.parseObject(taskParams, org.apache.dolphinscheduler.plugin.task.flink.FlinkMaterializedTableParameters.class);
        if (this.flinkMaterializedTableParameters == null || !this.flinkMaterializedTableParameters.checkParameters()) {
            throw new FlinkMaterializedTableTaskException("flink materialized table task params is not valid");
        }

        identifier = flinkMaterializedTableParameters.getIdentifier();
        gatewayEndpoint = flinkMaterializedTableParameters.getGatewayEndpoint();

        log.info("Initialize flink materialized table task with task params:{}", JSONUtils.toPrettyJsonString(flinkMaterializedTableParameters));
    }


    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // Create Session
            openSession();

            // Refresh Table
            refreshTable();
            setAppIds(jobId);

            JobStatus jobStatus = pokeRefreshTableJobStatus();

            // Track Refresh Job Status
            while (jobStatus == null || !jobStatus.isGloballyTerminalState()) {
                jobStatus = pokeRefreshTableJobStatus();
            }

            final int exitStatusCode = mapStatusToExitCode(jobStatus);
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Execute FlinkMaterializedTableTask exception");
        } finally {
            // Close Session
            closeSession();
        }
    }

    protected void openSession() throws IOException {
        String url = String.format("%s/v1/sessions", gatewayEndpoint);
        HttpPost post = new HttpPost(url);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)){

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, String> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                sessionHandle = map.get("sessionHandle");
                log.info(sessionHandle);
            } catch (Exception e) {
                log.error("Failed to get session handle", e);
            }
        }
    }

    protected void closeSession() {
        String url = String.format("%s/v1/sessions/%s", gatewayEndpoint, sessionHandle);
        HttpDelete delete = new HttpDelete(url);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(delete)){

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, String> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                // todo: check status
            } catch (Exception e) {
                log.error("Failed to close session", e);
            }
        } catch (IOException e) {
            log.error("Failed to close session", e);
        }
    }


    protected void refreshTable() throws IOException {

        String url = String.format("%s/v3/sessions/%s/materialized-tables/%s/refresh",
            gatewayEndpoint, sessionHandle, identifier);

        HttpPost post = new HttpPost(url);
        String refreshTableOperationHandle = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)){

            // add request parameters or form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("isPeriodic", "true"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, String> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                refreshTableOperationHandle = map.get("operationHandle");
            } catch (Exception e) {
                log.error("Failed to get session handle", e);
            }
        }

        waitForTermination(refreshTableOperationHandle);

        fetchJobId(refreshTableOperationHandle);
    }

    protected void fetchJobId(String refreshTableOperationHandle) throws IOException {
        Objects.requireNonNull(refreshTableOperationHandle);

        String url = String.format("%s/v3/sessions/%s/operations/%s/result/%s",
            gatewayEndpoint, sessionHandle, refreshTableOperationHandle, refreshTableOperationHandle);

        HttpGet get = new HttpGet(url);


        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)){

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, Object> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                Map<String, Object> results = mapper.readValue((String) map.get("results"), Map.class);
                List<Object> data = mapper.readValue((String) results.get("data"), List.class);
                List<Object> fields = (List) ((Map) mapper.readValue((String) data.get(0), Map.class)).get("fields");
                jobId = (String) fields.get(0);
                tmpExecutionConfig = (Map) fields.get(1);
            } catch (Exception e) {
                log.error("Failed to get session handle", e);
            }
        }
    }

    protected void waitForTermination(String operationHandle) throws IOException {
        while (true) {
            String url = String.format("%s/v1/sessions/%s/operations/%s/status", gatewayEndpoint, sessionHandle, operationHandle);
            HttpPost post = new HttpPost(url);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)){

                String resultJsonString = EntityUtils.toString(response.getEntity());

                Map<String, String> map;
                ObjectMapper mapper = new ObjectMapper();

                try {
                    //convert JSON string to Map
                    map = mapper.readValue(resultJsonString, Map.class);
                    JobStatus status = JobStatus.valueOf(map.get("status"));
                    if (status.isTerminalState()) {
                        break;
                    } else {
                        Thread.sleep(5000);
                    }

                } catch (Exception e) {
                    log.error("Failed to get job status!");
                }
            }
        }
    }

    protected JobStatus pokeRefreshTableJobStatus() throws IOException {

        String url = String.format("%s/v3/sessions/%s/statements",
            gatewayEndpoint, sessionHandle);

        HttpPost post = new HttpPost(url);
        String describeJobOperationHandle = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)){

            // add request parameters or form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            String statement = String.format("DESCRIBE JOB %s", jobId);
            urlParameters.add(new BasicNameValuePair("statement", statement));
            urlParameters.add(new BasicNameValuePair("executionConfig", tmpExecutionConfig.toString()));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, String> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                describeJobOperationHandle = map.get("operationHandle");
            } catch (Exception e) {
                log.error("Failed to get session handle", e);
            }
        }

        waitForTermination(describeJobOperationHandle);

        JobStatus jobStatus = null;
        url = String.format("%s/v3/sessions/%s/operations/%s/result/%s",
            gatewayEndpoint, sessionHandle, describeJobOperationHandle, describeJobOperationHandle);
        HttpGet get = new HttpGet(url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)){

            String resultJsonString = EntityUtils.toString(response.getEntity());

            Map<String, Object> map;
            ObjectMapper mapper = new ObjectMapper();

            try {
                //convert JSON string to Map
                map = mapper.readValue(resultJsonString, Map.class);
                jobStatus = JobStatus.valueOf((String) map.get("status"));
            } catch (Exception e) {
                log.error("Failed to get session handle", e);
            }
        }

        return jobStatus;
    }


    private int mapStatusToExitCode(JobStatus status) {
        switch (status) {
            case FINISHED:
                return TaskConstants.EXIT_CODE_SUCCESS;
            case CANCELED:
                return TaskConstants.EXIT_CODE_KILL;
            default:
                return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkMaterializedTableParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {

    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }


    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

}
