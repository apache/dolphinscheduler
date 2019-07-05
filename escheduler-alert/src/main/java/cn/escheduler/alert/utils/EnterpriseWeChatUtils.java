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
package cn.escheduler.alert.utils;

import cn.escheduler.common.enums.ShowType;
import cn.escheduler.dao.model.Alert;
import com.alibaba.fastjson.JSON;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static cn.escheduler.alert.utils.PropertyUtils.getString;

/**
 * Enterprise WeChat utils
 */
public class EnterpriseWeChatUtils {

    public static final Logger logger = LoggerFactory.getLogger(EnterpriseWeChatUtils.class);

    private static final String enterpriseWeChatCorpId = getString(Constants.ENTERPRISE_WECHAT_CORP_ID);

    private static final String enterpriseWeChatSecret = getString(Constants.ENTERPRISE_WECHAT_SECRET);

    private static final String enterpriseWeChatTokenUrl = getString(Constants.ENTERPRISE_WECHAT_TOKEN_URL);
    private static String enterpriseWeChatTokenUrlReplace = enterpriseWeChatTokenUrl
            .replaceAll("\\$corpId", enterpriseWeChatCorpId)
            .replaceAll("\\$secret", enterpriseWeChatSecret);

    private static final String enterpriseWeChatPushUrl = getString(Constants.ENTERPRISE_WECHAT_PUSH_URL);

    private static final String enterpriseWeChatTeamSendMsg = getString(Constants.ENTERPRISE_WECHAT_TEAM_SEND_MSG);

    private static final String enterpriseWeChatUserSendMsg = getString(Constants.ENTERPRISE_WECHAT_USER_SEND_MSG);

    public static final String enterpriseWeChatAgentId = getString(Constants.ENTERPRISE_WECHAT_AGENT_ID);

    public static final String enterpriseWeChatUsers = getString(Constants.ENTERPRISE_WECHAT_USERS);

    /**
     * get Enterprise WeChat token info
     * @return token string info
     * @throws IOException
     */
    public static String getToken() throws IOException {
        String resp;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(enterpriseWeChatTokenUrlReplace);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, Constants.UTF_8);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        Map<String, Object> map = JSON.parseObject(resp,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return map.get("access_token").toString();
    }

    /**
     * make team single Enterprise WeChat message
     * @param toParty
     * @param agentId
     * @param msg
     * @return Enterprise WeChat send message
     */
    public static String makeTeamSendMsg(String toParty, String agentId, String msg) {
        return enterpriseWeChatTeamSendMsg.replaceAll("\\$toParty", toParty)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team multi Enterprise WeChat message
     * @param toParty
     * @param agentId
     * @param msg
     * @return Enterprise WeChat send message
     */
    public static String makeTeamSendMsg(Collection<String> toParty, String agentId, String msg) {
        String listParty = FuncUtils.mkString(toParty, "|");
        return enterpriseWeChatTeamSendMsg.replaceAll("\\$toParty", listParty)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team single user message
     * @param toUser
     * @param agentId
     * @param msg
     * @return Enterprise WeChat send message
     */
    public static String makeUserSendMsg(String toUser, String agentId, String msg) {
        return enterpriseWeChatUserSendMsg.replaceAll("\\$toUser", toUser)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team multi user message
     * @param toUser
     * @param agentId
     * @param msg
     * @return Enterprise WeChat send message
     */
    public static String makeUserSendMsg(Collection<String> toUser, String agentId, String msg) {
        String listUser = FuncUtils.mkString(toUser, "|");
        return enterpriseWeChatUserSendMsg.replaceAll("\\$toUser", listUser)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * send Enterprise WeChat
     * @param charset
     * @param data
     * @param token
     * @return Enterprise WeChat resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     * @throws IOException
     */
    public static String sendEnterpriseWeChat(String charset, String data, String token) throws IOException {
        String enterpriseWeChatPushUrlReplace = enterpriseWeChatPushUrl.replaceAll("\\$token", token);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(enterpriseWeChatPushUrlReplace);
        httpPost.setEntity(new StringEntity(data, charset));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        String resp;
        try {
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, charset);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        logger.info("Enterprise WeChat send [{}], param:{}, resp:{}", enterpriseWeChatPushUrl, data, resp);
        return resp;
    }

    /**
     * convert table to markdown style
     * @param title
     * @param content
     * @return
     */
    public static String markdownTable(String title,String content){
        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
        StringBuilder contents = new StringBuilder(200);
        for (LinkedHashMap mapItems : mapItemsList){

            Set<Map.Entry<String, String>> entries = mapItems.entrySet();

            Iterator<Map.Entry<String, String>> iterator = entries.iterator();

            StringBuilder t = new StringBuilder(String.format("`%s`%s",title,Constants.MARKDOWN_ENTER));
            while (iterator.hasNext()){

                Map.Entry<String, String> entry = iterator.next();
                t.append(Constants.MARKDOWN_QUOTE);
                t.append(entry.getKey()).append(":").append(entry.getValue());
                t.append(Constants.MARKDOWN_ENTER);
            }

            contents.append(t);
        }
        return contents.toString();
    }

    /**
     * convert text to markdown style
     * @param title
     * @param content
     * @return
     */
    public static String markdownText(String title,String content){
        if (StringUtils.isNotEmpty(content)){
            List<String> list;
            try {
                list = JSONUtils.toList(content,String.class);
            }catch (Exception e){
                logger.error("json format exception",e);
                return null;
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`\n",title));
            for (String str : list){
                contents.append(Constants.MARKDOWN_QUOTE);
                contents.append(str);
                contents.append(Constants.MARKDOWN_ENTER);
            }

            return contents.toString();

        }
        return null;
    }

    /**
     * Determine the mardown style based on the show type of the alert
     * @param alert
     * @return
     */
    public static String markdownByAlert(Alert alert){
        String result = "";
        if (alert.getShowType() == ShowType.TABLE) {
            result = markdownTable(alert.getTitle(),alert.getContent());
        }else if(alert.getShowType() == ShowType.TEXT){
            result = markdownText(alert.getTitle(),alert.getContent());
        }
        return result;

    }

}
