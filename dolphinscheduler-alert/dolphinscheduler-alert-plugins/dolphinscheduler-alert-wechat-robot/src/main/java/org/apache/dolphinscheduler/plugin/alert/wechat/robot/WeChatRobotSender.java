package org.apache.dolphinscheduler.plugin.alert.wechat.robot;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WeChatRobotSender {

    private final String url;
    private final String msgType;
    public WeChatRobotSender(Map<String, String> params) {
        url = params.get(WeChatRobotAlertParamsConstants.NAME_WECHAT_ROBOT_WEB_HOOK);
        msgType = params.get(WeChatRobotAlertParamsConstants.NAME_MSG_TYPE);
    }

    public AlertResult sendGroupChatMsg(String title, String content) {
        AlertResult alertResult;
        try {
            return sendMsg(title, content);
        } catch (Exception e) {
            log.info("send WeChat Group alert msg exception: {}", e.getMessage());
            alertResult = new AlertResult("false", "send WeChat Group alert fail.");
        }
        return alertResult;
    }

    private AlertResult sendMsg(String title, String content) throws IOException {
        String msg = generateMsg(title, content);
        HttpPost httpPost = constructHttpPost(url, msg);

        try (CloseableHttpClient httpClient = getDefaultClient()) {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String res = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new AlertResult("true", "success");
    }

    private static CloseableHttpClient getDefaultClient() {
        return HttpClients.createDefault();
    }

    private static HttpPost constructHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }

    private String generateMsg(String title, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("msgtype", msgType);
        Map<String, Object> text = new HashMap<>();
        text.put("content", String.join("\n", title, content));
        map.put(msgType, text);
        return JSONUtils.toJsonString(map);
    }
}
