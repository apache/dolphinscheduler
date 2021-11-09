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
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.clickhouse.ClickHouseDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.db2.Db2DatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.hive.HiveDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.mysql.MysqlDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.oracle.OracleDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.postgresql.PostgreSqlDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.presto.PrestoDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.spark.SparkDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.sqlserver.SqlServerDatasourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceUtil {

    private DatasourceUtil() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DatasourceUtil.class);

    private static final DatasourceProcessor mysqlProcessor = new MysqlDatasourceProcessor();
    private static final DatasourceProcessor postgreSqlProcessor = new PostgreSqlDatasourceProcessor();
    private static final DatasourceProcessor hiveProcessor = new HiveDatasourceProcessor();
    private static final DatasourceProcessor sparkProcessor = new SparkDatasourceProcessor();
    private static final DatasourceProcessor clickhouseProcessor = new ClickHouseDatasourceProcessor();
    private static final DatasourceProcessor oracleProcessor = new OracleDatasourceProcessor();
    private static final DatasourceProcessor sqlServerProcessor = new SqlServerDatasourceProcessor();
    private static final DatasourceProcessor db2PROCESSOR = new Db2DatasourceProcessor();
    private static final DatasourceProcessor prestoPROCESSOR = new PrestoDatasourceProcessor();

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

    public static BaseDataSourceParamDTO buildDatasourceParamDTO(DbType dbType, String connectionParams) {
        return getDatasourceProcessor(dbType).createDatasourceParamDTO(connectionParams);
    }

    public static DatasourceProcessor getDatasourceProcessor(DbType dbType) {
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
