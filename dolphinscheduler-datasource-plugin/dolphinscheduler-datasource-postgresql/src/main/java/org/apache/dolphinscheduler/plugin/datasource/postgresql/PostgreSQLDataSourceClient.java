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

package org.apache.dolphinscheduler.plugin.datasource.postgresql;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class PostgreSQLDataSourceClient extends CommonDataSourceClient {

    public PostgreSQLDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected String getDatabaseListSql(String databasePattern) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT datname FROM pg_database\n");
        if (StringUtils.isNotBlank(databasePattern)) {
            sql.append("where datname like %")
                .append(databasePattern.trim())
                .append("% \n");
        }
        return sql.toString();
    }

    @Override
    protected String getTableListSql(String dbName, String schemaName, String tablePattern) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.relname as table_name\n"
            + "FROM pg_class c,\n"
            + " pg_namespace n\n"
            + "WHERE c.relnamespace = n.oid\n"
            + "  and c.relkind IN ('r', 't')\n");

        if (StringUtils.isNotBlank(schemaName)) {
            sql.append(String.format("  and n.nspname = '%s'\n", schemaName.trim()));
        } else {
            sql.append("  and n.nspname not in ('information_schema', 'pg_catalog')\n");
        }

        if (StringUtils.isNotBlank(tablePattern)) {
            sql.append("and table_name like '%")
                .append(tablePattern.trim())
                .append("%' \n");
        }
        return sql.toString();
    }


    @Override
    public List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new DataSourceException("TABLE_NAME_IS_BLANK");
        }
        String sql = String.format(
            "SELECT a.attname      as col_name,\n" +
                "       ty.typname     as data_type,\n" +
                "       de.description as comment\n" +
                "FROM pg_class c,\n" +
                "     pg_namespace n,\n" +
                "     pg_attribute a\n" +
                "         LEFT OUTER JOIN pg_description de on a.attrelid = de.objoid and a.attnum = de.objsubid,\n" +
                "     pg_type ty\n" +
                "WHERE c.relnamespace = n.oid\n" +
                "  and n.nspname = %s\n" +
                "  and c.oid = a.attrelid\n" +
                "  and a.attnum > 0\n" +
                "  and a.attisdropped = false\n" +
                "  and a.atttypid = ty.oid\n" +
                "  and c.relname = '%s'\n" +
                "order by a.attnum", StringUtils.isNotBlank(schemaName) ? String.format("'%s'", schemaName.trim()) : "current_schema()", tableName.trim());

        return super.executeSql(dbName, schemaName, Boolean.FALSE, sql).getMiddle();
    }
}
