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

package org.apache.dolphinscheduler.plugin.task.datasource;

import org.apache.dolphinscheduler.plugin.task.datasource.clickhouse.ClickHouseDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.db2.Db2DatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.hive.HiveDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.mysql.MysqlDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.oracle.OracleDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.postgresql.PostgreSqlDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.presto.PrestoDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.spark.SparkDatasourceProcessor;
import org.apache.dolphinscheduler.plugin.task.datasource.sqlserver.SqlServerDatasourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;

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

    public static ConnectionParam buildConnectionParams(DbType dbType, String connectionJson) {
        return getDatasourceProcessor(dbType).createConnectionParams(connectionJson);
    }

    public static Connection getConnection(DbType dbType, ConnectionParam connectionParam) {
        try {
            return getDatasourceProcessor(dbType).getConnection(connectionParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJdbcUrl(DbType dbType, ConnectionParam baseConnectionParam) {
        return getDatasourceProcessor(dbType).getJdbcUrl(baseConnectionParam);
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

}
