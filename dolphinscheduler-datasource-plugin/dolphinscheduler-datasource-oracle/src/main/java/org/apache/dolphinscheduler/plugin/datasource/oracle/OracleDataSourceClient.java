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

package org.apache.dolphinscheduler.plugin.datasource.oracle;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class OracleDataSourceClient extends CommonDataSourceClient {

    public OracleDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected void setDefaultValidationQuery(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setValidationQuery(Constants.ORACLE_VALIDATION_QUERY);
    }

    @Override
    protected String getDatabaseListSql(String databasePattern) {
        return "show databases";
    }

    @Override
    public String getTableListSql(String dbName, String schemaName, String tablePattern) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TABLE_NAME FROM user_tables\n");
        if (StringUtils.isNotBlank(tablePattern)) {
            sql.append("where TABLE_NAME like '%")
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
        String sql = String.format("SELECT b.comments as \"COMMENT\",\n"
            + " a.column_name as col_name,\n"
            + " a.data_type || '(' || a.data_length || ')' data_type\n"
            + " FROM user_tab_columns a, user_col_comments b\n"
            + " WHERE a.TABLE_NAME = '%s'\n"
            + " and b.table_name = '%s'\n"
            + " and a.column_name = b.column_name", tableName, tableName);
        return super.executeSql(dbName, tableName, Boolean.FALSE, sql).getMiddle();
    }
}
