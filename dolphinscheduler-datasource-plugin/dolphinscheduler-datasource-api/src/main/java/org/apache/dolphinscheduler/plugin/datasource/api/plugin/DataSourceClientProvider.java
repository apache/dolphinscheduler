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

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.datasource.AdHocDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.PooledDataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

@Slf4j
public class DataSourceClientProvider {

    // We use the cache here to avoid creating a new datasource client every time,
    // One DataSourceClient corresponds to one unique datasource.
    private static final Cache<String, PooledDataSourceClient> POOLED_DATASOURCE_CLIENT_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(PropertyUtils.getLong(TaskConstants.KERBEROS_EXPIRE_TIME, 24L), TimeUnit.HOURS)
                    .removalListener((RemovalListener<String, PooledDataSourceClient>) notification -> {
                        try (PooledDataSourceClient closedClient = notification.getValue()) {
                            log.info("Datasource: {} is removed from cache due to expire", notification.getKey());
                        } catch (Exception e) {
                            log.error("Close datasource client error", e);
                        }
                    })
                    .maximumSize(100)
                    .build();
    private static final DataSourcePluginManager dataSourcePluginManager = new DataSourcePluginManager();

    static {
        dataSourcePluginManager.installPlugin();
    }

    public static DataSourceClient getPooledDataSourceClient(DbType dbType,
                                                             ConnectionParam connectionParam) throws ExecutionException {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        String datasourceUniqueId = DataSourceUtils.getDatasourceUniqueId(baseConnectionParam, dbType);
        return POOLED_DATASOURCE_CLIENT_CACHE.get(datasourceUniqueId, () -> {
            Map<String, DataSourceChannel> dataSourceChannelMap = dataSourcePluginManager.getDataSourceChannelMap();
            DataSourceChannel dataSourceChannel = dataSourceChannelMap.get(dbType.getName());
            if (null == dataSourceChannel) {
                throw new RuntimeException(String.format("datasource plugin '%s' is not found", dbType.getName()));
            }
            return dataSourceChannel.createPooledDataSourceClient(baseConnectionParam, dbType);
        });
    }

    public static Connection getPooledConnection(DbType dbType,
                                                 ConnectionParam connectionParam) throws SQLException, ExecutionException {
        return getPooledDataSourceClient(dbType, connectionParam).getConnection();
    }

    public static AdHocDataSourceClient getAdHocDataSourceClient(DbType dbType, ConnectionParam connectionParam) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        Map<String, DataSourceChannel> dataSourceChannelMap = dataSourcePluginManager.getDataSourceChannelMap();
        DataSourceChannel dataSourceChannel = dataSourceChannelMap.get(dbType.getName());
        if (null == dataSourceChannel) {
            throw new RuntimeException(String.format("datasource plugin '%s' is not found", dbType.getName()));
        }
        return dataSourceChannel.createAdHocDataSourceClient(baseConnectionParam, dbType);
    }

    public static Connection getAdHocConnection(DbType dbType,
                                                ConnectionParam connectionParam) throws SQLException, ExecutionException {
        return getAdHocDataSourceClient(dbType, connectionParam).getConnection();
    }
}
