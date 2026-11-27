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

import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Minimal SQL Gateway REST v1 transport (FLIP-293 wire contract, stable since Flink 1.16 and
 * unchanged through the 2.x line).
 *
 * <p>This is the fallback transport for gateway mode: the Java 8 JDBC driver negotiates the
 * gateway REST version on connect and aborts when the gateway advertises a version its enum does
 * not know (e.g. a 2.3 gateway listing V4). The REST v1 endpoints used here exist on every
 * gateway from 1.18 to 2.x, so this client works against both without any negotiation.
 */
@Slf4j
class FlinkSqlGatewayRestClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int SHORT_CALL_READ_TIMEOUT_MILLIS = 30_000;

    private final String gatewayUrl;

    FlinkSqlGatewayRestClient(String jdbcUrl) {
        this.gatewayUrl = toRestBaseUrl(jdbcUrl);
    }

    /**
     * Translates a JDBC gateway URL ({@code jdbc:flink://host:port}) into the REST base URL
     * ({@code http://host:port}). An explicit http(s) URL is passed through unchanged.
     */
    static String toRestBaseUrl(String jdbcUrl) {
        if (StringUtils.isBlank(jdbcUrl)) {
            throw new TaskException("Flink sql gateway url is blank");
        }
        String rest = jdbcUrl.trim();
        if (rest.startsWith("jdbc:flink://")) {
            rest = rest.substring("jdbc:flink://".length());
        } else if (rest.startsWith("jdbc:flink:")) {
            rest = rest.substring("jdbc:flink:".length());
        }
        if (!rest.startsWith("http://") && !rest.startsWith("https://")) {
            rest = "http://" + rest;
        }
        while (rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        return rest;
    }

    String openSession() throws IOException {
        JsonNode response = post("/v1/sessions", MAPPER.createObjectNode());
        String sessionHandle = response.path("sessionHandle").asText();
        if (StringUtils.isBlank(sessionHandle)) {
            throw new IOException("Gateway open session response has no sessionHandle: " + response);
        }
        return sessionHandle;
    }

    String executeStatement(String sessionHandle, String sql) throws IOException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("statement", sql);
        JsonNode response = post("/v1/sessions/" + sessionHandle + "/statements", body);
        String operationHandle = response.path("operationHandle").asText();
        if (StringUtils.isBlank(operationHandle)) {
            throw new IOException("Gateway execute statement response has no operationHandle: " + response);
        }
        return operationHandle;
    }

    String getOperationStatus(String sessionHandle, String operationHandle) throws IOException {
        JsonNode response = request("GET",
                "/v1/sessions/" + sessionHandle + "/operations/" + operationHandle + "/status", null,
                SHORT_CALL_READ_TIMEOUT_MILLIS);
        return response.path("status").asText();
    }

    ResultPages fetchAllResults(String sessionHandle, String operationHandle) throws IOException {
        String uri = "/v1/sessions/" + sessionHandle + "/operations/" + operationHandle + "/result/0?fetchType=EOS";
        List<String> columns = null;
        List<List<String>> rows = new ArrayList<>();
        while (uri != null) {
            // The EOS fetch blocks server-side until the operation reaches a terminal state.
            JsonNode response = request("GET", uri, null, 0);
            JsonNode results = response.path("results");
            if (columns == null && results.has("columns")) {
                columns = new ArrayList<>();
                for (JsonNode column : results.path("columns")) {
                    columns.add(column.path("name").asText());
                }
            }
            for (JsonNode row : results.path("data")) {
                List<String> fields = new ArrayList<>();
                for (JsonNode field : row.path("fields")) {
                    fields.add(field.isNull() ? null : field.asText());
                }
                rows.add(fields);
            }
            if ("EOS".equals(response.path("resultType").asText())) {
                break;
            }
            String nextResultUri = response.path("nextResultUri").asText(null);
            uri = StringUtils.isBlank(nextResultUri) ? null : nextResultUri;
        }
        return new ResultPages(columns == null ? new ArrayList<>() : columns, rows);
    }

    void cancelOperation(String sessionHandle, String operationHandle) throws IOException {
        request("DELETE", "/v1/sessions/" + sessionHandle + "/operations/" + operationHandle, null,
                SHORT_CALL_READ_TIMEOUT_MILLIS);
    }

    void closeSession(String sessionHandle) throws IOException {
        request("DELETE", "/v1/sessions/" + sessionHandle, null, SHORT_CALL_READ_TIMEOUT_MILLIS);
    }

    private JsonNode post(String uri, ObjectNode body) throws IOException {
        return request("POST", uri, MAPPER.writeValueAsBytes(body), SHORT_CALL_READ_TIMEOUT_MILLIS);
    }

    private JsonNode request(String method, String uri, byte[] body, int readTimeoutMillis) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(gatewayUrl + uri).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(readTimeoutMillis);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        if (body != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(body);
        }
        int code = connection.getResponseCode();
        InputStream in = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String text = in == null ? "" : readAll(in);
        if (code >= 400) {
            throw new IOException(
                    "Gateway " + method + " " + uri + " failed: HTTP " + code + (text.isEmpty() ? "" : " " + text));
        }
        return text.isEmpty() ? MAPPER.createObjectNode() : MAPPER.readTree(text);
    }

    private static String readAll(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) > 0) {
            builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        in.close();
        return builder.toString();
    }

    /**
     * The terminal-state result set of one statement, mirroring what the JDBC driver would
     * surface: named columns plus stringified row fields.
     */
    @Getter
    static class ResultPages {

        private final List<String> columns;
        private final List<List<String>> rows;

        ResultPages(List<String> columns, List<List<String>> rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }
}
