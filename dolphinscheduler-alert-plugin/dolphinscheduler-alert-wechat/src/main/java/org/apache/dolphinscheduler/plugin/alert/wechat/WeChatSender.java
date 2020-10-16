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

import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangli
 * @date 2020-07-20 21:29
 */
public class WeChatSender {

    private static Logger logger = LoggerFactory.getLogger(WeChatSender.class);

    private String weChatAgentId;

    private String weChatUsers;

    private String weChatCorpId;

    private String weChatSecret;

    private String weChatTokenUrl;

    private String weChatPushUrl;

    private String weChatTeamSendMsg;

    private String weChatUserSendMsg;

    private String weChatTokenUrlReplace;

    private String weChatToken;


    private static final String agentIdRegExp = "\\{agentId}";
    private static final String msgRegExp = "\\{msg}";
    private static final String userRegExp = "\\{toUser}";

    WeChatSender(Map<String, String> config) {
        weChatAgentId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID);
        weChatUsers = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS);
        weChatCorpId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID);
        weChatSecret = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET);
        weChatTokenUrl = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_TOKEN_URL);
        weChatPushUrl = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_PUSH_URL);
        weChatTeamSendMsg = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_TEAM_SEND_MSG);
        weChatUserSendMsg = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USER_SEND_MSG);
        weChatTokenUrlReplace = weChatTokenUrl == null ? null : weChatTokenUrl
                .replaceAll("\\{corpId}", weChatCorpId)
                .replaceAll("\\{secret}", weChatSecret);
        weChatToken = getToken();
    }


    /**
     * make team single Enterprise WeChat message
     *
     * @param toParty the toParty
     * @param agentId the agentId
     * @param msg the msg
     * @return Enterprise WeChat send message
     */
    private String makeTeamSendMsg(String toParty, String agentId, String msg) {
        return weChatTeamSendMsg.replaceAll("\\{toParty}", toParty)
                .replaceAll(agentIdRegExp, agentId)
                .replaceAll(msgRegExp, msg);
    }

    /**
     * make team multi Enterprise WeChat message
     *
     * @param toParty the toParty
     * @param agentId the agentId
     * @param msg the msg
     * @return Enterprise WeChat send message
     */
    private String makeTeamSendMsg(Collection<String> toParty, String agentId, String msg) {
        String listParty = mkString(toParty, "|");
        return weChatTeamSendMsg.replaceAll("\\{toParty}", listParty)
                .replaceAll(agentIdRegExp, agentId)
                .replaceAll(msgRegExp, msg);
    }

    /**
     * make team single user message
     *
     * @param toUser the toUser
     * @param agentId the agentId
     * @param msg the msg
     * @return Enterprise WeChat send message
     */
    private String makeUserSendMsg(String toUser, String agentId, String msg) {
        return weChatUserSendMsg.replaceAll("\\{toUser}", toUser)
                .replaceAll(agentIdRegExp, agentId)
                .replaceAll(msgRegExp, msg);
    }

    /**
     * make team multi user message
     *
     * @param toUser the toUser
     * @param agentId the agentId
     * @param msg the msg
     * @return Enterprise WeChat send message
     */
    private String makeUserSendMsg(Collection<String> toUser, String agentId, String msg) {
        String listUser = mkString(toUser, "|");
        return weChatUserSendMsg.replaceAll(userRegExp, listUser)
                .replaceAll(agentIdRegExp, agentId)
                .replaceAll(msgRegExp, msg);
    }

    /**
     * send Enterprise WeChat
     *
     * @param charset the charset
     * @param data the data
     * @return Enterprise WeChat resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     * @throws IOException the IOException
     */
    public String sendEnterpriseWeChat(String charset, String data) throws IOException {
        String enterpriseWeChatPushUrlReplace = weChatPushUrl.replaceAll("\\{token}", weChatToken);
        return post(enterpriseWeChatPushUrlReplace, data);
    }

    private static String post(String url, String data) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, WeChatAlertConstants.CHARSET));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("Enterprise WeChat send [{}], param:{}, resp:{}",
                    url, data, resp);
            return resp;
        }
    }

    /**
     * convert table to markdown style
     *
     * @param title the title
     * @param content the content
     * @return markdown table content
     */
    public static String markdownTable(String title, String content) {
        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
        StringBuilder contents = new StringBuilder(200);

        if (null != mapItemsList) {
            for (LinkedHashMap mapItems : mapItemsList) {
                Set<Entry<String, Object>> entries = mapItems.entrySet();
                Iterator<Entry<String, Object>> iterator = entries.iterator();
                StringBuilder t = new StringBuilder(String.format("`%s`%s", title, WeChatAlertConstants.MARKDOWN_ENTER));

                while (iterator.hasNext()) {

                    Map.Entry<String, Object> entry = iterator.next();
                    t.append(WeChatAlertConstants.MARKDOWN_QUOTE);
                    t.append(entry.getKey()).append(":").append(entry.getValue());
                    t.append(WeChatAlertConstants.MARKDOWN_ENTER);
                }
                contents.append(t);
            }
        }
        return contents.toString();
    }

    /**
     * convert text to markdown style
     *
     * @param title the title
     * @param content the content
     * @return markdown text
     */
    public static String markdownText(String title, String content) {
        if (StringUtils.isNotEmpty(content)) {
            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
            if (null != mapItemsList) {
                StringBuilder contents = new StringBuilder(100);
                contents.append(String.format("`%s`%n", title));
                for (LinkedHashMap mapItems : mapItemsList) {

                    Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
                    Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        contents.append(WeChatAlertConstants.MARKDOWN_QUOTE);
                        contents.append(entry.getKey()).append(":").append(entry.getValue());
                        contents.append(WeChatAlertConstants.MARKDOWN_ENTER);
                    }

                }
                return contents.toString();
            }

        }
        return null;
    }

    /**
     * Determine the mardown style based on the show type of the alert
     *
     * @return the markdown alert table/text
     */
    public static String markdownByAlert(AlertInfo alertInfo) {
        String result = "";
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(alertInfo.getAlertParams());
        String showType = paramsMap.get(AlertConstants.SHOW_TYPE);
        if (showType.equals(ShowType.TABLE.getDescp())) {
            result = markdownTable(alertInfo.getAlertData().getTitle(), alertInfo.getAlertData().getContent());
        } else if (showType.equals(ShowType.TEXT.getDescp())) {
            result = markdownText(alertInfo.getAlertData().getTitle(), alertInfo.getAlertData().getContent());
        }
        return result;

    }

    private String getToken() {
        try {
            return get(weChatTokenUrlReplace);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get(String url) throws IOException {
        String resp;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }

            Map<String, String> map = JSONUtils.toMap(resp);
            if (map != null) {
                return map.get("access_token");
            } else {
                return null;
            }
        } finally {
            httpClient.close();
        }
    }

    private static String mkString(Iterable<String> list, String split) {

        if (null == list || StringUtils.isEmpty(split)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(split);
            }
            sb.append(item);
        }
        return sb.toString();
    }

}
