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

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourcePluginManager {

    private static final Map<String, DataSourceChannel> datasourceChannelMap = new ConcurrentHashMap<>();

    private static final Map<String, DataSourceProcessor> dataSourceProcessorMap = new ConcurrentHashMap<>();

    static {
        loadDataSourcePlugin();
    }

    public static DataSourceChannel getDataSourceChannel(@NonNull DbType dbType) {
        return datasourceChannelMap.get(dbType.getName());
    }

    public static DataSourceProcessor getDataSourceProcessor(@NonNull DbType dbType) {
        return dataSourceProcessorMap.get(dbType.getName());
    }

    public static void loadDataSourcePlugin() {
        initializeDataSourceChannel();
        initializeDataSourceProcessor();
    }

    private static synchronized void initializeDataSourceChannel() {
        if (MapUtils.isNotEmpty(datasourceChannelMap)) {
            return;
        }
        new PrioritySPIFactory<>(DataSourceChannelFactory.class).getSPIMap().forEach(
                (dataSourceChannelName, dataSourceChannelFactory) -> {
                    if (datasourceChannelMap.containsKey(dataSourceChannelName)) {
                        throw new IllegalStateException(
                                format("Duplicate datasource channel named '%s'", dataSourceChannelName));
                    }
                    datasourceChannelMap.put(dataSourceChannelName, dataSourceChannelFactory.create());
                    log.info("Registered datasource channel: {}", dataSourceChannelName);
                });
    }

    private static synchronized void initializeDataSourceProcessor() {
        if (MapUtils.isNotEmpty(dataSourceProcessorMap)) {
            return;
        }

        ServiceLoader.load(DataSourceProcessor.class).forEach(factory -> {
            final String name = factory.getDbType().getName();
            if (dataSourceProcessorMap.containsKey(name)) {
                throw new IllegalStateException(format("Duplicate datasource processor named '%s'", name));
            }
            DataSourceProcessor dataSourceProcessor = factory.create();
            dataSourceProcessorMap.put(name, dataSourceProcessor);
            log.info("Success register datasource processor -> {}", name);
        });
    }

}
