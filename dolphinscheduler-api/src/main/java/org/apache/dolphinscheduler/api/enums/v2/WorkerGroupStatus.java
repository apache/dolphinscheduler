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

public enum WorkerGroupStatus implements Status {

    DELETE_WORKER_GROUP_BY_ID_FAIL(10145,
            "delete worker group by id fail, for there are {0} process instances in executing using it",
            "删除Worker分组失败，有[{0}]个运行中的工作流实例正在使用"),
    DELETE_WORKER_GROUP_NOT_EXIST(10174, "delete worker group not exist ", "删除worker分组不存在"),
    WORKER_ADDRESS_INVALID(10177, "worker address {0} invalid", "worker地址[{0}]无效"),
    DELETE_WORKER_GROUP_BY_ID_FAIL_ENV(1400005,
            "delete worker group fail, for there are [{0}] enviroments using:{1}", "删除工作组失败，有 [{0}] 个环境正在使用：{1}"),

    WORKER_GROUP_DEPENDENT_TASK_EXISTS(1401000,
            "You can not modify or remove this worker group, cause it has [{0}] dependent tasks like :{1}",
            "不能修改或删除该Worker组，有 [{0}] 个任务正在使用：{1}"),

    WORKER_GROUP_DEPENDENT_SCHEDULER_EXISTS(1401001,
            "You can not modify or remove this worker group, cause it has [{0}] dependent workflow timings like :{1}",
            "不能修改或删除该Worker组，有 [{0}] 个工作流定时正在使用：{1}"),

    WORKER_GROUP_DEPENDENT_ENVIRONMENT_EXISTS(1401002,
            "You can not modify or remove this worker group, cause it has [{0}] dependent environments.",
            "不能修改或删除该Worker组，有 [{0}] 个环境配置正在使用"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    WorkerGroupStatus(int code, String enMsg, String zhMsg) {
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
