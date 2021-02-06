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

package org.apache.dolphinscheduler.plugin.alert.feishu;

import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeiShuSender {

    private static final Logger logger = LoggerFactory.getLogger(FeiShuSender.class);

    private String url;

    private Boolean enableProxy;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    FeiShuSender(Map<String, String> config) {
        url = config.get(FeiShuParamsConstants.NAME_WEB_HOOK);
        enableProxy = Boolean.valueOf(config.get(FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(FeiShuParamsConstants.NAME_FEI_SHU_PORT));
            proxy = config.get(FeiShuParamsConstants.NAME_FEI_SHU_PROXY);
            user = config.get(FeiShuParamsConstants.NAME_FEI_SHU_USER);
            password = config.get(FeiShuParamsConstants.NAME_FEI_SHU_PASSWORD);
        }

    }

    private static RequestConfig getProxyConfig(String proxy, int port) {
        HttpHost httpProxy = new HttpHost(proxy, port);
        return RequestConfig.custom().setProxy(httpProxy).build();
    }

    private static String textToJsonString(AlertData alertData) {

        Map<String, Object> items = new HashMap<>(2);
        items.put("msg_type", "text");
        Map<String, String> textContent = new HashMap<>();
        byte[] byt = StringUtils.getBytesUtf8(formatContent(alertData));
        String txt = StringUtils.newStringUtf8(byt);
        textContent.put("text", txt);
        items.put("content", textContent);
        return JSONUtils.toJsonString(items);
    }

    private static AlertResult checkSendFeiShuSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (org.apache.dolphinscheduler.spi.utils.StringUtils.isBlank(result)) {
            alertResult.setMessage("send fei shu msg error");
            logger.info("send fei shu msg error,fei shu server resp is null");
            return alertResult;
        }
        FeiShuSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, FeiShuSendMsgResponse.class);

        if (null == sendMsgResponse) {
            alertResult.setMessage("send fei shu msg fail");
            logger.info("send fei shu msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.statusCode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("send fei shu msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send fei shu msg error : %s", sendMsgResponse.getStatusMessage()));
        logger.info("alert send fei shu msg error : {} ,Extra : {} ", sendMsgResponse.getStatusMessage(), sendMsgResponse.getExtra());
        return alertResult;
    }

    public static String formatContent(AlertData alertData) {
        if (alertData.getContent() != null) {
            List<Map> list;
            try {
                list = JSONUtils.toList(alertData.getContent(), Map.class);
            } catch (Exception e) {
                logger.error("json format exception", e);
                return null;
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`%n", alertData.getTitle()));
            for (Map map : list) {
                Iterator<Entry<String, Object>> entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Entry<String, Object> entry = entries.next();
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

    public AlertResult sendFeiShuMsg(AlertData alertData) {
        AlertResult alertResult;
        try {
            String resp = sendMsg(alertData);
            return checkSendFeiShuSendMsgResult(resp);
        } catch (Exception e) {
            logger.info("send fei shu alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send fei shu alert fail.");
        }
        return alertResult;
    }

    private String sendMsg(AlertData alertData) throws IOException {

        String msgToJson = textToJsonString(alertData);

        HttpPost httpPost = HttpRequestUtil.constructHttpPost(url, msgToJson);

        CloseableHttpClient httpClient;

        httpClient = HttpRequestUtil.getHttpClient(enableProxy, proxy, port, user, password);

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("send feishu message error, return http status code: " + statusCode);
            }
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, "utf-8");
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("Ding Talk send title :{} ,content :{}, resp: {}", alertData.getTitle(), alertData.getContent(), resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    public static class FeiShuSendMsgResponse {
        @JsonProperty("Extra")
        private String extra;
        @JsonProperty("StatusCode")
        private Integer statusCode;
        @JsonProperty("StatusMessage")
        private String statusMessage;

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }

}
