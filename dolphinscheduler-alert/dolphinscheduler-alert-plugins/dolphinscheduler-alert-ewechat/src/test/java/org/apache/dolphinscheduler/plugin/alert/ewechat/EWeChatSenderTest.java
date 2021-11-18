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

package org.apache.dolphinscheduler.plugin.alert.ewechat;

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EWeChatSenderTest
 */
public class EWeChatSenderTest {

    private static Map<String, String> eWeChatConfig = new HashMap<>();

    @Before
    public void initDingTalkConfig() {
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_WEB_HOOK, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxx");
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY_ENABLE, "false");
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_PASSWD, "passwd");
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_PORT, "9988");
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_USER, "user1,user2");
    }

    @Test
    public void testSend() {
        EWeChatSender eWeChatSender = new EWeChatSender(eWeChatConfig);
        String title = "start process success";
        String content = "[{\"projectId\":1,\"projectName\":\"test\",\"owner\":\"admin\",\"processId\":17,\"processName\":\"test_wechat-1-20211110112316488\"}]";
        eWeChatSender.sendEWeChatMsg(title, content);
        eWeChatConfig.put(EWeChatParamsConstants.NAME_EWE_CHAT_PROXY_ENABLE, "true");
        eWeChatSender = new EWeChatSender(eWeChatConfig);
        AlertResult alertResult = eWeChatSender.sendEWeChatMsg("title", "content test");
        Assert.assertEquals("false", alertResult.getStatus());
    }
}