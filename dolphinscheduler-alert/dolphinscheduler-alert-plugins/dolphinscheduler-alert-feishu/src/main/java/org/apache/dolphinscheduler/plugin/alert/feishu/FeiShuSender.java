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
            alertResult.setMessage("send fei shu msg error");
            log.info("send fei shu msg error,fei shu server resp is null");
            return alertResult;
        }
        FeiShuSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, FeiShuSendMsgResponse.class);

        if (null == sendMsgResponse) {
            alertResult.setMessage("send fei shu msg fail");
            log.info("send fei shu msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.statusCode == 0) {
            alertResult.setSuccess(true);
            alertResult.setMessage("send fei shu msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send fei shu msg error : %s", sendMsgResponse.getStatusMessage()));
        log.info("alert send fei shu msg error : {} ,Extra : {} ", sendMsgResponse.getStatusMessage(),
                sendMsgResponse.getExtra());
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
            log.info("send fei shu alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setSuccess(false);
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
            log.info("Fei Shu send title :{} ,content :{}, resp: {}", alertData.getTitle(), alertData.getContent(),
                    resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    static final class FeiShuSendMsgResponse {

        @JsonProperty("Extra")
        private String extra;
        @JsonProperty("StatusCode")
        private Integer statusCode;
        @JsonProperty("StatusMessage")
        private String statusMessage;

        public FeiShuSendMsgResponse() {
        }

        public String getExtra() {
            return this.extra;
        }

        @JsonProperty("Extra")
        public void setExtra(String extra) {
            this.extra = extra;
        }

        public Integer getStatusCode() {
            return this.statusCode;
        }

        @JsonProperty("StatusCode")
        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusMessage() {
            return this.statusMessage;
        }

        @JsonProperty("StatusMessage")
        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FeiShuSendMsgResponse)) {
                return false;
            }
            final FeiShuSendMsgResponse other = (FeiShuSendMsgResponse) o;
            final Object this$extra = this.getExtra();
            final Object other$extra = other.getExtra();
            if (this$extra == null ? other$extra != null : !this$extra.equals(other$extra)) {
                return false;
            }
            final Object this$statusCode = this.getStatusCode();
            final Object other$statusCode = other.getStatusCode();
            if (this$statusCode == null ? other$statusCode != null : !this$statusCode.equals(other$statusCode)) {
                return false;
            }
            final Object this$statusMessage = this.getStatusMessage();
            final Object other$statusMessage = other.getStatusMessage();
            if (this$statusMessage == null ? other$statusMessage != null
                    : !this$statusMessage.equals(other$statusMessage)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $extra = this.getExtra();
            result = result * PRIME + ($extra == null ? 43 : $extra.hashCode());
            final Object $statusCode = this.getStatusCode();
            result = result * PRIME + ($statusCode == null ? 43 : $statusCode.hashCode());
            final Object $statusMessage = this.getStatusMessage();
            result = result * PRIME + ($statusMessage == null ? 43 : $statusMessage.hashCode());
            return result;
        }

        public String toString() {
            return "FeiShuSender.FeiShuSendMsgResponse(extra=" + this.getExtra() + ", statusCode="
                    + this.getStatusCode() + ", statusMessage=" + this.getStatusMessage() + ")";
        }
    }
}
