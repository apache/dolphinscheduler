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

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.ShowType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WeChatSenderTest
 */
public class WeChatSenderTest {

    private static Map<String, String> weChatConfig = new HashMap<>();

    private String content = "[{\"id\":\"69\","
            +
            "\"name\":\"UserBehavior-0--1193959466\","
            +
            "\"Job name\":\"Start workflow\","
            +
            "\"State\":\"SUCCESS\","
            +
            "\"Recovery\":\"NO\","
            +
            "\"Run time\":\"1\","
            +
            "\"Start time\": \"2018-08-06 10:31:34.0\","
            +
            "\"End time\": \"2018-08-06 10:31:49.0\","
            +
            "\"Host\": \"192.168.xx.xx\","
            +
            "\"Notify group\" :\"4\"}]";

    @BeforeEach
    public void initDingTalkConfig() {
        // Just for this test, I will delete these configurations before this PR is merged
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID, "100000");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID, "NAME_ENTERPRISE_WE_CHAT_CORP_ID");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET, "NAME_ENTERPRISE_WE_CHAT_SECRET");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS, "Kris");
        weChatConfig.put(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_TEAM_SEND_MSG, "msg");
        weChatConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TABLE.getDescp());
    }

    @Test
    public void testSendWeChatTableMsg() {
        WeChatSender weChatSender = new WeChatSender(weChatConfig);

        AlertResult alertResult = weChatSender.sendEnterpriseWeChat("test", content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testSendWeChatTextMsg() {
        weChatConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        WeChatSender weChatSender = new WeChatSender(weChatConfig);
        AlertResult alertResult = weChatSender.sendEnterpriseWeChat("test", content);
        Assertions.assertFalse(alertResult.isSuccess());
    }

}
