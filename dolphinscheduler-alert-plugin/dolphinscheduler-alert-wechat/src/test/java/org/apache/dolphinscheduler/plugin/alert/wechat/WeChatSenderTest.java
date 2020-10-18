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
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.alert.ShowType;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * WeChatSenderTest
 */
public class WeChatSenderTest {

    private static Map<String, String> weChatConfig = new HashMap<>();

    @Before
    public void initDingTalkConfig() {
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID, "1234");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID, "1234");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_PUSH_URL, "https://www.*");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET, "SECRET");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_TOKEN_URL, "https://token.url");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USER_SEND_MSG, "msg");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS, "user");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_TEAM_SEND_MSG, "msg");
        weChatConfig.put(AlertConstants.SHOW_TYPE, ShowType.TABLE.getDescp());
    }

    @Test
    public void testSendWeChatTableMsg() {
        WeChatSender weChatSender = new WeChatSender(weChatConfig);
        String content = "[{\"name\":\"ds name\",\"value\":\"ds value\"}]";
        AlertResult alertResult = weChatSender.sendEnterpriseWeChat("test", content);
        Assert.assertEquals(alertResult.getStatus(), "false");
    }

    @Test
    public void testSendWeChatTextMsg() {
        weChatConfig.put(AlertConstants.SHOW_TYPE, ShowType.TEXT.getDescp());
        WeChatSender weChatSender = new WeChatSender(weChatConfig);
        String content = "[{\"name\":\"ds name\",\"value\":\"ds value\"}]";
        AlertResult alertResult = weChatSender.sendEnterpriseWeChat("test", content);
        Assert.assertEquals(alertResult.getStatus(), "false");
    }


}
