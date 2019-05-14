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

import com.alibaba.fastjson.JSON;

import com.google.common.reflect.TypeToken;
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
import java.util.Collection;
import java.util.Map;

import static cn.escheduler.alert.utils.PropertyUtils.getString;

/**
 * qiye weixin utils
 */
public class QiyeWeixinUtils {

    public static final Logger logger = LoggerFactory.getLogger(QiyeWeixinUtils.class);

    private static final String qiyeWeixinCorpId = getString(Constants.QIYE_WEIXIN_CORP_ID);

    private static final String qiyeWeixinSecret = getString(Constants.QIYE_WEIXIN_SECRET);

    private static final String qiyeWeixinTokenUrl = getString(Constants.QIYE_WEIXIN_TOKEN_URL);
    private String qiyeWeixinTokenUrlReplace = qiyeWeixinTokenUrl
            .replaceAll("\\$weixinCorpId", qiyeWeixinCorpId)
            .replaceAll("\\$weixinSecret", qiyeWeixinSecret);

    private static final String qiyeWeixinPushUrl = getString(Constants.QIYE_WEIXIN_PUSH_URL);

    private static final String qiyeWeixinTeamSendMsg = getString(Constants.QIYE_WEIXIN_TEAM_SEND_MSG);

    private static final String qiyeWeixinUserSendMsg = getString(Constants.QIYE_WEIXIN_USER_SEND_MSG);

    /**
     * get winxin token info
     * @return token string info
     * @throws IOException
     */
    public String getToken() throws IOException {
        String resp;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(qiyeWeixinTokenUrlReplace);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            HttpEntity entity = response.getEntity();
            resp = EntityUtils.toString(entity, "utf-8");
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
     * make team single weixin message
     * @param toParty
     * @param agentId
     * @param msg
     * @return weixin send message
     */
    public String makeTeamSendMsg(String toParty, String agentId, String msg) {
        return qiyeWeixinTeamSendMsg.replaceAll("\\$toParty", toParty)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team multi weixin message
     * @param toParty
     * @param agentId
     * @param msg
     * @return weixin send message
     */
    public String makeTeamSendMsg(Collection<String> toParty, String agentId, String msg) {
        String listParty = FuncUtils.mkString(toParty, "|");
        return qiyeWeixinTeamSendMsg.replaceAll("\\$toParty", listParty)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team single user message
     * @param toUser
     * @param agentId
     * @param msg
     * @return weixin send message
     */
    public String makeUserSendMsg(String toUser, String agentId, String msg) {
        return qiyeWeixinUserSendMsg.replaceAll("\\$toUser", toUser)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * make team multi user message
     * @param toUser
     * @param agentId
     * @param msg
     * @return weixin send message
     */
    public String makeUserSendMsg(Collection<String> toUser, String agentId, String msg) {
        String listUser = FuncUtils.mkString(toUser, "|");
        return qiyeWeixinUserSendMsg.replaceAll("\\$toUser", listUser)
                .replaceAll("\\$agentId", agentId)
                .replaceAll("\\$msg", msg);
    }

    /**
     * send weixin
     * @param charset
     * @param data
     * @param token
     * @return weixin resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     * @throws IOException
     */
    public String sendQiyeWeixin(String charset, String data, String token) throws IOException {
        String qiyeWeixinPushUrlReplace = qiyeWeixinPushUrl.replaceAll("\\$weixinToken", token);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(qiyeWeixinPushUrlReplace);
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
        logger.info("qiye weixin send [{}], param:{}, resp:{}", qiyeWeixinPushUrl, data, resp);
        return resp;
    }

}
