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

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

public enum ProcessStatus implements Status {
    CREATE_PROCESS_DEFINITION_ERROR(10105, "create process definition error", "创建工作流错误"),
    UPDATE_PROCESS_DEFINITION_ERROR(10107, "update process definition error", "更新工作流定义错误"),
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
    QUERY_PROCESS_DEFINITION_VERSIONS_ERROR(10154, "query process definition versions error", "查询工作流历史版本信息出错"),
    DELETE_PROCESS_DEFINITION_VERSION_ERROR(10156, "delete process definition version error", "删除工作流历史版本出错"),
    PROCESS_DEFINITION_CODES_IS_EMPTY(10158, "process definition codes is empty", "工作流CODES不能为空"),
    BATCH_COPY_PROCESS_DEFINITION_ERROR(10159, "batch copy process definition error", "复制工作流错误"),
    DELETE_PROCESS_DEFINITION_EXECUTING_FAIL(10163,
            "delete process definition by code fail, for there are {0} process instances in executing using it",
            "删除工作流定义失败，有[{0}]个运行中的工作流实例正在使用"),
    PROCESS_DEFINITION_NAME_EXIST(10168, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),
    DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL(10193, "delete process definition fail, cause used by other tasks: {0}",
            "删除工作流定时失败，被其他任务引用：{0}"),
    CREATE_PROCESS_DEFINITION_LOG_ERROR(10201, "Create process definition log error", "创建 process definition log 对象失败"),
    START_NODE_NOT_EXIST_IN_LAST_PROCESS(10207, "this node {0} does not exist in the latest process definition",
            "该节点 {0} 不存在于最新的流程定义中"),
    PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
    PROCESS_DEFINE_NOT_EXIST(50003, "process definition {0} does not exist", "工作流定义[{0}]不存在"),
    PROCESS_DEFINE_NOT_RELEASE(50004, "process definition {0} process version {1} not online",
            "工作流定义[{0}] 工作流版本[{1}]不是上线状态"),
    SUB_PROCESS_DEFINE_NOT_RELEASE(50004, "exist sub process definition not online", "存在子工作流定义不是上线状态"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(50006,
            "the status of process instance {0} is {1},Cannot perform {2} operation",
            "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
    SUB_PROCESS_INSTANCE_NOT_EXIST(50007, "the task belong to process instance does not exist", "子工作流实例不存在"),
    PROCESS_DEFINE_NOT_ALLOWED_EDIT(50008, "process definition {0} does not allow edit", "工作流定义[{0}]不允许修改"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(50009, "process instance {0} is executing the command, please wait ...",
            "工作流实例[{0}]正在执行命令，请稍等..."),
    PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE(50010, "process instance {0} is not sub process instance",
            "工作流实例[{0}]不是子工作流实例"),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    PROCESS_NODE_HAS_CYCLE(50019, "process node has cycle", "流程节点间存在循环依赖"),
    PROCESS_NODE_S_PARAMETER_INVALID(50020, "process node {0} parameter invalid", "流程节点[{0}]参数无效"),
    PROCESS_DEFINE_STATE_ONLINE(50021, "process definition [{0}] is already online", "工作流定义[{0}]已上线"),
    DELETE_PROCESS_DEFINE_BY_CODE_ERROR(50022, "delete process definition by code error", "删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR(50026, "batch delete process definition by codes error: {0}",
            "批量删除工作流定义错误: {0}"),
    DELETE_PROCESS_DEFINE_BY_CODES_ERROR(50026, "delete process definition by codes error: {0}",
            "删除工作流定义错误: {0}"),
    IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),
    CREATE_PROCESS_TASK_RELATION_ERROR(50032, "create process task relation error", "创建工作流任务关系错误"),
    PROCESS_DAG_IS_EMPTY(50035, "process dag is empty", "工作流dag是空"),
    CHECK_PROCESS_TASK_RELATION_ERROR(50036, "check process task relation error", "工作流任务关系参数错误"),
    CREATE_TASK_DEFINITION_ERROR(50037, "create task definition error", "创建任务错误"),
    UPDATE_TASK_DEFINITION_ERROR(50038, "update task definition error", "更新任务定义错误"),
    BATCH_EXECUTE_PROCESS_INSTANCE_ERROR(50058, "change process instance status error: {0}", "修改工作实例状态错误: {0}"),
    DELETE_PROCESS_DEFINE_ERROR(50060, "delete process definition [{0}] error: {1}", "删除工作流定义[{0}]错误: {1}"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ProcessStatus(int code, String enMsg, String zhMsg) {
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
