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

package org.apache.dolphinscheduler.plugin.alert.slack;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SlackSenderTest {

    @Test
    public void testSendMessage() {
        Map<String, String> alertparam = new HashMap<>();
        alertparam.put(SlackParamsConstants.SLACK_WEB_HOOK_URL_NAME,
                "https://hooks.slack.com/services/123456");
        alertparam.put(SlackParamsConstants.SLACK_BOT_NAME, "Dolphinscheduler");

        SlackSender slackSender = new SlackSender(alertparam);
        String response = slackSender.sendMessage("test title", "test content");
        Assertions.assertNotEquals("ok", response);
    }
}
