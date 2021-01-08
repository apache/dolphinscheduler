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

import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * AlertPluginManager Tester.
 */
@Ignore
public class AlertPluginManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginManagerTest.class);

    @Test
    public void testLoadPlugins() throws Exception {
        logger.info("begin test AlertPluginManagerTest");
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = DolphinPluginLoader.class.getClassLoader().getResource("").getPath();
        alertPluginManagerConfig.setPlugins(path + "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml");
        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(org.apache.dolphinscheduler.alert.utils.PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR, Constants.ALERT_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }

        Assert.assertNotNull(alertPluginManager.getAlertChannelFactoryMap().get("email alert"));
    }
}
