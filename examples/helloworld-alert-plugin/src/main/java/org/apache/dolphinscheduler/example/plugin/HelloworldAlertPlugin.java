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
package org.apache.dolphinscheduler.example.plugin;

import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.PluginName;
import org.apache.dolphinscheduler.plugin.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HelloworldAlertPlugin implements AlertPlugin {

    private static final Logger logger = LoggerFactory.getLogger(HelloworldAlertPlugin.class);

    private PluginName pluginName;

    public HelloworldAlertPlugin() {
        pluginName = new PluginName();
        pluginName.setEnglish("helloworld")
                .setChinese("你好世界");
    }

    @Override
    public String getId() {
        return "helloworld";
    }

    @Override
    public PluginName getName() {
        return pluginName;
    }

    @Override
    public Map<String, Object> process(AlertInfo info) {
        logger.info("{}", PropertyUtils.getInt("helloworld.int"));
        logger.info(PropertyUtils.getString("helloworld.string"));
        logger.info(info.getAlertData().getTitle());
        return new HashMap<>();
    }
}
