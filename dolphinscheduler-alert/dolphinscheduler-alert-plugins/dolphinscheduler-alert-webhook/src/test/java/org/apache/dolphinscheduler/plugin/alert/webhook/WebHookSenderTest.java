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
