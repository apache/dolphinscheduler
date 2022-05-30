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

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public enum FuncPermissionEnum {

    HOME("首页", "Home", "首页", "Home", "home:view", 1),
    PROJECT("项目管理", "Project", "项目管理", "Project", "project:view", 1),
    CREATE_PROJECT("项目管理", "Project", "创建项目", "Create Project", "project:create", 4),
    EDIT_PROJECT("项目管理", "Project", "编辑项目", "Edit Project", "project:edit", 4),
    DELETE_PROJECT("项目管理", "Project", "删除项目", "Delete Project", "project:delete", 4),
    WORKFLOW_DEFINITION("项目管理", "Project", "工作流定义", "Workflow Definition", "project:definition:list", 4),
    CREATE_WORKFLOW("项目管理", "Project", "创建工作流", "Create Workflow", "project:definition:create", 3),
    IMPORT_WORKFLOW("项目管理", "Project", "导入工作流", "Import Workflow", "project:definition:import", 4),
    EDIT("项目管理", "Project", "编辑工作流", "Edit", "project:definition:update", 3),
    START("项目管理", "Project", "运行工作流", "Start", "project:executors:start", 4),
    TIMING("项目管理", "Project", "设置定时", "Timing", "project:schedules:timing", 4),
    ONLINE_OFFLINE("项目管理", "Project", "工作流上下线", "Online&Offline", "project:definition:release", 4),
    COPY_WORKFLOW("项目管理", "Project", "复制工作流", "Copy Workflow", "project:definition:copy", 4),
    CRON_MANAGE("项目管理", "Project", "定时管理", "Cron manage", "project:schedules:corn", 3),
    DELETE("项目管理", "Project", "删除工作流", "Delete", "project:definition:delete", 4),
    TREE_VIEW("项目管理", "Project", "树形图", "Tree View", "project:definition:view-tree", 3),
    EXPORT("项目管理", "Project", "导出工作流", "Export", "project:definition:export", 4),
    BATCH_COPY("项目管理", "Project", "批量复制", "Batch Copy", "project:definition:batch-copy", 4),
    DEFINITION_EXPORT("项目管理", "Project", "批量导出", "Export", "project:definition:batch-export", 4),
    DEFINITION_BATCH_DELETE("项目管理", "Project", "批量删除", "Delete", "project:definition:batch-delete", 4),
    SWITCH_TO_THIS_VERSION("项目管理", "Project", "切换到该版本", "Switch To This Version", "project:definition:version:switch", 4),
    DEFINITION_DELETE("项目管理", "Project", "删除工作流", "Delete", "project:definition:version:delete", 4),
    SAVE("项目管理", "Project", "保存", "save", "project:definition:verify-name", 4),
    WORKFLOW_INSTANCE("项目管理", "Project", "工作流实例", "Workflow Instance", "project:process-instance:list", 4),
    RERUN("项目管理", "Project", "重跑工作流实例", "Rerun", "project:executors:execute", 4),
    FAILED_TO_RETRY("项目管理", "Project", "重跑失败的任务", "Failed to retry", "project:executors:retry", 4),
    STOP("项目管理", "Project", "停止工作流实例", "stop", "project:executors:stop", 4),
    RECOVERY_SUSPEND("项目管理", "Project", "恢复运行", "Recovery Suspend", "project:executors:recover", 4),
    PAUSE("项目管理", "Project", "暂停工作流实例", "Pause", "project:executors:pause", 4),
    INSTANCE_DELETE("项目管理", "Project", "删除工作流实例", "Delete", "project:process-instance:delete", 4),
    GANTT("项目管理", "Project", "甘特图", "Gantt", "project:process-instance:view-gantt", 3),
    INSTANCE_BATCH_DELETE("项目管理", "Project", "批量删除", "Delete", "project:process-instance:batch-delete", 4),
    FORCED_SUCCESS("项目管理", "Project", "强制成功", "Forced Success", "project:task-instance:force-success", 4),
    VIEW_LOG("项目管理", "Project", "查看日志", "View Log", "project:log:detail", 4),
    DOWNLOAD_LOG("项目管理", "Project", "下载日志", "Download Log", "project:log:download-log", 4),
    PROJECT_OVERVIEW("项目管理", "Project", "项目概览", "Project Overview", "project:overview:view", 3),
    WORKFLOW_RELATION("项目管理", "Project", "工作流关系", "Workflow Relation", "project:lineages:view", 2),
    WORKFLOW_DEFINITION_VIEW("项目管理", "Project", "工作流定义", "Workflow Definition", "project:definition:view", 2),
    WORKFLOW_INSTANCE_VIEW("项目管理", "Project", "工作流实例", "Workflow Instance", "project:process-instance:view", 2),
    TASK_INSTANCE("项目管理", "Project", "任务实例", "Task Instance", "project:task-instance:view", 2),
    UPDATE("项目管理", "Project", "编辑工作流实例", "Update", "project:process-instance:update", 3),
    VERSION_LIST("项目管理", "Project", "查看版本信息", "Version List", "project:version:list", 4),
    TASK_DEFINITION("项目管理", "Project", "任务定义", "Task Definition", "project:task-definition:view", 2),
    CREATE_TASK_DEFINITION("项目管理", "Project", "创建任务", "Create Task Definition", "project:task-definition:create", 4),
    EDIT_TASK_DEFINITION("项目管理", "Project", "编辑任务", "Edit Task Definition", "project:task-definition:edit", 4),
    MOVE_TASK_DEFINITION("项目管理", "Project", "移动任务", "Move Task Definition", "project:task-definition:move", 4),
    TASK_VERSION_VIEW("项目管理", "Project", "查看任务版本", "Task Version View", "project:task-definition:version", 4),
    DELETE_TASK_DEFINITION("项目管理", "Project", "删除任务", "Delete Task Definition", "project:task-definition:delete", 4),
    RESOURCES("资源中心", "Resources", "资源中心", "Resources", "resources:view", 1),
    FOLDER_CREATE("资源中心", "Resources", "创建文件夹", "Create Folder", "resources:file:create", 4),
    FILE_CREATE("资源中心", "Resources", "创建文件", "Create File", "resources:file:online-create", 3),
    FILE_UPLOAD("资源中心", "Resources", "上传文件", "Upload Files", "resources:file:update", 4),
    FILE_EDIT("资源中心", "Resources", "编辑", "Edit", "resources:file:update-content", 3),
    FILE_RENAME("资源中心", "Resources", "重命名", "Rename", "resources:file:rename", 4),
    FILE_DOWNLOAD("资源中心", "Resources", "下载", "Download", "resources:file:download", 4),
    FILE_DELETE("资源中心", "Resources", "删除", "Delete", "resources:file:delete", 4),
    UDF_CREATE_FOLDER("资源中心", "Resources", "创建文件夹", "Create Folder", "resources:udf:create", 4),
    UPLOAD_UDF_RESOURCES("资源中心", "Resources", "上传UDF资源", "Upload UDF Resources", "resources:udf:upload", 4),
    UDF_EDIT("资源中心", "Resources", "编辑", "Edit", "resources:udf:edit", 4),
    UDF_DOWNLOAD("资源中心", "Resources", "下载", "Download", "resources:udf:download", 4),
    UDF_DELETE("资源中心", "Resources", "删除", "Delete", "resources:udf:delete", 4),
    CREATE_UDF_FUNCTION("资源中心", "Resources", "创建UDF函数", "Create UDF Function", "resources:udf-func:create", 4),
    UDF_FUNC_EDIT("资源中心", "Resources", "编辑", "Edit", "resources:udf-func:update", 4),
    UDF_FUNC_DELETE("资源中心", "Resources", "删除", "Delete", "resources:udf-func:delete", 4),
    TASK_GROUP_CREATE("资源中心", "Resources", "创建任务组", "Create task group", "resources:task-group:create", 4),
    TASK_GROUP_CLOSE("资源中心", "Resources", "切换任务组状态", "Switch status", "resources:task-group:close", 4),
    TASK_GROUP_EDIT("资源中心", "Resources", "编辑任务组", "Edit task group", "resources:task-group:update", 4),
    TASK_GROUP_VIEW_QUEUE("资源中心", "Resources", "查看任务组队列", "View the queue of the task group", "resources:task-group:queue-view", 4),
    TASK_GROUP_QUEUE_PRIORITY("资源中心", "Resources", "修改优先级", "Edit the priority", "resources:task-group-queue:priority", 4),
    TASK_GROUP_QUEUE_START("资源中心", "Resources", "强制启动", "start the task", "resources:task-group-queue:start", 4),
    FILE_VIEW("资源中心", "Resources", "文件管理", "File Manage", "resources:file:view", 2),
    UDF_FILE_VIEW("资源中心", "Resources", "资源管理", "Resource Manage", "resources:udf:view", 2),
    UDF_FUNCTION_VIEW("资源中心", "Resources", "函数管理", "Function Manage", "resources:udf-func:view", 2),
    TASK_GROUP_OPTION_VIEW("资源中心", "Resources", "任务组配置", "Task Group Option", "resources:task-group:view", 3),
    TASK_GROUP_QUEUE("资源中心", "Resources", "任务组队列", "Task Group Queue", "resources:task-group-queue:view", 3),
    MONITOR("监控中心", "Monitor", "监控中心", "Monitor", "monitor:view", 1),
    MONITOR_MASTER("监控中心", "Monitor", "Master", "Master", "monitor:masters:view", 2),
    MONITOR_WORKER("监控中心", "Monitor", "Worker", "Worker", "monitor:workers:view", 2),
    MONITOR_DB("监控中心", "Monitor", "DB", "DB", "monitor:databases:view", 2),
    MONITOR_STATISTICS("监控中心", "Monitor", "Statistics", "Statistics", "monitor:statistics:view", 2),
    MONITOR_EVENT_LIST("监控中心", "Monitor", "事件列表", "Event List", "monitor:event:view", 2),
    DATASOURCE("数据源中心", "Datasource", "数据源中心", "Datasource", "datasource:view", 1),
    DATASOURCE_CREATE_DATASOURCE("数据源中心", "Datasource", "创建数据源", "Create DataSource", "datasource:create", 4),
    DATASOURCE_EDIT("数据源中心", "Datasource", "编辑", "Edit", "datasource:update", 4),
    DATASOURCE_DELETE("数据源中心", "Datasource", "删除", "Delete", "datasource:delete", 4),
    DATASOURCE_LIST("数据源中心", "Datasource", "数据源中心", "Datasource", "datasource:list", 2),
    DATASOURCE_PARAM_VIEW("数据源中心", "Datasource", "查看数据源参数", "DataSource Param View", "datasource:param-view", 4),
    SECURITY("安全中心", "Security", "安全中心", "Security", "security:view", 1),
    CREATE_TENANT("安全中心", "Security", "创建租户", "Create Tenant", "security:tenant:create", 4),
    TENANT_EDIT("安全中心", "Security", "编辑", "Edit", "security:tenant:update", 4),
    TENANT_DELETE("安全中心", "Security", "删除", "Delete", "security:tenant:delete", 4),
    CREATE_ALARM_GROUP("安全中心", "Security", "创建告警组", "Create Alarm Group", "security:alert-group:create", 4),
    ALERT_GROUP_EDIT("安全中心", "Security", "编辑", "Edit", "security:alert-group:update", 4),
    ALERT_GROUP_DELETE("安全中心", "Security", "删除", "Delete", "security:alert-group:delete", 4),
    CREATE_ALARM_INSTANCE("安全中心", "Security", "创建告警实例", "Create Alarm Instance", "security:alert-plugin:create", 4),
    ALERT_PLUGIN_EDIT("安全中心", "Security", "编辑", "Edit", "security:alert-plugin:update", 4),
    ALERT_PLUGIN_DELETE("安全中心", "Security", "删除", "Delete", "security:alert-plugin:delete", 4),
    CREATE_WORKER_GROUP("安全中心", "Security", "创建Worker分组", "Create Worker Group", "security:worker-group:create", 4),
    WORKER_GROUP_EDIT("安全中心", "Security", "编辑", "Edit", "security:worker-group:update", 4),
    WORKER_GROUP_DELETE("安全中心", "Security", "删除", "Delete", "security:worker-group:delete", 4),
    CREATE_YARN_QUEUE("安全中心", "Security", "创建队列", "Create Yarn Queue", "security:queue:create", 4),
    QUEUE_EDIT("安全中心", "Security", "编辑队列", "Edit", "security:queue:update", 4),
    CREATE_ENVIRONMENT("安全中心", "Security", "创建环境", "Create Environment", "security:environment:create", 4),
    ENVIRONMENT_EDIT("安全中心", "Security", "编辑环境", "Edit", "security:environment:update", 4),
    ENVIRONMENT_DELETE("安全中心", "Security", "删除环境", "Delete", "security:environment:delete", 4),
    TOKEN_EDIT("安全中心", "Security", "编辑令牌", "Edit", "security:token:update", 4),
    TOKEN_DELETE("安全中心", "Security", "删除令牌", "Delete", "security:token:delete", 4),
    CREATE_TOKEN("安全中心", "Security", "创建令牌", "Create oken", "security:token:create", 4),
    CREATE_CALENDAR("安全中心", "Security", "创建日历", "Create Calendar", "security:calendar:create", 4),
    CALENDAR_EDIT("安全中心", "Security", "编辑日历", "Edit", "security:calendar:update", 4),
    CALENDAR_DELETE("安全中心", "Security", "删除日历", "Delete", "security:calendar:delete", 4),
    CREATE_CARD("安全中心", "Security", "创建牌", "Create Card", "security:cards:create", 4),
    CARDS_EDIT("安全中心", "Security", "编辑牌", "Edit", "security:cards:update", 4),
    EDIT_VALUE("安全中心", "Security", "编辑牌值", "Edit Value", "security:cards:value", 4),
    CARDS_DELETE("安全中心", "Security", "删除牌", "Delete", "security:cards:delete", 4),
    ALARM_GROUP_MANAGE("安全中心", "Security", "告警组管理", "Alarm Group Manage", "security:alert-group:view", 2),
    ALARM_INSTANCE_MANAGE("安全中心", "Security", "告警实例管理", "Alarm Instance Manage", "security:alert-plugin:view", 2),
    WORKER_GROUP_MANAGE("安全中心", "Security", "Worker分组管理", "Worker Group Manage", "security:worker-group:view", 2),
    TOKEN_MANAGE("安全中心", "Security", "令牌管理", "Token Manage", "security:token:view", 2),
    CALENDAR_MANAGE("安全中心", "Security", "日历管理", "Calendar Manage", "security:calendar:view", 2),
    CARD_MANAGER("安全中心", "Security", "牌管理", "Card Manager", "security:cards:view", 2),
    TENANT_MANAGER("安全中心", "Security", "租户管理", "Tenant Manager", "security:tenant:view", 2),
    YARN_QUEUE_MANAGE("安全中心", "Security", "Yarn队列管理", "Yarn Queue Manage", "security:queue:view", 2),
    USER_MANAGER("安全中心", "Security", "用户管理", "User Manager", "security:user:view", 2),
    ALL_ROLES("安全中心", "Security", "所有角色", "All Roles", "security:user:roles", 4),
    CREATE_USER("安全中心", "Security", "创建用户", "Create User", "security:user:create", 4),
    IMPORT_USERS("安全中心", "Security", "导入用户", "Import Users", "security:user:import", 4),
    DOWNLOAD_TEMPLATE("安全中心", "Security", "下载模版", "Download Template", "security:user:template", 4),
    USER_EDIT("安全中心", "Security", "编辑", "Edit", "security:user:update", 4),
    ASSOCIATED_ROLE("安全中心", "Security", "关联角色", "Associated Role", "security:user:role", 4),
    VIEW_PERMISSIONS("安全中心", "Security", "查看权限", "View Permissions", "security:user:permission", 2),
    RESET_PASSWORD("安全中心", "Security", "重置密码", "Reset Password", "security:user:reset-pwd", 4),
    USER_DELETE("安全中心", "Security", "删除", "Delete", "security:user:delete", 4),
    BATCH_DELETION("安全中心", "Security", "批量删除", "Batch Deletion", "security:user:batch-delete", 4),
    ROLE_MANAGER("安全中心", "Security", "角色管理", "Role Manager", "security:role:view", 2),
    ROLE_CREATE("安全中心", "Security", "创建角色", "Role Create", "security:role:create", 4),
    VIEW_PERMISSION("安全中心", "Security", "查看权限", "View Permission", "security:role:permission-view", 3),
    ROLE_RENAME("安全中心", "Security", "重命名", "Rename", "security:role:rename", 4),
    ASSIGN_PERMISSION("安全中心", "Security", "分配权限", "Assign Permission", "security:role:permission-assign", 3),
    ROLE_DELETE("安全中心", "Security", "删除", "Delete", "security:role:delete", 4),
    ENVIRONMENT_MANAGER("安全中心", "Security", "环境管理", "Environment Manager", "security:environment:view", 2),
    ALARM_LIST("安全中心", "Monitor", "告警列表", "Alarm List", "monitor:alert:view", 2),
            ;

    FuncPermissionEnum(String zhModule, String enModule, String zhName, String enName, String key, Integer type) {
        this.zhModule = zhModule;
        this.enModule = enModule;
        this.zhName = zhName;
        this.enName = enName;
        this.key = key;
        this.type = type;
    }

    private final String zhModule;

    private final String enModule;

    private final String zhName;

    private final String enName;

    private final String key;

    private final Integer type;

    public String getKey() {
        return key;
    }

    public String getName() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhName;
        } else {
            return this.enName;
        }
    }
}
