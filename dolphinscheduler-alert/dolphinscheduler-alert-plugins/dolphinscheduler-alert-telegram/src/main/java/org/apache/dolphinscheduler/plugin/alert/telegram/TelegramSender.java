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

package org.apache.dolphinscheduler.plugin.alert.telegram;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.HttpServiceRetryStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
public final class TelegramSender {

    private static final String BOT_TOKEN_REGEX = "{botToken}";

    private final String chatId;

    private final String parseMode;

    private final Boolean enableProxy;

    private String botToken;

    private String url;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    TelegramSender(Map<String, String> config) {
        url = config.get(TelegramParamsConstants.NAME_TELEGRAM_WEB_HOOK);
        botToken = config.get(TelegramParamsConstants.NAME_TELEGRAM_BOT_TOKEN);
        chatId = config.get(TelegramParamsConstants.NAME_TELEGRAM_CHAT_ID);
        parseMode = config.get(TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE);
        if (url == null || url.isEmpty()) {
            url = TelegramAlertConstants.TELEGRAM_PUSH_URL.replace(BOT_TOKEN_REGEX, botToken);
        } else {
            url = url.replace(BOT_TOKEN_REGEX, botToken);
        }
        enableProxy = Boolean.valueOf(config.get(TelegramParamsConstants.NAME_TELEGRAM_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(TelegramParamsConstants.NAME_TELEGRAM_PORT));
            proxy = config.get(TelegramParamsConstants.NAME_TELEGRAM_PROXY);
            user = config.get(TelegramParamsConstants.NAME_TELEGRAM_USER);
            password = config.get(TelegramParamsConstants.NAME_TELEGRAM_PASSWORD);
        }
    }

    /**
     * sendMessage
     *
     * @param alertData alert data
     * @return alert result
     * @see <a href="https://core.telegram.org/bots/api#sendmessage">telegram bot api</a>
     */
    public AlertResult sendMessage(AlertData alertData) {
        AlertResult result;
        try {
            String resp = sendInvoke(alertData.getTitle(), alertData.getContent());
            result = parseRespToResult(resp);
        } catch (Exception e) {
            log.warn("send telegram alert msg exception : {}", e.getMessage());
            result = new AlertResult();
            result.setSuccess(false);
            result.setMessage(String.format("send telegram alert fail. %s", e.getMessage()));
        }
        return result;
    }

    private AlertResult parseRespToResult(String resp) {
        AlertResult result = new AlertResult();
        result.setSuccess(false);
        if (null == resp || resp.isEmpty()) {
            result.setMessage("send telegram msg error. telegram server resp is empty");
            return result;
        }
        TelegramSendMsgResponse response = JSONUtils.parseObject(resp, TelegramSendMsgResponse.class);
        if (null == response) {
            result.setMessage("send telegram msg fail.");
            return result;
        }
        if (!response.isOk()) {
            result.setMessage(String.format("send telegram alert fail. telegram server error_code: %d, description: %s",
                    response.errorCode, response.description));
        } else {
            result.setSuccess(true);
            result.setMessage("send telegram msg success.");
        }
        return result;
    }

    private String sendInvoke(String title, String content) throws IOException {
        HttpPost httpPost = buildHttpPost(url, buildMsgJsonStr(content));
        CloseableHttpClient httpClient;
        if (Boolean.TRUE.equals(enableProxy)) {
            if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
                httpClient = getProxyClient(proxy, port, user, password);
            } else {
                httpClient = getDefaultClient();
            }
            RequestConfig rcf = getProxyConfig(proxy, port);
            httpPost.setConfig(rcf);
        } else {
            httpClient = getDefaultClient();
        }

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("Telegram send title :{},content : {}, resp: {}", title, content, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    private String buildMsgJsonStr(String content) {
        Map<String, Object> items = new HashMap<>();
        items.put("chat_id", chatId);
        if (!isTextParseMode()) {
            items.put("parse_mode", parseMode);
        }
        items.put("text", content);
        return JSONUtils.toJsonString(items);
    }

    private boolean isTextParseMode() {
        return null == parseMode || TelegramAlertConstants.PARSE_MODE_TXT.equals(parseMode);
    }

    static class TelegramSendMsgResponse {

        @JsonProperty("ok")
        private Boolean ok;
        @JsonProperty("error_code")
        private Integer errorCode;
        @JsonProperty("description")
        private String description;
        @JsonProperty("result")
        private Object result;

        public boolean isOk() {
            return null != ok && ok;
        }

        public Boolean getOk() {
            return ok;
        }

        @JsonProperty("ok")
        public void setOk(Boolean ok) {
            this.ok = ok;
        }

        @JsonProperty("error_code")
        public void setErrorCode(Integer errorCode) {
            this.errorCode = errorCode;
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public String getDescription() {
            return description;
        }

        @JsonProperty("description")
        public void setDescription(String description) {
            this.description = description;
        }

        public Object getResult() {
            return result;
        }

        @JsonProperty("result")
        public void setResult(Object result) {
            this.result = result;
        }
    }

    private static HttpPost buildHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        return post;
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();
    }

    private static CloseableHttpClient getProxyClient(String proxy, int port, String user, String password) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(httpProxy), new UsernamePasswordCredentials(user, password));
        return HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy)
                .setDefaultCredentialsProvider(provider).build();
    }

    private static RequestConfig getProxyConfig(String proxy, int port) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setProxy(httpProxy).build();
    }
}
