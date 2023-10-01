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

package org.apache.dolphinscheduler.plugin.alert.feishu.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 *     https://open.feishu.cn/document/home/introduction-to-custom-app-development/self-built-application-development-process
 *     https://open.feishu.cn/document/faq/trouble-shooting/how-to-enable-bot-ability
 *     https://open.feishu.cn/document/server-docs/im-v1/message/create?appId=cli_a59c4fb4e838500e
 * </p>
 */
@Slf4j
public class FeiShuPersonalWorkSender {

    private FeiShuPersonalWorkAccessToken accessToken;
    private String appId;
    private String appSecret;
    private String receiveIdType;
    private String receiveId;

    public FeiShuPersonalWorkSender(Map<String, String> config) {
        appId = config.get(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_ID);
        appSecret = config.get(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_SECRET);
        receiveIdType = config.get(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID_TYPE);
        receiveId = config.get(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID);
    }

    public void queryAndSetAccessToken() throws IOException, URISyntaxException {
        CloseableHttpClient httpClient = getDefaultClient();
        Map<String, String> params = generateAccessParams();
        final String accessTokenUrl = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
        HttpPost httpPost = constructHttpPost(accessTokenUrl, JSONUtils.toJsonString(params), null);

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

            FeiShuAccessTokenResponse tokenResponse = JSONUtils.parseObject(resp, FeiShuAccessTokenResponse.class);
            if (tokenResponse == null) {
                log.info(String.format("FeiShu Personal Work Sender get access token failed, resp: %s", resp));
                return;
            }

            if (tokenResponse.getTenantAccessToken() == null || tokenResponse.getTenantAccessToken().isEmpty()) {
                log.info(String.format("FeiShu Personal Work Sender get access token failed, resp: %s", resp));
                return;
            }

            log.info("FeiShu Personal Work Sender get access token succeed");
            FeiShuPersonalWorkAccessToken token = new FeiShuPersonalWorkAccessToken();
            token.setTenantAccessToken(tokenResponse.getTenantAccessToken());
            token.setStartTime(System.currentTimeMillis());
            token.setExpireTime(System.currentTimeMillis() + tokenResponse.getExpire());
            this.accessToken = token;
        } finally {
            httpClient.close();
        }
    }

    private Map<String, String> generateAccessParams() {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("app_secret", appSecret);
        return params;
    }

    public AlertResult sendPersonalWorkMsg(AlertData alertData) {
        AlertResult alertResult;
        try {
            queryAndSetAccessToken();
            String resp = sendMsg(alertData);
            return checkSendPersonalWorkMsgResult(resp);
        } catch (Exception e) {
            log.info("send FeiShu personal work alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send FeiShu personal work alert fail.");
        }
        return alertResult;

    }

    private AlertResult checkSendPersonalWorkMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (null == result) {
            alertResult.setMessage("send FeiShu personal work msg error, FeiShu server resp is null");
            log.info("send FeiShu personal work msg error, FeiShu server resp is null");
            return alertResult;
        }
        FeiShuPersonalWorkResponse sendMsgResponse =
                JSONUtils.parseObject(result, FeiShuPersonalWorkResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send FeiShu personal work msg fail");
            log.info(String.format("send FeiShu personal work resp error, resp : %s", result));
            return alertResult;
        }
        if (sendMsgResponse.getCode() != 0) {
            alertResult.setMessage(
                    String.format("send FeiShu personal work msg fail, code is %d", sendMsgResponse.getCode()));
            log.info(String.format("send FeiShu personal work resp error, resp : %s", result));
            return alertResult;
        }
        alertResult.setStatus("true");
        alertResult.setMessage("send FeiShu personal work msg success");
        log.info("send FeiShu personal work msg success");
        return alertResult;
    }

    private String sendMsg(AlertData alertData) throws IOException, URISyntaxException {
        String msg = generateContentJson(alertData);

        final String accessTokenUrl = "https://open.feishu.cn/open-apis/im/v1/messages";
        HttpPost httpPost = constructHttpPost(accessTokenUrl, msg, new HashMap<String, String>() {

            {
                put("receive_id_type", receiveIdType);
            }
        });
        httpPost.setHeader("Authorization", String.format("Bearer %s", accessToken.getTenantAccessToken()));

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
            log.info("FeiShu personal work send msg :{}, resp: {}", msg, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    private static HttpPost constructHttpPost(String url, String msg,
                                              HashMap<String, String> pathParams) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (pathParams != null) {
            List<NameValuePair> pairs = new ArrayList<>();
            pathParams.forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
            uriBuilder.addParameters(pairs);
        }
        HttpPost post = new HttpPost(uriBuilder.build());
        StringEntity entity = new StringEntity(msg, ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    private String generateContentJson(AlertData alertData) {
        Map<String, String> text = new HashMap<>();
        text.put("text", formatContent(alertData));
        Map<String, String> body = new HashMap<>();
        body.put("content", JSONUtils.toJsonString(text));
        body.put("msg_type", "text");
        body.put("receive_id", receiveId);
        return JSONUtils.toJsonString(body);
    }

    private static String formatContent(AlertData alertData) {
        if (alertData.getContent() != null) {

            List<Map> list = JSONUtils.toList(alertData.getContent(), Map.class);
            if (CollectionUtils.isEmpty(list)) {
                return alertData.getTitle() + "\n" + alertData.getContent();
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(alertData.getTitle());
            contents.append("\n");
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
        return null;
    }

    public FeiShuPersonalWorkAccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(FeiShuPersonalWorkAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    static final class FeiShuPersonalWorkResponse {

        private int code;
        private String msg;
        private Object data;

        public void setCode(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
        public String getMsg() {
            return msg;
        }

        public void setData(Object data) {
            this.data = data;
        }
        public Object getData() {
            return data;
        }

    }

    static final class FeiShuAccessTokenResponse {

        private int code;
        private String msg;

        @JsonProperty("tenant_access_token")
        private String tenantAccessToken;
        private int expire;

        public FeiShuAccessTokenResponse() {
        }

        public void setCode(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
        public String getMsg() {
            return msg;
        }

        public void setTenantAccessToken(String tenantAccessToken) {
            this.tenantAccessToken = tenantAccessToken;
        }
        public String getTenantAccessToken() {
            return tenantAccessToken;
        }

        public void setExpire(int expire) {
            this.expire = expire;
        }
        public int getExpire() {
            return expire;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FeiShuAccessTokenResponse that = (FeiShuAccessTokenResponse) o;
            return code == that.code && expire == that.expire && Objects.equals(msg, that.msg)
                    && Objects.equals(tenantAccessToken, that.tenantAccessToken);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, msg, tenantAccessToken, expire);
        }
    }
}
