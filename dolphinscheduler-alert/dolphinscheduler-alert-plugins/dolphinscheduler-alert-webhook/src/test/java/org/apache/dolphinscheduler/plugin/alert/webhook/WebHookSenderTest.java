package org.apache.dolphinscheduler.plugin.alert.webhook;

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebHookSenderTest {

    @Test
    public void sendTest() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(WebHookAlertConstants.WEBHOOK_URL, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxx");
        paramsMap.put(WebHookAlertConstants.CONTENT_KEY, "test webhook msg send");
        paramsMap.put(AlertConstants.NAME_SHOW_TYPE, "text");
        WebHookSender webHookSender = new WebHookSender(paramsMap);
        AlertResult alertResult = webHookSender.send("title", "content");
        Assertions.assertEquals("true", alertResult.getStatus());
    }
}
