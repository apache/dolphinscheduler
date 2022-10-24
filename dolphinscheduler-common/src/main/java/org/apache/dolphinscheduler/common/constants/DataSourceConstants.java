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

package org.apache.dolphinscheduler.common.constants;

public class DataSourceConstants {

    public static final String DATASOURCE = "datasource";

    /**
     * driver
     */
    public static final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String COM_MYSQL_CJ_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORG_APACHE_HIVE_JDBC_HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    public static final String COM_CLICKHOUSE_JDBC_DRIVER = "ru.yandex.clickhouse.ClickHouseDriver";
    public static final String COM_ORACLE_JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String COM_SQLSERVER_JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String COM_DB2_JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    public static final String COM_PRESTO_JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    public static final String COM_REDSHIFT_JDBC_DRIVER = "com.amazon.redshift.jdbc42.Driver";
    public static final String COM_ATHENA_JDBC_DRIVER = "com.simba.athena.jdbc.Driver";

    /**
     * validation Query
     */
    public static final String POSTGRESQL_VALIDATION_QUERY = "select version()";
    public static final String MYSQL_VALIDATION_QUERY = "select 1";
    public static final String HIVE_VALIDATION_QUERY = "select 1";
    public static final String CLICKHOUSE_VALIDATION_QUERY = "select 1";
    public static final String ORACLE_VALIDATION_QUERY = "select 1 from dual";
    public static final String SQLSERVER_VALIDATION_QUERY = "select 1";
    public static final String DB2_VALIDATION_QUERY = "select 1 from sysibm.sysdummy1";
    public static final String PRESTO_VALIDATION_QUERY = "select 1";
    public static final String REDHIFT_VALIDATION_QUERY = "select 1";
    public static final String ATHENA_VALIDATION_QUERY = "select 1";

    /**
     * jdbc url
     */
    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    public static final String JDBC_ORACLE_SID = "jdbc:oracle:thin:@";
    public static final String JDBC_ORACLE_SERVICE_NAME = "jdbc:oracle:thin:@//";
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";
    public static final String JDBC_DB2 = "jdbc:db2://";
    public static final String JDBC_PRESTO = "jdbc:presto://";
    public static final String JDBC_REDSHIFT = "jdbc:redshift://";
    public static final String JDBC_ATHENA = "jdbc:awsathena://";

    /**
     * database type
     */
    public static final String MYSQL = "MYSQL";
    public static final String HIVE = "HIVE";

    /**
     * dataSource sensitive param
     */
    public static final String DATASOURCE_PASSWORD_REGEX =
            "(?<=((?i)password((\":\")|(=')))).*?(?=((\")|(')))";

    /**
     * datasource encryption salt
     */
    public static final String DATASOURCE_ENCRYPTION_SALT_DEFAULT = "!@#$%^&*";
    public static final String DATASOURCE_ENCRYPTION_ENABLE = "datasource.encryption.enable";
    public static final String DATASOURCE_ENCRYPTION_SALT = "datasource.encryption.salt";

    /**
     * datasource config
     */
    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";

    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";

    public static final String SPRING_DATASOURCE_TEST_ON_BORROW = "spring.datasource.testOnBorrow";
}
