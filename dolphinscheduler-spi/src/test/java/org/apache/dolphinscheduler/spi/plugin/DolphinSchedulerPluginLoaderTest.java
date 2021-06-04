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

package org.apache.dolphinscheduler.spi.plugin;

import java.util.Objects;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DolphinSchedulerPluginLoaderTest {

    /**
     * Method: loadPlugins()
     *
     * The spi module cannot test specific plug-ins and should be mocked. However, the current loading method is not easy to implement, although it is ultimately the service load method.
     */
    @Test
    public void testLoadPlugins() {
        PluginManagerTest pluginManager = new PluginManagerTest();
        DolphinPluginManagerConfig registryPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = Objects.requireNonNull(DolphinPluginLoader.class.getClassLoader().getResource("")).getPath();
        registryPluginManagerConfig.setPlugins(path + "../../../dolphinscheduler-registry-plugin/dolphinscheduler-registry-zookeeper/pom.xml");
        DolphinPluginLoader registryPluginLoader = new DolphinPluginLoader(registryPluginManagerConfig, ImmutableList.of(pluginManager));
        try {
            registryPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load registry Plugin Failed !", e);
        }
    }
}
