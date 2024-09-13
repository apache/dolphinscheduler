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

package org.apache.dolphinscheduler.plugin.datasource.api.constants;

import org.apache.dolphinscheduler.common.constants.DateConstants;

import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

import com.google.common.collect.Sets;

@UtilityClass
public class DataSourceConstants {

    public static final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String COM_MYSQL_CJ_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORG_APACHE_HIVE_JDBC_HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    public static final String COM_CLICKHOUSE_JDBC_DRIVER = "com.clickhouse.jdbc.ClickHouseDriver";
    public static final String COM_DATABEND_JDBC_DRIVER = "com.databend.jdbc.DatabendDriver";
    public static final String COM_ORACLE_JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String COM_SQLSERVER_JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String COM_DB2_JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    public static final String COM_PRESTO_JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    public static final String COM_REDSHIFT_JDBC_DRIVER = "com.amazon.redshift.jdbc42.Driver";
    public static final String COM_ATHENA_JDBC_DRIVER = "com.simba.athena.jdbc.Driver";
    public static final String COM_TRINO_JDBC_DRIVER = "io.trino.jdbc.TrinoDriver";
    public static final String COM_DAMENG_JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    public static final String ORG_APACHE_KYUUBI_JDBC_DRIVER = "org.apache.kyuubi.jdbc.KyuubiHiveDriver";
    public static final String COM_OCEANBASE_JDBC_DRIVER = "com.oceanbase.jdbc.Driver";
    public static final String NET_SNOWFLAKE_JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";
    public static final String COM_VERTICA_JDBC_DRIVER = "com.vertica.jdbc.Driver";
    public static final String COM_HANA_DB_JDBC_DRIVER = "com.sap.db.jdbc.Driver";

    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final String JDBC_MYSQL_LOADBALANCE = "jdbc:mysql:loadbalance://";
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    public static final String JDBC_KYUUBI = "jdbc:kyuubi://";
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    public static final String JDBC_DATABEND = "jdbc:databend://";
    public static final String JDBC_ORACLE_SID = "jdbc:oracle:thin:@";
    public static final String JDBC_ORACLE_SERVICE_NAME = "jdbc:oracle:thin:@//";
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";
    public static final String JDBC_DB2 = "jdbc:db2://";
    public static final String JDBC_PRESTO = "jdbc:presto://";
    public static final String JDBC_REDSHIFT = "jdbc:redshift://";
    public static final String JDBC_REDSHIFT_IAM = "jdbc:redshift:iam://";
    public static final String JDBC_ATHENA = "jdbc:awsathena://";
    public static final String JDBC_TRINO = "jdbc:trino://";
    public static final String JDBC_DAMENG = "jdbc:dm://";
    public static final String JDBC_OCEANBASE = "jdbc:oceanbase://";
    public static final String JDBC_SNOWFLAKE = "jdbc:snowflake://";
    public static final String JDBC_VERTICA = "jdbc:vertica://";
    public static final String JDBC_HANA = "jdbc:sap://";

    public static final String POSTGRESQL_VALIDATION_QUERY = "select version()";
    public static final String MYSQL_VALIDATION_QUERY = "select 1";
    public static final String HIVE_VALIDATION_QUERY = "select 1";
    public static final String CLICKHOUSE_VALIDATION_QUERY = "select 1";
    public static final String DATABEND_VALIDATION_QUERY = "select 1";
    public static final String ORACLE_VALIDATION_QUERY = "select 1 from dual";
    public static final String SQLSERVER_VALIDATION_QUERY = "select 1";
    public static final String DB2_VALIDATION_QUERY = "select 1 from sysibm.sysdummy1";
    public static final String PRESTO_VALIDATION_QUERY = "select 1";
    public static final String REDHIFT_VALIDATION_QUERY = "select 1";
    public static final String ATHENA_VALIDATION_QUERY = "select 1";
    public static final String TRINO_VALIDATION_QUERY = "select 1";
    public static final String DAMENG_VALIDATION_QUERY = "select 1";
    public static final String SNOWFLAKE_VALIDATION_QUERY = "select 1";

    public static final String KYUUBI_VALIDATION_QUERY = "select 1";
    public static final String VERTICA_VALIDATION_QUERY = "select 1";

    public static final String HANA_VALIDATION_QUERY = "select 1 from DUMMY";

    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";

    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";

    public static final String SUPPORT_HIVE_ONE_SESSION = "support.hive.oneSession";
    /**
     * QUESTION ?
     */
    public static final String QUESTION = "?";

    /**
     * comma ,
     */
    public static final String COMMA = ",";

    /**
     * hyphen
     */
    public static final String HYPHEN = "-";

    /**
     * slash /
     */
    public static final String SLASH = "/";

    /**
     * COLON :
     */
    public static final String COLON = ":";

    /**
     * SPACE " "
     */
    public static final String SPACE = " ";

    /**
     * SINGLE_SLASH /
     */
    public static final String SINGLE_SLASH = "/";

    /**
     * DOUBLE_SLASH //
     */
    public static final String DOUBLE_SLASH = "//";

    /**
     * SINGLE_QUOTES "'"
     */
    public static final String SINGLE_QUOTES = "'";
    /**
     * DOUBLE_QUOTES "\""
     */
    public static final String DOUBLE_QUOTES = "\"";

    /**
     * SEMICOLON ;
     */
    public static final String SEMICOLON = ";";

    /**
     * EQUAL SIGN
     */
    public static final String EQUAL_SIGN = "=";
    /**
     * AT SIGN
     */
    public static final String AT_SIGN = "@";
    /**
     * UNDERLINE
     */
    public static final String UNDERLINE = "_";

    /**
     * sleep time
     */
    public static final int SLEEP_TIME_MILLIS = 1000;

    /**
     * exit code failure
     */
    public static final int EXIT_CODE_FAILURE = -1;

    /**
     * exit code success
     */
    public static final int EXIT_CODE_SUCCESS = 0;
    /**
     * running code
     */
    public static final int RUNNING_CODE = 1;

    public static final String SH = "sh";

    /**
     * log flush interval?output when reach the interval
     */
    public static final int DEFAULT_LOG_FLUSH_INTERVAL = 1000;

    /**
     * pstree, get pud and sub pid
     */
    public static final String PSTREE = "pstree";

    public static final String RWXR_XR_X = "rwxr-xr-x";

    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String PARAMETER_FORMAT_TIME = "yyyyMMddHHmmss";

    /**
     * new
     * schedule time
     */
    public static final String PARAMETER_SHECDULE_TIME = "schedule.time";

    /**
     * system date(yyyyMMddHHmmss)
     */
    public static final String PARAMETER_DATETIME = DateConstants.PARAMETER_DATETIME;

    /**
     * system date(yyyymmdd) today
     */
    public static final String PARAMETER_CURRENT_DATE = DateConstants.PARAMETER_CURRENT_DATE;

    /**
     * system date(yyyymmdd) yesterday
     */
    public static final String PARAMETER_BUSINESS_DATE = DateConstants.PARAMETER_BUSINESS_DATE;

    /**
     * the absolute path of current executing task
     */
    public static final String PARAMETER_TASK_EXECUTE_PATH = "system.task.execute.path";

    /**
     * the instance id of current task
     */
    public static final String PARAMETER_TASK_INSTANCE_ID = "system.task.instance.id";

    /**
     * the definition code of current task
     */
    public static final String PARAMETER_TASK_DEFINITION_CODE = "system.task.definition.code";

    /**
     * the definition name of current task
     */
    public static final String PARAMETER_TASK_DEFINITION_NAME = "system.task.definition.name";

    /**
     * the instance id of the workflow to which current task belongs
     */
    public static final String PARAMETER_WORKFLOW_INSTANCE_ID = "system.workflow.instance.id";

    /**
     * the definition code of the workflow to which current task belongs
     */
    public static final String PARAMETER_WORKFLOW_DEFINITION_CODE = "system.workflow.definition.code";

    /**
     * the definition name of the workflow to which current task belongs
     */
    public static final String PARAMETER_WORKFLOW_DEFINITION_NAME = "system.workflow.definition.name";

    /**
     * the code of the project to which current task belongs
     */
    public static final String PARAMETER_PROJECT_CODE = "system.project.code";

    /**
     * the name of the project to which current task belongs
     */
    public static final String PARAMETER_PROJECT_NAME = "system.project.name";
    /**
     * month_begin
     */
    public static final String MONTH_BEGIN = "month_begin";
    /**
     * add_months
     */
    public static final String ADD_MONTHS = "add_months";
    /**
     * month_end
     */
    public static final String MONTH_END = "month_end";
    /**
     * week_begin
     */
    public static final String WEEK_BEGIN = "week_begin";
    /**
     * week_end
     */
    public static final String WEEK_END = "week_end";
    /**
     * this_day
     */
    public static final String THIS_DAY = "this_day";
    /**
     * last_day
     */
    public static final String LAST_DAY = "last_day";

    /**
     * month_first_day
     */
    public static final String MONTH_FIRST_DAY = "month_first_day";

    /**
     * month_last_day
     */
    public static final String MONTH_LAST_DAY = "month_last_day";

    /**
     * week_first_day
     */
    public static final String WEEK_FIRST_DAY = "week_first_day";

    /**
     * week_last_day
     */
    public static final String WEEK_LAST_DAY = "week_last_day";

    /**
     * year_week
     */
    public static final String YEAR_WEEK = "year_week";
    /**
     * timestamp
     */
    public static final String TIMESTAMP = "timestamp";
    public static final char SUBTRACT_CHAR = '-';
    public static final char ADD_CHAR = '+';
    public static final char MULTIPLY_CHAR = '*';
    public static final char DIVISION_CHAR = '/';
    public static final char LEFT_BRACE_CHAR = '(';
    public static final char RIGHT_BRACE_CHAR = ')';
    public static final String ADD_STRING = "+";
    public static final String MULTIPLY_STRING = "*";
    public static final String DIVISION_STRING = "/";
    public static final String LEFT_BRACE_STRING = "(";
    public static final char P = 'P';
    public static final char N = 'N';
    public static final String SUBTRACT_STRING = "-";
    public static final String LOCAL_PARAMS_LIST = "localParamsList";
    public static final String TASK_TYPE = "taskType";
    public static final String QUEUE = "queue";
    /**
     * default display rows
     */
    public static final int DEFAULT_DISPLAY_ROWS = 10;

    /**
     * jar
     */
    public static final String JAR = "jar";

    /**
     * hadoop
     */
    public static final String HADOOP = "hadoop";

    /**
     * -D <property>=<value>
     */
    public static final String D = "-D";

    /**
     * datasource encryption salt
     */
    public static final String DATASOURCE_ENCRYPTION_SALT_DEFAULT = "!@#$%^&*";
    public static final String DATASOURCE_ENCRYPTION_ENABLE = "datasource.encryption.enable";
    public static final String DATASOURCE_ENCRYPTION_SALT = "datasource.encryption.salt";

    /**
     * kerberos
     */
    public static final String KERBEROS = "kerberos";

    /**
     * kerberos expire time
     */
    public static final String KERBEROS_EXPIRE_TIME = "kerberos.expire.time";

    /**
     * java.security.krb5.conf
     */
    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    /**
     * java.security.krb5.conf.path
     */
    public static final String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";

    /**
     * loginUserFromKeytab user
     */
    public static final String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";

    /**
     * loginUserFromKeytab path
     */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";

    /**
     * hadoop.security.authentication
     */
    public static final String HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE =
            "hadoop.security.authentication.startup.state";

    /**
     * hdfs/s3 configuration
     * resource.storage.upload.base.path
     */
    public static final String RESOURCE_UPLOAD_PATH = "resource.storage.upload.base.path";

    /**
     * data.quality.jar.dir
     */
    public static final String DATA_QUALITY_JAR_DIR = "data-quality.jar.dir";

    public static final String TASK_TYPE_DATA_QUALITY = "DATA_QUALITY";

    public static final Set<String> TASK_TYPE_SET_K8S = Sets.newHashSet("K8S", "KUBEFLOW");

    /**
     * azure config
     */
    public static final String AZURE_CLIENT_ID = "resource.azure.client.id";
    public static final String AZURE_CLIENT_SECRET = "resource.azure.client.secret";
    public static final String AZURE_ACCESS_SUB_ID = "resource.azure.subId";
    public static final String AZURE_SECRET_TENANT_ID = "resource.azure.tenant.id";
    public static final String QUERY_INTERVAL = "resource.query.interval";

    /**
     * use for k8s task
     */
    public static final String API_VERSION = "batch/v1";
    public static final String RESTART_POLICY = "Never";
    public static final String MEMORY = "memory";
    public static final String CPU = "cpu";
    public static final String LAYER_LABEL = "k8s.cn/layer";
    public static final String LAYER_LABEL_VALUE = "batch";
    public static final String NAME_LABEL = "k8s.cn/name";
    public static final String TASK_INSTANCE_ID = "taskInstanceId";
    public static final String MI = "Mi";
    public static final int JOB_TTL_SECONDS = 300;
    public static final int LOG_LINES = 500;
    public static final String NAMESPACE_NAME = "name";
    public static final String CLUSTER = "cluster";

    /**
     * spark / flink on k8s label name
     */
    public static final String UNIQUE_LABEL_NAME = "dolphinscheduler-label";

    /**
     * conda config used by jupyter task plugin
     */
    public static final String CONDA_PATH = "conda.path";

    // Loop task constants
    public static final Duration DEFAULT_LOOP_STATUS_INTERVAL = Duration.ofSeconds(5L);

    /**
     * sql params regex
     */
    public static final String GROUP_NAME1 = "paramName1";
    public static final String GROUP_NAME2 = "paramName2";
    public static final String SQL_PARAMS_REGEX =
            String.format("['\"]\\$\\{(?<%s>.*?)}['\"]|\\$\\{(?<%s>.*?)}", GROUP_NAME1, GROUP_NAME2);
    public static final Pattern SQL_PARAMS_PATTERN = Pattern.compile(SQL_PARAMS_REGEX);

    public static final String AZURE_SQL_DATABASE_SPN = "https://database.windows.net/";
    public static final String AZURE_SQL_DATABASE_TOKEN_SCOPE = "/.default";

}
