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

package org.apache.dolphinscheduler.plugin.alert.dingtalk;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.HttpServiceRetryStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     https://open.dingtalk.com/document/robots/custom-robot-access
 *     https://open.dingtalk.com/document/robots/customize-robot-security-settings
 * </p>
 */
@Slf4j
public final class DingTalkSender {

    private final String url;
    private final String keyword;
    private final String secret;
    private String msgType;

    private final String atMobiles;
    private final String atUserIds;
    private final Boolean atAll;

    private final Boolean enableProxy;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    DingTalkSender(Map<String, String> config) {
        url = config.get(DingTalkParamsConstants.NAME_DING_TALK_WEB_HOOK);
        keyword = config.get(DingTalkParamsConstants.NAME_DING_TALK_KEYWORD);
        secret = config.get(DingTalkParamsConstants.NAME_DING_TALK_SECRET);
        msgType = config.get(DingTalkParamsConstants.NAME_DING_TALK_MSG_TYPE);

        atMobiles = config.get(DingTalkParamsConstants.NAME_DING_TALK_AT_MOBILES);
        atUserIds = config.get(DingTalkParamsConstants.NAME_DING_TALK_AT_USERIDS);
        atAll = Boolean.valueOf(config.get(DingTalkParamsConstants.NAME_DING_TALK_AT_ALL));

        enableProxy = Boolean.valueOf(config.get(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(DingTalkParamsConstants.NAME_DING_TALK_PORT));
            proxy = config.get(DingTalkParamsConstants.NAME_DING_TALK_PROXY);
            user = config.get(DingTalkParamsConstants.NAME_DING_TALK_USER);
            password = config.get(DingTalkParamsConstants.NAME_DING_TALK_PASSWORD);
        }
    }

    private static HttpPost constructHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    private static CloseableHttpClient getProxyClient(String proxy, int port, String user, String password) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(httpProxy), new UsernamePasswordCredentials(user, password));
        return HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy)
                .setDefaultCredentialsProvider(provider).build();
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();
    }

    private static RequestConfig getProxyConfig(String proxy, int port) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setProxy(httpProxy).build();
    }

    private AlertResult checkSendDingTalkSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);

        if (null == result) {
            alertResult.setMessage("send ding talk msg error");
            log.info("send ding talk msg error,ding talk server resp is null");
            return alertResult;
        }
        DingTalkSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, DingTalkSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send ding talk msg fail");
            log.info("send ding talk msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setSuccess(true);
            alertResult.setMessage("send ding talk msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send ding talk msg error : %s", sendMsgResponse.getErrmsg()));
        log.info("alert send ding talk msg error : {}", sendMsgResponse.getErrmsg());
        return alertResult;
    }

    /**
     * send dingtalk msg handler
     *
     * @param title title
     * @param content content
     * @return
     */
    public AlertResult sendDingTalkMsg(String title, String content) {
        AlertResult alertResult;
        try {
            String resp = sendMsg(title, content);
            return checkSendDingTalkSendMsgResult(resp);
        } catch (Exception e) {
            log.info("send ding talk alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setSuccess(false);
            alertResult.setMessage("send ding talk alert fail.");
        }
        return alertResult;
    }

    private String sendMsg(String title, String content) throws IOException {

        String msg = generateMsgJson(title, content);

        HttpPost httpPost = constructHttpPost(
                org.apache.commons.lang3.StringUtils.isBlank(secret) ? url : generateSignedUrl(), msg);

        CloseableHttpClient httpClient;
        if (Boolean.TRUE.equals(enableProxy)) {
            httpClient = getProxyClient(proxy, port, user, password);
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
            log.info("Ding Talk send msg :{}, resp: {}", msg, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    /**
     * generate msg json
     *
     * @param title title
     * @param content content
     * @return msg
     */
    private String generateMsgJson(String title, String content) {
        if (org.apache.commons.lang3.StringUtils.isBlank(msgType)) {
            msgType = DingTalkParamsConstants.DING_TALK_MSG_TYPE_TEXT;
        }
        Map<String, Object> items = new HashMap<>();
        items.put("msgtype", msgType);
        Map<String, Object> text = new HashMap<>();
        items.put(msgType, text);

        if (DingTalkParamsConstants.DING_TALK_MSG_TYPE_MARKDOWN.equals(msgType)) {
            generateMarkdownMsg(title, content, text);
        } else {
            generateTextMsg(title, content, text);
        }

        setMsgAt(items);
        return JSONUtils.toJsonString(items);

    }

    /**
     * generate text msg
     *
     * @param title title
     * @param content content
     * @param text text
     */
    private void generateTextMsg(String title, String content, Map<String, Object> text) {
        StringBuilder builder = new StringBuilder(title);
        builder.append("\n");
        builder.append(content);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(keyword)) {
            builder.append(" ");
            builder.append(keyword);
        }
        byte[] byt = StringUtils.getBytesUtf8(builder.toString());
        String txt = StringUtils.newStringUtf8(byt);
        text.put("content", txt);
    }

    /**
     * generate markdown msg
     *
     * @param title title
     * @param content content
     * @param text text
     */
    private void generateMarkdownMsg(String title, String content, Map<String, Object> text) {
        StringBuilder builder = new StringBuilder(content);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(keyword)) {
            builder.append(" ");
            builder.append(keyword);
        }
        builder.append("\n\n");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(atMobiles)) {
            Arrays.stream(atMobiles.split(",")).forEach(value -> {
                builder.append("@");
                builder.append(value);
                builder.append(" ");
            });
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(atUserIds)) {
            Arrays.stream(atUserIds.split(",")).forEach(value -> {
                builder.append("@");
                builder.append(value);
                builder.append(" ");
            });
        }

        byte[] byt = StringUtils.getBytesUtf8(builder.toString());
        String txt = StringUtils.newStringUtf8(byt);
        text.put("title", title);
        text.put("text", txt);
    }

    /**
     * configure msg @person
     *
     * @param items items
     */
    private void setMsgAt(Map<String, Object> items) {
        Map<String, Object> at = new HashMap<>();

        String[] atMobileArray =
                org.apache.commons.lang3.StringUtils.isNotBlank(atMobiles) ? atMobiles.split(",")
                        : new String[0];
        String[] atUserArray =
                org.apache.commons.lang3.StringUtils.isNotBlank(atUserIds) ? atUserIds.split(",")
                        : new String[0];
        boolean isAtAll = Objects.isNull(atAll) ? false : atAll;

        at.put("atMobiles", atMobileArray);
        at.put("atUserIds", atUserArray);
        at.put("isAtAll", isAtAll);

        items.put("at", at);
    }

    /**
     * generate sign url
     *
     * @return sign url
     */
    private String generateSignedUrl() {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        String sign = org.apache.commons.lang3.StringUtils.EMPTY;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("generate sign error, message:{}", e);
        }
        return url + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    @Getter
    @Setter
    static final class DingTalkSendMsgResponse {

        private Integer errcode;
        private String errmsg;

        public DingTalkSendMsgResponse() {
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DingTalkSendMsgResponse)) {
                return false;
            }
            final DingTalkSendMsgResponse other = (DingTalkSendMsgResponse) o;
            final Object this$errcode = this.getErrcode();
            final Object other$errcode = other.getErrcode();
            if (this$errcode == null ? other$errcode != null : !this$errcode.equals(other$errcode)) {
                return false;
            }
            final Object this$errmsg = this.getErrmsg();
            final Object other$errmsg = other.getErrmsg();
            if (this$errmsg == null ? other$errmsg != null : !this$errmsg.equals(other$errmsg)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $errcode = this.getErrcode();
            result = result * PRIME + ($errcode == null ? 43 : $errcode.hashCode());
            final Object $errmsg = this.getErrmsg();
            result = result * PRIME + ($errmsg == null ? 43 : $errmsg.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "DingTalkSender.DingTalkSendMsgResponse(errcode=" + this.getErrcode() + ", errmsg="
                    + this.getErrmsg() + ")";
        }
    }
}
