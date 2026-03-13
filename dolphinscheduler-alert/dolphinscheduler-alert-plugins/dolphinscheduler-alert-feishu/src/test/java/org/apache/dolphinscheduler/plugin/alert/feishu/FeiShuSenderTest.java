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

}
