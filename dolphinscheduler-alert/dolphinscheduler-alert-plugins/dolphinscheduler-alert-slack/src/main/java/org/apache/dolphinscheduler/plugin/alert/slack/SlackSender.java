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

package org.apache.dolphinscheduler.plugin.alert.slack;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
public final class SlackSender {

    private final String webHookUrl;
    private final String botName;

    public SlackSender(Map<String, String> slackAlertParam) {
        webHookUrl = slackAlertParam.get(SlackParamsConstants.SLACK_WEB_HOOK_URL_NAME);
        botName = slackAlertParam.get(SlackParamsConstants.SLACK_BOT_NAME);
        Preconditions.checkArgument(!Objects.isNull(webHookUrl), "SlackWebHookURL can not be null");
        Preconditions.checkArgument(webHookUrl.startsWith("https://hooks.slack.com/services/"),
                "SlackWebHookURL invalidate");
        Preconditions.checkArgument(!Objects.isNull(botName), "slack bot name can not be null");
    }

    /**
     * Send message to slack channel
     *
     * @param title title
     * @param content content
     * @return slack response
     */
    public String sendMessage(String title, String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(SlackParamsConstants.SLACK_BOT_NAME, botName);
            paramMap.put(SlackParamsConstants.TEXT, title);
            if (StringUtils.isNotEmpty(content)) {
                Map<String, String> attachmentTable = new HashMap<>();
                attachmentTable.put(SlackParamsConstants.TEXT, generateMarkDownTable(content));
                List<Map<String, String>> attachments = new ArrayList<>();
                attachments.add(attachmentTable);
                paramMap.put(SlackParamsConstants.ATTACHMENT, attachments);
            }

            HttpPost httpPost = new HttpPost(webHookUrl);
            httpPost.setEntity(new StringEntity(JSONUtils.toJsonString(paramMap), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error("Send message to slack error.", e);
            return "System Exception";
        }
    }

    /**
     * Because the slack does not support table we can transform to specific markdown table
     *
     * @param content sql data content
     */
    private String generateMarkDownTable(String content) {
        List<LinkedHashMap> linkedHashMaps = JSONUtils.toList(content, LinkedHashMap.class);
        if (linkedHashMaps.size() > SlackParamsConstants.MAX_SHOW_NUMBER) {
            linkedHashMaps = linkedHashMaps.subList(0, SlackParamsConstants.MAX_SHOW_NUMBER);
        }
        int maxLen = 0;
        List<String> headers = new LinkedList<>();
        LinkedHashMap<String, Object> tmp = linkedHashMaps.get(0);
        for (Entry<String, Object> entry : tmp.entrySet()) {
            maxLen = Math.max(maxLen, entry.getKey().length());
            headers.add(entry.getKey());
        }
        List<List<String>> elements = new ArrayList<>(tmp.size());
        // build header
        for (LinkedHashMap<String, Object> linkedHashMap : linkedHashMaps) {
            List<String> element = new ArrayList<>(linkedHashMap.size());
            for (Object value : linkedHashMap.values()) {
                String valueStr = value.toString();
                maxLen = Math.max(maxLen, valueStr.length());
                element.add(valueStr);
            }
            elements.add(element);
        }
        final int elementLen = maxLen;
        StringBuilder stringBuilder = new StringBuilder(200);
        stringBuilder.append(headers.stream()
                .map(header -> generateString(header, elementLen, " "))
                .collect(Collectors.joining("|")));
        stringBuilder.append("\n");
        for (List<String> element : elements) {
            stringBuilder.append(element.stream()
                    .map(lement -> generateString("", elementLen, "-"))
                    .collect(Collectors.joining("|")));
            stringBuilder.append("\n");
            stringBuilder.append(element.stream()
                    .map(e -> generateString(e, elementLen, " "))
                    .collect(Collectors.joining("|")));
            stringBuilder.append("\n");
        }
        return String.format("```%s```", stringBuilder);
    }

    private String generateString(String value, int len, String supplement) {
        StringBuilder stringBuilder = new StringBuilder(len);
        stringBuilder.append(value);
        for (int i = 0; i < len - stringBuilder.length(); i++) {
            stringBuilder.append(supplement);
        }
        return stringBuilder.toString();
    }
}
