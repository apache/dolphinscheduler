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

package org.apache.dolphinscheduler.plugin.alert.webexteams;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebexTeamsSenderTest {

    private static final Map<String, String> webexTeamsConfig = new HashMap<>();

    private AlertData alertData;

    private String content = "[{" +
            "\"projectId\":90001," +
            "\"projectName\":\"test-k8s\"," +
            "\"owner\":\"test@cisco.com\"," +
            "\"processId\":90019," +
            "\"processDefinitionCode\":1111111," +
            "\"processName\":\"test-name\"," +
            "\"taskCode\":2222222," +
            "\"taskName\":\"test\"," +
            "\"taskType\":\"SQL\"," +
            "\"taskState\":\"FAILURE\"," +
            "\"taskStartTime\":\"2022-01-12 11:05:27\"," +
            "\"taskEndTime\":\"2022-01-12 11:05:28\"," +
            "\"taskHost\":\"dolphinscheduler-test\"," +
            "\"logPath\":\"test.log\"}]";

    @BeforeEach
    public void initDingTalkConfig() {
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_BOT_ACCESS_TOKEN, "accessToken");
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_ROOM_ID, "roomId");
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_EMAIL, "email");
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_TO_PERSON_ID, "id");
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_AT_SOMEONE_IN_ROOM, "email1,email2");
        alertData = new AlertData();
        alertData.setTitle("test");
        alertData.setContent(content);
    }

    @Test
    public void testSendToRoomId() {
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_DESTINATION,
                WebexTeamsDestination.ROOM_ID.getDescp());
        testSend();
    }

    @Test
    public void testSendToPersonEmail() {
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_DESTINATION,
                WebexTeamsDestination.PERSON_EMAIL.getDescp());
        testSend();
    }

    @Test
    public void testSendToPersonId() {
        webexTeamsConfig.put(WebexTeamsParamsConstants.NAME_WEBEX_TEAMS_DESTINATION,
                WebexTeamsDestination.PERSON_ID.getDescp());
        testSend();
    }

    public void testSend() {
        WebexTeamsSender webexTeamsSender = new WebexTeamsSender(webexTeamsConfig);
        AlertResult alertResult = webexTeamsSender.sendWebexTeamsAlter(alertData);
        Assertions.assertFalse(alertResult.isSuccess());
    }
}
