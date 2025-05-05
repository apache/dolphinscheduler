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

package org.apache.dolphinscheduler.plugin.task.flink.gateway;

import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.JobStatus;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.RefreshMaterializedTableRequest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client for interacting with Flink SQL Gateway.
 */
public class FlinkSqlClient {

    private static final Logger log = LoggerFactory.getLogger(FlinkSqlClient.class);

    private static final int MAX_RETRIES = 30;
    private static final int RETRY_INTERVAL_SECONDS = 2;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final CloseableHttpClient httpClient;

    /**
     * Constructs a new FlinkSqlClient with the specified base URL.
     *
     * @param baseUrl The base URL of the Flink SQL Gateway
     */
    public FlinkSqlClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Opens a new session with the Flink SQL Gateway.
     *
     * @param config Configuration parameters for the session
     * @return The session handle
     * @throws IOException if an I/O error occurs
     */
    public String openSession(Map<String, String> config) throws IOException {
        String url = baseUrl + "/v3/sessions";
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(config)));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            return objectMapper.readTree(responseBody).get("sessionHandle").asText();
        }
    }

    /**
     * Executes a SQL statement in the specified session.
     *
     * @param sessionHandle The session handle
     * @param statement The SQL statement to execute
     * @return The operation handle
     * @throws IOException if an I/O error occurs
     */
    public String executeStatement(String sessionHandle, String statement) throws IOException {
        String url = baseUrl + "/v3/sessions/" + sessionHandle + "/statements";
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(String.format("{\"statement\": \"%s\"}", statement)));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            return objectMapper.readTree(responseBody).get("operationHandle").asText();
        }
    }

    /**
     * Gets the result of an operation.
     *
     * @param sessionHandle The session handle
     * @param operationHandle The operation handle
     * @return The operation result
     * @throws IOException if an I/O error occurs
     */
    public FetchResultResponseBody getOperationResult(String sessionHandle, String operationHandle) throws IOException {
        String url = baseUrl + "/v3/sessions/" + sessionHandle + "/operations/" + operationHandle + "/result/0";
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            return objectMapper.readValue(responseBody, FetchResultResponseBody.class);
        }
    }

    /**
     * Waits for an operation to complete and returns its result.
     *
     * @param sessionHandle The session handle
     * @param operationHandle The operation handle
     * @return The operation result
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public FetchResultResponseBody waitForOperationResult(String sessionHandle,
                                                          String operationHandle) throws IOException, InterruptedException {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            FetchResultResponseBody result = getOperationResult(sessionHandle, operationHandle);

            if ("PAYLOAD".equals(result.getResultType())) {
                return result;
            }

            // If the operation is not ready, wait and retry
            log.info("Operation not ready, retrying... (attempt {}/{})", retryCount + 1, MAX_RETRIES);
            TimeUnit.SECONDS.sleep(RETRY_INTERVAL_SECONDS);
            retryCount++;
        }

        throw new IOException("Operation timed out after " + MAX_RETRIES + " retries");
    }

    /**
     * Closes a session.
     *
     * @param sessionHandle The session handle
     * @throws IOException if an I/O error occurs
     */
    public void closeSession(String sessionHandle) throws IOException {
        String url = baseUrl + "/v3/sessions/" + sessionHandle;
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Failed to close session: " + response.getStatusLine().getReasonPhrase());
            }
        }
    }

    /**
     * Closes the HTTP client.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        httpClient.close();
    }

    /**
     * Refreshes a materialized table.
     *
     * @param sessionHandle The session handle
     * @param tableName The name of the materialized table
     * @param request The refresh request
     * @return The operation handle
     * @throws IOException if an I/O error occurs
     */
    public String refreshMaterializedTable(String sessionHandle, String tableName,
                                           RefreshMaterializedTableRequest request) throws IOException {
        String url = baseUrl + "/v3/sessions/" + sessionHandle + "/materialized-tables/" + tableName + "/refresh";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request)));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("operationHandle").asText();
        }
    }

    /**
     * Describes the status of a job.
     *
     * @param sessionHandle The session handle
     * @param jobId The job ID
     * @return The job status
     * @throws IOException if an I/O error occurs
     */
    public JobStatus describeJob(String sessionHandle, String jobId) throws IOException {
        String sql = String.format("DESCRIBE JOB '%s'", jobId);
        String operationHandle = executeStatement(sessionHandle, sql);
        try {
            FetchResultResponseBody result = waitForOperationResult(sessionHandle, operationHandle);
            String jobStatus = result.getResult().get(0).getValues().get(2);
            return JobStatus.valueOf(jobStatus);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operation interrupted while describing job", e);
        }
    }
}
