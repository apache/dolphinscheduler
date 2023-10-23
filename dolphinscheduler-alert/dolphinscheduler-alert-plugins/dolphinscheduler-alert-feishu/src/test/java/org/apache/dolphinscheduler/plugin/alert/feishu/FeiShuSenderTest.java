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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeiShuSenderTest {

    private static Map<String, String> feiShuConfig = new HashMap<>();

    @AfterEach
    public void resetFeiShuPersonalWorkConfig() {
        feiShuConfig = new HashMap<>();
    }

    @Test
    public void testSendCustomRobotMassage() {
        feiShuConfig.put(FeiShuParamsConstants.NAME_WEB_HOOK_OR_APP_ID,
                "https://open.feishu.cn/open-apis/bot/v2/hook/xxxxx");
        feiShuConfig.put(FeiShuParamsConstants.NAME_FEI_SHU_SEND_TYPE, FeiShuType.CUSTOM_ROBOT.getDescp());
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.sendFeiShuMsg(alertData);
        Assertions.assertEquals("false", alertResult.getStatus());
    }

    @Test
    public void testSendApplicationMassage() {
        feiShuConfig.put(FeiShuParamsConstants.NAME_FEI_SHU_SEND_TYPE, FeiShuType.APPLIANCE_ROBOT.getDescp());
        feiShuConfig.put(FeiShuParamsConstants.NAME_WEB_HOOK_OR_APP_ID, "appid");
        feiShuConfig.put(FeiShuParamsConstants.NAME_PERSONAL_WORK_APP_SECRET, "app_secret");
        feiShuConfig.put(FeiShuParamsConstants.NAME_RECEIVE_ID_TYPE, "email");
        feiShuConfig.put(FeiShuParamsConstants.NAME_RECEIVE_ID, "xxxx@xxx.com");
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.sendFeiShuMsg(alertData);
        Assertions.assertEquals("false", alertResult.getStatus());
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
    public void testQueryAndSetAccessToken() throws IOException {
        feiShuConfig.put(FeiShuParamsConstants.NAME_WEB_HOOK_OR_APP_ID, "appid");
        feiShuConfig.put(FeiShuParamsConstants.NAME_PERSONAL_WORK_APP_SECRET, "app_secret");
        FeiShuSender sender = new FeiShuSender(feiShuConfig);
        sender.queryAndSetAccessToken();
        Assertions.assertNull(sender.getAccessToken());
    }

    @Test
    public void testSendWithFormatException() {
        AlertData alertData = new AlertData();
        alertData.setTitle("feishu test title");
        alertData.setContent("feishu test content");
        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        String alertResult = feiShuSender.formatContent(alertData);
        Assertions.assertEquals(alertResult, alertData.getTitle() + "\n" + alertData.getContent());
    }

    @Test
    public void testCheckSendCustomRobotMsgResult() {

        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.checkSendCustomRobotMsgResult("");
        Assertions.assertFalse(Boolean.valueOf(alertResult.getStatus()));
        AlertResult alertResult2 = feiShuSender.checkSendCustomRobotMsgResult("123");
        Assertions.assertEquals("send fei shu msg fail", alertResult2.getMessage());

        String response = "{\"StatusCode\":\"0\",\"extra\":\"extra\",\"StatusMessage\":\"StatusMessage\"}";
        AlertResult alertResult3 = feiShuSender.checkSendCustomRobotMsgResult(response);
        Assertions.assertTrue(Boolean.valueOf(alertResult3.getStatus()));
    }

    @Test
    public void testCheckSendApplicationRobotMsgResult() {

        FeiShuSender feiShuSender = new FeiShuSender(feiShuConfig);
        AlertResult alertResult = feiShuSender.checkSendApplicationRobotMsgResult("");
        Assertions.assertFalse(Boolean.valueOf(alertResult.getStatus()));
        AlertResult alertResult2 = feiShuSender.checkSendApplicationRobotMsgResult("123");
        Assertions.assertEquals("send FeiShu personal work msg fail", alertResult2.getMessage());

        String response = "{\"code\":\"0\",\"msg\":\"msg\",\"data\":\"data\"}";
        AlertResult alertResult3 = feiShuSender.checkSendApplicationRobotMsgResult(response);
        Assertions.assertTrue(Boolean.valueOf(alertResult3.getStatus()));
    }
}
