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
package org.apache.dolphinscheduler.common.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.datanucleus.store.rdbms.exceptions.UnsupportedDataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;


public class SqlUtils {

    public static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    /**
     * resolve sql statement select table names
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlSelectTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.SELECT);
    }

    /**
     * resolve sql statement insert table names
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlInsertTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.INSERT);
    }

    /**
     * resolve sql statement update table names
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlUpdateTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.UPDATE);
    }

    /**
     * resolve sql statement delete table names
     *
     * @param dbType database type
     * @param sql select sql
     * @return table List
     * @throws UnsupportedDataTypeException
     */
    public static List<String> resolveSqlDeleteTables(DbType dbType, String sql) throws SQLException {
        return resolveSqlTables(dbType, sql).get(Token.DELETE);
    }

    /**
     * resolve sql statement table names
     *
     * @param dbType database type
     * @param sql sql
     * @return table map Token:List
     * @throws UnsupportedDataTypeException
     */
    public static Map<Token, List<String>> resolveSqlTables(DbType dbType, String sql) throws SQLException {
        Map<Token, List<String>> tableMap = new HashMap<>();
        tableMap.put(Token.SELECT, new ArrayList<>());
        tableMap.put(Token.INSERT, new ArrayList<>());
        tableMap.put(Token.UPDATE, new ArrayList<>());
        tableMap.put(Token.DELETE, new ArrayList<>());

        try {
            List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType.getDesc());

            for (SQLStatement stmt : stmtList) {
                SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(dbType.getDesc());
                stmt.accept(visitor);

                if (visitor.getTables() != null) {
                    for (TableStat.Name name : visitor.getTables().keySet()) {
                        TableStat tableStat = visitor.getTables().get(name);
                        if (tableStat.getSelectCount() > 0) {
                            tableMap.get(Token.SELECT).add(name.getName());
                        }

                        if (tableStat.getInsertCount() > 0) {
                            tableMap.get(Token.INSERT).add(name.getName());
                        }

                        if (tableStat.getUpdateCount() > 0) {
                            tableMap.get(Token.UPDATE).add(name.getName());
                        }

                        if (tableStat.getDeleteCount() > 0) {
                            tableMap.get(Token.DELETE).add(name.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            throw new SQLException(e);
        }

        return tableMap;
    }

    public static String[] convertKeywordsColumns(DbType dbType, String[] columns) {
        if (columns == null) {
            return null;
        }

        String[] toColumns = new String[columns.length];
        for (int i = 0; i < columns.length; i++ ) {
            toColumns[i] = doConvertKeywordsColumn(dbType, columns[i]);
        }

        return toColumns;
    }

    public static String doConvertKeywordsColumn(DbType dbType, String column) {
        if (column == null) {
            return column;
        }

        column = column.trim();
        column = column.replace("`", "");
        column = column.replace("\"", "");
        column = column.replace("'", "");

        switch (dbType) {
            case MYSQL:
                return String.format("`%s`", column);
            case POSTGRESQL:
                return String.format("\"%s\"", column);
            case ORACLE:
                return String.format("\"%s\"", column);
            case SQLSERVER:
                return String.format("`%s`", column);
            default:
                return column;
        }
    }

}
