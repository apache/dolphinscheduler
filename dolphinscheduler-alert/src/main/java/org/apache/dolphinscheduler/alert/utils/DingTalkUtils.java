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
package org.apache.dolphinscheduler.alert.utils;


import org.apache.dolphinscheduler.common.utils.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DingTalkUtils utils
 * support send msg to ding talk by robot message push function.
 * support proxy setting
 */
public class DingTalkUtils {
    public static final Logger logger = LoggerFactory.getLogger(DingTalkUtils.class);

    public static final boolean isEnableDingTalk = PropertyUtils.getBoolean(Constants.DINGTALK_ENABLE);
    private static final String dingTaskUrl = PropertyUtils.getString(Constants.DINGTALK_WEBHOOK);
    private static final String keyword = PropertyUtils.getString(Constants.DINGTALK_KEYWORD);
    private static final Boolean isEnableProxy = PropertyUtils.getBoolean(Constants.DINGTALK_PROXY_ENABLE);
    private static final String proxy = PropertyUtils.getString(Constants.DINGTALK_PROXY);
    private static final String user = PropertyUtils.getString(Constants.DINGTALK_USER);
    private static final String passwd = PropertyUtils.getString(Constants.DINGTALK_PASSWORD);
    private static final Integer port = PropertyUtils.getInt(Constants.DINGTALK_PORT);

    /**
     * send message interface
     * only support text message format now.
     * @param msg message context to send
     * @param charset charset type
     * @return  result of sending msg
     * @throws IOException the IOException
     */
    public static String sendDingTalkMsg(String msg, String charset) throws IOException {
        String msgToJson = textToJsonString(msg + "#" + keyword);
        HttpPost httpPost = constructHttpPost(msgToJson, charset);

        CloseableHttpClient httpClient;
        if (isEnableProxy) {
            httpClient = getProxyClient();
            RequestConfig rcf = getProxyConfig();
            httpPost.setConfig(rcf);
        } else {
            httpClient = getDefaultClient();
        }

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, charset);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("Ding Talk send [{}], resp:{%s}", msg, resp);
            return resp;
        }  finally {
            httpClient.close();
        }
    }

    public static HttpPost constructHttpPost(String msg, String charset) {
        HttpPost post =  new HttpPost(dingTaskUrl);
        StringEntity entity = new StringEntity(msg, charset);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }


    public static CloseableHttpClient getProxyClient() {
        HttpHost httpProxy = new HttpHost(proxy, port);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(httpProxy), new UsernamePasswordCredentials(user, passwd));
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
        return httpClient;
    }

    public static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    public static RequestConfig getProxyConfig() {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setProxy(httpProxy).build();
    }

    public static String textToJsonString(String text) {
        Map<String, Object> items = new HashMap<String, Object>();
        items.put("msgtype", "text");
        Map<String, String> textContent = new HashMap<String, String>();
        byte[] byt = StringUtils.getBytesUtf8(text);
        String txt = StringUtils.newStringUtf8(byt);
        textContent.put("content", txt);
        items.put("text", textContent);

        return JSONUtils.toJsonString(items);

    }

}
