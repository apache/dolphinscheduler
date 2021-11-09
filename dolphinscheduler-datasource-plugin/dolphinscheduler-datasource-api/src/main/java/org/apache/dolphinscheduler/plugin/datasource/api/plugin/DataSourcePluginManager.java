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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;
import org.apache.dolphinscheduler.spi.exception.PluginException;
import org.apache.dolphinscheduler.spi.plugin.AbstractDolphinPluginManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourcePluginManager extends AbstractDolphinPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(DataSourcePluginManager.class);

    private final Map<String, DataSourceChannelFactory> datasourceClientFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, DataSourceChannel> datasourceClientMap = new ConcurrentHashMap<>();

    public Map<String, DataSourceChannel> getDataSourceChannelMap() {
        return datasourceClientMap;
    }

    private void addDatasourceClientFactory(DataSourceChannelFactory datasourceChannelFactory) {
        requireNonNull(datasourceChannelFactory, "datasourceChannelFactory is null");

        if (datasourceClientFactoryMap.putIfAbsent(datasourceChannelFactory.getName(), datasourceChannelFactory) != null) {
            throw PluginException.getInstance(format("Datasource Plugin '%s' is already registered", datasourceChannelFactory.getName()));
        }

        try {
            loadDatasourceClient(datasourceChannelFactory.getName());
        } catch (Exception e) {
            throw PluginException.getInstance(format("Datasource Plugin '%s' is can not load .", datasourceChannelFactory.getName()));
        }
    }

    private void loadDatasourceClient(String name) {
        requireNonNull(name, "name is null");

        DataSourceChannelFactory datasourceChannelFactory = datasourceClientFactoryMap.get(name);
        checkState(datasourceChannelFactory != null, "datasource Plugin {} is not registered", name);

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(datasourceChannelFactory.getClass().getClassLoader())) {
            DataSourceChannel datasourceChannel = datasourceChannelFactory.create();
            this.datasourceClientMap.put(name, datasourceChannel);
        }

        logger.info("-- Loaded datasource Plugin {} --", name);
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (DataSourceChannelFactory datasourceChannelFactory : dolphinSchedulerPlugin.getDatasourceChannelFactorys()) {
            logger.info("Registering datasource Plugin '{}'", datasourceChannelFactory.getName());
            this.addDatasourceClientFactory(datasourceChannelFactory);
        }
    }
}
