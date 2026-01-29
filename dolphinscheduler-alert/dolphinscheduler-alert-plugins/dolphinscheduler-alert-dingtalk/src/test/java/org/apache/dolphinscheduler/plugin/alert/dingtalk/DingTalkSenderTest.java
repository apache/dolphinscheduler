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

package org.apache.dolphinscheduler.plugin.alert.dingtalk;

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DingTalkSenderTest {

    private static final Map<String, String> dingTalkConfig = new HashMap<>();

    @BeforeEach
    public void initDingTalkConfig() {

        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_KEYWORD, "keyword");
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_WEB_HOOK, "url");
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_MSG_TYPE,
                DingTalkParamsConstants.DING_TALK_MSG_TYPE_MARKDOWN);

        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE, "false");
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_PASSWORD, "password");
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_PORT, "9988");
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_USER, "user1,user2");
    }

    @Test
    public void testSend() {
        DingTalkSender dingTalkSender = new DingTalkSender(dingTalkConfig);
        dingTalkSender.sendDingTalkMsg("keyWord+Welcome", StandardCharsets.UTF_8.name());
        dingTalkConfig.put(DingTalkParamsConstants.NAME_DING_TALK_PROXY_ENABLE, "true");
        dingTalkSender = new DingTalkSender(dingTalkConfig);
        AlertResult alertResult = dingTalkSender.sendDingTalkMsg("title", "content test");
        Assertions.assertEquals(false, alertResult.isSuccess());
    }

}
