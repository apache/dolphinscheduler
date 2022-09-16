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

package org.apache.dolphinscheduler.spi.utils;

/**
 * constants
 */
public class Constants {
    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    /** alert plugin param field string **/
    public static final String STRING_PLUGIN_PARAM_FIELD = "field";
    /** alert plugin param name string **/
    public static final String STRING_PLUGIN_PARAM_NAME = "name";
    /** alert plugin param props string **/
    public static final String STRING_PLUGIN_PARAM_PROPS = "props";
    /** alert plugin param type string **/
    public static final String STRING_PLUGIN_PARAM_TYPE = "type";
    /** alert plugin param title string **/
    public static final String STRING_PLUGIN_PARAM_TITLE = "title";
    /** alert plugin param value string **/
    public static final String STRING_PLUGIN_PARAM_VALUE = "value";
    /** alert plugin param validate string **/
    public static final String STRING_PLUGIN_PARAM_VALIDATE = "validate";
    /** alert plugin param options string **/
    public static final String STRING_PLUGIN_PARAM_OPTIONS = "options";
    /**plugin param emit string **/
    public static final String STRING_PLUGIN_PARAM_EMIT = "emit";


    /** string true */
    public static final String STRING_TRUE = "true";
    /** string false */
    public static final String STRING_FALSE = "false";
    /** string yes */
    public static final String STRING_YES = "YES";
    /** string no */
    public static final String STRING_NO = "NO";

    /**
     * common properties path
     */
    public static final String COMMON_PROPERTIES_PATH = "/common.properties";

    /**
     * date format of yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * date format of yyyyMMddHHmmssSSS
     */
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT = "{{";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT = "}}";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT_SPACE = "{ {";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT_SPACE = "} }";

    public static final String SMALL = "small";

    public static final String CHANGE = "change";

    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";

    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";

    public static final String SPRING_DATASOURCE_TEST_ON_BORROW = "spring.datasource.testOnBorrow";

    /**
     * java.security.krb5.conf
     */
    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    /**
     * java.security.krb5.conf.path
     */
    public static final String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE = "hadoop.security.authentication.startup.state";

    /**
     * loginUserFromKeytab user
     */
    public static final String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";

    /**
     * loginUserFromKeytab path
     */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    /**
     * resource storage type
     */
    public static final String RESOURCE_STORAGE_TYPE = "resource.storage.type";

    /**
     * kerberos
     */
    public static final String KERBEROS = "kerberos";

    /**
     *  support hive datasource in one session
     */
    public static final String SUPPORT_HIVE_ONE_SESSION = "support.hive.oneSession";

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

    public static final String ADDRESS = "address";
    public static final String DATABASE = "database";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String PRINCIPAL = "principal";
    public static final String OTHER = "other";
    public static final String ORACLE_DB_CONNECT_TYPE = "connectType";
    public static final String KERBEROS_KRB5_CONF_PATH = "javaSecurityKrb5Conf";
    public static final String KERBEROS_KEY_TAB_USERNAME = "loginUserKeytabUsername";
    public static final String KERBEROS_KEY_TAB_PATH = "loginUserKeytabPath";

    /**
     * DOUBLE_SLASH //
     */
    public static final String DOUBLE_SLASH = "//";

    /**
     * SLASH /
     */
    public static final String SLASH = "/";

    /**
     * comma ,
     */
    public static final String COMMA = ",";

    /**
     * COLON :
     */
    public static final String COLON = ":";


    /**
     * AT SIGN @
     */
    public static final String AT_SIGN = "@";

    /**
     * SEMICOLON ;
     */
    public static final String SEMICOLON = ";";


    /**
     * EQUAL_SIGN =
     */
    public static final String EQUAL_SIGN = "=";

    /**
     * datasource encryption salt
     */
    public static final String DATASOURCE_ENCRYPTION_SALT_DEFAULT = "!@#$%^&*";
    public static final String DATASOURCE_ENCRYPTION_ENABLE = "datasource.encryption.enable";
    public static final String DATASOURCE_ENCRYPTION_SALT = "datasource.encryption.salt";

}
