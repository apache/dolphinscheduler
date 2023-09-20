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

package org.apache.dolphinscheduler.plugin.alert.dingtalk.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     https://open.dingtalk.com/document/orgapp/enterprise-created-chatbot
 *     https://open.dingtalk.com/document/orgapp/chatbot-sends-queries-and-withdraws-one-on-one-chat-messages
 * </p>
 */
@Slf4j
public class DingTalkPersonalWorkSender {

    private DingTalkPersonWorkAccessToken accessToken;
    private String appKey;
    private String appSecret;
    private String robotCode;
    private List<String> userIds;
    private String msgKey;

    public DingTalkPersonalWorkSender(Map<String, String> config) {
        appKey = config.get(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_KEY);
        appSecret = config.get(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_SECRET);
        robotCode = config.get(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_ROBOT_CODE);
        String userIdsStr = config.get(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_USER_IDS);
        userIds = Arrays.asList(StringUtils.isNotBlank(userIdsStr) ? userIdsStr.split(",")
                : new String[0]);
        msgKey = config.get(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_MSG_KEY);
    }

    public AlertResult sendPersonalWorkMsg(String title, String content) {
        AlertResult alertResult;
        try {
            // TODO: query access token should not be frequently, there should be optimized
            // see {@link https://open.dingtalk.com/document/orgapp/obtain-the-access_token-of-an-internal-app}
            queryAndSetAccessToken();
            String resp = sendMsg(title, content);
            return checkSendPersonalWorkMsgResult(resp);
        } catch (Exception e) {
            log.info("send ding talk alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send ding talk alert fail.");
        }
        return alertResult;
    }

    private AlertResult checkSendPersonalWorkMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (null == result) {
            alertResult.setMessage("send ding talk personal work msg error, ding talk server resp is null");
            log.info("send ding talk personal work msg error, ding talk server resp is null");
            return alertResult;
        }
        DingTalkPersonalWorkResponse sendMsgResponse =
                JSONUtils.parseObject(result, DingTalkPersonalWorkResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send ding talk personal work msg fail");
            log.info(String.format("send ding talk personal work resp error, resp : %s", result));
            return alertResult;
        }
        if (sendMsgResponse.getProcessQueryKey() == null || sendMsgResponse.getProcessQueryKey().isEmpty()) {
            alertResult.setMessage("send ding talk personal work msg fail, processQueryKey is empty");
            log.info(String.format("send ding talk personal work resp error, resp : %s", result));
            return alertResult;
        }
        if (sendMsgResponse.getInvalidStaffIdList() != null && sendMsgResponse.getInvalidStaffIdList().size() != 0) {
            alertResult.setMessage("send ding talk personal work msg fail, there are invalid users");
            log.info(String.format("send ding talk personal work error, there are invalid users: %s",
                    sendMsgResponse.getInvalidStaffIdList()));
            return alertResult;
        }
        if (sendMsgResponse.getFlowControlledStaffIdList() != null
                && sendMsgResponse.getFlowControlledStaffIdList().size() != 0) {
            alertResult.setMessage("send ding talk personal work msg fail, there are flow controlled users");
            log.info(String.format("send ding talk personal work error, there are flow controlled users: %s",
                    sendMsgResponse.getFlowControlledStaffIdList()));
            return alertResult;
        }
        alertResult.setStatus("true");
        alertResult.setMessage("send ding talk personal work msg success");
        log.info("send ding talk personal work msg success");
        return alertResult;
    }

    private String sendMsg(String title, String content) throws IOException {
        String msg = generateMsgJson(title, content);

        final String accessTokenUrl = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";
        HttpPost httpPost = constructHttpPost(accessTokenUrl, msg);
        httpPost.setHeader("x-acs-dingtalk-access-token", accessToken.getAccessToken());

        CloseableHttpClient httpClient = getDefaultClient();;

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("Ding Talk Personal Work send msg :{}, resp: {}", msg, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    private String generateMsgJson(String title, String content) {
        if (org.apache.commons.lang3.StringUtils.isBlank(msgKey)) {
            msgKey = DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_TEXT;
        }
        Map<String, Object> items = new HashMap<>();
        items.put("msgKey", msgKey);
        items.put("userIds", userIds);
        items.put("robotCode", robotCode);

        Map<String, String> text = new HashMap<>();
        String msgParam = "";
        if (DingTalkPersonalWorkParamConstants.DING_TALK_PERSONAL_WORK_MSG_KEY_TEXT.equals(msgKey)) {
            msgParam = generateSampleTextMsg(title, content, text);
        } else {
            msgParam = generateSampleMarkdownMsg(title, content, text);
        }
        items.put("msgParam", msgParam);

        return JSONUtils.toJsonString(items);
    }

    private String generateSampleMarkdownMsg(String title, String content, Map<String, String> text) {
        text.put("title", title);
        text.put("text", content);
        return JSONUtils.toJsonString(text);
    }

    private String generateSampleTextMsg(String title, String content, Map<String, String> text) {
        StringBuilder builder = new StringBuilder(title);
        builder.append("\n");
        builder.append(content);
        byte[] byt = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(builder.toString());
        String txt = org.apache.commons.codec.binary.StringUtils.newStringUtf8(byt);
        text.put("content", txt);
        return JSONUtils.toJsonString(text);
    }

    public void queryAndSetAccessToken() throws IOException {
        CloseableHttpClient httpClient = getDefaultClient();
        Map<String, String> params = generateAccessParams();
        final String accessTokenUrl = "https://api.dingtalk.com/v1.0/oauth2/accessToken";
        HttpPost httpPost = constructHttpPost(accessTokenUrl, JSONUtils.toJsonString(params));

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }

            DingTalkAccessTokenResponse tokenResponse = JSONUtils.parseObject(resp, DingTalkAccessTokenResponse.class);
            if (tokenResponse == null) {
                log.info(String.format("Ding Talk Personal Work Sender get access token failed, resp: %s", resp));
                return;
            }

            if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isEmpty()) {
                log.info(String.format("Ding Talk Personal Work Sender get access token failed, resp: %s", resp));
                return;
            }

            log.info("Ding Talk Personal Work Sender get access token succeed");
            DingTalkPersonWorkAccessToken token = new DingTalkPersonWorkAccessToken();
            token.setAccessToken(tokenResponse.getAccessToken());
            token.setStartTime(System.currentTimeMillis());
            token.setExpireTime(System.currentTimeMillis() + tokenResponse.getExpireIn());
            this.accessToken = token;
        } finally {
            httpClient.close();
        }
    }

    public DingTalkPersonWorkAccessToken getAccessToken() {
        return accessToken;
    }

    private Map<String, String> generateAccessParams() {
        Map<String, String> params = new HashMap<>();
        params.put("appKey", appKey);
        params.put("appSecret", appSecret);
        return params;
    }

    private static HttpPost constructHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    static final class DingTalkAccessTokenResponse {

        private String accessToken;
        private Integer expireIn;

        public DingTalkAccessTokenResponse() {
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Integer getExpireIn() {
            return expireIn;
        }

        public void setExpireIn(Integer expireIn) {
            this.expireIn = expireIn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DingTalkAccessTokenResponse that = (DingTalkAccessTokenResponse) o;
            return Objects.equals(accessToken, that.accessToken) && Objects.equals(expireIn, that.expireIn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accessToken, expireIn);
        }
    }

    static final class DingTalkPersonalWorkResponse {

        private String processQueryKey;
        private List<String> invalidStaffIdList;
        private List<String> flowControlledStaffIdList;

        public DingTalkPersonalWorkResponse() {
        }

        public String getProcessQueryKey() {
            return processQueryKey;
        }

        public void setProcessQueryKey(String processQueryKey) {
            this.processQueryKey = processQueryKey;
        }

        public List<String> getInvalidStaffIdList() {
            return invalidStaffIdList;
        }

        public void setInvalidStaffIdList(List<String> invalidStaffIdList) {
            this.invalidStaffIdList = invalidStaffIdList;
        }

        public List<String> getFlowControlledStaffIdList() {
            return flowControlledStaffIdList;
        }

        public void setFlowControlledStaffIdList(List<String> flowControlledStaffIdList) {
            this.flowControlledStaffIdList = flowControlledStaffIdList;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DingTalkPersonalWorkResponse that = (DingTalkPersonalWorkResponse) o;
            return Objects.equals(processQueryKey, that.processQueryKey)
                    && Objects.equals(invalidStaffIdList, that.invalidStaffIdList)
                    && Objects.equals(flowControlledStaffIdList, that.flowControlledStaffIdList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(processQueryKey, invalidStaffIdList, flowControlledStaffIdList);
        }
    }
}
