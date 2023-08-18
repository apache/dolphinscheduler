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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.common.constants.DateConstants;

import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public class TaskConstants {

    private TaskConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String YARN_APPLICATION_REGEX = "application_\\d+_\\d+";

    public static final String FLINK_APPLICATION_REGEX = "JobID \\w+";

    public static final String SETVALUE_REGEX = "[\\$#]\\{setValue\\((.*?)\\)}";

    /**
     * string false
     */
    public static final String STRING_FALSE = "false";

    /**
     * exit code kill
     */
    public static final int EXIT_CODE_KILL = 137;
    public static final String PID = "pid";

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
     * date format of yyyyMMdd
     */
    public static final String PARAMETER_FORMAT_DATE = "yyyyMMdd";

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
    public static final String GLOBAL_PARAMS = "globalParams";
    public static final String LOCAL_PARAMS = "localParams";
    public static final String LOCAL_PARAMS_LIST = "localParamsList";
    public static final String SUBPROCESS_INSTANCE_ID = "subProcessInstanceId";
    public static final String PROCESS_INSTANCE_STATE = "processInstanceState";
    public static final String PARENT_WORKFLOW_INSTANCE = "parentWorkflowInstance";
    public static final String CONDITION_RESULT = "conditionResult";
    public static final String SWITCH_RESULT = "switchResult";
    public static final String DEPENDENCE = "dependence";
    public static final String TASK_TYPE = "taskType";
    public static final String TASK_LIST = "taskList";
    public static final String QUEUE = "queue";
    public static final String QUEUE_NAME = "queueName";
    public static final int LOG_QUERY_SKIP_LINE_NUMBER = 0;
    public static final int LOG_QUERY_LIMIT = 4096;

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
     * jdbc url
     */
    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    public static final String JDBC_DATABEND = "jdbc:databend://";
    public static final String JDBC_ORACLE_SID = "jdbc:oracle:thin:@";
    public static final String JDBC_ORACLE_SERVICE_NAME = "jdbc:oracle:thin:@//";
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";
    public static final String JDBC_DB2 = "jdbc:db2://";
    public static final String JDBC_PRESTO = "jdbc:presto://";

    /**
     * driver
     */
    public static final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String COM_MYSQL_CJ_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String ORG_APACHE_HIVE_JDBC_HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    public static final String COM_CLICKHOUSE_JDBC_DRIVER = "com.clickhouse.jdbc.ClickHouseDriver";
    public static final String COM_DATABEND_JDBC_DRIVER = "com.databend.jdbc.DatabendDriver";
    public static final String COM_ORACLE_JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    public static final String COM_SQLSERVER_JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String COM_DB2_JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    public static final String COM_PRESTO_JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";

    /**
     * datasource encryption salt
     */
    public static final String DATASOURCE_ENCRYPTION_SALT_DEFAULT = "!@#$%^&*";
    public static final String DATASOURCE_ENCRYPTION_ENABLE = "datasource.encryption.enable";
    public static final String DATASOURCE_ENCRYPTION_SALT = "datasource.encryption.salt";

    /**
     * resource storage type
     */
    // public static final String RESOURCE_STORAGE_TYPE = "resource.storage.type";

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
     * Task Logger Thread's name
     */
    public static final String TASK_LOGGER_THREAD_NAME = "TaskLogInfo";

    /**
     * hdfs/s3 configuration
     * resource.storage.upload.base.path
     */
    public static final String RESOURCE_UPLOAD_PATH = "resource.storage.upload.base.path";

    /**
     * data.quality.jar.name
     */
    public static final String DATA_QUALITY_JAR_NAME = "data-quality.jar.name";

    /**
     * data.quality.error.output.path
     */
    public static final String DATA_QUALITY_ERROR_OUTPUT_PATH = "data-quality.error.output.path";

    public static final String TASK_TYPE_CONDITIONS = "CONDITIONS";

    public static final String TASK_TYPE_SWITCH = "SWITCH";

    public static final String TASK_TYPE_SUB_PROCESS = "SUB_PROCESS";

    public static final String TASK_TYPE_DYNAMIC = "DYNAMIC";

    public static final String TASK_TYPE_DEPENDENT = "DEPENDENT";

    public static final String TASK_TYPE_SQL = "SQL";

    public static final String TASK_TYPE_DATA_QUALITY = "DATA_QUALITY";

    public static final String DEPLOY_MODE_KUBERNETES = "Kubernetes";

    public static final Set<String> TASK_TYPE_SET_K8S = Sets.newHashSet("K8S", "KUBEFLOW");

    public static final String TASK_TYPE_BLOCKING = "BLOCKING";

    public static final String TASK_TYPE_STREAM = "STREAM";

    /**
     * azure config
     */
    public static final String AZURE_CLIENT_ID = "resource.azure.client.id";
    public static final String AZURE_CLIENT_SECRET = "resource.azure.client.secret";
    public static final String AZURE_ACCESS_SUB_ID = "resource.azure.subId";
    public static final String AZURE_SECRET_TENANT_ID = "resource.azure.tenant.id";
    public static final String QUERY_INTERVAL = "resource.query.interval";

    /**
     * aws config
     */
    public static final String AWS_ACCESS_KEY_ID = "resource.aws.access.key.id";
    public static final String AWS_SECRET_ACCESS_KEY = "resource.aws.secret.access.key";
    public static final String AWS_REGION = "resource.aws.region";

    /**
     * alibaba cloud config
     */
    public static final String ALIBABA_CLOUD_ACCESS_KEY_ID = "resource.alibaba.cloud.access.key.id";
    public static final String ALIBABA_CLOUD_ACCESS_KEY_SECRET = "resource.alibaba.cloud.access.key.secret";
    public static final String ALIBABA_CLOUD_REGION = "resource.alibaba.cloud.region";

    /**
     * huawei cloud config
     */
    public static final String HUAWEI_CLOUD_ACCESS_KEY_ID = "resource.huawei.cloud.access.key.id";
    public static final String HUAWEI_CLOUD_ACCESS_KEY_SECRET = "resource.huawei.cloud.access.key.secret";

    /**
     * use for k8s task
     */
    public static final String API_VERSION = "batch/v1";
    public static final String IMAGE_PULL_POLICY = "Always";
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
    public static final Pattern COMMAND_SPLIT_REGEX = Pattern.compile("[^\\s\"'`]+|\"([^\"]+)\"|'([^']+)'|`([^`]+)`");

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

}
