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

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourcePluginManager {

    private final Map<String, DataSourceChannel> datasourceChannelMap = new ConcurrentHashMap<>();

    public Map<String, DataSourceChannel> getDataSourceChannelMap() {
        return Collections.unmodifiableMap(datasourceChannelMap);
    }

    public void installPlugin() {

        PrioritySPIFactory<DataSourceChannelFactory> prioritySPIFactory =
                new PrioritySPIFactory<>(DataSourceChannelFactory.class);
        for (Map.Entry<String, DataSourceChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            final DataSourceChannelFactory factory = entry.getValue();
            final String name = entry.getKey();

            log.info("Registering datasource plugin: {}", name);

            if (datasourceChannelMap.containsKey(name)) {
                throw new IllegalStateException(format("Duplicate datasource plugins named '%s'", name));
            }

            loadDatasourceClient(factory);

            log.info("Registered datasource plugin: {}", name);
        }
    }

    private void loadDatasourceClient(DataSourceChannelFactory datasourceChannelFactory) {
        DataSourceChannel datasourceChannel = datasourceChannelFactory.create();
        datasourceChannelMap.put(datasourceChannelFactory.getName(), datasourceChannel);
    }
}
