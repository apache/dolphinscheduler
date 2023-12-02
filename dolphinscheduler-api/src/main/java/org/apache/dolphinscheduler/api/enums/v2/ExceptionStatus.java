package org.apache.dolphinscheduler.api.enums.v2;

import org.apache.dolphinscheduler.api.enums.Status;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

public enum ExceptionStatus {
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
    QUERY_ALERT_GROUP_ERROR(10180, "query alert group error", "查询告警组错误"),
    CURRENT_LOGIN_USER_TENANT_NOT_EXIST(10181, "the tenant of the currently login user is not specified",
            "未指定当前登录用户的租户"),
    REVOKE_PROJECT_ERROR(10182, "revoke project error", "撤销项目授权错误"),
    QUERY_AUTHORIZED_USER(10183, "query authorized user error", "查询拥有项目权限的用户错误"),
    QUERY_EXECUTING_WORKFLOW_ERROR(10192, "query executing workflow error", "查询运行的工作流实例错误"),
    LIST_TASK_TYPE_ERROR(10200, "list task type error", "查询任务类型列表错误"),
    ADD_TASK_TYPE_ERROR(10200, "add task type error", "添加任务类型错误"),
    DELETE_TASK_TYPE_ERROR(10200, "delete task type error", "删除任务类型错误"),
    LIST_AZURE_DATA_FACTORY_ERROR(10208, "list azure data factory error", "查询AZURE数据工厂列表错误"),
    LIST_AZURE_RESOURCE_GROUP_ERROR(10209, "list azure resource group error", "查询AZURE资源组列表错误"),
    LIST_AZURE_DATA_FACTORY_PIPELINE_ERROR(10210, "list azure data factory pipeline error", "查询AZURE数据工厂pipeline列表错误"),
    NOT_SUPPORT_SSO(10211, "Not support SSO login.", "不支持SSO登录"),
    CREATE_PROJECT_PARAMETER_ERROR(10214, "create project parameter error", "创建项目参数错误"),

    UPDATE_PROJECT_PARAMETER_ERROR(10215, "update project parameter error", "更新项目参数错误"),

    DELETE_PROJECT_PARAMETER_ERROR(10216, "delete project parameter error {0}", "删除项目参数错误 {0}"),

    QUERY_PROJECT_PARAMETER_ERROR(10217, "query project parameter error", "查询项目参数错误"),
    TASK_INSTANCE_STATE_COUNT_ERROR(50011, "task instance state count error", "查询各状态任务实例数错误"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(50012, "count process instance state error", "查询各状态流程实例数错误"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(50013, "count process definition user error", "查询各用户流程定义数错误"),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    CHECK_PROCESS_DEFINITION_ERROR(50016, "check process definition error", "工作流定义错误"),
    DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
    DELETE_PROCESS_DEFINE_BY_CODE_ERROR(50022, "delete process definition by code error", "删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR(50026, "batch delete process definition by codes error: {0}",
            "批量删除工作流定义错误: {0}"),

    IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),
    CREATE_PROCESS_TASK_RELATION_ERROR(50032, "create process task relation error", "创建工作流任务关系错误"),
    RELEASE_TASK_DEFINITION_ERROR(50046, "release task definition error", "上线任务错误"),
    MOVE_PROCESS_TASK_RELATION_ERROR(50047, "move process task relation error", "移动任务到其他工作流错误"),
    DELETE_TASK_PROCESS_RELATION_ERROR(50048, "delete process task relation error", "删除工作流任务关系错误"),
    QUERY_TASK_PROCESS_RELATION_ERROR(50049, "query process task relation error", "查询工作流任务关系错误"),
    DELETE_EDGE_ERROR(50055, "delete edge error", "删除工作流任务连接线错误"),
    BATCH_EXECUTE_PROCESS_INSTANCE_ERROR(50058, "change process instance status error: {0}", "修改工作实例状态错误: {0}"),
    QUERY_DATABASE_STATE_ERROR(70001, "query database state error", "查询数据库状态错误"),
    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    QUERY_ACCESSTOKEN_BY_USER_ERROR(70016, "query access token by user error", "查询访问指定用户的token错误"),
    COMMAND_STATE_COUNT_ERROR(80001, "task instance state count error", "查询各状态任务实例数错误"),
    QUEUE_COUNT_ERROR(90001, "queue count error", "查询队列数据错误"),
    KERBEROS_STARTUP_STATE(100001, "get kerberos startup state error", "获取kerberos启动状态错误"),
    UNAUTHORIZED_DATASOURCE(10040, "unauthorized datasource", "未经授权的数据源"),
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
    QUERY_UNAUTHORIZED_NAMESPACE_ERROR(1300012, "query unauthorized namespace error", "查询未授权命名空间错误"),
    QUERY_AUTHORIZED_NAMESPACE_ERROR(1300013, "query authorized namespace error", "查询授权命名空间错误"),
    QUERY_CAN_USE_K8S_NAMESPACE_ERROR(1300018, "login user query can used namespace list error", "查询可用命名空间错误"),
    QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR(1300100, "query process definition all variables error",
            "查询工作流自定义变量信息错误"),;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ExceptionStatus (int code, String enMsg, String zhMsg) {
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
