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
package cn.escheduler.api.utils;

/**
 * web application constants
 */
public class Constants {

    /**
     * status
     */
    public static final String STATUS = "status";

    /**
     * message
     */
    public static final String MSG = "msg";

    /**
     * data total
     * 数据总数
     */
    public  static final String COUNT = "count";

    /**
     * page size
     * 每页数据条数
     */
    public  static final String PAGE_SIZE = "pageSize";

    /**
     * current page no
     * 当前页码
     */
    public  static final String PAGE_NUMBER = "pageNo";

    /**
     * result
     */
    public static final String RESULT = "result";

    /**
     *
     */
    public static final String DATA_LIST = "data";

    public static final String TOTAL_LIST = "totalList";

    public static final String CURRENT_PAGE = "currentPage";

    public static final String TOTAL_PAGE = "totalPage";

    public static final String TOTAL = "total";

    /**
     * session user
     */
    public static final String SESSION_USER = "session.user";

    public static final String SESSION_ID = "sessionId";

    public static final String PASSWORD_DEFAULT = "******";

    /**
     * driver
     */
    public static final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORG_APACHE_HIVE_JDBC_HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    public static final String COM_CLICKHOUSE_JDBC_DRIVER = "ru.yandex.clickhouse.ClickHouseDriver";
    public static final String COM_ORACLE_JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    public static final String COM_SQLSERVER_JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    /**
     * database type
     */
    public static final String MYSQL = "MYSQL";
    public static final String POSTGRESQL = "POSTGRESQL";
    public static final String HIVE = "HIVE";
    public static final String SPARK = "SPARK";
    public static final String CLICKHOUSE = "CLICKHOUSE";
    public static final String ORACLE = "ORACLE";
    public static final String SQLSERVER = "SQLSERVER";

    /**
     * jdbc url
     */
    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    public static final String JDBC_ORACLE = "jdbc:oracle:thin:@//";
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";


    public static final String ADDRESS = "address";
    public static final String DATABASE = "database";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String PRINCIPAL = "principal";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String OTHER = "other";


    /**
     * session timeout
     */
    public static final int SESSION_TIME_OUT = 7200;
    public static final int maxFileSize = 1024 * 1024 * 1024;
    public static final String UDF = "UDF";
    public static final String CLASS = "class";
    public static final String RECEIVERS = "receivers";
    public static final String RECEIVERS_CC = "receiversCc";
}