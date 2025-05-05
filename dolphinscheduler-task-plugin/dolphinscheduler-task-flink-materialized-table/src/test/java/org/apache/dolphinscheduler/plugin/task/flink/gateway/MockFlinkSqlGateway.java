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

import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBodyImpl;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.NotReadyFetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.Row;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class MockFlinkSqlGateway {

    private static final String SELECT_OPERATION_HANDLE = "5de60b8e-b7e3-44ff-bbd4-f81223caf174";
    private static final String DESCRIBE_OPERATION_HANDLE = "89c4ddc7-cfa1-4f65-a8a8-516a1aec62c2";
    private static final String REFRESH_OPERATION_HANDLE = "7f8e9d0c-1b2a-3c4d-5e6f-7a8b9c0d1e2f";
    private static final String TEST_JOB_ID = "a21aac0ecda1283ff444d63fac1cf60b";
    private static final String TEST_SESSION_HANDLE = "test-session-handle";
    private final HttpServer server;
    private final ObjectMapper objectMapper;
    private final Map<String, String> operationHandles = new HashMap<>();
    private final Map<String, AtomicInteger> resultRetryCount = new ConcurrentHashMap<>();
    private final int port;

    public MockFlinkSqlGateway() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        port = server.getAddress().getPort();
        server.setExecutor(Executors.newFixedThreadPool(10));
        objectMapper = new ObjectMapper();
        createContexts();
    }

    public String getBaseUrl() {
        return String.format("http://localhost:%d", port);
    }

    public void start() {
        server.start();
        System.out.println("Mock Flink SQL Gateway started on port " + port);
    }

    public void stop() {
        server.stop(0);
        System.out.println("Mock Flink SQL Gateway stopped");
    }

    private void createContexts() {
        server.createContext("/v3/sessions", exchange -> {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    handleCreateSession(exchange);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error");
            }
        });

        server.createContext("/v3/sessions/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                if (path.contains("/operations/") && path.contains("/result/")) {
                    handleGetResult(exchange);
                } else if (path.contains("/statements")) {
                    handleExecuteStatement(exchange);
                } else if (path.contains("/materialized-tables/") && path.contains("/refresh")) {
                    handleRefreshMaterializedTable(exchange);
                } else {
                    sendErrorResponse(exchange, 404, "Not Found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error");
            }
        });
    }

    private void handleCreateSession(HttpExchange exchange) throws IOException {
        String response = String.format("{\"sessionHandle\":\"%s\"}", TEST_SESSION_HANDLE);
        sendResponse(exchange, response, 200);
    }

    private void handleExecuteStatement(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange.getRequestBody());
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(requestBody);
        String statement = jsonNode.get("statement").asText();

        String operationHandle;
        if (statement.equals("SELECT 1")) {
            operationHandle = SELECT_OPERATION_HANDLE;
        } else if (statement.startsWith("DESCRIBE JOB")) {
            operationHandle = DESCRIBE_OPERATION_HANDLE;
        } else {
            throw new IllegalArgumentException("Unsupported statement: " + statement);
        }

        operationHandles.put(operationHandle, statement);
        resultRetryCount.put(operationHandle, new AtomicInteger(0));

        String response = String.format("{\"operationHandle\":\"%s\"}", operationHandle);
        sendResponse(exchange, response, 200);
    }

    private String readRequestBody(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    private void handleGetResult(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String operationHandle = path.substring(path.indexOf("/operations/") + 12, path.indexOf("/result/"));

        String statement = operationHandles.get(operationHandle);
        if (statement == null && !operationHandle.equals(REFRESH_OPERATION_HANDLE)) {
            throw new IllegalArgumentException("Unknown operation handle: " + operationHandle);
        }

        AtomicInteger retryCount = resultRetryCount.get(operationHandle);
        int currentRetry = retryCount.getAndIncrement();

        if (currentRetry == 0) {
            handleNotReadyResult(exchange, operationHandle);
            return;
        }

        if (operationHandle.equals(REFRESH_OPERATION_HANDLE)) {
            handleRefreshResult(exchange, operationHandle);
        } else if (statement.equals("SELECT 1")) {
            handleSelect1Result(exchange, operationHandle);
        } else if (statement.startsWith("DESCRIBE JOB")) {
            handleDescribeJobResult(exchange, operationHandle);
        }
    }

    private void handleNotReadyResult(HttpExchange exchange, String operationHandle) throws IOException {
        NotReadyFetchResultResponseBody response = new NotReadyFetchResultResponseBody(
                String.format("/v3/sessions/%s/operations/%s/result/0?rowFormat=JSON", TEST_SESSION_HANDLE,
                        operationHandle),
                "NOT_READY");
        sendResponse(exchange, objectMapper.writeValueAsString(response), 200);
    }

    private void handleSelect1Result(HttpExchange exchange, String operationHandle) throws IOException {
        List<Row> rows = new ArrayList<>();
        rows.add(new FetchResultResponseBodyImpl.RowImpl(Arrays.asList("1")));

        FetchResultResponseBodyImpl response = new FetchResultResponseBodyImpl(
                "PAYLOAD",
                String.format("/v3/sessions/%s/operations/%s/result/1?rowFormat=JSON", TEST_SESSION_HANDLE,
                        operationHandle),
                rows,
                TEST_JOB_ID);
        sendResponse(exchange, objectMapper.writeValueAsString(response), 200);
    }

    private void handleDescribeJobResult(HttpExchange exchange, String operationHandle) throws IOException {
        List<Row> rows = new ArrayList<>();
        rows.add(new FetchResultResponseBodyImpl.RowImpl(Arrays.asList(
                TEST_JOB_ID,
                "SELECT 1",
                "FINISHED",
                "2025-04-28T01:32:11.958Z")));

        FetchResultResponseBodyImpl response = new FetchResultResponseBodyImpl(
                "PAYLOAD",
                String.format("/v3/sessions/%s/operations/%s/result/1?rowFormat=JSON", TEST_SESSION_HANDLE,
                        operationHandle),
                rows,
                TEST_JOB_ID);
        sendResponse(exchange, objectMapper.writeValueAsString(response), 200);
    }

    private void handleRefreshMaterializedTable(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange.getRequestBody());
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(requestBody);

        if (!jsonNode.has("isPeriodic")) {
            String errorResponse = "{\"errors\":[\"Field 'isPeriodic' is required\"]}";
            sendResponse(exchange, errorResponse, 400);
            return;
        }

        com.fasterxml.jackson.databind.JsonNode isPeriodicNode = jsonNode.get("isPeriodic");
        if (isPeriodicNode.isNull()) {
            String errorResponse = "{\"errors\":[\"Field 'isPeriodic' cannot be null\"]}";
            sendResponse(exchange, errorResponse, 400);
            return;
        }

        resultRetryCount.put(REFRESH_OPERATION_HANDLE, new AtomicInteger(0));

        String response = String.format("{\"operationHandle\":\"%s\"}", REFRESH_OPERATION_HANDLE);
        sendResponse(exchange, response, 200);
    }

    private void handleRefreshResult(HttpExchange exchange, String operationHandle) throws IOException {
        List<Row> rows = new ArrayList<>();
        rows.add(new FetchResultResponseBodyImpl.RowImpl(Arrays.asList(TEST_JOB_ID)));

        FetchResultResponseBodyImpl response = new FetchResultResponseBodyImpl(
                "PAYLOAD",
                String.format("/v3/sessions/%s/operations/%s/result/1?rowFormat=JSON", TEST_SESSION_HANDLE,
                        operationHandle),
                rows,
                TEST_JOB_ID);
        sendResponse(exchange, objectMapper.writeValueAsString(response), 200);
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"errors\":[\"%s\"]}", message);
        sendResponse(exchange, response, statusCode);
    }
}
