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

public enum TaskStatus implements Status {

    TASK_TIMEOUT_PARAMS_ERROR(10002, "task timeout parameter is not valid", "任务超时参数无效"),
    TASK_INSTANCE_NOT_FOUND(10008, "task instance not found", "任务实例不存在"),
    TASK_INSTANCE_NOT_EXISTS(10020, "task instance {0} does not exist", "任务实例[{0}]不存在"),
    TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE(10021, "task instance {0} is not sub process instance", "任务实例[{0}]不是子流程实例"),
    FORCE_TASK_SUCCESS_ERROR(10165, "force task success error", "强制成功任务实例错误"),
    TASK_INSTANCE_STATE_OPERATION_ERROR(10166,
            "the status of task instance {0} is {1},Cannot perform force success operation",
            "任务实例[{0}]的状态是[{1}]，无法执行强制成功操作"),
    TASK_INSTANCE_HOST_IS_NULL(10191, "task instance host is null", "任务实例host为空"),
    DELETE_TASK_USE_BY_OTHER_FAIL(10194, "delete task {0} fail, cause used by other tasks: {1}",
            "删除任务 {0} 失败，被其他任务引用：{1}"),
    EXECUTE_NOT_DEFINE_TASK(10206, "please save and try again",
            "请先保存后再执行"),
    TASK_INSTANCE_NOT_DYNAMIC_TASK(10213, "task instance {0} is not dynamic", "任务实例[{0}]不是Dynamic类型"),
    TASK_DEFINE_NOT_EXIST(50030, "task definition [{0}] does not exist", "任务定义[{0}]不存在"),
    CREATE_TASK_DEFINITION_ERROR(50037, "create task definition error", "创建任务错误"),
    UPDATE_TASK_DEFINITION_ERROR(50038, "update task definition error", "更新任务定义错误"),
    SWITCH_TASK_DEFINITION_VERSION_ERROR(50040, "Switch task definition version error", "切换任务版本出错"),
    DELETE_TASK_DEFINITION_VERSION_ERROR(50041, "delete task definition version error", "删除任务历史版本出错"),
    DELETE_TASK_DEFINE_BY_CODE_ERROR(50042, "delete task definition by code error", "删除任务定义错误"),
    DELETE_TASK_PROCESS_RELATION_ERROR(50048, "delete process task relation error", "删除工作流任务关系错误"),
    TASK_DEFINE_STATE_ONLINE(50050, "task definition [{0}] is already online", "任务定义[{0}]已上线"),
    TASK_HAS_DOWNSTREAM(50051, "Task exists downstream [{0}] dependence", "任务存在下游[{0}]依赖"),
    NOT_SUPPORT_UPDATE_TASK_DEFINITION(50056, "task state does not support modification", "当前任务不支持修改"),
    TASK_DEFINITION_NOT_MODIFY_ERROR(50057, "task [{0}] definition not modify error", "任务[{0}]定义未修改错误"),
    START_TASK_INSTANCE_ERROR(50059, "start task instance error", "运行任务流实例错误"),
    CREATE_TASK_DEFINITION_LOG_ERROR(50061, "create task definition log {0} error", "创建任务操作记录 {0} 错误"),
    DELETE_TASK_DEFINE_BY_CODE_MSG_ERROR(50062, "delete task definition {0} error", "删除任务定义 {0} 错误"),
    TASK_DEFINITION_NOT_CHANGE(50063, "task definition {0} do not change", "任务定义 {0} 没有变化"),
    TASK_DEFINITION_NOT_EXISTS(50064, "task definition {0} do not exists", "任务定义 {0} 不存在"),
    TASK_PARALLELISM_PARAMS_ERROR(50080, "task parallelism parameter is not valid", "任务并行度参数无效"),
    TASK_COMPLEMENT_DATA_DATE_ERROR(50081, "The range of date for complementing date is not valid", "补数选择的日期范围无效"),
    TASK_GROUP_NAME_EXSIT(130001, "this task group name is repeated in a project", "该任务组名称在一个项目中已经使用"),
    TASK_GROUP_SIZE_ERROR(130002, "task group size error", "任务组大小应该为大于1的整数"),
    TASK_GROUP_STATUS_ERROR(130003, "task group status error", "任务组已经被关闭"),
    CREATE_TASK_GROUP_ERROR(130008, "create task group error", "创建任务组错误"),
    UPDATE_TASK_GROUP_ERROR(130009, "update task group list error", "更新任务组错误"),
    TASK_GROUP_QUEUE_ALREADY_START(130017, "task group queue already start", "节点已经获取任务组资源"),
    TASK_GROUP_STATUS_CLOSED(130018, "The task group has been closed.", "任务组已经被关闭"),
    TASK_GROUP_STATUS_OPENED(130019, "The task group has been opened.", "任务组已经被开启"),
    ;
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