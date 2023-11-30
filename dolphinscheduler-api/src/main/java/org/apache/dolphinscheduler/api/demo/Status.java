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
package org.apache.dolphinscheduler.api.demo;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public interface Status {

    enum BaseStatus implements Status {
        SUCCESS(0, "success", "成功"),

        INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),
        REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
        TASK_TIMEOUT_PARAMS_ERROR(10002, "task timeout parameter is not valid", "任务超时参数无效"),
        TASK_INSTANCE_NOT_FOUND(10008, "task instance not found", "任务实例不存在"),
        ALERT_GROUP_EXIST(10012, "alarm group already exists", "告警组名称已存在"),
        DATASOURCE_EXIST(10015, "data source name already exists", "数据源名称已存在"),
        DATASOURCE_CONNECT_FAILED(10016, "data source connection failed", "建立数据源连接失败"),
        TENANT_NOT_EXIST(10017, "tenant [{0}] not exists", "租户[{0}]不存在"),
        PROJECT_NOT_FOUND(10018, "project {0} not found ", "项目[{0}]不存在"),
        PROJECT_ALREADY_EXISTS(10019, "project {0} already exists", "项目名称[{0}]已存在"),
        MASTER_NOT_EXISTS(10025, "master does not exist", "无可用master节点"),
        QUERY_DATASOURCE_ERROR(10035, "query datasource error", "查询数据源错误"),
        CONNECTION_TEST_FAILURE(10037, "connection test failure", "测试数据源连接失败"),
        UPDATE_PROJECT_ERROR(10046, "update project error", "更新项目信息错误"),
        CREATE_PROJECT_ERROR(10048, "create project error", "创建项目错误"),
        DELETE_PROJECT_ERROR(10050, "delete project error", "删除项目错误"),
        CREATE_PROCESS_DEFINITION_ERROR(10105, "create process definition error", "创建工作流错误"),
        UPDATE_PROCESS_DEFINITION_ERROR(10107, "update process definition error", "更新工作流定义错误"),
        SAVE_ERROR(10136, "save error", "保存错误"),
        DELETE_PROJECT_ERROR_DEFINES_NOT_NULL(10137, "please delete the process definitions in project first!",
                "请先删除全部工作流定义"),
        COPY_PROCESS_DEFINITION_ERROR(10149, "copy process definition from {0} to {1} error : {2}",
                "从{0}复制工作流到{1}错误 : {2}"),
        MOVE_PROCESS_DEFINITION_ERROR(10150, "move process definition from {0} to {1} error : {2}",
                "从{0}移动工作流到{1}错误 : {2}"),
        SWITCH_PROCESS_DEFINITION_VERSION_ERROR(10151, "Switch process definition version error", "切换工作流版本出错"),
        SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR(10152,
                "Switch process definition version error: not exists process definition, [process definition id {0}]",
                "切换工作流版本出错：工作流不存在，[工作流id {0}]"),
        SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR(10153,
                "Switch process definition version error: not exists process definition version, [process definition id {0}] [version number {1}]",
                "切换工作流版本出错：工作流版本信息不存在，[工作流id {0}] [版本号 {1}]"),
        PROCESS_DEFINITION_CODES_IS_EMPTY(10158, "process definition codes is empty", "工作流CODES不能为空"),
        DELETE_PROCESS_DEFINITION_EXECUTING_FAIL(10163,
                "delete process definition by code fail, for there are {0} process instances in executing using it",
                "删除工作流定义失败，有[{0}]个运行中的工作流实例正在使用"),
        PROCESS_DEFINITION_NAME_EXIST(10168, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),
        DATASOURCE_NAME_ILLEGAL(10172, "datasource name illegal", "数据源名称不合法"),
        CREATE_SCHEDULE_ERROR(10076, "create schedule error", "创建调度配置错误"),
        PROJECT_NOT_EXIST(10190, "This project was not found. Please refresh page.", "该项目不存在,请刷新页面"),
        TASK_INSTANCE_HOST_IS_NULL(10191, "task instance host is null", "任务实例host为空"),
        DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL(10193, "delete process definition fail, cause used by other tasks: {0}",
                "删除工作流定时失败，被其他任务引用：{0}"),

        CREATE_PROCESS_DEFINITION_LOG_ERROR(10201, "Create process definition log error", "创建 process definition log 对象失败"),
        EXECUTE_NOT_DEFINE_TASK(10206, "please save and try again",
                "请先保存后再执行"),
        START_NODE_NOT_EXIST_IN_LAST_PROCESS(10207, "this node {0} does not exist in the latest process definition",
                "该节点 {0} 不存在于最新的流程定义中"),

        RESOURCE_NOT_EXIST(20004, "resource not exist", "资源不存在"),
        RESOURCE_IS_USED(20014, "resource file is used by process definition", "资源文件被上线的流程定义使用了"),

        PLUGIN_INSTANCE_ALREADY_EXISTS(110010, "plugin instance already exists", "该告警插件实例已存在"),
        DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED(110012,
                "failed to delete the alert instance, there is an alarm group associated with this alert instance",
                "删除告警实例失败，存在与此告警实例关联的警报组"),
        ALERT_TEST_SENDING_FAILED(110014, "Alert test sending failed, [{0}]", "alert测试发送失败，[{0}]"),
        ALERT_SERVER_NOT_EXIST(110017, "Alert server does not exist", "Alert server不存在"),

        CREATE_ENVIRONMENT_ERROR(120001, "create environment error", "创建环境失败"),
        ENVIRONMENT_NAME_EXISTS(120002, "this environment name [{0}] already exists", "环境名称[{0}]已经存在"),
        ENVIRONMENT_NAME_IS_NULL(120003, "this environment name shouldn't be empty.", "环境名称不能为空"),
        ENVIRONMENT_CONFIG_IS_NULL(120004, "this environment config shouldn't be empty.", "环境配置信息不能为空"),
        UPDATE_ENVIRONMENT_ERROR(120005, "update environment [{0}] info error", "更新环境[{0}]信息失败"),
        DELETE_ENVIRONMENT_ERROR(120006, "delete environment error", "删除环境信息失败"),
        DELETE_ENVIRONMENT_RELATED_TASK_EXISTS(120007, "this environment has been used in tasks,so you can't delete it.",
                "该环境已经被任务使用，所以不能删除该环境信息"),
        QUERY_RULE_INPUT_ENTRY_LIST_ERROR(1200015, "query rule list error", "获取规则列表错误"),
        GET_DATASOURCE_TABLES_ERROR(1200018, "get datasource tables error", "获取数据源表列表错误"),

        FUNCTION_DISABLED(1400002, "The current feature is disabled.", "当前功能已被禁用"),
        DESCRIPTION_TOO_LONG_ERROR(1400004, "description is too long error", "描述过长");
        private final int code;
        private final String enMsg;
        private final String zhMsg;

        BaseStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum K8sStatus implements Status {

        K8S_NAMESPACE_EXIST(1300002, "k8s namespace {0} already exists", "k8s命名空间[{0}]已存在"),
        K8S_NAMESPACE_NOT_EXIST(1300005, "k8s namespace {0} not exists", "命名空间ID[{0}]不存在"),
        K8S_CLIENT_OPS_ERROR(1300006, "k8s error with exception {0}", "k8s操作报错[{0}]");


        private final int code;
        private final String enMsg;
        private final String zhMsg;

        K8sStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum ClusterStatus implements Status {
        CREATE_CLUSTER_ERROR(120020, "create cluster error", "创建集群失败"),
        CLUSTER_NAME_EXISTS(120021, "this cluster name [{0}] already exists", "集群名称[{0}]已经存在"),
        CLUSTER_NAME_IS_NULL(120022, "this cluster name shouldn't be empty.", "集群名称不能为空"),
        CLUSTER_CONFIG_IS_NULL(120023, "this cluster config shouldn't be empty.", "集群配置信息不能为空"),
        DELETE_CLUSTER_ERROR(120025, "delete cluster error", "删除集群信息失败"),
        CLUSTER_NOT_EXISTS(120033, "this cluster can not found in db.", "集群配置数据库里查询不到为空"),
        DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS(120034, "this cluster has been used in namespace,so you can't delete it.",
                "该集群已经被命名空间使用，所以不能删除该集群信息"),

        QUERY_ENVIRONMENT_BY_NAME_ERROR(1200008, "not found environment name [{0}] ", "查询环境名称[{0}]不存在"),
        QUERY_ENVIRONMENT_BY_CODE_ERROR(1200009, "not found environment code [{0}] ", "查询环境编码[{0}]不存在"),
        QUERY_CLUSTER_BY_NAME_ERROR(1200027, "not found cluster [{0}] ", "查询集群名称[{0}]信息不存在"),
        QUERY_CLUSTER_BY_CODE_ERROR(1200028, "not found cluster [{0}] ", "查询集群编码[{0}]不存在"),
        ;


        private final int code;
        private final String enMsg;
        private final String zhMsg;

        ClusterStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum UserStatus implements Status {

        USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
        USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
        ALERT_GROUP_NOT_EXIST(10011, "alarm group not found", "告警组不存在"),

        CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
        UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
        DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
        TRANSFORM_PROJECT_OWNERSHIP(10179, "Please transform project ownership [{0}]", "请先转移项目所有权[{0}]"),

        USER_NO_OPERATION_PERM(30001, "user has no operation privilege", "当前用户没有操作权限"),
        USER_NO_OPERATION_PROJECT_PERM(30002, "user {0} is not has project {1} permission", "当前用户[{0}]没有[{1}]项目的操作权限"),
        USER_NO_WRITE_PROJECT_PERM(30003, "user [{0}] does not have write permission for project [{1}]",
                "当前用户[{0}]没有[{1}]项目的写权限"),
        ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist, tokenId {0}", "访问token不存在, {0}"),
        CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
        NOT_ALLOW_TO_DISABLE_OWN_ACCOUNT(130020, "Not allow to disable your own account", "不能停用自己的账号"),
        TIME_ZONE_ILLEGAL(130031, "time zone [{0}] is illegal", "时区参数 [{0}] 不合法"),

        USER_PASSWORD_LENGTH_ERROR(1300017, "user's password length error", "用户密码长度错误"),
        NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP(130030, "Not allow to delete the default alarm group ", "不能删除默认告警组"),
        NOT_ALLOW_TO_UPDATE_GLOBAL_ALARM_GROUP(130032, "Not allow to update the global alert group ", "不能更新全局告警组"),
        ;
        private final int code;
        private final String enMsg;
        private final String zhMsg;

        UserStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum WorkFlowStatus implements Status {

        PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
        PROCESS_DEFINE_NOT_EXIST(50003, "process definition {0} does not exist", "工作流定义[{0}]不存在"),
        PROCESS_DEFINE_NOT_RELEASE(50004, "process definition {0} process version {1} not online",
                "工作流定义[{0}] 工作流版本[{1}]不是上线状态"),
        SUB_PROCESS_DEFINE_NOT_RELEASE(50004, "exist sub process definition not online", "存在子工作流定义不是上线状态"),
        PROCESS_INSTANCE_STATE_OPERATION_ERROR(50006,
                "the status of process instance {0} is {1},Cannot perform {2} operation",
                "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
        PROCESS_DEFINE_NOT_ALLOWED_EDIT(50008, "process definition {0} does not allow edit", "工作流定义[{0}]不允许修改"),
        PROCESS_INSTANCE_EXECUTING_COMMAND(50009, "process instance {0} is executing the command, please wait ...",
                "工作流实例[{0}]正在执行命令，请稍等..."),
        START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),

        DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
        DATA_IS_NULL(50018, "data {0} is null", "数据[{0}]不能为空"),
        PROCESS_NODE_HAS_CYCLE(50019, "process node has cycle", "流程节点间存在循环依赖"),
        SCHEDULE_STATE_ONLINE(50023, "the status of schedule {0} is already online", "调度配置[{0}]已上线"),
        DELETE_SCHEDULE_BY_ID_ERROR(50024, "delete schedule by id error", "删除调度配置错误"),
        START_TASK_INSTANCE_ERROR(50059, "start task instance error", "运行任务流实例错误"),
        EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
        PROCESS_NODE_S_PARAMETER_INVALID(50020, "process node {0} parameter invalid", "流程节点[{0}]参数无效"),
        DELETE_PROCESS_DEFINE_BY_CODE_ERROR(50022, "delete process definition by code error", "删除工作流定义错误"),
        PROCESS_DEFINE_STATE_ONLINE(50021, "process definition [{0}] is already online", "工作流定义[{0}]已上线"),
        BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR(50026, "batch delete process definition by codes error: {0}",
                "批量删除工作流定义错误: {0}"),
        IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),
        TASK_DEFINE_NOT_EXIST(50030, "task definition [{0}] does not exist", "任务定义[{0}]不存在"),
        CREATE_PROCESS_TASK_RELATION_ERROR(50032, "create process task relation error", "创建工作流任务关系错误"),
        PROCESS_DAG_IS_EMPTY(50035, "process dag is empty", "工作流dag是空"),
        CHECK_PROCESS_TASK_RELATION_ERROR(50036, "check process task relation error", "工作流任务关系参数错误"),
        CREATE_TASK_DEFINITION_ERROR(50037, "create task definition error", "创建任务错误"),
        UPDATE_TASK_DEFINITION_ERROR(50038, "update task definition error", "更新任务定义错误"),
        MAIN_TABLE_USING_VERSION(50053, "the version that the master table is using", "主表正在使用该版本"),
        DELETE_PROCESS_DEFINE_ERROR(50060, "delete process definition [{0}] error: {1}", "删除工作流定义[{0}]错误: {1}"),
        WORKFLOW_INSTANCE_IS_NOT_FINISHED(50071, "the workflow instance is not finished, can not do this operation",
                "工作流实例未结束，不能执行此操作"),
        TASK_PARALLELISM_PARAMS_ERROR(50080, "task parallelism parameter is not valid", "任务并行度参数无效"),
        TASK_COMPLEMENT_DATA_DATE_ERROR(50081, "The range of date for complementing date is not valid", "补数选择的日期范围无效");

        private final int code;
        private final String enMsg;
        private final String zhMsg;

        WorkFlowStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum TaskStatus implements Status {

        ENVIRONMENT_WORKER_GROUPS_IS_INVALID(130015, "environment worker groups is invalid format", "环境关联的工作组参数解析错误"),
        UPDATE_ENVIRONMENT_WORKER_GROUP_RELATION_ERROR(130016,
                "You can't modify the worker group, because the worker group [{0}] and this environment [{1}] already be used in the task [{2}]",
                "您不能修改工作组选项，因为该工作组 [{0}] 和 该环境 [{1}] 已经被用在任务 [{2}] 中"),
        TASK_GROUP_QUEUE_ALREADY_START(130017, "task group queue already start", "节点已经获取任务组资源");

        private final int code;
        private final String enMsg;
        private final String zhMsg;

        TaskStatus(int code, String enMsg, String zhMsg) {
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
    }

    enum ScheduleStatus implements Status {
        SCHEDULE_TIME_NUMBER_EXCEED(1400003, "The number of complement dates exceed 100.", "补数日期个数超过100");

        private final int code;
        private final String enMsg;
        private final String zhMsg;

        ScheduleStatus(int code, String enMsg, String zhMsg) {
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
    }

    int getCode();

    String getMsg();
}
