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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeiShuSenderTest {

    private static Map<String, String> feiShuConfig = new HashMap<>();

    @BeforeEach
    public void initFeiShuConfig() {
        feiShuConfig.put(FeiShuParamsConstants.WEB_HOOK, "https://open.feishu.cn/open-apis/bot/v2/hook/xxxxx");
    }

    @Test
    public void testSend() {
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.sendFeiShuMsg(alertData);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testFormatContent() {
        String alertMsg = "[\n"
                + "  {\n"
                + "    \"owner\": \"dolphinscheduler\",\n"
                + "    \"processEndTime\": \"2021-01-29 19:01:11\",\n"
                + "    \"processHost\": \"10.81.129.4:5678\",\n"
                + "    \"processId\": 2926,\n"
                + "    \"processName\": \"3-20210129190038108\",\n"
                + "    \"processStartTime\": \"2021-01-29 19:00:38\",\n"
                + "    \"processState\": \"SUCCESS\",\n"
                + "    \"processType\": \"START_PROCESS\",\n"
                + "    \"projectId\": 2,\n"
                + "    \"projectName\": \"testdelproject\",\n"
                + "    \"recovery\": \"NO\",\n"
                + "    \"retryTimes\": 0,\n"
                + "    \"runTimes\": 1,\n"
                + "    \"taskId\": 0\n"
                + "  }\n"
                + "]";
        AlertData alertData = new AlertData();
        alertData.setTitle("");
        alertData.setContent(alertMsg);
        Assertions.assertNotNull(FeiShuSender.formatContent(alertData));
    }

    @Test
    public void testSendWithFormatException() {
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        String alertResult = feiShuSender.formatContent(alertData);
        Assertions.assertEquals(alertResult, alertData.getTitle() + alertData.getContent());
    }

    @Test
    public void testCheckSendFeiShuSendMsgResult() {

        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.checkSendFeiShuSendMsgResult("");
        Assertions.assertFalse(alertResult.isSuccess());
        AlertResult alertResult2 = feiShuSender.checkSendFeiShuSendMsgResult("123");
        Assertions.assertEquals("send fei shu msg fail", alertResult2.getMessage());

        String response = "{\"StatusCode\":\"0\",\"extra\":\"extra\",\"StatusMessage\":\"StatusMessage\"}";
        AlertResult alertResult3 = feiShuSender.checkSendFeiShuSendMsgResult(response);
        Assertions.assertTrue(alertResult3.isSuccess());
    }
}
