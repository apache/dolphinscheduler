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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PagerDutyAlertChannelFactoryTest {

    @Test
    public void testGetParams() {
        PagerDutyAlertChannelFactory pagerDutyAlertChannelFactory = new PagerDutyAlertChannelFactory();
        List<PluginParams> params = pagerDutyAlertChannelFactory.params();
        JSONUtils.toJsonString(params);
        Assertions.assertEquals(1, params.size());
    }

    @Test
    public void testCreate() {
        PagerDutyAlertChannelFactory pagerDutyAlertChannelFactory = new PagerDutyAlertChannelFactory();
        AlertChannel alertChannel = pagerDutyAlertChannelFactory.create();
        Assertions.assertNotNull(alertChannel);
    }
}
