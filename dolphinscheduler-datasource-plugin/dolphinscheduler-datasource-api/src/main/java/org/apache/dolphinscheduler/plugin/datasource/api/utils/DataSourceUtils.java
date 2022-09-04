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

package org.apache.dolphinscheduler.plugin.datasource.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceProcessorProvider;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;

public class DataSourceUtils {

    public DataSourceUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

    /**
     * check datasource param
     *
     * @param baseDataSourceParamDTO datasource param
     */
    public static void checkDataSourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        getDataSourceProcessor(baseDataSourceParamDTO.getType()).checkDataSourceParam(baseDataSourceParamDTO);
    }

    /**
     * build connection url
     *
     * @param baseDataSourceParamDTO datasourceParam
     */
    public static ConnectionParam buildConnectionParams(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        ConnectionParam connectionParams = getDataSourceProcessor(baseDataSourceParamDTO.getType())
                .createConnectionParams(baseDataSourceParamDTO);
        logger.info("parameters map:{}", connectionParams);
        return connectionParams;
    }

    public static ConnectionParam buildConnectionParams(DbType dbType, String connectionJson) {
        return getDataSourceProcessor(dbType).createConnectionParams(connectionJson);
    }

    public static String getJdbcUrl(DbType dbType, ConnectionParam baseConnectionParam) {
        return getDataSourceProcessor(dbType).getJdbcUrl(baseConnectionParam);
    }

    public static Connection getConnection(DbType dbType, ConnectionParam connectionParam) {
        try {
            return getDataSourceProcessor(dbType).getConnection(connectionParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDataSourceDriver(DbType dbType) {
        return getDataSourceProcessor(dbType).getDataSourceDriver();
    }

    public static BaseDataSourceParamDTO buildDataSourceParamDTO(DbType dbType, String connectionParams) {
        return getDataSourceProcessor(dbType).createDataSourceParamDTO(connectionParams);
    }

    public static DataSourceProcessor getDataSourceProcessor(DbType dbType) {
        Map<String, DataSourceProcessor> dataSourceProcessorMap = DataSourceProcessorProvider.getInstance().getDataSourceProcessorMap();
        if (!dataSourceProcessorMap.containsKey(dbType.name())) {
            throw new IllegalArgumentException("illegal datasource type");
        }
        return dataSourceProcessorMap.get(dbType.name());
    }

    /**
     * get datasource UniqueId
     */
    public static String getDataSourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        return getDataSourceProcessor(dbType).getDataSourceUniqueId(connectionParam, dbType);
    }

    /**
     * build connection url
     */
    public static BaseDataSourceParamDTO buildDataSourceParam(String param) {
        JsonNode jsonNodes = JSONUtils.parseObject(param);

        return getDataSourceProcessor(DbType.ofName(jsonNodes.get("type").asText().toUpperCase()))
                .castDataSourceParamDTO(param);
    }
}
