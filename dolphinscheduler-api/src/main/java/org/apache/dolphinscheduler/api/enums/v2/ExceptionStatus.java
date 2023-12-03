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

package org.apache.dolphinscheduler.api.enums.v2;

import org.apache.dolphinscheduler.api.enums.Status;

import java.util.Locale;
import java.util.Optional;

import org.springframework.context.i18n.LocaleContextHolder;

public enum ExceptionStatus {

    INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),
    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
    QUERY_ALL_ALERTGROUP_ERROR(10028, "query all alertgroup error", "查询告警组错误"),
    LIST_PAGING_ALERT_GROUP_ERROR(10029, "list paging alert group error", "分页查询告警组错误"),
    UPDATE_ALERT_GROUP_ERROR(10030, "update alert group error", "更新告警组错误"),
    DELETE_ALERT_GROUP_ERROR(10031, "delete alert group error", "删除告警组错误"),
    CREATE_DATASOURCE_ERROR(10033, "create datasource error", "创建数据源错误"),
    UPDATE_DATASOURCE_ERROR(10034, "update datasource error", "更新数据源错误"),
    QUERY_DATASOURCE_ERROR(10035, "query datasource error", "查询数据源错误"),
    CONNECT_DATASOURCE_FAILURE(10036, "connect datasource failure", "建立数据源连接失败"),
    CONNECTION_TEST_FAILURE(10037, "connection test failure", "测试数据源连接失败"),
    DELETE_DATA_SOURCE_FAILURE(10038, "delete data source failure", "删除数据源失败"),
    VERIFY_DATASOURCE_NAME_FAILURE(10039, "verify datasource name failure", "验证数据源名称失败"),
    AUTHORIZED_DATA_SOURCE(10041, "authorized data source", "授权数据源失败"),
    USER_LOGIN_FAILURE(10043, "user login failure", "用户登录失败"),
    LIST_WORKERS_ERROR(10044, "list workers error", "查询worker列表错误"),
    LIST_MASTERS_ERROR(10045, "list masters error", "查询master列表错误"),
    UPDATE_PROJECT_ERROR(10046, "update project error", "更新项目信息错误"),
    QUERY_PROJECT_DETAILS_BY_CODE_ERROR(10047, "query project details by code error", "查询项目详细信息错误"),
    CREATE_PROJECT_ERROR(10048, "create project error", "创建项目错误"),
    LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR(10049, "login user query project list paging error", "分页查询项目列表错误"),
    DELETE_PROJECT_ERROR(10050, "delete project error", "删除项目错误"),
    QUERY_UNAUTHORIZED_PROJECT_ERROR(10051, "query unauthorized project error", "查询未授权项目错误"),
    QUERY_AUTHORIZED_PROJECT(10052, "query authorized project", "查询授权项目错误"),
    QUERY_QUEUE_LIST_ERROR(10053, "query queue list error", "查询队列列表错误"),
    CREATE_RESOURCE_ERROR(10054, "create resource error", "创建资源错误"),
    UPDATE_RESOURCE_ERROR(10055, "update resource error", "更新资源错误"),
    QUERY_RESOURCES_LIST_ERROR(10056, "query resources list error", "查询资源列表错误"),
    QUERY_AUDIT_LOG_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),
    QUERY_RESOURCES_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),
    DELETE_RESOURCE_ERROR(10058, "delete resource error", "删除资源错误"),
    VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR(10059, "verify resource by name and type error", "资源名称或类型验证错误"),
    VIEW_RESOURCE_FILE_ON_LINE_ERROR(10060, "view resource file online error", "查看资源文件错误"),
    CREATE_RESOURCE_FILE_ON_LINE_ERROR(10061, "create resource file online error", "创建资源文件错误"),
    RESOURCE_FILE_IS_EMPTY(10062, "resource file is empty", "资源文件内容不能为空"),
    EDIT_RESOURCE_FILE_ON_LINE_ERROR(10063, "edit resource file online error", "更新资源文件错误"),
    DOWNLOAD_RESOURCE_FILE_ERROR(10064, "download resource file error", "下载资源文件错误"),
    CREATE_UDF_FUNCTION_ERROR(10065, "create udf function error", "创建UDF函数错误"),
    VIEW_UDF_FUNCTION_ERROR(10066, "view udf function error", "查询UDF函数错误"),
    UPDATE_UDF_FUNCTION_ERROR(10067, "update udf function error", "更新UDF函数错误"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR(10068, "query udf function list paging error", "分页查询UDF函数列表错误"),
    UERY_UDF_FUNCTION_LIST_PAGING_ERROR(10068, "query udf function list paging error", "分页查询UDF函数列表错误"),
    QUERY_DATASOURCE_BY_TYPE_ERROR(10069, "query datasource by type error", "查询数据源信息错误"),
    VERIFY_UDF_FUNCTION_NAME_ERROR(10070, "verify udf function name error", "UDF函数名称验证错误"),
    DELETE_UDF_FUNCTION_ERROR(10071, "delete udf function error", "删除UDF函数错误"),
    AUTHORIZED_FILE_RESOURCE_ERROR(10072, "authorized file resource error", "授权资源文件错误"),
    AUTHORIZE_RESOURCE_TREE(10073, "authorize resource tree display error", "授权资源目录树错误"),
    UNAUTHORIZED_UDF_FUNCTION_ERROR(10074, "unauthorized udf function error", "查询未授权UDF函数错误"),
    AUTHORIZED_UDF_FUNCTION_ERROR(10075, "authorized udf function error", "授权UDF函数错误"),
    CREATE_SCHEDULE_ERROR(10076, "create schedule error", "创建调度配置错误"),
    UPDATE_SCHEDULE_ERROR(10077, "update schedule error", "更新调度配置错误"),
    PUBLISH_SCHEDULE_ONLINE_ERROR(10078, "publish schedule online error", "上线调度配置错误"),
    OFFLINE_SCHEDULE_ERROR(10079, "offline schedule error", "下线调度配置错误"),
    QUERY_SCHEDULE_LIST_PAGING_ERROR(10080, "query schedule list paging error", "分页查询调度配置列表错误"),
    QUERY_SCHEDULE_LIST_ERROR(10081, "query schedule list error", "查询调度配置列表错误"),
    QUERY_TASK_LIST_PAGING_ERROR(10082, "query task list paging error", "分页查询任务列表错误"),
    CREATE_TENANT_ERROR(10084, "create tenant error", "创建租户错误"),
    QUERY_TENANT_LIST_PAGING_ERROR(10085, "query tenant list paging error", "分页查询租户列表错误"),
    QUERY_TENANT_LIST_ERROR(10086, "query tenant list error", "查询租户列表错误"),
    UPDATE_TENANT_ERROR(10087, "update tenant error", "更新租户错误"),
    DELETE_TENANT_BY_ID_ERROR(10088, "delete tenant by id error", "删除租户错误"),
    VERIFY_OS_TENANT_CODE_ERROR(10089, "verify os tenant code error", "操作系统租户验证错误"),
    UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
    USER_LIST_ERROR(10099, "user list error", "查询用户列表错误"),
    VERIFY_USERNAME_ERROR(10100, "verify username error", "用户名验证错误"),
    UNAUTHORIZED_USER_ERROR(10101, "unauthorized user error", "查询未授权用户错误"),
    AUTHORIZED_USER_ERROR(10102, "authorized user error", "查询授权用户错误"),
    QUERY_TASK_INSTANCE_LOG_ERROR(10103, "view task instance log error", "查询任务实例日志错误"),
    DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR(10104, "download task instance log file error", "下载任务日志文件错误"),
    CREATE_PROCESS_DEFINITION_ERROR(10105, "create process definition error", "创建工作流错误"),
    VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR(10106, "verify process definition name unique error", "工作流定义名称验证错误"),
    UPDATE_PROCESS_DEFINITION_ERROR(10107, "update process definition error", "更新工作流定义错误"),
    RELEASE_PROCESS_DEFINITION_ERROR(10108, "release process definition error", "上线工作流错误"),
    QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR(10109, "query detail of process definition error", "查询工作流详细信息错误"),
    QUERY_PROCESS_DEFINITION_LIST(10110, "query process definition list", "查询工作流列表错误"),
    ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR(10111, "encapsulation treeview structure error", "查询工作流树形图数据错误"),
    GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR(10112, "get tasks list by process definition id error",
            "查询工作流定义节点信息错误"),
    QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR(10113, "query process instance list paging error", "分页查询工作流实例列表错误"),
    QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR(10114, "query task list by process instance id error", "查询任务实例列表错误"),
    UPDATE_PROCESS_INSTANCE_ERROR(10115, "update process instance error", "更新工作流实例错误"),
    QUERY_PROCESS_INSTANCE_BY_ID_ERROR(10116, "query process instance by id error", "查询工作流实例错误"),
    DELETE_PROCESS_INSTANCE_BY_ID_ERROR(10117, "delete process instance by id error", "删除工作流实例错误"),
    BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR(10117, "batch delete process instance by ids {0} error",
            "批量删除工作流实例错误: {0}"),
    QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR(10118,
            "query sub process instance detail info by task id error", "查询子流程任务实例错误"),
    QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR(10119,
            "query parent process instance detail info by sub process instance id error", "查询子流程该工作流实例错误"),
    QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR(10120, "query process instance all variables error", "查询工作流自定义变量信息错误"),
    ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR(10121, "encapsulation process instance gantt structure error",
            "查询工作流实例甘特图数据错误"),
    QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR(10122, "query process definition list paging error", "分页查询工作流定义列表错误"),
    SIGN_OUT_ERROR(10123, "sign out error", "退出错误"),
    IP_IS_EMPTY(10125, "ip is empty", "IP地址不能为空"),
    CREATE_QUEUE_ERROR(10127, "create queue error", "创建队列错误"),
    QUEUE_NOT_EXIST(10128, "queue {0} not exists", "队列ID[{0}]不存在"),
    QUEUE_VALUE_EXIST(10129, "queue value {0} already exists", "队列值[{0}]已存在"),
    QUEUE_NAME_EXIST(10130, "queue name {0} already exists", "队列名称[{0}]已存在"),
    SAVE_ERROR(10136, "save error", "保存错误"),
    PREVIEW_SCHEDULE_ERROR(10139, "preview schedule error", "预览调度配置错误"),
    QUERY_WORKFLOW_STATES_COUNT_ERROR(10139, "query all workflow states count error", "查询所有工作流状态数量错误"),
    PARSE_TO_CRON_EXPRESSION_ERROR(10140, "parse cron to cron expression error", "解析调度表达式错误"),
    QUERY_ONE_WORKFLOW_STATE_COUNT_ERROR(10140, "query one workflow state count error", "查询工作流状态数量错误"),
    QUERY_TASK_STATES_COUNT_ERROR(10141, "query all task states count error", "查询所有任务状态数量错误"),
    QUERY_ONE_TASK_STATES_COUNT_ERROR(10142, "query one task states count error", "查询任务状态数量错误"),
    QUERY_WORKER_GROUP_FAIL(10146, "query worker group fail ", "查询worker分组失败"),
    DELETE_WORKER_GROUP_FAIL(10147, "delete worker group fail ", "删除worker分组失败"),
    SWITCH_PROCESS_DEFINITION_VERSION_ERROR(10151, "Switch process definition version error", "切换工作流版本出错"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR(10152,
            "Switch process definition version error: not exists process definition, [process definition id {0}]",
            "切换工作流版本出错：工作流不存在，[工作流id {0}]"),
    QUERY_PROCESS_DEFINITION_VERSIONS_ERROR(10154, "query process definition versions error", "查询工作流历史版本信息出错"),
    DELETE_PROCESS_DEFINITION_VERSION_ERROR(10156, "delete process definition version error", "删除工作流历史版本出错"),
    BATCH_COPY_PROCESS_DEFINITION_ERROR(10159, "batch copy process definition error", "复制工作流错误"),
    BATCH_MOVE_PROCESS_DEFINITION_ERROR(10160, "batch move process definition error", "移动工作流错误"),
    QUERY_WORKFLOW_LINEAGE_ERROR(10161, "query workflow lineage error", "查询血缘失败"),
    QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR(10162, "query authorized and user created project error error",
            "查询授权的和用户创建的项目错误"),
    FORCE_TASK_SUCCESS_ERROR(10165, "force task success error", "强制成功任务实例错误"),
    QUERY_WORKER_ADDRESS_LIST_FAIL(10178, "query worker address list fail ", "查询worker地址列表失败"),
    QUERY_ALERT_GROUP_ERROR(10180, "query alert group error", "查询告警组错误"),
    CURRENT_LOGIN_USER_TENANT_NOT_EXIST(10181, "the tenant of the currently login user is not specified",
            "未指定当前登录用户的租户"),
    REVOKE_PROJECT_ERROR(10182, "revoke project error", "撤销项目授权错误"),
    QUERY_AUTHORIZED_USER(10183, "query authorized user error", "查询拥有项目权限的用户错误"),
    CREATE_USER_ERROR(10090, "create user error", "创建用户错误"),
    QUERY_USER_LIST_PAGING_ERROR(10091, "query user list paging error", "分页查询用户列表错误"),
    QUERY_EXECUTING_WORKFLOW_ERROR(10192, "query executing workflow error", "查询运行的工作流实例错误"),
    DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
    GRANT_PROJECT_ERROR(10094, "grant project error", "授权项目错误"),
    GRANT_RESOURCE_ERROR(10095, "grant resource error", "授权资源错误"),
    GRANT_UDF_FUNCTION_ERROR(10096, "grant udf function error", "授权UDF函数错误"),
    GRANT_DATASOURCE_ERROR(10097, "grant datasource error", "授权数据源错误"),
    GET_USER_INFO_ERROR(10098, "get user info error", "获取用户信息错误"),
    TASK_WITH_DEPENDENT_ERROR(10195, "task used in other tasks", "删除被其他任务引用"),
    TASK_SAVEPOINT_ERROR(10196, "task savepoint error", "任务实例savepoint错误"),
    TASK_STOP_ERROR(10197, "task stop error", "任务实例停止错误"),
    LIST_TASK_TYPE_ERROR(10200, "list task type error", "查询任务类型列表错误"),
    ADD_TASK_TYPE_ERROR(10200, "add task type error", "添加任务类型错误"),
    DELETE_TASK_TYPE_ERROR(10200, "delete task type error", "删除任务类型错误"),
    QUERY_TASK_INSTANCE_ERROR(10205, "query task instance error", "查询任务实例错误"),
    LIST_AZURE_DATA_FACTORY_ERROR(10208, "list azure data factory error", "查询AZURE数据工厂列表错误"),
    LIST_AZURE_RESOURCE_GROUP_ERROR(10209, "list azure resource group error", "查询AZURE资源组列表错误"),
    LIST_AZURE_DATA_FACTORY_PIPELINE_ERROR(10210, "list azure data factory pipeline error", "查询AZURE数据工厂pipeline列表错误"),
    NOT_SUPPORT_SSO(10211, "Not support SSO login.", "不支持SSO登录"),
    CREATE_PROJECT_PARAMETER_ERROR(10214, "create project parameter error", "创建项目参数错误"),

    UPDATE_PROJECT_PARAMETER_ERROR(10215, "update project parameter error", "更新项目参数错误"),

    DELETE_PROJECT_PARAMETER_ERROR(10216, "delete project parameter error {0}", "删除项目参数错误 {0}"),

    QUERY_PROJECT_PARAMETER_ERROR(10217, "query project parameter error", "查询项目参数错误"),
    UPDATE_PROJECT_PREFERENCE_ERROR(10301, "update project preference error", "更新项目偏好设置错误"),
    QUERY_PROJECT_PREFERENCE_ERROR(10302, "query project preference error", "查询项目偏好设置错误"),
    UPDATE_PROJECT_PREFERENCE_STATE_ERROR(10303, "Failed to update the state of the project preference", "更新项目偏好设置错误"),
    DELETE_QUEUE_BY_ID_ERROR(10307, "delete queue by id error", "删除队列错误"),
    UPDATE_QUEUE_ERROR(10131, "update queue error", "更新队列信息错误"),
    VERIFY_QUEUE_ERROR(10133, "verify queue error", "验证队列信息错误"),
    RESOURCE_NOT_EXIST(20004, "resource not exist", "资源不存在"),
    REMOVE_TASK_INSTANCE_CACHE_ERROR(20019, "remove task instance cache error", "删除任务实例缓存错误"),
    TASK_INSTANCE_STATE_COUNT_ERROR(50011, "task instance state count error", "查询各状态任务实例数错误"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(50012, "count process instance state error", "查询各状态流程实例数错误"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(50013, "count process definition user error", "查询各用户流程定义数错误"),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    CHECK_PROCESS_DEFINITION_ERROR(50016, "check process definition error", "工作流定义错误"),
    DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
    DELETE_PROCESS_DEFINE_BY_CODE_ERROR(50022, "delete process definition by code error", "删除工作流定义错误"),
    DELETE_SCHEDULE_BY_ID_ERROR(50024, "delete schedule by id error", "删除调度配置错误"),
    BATCH_DELETE_PROCESS_DEFINE_ERROR(50025, "batch delete process definition error", "批量删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR(50026, "batch delete process definition by codes error: {0}",
            "批量删除工作流定义错误: {0}"),
    IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),
    CREATE_PROCESS_TASK_RELATION_ERROR(50032, "create process task relation error", "创建工作流任务关系错误"),
    CREATE_TASK_DEFINITION_ERROR(50037, "create task definition error", "创建任务错误"),
    UPDATE_TASK_DEFINITION_ERROR(50038, "update task definition error", "更新任务定义错误"),
    QUERY_TASK_DEFINITION_VERSIONS_ERROR(50039, "query task definition versions error", "查询任务历史版本信息出错"),
    SWITCH_TASK_DEFINITION_VERSION_ERROR(50040, "Switch task definition version error", "切换任务版本出错"),
    DELETE_TASK_DEFINITION_VERSION_ERROR(50041, "delete task definition version error", "删除任务历史版本出错"),
    DELETE_TASK_DEFINE_BY_CODE_ERROR(50042, "delete task definition by code error", "删除任务定义错误"),
    QUERY_DETAIL_OF_TASK_DEFINITION_ERROR(50043, "query detail of task definition error", "查询任务详细信息错误"),
    QUERY_TASK_DEFINITION_LIST_PAGING_ERROR(50044, "query task definition list paging error", "分页查询任务定义列表错误"),
    RELEASE_TASK_DEFINITION_ERROR(50046, "release task definition error", "上线任务错误"),
    MOVE_PROCESS_TASK_RELATION_ERROR(50047, "move process task relation error", "移动任务到其他工作流错误"),
    DELETE_TASK_PROCESS_RELATION_ERROR(50048, "delete process task relation error", "删除工作流任务关系错误"),
    QUERY_TASK_PROCESS_RELATION_ERROR(50049, "query process task relation error", "查询工作流任务关系错误"),
    DELETE_EDGE_ERROR(50055, "delete edge error", "删除工作流任务连接线错误"),
    BATCH_EXECUTE_PROCESS_INSTANCE_ERROR(50058, "change process instance status error: {0}", "修改工作实例状态错误: {0}"),
    UPDATE_UPSTREAM_TASK_PROCESS_RELATION_ERROR(50065, "update task upstream relation error", "更新任务上游关系错误"),
    QUERY_DATABASE_STATE_ERROR(70001, "query database state error", "查询数据库状态错误"),
    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist, tokenId {0}", "访问token不存在, {0}"),
    QUERY_ACCESSTOKEN_BY_USER_ERROR(70016, "query access token by user error", "查询访问指定用户的token错误"),
    COMMAND_STATE_COUNT_ERROR(80001, "task instance state count error", "查询各状态任务实例数错误"),
    QUEUE_COUNT_ERROR(90001, "queue count error", "查询队列数据错误"),
    KERBEROS_STARTUP_STATE(100001, "get kerberos startup state error", "获取kerberos启动状态错误"),
    UNAUTHORIZED_DATASOURCE(10040, "unauthorized datasource", "未经授权的数据源"),
    QUERY_PLUGINS_ERROR(110003, "query plugins error", "查询插件错误"),
    UPDATE_ALERT_PLUGIN_INSTANCE_ERROR(110005, "update alert plugin instance error", "更新告警组和告警组插件实例错误"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR(110006, "delete alert plugin instance error", "删除告警组和告警组插件实例错误"),
    GET_ALERT_PLUGIN_INSTANCE_ERROR(110007, "get alert plugin instance error", "获取告警组和告警组插件实例错误"),
    CREATE_ALERT_PLUGIN_INSTANCE_ERROR(110008, "create alert plugin instance error", "创建告警组和告警组插件实例错误"),
    QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR(110009, "query all alert plugin instance error", "查询所有告警实例失败"),
    LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR(110011, "query plugin instance page error", "分页查询告警实例失败"),
    SEND_TEST_ALERT_PLUGIN_INSTANCE_ERROR(110016, "send test alert plugin instance error", "发送测试告警错误"),
    UPDATE_ENVIRONMENT_ERROR(120005, "update environment [{0}] info error", "更新环境[{0}]信息失败"),
    DELETE_ENVIRONMENT_ERROR(120006, "delete environment error", "删除环境信息失败"),
    CREATE_CLUSTER_ERROR(120020, "create cluster error", "创建集群失败"),
    CREATE_ENVIRONMENT_ERROR(120001, "create environment error", "创建环境失败"),
    UPDATE_CLUSTER_ERROR(120024, "update cluster [{0}] info error", "更新集群[{0}]信息失败"),
    DELETE_CLUSTER_ERROR(120025, "delete cluster error", "删除集群信息失败"),
    CLOSE_TASK_GROUP_ERROR(130011, "close task group error", "关闭任务组错误"),
    CREATE_TASK_GROUP_ERROR(130008, "create task group error", "创建任务组错误"),
    UPDATE_TASK_GROUP_ERROR(130009, "update task group list error", "更新任务组错误"),
    QUERY_TASK_GROUP_LIST_ERROR(130010, "query task group list error", "查询任务组列表错误"),
    START_TASK_GROUP_ERROR(130012, "start task group error", "启动任务组错误"),
    QUERY_TASK_GROUP_QUEUE_LIST_ERROR(130013, "query task group queue list error", "查询任务组队列列表错误"),
    QUERY_ENVIRONMENT_BY_CODE_ERROR(1200009, "not found environment code [{0}] ", "查询环境编码[{0}]不存在"),
    QUERY_ENVIRONMENT_ERROR(1200010, "login user query environment error", "分页查询环境列表错误"),
    VERIFY_ENVIRONMENT_ERROR(1200011, "verify environment error", "验证环境信息错误"),
    GET_RULE_FORM_CREATE_JSON_ERROR(1200012, "get rule form create json error", "获取规则 FROM-CREATE-JSON 错误"),
    QUERY_RULE_LIST_PAGING_ERROR(1200013, "query rule list paging error", "获取规则分页列表错误"),
    QUERY_RULE_LIST_ERROR(1200014, "query rule list error", "获取规则列表错误"),
    QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR(1200016, "query execute result list paging error", "获取数据质量任务结果分页错误"),
    GET_DATASOURCE_OPTIONS_ERROR(1200017, "get datasource options error", "获取数据源Options错误"),
    GET_DATASOURCE_TABLES_ERROR(1200018, "get datasource tables error", "获取数据源表列表错误"),
    GET_DATASOURCE_TABLE_COLUMNS_ERROR(1200019, "get datasource table columns error", "获取数据源表列名错误"),
    QUERY_CLUSTER_BY_CODE_ERROR(1200028, "not found cluster [{0}] ", "查询集群编码[{0}]不存在"),
    QUERY_CLUSTER_ERROR(1200029, "login user query cluster error", "分页查询集群列表错误"),
    VERIFY_CLUSTER_ERROR(1200030, "verify cluster error", "验证集群信息错误"),
    GET_DATASOURCE_DATABASES_ERROR(1200035, "get datasource databases error", "获取数据库列表错误"),
    QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR(1300001, "login user query k8s namespace list paging error",
            "分页查询k8s名称空间列表错误"),
    CREATE_K8S_NAMESPACE_ERROR(1300003, "create k8s namespace error", "创建k8s命名空间错误"),
    VERIFY_K8S_NAMESPACE_ERROR(1300007, "verify k8s and namespace error", "验证k8s命名空间信息错误"),
    DELETE_K8S_NAMESPACE_BY_ID_ERROR(1300008, "delete k8s namespace by id error", "删除命名空间错误"),
    GRANT_K8S_NAMESPACE_ERROR(1300011, "grant namespace error", "授权资源错误"),
    QUERY_UNAUTHORIZED_NAMESPACE_ERROR(1300012, "query unauthorized namespace error", "查询未授权命名空间错误"),
    QUERY_AUTHORIZED_NAMESPACE_ERROR(1300013, "query authorized namespace error", "查询授权命名空间错误"),
    QUERY_CAN_USE_K8S_NAMESPACE_ERROR(1300018, "login user query can used namespace list error", "查询可用命名空间错误"),

    QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR(1300100, "query process definition all variables error",
            "查询工作流自定义变量信息错误"),;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ExceptionStatus(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }

    /**
     * Retrieve Status enum entity by status code.
     */
    public static Optional<Status> findStatusBy(int code) {
        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
