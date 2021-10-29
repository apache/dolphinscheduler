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

package org.apache.dolphinscheduler.plugin.datasource.api.plugin;

import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSourcePluginManager Test
 */
public class DataSourcePluginManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(DataSourcePluginManagerTest.class);

    @Test
    public void testLoadPlugins() {
        logger.info("begin test DataSourcePluginManagerTest");
        DataSourcePluginManager dataSourcePluginManager = new DataSourcePluginManager();
        DolphinPluginManagerConfig datasourcePluginManagerConfig = new DolphinPluginManagerConfig();
        String path = Objects.requireNonNull(DolphinPluginLoader.class.getClassLoader().getResource("")).getPath();
        datasourcePluginManagerConfig.setPlugins(path + "../../../dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-sqlserver/pom.xml");

        if (StringUtils.isNotBlank(PropertyUtils.getString(Constants.DATASOURCE_PLUGIN_DIR, Constants.DATASOURCE_PLUGIN_PATH))) {
            datasourcePluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(Constants.DATASOURCE_PLUGIN_DIR, Constants.DATASOURCE_PLUGIN_PATH));

        }
        if (StringUtils.isNotBlank(PropertyUtils.getString(Constants.MAVEN_LOCAL_REPOSITORY))) {
            datasourcePluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(Constants.MAVEN_LOCAL_REPOSITORY).trim());
        }

        Assert.assertNull(dataSourcePluginManager.getDataSourceChannelMap().get("sqlserver"));
    }
}
