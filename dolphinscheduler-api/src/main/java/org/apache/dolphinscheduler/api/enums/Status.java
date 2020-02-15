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
package org.apache.dolphinscheduler.api.enums;

/**
 *  status enum
 */
public enum Status {

    SUCCESS(0, "success"),

    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid"),
    TASK_TIMEOUT_PARAMS_ERROR(10002, "task timeout parameter is not valid"),
    USER_NAME_EXIST(10003, "user name already exists"),
    USER_NAME_NULL(10004,"user name is null"),
    HDFS_OPERATION_ERROR(10006, "hdfs operation error"),
    TASK_INSTANCE_NOT_FOUND(10008, "task instance not found"),
    TENANT_NAME_EXIST(10009, "tenant code already exists"),
    USER_NOT_EXIST(10010, "user {0} not exists"),
    ALERT_GROUP_NOT_EXIST(10011, "alarm group not found"),
    ALERT_GROUP_EXIST(10012, "alarm group already exists"),
    USER_NAME_PASSWD_ERROR(10013,"user name or password error"),
    LOGIN_SESSION_FAILED(10014,"create session failed!"),
    DATASOURCE_EXIST(10015, "data source name already exists"),
    DATASOURCE_CONNECT_FAILED(10016, "data source connection failed"),
    TENANT_NOT_EXIST(10017, "tenant not exists"),
    PROJECT_NOT_FOUNT(10018, "project {0} not found "),
    PROJECT_ALREADY_EXISTS(10019, "project {0} already exists"),
    TASK_INSTANCE_NOT_EXISTS(10020, "task instance {0} does not exist"),
    TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE(10021, "task instance {0} is not sub process instance"),
    SCHEDULE_CRON_NOT_EXISTS(10022, "scheduler crontab {0} does not exist"),
    SCHEDULE_CRON_ONLINE_FORBID_UPDATE(10023, "online status does not allow updateProcessInstance operations"),
    SCHEDULE_CRON_CHECK_FAILED(10024, "scheduler crontab expression validation failure: {0}"),
    MASTER_NOT_EXISTS(10025, "master does not exist"),
    SCHEDULE_STATUS_UNKNOWN(10026, "unknown command: {0}"),
    CREATE_ALERT_GROUP_ERROR(10027,"create alert group error"),
    QUERY_ALL_ALERTGROUP_ERROR(10028,"query all alertgroup error"),
    LIST_PAGING_ALERT_GROUP_ERROR(10029,"list paging alert group error"),
    UPDATE_ALERT_GROUP_ERROR(10030,"updateProcessInstance alert group error"),
    DELETE_ALERT_GROUP_ERROR(10031,"delete alert group error"),
    ALERT_GROUP_GRANT_USER_ERROR(10032,"alert group grant user error"),
    CREATE_DATASOURCE_ERROR(10033,"create datasource error"),
    UPDATE_DATASOURCE_ERROR(10034,"updateProcessInstance datasource error"),
    QUERY_DATASOURCE_ERROR(10035,"query datasource error"),
    CONNECT_DATASOURCE_FAILURE(10036,"connect datasource failure"),
    CONNECTION_TEST_FAILURE(10037,"connection test failure"),
    DELETE_DATA_SOURCE_FAILURE(10038,"delete data source failure"),
    VERFIY_DATASOURCE_NAME_FAILURE(10039,"verfiy datasource name failure"),
    UNAUTHORIZED_DATASOURCE(10040,"unauthorized datasource"),
    AUTHORIZED_DATA_SOURCE(10041,"authorized data source"),
    LOGIN_SUCCESS(10042,"login success"),
    USER_LOGIN_FAILURE(10043,"user login failure"),
    LIST_WORKERS_ERROR(10044,"list workers error"),
    LIST_MASTERS_ERROR(10045,"list masters error"),
    UPDATE_PROJECT_ERROR(10046,"updateProcessInstance project error"),
    QUERY_PROJECT_DETAILS_BY_ID_ERROR(10047,"query project details by id error"),
    CREATE_PROJECT_ERROR(10048,"create project error"),
    LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR(10049,"login user query project list paging error"),
    DELETE_PROJECT_ERROR(10050,"delete project error"),
    QUERY_UNAUTHORIZED_PROJECT_ERROR(10051,"query unauthorized project error"),
    QUERY_AUTHORIZED_PROJECT(10052,"query authorized project"),
    QUERY_QUEUE_LIST_ERROR(10053,"query queue list error"),
    CREATE_RESOURCE_ERROR(10054,"create resource error"),
    UPDATE_RESOURCE_ERROR(10055,"updateProcessInstance resource error"),
    QUERY_RESOURCES_LIST_ERROR(10056,"query resources list error"),
    QUERY_RESOURCES_LIST_PAGING(10057,"query resources list paging"),
    DELETE_RESOURCE_ERROR(10058,"delete resource error"),
    VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR(10059,"verify resource by name and type error"),
    VIEW_RESOURCE_FILE_ON_LINE_ERROR(10060,"view resource file online error"),
    CREATE_RESOURCE_FILE_ON_LINE_ERROR(10061,"create resource file online error"),
    RESOURCE_FILE_IS_EMPTY(10062,"resource file is empty"),
    EDIT_RESOURCE_FILE_ON_LINE_ERROR(10063,"edit resource file online error"),
    DOWNLOAD_RESOURCE_FILE_ERROR(10064,"download resource file error"),
    CREATE_UDF_FUNCTION_ERROR(10065 ,"create udf function error"),
    VIEW_UDF_FUNCTION_ERROR( 10066,"view udf function error"),
    UPDATE_UDF_FUNCTION_ERROR(10067,"updateProcessInstance udf function error"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR( 10068,"query udf function list paging error"),
    QUERY_DATASOURCE_BY_TYPE_ERROR( 10069,"query datasource by type error"),
    VERIFY_UDF_FUNCTION_NAME_ERROR( 10070,"verify udf function name error"),
    DELETE_UDF_FUNCTION_ERROR( 10071,"delete udf function error"),
    AUTHORIZED_FILE_RESOURCE_ERROR( 10072,"authorized file resource error"),
    UNAUTHORIZED_FILE_RESOURCE_ERROR( 10073,"unauthorized file resource error"),
    UNAUTHORIZED_UDF_FUNCTION_ERROR( 10074,"unauthorized udf function error"),
    AUTHORIZED_UDF_FUNCTION_ERROR(10075,"authorized udf function error"),
    CREATE_SCHEDULE_ERROR(10076,"create schedule error"),
    UPDATE_SCHEDULE_ERROR(10077,"updateProcessInstance schedule error"),
    PUBLISH_SCHEDULE_ONLINE_ERROR(10078,"publish schedule online error"),
    OFFLINE_SCHEDULE_ERROR(10079,"offline schedule error"),
    QUERY_SCHEDULE_LIST_PAGING_ERROR(10080,"query schedule list paging error"),
    QUERY_SCHEDULE_LIST_ERROR(10081,"query schedule list error"),
    QUERY_TASK_LIST_PAGING_ERROR(10082,"query task list paging error"),
    QUERY_TASK_RECORD_LIST_PAGING_ERROR(10083,"query task record list paging error"),
    CREATE_TENANT_ERROR(10084,"create tenant error"),
    QUERY_TENANT_LIST_PAGING_ERROR(10085,"query tenant list paging error"),
    QUERY_TENANT_LIST_ERROR(10086,"query tenant list error"),
    UPDATE_TENANT_ERROR(10087,"updateProcessInstance tenant error"),
    DELETE_TENANT_BY_ID_ERROR(10088,"delete tenant by id error"),
    VERIFY_TENANT_CODE_ERROR(10089,"verify tenant code error"),
    CREATE_USER_ERROR(10090,"create user error"),
    QUERY_USER_LIST_PAGING_ERROR(10091,"query user list paging error"),
    UPDATE_USER_ERROR(10092,"updateProcessInstance user error"),
    DELETE_USER_BY_ID_ERROR(10093,"delete user by id error"),
    GRANT_PROJECT_ERROR(10094,"grant project error"),
    GRANT_RESOURCE_ERROR(10095,"grant resource error"),
    GRANT_UDF_FUNCTION_ERROR(10096,"grant udf function error"),
    GRANT_DATASOURCE_ERROR(10097,"grant datasource error"),
    GET_USER_INFO_ERROR(10098,"get user info error"),
    USER_LIST_ERROR(10099,"user list error"),
    VERIFY_USERNAME_ERROR(10100,"verify username error"),
    UNAUTHORIZED_USER_ERROR(10101,"unauthorized user error"),
    AUTHORIZED_USER_ERROR(10102,"authorized user error"),
    QUERY_TASK_INSTANCE_LOG_ERROR(10103,"view task instance log error"),
    DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR(10104,"download task instance log file error"),
    CREATE_PROCESS_DEFINITION(10105,"create process definition"),
    VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR(10106,"verify process definition name unique error"),
    UPDATE_PROCESS_DEFINITION_ERROR(10107,"updateProcessInstance process definition error"),
    RELEASE_PROCESS_DEFINITION_ERROR(10108,"release process definition error"),
    QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR(10109,"query datail of process definition error"),
    QUERY_PROCCESS_DEFINITION_LIST(10110,"query proccess definition list"),
    ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR(10111,"encapsulation treeview structure error"),
    GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR(10112,"get tasks list by process definition id error"),
    QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR(10113,"query process instance list paging error"),
    QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR(10114,"query task list by process instance id error"),
    UPDATE_PROCESS_INSTANCE_ERROR(10115,"updateProcessInstance process instance error"),
    QUERY_PROCESS_INSTANCE_BY_ID_ERROR(10116,"query process instance by id error"),
    DELETE_PROCESS_INSTANCE_BY_ID_ERROR(10117,"delete process instance by id error"),
    QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR(10118,"query sub process instance detail info by task id error"),
    QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR(10119,"query parent process instance detail info by sub process instance id error"),
    QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR(10120,"query process instance all variables error"),
    ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR(10121,"encapsulation process instance gantt structure error"),
    QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR(10122,"query proccess definition list paging error"),
    SIGN_OUT_ERROR(10123,"sign out error"),
    TENANT_CODE_HAS_ALREADY_EXISTS(10124,"tenant code has already exists"),
    IP_IS_EMPTY(10125,"ip is empty"),
    SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE(10126, "schedule release is already {0}"),
    CREATE_QUEUE_ERROR(10127, "create queue error"),
    QUEUE_NOT_EXIST(10128, "queue {0} not exists"),
    QUEUE_VALUE_EXIST(10129, "queue value {0} already exists"),
    QUEUE_NAME_EXIST(10130, "queue name {0} already exists"),
    UPDATE_QUEUE_ERROR(10131, "update queue error"),
    NEED_NOT_UPDATE_QUEUE(10132, "no content changes, no updates are required"),
    VERIFY_QUEUE_ERROR(10133,"verify queue error"),
    NAME_NULL(10134,"name must be not null"),
    NAME_EXIST(10135, "name {0} already exists"),
    SAVE_ERROR(10136, "save error"),
    DELETE_PROJECT_ERROR_DEFINES_NOT_NULL(10137, "please delete the process definitions in project first!"),
    BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR(10117,"batch delete process instance by ids {0} error"),
    PREVIEW_SCHEDULE_ERROR(10139,"preview schedule error"),
    PARSE_TO_CRON_EXPRESSION_ERROR(10140,"parse cron to cron expression error"),
    SCHEDULE_START_TIME_END_TIME_SAME(10141,"The start time must not be the same as the end"),
    DELETE_TENANT_BY_ID_FAIL(100142,"delete tenant by id fail, for there are {0} process instances in executing using it"),
    DELETE_TENANT_BY_ID_FAIL_DEFINES(100143,"delete tenant by id fail, for there are {0} process definitions using it"),
    DELETE_TENANT_BY_ID_FAIL_USERS(100144,"delete tenant by id fail, for there are {0} users using it"),

    DELETE_WORKER_GROUP_BY_ID_FAIL(100145,"delete worker group by id fail, for there are {0} process instances in executing using it"),

    QUERY_WORKER_GROUP_FAIL(100146,"query worker group fail "),
    DELETE_WORKER_GROUP_FAIL(100147,"delete worker group fail "),


    UDF_FUNCTION_NOT_EXIST(20001, "UDF function not found"),
    UDF_FUNCTION_EXISTS(20002, "UDF function already exists"),
    RESOURCE_NOT_EXIST(20004, "resource not exist"),
    RESOURCE_EXIST(20005, "resource already exists"),
    RESOURCE_SUFFIX_NOT_SUPPORT_VIEW(20006, "resource suffix do not support online viewing"),
    RESOURCE_SIZE_EXCEED_LIMIT(20007, "upload resource file size exceeds limit"),
    RESOURCE_SUFFIX_FORBID_CHANGE(20008, "resource suffix not allowed to be modified"),
    UDF_RESOURCE_SUFFIX_NOT_JAR(20009, "UDF resource suffix name must be jar"),
    HDFS_COPY_FAIL(20009, "hdfs copy {0} -> {1} fail"),
    RESOURCE_FILE_EXIST(20010, "resource file {0} already exists in hdfs,please delete it or change name!"),
    RESOURCE_FILE_NOT_EXIST(20011, "resource file {0} not exists in hdfs!"),



    USER_NO_OPERATION_PERM(30001, "user has no operation privilege"),
    USER_NO_OPERATION_PROJECT_PERM(30002, "user {0} is not has project {1} permission"),


    PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist"),
    PROCESS_INSTANCE_EXIST(50002, "process instance {0} already exists"),
    PROCESS_DEFINE_NOT_EXIST(50003, "process definition {0} does not exist"),
    PROCESS_DEFINE_NOT_RELEASE(50004, "process definition {0} not on line"),
    PROCESS_INSTANCE_ALREADY_CHANGED(50005, "the status of process instance {0} is already {1}"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(50006, "the status of process instance {0} is {1},Cannot perform {2} operation"),
    SUB_PROCESS_INSTANCE_NOT_EXIST(50007, "the task belong to process instance does not exist"),
    PROCESS_DEFINE_NOT_ALLOWED_EDIT(50008, "process definition {0} does not allow edit"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(50009, "process instance {0} is executing the command, please wait ..."),
    PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE(50010, "process instance {0} is not sub process instance"),
    TASK_INSTANCE_STATE_COUNT_ERROR(50011,"task instance state count error"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(50012,"count process instance state error"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(50013,"count process definition user error"),
    START_PROCESS_INSTANCE_ERROR(50014,"start process instance error"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015,"execute process instance error"),
    CHECK_PROCESS_DEFINITION_ERROR(50016,"check process definition error"),
    QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR(50017,"query recipients and copyers by process definition error"),
    DATA_IS_NOT_VALID(50017,"data %s not valid"),
    DATA_IS_NULL(50018,"data %s is null"),
    PROCESS_NODE_HAS_CYCLE(50019,"process node has cycle"),
    PROCESS_NODE_S_PARAMETER_INVALID(50020,"process node %s parameter invalid"),
    PROCESS_DEFINE_STATE_ONLINE(50021, "process definition {0} is already on line"),
    DELETE_PROCESS_DEFINE_BY_ID_ERROR(50022,"delete process definition by id error"),
    SCHEDULE_CRON_STATE_ONLINE(50023,"the status of schedule {0} is already on line"),
    DELETE_SCHEDULE_CRON_BY_ID_ERROR(50024,"delete schedule by id error"),
    BATCH_DELETE_PROCESS_DEFINE_ERROR(50025,"batch delete process definition error"),
    BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR(50026,"batch delete process definition by ids {0} error"),
    TENANT_NOT_SUITABLE(50027,"there is not any tenant suitable, please choose a tenant available."),
    EXPORT_PROCESS_DEFINE_BY_ID_ERROR(50028,"export process definition by id error"),
    IMPORT_PROCESS_DEFINE_ERROR(50029,"import process definition error"),
    PROCESS_NODE_EXTRA_PARAMETER_INVALID(50030,"process node %s extra parameter invalid"),

    HDFS_NOT_STARTUP(60001,"hdfs not startup"),
    HDFS_TERANT_RESOURCES_FILE_EXISTS(60002,"resource file exists,please delete resource first"),
    HDFS_TERANT_UDFS_FILE_EXISTS(60003,"udf file exists,please delete resource first"),

    /**
     * for monitor
     */
    QUERY_DATABASE_STATE_ERROR(70001,"query database state error"),
    QUERY_ZOOKEEPER_STATE_ERROR(70002,"query zookeeper state error"),



    CREATE_ACCESS_TOKEN_ERROR(70010,"create access token error"),
    GENERATE_TOKEN_ERROR(70011,"generate token error"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012,"query access token list paging error"),
    UPDATE_ACCESS_TOKEN_ERROR(70013,"update access token error"),
    DELETE_ACCESS_TOKEN_ERROR(70014,"delete access token error"),
    ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist"),


    COMMAND_STATE_COUNT_ERROR(80001,"task instance state count error"),

    QUEUE_COUNT_ERROR(90001,"queue count error"),

    KERBEROS_STARTUP_STATE(100001,"get kerberos startup state error"),
    ;

    private final int code;
    private final String msg;

    private Status(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
