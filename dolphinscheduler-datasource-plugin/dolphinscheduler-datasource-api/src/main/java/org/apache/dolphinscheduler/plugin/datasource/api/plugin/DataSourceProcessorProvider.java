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

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Map;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceProcessorProvider {

    private static final DataSourceProcessorManager dataSourcePluginManager = new DataSourceProcessorManager();

    static {
        dataSourcePluginManager.installProcessor();
    }

    private DataSourceProcessorProvider() {
    }

    public static DataSourceProcessor getDataSourceProcessor(@NonNull DbType dbType) {
        return dataSourcePluginManager.getDataSourceProcessorMap().get(dbType.name());
    }

    public static Map<String, DataSourceProcessor> getDataSourceProcessorMap() {
        return dataSourcePluginManager.getDataSourceProcessorMap();
    }

}
