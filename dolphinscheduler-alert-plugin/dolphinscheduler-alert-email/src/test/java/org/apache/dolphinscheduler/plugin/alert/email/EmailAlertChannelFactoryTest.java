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

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * EmailAlertChannelFactory Tester.
 *
 * @version 1.0
 * @since <pre>Aug 20, 2020</pre>
 */
public class EmailAlertChannelFactoryTest {

    /**
     * Method: getParams()
     */
    @Test
    public void testGetParams() {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        List<PluginParams> params = emailAlertChannelFactory.getParams();
        Assert.assertEquals(12, params.size());
    }

    /**
     * Method: create()
     */
    @Test
    public void testCreate() {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        AlertChannel alertChannel = emailAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }

}
