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
package cn.escheduler.common;

import cn.escheduler.common.utils.OSUtils;

import java.util.regex.Pattern;

/**
 * Constants
 */
public final class Constants {

    /**
     * zookeeper properties path
     */
    public static final String ZOOKEEPER_PROPERTIES_PATH = "zookeeper.properties";

    /**
     * worker properties path
     */
    public static final String WORKER_PROPERTIES_PATH = "worker.properties";

    /**
     * master properties path
     */
    public static final String MASTER_PROPERTIES_PATH = "master.properties";

    /**
     * hadoop properties path
     */
    public static final String HADOOP_PROPERTIES_PATH = "/common/hadoop/hadoop.properties";

    /**
     * common properties path
     */
    public static final String COMMON_PROPERTIES_PATH = "/common/common.properties";

    /**
     * dao properties path
     */
    public static final String DAO_PROPERTIES_PATH = "/dao/data_source.properties";

    /**
     * fs.defaultFS
     */
    public static final String FS_DEFAULTFS = "fs.defaultFS";

    /**
     * yarn.resourcemanager.ha.rm.idsfs.defaultFS
     */
    public static final String YARN_RESOURCEMANAGER_HA_RM_IDS = "yarn.resourcemanager.ha.rm.ids";

    /**
     * yarn.application.status.address
     */
    public static final String YARN_APPLICATION_STATUS_ADDRESS = "yarn.application.status.address";

    /**
     * spring.redis.maxIdle
     */
    public static final String SPRING_REDIS_MAXIDLE = "spring.redis.maxIdle";

    /**
     * spring.redis.maxTotal
     */
    public static final String SPRING_REDIS_MAXTOTAL = "spring.redis.maxTotal";

    /**
     * spring.redis.host
     */
    public static final String SPRING_REDIS_HOST = "spring.redis.host";

    /**
     * spring.redis.port
     */
    public static final String SPRING_REDIS_PORT = "spring.redis.port";

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
     * escheduler.env.path
     */
    public static final String ESCHEDULER_ENV_PATH = "escheduler.env.path";

    /**
     * escheduler.env.py
     */
    public static final String ESCHEDULER_ENV_PY = "escheduler.env.py";

    /**
     * resource.view.suffixs
     */
    public static final String RESOURCE_VIEW_SUFFIXS = "resource.view.suffixs";

    /**
     * development.state
     */
    public static final String DEVELOPMENT_STATE = "development.state";

    /**
     * hdfs.startup.state
     */
    public static final String HDFS_STARTUP_STATE = "hdfs.startup.state";

    /**
     * zookeeper quorum
     */
    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";

    /**
     * MasterServer directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_MASTERS = "zookeeper.escheduler.masters";

    /**
     * WorkerServer directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_WORKERS = "zookeeper.escheduler.workers";

    /**
     * all servers directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_DEAD_SERVERS = "zookeeper.escheduler.dead.servers";

    /**
     * MasterServer lock directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_LOCK_MASTERS = "zookeeper.escheduler.lock.masters";

    /**
     * WorkerServer lock directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_LOCK_WORKERS = "zookeeper.escheduler.lock.workers";

    /**
     * MasterServer failover directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_LOCK_FAILOVER_MASTERS = "zookeeper.escheduler.lock.failover.masters";

    /**
     * WorkerServer failover directory registered in zookeeper
     */
    public static final String ZOOKEEPER_ESCHEDULER_LOCK_FAILOVER_WORKERS = "zookeeper.escheduler.lock.failover.workers";

    /**
     * need send warn times when master server or worker server failover
     */
    public static final int ESCHEDULER_WARN_TIMES_FAILOVER = 3;

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
     * ZOOKEEPER_SESSION_TIMEOUT
     */
    public static final String ZOOKEEPER_SESSION_TIMEOUT = "zookeeper.session.timeout";

    public static final String ZOOKEEPER_CONNECTION_TIMEOUT = "zookeeper.connection.timeout";

    public static final String ZOOKEEPER_RETRY_SLEEP = "zookeeper.retry.sleep";

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
     * escheduler tasks queue
     */
    public static final String SCHEDULER_TASKS_QUEUE = "tasks_queue";

    public static final String SCHEDULER_TASKS_KILL = "tasks_kill";
    public static final String ZOOKEEPER_SCHEDULER_ROOT = "zookeeper.escheduler.root";

    public static final String SCHEDULER_QUEUE_IMPL = "escheduler.queue.impl";

    public static final String SCHEDULER_QUEUE_REDIS_IMPL = "redis";


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
    public static final Pattern REGEX_USER_NAME = Pattern.compile("[a-zA-Z0-9]{3,20}");

    /**
     * email regex
     */
    public static final Pattern REGEX_MAIL_NAME = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

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
    public static final int defaulWorkerHeartbeatThreadNum = 5;

    /**
     * heartbeat interval
     */
    public static final int defaultWorkerHeartbeatInterval = 60;

    /**
     * worker fetch task number
     */
    public static final int defaultWorkerFetchTaskNum = 1;

    /**
     * worker execute threads number
     */
    public static final int defaultWorkerExecThreadNum = 10;

    /**
     * master cpu load
     */
    public static final int defaultMasterCpuLoad = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * master reserved memory
     */
    public static final double defaultMasterReservedMemory = OSUtils.totalMemorySize() / 10;

    /**
     * worker cpu load
     */
    public static final int defaultWorkerCpuLoad = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * worker reserved memory
     */
    public static final double defaultWorkerReservedMemory = OSUtils.totalMemorySize() / 10;


    /**
     * master execute threads number
     */
    public static final int defaultMasterExecThreadNum = 100;


    /**
     * default master concurrent task execute num
     */
    public static final int defaultMasterTaskExecNum = 20;

    /**
     * default log cache rows num,output when reach the number
     */
    public static final int defaultLogRowsNum = 4 * 16;

    /**
     * log flush interval，output when reach the interval
     */
    public static final int defaultLogFlushInterval = 1000;


    /**
     * default master heartbeat thread number
     */
    public static final int defaulMasterHeartbeatThreadNum = 5;


    /**
     * default master heartbeat interval
     */
    public static final int defaultMasterHeartbeatInterval = 60;

    /**
     * default master commit retry times
     */
    public static final int defaultMasterCommitRetryTimes = 5;


    /**
     * default master commit retry interval
     */
    public static final int defaultMasterCommitRetryInterval = 100;

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
    public static final String TASK_RECORD_PROPERTIES_PATH = "dao/data_source.properties";

    public static final String TASK_RECORD_URL = "task.record.datasource.url";

    public static final String TASK_RECORD_FLAG = "task.record.flag";

    public static final String TASK_RECORD_USER = "task.record.datasource.username";

    public static final String TASK_RECORD_PWD = "task.record.datasource.password";

    public static  String TASK_RECORD_TABLE_HIVE_LOG = "eamp_hive_log_hd";

    public static  String TASK_RECORD_TABLE_HISTORY_HIVE_LOG = "eamp_hive_hist_log_hd";

    public static final String STATUS = "status";


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
    public static final int HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH = 6;


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
     * jdbc class name
     */
    /**
     * mysql
     */
    public static final String JDBC_MYSQL_CLASS_NAME = "com.mysql.jdbc.Driver";

    /**
     * postgresql
     */
    public static final String JDBC_POSTGRESQL_CLASS_NAME = "org.postgresql.Driver";

    /**
     * hive
     */
    public static final String JDBC_HIVE_CLASS_NAME = "org.apache.hive.jdbc.HiveDriver";

    /**
     * spark
     */
    public static final String JDBC_SPARK_CLASS_NAME = "org.apache.hive.jdbc.HiveDriver";

    /**
     * ClickHouse
     */
    public static final String JDBC_CLICKHOUSE_CLASS_NAME = "ru.yandex.clickhouse.ClickHouseDriver";

    /**
     * Oracle
     */
    public static final String JDBC_ORACLE_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";

    /**
     * Oracle
     */
    public static final String JDBC_SQLSERVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    /**
     * spark params constant
     */
    public static final String MASTER = "--master";

    public static final String DEPLOY_MODE = "--deploy-mode";

    /**
     * --class CLASS_NAME
     */
    public static final String CLASS = "--class";

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
     *
     */
}
