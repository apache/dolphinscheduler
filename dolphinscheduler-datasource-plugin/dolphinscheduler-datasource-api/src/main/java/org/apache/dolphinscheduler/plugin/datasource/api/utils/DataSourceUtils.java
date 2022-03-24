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

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.clickhouse.ClickHouseDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.db2.Db2DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.hive.HiveDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.mysql.MySQLDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.oracle.OracleDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.postgresql.PostgreSQLDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.presto.PrestoDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.redshift.RedshiftDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.spark.SparkDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.sqlserver.SQLServerDataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceUtils {

    private DataSourceUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

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

    public static ConnectionParam buildConnectionParams(DbType dbType, String connectionJson) {
        return getDatasourceProcessor(dbType).createConnectionParams(connectionJson);
    }

    public static String getJdbcUrl(DbType dbType, ConnectionParam baseConnectionParam) {
        return getDatasourceProcessor(dbType).getJdbcUrl(baseConnectionParam);
    }

    public static Connection getConnection(DbType dbType, ConnectionParam connectionParam) {
        try {
            return getDatasourceProcessor(dbType).getConnection(connectionParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDatasourceDriver(DbType dbType) {
        return getDatasourceProcessor(dbType).getDatasourceDriver();
    }

    public static BaseDataSourceParamDTO buildDatasourceParamDTO(DbType dbType, String connectionParams) {
        return getDatasourceProcessor(dbType).createDatasourceParamDTO(connectionParams);
    }

    public static DataSourceProcessor getDatasourceProcessor(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return mysqlProcessor;
            case POSTGRESQL:
                return postgreSqlProcessor;
            case HIVE:
                return hiveProcessor;
            case SPARK:
                return sparkProcessor;
            case CLICKHOUSE:
                return clickhouseProcessor;
            case ORACLE:
                return oracleProcessor;
            case SQLSERVER:
                return sqlServerProcessor;
            case DB2:
                return db2PROCESSOR;
            case PRESTO:
                return prestoPROCESSOR;
            case REDSHIFT:
                return redshiftProcessor;
            default:
                throw new IllegalArgumentException("datasource type illegal:" + dbType);
        }
    }

    /**
     * get datasource UniqueId
     */
    public static String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        return getDatasourceProcessor(dbType).getDatasourceUniqueId(connectionParam, dbType);
    }
}
