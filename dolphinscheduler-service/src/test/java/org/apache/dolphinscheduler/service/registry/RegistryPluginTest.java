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

package org.apache.dolphinscheduler.service.registry;

import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.register.RegistryPluginManager;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class RegistryPluginTest {

    @Test
    public void testLoadPlugin() throws Exception {
        DolphinPluginManagerConfig registryPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = DolphinPluginLoader.class.getClassLoader().getResource("").getPath();

        String registryPluginZkPath = path + "../../../dolphinscheduler-registry-plugin/dolphinscheduler-registry-zookeeper/pom.xml";
        registryPluginManagerConfig.setPlugins(registryPluginZkPath);
        RegistryPluginManager registryPluginManager = new RegistryPluginManager("zookeeper");

        DolphinPluginLoader registryPluginLoader = new DolphinPluginLoader(registryPluginManagerConfig, ImmutableList.of(registryPluginManager));
        registryPluginLoader.loadPlugins();
        Assert.assertNotNull(registryPluginManager.getRegistry());

    }
}
