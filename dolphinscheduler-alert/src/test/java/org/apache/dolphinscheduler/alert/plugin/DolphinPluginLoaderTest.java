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

import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * DolphinPluginLoader Tester.
 */
@Ignore("load jar fail,don't care plugin,should mock plugin")
public class DolphinPluginLoaderTest {

    /**
     * Method: loadPlugins()
     */
    @Test
    public void testLoadPlugins() {
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = Objects.requireNonNull(DolphinPluginLoader.class.getClassLoader().getResource("")).getPath();
        alertPluginManagerConfig.setPlugins(path + "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml");
        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }

        Assert.assertNotNull(alertPluginManager.getAlertChannelFactoryMap().get("Email"));
    }
}
