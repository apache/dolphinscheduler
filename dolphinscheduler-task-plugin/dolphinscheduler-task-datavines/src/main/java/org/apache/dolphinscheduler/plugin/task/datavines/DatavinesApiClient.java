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

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class DatavinesApiClient {

    private final DatavinesParameters parameters;

    public DatavinesApiClient(DatavinesParameters parameters) {
        this.parameters = parameters;
    }

    public long execute() {
        JsonNode data = request(true,
                parameters.getNormalizedAddress() + DatavinesConstants.API_EXECUTE + parameters.getJobId());
        if (data == null || data.isNull()) {
            throw new DatavinesApiException("Datavines execute API returned empty data", false);
        }
        long executionId = data.asLong(-1L);
        if (executionId <= 0) {
            throw new DatavinesApiException(
                    "Datavines execute API returned invalid execution id: " + sanitize(data.toString()), false);
        }
        return executionId;
    }

    public String status(long executionId) {
        return readText(request(false,
                parameters.getNormalizedAddress() + DatavinesConstants.API_STATUS + executionId));
    }

    public String result(long executionId) {
        return readText(request(false,
                parameters.getNormalizedAddress() + DatavinesConstants.API_RESULT + executionId));
    }

    private String readText(JsonNode data) {
        if (data == null || data.isNull()) {
            throw new DatavinesApiException("Datavines API returned empty data", true);
        }
        return data.asText();
    }

    private JsonNode request(boolean post, String url) {
        try {
            OkHttpResponse response = send(post, url);
            int statusCode = response.getStatusCode();
            String body = response.getBody();
            if (statusCode < 200 || statusCode >= 300) {
                throw new DatavinesApiException(
                        String.format("Datavines API returned HTTP %d: %s", statusCode, sanitize(body)),
                        statusCode >= 500);
            }
            if (StringUtils.isBlank(body)) {
                throw new DatavinesApiException("Datavines API returned empty response", true);
            }
            JsonNode root = JSONUtils.parseObject(body, JsonNode.class);
            if (root == null) {
                throw new DatavinesApiException("Datavines API returned invalid JSON: " + sanitize(body), true);
            }
            int code = root.path("code").asInt(200);
            if (code != 200) {
                String message = root.path("msg").asText(root.path("message").asText(body));
                throw new DatavinesApiException(
                        String.format("Datavines API returned code %d: %s", code, sanitize(message)), false);
            }
            return root.get("data");
        } catch (DatavinesApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DatavinesApiException(
                    "Datavines API request failed: " + sanitize(e.getMessage()), e, true);
        }
    }

    private OkHttpResponse send(boolean post, String url) throws IOException {
        OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(DatavinesConstants.AUTHORIZATION, DatavinesConstants.BEARER_PREFIX + parameters.getToken());
        headerMap.put(DatavinesConstants.CONTENT_TYPE, DatavinesConstants.APPLICATION_JSON);
        headers.setHeaders(headerMap);
        headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_JSON);

        if (post) {
            return OkHttpUtils.post(url, headers, null, Collections.emptyMap(),
                    DatavinesConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                    DatavinesConstants.DEFAULT_READ_TIMEOUT_MILLIS,
                    DatavinesConstants.DEFAULT_READ_TIMEOUT_MILLIS);
        }
        return OkHttpUtils.get(url, headers, null,
                DatavinesConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DatavinesConstants.DEFAULT_READ_TIMEOUT_MILLIS,
                DatavinesConstants.DEFAULT_READ_TIMEOUT_MILLIS);
    }

    private String sanitize(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String sanitized = text;
        if (StringUtils.isNotBlank(parameters.getToken())) {
            sanitized = sanitized.replace(parameters.getToken(), "***");
        }
        return StringUtils.abbreviate(sanitized, 500);
    }
}
