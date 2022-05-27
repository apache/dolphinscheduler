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
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceUtils {

    public DataSourceUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

    private static final Map<String, DataSourceProcessor> dataSourceProcessorMap = new ConcurrentHashMap<>();
    private static final Map<Integer, String> dataSourceIdMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> dataSourceNameMap = new ConcurrentHashMap<>();


   /* private static final Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

    private static final DataSourceProcessor mysqlProcessor = new MySQLDataSourceProcessor();
    private static final DataSourceProcessor postgreSqlProcessor = new PostgreSQLDataSourceProcessor();
    private static final DataSourceProcessor hiveProcessor = new HiveDataSourceProcessor();
    private static final DataSourceProcessor sparkProcessor = new SparkDataSourceProcessor();
    private static final DataSourceProcessor clickhouseProcessor = new ClickHouseDataSourceProcessor();
    private static final DataSourceProcessor oracleProcessor = new OracleDataSourceProcessor();
    private static final DataSourceProcessor sqlServerProcessor = new SQLServerDataSourceProcessor();
    private static final DataSourceProcessor db2PROCESSOR = new Db2DataSourceProcessor();
    private static final DataSourceProcessor prestoPROCESSOR = new PrestoDataSourceProcessor();
    private static final DataSourceProcessor redshiftProcessor = new RedshiftDataSourceProcessor();
*/

    public void installProcessor() {
        final Set<String> names = new HashSet<>();
        ServiceLoader<DataSourceProcessor> load = ServiceLoader.load(DataSourceProcessor.class);

        ServiceLoader.load(DataSourceProcessor.class).forEach(factory -> {
            final String name = factory.getDbType();

            logger.info("start register processor: " + name);
          /*  if (!names.add(name)) {
                throw new IllegalStateException(format("Duplicate datasource plugins named '%s'", name));
            }
*/
            loadDatasourceClient(factory);

            logger.info("done register processor: " + name);

        });
        int i = 1;
    }

    private void loadDatasourceClient(DataSourceProcessor processor) {
        DataSourceProcessor instance = processor.create();
        dataSourceProcessorMap.put(processor.getDbType(), instance);
        dataSourceIdMap.put(instance.getDbId(), instance.getDbType());
        dataSourceNameMap.put(instance.getDbType(), instance.getDbId());
    }

    /**
     * get db type name by db id
     */
    public static String getDbTypeNameById(int dbTypeId) {
        if (!dataSourceIdMap.containsKey(dbTypeId)) {
            throw new NoSuchElementException("no such db type id:" + dbTypeId);
        }
        return dataSourceIdMap.get(dbTypeId);
    }

    /**
     * get db type name by db id
     */
    public static Integer getDbTypeNameByName(String dbName) {
        if (!dataSourceNameMap.containsKey(dbName)) {
            throw new NoSuchElementException("no such db type id:" + dbName);
        }
        return dataSourceNameMap.get(dbName);
    }

    /**
     * check datasource param
     *
     * @param baseDataSourceParamDTO datasource param
     */
    public static void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        getDatasourceProcessor(baseDataSourceParamDTO.getType()).checkDatasourceParam(baseDataSourceParamDTO);
    }

    /**
     * build connection url
     */
    public static BaseDataSourceParamDTO buildDatasourceParam(String param) {
        JsonNode jsonNodes = JSONUtils.parseObject(param);

        return getDatasourceProcessor(jsonNodes.get("type").asText()).castDatasourceParamDTO(param);
    }

    /**
     * build connection url
     *
     * @param baseDataSourceParamDTO datasourceParam
     */
    public static ConnectionParam buildConnectionParams(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        ConnectionParam connectionParams = getDatasourceProcessor(baseDataSourceParamDTO.getType())
                .createConnectionParams(baseDataSourceParamDTO);
        if (logger.isDebugEnabled()) {
            logger.info("parameters map:{}", connectionParams);
        }
        return connectionParams;
    }

    public static ConnectionParam buildConnectionParams(String dbType, String connectionJson) {
        return getDatasourceProcessor(dbType).createConnectionParams(connectionJson);
    }

    public static String getJdbcUrl(String dbType, ConnectionParam baseConnectionParam) {
        return getDatasourceProcessor(dbType).getJdbcUrl(baseConnectionParam);
    }

    public static Connection getConnection(String dbType, ConnectionParam connectionParam) {
        try {
            return getDatasourceProcessor(dbType).getConnection(connectionParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDatasourceDriver(DbType dbType) {
        return getDatasourceProcessor(dbType.name()).getDatasourceDriver();
    }

    public static BaseDataSourceParamDTO buildDatasourceParamDTO(String dbName, String connectionParams) {
        return getDatasourceProcessor(dbName).createDatasourceParamDTO(connectionParams);
    }

    public static DataSourceProcessor getDatasourceProcessor(String type) {
        if (!dataSourceProcessorMap.containsKey(type.toUpperCase())) {
            throw new IllegalArgumentException("illegal datasource type");
        }
        return dataSourceProcessorMap.get(type.toUpperCase());
    }

    /**
     * get datasource UniqueId
     */
    public static String getDatasourceUniqueId(ConnectionParam connectionParam, String dbType) {
        return getDatasourceProcessor(dbType).getDatasourceUniqueId(connectionParam, dbType);
    }
}
