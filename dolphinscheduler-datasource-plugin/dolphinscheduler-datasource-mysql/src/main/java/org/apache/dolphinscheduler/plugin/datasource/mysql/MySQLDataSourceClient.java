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

package org.apache.dolphinscheduler.plugin.datasource.mysql;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class MySQLDataSourceClient extends CommonDataSourceClient {

    public MySQLDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected String getDatabaseListSql(String databasePattern) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA \n");
        if (StringUtils.isNotBlank(databasePattern)) {
            sql.append("where SCHEMA_NAME like '%")
                .append(databasePattern.trim())
                .append("%' \n");
        }
        return sql.toString();
    }

    @Override
    protected String getTableListSql(String dbName, String schemaName, String tablePattern) {
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("select table_name from information_schema.tables where table_schema = '%s'", dbName));
        if (StringUtils.isNotBlank(tablePattern)) {
            sql.append("\n and table_name like '%")
                .append(tablePattern.trim())
                .append("%' \n");
        }
        return sql.toString();
    }

    @Override
    public List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new DataSourceException("table name is blank");
        }
        String sql = String.format("SELECT\n"
            + "   t.COLUMN_NAME as col_name,\n"
            + "   t.COLUMN_COMMENT as comment,\n"
            + "   t.COLUMN_TYPE as data_type \n"
            + " FROM information_schema.`COLUMNS` t\n"
            + " WHERE t.TABLE_SCHEMA = '%s' "
            + "AND t.TABLE_NAME = '%s'", dbName, tableName);
        return super.executeSql(dbName, tableName, Boolean.FALSE, sql).getMiddle();
    }

}
