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

import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * EmailAlertChannelFactory Tester.
 *
 * @version 1.0
 * @since <pre>Aug 20, 2020</pre>
 */
public class EmailAlertChannelFactoryTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getName()
     */
    @Test
    public void testGetName() throws Exception {
    }

    /**
     * Method: getParams()
     */
    @Test
    public void testGetParams() throws Exception {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        List<PluginParams> params = emailAlertChannelFactory.getParams();
        JSONUtils.toJsonString(params);
        Assert.assertEquals(12, params.size());
    }

    /**
     * Method: create()
     */
    @Test
    public void testCreate() throws Exception {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        AlertChannel alertChannel = emailAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }

    @Test
    public void testLoadPlugins() throws Exception {
        System.out.println(System.getProperty("user.dir"));
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = DolphinPluginLoader.class.getClassLoader().getResource("").getPath();
        System.out.println(path);
        alertPluginManagerConfig.setPlugins(path + "../../pom.xml");
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
