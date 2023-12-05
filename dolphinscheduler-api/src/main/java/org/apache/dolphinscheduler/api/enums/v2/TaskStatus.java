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
    TASK_INSTANCE_HOST_IS_NULL(10191, "task instance host is null", "任务实例host为空"),
    EXECUTE_NOT_DEFINE_TASK(10206, "please save and try again",
            "请先保存后再执行"),
    START_TASK_INSTANCE_ERROR(50059, "start task instance error", "运行任务流实例错误"),
    TASK_PARALLELISM_PARAMS_ERROR(50080, "task parallelism parameter is not valid", "任务并行度参数无效"),
    TASK_COMPLEMENT_DATA_DATE_ERROR(50081, "The range of date for complementing date is not valid", "补数选择的日期范围无效"),
    TASK_GROUP_QUEUE_ALREADY_START(130017, "task group queue already start", "节点已经获取任务组资源"),;
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
