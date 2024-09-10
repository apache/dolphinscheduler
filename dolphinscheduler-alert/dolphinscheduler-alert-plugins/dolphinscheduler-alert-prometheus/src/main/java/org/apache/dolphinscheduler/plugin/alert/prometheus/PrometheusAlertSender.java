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

package org.apache.dolphinscheduler.plugin.alert.prometheus;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.HttpServiceRetryStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusAlertSender {

    private String url;
    private String generatorURL;
    private String annotations;

    public PrometheusAlertSender(Map<String, String> config) {
        url = config.get(PrometheusAlertConstants.NAME_ALERT_MANAGER_URL);
        generatorURL = config.get(PrometheusAlertConstants.NAME_GENERATOR_URL);
        annotations = config.get(PrometheusAlertConstants.NAME_ALERT_MANAGER_ANNOTATIONS);
    }

    public AlertResult sendMessage(AlertData alertData) {
        AlertResult alertResult;
        try {
            String resp = sendMsg(alertData);
            return checkSendAlertManageMsgResult(resp);
        } catch (Exception e) {
            log.error("Send prometheus alert manager alert error", e);
            alertResult = new AlertResult();
            alertResult.setSuccess(false);
            alertResult.setMessage(ExceptionUtils.getMessage(e));
        }
        return alertResult;
    }

    private String sendMsg(AlertData alertData) throws IOException {
        String v2Path = String.format("%s%s", this.url, PrometheusAlertConstants.ALERT_V2_API_PATH);
        String msg = generateContentJson(alertData);
        HttpPost httpPost = constructHttpPost(v2Path, msg);

        try (CloseableHttpClient httpClient = getDefaultClient()) {
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String resp;
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    resp = PrometheusAlertConstants.ALERT_SUCCESS;
                    log.info("Prometheus alert manager send alert succeed, title: {} ,content: {}",
                            alertData.getTitle(),
                            alertData.getContent());
                    return resp;
                }

                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
                log.error(
                        "Prometheus alert manager send alert failed, http status code: {}, title: {} ,content: {}, resp: {}",
                        statusCode,
                        alertData.getTitle(),
                        alertData.getContent(), resp);

                return resp;
            }
        }
    }

    public AlertResult checkSendAlertManageMsgResult(String resp) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);

        if (Objects.equals(resp, PrometheusAlertConstants.ALERT_SUCCESS)) {
            alertResult.setSuccess(true);
            alertResult.setMessage("prometheus alert manager send success");
            return alertResult;
        }

        alertResult.setMessage(String.format("prometheus alert manager send fail, resp is %s", resp));
        log.info("send prometheus alert manager msg error, resp error");
        return alertResult;
    }

    public String generateContentJson(AlertData alertData) {
        List<HashMap> list = JSONUtils.toList(alertData.getContent(), HashMap.class);
        HashMap<String, String> labels = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            labels.put("content", alertData.getContent());
        }
        for (Map map : list) {
            for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>) map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                labels.put(key, value);
            }
        }
        labels.put("title", alertData.getTitle());

        Map<String, Object> alert = new HashMap<>();
        alert.put("labels", labels);

        Map<String, String> annotations = JSONUtils.toMap(this.annotations);
        if (annotations != null) {
            alert.put("annotations", annotations);
        }

        String formattedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        alert.put("startsAt", formattedTime);
        alert.put("endsAt", formattedTime);

        if (generatorURL != null && generatorURL.length() != 0) {
            alert.put("generatorURL", generatorURL);
        }
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(alert);
        return JSONUtils.toJsonString(body);
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();
    }

    private static HttpPost constructHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        return post;
    }
}
