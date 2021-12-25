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

import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourcePluginManager {
    private static final Logger logger = LoggerFactory.getLogger(DataSourcePluginManager.class);

    private static final Map<String, DataSourceClient> uniqueId2dataSourceClientMap = new ConcurrentHashMap<>();

    public static Connection getConnection(JdbcConnectionParam jdbcConnectionParam) {
        String datasourceUniqueId = jdbcConnectionParam.getDatasourceUniqueId();
        logger.info("getConnection datasourceUniqueId {}", datasourceUniqueId);

        DataSourceClient dataSourceClient = uniqueId2dataSourceClientMap.computeIfAbsent(datasourceUniqueId, $ ->
                DataSourceClientProvider.createDataSourceClient(jdbcConnectionParam));
        return dataSourceClient.getConnection();
    }
}
