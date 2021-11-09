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

import org.apache.dolphinscheduler.plugin.datasource.api.utils.DatasourceUtil;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.exception.PluginException;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class DataSourceClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceClientProvider.class);

    private static final Map<String, DataSourceClient> uniqueId2dataSourceClientMap = new ConcurrentHashMap<>();

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

    public Connection getConnection(DbType dbType, ConnectionParam connectionParam) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        String datasourceUniqueId = DatasourceUtil.getDatasourceUniqueId(baseConnectionParam, dbType);
        logger.info("getConnection datasourceUniqueId {}", datasourceUniqueId);

        DataSourceClient dataSourceClient;
        dataSourceClient = uniqueId2dataSourceClientMap.get(datasourceUniqueId);
        if (dataSourceClient == null) {
            Map<String, DataSourceChannel> dataSourceChannelMap = dataSourcePluginManager.getDataSourceChannelMap();
            DataSourceChannel dataSourceChannel = dataSourceChannelMap.get(dbType.getDescp());
            if (null == dataSourceChannel) {
                throw PluginException.getInstance(String.format("%s dataSource Plugin Not Found,Please Check Config File.", dbType.getDescp()));
            }
            dataSourceClient = dataSourceChannel.createDataSourceClient(baseConnectionParam);
            uniqueId2dataSourceClientMap.put(datasourceUniqueId, dataSourceClient);
        }
        return dataSourceClient.getConnection();
    }

    private void initDataSourcePlugin() {
        dataSourcePluginManager = new DataSourcePluginManager();
        DolphinPluginManagerConfig datasourcePluginManagerConfig = new DolphinPluginManagerConfig();
        datasourcePluginManagerConfig.setPlugins(PropertyUtils.getString(Constants.DATASOURCE_PLUGIN_BINDING));

        datasourcePluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(Constants.DATASOURCE_PLUGIN_DIR, Constants.DATASOURCE_PLUGIN_PATH));

        if (StringUtils.isNotBlank(PropertyUtils.getString(Constants.MAVEN_LOCAL_REPOSITORY))) {
            datasourcePluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(Constants.MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader datasourcePluginLoader = new DolphinPluginLoader(datasourcePluginManagerConfig, ImmutableList.of(dataSourcePluginManager));
        try {
            datasourcePluginLoader.loadPlugins();
        } catch (Exception e) {
            throw PluginException.getInstance("Load datasource Plugin Failed !", e);
        }
        if (MapUtils.isEmpty(dataSourcePluginManager.getDataSourceChannelMap())) {
            throw PluginException.getInstance("datasource Plugin Not Found,Please Check Config File");
        }
    }

}
