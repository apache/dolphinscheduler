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

package org.apache.dolphinscheduler.plugin.alert.pagerduty;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.HttpServiceRetryStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
public final class PagerDutySender {

    private final String integrationKey;

    public PagerDutySender(Map<String, String> config) {
        integrationKey = config.get(PagerDutyParamsConstants.NAME_PAGER_DUTY_INTEGRATION_KEY_NAME);
        Preconditions.checkArgument(!Objects.isNull(integrationKey), "PagerDuty integration key can not be null");
    }

    public AlertResult sendPagerDutyAlter(String title, String content) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);
        alertResult.setMessage("send pager duty alert fail.");

        try {
            sendPagerDutyAlterV2(alertResult, title, content);
        } catch (Exception e) {
            log.info("send pager duty alert exception : {}", e.getMessage());
        }

        return alertResult;
    }

    private AlertResult sendPagerDutyAlterV2(AlertResult alertResult, String title, String content) throws IOException {
        String requestBody = textToJsonStringV2(title, content);
        return send(alertResult, PagerDutyParamsConstants.PAGER_DUTY_EVENT_API, requestBody);
    }

    private AlertResult send(AlertResult alertResult, String url, String requestBody) throws IOException {
        HttpPost httpPost = constructHttpPost(url, requestBody);
        CloseableHttpClient httpClient =
                HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            try {
                if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {
                    alertResult.setSuccess(true);
                    alertResult.setMessage("send pager duty alert success");
                } else {
                    alertResult.setMessage(
                            String.format("send pager duty alert error, statusCode: %s, responseContent: %s",
                                    statusCode, responseContent));
                    log.info("send pager duty alert fail, statusCode : {}", statusCode);
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            log.info("send pager duty alert exception : {}", e.getMessage());
        } finally {
            httpClient.close();
        }

        return alertResult;
    }

    private String textToJsonStringV2(String title, String content) {
        Map<String, Object> items = new HashMap<>();
        items.put("routing_key", integrationKey);
        items.put("event_action", PagerDutyParamsConstants.PAGER_DUTY_EVENT_ACTION_TRIGGER);
        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", title);
        payload.put("source", PagerDutyParamsConstants.PAGER_DUTY_EVENT_SOURCE);
        payload.put("severity", "critical");
        payload.put("custom_details", formatContent(content));
        items.put("payload", payload);
        return JSONUtils.toJsonString(items);
    }

    private static HttpPost constructHttpPost(String url, String requestBody) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    public static String formatContent(String content) {
        List<Map> list = JSONUtils.toList(content, Map.class);
        if (list.isEmpty()) {
            return content;
        }

        StringBuilder contents = new StringBuilder(100);
        for (Map map : list) {
            for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>) map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                contents.append(key + ":" + value);
                contents.append("\n");
            }
        }

        return contents.toString();
    }
}
