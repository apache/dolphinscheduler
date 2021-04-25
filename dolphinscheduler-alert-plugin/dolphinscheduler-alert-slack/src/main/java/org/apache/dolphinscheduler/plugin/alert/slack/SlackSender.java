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

import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SlackSender {

    private static final Logger logger = LoggerFactory.getLogger(SlackSender.class);

    private String webHookUrl;

    private String botName;

    public SlackSender(Map<String, String> slackAlertParam) {
        webHookUrl = slackAlertParam.get(SlackParamsConstants.SLACK_WEN_HOOK_URL_NAME);
        botName = slackAlertParam.get(SlackParamsConstants.SLACK_BOT_NAME);
        Preconditions.checkArgument(!Objects.isNull(webHookUrl), "SlackWebHookURL can not be null");
        Preconditions.checkArgument(webHookUrl.startsWith("https://hooks.slack.com/services/"), "SlackWebHookURL invalidate");
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
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put(SlackParamsConstants.SLACK_BOT, botName);
            paramMap.put(SlackParamsConstants.text, title + "\n" + content);

            HttpPost httpPost = new HttpPost(webHookUrl);
            httpPost.setEntity(new StringEntity(JSONUtils.toJsonString(paramMap), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            logger.error("Send message to slack error.", e);
            return "System Exception";
        }
    }
}
