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

import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

public class DataSourceClientManager {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceClientManager.class);

    private static final long duration = PropertyUtils.getLong(TaskConstants.KERBEROS_EXPIRE_TIME, 24);
    private static final Cache<String, DataSourceClient> uniqueId2dataSourceClientCache = CacheBuilder.newBuilder()
        .expireAfterWrite(duration, TimeUnit.HOURS)
        .removalListener((RemovalListener<String, DataSourceClient>) notification -> {
            try (DataSourceClient closedClient = notification.getValue()) {
                logger.info("Datasource: {} is removed from cache due to expire", notification.getKey());
            }
        })
        .maximumSize(100)
        .build();
    private DataSourcePluginManager dataSourcePluginManager;

    private DataSourceClientManager() {
        initDataSourcePlugin();
    }

    private static class DataSourceClientManagerHolder {
        private static final DataSourceClientManager INSTANCE = new DataSourceClientManager();
    }

    public static DataSourceClientManager getInstance() {
        return DataSourceClientManagerHolder.INSTANCE;
    }

    public DataSourceClient getDataSource(DbType dbType, ConnectionParam connectionParam) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        String datasourceUniqueId = DataSourceUtils.getDatasourceUniqueId(baseConnectionParam, dbType);
        logger.info("Get connection from datasource {}", datasourceUniqueId);

        try {
            return uniqueId2dataSourceClientCache.get(datasourceUniqueId, () -> {
                Map<String, DataSourceChannel> dataSourceChannelMap = dataSourcePluginManager.getDataSourceChannelMap();
                DataSourceChannel dataSourceChannel = dataSourceChannelMap.get(dbType.getDescp());
                if (null == dataSourceChannel) {
                    throw new RuntimeException(String.format("datasource plugin '%s' is not found", dbType.getDescp()));
                }
                return dataSourceChannel.createDataSourceClient(baseConnectionParam, dbType);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("datasourceUniqueId '%s' is not found datasource", datasourceUniqueId));
        }
    }

    private void initDataSourcePlugin() {
        dataSourcePluginManager = new DataSourcePluginManager();
        dataSourcePluginManager.installPlugin();
    }
}
