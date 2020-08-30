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
package org.apache.dolphinscheduler.server.utils;


import org.apache.dolphinscheduler.common.enums.DbType;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;


public class DataxUtils {

    public static final String DATAX_READER_PLUGIN_MYSQL = "mysqlreader";

    public static final String DATAX_READER_PLUGIN_POSTGRESQL = "postgresqlreader";

    public static final String DATAX_READER_PLUGIN_ORACLE = "oraclereader";

    public static final String DATAX_READER_PLUGIN_SQLSERVER = "sqlserverreader";

    public static final String DATAX_WRITER_PLUGIN_MYSQL = "mysqlwriter";

    public static final String DATAX_WRITER_PLUGIN_POSTGRESQL = "postgresqlwriter";

    public static final String DATAX_WRITER_PLUGIN_ORACLE = "oraclewriter";

    public static final String DATAX_WRITER_PLUGIN_SQLSERVER = "sqlserverwriter";

    public static String getReaderPluginName(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return DATAX_READER_PLUGIN_MYSQL;
            case POSTGRESQL:
                return DATAX_READER_PLUGIN_POSTGRESQL;
            case ORACLE:
                return DATAX_READER_PLUGIN_ORACLE;
            case SQLSERVER:
                return DATAX_READER_PLUGIN_SQLSERVER;
            default:
                return null;
        }
    }

    public static String getWriterPluginName(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return DATAX_WRITER_PLUGIN_MYSQL;
            case POSTGRESQL:
                return DATAX_WRITER_PLUGIN_POSTGRESQL;
            case ORACLE:
                return DATAX_WRITER_PLUGIN_ORACLE;
            case SQLSERVER:
                return DATAX_WRITER_PLUGIN_SQLSERVER;
            default:
                return null;
        }
    }

    public static SQLStatementParser getSqlStatementParser(DbType dbType, String sql) {
        switch (dbType) {
            case MYSQL:
                return new MySqlStatementParser(sql);
            case POSTGRESQL:
                return new PGSQLStatementParser(sql);
            case ORACLE:
                return new OracleStatementParser(sql);
            case SQLSERVER:
                return new SQLServerStatementParser(sql);
            default:
                return null;
        }
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
