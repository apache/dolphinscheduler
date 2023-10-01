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

package org.apache.dolphinscheduler.plugin.alert.feishu.personalwork;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeiShuPersonalWorkSenderTest {

    private static Map<String, String> config = new HashMap<>();

    @BeforeEach
    public void initFeiShuTalkPersonalWorkConfig() {
        config.put(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_ID, "app_id");
        config.put(FeiShuPersonalWorkParamConstants.NAME_PERSONAL_WORK_APP_SECRET, "app_secret");
        config.put(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID_TYPE, "email");
        config.put(FeiShuPersonalWorkParamConstants.NAME_RECEIVE_ID, "xxxx@xxx.com");
    }

    @AfterEach
    public void resetFeiShuPersonalWorkConfig() {
        config = new HashMap<>();
    }

    @Test
    public void testQueryAndSetAccessToken() throws IOException, URISyntaxException {
        FeiShuPersonalWorkSender sender = new FeiShuPersonalWorkSender(config);
        sender.queryAndSetAccessToken();
        Assertions.assertNull(sender.getAccessToken());
    }

    @Test
    public void testSendPersonalWorkMsg() {
        FeiShuPersonalWorkSender sender = new FeiShuPersonalWorkSender(config);
        AlertData alertData = new AlertData();
        alertData.setTitle("title");
        alertData.setContent("hello, this is a content");
        AlertResult alertResult = sender.sendPersonalWorkMsg(alertData);
        Assertions.assertEquals(alertResult.getStatus(), "false");
    }
}
