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
package org.apache.dolphinscheduler.common;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.OSUtils;

import java.util.regex.Pattern;

/**
 * Constants
 */
public final class Constants {
    private Constants() {
        throw new IllegalStateException("Constants class");
    }
    /**
     * common properties path
     */
    public static final String COMMON_PROPERTIES_PATH = "/common.properties";

    /**
     * fs.defaultFS
     */
    public static final String FS_DEFAULTFS = "fs.defaultFS";


    /**
     * fs s3a endpoint
     */
    public static final String FS_S3A_ENDPOINT = "fs.s3a.endpoint";

    /**
     * fs s3a access key
     */
    public static final String FS_S3A_ACCESS_KEY = "fs.s3a.access.key";

    /**
     * fs s3a secret key
     */
    public static final String FS_S3A_SECRET_KEY = "fs.s3a.secret.key";


    /**
     * yarn.resourcemanager.ha.rm.idsfs.defaultFS
     */
    public static final String YARN_RESOURCEMANAGER_HA_RM_IDS = "yarn.resourcemanager.ha.rm.ids";

    /**
     * yarn.application.status.address
     */
    public static final String YARN_APPLICATION_STATUS_ADDRESS = "yarn.application.status.address";

    /**
     * hdfs configuration
     * hdfs.root.user
     */
    public static final String HDFS_ROOT_USER = "hdfs.root.user";

    /**
     * hdfs configuration
     * data.store2hdfs.basepath
     */
    public static final String DATA_STORE_2_HDFS_BASEPATH = "data.store2hdfs.basepath";

    /**
     * data.basedir.path
     */
    public static final String DATA_BASEDIR_PATH = "data.basedir.path";

    /**
     * data.download.basedir.path
     */
    public static final String DATA_DOWNLOAD_BASEDIR_PATH = "data.download.basedir.path";

    /**
     * process.exec.basepath
     */
    public static final String PROCESS_EXEC_BASEPATH = "process.exec.basepath";

    /**
     * dolphinscheduler.env.path
     */
    public static final String DOLPHINSCHEDULER_ENV_PATH = "dolphinscheduler.env.path";


    /**
     * python home
     */
    public static final String PYTHON_HOME="PYTHON_HOME";

    /**
     * resource.view.suffixs
     */
    public static final String RESOURCE_VIEW_SUFFIXS = "resource.view.suffixs";

    /**
     * development.state
     */
    public static final String DEVELOPMENT_STATE = "development.state";

    /**
     * res.upload.startup.type
     */
    public static final String RES_UPLOAD_STARTUP_TYPE = "res.upload.startup.type";

    /**
     * zookeeper quorum
     */
    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";

    /**
     * MasterServer directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_MASTERS = "/masters";

    /**
     * WorkerServer directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_WORKERS = "/workers";

    /**
     * all servers directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS = "/dead-servers";

    /**
     * MasterServer lock directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_MASTERS = "/lock/masters";

    /**
     * WorkerServer lock directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_WORKERS = "/lock/workers";

    /**
     * MasterServer failover directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS = "/lock/failover/masters";

    /**
     * WorkerServer failover directory registered in zookeeper
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS = "/lock/failover/workers";

    /**
     * MasterServer startup  failover runing and fault tolerance process
     */
    public static final String ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS = "/lock/failover/startup-masters";

    /**
     * need send warn times when master server or worker server failover
     */
    public static final int DOLPHINSCHEDULER_WARN_TIMES_FAILOVER = 3;

    /**
     * comma ,
     */
    public static final String COMMA = ",";

    /**
     * COLON :
     */
    public static final String COLON = ":";

    /**
     * SINGLE_SLASH /
     */
    public static final String SINGLE_SLASH = "/";

    /**
     * DOUBLE_SLASH //
     */
    public static final String DOUBLE_SLASH = "//";

    /**
     * SEMICOLON ;
     */
    public static final String SEMICOLON = ";";

    /**
     * EQUAL SIGN
     */
    public static final String EQUAL_SIGN = "=";

    /**
     * ZOOKEEPER_SESSION_TIMEOUT
     */
    public static final String ZOOKEEPER_SESSION_TIMEOUT = "zookeeper.session.timeout";

    public static final String ZOOKEEPER_CONNECTION_TIMEOUT = "zookeeper.connection.timeout";

    public static final String ZOOKEEPER_RETRY_SLEEP = "zookeeper.retry.sleep";
    public static final String ZOOKEEPER_RETRY_BASE_SLEEP = "zookeeper.retry.base.sleep";
    public static final String ZOOKEEPER_RETRY_MAX_SLEEP = "zookeeper.retry.max.sleep";

    public static final String ZOOKEEPER_RETRY_MAXTIME = "zookeeper.retry.maxtime";


    public static final String MASTER_HEARTBEAT_INTERVAL = "master.heartbeat.interval";

    public static final String MASTER_EXEC_THREADS = "master.exec.threads";

    public static final String MASTER_EXEC_TASK_THREADS = "master.exec.task.number";


    public static final String MASTER_COMMIT_RETRY_TIMES = "master.task.commit.retryTimes";

    public static final String MASTER_COMMIT_RETRY_INTERVAL = "master.task.commit.interval";


    public static final String WORKER_EXEC_THREADS = "worker.exec.threads";

    public static final String WORKER_HEARTBEAT_INTERVAL = "worker.heartbeat.interval";

    public static final String WORKER_FETCH_TASK_NUM = "worker.fetch.task.num";

    public static final String WORKER_MAX_CPULOAD_AVG = "worker.max.cpuload.avg";

    public static final String WORKER_RESERVED_MEMORY = "worker.reserved.memory";

    public static final String MASTER_MAX_CPULOAD_AVG = "master.max.cpuload.avg";

    public static final String MASTER_RESERVED_MEMORY = "master.reserved.memory";


    /**
     * dolphinscheduler tasks queue
     */
    public static final String DOLPHINSCHEDULER_TASKS_QUEUE = "tasks_queue";

    /**
     * dolphinscheduler need kill tasks queue
     */
    public static final String DOLPHINSCHEDULER_TASKS_KILL = "tasks_kill";

    public static final String ZOOKEEPER_DOLPHINSCHEDULER_ROOT = "zookeeper.dolphinscheduler.root";

    public static final String SCHEDULER_QUEUE_IMPL = "dolphinscheduler.queue.impl";


    /**
     * date format of yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * http connect time out
     */
    public static final int HTTP_CONNECT_TIMEOUT = 60 * 1000;


    /**
     * http connect request time out
     */
    public static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 60 * 1000;

    /**
     * httpclient soceket time out
     */
    public static final int SOCKET_TIMEOUT = 60 * 1000;

    /**
     * http header
     */
    public static final String HTTP_HEADER_UNKNOWN = "unKnown";

    /**
     * http X-Forwarded-For
     */
    public static final String HTTP_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * http X-Real-IP
     */
    public static final String HTTP_X_REAL_IP = "X-Real-IP";

    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * user name regex
     */
    public static final Pattern REGEX_USER_NAME = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");

    /**
     * email regex
     */
    public static final Pattern REGEX_MAIL_NAME = Pattern.compile("^([a-z0-9A-Z]+[_|\\-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

    /**
     * read permission
     */
    public static final int READ_PERMISSION = 2 * 1;


    /**
     * write permission
     */
    public static final int WRITE_PERMISSION = 2 * 2;


    /**
     * execute permission
     */
    public static final int EXECUTE_PERMISSION = 1;

    /**
     * default admin permission
     */
    public static final int DEFAULT_ADMIN_PERMISSION = 7;


    /**
     * all permissions
     */
    public static final int ALL_PERMISSIONS = READ_PERMISSION | WRITE_PERMISSION | EXECUTE_PERMISSION;

    /**
     * max task timeout
     */
    public static final int MAX_TASK_TIMEOUT = 24 * 3600;


    /**
     * heartbeat threads number
     */
    public static final int DEFAUL_WORKER_HEARTBEAT_THREAD_NUM = 1;

    /**
     * heartbeat interval
     */
    public static final int DEFAULT_WORKER_HEARTBEAT_INTERVAL = 60;

    /**
     * worker fetch task number
     */
    public static final int DEFAULT_WORKER_FETCH_TASK_NUM = 1;

    /**
     * worker execute threads number
     */
    public static final int DEFAULT_WORKER_EXEC_THREAD_NUM = 10;

    /**
     * master cpu load
     */
    public static final int DEFAULT_MASTER_CPU_LOAD = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * master reserved memory
     */
    public static final double DEFAULT_MASTER_RESERVED_MEMORY = OSUtils.totalMemorySize() / 10;

    /**
     * worker cpu load
     */
    public static final int DEFAULT_WORKER_CPU_LOAD = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * worker reserved memory
     */
    public static final double DEFAULT_WORKER_RESERVED_MEMORY = OSUtils.totalMemorySize() / 10;


    /**
     * master execute threads number
     */
    public static final int DEFAULT_MASTER_EXEC_THREAD_NUM = 100;


    /**
     * default master concurrent task execute num
     */
    public static final int DEFAULT_MASTER_TASK_EXEC_NUM = 20;

    /**
     * default log cache rows num,output when reach the number
     */
    public static final int DEFAULT_LOG_ROWS_NUM = 4 * 16;

    /**
     * log flush interval，output when reach the interval
     */
    public static final int DEFAULT_LOG_FLUSH_INTERVAL = 1000;


    /**
     * default master heartbeat thread number
     */
    public static final int DEFAULT_MASTER_HEARTBEAT_THREAD_NUM = 1;


    /**
     * default master heartbeat interval
     */
    public static final int DEFAULT_MASTER_HEARTBEAT_INTERVAL = 60;

    /**
     * default master commit retry times
     */
    public static final int DEFAULT_MASTER_COMMIT_RETRY_TIMES = 5;


    /**
     * default master commit retry interval
     */
    public static final int DEFAULT_MASTER_COMMIT_RETRY_INTERVAL = 3000;

    /**
     * time unit secong to minutes
     */
    public static final int SEC_2_MINUTES_TIME_UNIT = 60;


    /***
     *
     * rpc port
     */
    public static final int RPC_PORT = 50051;

    /**
     * forbid running task
     */
    public static final String FLOWNODE_RUN_FLAG_FORBIDDEN = "FORBIDDEN";

    /**
     * task record configuration path
     */
    public static final String APPLICATION_PROPERTIES = "application.properties";

    public static final String TASK_RECORD_URL = "task.record.datasource.url";

    public static final String TASK_RECORD_FLAG = "task.record.flag";

    public static final String TASK_RECORD_USER = "task.record.datasource.username";

    public static final String TASK_RECORD_PWD = "task.record.datasource.password";

    public static final String DEFAULT = "Default";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String XXXXXX = "******";
    public static final String NULL = "NULL";
    public static final String THREAD_NAME_MASTER_SERVER = "Master-Server";
    public static final String THREAD_NAME_WORKER_SERVER = "Worker-Server";

    public static final String TASK_RECORD_TABLE_HIVE_LOG = "eamp_hive_log_hd";

    public static final String TASK_RECORD_TABLE_HISTORY_HIVE_LOG = "eamp_hive_hist_log_hd";


    /**
     * command parameter keys
     */
    public static final String CMDPARAM_RECOVER_PROCESS_ID_STRING = "ProcessInstanceId";

    public static final String CMDPARAM_RECOVERY_START_NODE_STRING = "StartNodeIdList";

    public static final String CMDPARAM_RECOVERY_WAITTING_THREAD = "WaittingThreadInstanceId";

    public static final String CMDPARAM_SUB_PROCESS = "processInstanceId";

    public static final String CMDPARAM_EMPTY_SUB_PROCESS = "0";

    public static final String CMDPARAM_SUB_PROCESS_PARENT_INSTANCE_ID = "parentProcessInstanceId";

    public static final String CMDPARAM_SUB_PROCESS_DEFINE_ID = "processDefinitionId";

    public static final String CMDPARAM_START_NODE_NAMES = "StartNodeNameList";

    /**
     * complement data start date
     */
    public static final String CMDPARAM_COMPLEMENT_DATA_START_DATE = "complementStartDate";

    /**
     * complement data end date
     */
    public static final String CMDPARAM_COMPLEMENT_DATA_END_DATE = "complementEndDate";

    /**
     * hadoop configuration
     */
    public static final String HADOOP_RM_STATE_ACTIVE = "ACTIVE";

    public static final String HADOOP_RM_STATE_STANDBY = "STANDBY";

    public static final String HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT = "resource.manager.httpaddress.port";


    /**
     * data source config
     */

    public static final String SPRING_DATASOURCE_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";

    public static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

    public static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";

    public static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    public static final String SPRING_DATASOURCE_VALIDATION_QUERY_TIMEOUT = "spring.datasource.validationQueryTimeout";

    public static final String SPRING_DATASOURCE_INITIAL_SIZE = "spring.datasource.initialSize";

    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";

    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";

    public static final String SPRING_DATASOURCE_MAX_WAIT = "spring.datasource.maxWait";

    public static final String SPRING_DATASOURCE_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "spring.datasource.timeBetweenEvictionRunsMillis";

    public static final String SPRING_DATASOURCE_TIME_BETWEEN_CONNECT_ERROR_MILLIS = "spring.datasource.timeBetweenConnectErrorMillis";

    public static final String SPRING_DATASOURCE_MIN_EVICTABLE_IDLE_TIME_MILLIS = "spring.datasource.minEvictableIdleTimeMillis";

    public static final String SPRING_DATASOURCE_VALIDATION_QUERY = "spring.datasource.validationQuery";

    public static final String SPRING_DATASOURCE_TEST_WHILE_IDLE = "spring.datasource.testWhileIdle";

    public static final String SPRING_DATASOURCE_TEST_ON_BORROW = "spring.datasource.testOnBorrow";

    public static final String SPRING_DATASOURCE_TEST_ON_RETURN = "spring.datasource.testOnReturn";

    public static final String SPRING_DATASOURCE_POOL_PREPARED_STATEMENTS = "spring.datasource.poolPreparedStatements";

    public static final String SPRING_DATASOURCE_DEFAULT_AUTO_COMMIT = "spring.datasource.defaultAutoCommit";

    public static final String SPRING_DATASOURCE_KEEP_ALIVE = "spring.datasource.keepAlive";

    public static final String SPRING_DATASOURCE_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = "spring.datasource.maxPoolPreparedStatementPerConnectionSize";

    public static final String DEVELOPMENT = "development";

    public static final String QUARTZ_PROPERTIES_PATH = "quartz.properties";

    /**
     * sleep time
     */
    public static final int SLEEP_TIME_MILLIS = 1000;

    /**
     * heartbeat for zk info length
     */
    public static final int HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH = 7;


    /**
     * hadoop params constant
     */
    /**
     * jar
     */
    public static final String JAR = "jar";

    /**
     * hadoop
     */
    public static final String HADOOP = "hadoop";

    /**
     * -D parameter
     */
    public static final String D = "-D";

    /**
     * -D mapreduce.job.queuename=ququename
     */
    public static final String MR_QUEUE = "mapreduce.job.queuename";


    /**
     * spark params constant
     */
    public static final String MASTER = "--master";

    public static final String DEPLOY_MODE = "--deploy-mode";

    /**
     * --class CLASS_NAME
     */
    public static final String MAIN_CLASS = "--class";

    /**
     * --driver-cores NUM
     */
    public static final String DRIVER_CORES = "--driver-cores";

    /**
     * --driver-memory MEM
     */
    public static final String DRIVER_MEMORY = "--driver-memory";

    /**
     * --num-executors NUM
     */
    public static final String NUM_EXECUTORS = "--num-executors";

    /**
     * --executor-cores NUM
     */
    public static final String EXECUTOR_CORES = "--executor-cores";

    /**
     * --executor-memory MEM
     */
    public static final String EXECUTOR_MEMORY = "--executor-memory";


    /**
     * --queue QUEUE
     */
    public static final String SPARK_QUEUE = "--queue";


    /**
     * --queue --qu
     */
    public static final String FLINK_QUEUE = "--qu";


    /**
     * exit code success
     */
    public static final int EXIT_CODE_SUCCESS = 0;

    /**
     * exit code kill
     */
    public static final int EXIT_CODE_KILL = 137;

    /**
     * exit code failure
     */
    public static final int EXIT_CODE_FAILURE = -1;

    /**
     * date format of yyyyMMdd
     */
    public static final String PARAMETER_FORMAT_DATE = "yyyyMMdd";

    /**
     * date format of yyyyMMddHHmmss
     */
    public static final String PARAMETER_FORMAT_TIME = "yyyyMMddHHmmss";

    /**
     * system date(yyyyMMddHHmmss)
     */
    public static final String PARAMETER_DATETIME = "system.datetime";

    /**
     * system date(yyyymmdd) today
     */
    public static final String PARAMETER_CURRENT_DATE = "system.biz.curdate";

    /**
     * system date(yyyymmdd) yesterday
     */
    public static final String PARAMETER_BUSINESS_DATE = "system.biz.date";

    /**
     * ACCEPTED
     */
    public static final String ACCEPTED = "ACCEPTED";

    /**
     * SUCCEEDED
     */
    public static final String SUCCEEDED = "SUCCEEDED";
    /**
     * NEW
     */
    public static final String NEW = "NEW";
    /**
     * NEW_SAVING
     */
    public static final String NEW_SAVING = "NEW_SAVING";
    /**
     * SUBMITTED
     */
    public static final String SUBMITTED = "SUBMITTED";
    /**
     * FAILED
     */
    public static final String FAILED = "FAILED";
    /**
     * KILLED
     */
    public static final String KILLED = "KILLED";
    /**
     * RUNNING
     */
    public static final String RUNNING = "RUNNING";
    /**
     * underline  "_"
     */
    public static final String UNDERLINE = "_";
    /**
     * quartz job prifix
     */
    public static final String QUARTZ_JOB_PRIFIX = "job";
    /**
     * quartz job group prifix
     */
    public static final String QUARTZ_JOB_GROUP_PRIFIX = "jobgroup";
    /**
     * projectId
     */
    public static final String PROJECT_ID = "projectId";
    /**
     * processId
     */
    public static final String SCHEDULE_ID = "scheduleId";
    /**
     * schedule
     */
    public static final String SCHEDULE = "schedule";
    /**
     * application regex
     */
    public static final String APPLICATION_REGEX = "application_\\d+_\\d+";
    public static final String PID = "pid";
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
    public static final String PROCESS_INSTANCE_STATE = "processInstanceState";
    public static final String TASK_LIST = "taskList";
    public static final String RWXR_XR_X = "rwxr-xr-x";

    /**
     * master/worker server use for zk
     */
    public static final String MASTER_PREFIX = "master";
    public static final String WORKER_PREFIX = "worker";
    public static final String DELETE_ZK_OP = "delete";
    public static final String ADD_ZK_OP = "add";
    public static final String ALIAS = "alias";
    public static final String CONTENT = "content";
    public static final String DEPENDENT_SPLIT = ":||";
    public static final String DEPENDENT_ALL = "ALL";


    /**
     *  preview schedule execute count
     */
    public static final int PREVIEW_SCHEDULE_EXECUTE_COUNT = 5;

    /**
     * kerberos
     */
    public static final String KERBEROS = "kerberos";

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
     * default worker group id
     */
    public static final int DEFAULT_WORKER_ID = -1;

    /**
     * loginUserFromKeytab path
     */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    /**
     * task log info format
     */
    public static final String TASK_LOG_INFO_FORMAT = "TaskLogInfo-%s";

    /**
     * hive conf
     */
    public static final String HIVE_CONF = "hiveconf:";

    //flink 任务
    public static final String FLINK_YARN_CLUSTER = "yarn-cluster";
    public static final String FLINK_RUN_MODE = "-m";
    public static final String FLINK_YARN_SLOT = "-ys";
    public static final String FLINK_APP_NAME = "-ynm";
    public static final String FLINK_TASK_MANAGE = "-yn";

    public static final String FLINK_JOB_MANAGE_MEM = "-yjm";
    public static final String FLINK_TASK_MANAGE_MEM = "-ytm";
    public static final String FLINK_DETACH = "-d";
    public static final String FLINK_MAIN_CLASS = "-c";


    public static final int[] NOT_TERMINATED_STATES = new int[]{
            ExecutionStatus.SUBMITTED_SUCCESS.ordinal(),
            ExecutionStatus.RUNNING_EXEUTION.ordinal(),
            ExecutionStatus.READY_PAUSE.ordinal(),
            ExecutionStatus.READY_STOP.ordinal(),
            ExecutionStatus.NEED_FAULT_TOLERANCE.ordinal(),
            ExecutionStatus.WAITTING_THREAD.ordinal(),
            ExecutionStatus.WAITTING_DEPEND.ordinal()
    };

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
    public static final String COM_DB2_JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";

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
    public static final String DB2 = "DB2";

    /**
     * jdbc url
     */
    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    public static final String JDBC_ORACLE = "jdbc:oracle:thin:@//";
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";
    public static final String JDBC_DB2 = "jdbc:db2://";


    public static final String ADDRESS = "address";
    public static final String DATABASE = "database";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String PRINCIPAL = "principal";
    public static final String OTHER = "other";


    /**
     * session timeout
     */
    public static final int SESSION_TIME_OUT = 7200;
    public static final int MAX_FILE_SIZE = 1024 * 1024 * 1024;
    public static final String UDF = "UDF";
    public static final String CLASS = "class";
    public static final String RECEIVERS = "receivers";
    public static final String RECEIVERS_CC = "receiversCc";


    /**
     * dataSource sensitive param
     */
    public static final String DATASOURCE_PASSWORD_REGEX = "(?<=(\"password\":\")).*?(?=(\"))";
}
