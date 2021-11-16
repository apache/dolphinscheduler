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

package org.apache.dolphinscheduler.plugin.alert.ewechat;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.plugin.alert.ewechat.exception.EWeChatAlertException;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enterprise WeChat Sender
 */
public final class EWeChatSender {
    private static final Logger logger = LoggerFactory.getLogger(EWeChatSender.class);

    private String url;

    private Boolean enableProxy;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    EWeChatSender(Map<String, String> config) {
        url = config.get(EWeChatParamsConstants.NAME_EWE_CHAT_WEB_HOOK);
        enableProxy = Boolean.valueOf(config.get(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(EWeChatParamsConstants.NAME_EWE_CHAT_PORT));
            proxy = config.get(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY);
            user = config.get(EWeChatParamsConstants.NAME_EWE_CHAT_USER);
            password = config.get(EWeChatParamsConstants.NAME_EWE_CHAT_PASSWORD);
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
        items.put("msgtype", "markdown");
        Map<String, Object> textContent = new HashMap<>();
        byte[] byt = StringUtils.getBytesUtf8(text);
        String txt = StringUtils.newStringUtf8(byt);
        textContent.put("content", txt);
        items.put("markdown", textContent);
        return JSONUtils.toJsonString(items);
    }

    private static AlertResult checkSendEWeChatSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (null == result) {
            alertResult.setMessage("send enterprise wechat msg error");
            logger.info("send enterprise wechat msg error,enterprise we chat server resp is null");
            return alertResult;
        }
        EWeChatSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, EWeChatSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send enterprise we chat msg fail");
            logger.info("send enterprise wechat msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("send enterprise wechat msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send enterprise wechat msg error : %s", sendMsgResponse.getErrmsg()));
        logger.info("alert send enterprise wechat msg error : {}", sendMsgResponse.getErrmsg());
        return alertResult;
    }

    public AlertResult sendEWeChatMsg(String title, String content) {
        AlertResult alertResult;
        try {
            String resp = sendMsg(title, content);
            return checkSendEWeChatSendMsgResult(resp);
        } catch (Exception e) {
            logger.info("send enterprise wechat alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send enterprise wechat alert fail.");
        }
        return alertResult;
    }

    private String sendMsg(String title, String content) throws IOException {
        String data = markdownTableByAlert(title, content);
        String msg = textToJsonString(data);
        HttpPost httpPost = constructHttpPost(url, msg, EWeChatAlertConstants.CHARSET);

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
            logger.info("enterprise wechat send title :{},content : {}, resp: {}", title, content, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    public String markdownTableByAlert(String title, String content) {
        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
        if (null == mapItemsList || mapItemsList.isEmpty()) {
            logger.error("itemsList is null");
            throw new EWeChatAlertException("itemsList is null");
        }

        if (title.toLowerCase().contains(EWeChatAlertConstants.SUCCESS_FLAG)) {
            title = String.format(EWeChatAlertConstants.INFO_PATTERN, title);
        } else {
            title = String.format(EWeChatAlertConstants.ERROR_PATTERN, title);
        }
        StringBuilder contents = new StringBuilder(200);
        for (LinkedHashMap mapItems : mapItemsList) {
            Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
            StringBuilder t = new StringBuilder(title);
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                t.append(EWeChatAlertConstants.MARKDOWN_QUOTE);
                t.append(String.format(EWeChatAlertConstants.BOLD_PATTERN, entry.getKey()));
                t.append(EWeChatAlertConstants.COLON);
                t.append(String.format(EWeChatAlertConstants.COMMENT_PATTERN, entry.getValue()));
            }
            contents.append(t);
        }
        return contents.toString();
    }

    public String markdownShellByAlert(String title, String content) {
        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
        if (null == mapItemsList || mapItemsList.isEmpty()) {
            logger.error("itemsList is null");
            throw new EWeChatAlertException("itemsList is null");
        }
        StringBuilder contents = new StringBuilder(200);
        String pattern = " %-20s | %-40s |%n";
        for (LinkedHashMap mapItems : mapItemsList) {
            Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
            StringBuilder t = new StringBuilder();
            t.append(EWeChatAlertConstants.FIRST_DASH);
            t.append(String.format(EWeChatAlertConstants.ERROR_PATTERN, title));
            t.append(EWeChatAlertConstants.DASH_BREAK);
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                t.append(String.format(pattern, entry.getKey(), entry.getValue()));
            }
            t.append(EWeChatAlertConstants.DASH_BREAK);
            contents.append(t);
        }
        return contents.toString();

    }

    public static class EWeChatSendMsgResponse {
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
