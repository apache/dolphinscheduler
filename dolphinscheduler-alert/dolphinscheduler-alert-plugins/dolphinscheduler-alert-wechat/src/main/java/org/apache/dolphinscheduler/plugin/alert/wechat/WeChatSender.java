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

package org.apache.dolphinscheduler.plugin.alert.wechat;

import static java.util.Objects.requireNonNull;
import static org.apache.dolphinscheduler.plugin.alert.wechat.WeChatAlertConstants.WE_CHAT_DUPLICATE_CHECK_INTERVAL_ZERO;
import static org.apache.dolphinscheduler.plugin.alert.wechat.WeChatAlertConstants.WE_CHAT_ENABLE_ID_TRANS;
import static org.apache.dolphinscheduler.plugin.alert.wechat.WeChatAlertConstants.WE_CHAT_MESSAGE_SAFE_PUBLICITY;

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.HttpServiceRetryStrategy;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WeChatSender {

    private static final String MUST_NOT_NULL = " must not null";
    private static final String AGENT_ID_REG_EXP = "{agentId}";
    private static final String MSG_REG_EXP = "{msg}";
    private static final String USER_REG_EXP = "{toUser}";
    private static final String CORP_ID_REGEX = "{corpId}";
    private static final String SECRET_REGEX = "{secret}";
    private static final String TOKEN_REGEX = "{token}";
    private final String weChatAgentIdChatId;
    private final String weChatUsers;
    private final String weChatTokenUrlReplace;
    private final String weChatToken;
    private final String sendType;
    private final String showType;

    WeChatSender(Map<String, String> config) {
        weChatAgentIdChatId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID);
        weChatUsers = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS);
        String weChatCorpId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID);
        String weChatSecret = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET);
        String weChatTokenUrl = WeChatAlertConstants.WE_CHAT_TOKEN_URL;
        sendType = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SEND_TYPE);
        showType = config.get(AlertConstants.NAME_SHOW_TYPE);
        requireNonNull(showType, AlertConstants.NAME_SHOW_TYPE + MUST_NOT_NULL);
        weChatTokenUrlReplace = weChatTokenUrl
                .replace(CORP_ID_REGEX, weChatCorpId)
                .replace(SECRET_REGEX, weChatSecret);
        weChatToken = getToken();
    }

    private static String post(String url, String data) throws IOException {
        try (
                CloseableHttpClient httpClient =
                        HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, StandardCharsets.UTF_8));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("Enterprise WeChat send [{}], param:{}, resp:{}",
                    url, data, resp);
            return resp;
        }
    }

    /**
     * convert text to markdown style
     *
     * @param title   the title
     * @param content the content
     * @return markdown text
     */
    private static String markdownText(String title, String content) {
        if (StringUtils.isNotEmpty(content)) {
            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
            if (null == mapItemsList || mapItemsList.isEmpty()) {
                log.error("itemsList is null");
                throw new RuntimeException("itemsList is null");
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`%n", title));
            for (LinkedHashMap mapItems : mapItemsList) {

                Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
                for (Entry<String, Object> entry : entries) {
                    contents.append(WeChatAlertConstants.MARKDOWN_QUOTE);
                    contents.append(entry.getKey()).append(":").append(entry.getValue());
                    contents.append(WeChatAlertConstants.MARKDOWN_ENTER);
                }

            }
            return contents.toString();
        }
        return null;
    }

    private static String get(String url) throws IOException {
        String resp;

        try (
                CloseableHttpClient httpClient =
                        HttpClients.custom().setRetryHandler(HttpServiceRetryStrategy.retryStrategy).build();) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
            }

            HashMap<String, Object> map = JSONUtils.parseObject(resp, HashMap.class);
            if (map != null && null != map.get("access_token")) {
                return map.get("access_token").toString();
            } else {
                return null;
            }
        }
    }

    private static String mkString(Iterable<String> list) {
        if (null == list || StringUtils.isEmpty("|")) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append("|");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    private static AlertResult checkWeChatSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);

        if (null == result) {
            alertResult.setMessage("we chat send fail");
            log.info("send we chat msg error,resp is null");
            return alertResult;
        }
        WeChatSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, WeChatSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("we chat send fail");
            log.info("send we chat msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setSuccess(true);
            alertResult.setMessage("we chat alert send success");
            return alertResult;
        }
        alertResult.setSuccess(false);
        alertResult.setMessage(sendMsgResponse.getErrmsg());
        return alertResult;
    }

    /**
     * send Enterprise WeChat
     *
     * @return Enterprise WeChat resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     */
    public AlertResult sendEnterpriseWeChat(String title, String content) {
        AlertResult alertResult;
        String data = markdownByAlert(title, content);
        if (null == weChatToken) {
            alertResult = new AlertResult();
            alertResult.setMessage("send we chat alert fail,get weChat token error");
            alertResult.setSuccess(false);
            return alertResult;
        }
        String enterpriseWeChatPushUrlReplace = "";
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put(WeChatAlertConstants.WE_CHAT_CONTENT_KEY, data);
        String msgJson = "";
        if (sendType.equals(WeChatType.APP.getDescp())) {
            enterpriseWeChatPushUrlReplace = WeChatAlertConstants.WE_CHAT_PUSH_URL.replace(TOKEN_REGEX, weChatToken);
            WechatAppMessage wechatAppMessage = new WechatAppMessage(weChatUsers, showType,
                    Integer.valueOf(weChatAgentIdChatId), contentMap, WE_CHAT_MESSAGE_SAFE_PUBLICITY,
                    WE_CHAT_ENABLE_ID_TRANS, WE_CHAT_DUPLICATE_CHECK_INTERVAL_ZERO);
            msgJson = JSONUtils.toJsonString(wechatAppMessage);
        } else if (sendType.equals(WeChatType.APPCHAT.getDescp())) {
            enterpriseWeChatPushUrlReplace =
                    WeChatAlertConstants.WE_CHAT_APP_CHAT_PUSH_URL.replace(TOKEN_REGEX, weChatToken);
            WechatAppChatMessage wechatAppChatMessage =
                    new WechatAppChatMessage(weChatAgentIdChatId, showType, contentMap, WE_CHAT_MESSAGE_SAFE_PUBLICITY);
            msgJson = JSONUtils.toJsonString(wechatAppChatMessage);
        }

        try {
            return checkWeChatSendMsgResult(post(enterpriseWeChatPushUrlReplace, msgJson));
        } catch (Exception e) {
            log.info("send we chat alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setMessage("send we chat alert fail");
            alertResult.setSuccess(false);
        }
        return alertResult;
    }

    /**
     * Determine the mardown style based on the show type of the alert
     *
     * @return the markdown alert table/text
     */
    private String markdownByAlert(String title, String content) {
        return markdownText(title, content);
    }

    private String getToken() {
        try {
            return get(weChatTokenUrlReplace);
        } catch (IOException e) {
            log.info("we chat alert get token error{}", e.getMessage());
        }
        return null;
    }

    @Getter
    @Setter
    static final class WeChatSendMsgResponse {

        private Integer errcode;
        private String errmsg;

        public WeChatSendMsgResponse() {
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof WeChatSendMsgResponse)) {
                return false;
            }
            final WeChatSendMsgResponse other = (WeChatSendMsgResponse) o;
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

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $errcode = this.getErrcode();
            result = result * PRIME + ($errcode == null ? 43 : $errcode.hashCode());
            final Object $errmsg = this.getErrmsg();
            result = result * PRIME + ($errmsg == null ? 43 : $errmsg.hashCode());
            return result;
        }

        public String toString() {
            return "WeChatSender.WeChatSendMsgResponse(errcode=" + this.getErrcode() + ", errmsg=" + this.getErrmsg()
                    + ")";
        }
    }
}
