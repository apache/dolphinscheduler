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

package org.apache.dolphinscheduler.graphql.utils;

import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.clickhouse.ClickHouseDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.db2.Db2DatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.hive.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.oracle.OracleDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.postgresql.PostgreSqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.presto.PrestoDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.spark.SparkDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.sqlserver.SqlServerDatasourceParamDTO;
import org.apache.dolphinscheduler.common.enums.DbType;

public class DataSourceParamDTOUtil {
    public static BaseDataSourceParamDTO getDataSourceParamDTO(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return new MysqlDatasourceParamDTO();
            case POSTGRESQL:
                return new PostgreSqlDatasourceParamDTO();
            case HIVE:
                return new HiveDataSourceParamDTO();
            case SPARK:
                return new SparkDatasourceParamDTO();
            case CLICKHOUSE:
                return new ClickHouseDatasourceParamDTO();
            case ORACLE:
                return new OracleDatasourceParamDTO();
            case SQLSERVER:
                return new SqlServerDatasourceParamDTO();
            case DB2:
                return new Db2DatasourceParamDTO();
            case PRESTO:
                return new PrestoDatasourceParamDTO();
            default:
                throw new IllegalArgumentException("datasource type illegal:" + dbType);
        }
    }
}
