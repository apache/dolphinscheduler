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
package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertData;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.PluginName;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EmailAlertPluginTest {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertPluginTest.class);

    private AlertPlugin plugin;

    @Before
    public void before() {
        plugin = new EmailAlertPlugin();
    }

    @Test
    public void getId() {
        String id = plugin.getId();
        assertEquals(Constants.PLUGIN_DEFAULT_EMAIL, id);
    }

    @Test
    public void getName() {
        PluginName pluginName = plugin.getName();
        assertEquals(Constants.PLUGIN_DEFAULT_EMAIL_CH, pluginName.getChinese());
        assertEquals(Constants.PLUGIN_DEFAULT_EMAIL_EN, pluginName.getEnglish());
    }

    @Test
    public void process() {
        AlertInfo alertInfo = new AlertInfo();
        AlertData alertData = new AlertData();
        alertData.setId(1)
                .setAlertGroupId(1)
                .setContent("[\"alarm time：2018-02-05\", \"service name：MYSQL_ALTER\", \"alarm name：MYSQL_ALTER_DUMP\", " +
                        "\"get the alarm exception.！，interface error，exception information：timed out\", \"request address：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]")
                .setLog("test log")
                .setReceivers("xx@xx.com")
                .setReceiversCc("xx@xx.com")
                .setShowType(ShowType.TEXT.getDescp())
                .setTitle("test title");

        alertInfo.setAlertData(alertData);
        List<String> list = new ArrayList<String>(){{ add("xx@xx.com"); }};
        alertInfo.addProp("receivers", list);
        Map<String, Object> ret = plugin.process(alertInfo);
        assertFalse(Boolean.parseBoolean(String.valueOf(ret.get(Constants.STATUS))));
    }
}