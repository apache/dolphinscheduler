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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FeiShuSender {

    private FeiShuPersonalWorkAccessToken accessToken;

    private String sendType;

    private final String urlOrAppId;

    private String appSecret;

    private String receiveIdType;

    private String receiveId;

    private final Boolean enableProxy;

    private String proxy;

    private Integer port;

    private String user;

    private String password;

    FeiShuSender(Map<String, String> config) {
        sendType = config.get(FeiShuParamsConstants.NAME_FEI_SHU_SEND_TYPE);
        urlOrAppId = config.get(FeiShuParamsConstants.NAME_WEB_HOOK_OR_APP_ID);
        appSecret = config.get(FeiShuParamsConstants.NAME_PERSONAL_WORK_APP_SECRET);
        receiveIdType = config.get(FeiShuParamsConstants.NAME_RECEIVE_ID_TYPE);
        receiveId = config.get(FeiShuParamsConstants.NAME_RECEIVE_ID);
        enableProxy = Boolean.valueOf(config.get(FeiShuParamsConstants.NAME_FEI_SHU_PROXY_ENABLE));
        if (Boolean.TRUE.equals(enableProxy)) {
            port = Integer.parseInt(config.get(FeiShuParamsConstants.NAME_FEI_SHU_PORT));
            proxy = config.get(FeiShuParamsConstants.NAME_FEI_SHU_PROXY);
            user = config.get(FeiShuParamsConstants.NAME_FEI_SHU_USER);
            password = config.get(FeiShuParamsConstants.NAME_FEI_SHU_PASSWORD);
        }

    }

    private static String generateCustomRobotContentJson(AlertData alertData) {
        Map<String, Object> items = new HashMap<>(2);
        items.put("msg_type", "text");
        Map<String, String> textContent = new HashMap<>();
        byte[] byt = StringUtils.getBytesUtf8(formatContent(alertData));
        String txt = StringUtils.newStringUtf8(byt);
        textContent.put("text", txt);
        items.put("content", textContent);
        return JSONUtils.toJsonString(items);
    }

    private String generateApplicationRobotContentJson(AlertData alertData) {
        Map<String, String> text = new HashMap<>();
        text.put("text", formatContent(alertData));
        Map<String, String> body = new HashMap<>();
        body.put("content", JSONUtils.toJsonString(text));
        body.put("msg_type", "text");
        body.put("receive_id", receiveId);
        return JSONUtils.toJsonString(body);
    }

    public AlertResult checkSendCustomRobotMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

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
        if (sendMsgResponse.getStatusCode() == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("send fei shu msg success");
            return alertResult;
        }
        alertResult.setMessage(String.format("alert send fei shu msg error : %s", sendMsgResponse.getStatusMessage()));
        log.info("alert send fei shu msg error : {} ,Extra : {} ", sendMsgResponse.getStatusMessage(),
                sendMsgResponse.getExtra());
        return alertResult;
    }

    public AlertResult checkSendApplicationRobotMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        if (null == result) {
            alertResult.setMessage("send fei shu personal work msg error, fei shu server resp is null");
            log.info("send fei shu personal work msg error, fei shu server resp is null");
            return alertResult;
        }
        FeiShuPersonalWorkResponse sendMsgResponse =
                JSONUtils.parseObject(result, FeiShuPersonalWorkResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("send fei shu personal work msg fail");
            log.info(String.format("send fei shu personal work resp error, resp : %s", result));
            return alertResult;
        }
        if (sendMsgResponse.getCode() != 0) {
            alertResult.setMessage(
                    String.format("send fei shu personal work msg fail, code is %d", sendMsgResponse.getCode()));
            log.info(String.format("send fei shu personal work resp error, resp : %s", result));
            return alertResult;
        }
        alertResult.setStatus("true");
        alertResult.setMessage("send fei shu personal work msg success");
        log.info("send fei shu personal work msg success");
        return alertResult;
    }

    public static String formatContent(AlertData alertData) {
        if (alertData.getContent() != null) {

            List<Map> list = JSONUtils.toList(alertData.getContent(), Map.class);
            if (CollectionUtils.isEmpty(list)) {
                return alertData.getTitle() + "\n" + alertData.getContent();
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
            String resp = "";
            if (sendType.equals(FeiShuType.CUSTOM_ROBOT.getDescp())) {
                resp = sendCustomRobotMsg(alertData);
                return checkSendCustomRobotMsgResult(resp);
            } else if (sendType.equals(FeiShuType.APPLIANCE_ROBOT.getDescp())) {
                queryAndSetAccessToken();
                resp = sendApplicationRobotMsg(alertData);
                return checkSendApplicationRobotMsgResult(resp);
            }
            alertResult = new AlertResult();
            log.info("send fei shu alert fail, no support send type");
            alertResult.setStatus("false");
            alertResult.setMessage("send fei shu alert fail");
        } catch (Exception e) {
            log.info("send fei shu alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("send fei shu alert fail.");
        }
        return alertResult;
    }

    private String sendApplicationRobotMsg(AlertData alertData) throws IOException, URISyntaxException {
        String msg = generateApplicationRobotContentJson(alertData);

        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("receive_id_type", receiveIdType);
        HttpPost httpPost = HttpRequestUtil.constructHttpPost(FeiShuParamsConstants.accessMessageUrl, msg, pathParams);
        httpPost.setHeader("Authorization", String.format("Bearer %s", accessToken.getTenantAccessToken()));

        CloseableHttpClient httpClient = HttpRequestUtil.getHttpClient(enableProxy, proxy, port, user, password);

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
            log.info("fei shu personal work send msg :{}, resp: {}", msg, resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    private String sendCustomRobotMsg(AlertData alertData) throws IOException {

        String msgToJson = generateCustomRobotContentJson(alertData);

        HttpPost httpPost = HttpRequestUtil.constructHttpPost(urlOrAppId, msgToJson);

        CloseableHttpClient httpClient;

        httpClient = HttpRequestUtil.getHttpClient(enableProxy, proxy, port, user, password);

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.error("send fei shu message error, return http status code: {} ", statusCode);
            }
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, "utf-8");
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            log.info("fei shu send title :{} ,content :{}, resp: {}", alertData.getTitle(), alertData.getContent(),
                    resp);
            return resp;
        } finally {
            httpClient.close();
        }
    }

    public void queryAndSetAccessToken() throws IOException {
        CloseableHttpClient httpClient = HttpRequestUtil.getHttpClient(enableProxy, proxy, port, user, password);
        Map<String, String> params = generateAccessParams();
        HttpPost httpPost =
                HttpRequestUtil.constructHttpPost(FeiShuParamsConstants.accessTokenUrl, JSONUtils.toJsonString(params));

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

            FeiShuAccessTokenResponse tokenResponse =
                    JSONUtils.parseObject(resp, FeiShuAccessTokenResponse.class);
            if (tokenResponse == null) {
                log.info(String.format("fei shu sender get access token failed, resp: %s", resp));
                return;
            }

            if (tokenResponse.getTenantAccessToken() == null || tokenResponse.getTenantAccessToken().isEmpty()) {
                log.info(String.format("fei shu sender get access token failed, resp: %s", resp));
                return;
            }

            log.info("fei shu sender get access token succeed");
            FeiShuPersonalWorkAccessToken token = new FeiShuPersonalWorkAccessToken();
            token.setTenantAccessToken(tokenResponse.getTenantAccessToken());
            token.setStart(System.currentTimeMillis());
            token.setExpire(System.currentTimeMillis() + tokenResponse.getExpire());
            this.accessToken = token;
        } finally {
            httpClient.close();
        }
    }

    private Map<String, String> generateAccessParams() {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", urlOrAppId);
        params.put("app_secret", appSecret);
        return params;
    }

    public FeiShuPersonalWorkAccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(FeiShuPersonalWorkAccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
