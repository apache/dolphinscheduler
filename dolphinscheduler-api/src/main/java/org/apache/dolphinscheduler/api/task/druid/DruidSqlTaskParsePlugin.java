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

package org.apache.dolphinscheduler.api.task.druid;

import org.apache.dolphinscheduler.api.task.SqlTaskParseContext;
import org.apache.dolphinscheduler.api.task.SqlTaskParsePlugin;
import org.apache.dolphinscheduler.api.task.SqlTaskParseResult;
import org.apache.dolphinscheduler.dao.entity.DataSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

public class DruidSqlTaskParsePlugin implements SqlTaskParsePlugin {

    @Override
    public String name() {
        return "druid parser";
    }

    @Override
    public SqlTaskParseResult parse(SqlTaskParseContext context) {
        DbType dbType = getDbType(context.getDataSource());
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(context.getSql(), dbType);
        Set<String> upstreamList = new HashSet<>();
        Set<String> downstreamList = new HashSet<>();
        for (SQLStatement sqlStatement : sqlStatements) {
            SchemaStatVisitor schemaStatVisitor = SQLUtils.createSchemaStatVisitor(dbType);
            sqlStatement.accept(schemaStatVisitor);

            Map<TableStat.Name, TableStat> tables = schemaStatVisitor.getTables();
            for (Map.Entry<TableStat.Name, TableStat> table : tables.entrySet()) {
                if (table.getValue().getSelectCount() > 0) {
                    upstreamList.add(table.getKey().getName());
                } else {
                    downstreamList.add(table.getKey().getName());
                }
            }
        }

        SqlTaskParseResult result = new SqlTaskParseResult();
        result.setUpstreamSet(upstreamList);
        result.setDownstreamSet(downstreamList);
        return result;
    }

    private DbType getDbType(DataSource dataSource) {
        switch (dataSource.getType()) {
            case MYSQL:
                return DbType.mysql;
            case ORACLE:
                return DbType.oracle;
            case SQLSERVER:
                return DbType.sqlserver;
            case POSTGRESQL:
                return DbType.postgresql;
            case PRESTO:
                return DbType.presto;
            case CLICKHOUSE:
                return DbType.clickhouse;
            case TRINO:
                return DbType.trino;
            case DB2:
                return DbType.db2;
            case OCEANBASE:
                return DbType.oceanbase;
            case STARROCKS:
                return DbType.starrocks;
            case H2:
                return DbType.h2;
            case HIVE:
            case SPARK:
            case KYUUBI:
                return DbType.hive;
            default:
                return DbType.other;
        }
    }
}
