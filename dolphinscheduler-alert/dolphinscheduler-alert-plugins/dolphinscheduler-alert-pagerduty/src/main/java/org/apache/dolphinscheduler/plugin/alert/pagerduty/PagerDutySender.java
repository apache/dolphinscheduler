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

import com.google.common.base.Preconditions;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

public final class PagerDutySender {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PagerDutySender.class);

    private final String integrationKey;

    public PagerDutySender(Map<String, String> config) {
        integrationKey = config.get(PagerDutyParamsConstants.NAME_PAGER_DUTY_INTEGRATION_KEY_NAME);
        Preconditions.checkArgument(!Objects.isNull(integrationKey), "PagerDuty integration key can not be null");
    }

    public AlertResult sendPagerDutyAlter(String title, String content){
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        return sendPagerDutyAlterV2(alertResult, title, content);
    }

    private AlertResult sendPagerDutyAlterV2(AlertResult alertResult, String title, String content) {
        String requestBody = textToJsonStringV2(title, content);
        return send(alertResult, PagerDutyParamsConstants.PAGER_DUTY_EVENT_API, requestBody);
    }

    private AlertResult send(AlertResult alertResult, String url, String requestBody) {
        try {
            RestTemplate client = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = client.postForEntity(url, requestEntity, String.class);
            if (response.getStatusCodeValue() == HttpStatus.OK.value() || response.getStatusCodeValue() == HttpStatus.ACCEPTED.value()) {
                alertResult.setStatus("true");
                alertResult.setMessage("send pager duty alert success");
                return alertResult;
            }
        } catch (Exception e) {
            log.info("send pager duty alert exception : {}", e.getMessage());
            alertResult.setMessage("send pager duty alert fail.");
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
