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

package org.apache.dolphinscheduler.plugin.alert.script;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * ScriptAlertChannelFactoryTest
 */
public class ScriptAlertChannelFactoryTest {

    @Test
    public void testGetParams() {
        ScriptAlertChannelFactory scriptAlertChannelFactory = new ScriptAlertChannelFactory();
        List<PluginParams> params = scriptAlertChannelFactory.getParams();
        Assert.assertEquals(3, params.size());
    }

    @Test
    public void testCreate() {
        ScriptAlertChannelFactory scriptAlertChannelFactory = new ScriptAlertChannelFactory();
        AlertChannel alertChannel = scriptAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }
}
