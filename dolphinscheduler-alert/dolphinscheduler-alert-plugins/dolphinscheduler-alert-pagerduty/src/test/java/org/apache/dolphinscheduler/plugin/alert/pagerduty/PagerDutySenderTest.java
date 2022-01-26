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

package org.apache.dolphinscheduler.plugin.alert.pagerduty;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PagerDutySenderTest {
    private static final Map<String, String> pagerDutyConfig = new HashMap<>();

    @Before
    public void initDingTalkConfig() {
        pagerDutyConfig.put(PagerDutyParamsConstants.NAME_PAGER_DUTY_INTEGRATION_KEY_NAME, "test");
    }

    @Test
    public void testSend() {
        PagerDutySender pagerDutySender = new PagerDutySender(pagerDutyConfig);
        AlertResult alertResult = pagerDutySender.sendPagerDutyAlter("pagerduty test title", "pagerduty test content");
        Assert.assertEquals("false", alertResult.getStatus());
    }
}
