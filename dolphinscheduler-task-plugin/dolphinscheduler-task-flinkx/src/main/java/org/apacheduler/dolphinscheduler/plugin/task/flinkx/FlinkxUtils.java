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

package org.apacheduler.dolphinscheduler.plugin.task.flinkx;

import org.apache.dolphinscheduler.spi.enums.DbType;

public class FlinkxUtils {

    public static final String FLINKX_READER_PLUGIN_MYSQL = "mysqlreader";
    public static final String FLINKX_READER_PLUGIN_POSTGRESQL = "postgresqlreader";
    public static final String FLINKX_READER_PLUGIN_ORACLE = "oraclereader";
    public static final String FLINKX_READER_PLUGIN_SQLSERVER = "sqlserverreader";

    public static final String FLINKX_WRITER_PLUGIN_MYSQL = "mysqlwriter";
    public static final String FLINKX_WRITER_PLUGIN_POSTGRESQL = "postgresqlwriter";
    public static final String FLINKX_WRITER_PLUGIN_ORACLE = "oraclewriter";
    public static final String FLINKX_WRITER_PLUGIN_SQLSERVER = "sqlserverwriter";

    public static String getReaderPluginName(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return FLINKX_READER_PLUGIN_MYSQL;
            case POSTGRESQL:
                return FLINKX_READER_PLUGIN_POSTGRESQL;
            case ORACLE:
                return FLINKX_READER_PLUGIN_ORACLE;
            case SQLSERVER:
                return FLINKX_READER_PLUGIN_SQLSERVER;
            default:
                return null;
        }
    }

    public static String getWriterPluginName(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return FLINKX_WRITER_PLUGIN_MYSQL;
            case POSTGRESQL:
                return FLINKX_WRITER_PLUGIN_POSTGRESQL;
            case ORACLE:
                return FLINKX_WRITER_PLUGIN_ORACLE;
            case SQLSERVER:
                return FLINKX_WRITER_PLUGIN_SQLSERVER;
            default:
                return null;
        }
    }

}
