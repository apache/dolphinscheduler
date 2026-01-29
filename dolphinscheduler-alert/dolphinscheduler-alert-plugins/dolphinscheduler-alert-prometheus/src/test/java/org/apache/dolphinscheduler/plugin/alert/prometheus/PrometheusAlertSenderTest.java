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

package org.apache.dolphinscheduler.plugin.alert.prometheus;

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrometheusAlertSenderTest {

    private static Map<String, String> config = new HashMap<>();

    @BeforeEach
    public void initConfig() {
        config.put(PrometheusAlertConstants.NAME_ALERT_MANAGER_URL, "http://127.0.0.1:9093");
        config.put(PrometheusAlertConstants.NAME_GENERATOR_URL, "localhost:8080");
        config.put(PrometheusAlertConstants.NAME_ALERT_MANAGER_ANNOTATIONS, "{\"annotation1\": \"string\"," +
                " \"annotation2\": \"string\"}");
    }

    @AfterEach
    public void resetConfig() {
        config = new HashMap<>();
    }

    @Test
    public void testSendAlert() {
        AlertData alertData = new AlertData();
        alertData.setTitle("[alertManager alert] test title");
        alertData.setContent("[{\n" +
                "      \"additionalProp1\": \"string\",\n" +
                "      \"additionalProp2\": \"string\",\n" +
                "    }]");
        PrometheusAlertSender sender = new PrometheusAlertSender(config);
        AlertResult result = sender.sendMessage(alertData);
        Assertions.assertFalse(result.isSuccess());
    }

    @Test
    public void testCheckSendAlertManageMsgResult() {
        PrometheusAlertSender prometheusAlertSender = new PrometheusAlertSender(config);
        AlertResult alertResult1 = prometheusAlertSender.checkSendAlertManageMsgResult("");
        Assertions.assertFalse(alertResult1.isSuccess());
        Assertions.assertEquals("prometheus alert manager send fail, resp is ", alertResult1.getMessage());
        AlertResult alertResult2 = prometheusAlertSender.checkSendAlertManageMsgResult("alert success");
        Assertions.assertTrue(alertResult2.isSuccess());
        Assertions.assertEquals("prometheus alert manager send success", alertResult2.getMessage());
    }
}
