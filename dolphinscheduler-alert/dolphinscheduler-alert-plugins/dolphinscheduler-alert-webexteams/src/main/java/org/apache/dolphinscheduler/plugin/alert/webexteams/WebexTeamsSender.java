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

package org.apache.dolphinscheduler.plugin.alert.webexteams;

import org.apache.dolphinscheduler.alert.api.AlertData;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
public final class WebexTeamsSender {

    private final String botAccessToken;
    private final String roomId;
    private final String toPersonId;
    private final String toPersonEmail;
    private final String atSomeoneInRoom;
    private final WebexTeamsDestination destination;

    public WebexTeamsSender(Map<String, String> config) {
        botAccessToken = config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_BOT_ACCESS_TOKEN);
        roomId = config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_ROOM_ID);
        toPersonId = config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_ID);
        toPersonEmail = config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_EMAIL);
        atSomeoneInRoom = config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_AT_SOMEONE_IN_ROOM);
        destination = WebexTeamsDestination.of(config.get(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_DESTINATION));
        Preconditions.checkArgument(!Objects.isNull(botAccessToken), "WebexTeams bot access token can not be null");
        Preconditions.checkArgument(!Objects.isNull(destination), "WebexTeams message destination can not be null");
        Preconditions.checkArgument(
                (!Objects.isNull(roomId) || !Objects.isNull(toPersonId) || !Objects.isNull(toPersonEmail)),
                "WebexTeams message destination could not be determined. Provide only one destination in the roomId, toPersonEmail, or toPersonId field");
    }

    public AlertResult sendWebexTeamsAlter(AlertData alertData) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);
        alertResult.setMessage("send webex teams alert fail.");

        try {
            send(alertResult, alertData);
        } catch (Exception e) {
            log.info("send webex teams alert exception : {}", e.getMessage());
        }

        return alertResult;
    }

    private void send(AlertResult alertResult, AlertData alertData) throws IOException {
        CloseableHttpClient httpClient =
                HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();

        try {
            WebexMessage message = getMessage(alertData);
            HttpPost httpPost = constructHttpPost(message, botAccessToken);
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            try {
                if (statusCode == HttpStatus.SC_OK) {
                    alertResult.setSuccess(true);
                    alertResult.setMessage("send webex teams alert success");
                } else {
                    alertResult.setMessage(String.format(
                            "send webex teams alert error, message: %s, statusCode: %s, responseContent: %s", message,
                            statusCode, responseContent));
                    log.info("send webex teams alert fail, statusCode : {}", statusCode);
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            log.info("send webex teams alert exception : {}", e.getMessage());
        } finally {
            httpClient.close();
        }
    }

    private WebexMessage getMessage(AlertData alertData) {
        WebexMessage message = new WebexMessage();
        String formatContent = formatContent(alertData);

        switch (destination) {
            case ROOM_ID: {
                message.setRoomId(roomId);
                if (!Objects.isNull(atSomeoneInRoom)) {
                    formatContent = addAtPersonEmailInRoom(formatContent, atSomeoneInRoom);
                }
                break;
            }
            case PERSON_EMAIL: {
                message.setToPersonEmail(toPersonEmail);
                break;
            }
            case PERSON_ID: {
                message.setToPersonId(toPersonId);
                break;
            }
        }

        message.setMarkdown(formatContent);
        return message;
    }

    private static HttpPost constructHttpPost(WebexMessage message, String botAccessToken) {
        HttpPost post = new HttpPost(WebexTeamsParamsConstants.WEBEX_TEAMS_API);
        StringEntity entity = new StringEntity(JSONUtils.toJsonString(message), StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        post.addHeader("Authorization", "Bearer " + botAccessToken);
        return post;
    }

    public static String addAtPersonEmailInRoom(String formatContent, String atPersonEmailInRoom) {
        String[] emailArr = atPersonEmailInRoom.split(",");
        StringBuilder formatContentBuilder = new StringBuilder(formatContent);
        for (String email : emailArr) {
            formatContentBuilder.append(" <@personEmail:").append(email).append(">");
        }

        return formatContentBuilder.toString();
    }

    public static String formatContent(AlertData alertData) {
        if (alertData.getContent() != null) {
            List<Map> list = JSONUtils.toList(alertData.getContent(), Map.class);
            if (list.isEmpty()) {
                return alertData.getTitle() + alertData.getContent();
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`%n", alertData.getTitle()));
            for (Map map : list) {
                for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>) map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue() == null ? "" : entry.getValue().toString();
                    contents.append(key).append(":").append(value);
                    contents.append("\n");
                }
            }

            return contents.toString();
        }

        return null;
    }
}
