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

package org.apache.dolphinscheduler.plugin.alert.dingtalk.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DinkTalkPersonalWorkSenderTest {

    private static Map<String, String> config = new HashMap<>();

    @BeforeEach
    public void initDingTalkPersonalWorkConfig() {
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_KEY, "key");
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_APP_SECRET,
                "secret");
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_ROBOT_CODE, "code");
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_USER_IDS, "userids");
    }

    @AfterEach
    public void resetDingTalkPersonalWorkConfig() {
        config = new HashMap<>();
    }

    @Test
    public void testQueryAndSetAccessToken() throws IOException {
        DingTalkPersonalWorkSender sender = new DingTalkPersonalWorkSender(config);
        sender.queryAndSetAccessToken();
        Assertions.assertNull(sender.getAccessToken());
    }

    @Test
    public void testSendSampleText() {
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_MSG_KEY, "sampleText");

        DingTalkPersonalWorkSender dingTalkSender = new DingTalkPersonalWorkSender(config);
        AlertResult alertResult = dingTalkSender.sendPersonalWorkMsg("title", "content");
        Assertions.assertEquals(alertResult.getStatus(), "false");
    }

    @Test
    public void testSendSampleMarkdown() {
        config.put(DingTalkPersonalWorkParamConstants.NAME_DING_TALK_PERSONAL_WORK_MSG_KEY, "sampleMarkdown");

        DingTalkPersonalWorkSender dingTalkSender = new DingTalkPersonalWorkSender(config);
        AlertResult alertResult = dingTalkSender.sendPersonalWorkMsg("title", "content");
        Assertions.assertEquals(alertResult.getStatus(), "false");
    }
}
