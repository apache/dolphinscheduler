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

public enum EnvironmentStatus implements Status {

    CREATE_ENVIRONMENT_ERROR(120001, "create environment error", "创建环境失败"),
    ENVIRONMENT_NAME_EXISTS(120002, "this environment name [{0}] already exists", "环境名称[{0}]已经存在"),
    ENVIRONMENT_NAME_IS_NULL(120003, "this environment name shouldn't be empty.", "环境名称不能为空"),
    ENVIRONMENT_CONFIG_IS_NULL(120004, "this environment config shouldn't be empty.", "环境配置信息不能为空"),
    UPDATE_ENVIRONMENT_ERROR(120005, "update environment [{0}] info error", "更新环境[{0}]信息失败"),
    DELETE_ENVIRONMENT_ERROR(120006, "delete environment error", "删除环境信息失败"),
    DELETE_ENVIRONMENT_RELATED_TASK_EXISTS(120007, "this environment has been used in tasks,so you can't delete it.",
            "该环境已经被任务使用，所以不能删除该环境信息"),
    QUERY_ENVIRONMENT_BY_NAME_ERROR(1200008, "not found environment name [{0}] ", "查询环境名称[{0}]不存在"),
    QUERY_ENVIRONMENT_BY_CODE_ERROR(1200009, "not found environment code [{0}] ", "查询环境编码[{0}]不存在"),
    ENVIRONMENT_WORKER_GROUPS_IS_INVALID(130015, "environment worker groups is invalid format", "环境关联的工作组参数解析错误"),
    UPDATE_ENVIRONMENT_WORKER_GROUP_RELATION_ERROR(130016,
            "You can't modify the worker group, because the worker group [{0}] and this environment [{1}] already be used in the task [{2}]",
            "您不能修改工作组选项，因为该工作组 [{0}] 和 该环境 [{1}] 已经被用在任务 [{2}] 中"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    EnvironmentStatus(int code, String enMsg, String zhMsg) {
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
