///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.dolphinscheduler.alert.utils;
//
//import org.apache.dolphinscheduler.common.utils.JSONUtils;
//import org.apache.dolphinscheduler.common.utils.StringUtils;
//import org.apache.dolphinscheduler.spi.alert.AlertConstants;
//import org.apache.dolphinscheduler.spi.alert.AlertInfo;
//import org.apache.dolphinscheduler.spi.alert.ShowType;
//import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Enterprise WeChat utils
// */
//public class EnterpriseWeChatUtils {
//
//    public static final Logger logger = LoggerFactory.getLogger(EnterpriseWeChatUtils.class);
//    public static final String ENTERPRISE_WE_CHAT_AGENT_ID = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_AGENT_ID);
//    public static final String ENTERPRISE_WE_CHAT_USERS = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_USERS);
//    private static final String ENTERPRISE_WE_CHAT_CORP_ID = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_CORP_ID);
//    private static final String ENTERPRISE_WE_CHAT_SECRET = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_SECRET);
//    private static final String ENTERPRISE_WE_CHAT_TOKEN_URL = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_TOKEN_URL);
//    private static final String ENTERPRISE_WE_CHAT_TOKEN_URL_REPLACE = ENTERPRISE_WE_CHAT_TOKEN_URL == null ? null : ENTERPRISE_WE_CHAT_TOKEN_URL
//            .replaceAll("\\{corpId}", ENTERPRISE_WE_CHAT_CORP_ID)
//            .replaceAll("\\{secret}", ENTERPRISE_WE_CHAT_SECRET);
//    private static final String ENTERPRISE_WE_CHAT_PUSH_URL = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_PUSH_URL);
//    private static final String ENTERPRISE_WE_CHAT_TEAM_SEND_MSG = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_TEAM_SEND_MSG);
//    private static final String ENTERPRISE_WE_CHAT_USER_SEND_MSG = PropertyUtils.getString(Constants.ENTERPRISE_WECHAT_USER_SEND_MSG);
//
//    private static final String agentIdRegExp = "\\{agentId}";
//    private static final String msgRegExp = "\\{msg}";
//    private static final String userRegExp = "\\{toUser}";
//
//    /**
//     * get Enterprise WeChat is enable
//     *
//     * @return isEnable
//     */
//    public static boolean isEnable() {
//        Boolean isEnable = null;
//        try {
//            isEnable = PropertyUtils.getBoolean(Constants.ENTERPRISE_WECHAT_ENABLE);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        if (isEnable == null) {
//            return false;
//        }
//        return isEnable;
//    }
//
//    /**
//     * get Enterprise WeChat token info
//     *
//     * @return token string info
//     * @throws IOException the IOException
//     */
//    public static String getToken() throws IOException {
//        String resp;
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        try {
//            HttpGet httpGet = new HttpGet(ENTERPRISE_WE_CHAT_TOKEN_URL_REPLACE);
//            CloseableHttpResponse response = httpClient.execute(httpGet);
//            try {
//                HttpEntity entity = response.getEntity();
//                resp = EntityUtils.toString(entity, Constants.UTF_8);
//                EntityUtils.consume(entity);
//            } finally {
//                response.close();
//            }
//
//            Map<String, String> map = JSONUtils.toMap(resp);
//            if (map != null) {
//                return map.get("access_token");
//            } else {
//                return null;
//            }
//        } finally {
//            httpClient.close();
//        }
//    }
//
//    /**
//     * make team single Enterprise WeChat message
//     *
//     * @param toParty the toParty
//     * @param agentId the agentId
//     * @param msg     the msg
//     * @return Enterprise WeChat send message
//     */
//    public static String makeTeamSendMsg(String toParty, String agentId, String msg) {
//        return ENTERPRISE_WE_CHAT_TEAM_SEND_MSG.replaceAll("\\{toParty}", toParty)
//                .replaceAll(agentIdRegExp, agentId)
//                .replaceAll(msgRegExp, msg);
//    }
//
//    /**
//     * make team multi Enterprise WeChat message
//     *
//     * @param toParty the toParty
//     * @param agentId the agentId
//     * @param msg     the msg
//     * @return Enterprise WeChat send message
//     */
//    public static String makeTeamSendMsg(Collection<String> toParty, String agentId, String msg) {
//        String listParty = FuncUtils.mkString(toParty, "|");
//        return ENTERPRISE_WE_CHAT_TEAM_SEND_MSG.replaceAll("\\{toParty}", listParty)
//                .replaceAll(agentIdRegExp, agentId)
//                .replaceAll(msgRegExp, msg);
//    }
//
//    /**
//     * make team single user message
//     *
//     * @param toUser  the toUser
//     * @param agentId the agentId
//     * @param msg     the msg
//     * @return Enterprise WeChat send message
//     */
//    public static String makeUserSendMsg(String toUser, String agentId, String msg) {
//        return ENTERPRISE_WE_CHAT_USER_SEND_MSG.replaceAll("\\{toUser}", toUser)
//                .replaceAll(agentIdRegExp, agentId)
//                .replaceAll(msgRegExp, msg);
//    }
//
//    /**
//     * make team multi user message
//     *
//     * @param toUser  the toUser
//     * @param agentId the agentId
//     * @param msg     the msg
//     * @return Enterprise WeChat send message
//     */
//    public static String makeUserSendMsg(Collection<String> toUser, String agentId, String msg) {
//        String listUser = FuncUtils.mkString(toUser, "|");
//        return ENTERPRISE_WE_CHAT_USER_SEND_MSG.replaceAll(userRegExp, listUser)
//                .replaceAll(agentIdRegExp, agentId)
//                .replaceAll(msgRegExp, msg);
//    }
//
//    /**
//     * send Enterprise WeChat
//     *
//     * @param charset the charset
//     * @param data    the data
//     * @param token   the token
//     * @return Enterprise WeChat resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
//     * @throws IOException the IOException
//     */
//    public static String sendEnterpriseWeChat(String charset, String data, String token) throws IOException {
//        String enterpriseWeChatPushUrlReplace = ENTERPRISE_WE_CHAT_PUSH_URL.replaceAll("\\{token}", token);
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        try {
//            HttpPost httpPost = new HttpPost(enterpriseWeChatPushUrlReplace);
//            httpPost.setEntity(new StringEntity(data, charset));
//            CloseableHttpResponse response = httpClient.execute(httpPost);
//            String resp;
//            try {
//                HttpEntity entity = response.getEntity();
//                resp = EntityUtils.toString(entity, charset);
//                EntityUtils.consume(entity);
//            } finally {
//                response.close();
//            }
//            logger.info("Enterprise WeChat send [{}], param:{}, resp:{}",
//                    ENTERPRISE_WE_CHAT_PUSH_URL, data, resp);
//            return resp;
//        } finally {
//            httpClient.close();
//        }
//    }
//
//    /**
//     * convert table to markdown style
//     *
//     * @param title   the title
//     * @param content the content
//     * @return markdown table content
//     */
//    public static String markdownTable(String title, String content) {
//        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
//        StringBuilder contents = new StringBuilder(200);
//
//        if (null != mapItemsList) {
//            for (LinkedHashMap mapItems : mapItemsList) {
//                Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
//                Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
//                StringBuilder t = new StringBuilder(String.format("`%s`%s", title, Constants.MARKDOWN_ENTER));
//
//                while (iterator.hasNext()) {
//
//                    Map.Entry<String, Object> entry = iterator.next();
//                    t.append(Constants.MARKDOWN_QUOTE);
//                    t.append(entry.getKey()).append(":").append(entry.getValue());
//                    t.append(Constants.MARKDOWN_ENTER);
//                }
//                contents.append(t);
//            }
//        }
//        return contents.toString();
//    }
//
//    /**
//     * convert text to markdown style
//     *
//     * @param title   the title
//     * @param content the content
//     * @return markdown text
//     */
//    public static String markdownText(String title, String content) {
//        if (StringUtils.isNotEmpty(content)) {
//            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
//            if (null != mapItemsList) {
//                StringBuilder contents = new StringBuilder(100);
//                contents.append(String.format("`%s`%n", title));
//                for (LinkedHashMap mapItems : mapItemsList) {
//
//                    Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
//                    Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
//                    while (iterator.hasNext()) {
//                        Map.Entry<String, Object> entry = iterator.next();
//                        contents.append(Constants.MARKDOWN_QUOTE);
//                        contents.append(entry.getKey()).append(":").append(entry.getValue());
//                        contents.append(Constants.MARKDOWN_ENTER);
//                    }
//
//                }
//                return contents.toString();
//            }
//
//        }
//        return null;
//    }
//
//    /**
//     * Determine the mardown style based on the show type of the alert
//     *
//     * @return the markdown alert table/text
//     */
//    public static String markdownByAlert(AlertInfo alertInfo) {
//        String result = "";
//        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(alertInfo.getAlertParams());
//        String showType = paramsMap.get(AlertConstants.SHOW_TYPE);
//        if (showType.equals(ShowType.TABLE.getDescp())) {
//            result = markdownTable(alertInfo.getAlertData().getTitle(), alertInfo.getAlertData().getContent());
//        } else if (showType.equals(ShowType.TEXT.getDescp())) {
//            result = markdownText(alertInfo.getAlertData().getTitle(), alertInfo.getAlertData().getContent());
//        }
//        return result;
//
//    }
//
//}
