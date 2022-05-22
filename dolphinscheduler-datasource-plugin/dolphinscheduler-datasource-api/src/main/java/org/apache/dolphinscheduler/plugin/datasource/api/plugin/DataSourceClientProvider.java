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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class DataSourceClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceClientProvider.class);

    private static long duration = PropertyUtils.getLong(TaskConstants.KERBEROS_EXPIRE_TIME, 24);
    private static final Cache<String, DataSourceClient> uniqueId2dataSourceClientCache = CacheBuilder.newBuilder()
            .expireAfterWrite(duration, TimeUnit.HOURS)
            .maximumSize(100)
            .build();
    private DataSourcePluginManager dataSourcePluginManager;

    private DataSourceClientProvider() {
        initDataSourcePlugin();
    }

    private static class DataSourceClientProviderHolder {
        private static final DataSourceClientProvider INSTANCE = new DataSourceClientProvider();
    }

    public static DataSourceClientProvider getInstance() {
        return DataSourceClientProviderHolder.INSTANCE;
    }

    public Connection getConnection(DbType dbType, ConnectionParam connectionParam) throws ExecutionException {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        String datasourceUniqueId = DataSourceUtils.getDatasourceUniqueId(baseConnectionParam, dbType);
        logger.info("getConnection datasourceUniqueId {}", datasourceUniqueId);

        DataSourceClient dataSourceClient = uniqueId2dataSourceClientCache.get(datasourceUniqueId, () -> {
            Map<String, DataSourceChannel> dataSourceChannelMap = dataSourcePluginManager.getDataSourceChannelMap();
            DataSourceChannel dataSourceChannel = dataSourceChannelMap.get(dbType.getDescp());
            if (null == dataSourceChannel) {
                throw new RuntimeException(String.format("datasource plugin '%s' is not found", dbType.getDescp()));
            }
            return dataSourceChannel.createDataSourceClient(baseConnectionParam, dbType);
        });
        return dataSourceClient.getConnection();
    }

    private void initDataSourcePlugin() {
        dataSourcePluginManager = new DataSourcePluginManager();
        dataSourcePluginManager.installPlugin();
    }
}
