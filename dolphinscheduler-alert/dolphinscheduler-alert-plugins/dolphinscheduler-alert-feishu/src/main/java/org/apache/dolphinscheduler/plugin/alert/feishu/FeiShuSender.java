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

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
public final class FeiShuSender {

    private final String url;
    private final Boolean enableProxy;

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

    public static AlertResult checkSendFeiShuSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);

        if (org.apache.commons.lang3.StringUtils.isBlank(result)) {
            alertResult.setMessage("send feishu msg error: feishu server resp is blank.");
            log.info("send feishu msg error: feishu server resp is blank.");
            return alertResult;
        }
        FeiShuSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, FeiShuSendMsgResponse.class);

        if (null == sendMsgResponse) {
            alertResult.setMessage("send feishu msg error: feishu server resp parse error is null.");
            log.info("send feishu msg error: feishu server resp parse error is null.");
            return alertResult;
        }
        if (sendMsgResponse.code == 0) {
            alertResult.setSuccess(true);
            alertResult.setMessage("send feishu msg success.");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send feishu msg error: %s", sendMsgResponse.getMsg()));
        log.info("alert send feishu msg error: {}", sendMsgResponse);
        return alertResult;
    }

    public static String formatContent(AlertData alertData) {
        if (alertData.getContent() != null) {

            List<Map> list = JSONUtils.toList(alertData.getContent(), Map.class);
            if (CollectionUtils.isEmpty(list)) {
                return alertData.getTitle() + alertData.getContent();
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`%n", alertData.getTitle()));
            for (Map map : list) {
                for (Entry<String, Object> entry : (Iterable<Entry<String, Object>>) map.entrySet()) {
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
            log.error("send feishu alert failed:", e);
            alertResult = new AlertResult();
            alertResult.setSuccess(false);
            alertResult.setMessage("send feishu alert fail.");
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
                log.error("send feishu message error, return http status code: {} ", statusCode);
            }
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("feishu send title: {}, content: {}, resp: {}", alertData.getTitle(), alertData.getContent(),
                    resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }
    @AllArgsConstructor
    @Builder
    @Data
    @NoArgsConstructor
    static final class FeiShuSendMsgResponse {

        @JsonProperty("data")
        private Object data;
        @JsonProperty("code")
        private Integer code;
        @JsonProperty("msg")
        private String msg;

    }
}
