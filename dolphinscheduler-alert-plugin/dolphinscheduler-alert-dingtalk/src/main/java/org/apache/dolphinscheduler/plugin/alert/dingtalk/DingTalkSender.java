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

import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ding Talk Sender
 */
public class DingTalkSender {
    private static final Logger logger = LoggerFactory.getLogger(DingTalkSender.class);

    private String url;

    private String keyword;

    private Boolean enableProxy;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    DingTalkSender(Map<String, String> config) {
        url = config.get(DingTalkParamsConstants.NAME_DING_TALK_WEB_HOOK);
        keyword = config.get(DingTalkParamsConstants.NAME_DING_TALK_KEYWORD);
        enableProxy = Boolean.valueOf(config.get(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(DingTalkParamsConstants.NAME_DING_TALK_PORT));
            proxy = config.get(DingTalkParamsConstants.NAME_DING_TALK_PROXY);
            user = config.get(DingTalkParamsConstants.DING_TALK_USER);
            password = config.get(DingTalkParamsConstants.NAME_DING_TALK_PASSWORD);
        }

    }

    private static HttpPost constructHttpPost(String url, String msg, String charset) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, charset);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    private static CloseableHttpClient getProxyClient(String proxy, int port, String user, String password) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(httpProxy), new UsernamePasswordCredentials(user, password));
        return HttpClients.custom().setDefaultCredentialsProvider(provider).build();
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    private static RequestConfig getProxyConfig(String proxy, int port) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setProxy(httpProxy).build();
    }

    private static String textToJsonString(String text) {
        Map<String, Object> items = new HashMap<>();
        items.put("msgtype", "text");
        Map<String, String> textContent = new HashMap<>();
        byte[] byt = StringUtils.getBytesUtf8(text);
        String txt = StringUtils.newStringUtf8(byt);
        textContent.put("content", txt);
        items.put("text", textContent);
        return JSONUtils.toJsonString(items);
    }

    private static AlertResult checkSendDingTalkSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (null == result) {
            alertResult.setMessage("send ding talk msg error");
            logger.info("send ding talk msg error,ding talk server resp is null");
            return alertResult;
        }
        DingTalkSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, DingTalkSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send ding talk msg fail");
            logger.info("send ding talk msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("send ding talk msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send ding talk msg error : %s", sendMsgResponse.getErrmsg()));
        logger.info("alert send ding talk msg error : {}", sendMsgResponse.getErrmsg());
        return alertResult;
    }

    public AlertResult sendDingTalkMsg(String title, String content) {
        AlertResult alertResult;
        try {
            String resp = sendMsg(title, content);
            return checkSendDingTalkSendMsgResult(resp);
        } catch (Exception e) {
            logger.info("send ding talk alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send ding talk alert fail.");
        }
        return alertResult;
    }

    private String sendMsg(String title, String content) throws IOException {

        String msgToJson = textToJsonString(title + content + "#" + keyword);
        HttpPost httpPost = constructHttpPost(url, msgToJson, "UTF-8");

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
                resp = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("Ding Talk send title :{},content : {}, resp: {}", title, content, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    public static class DingTalkSendMsgResponse {
        private Integer errcode;
        private String errmsg;

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }

}
